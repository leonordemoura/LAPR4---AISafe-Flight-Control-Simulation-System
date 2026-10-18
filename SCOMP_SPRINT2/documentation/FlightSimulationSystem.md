# ✈️ Flight Simulation System (SO + Signals + IPC)

## Visão Geral

Este projeto implementa um sistema de simulação de voos concorrentes em C, onde:

- Cada voo é um **processo filho**
- O controlador é o **processo pai**
- A comunicação é feita via:
    - **pipes (IPC)**
    - **signals (SIGUSR1, SIGUSR2, SIGTERM, SIGINT)**
- O sistema garante:
    - sincronização por steps
    - deteção de violações de segurança
    - validação de combustível
    - geração de relatório final

---

# Arquitetura Geral
controller (pai)
│
├── flight 1 (processo filho)
├── flight 2
├── flight N


Cada flight:
- espera autorização (`SIGUSR2`)
- executa 1 step de simulação
- envia posição ao controller via pipe
- sinaliza “ready” antes de cada step

---

# ⚙️ start_simulation() 

## 1. Validações iniciais (segurança do sistema)

```c
if (!validate_rules(rules))
```

Garante que as regras de segurança são válidas antes de iniciar.

✔ evita simulação com parâmetros inválidos
✔ falha rápida (fail-fast)

```c
if (!validate_config(config))
```
Valida configuração global:
* número de voos
* tempos
* consistência estrutural

```c
if (!validate_areaBounds(&plans[i], config))
```
Garante que os planos estão dentro do espaço geográfico permitido.

```c
if (!validate_plan(&plans[i]))
```
Validação estrutural do flight plan:
* segmentos válidos
* coordenadas coerentes
* dados consistentes

```c
if (!check_fuel(&plans[i]))
```
Calcula se o combustível é suficiente para o trajeto completo.
✔ evita voos impossíveis
✔ simula restrição real de aviação

## 2. Setup de logs

mkdir("log", 0755);
cria diretório de logs se não existir
FILE *log = fopen(SIMULATION_LOG_FILE, "w");
abre ficheiro principal da simulação

## 3. Signal handling (CTRL+C)

static volatile sig_atomic_t running = 1;
variável segura para signals

void handle_SIGINT(int sig)
quando o utilizador faz CTRL+C:
a simulação para de forma limpa
✔ evita kill abrupto
✔ permite cleanup

## 4. Criação de estruturas IPC
int pipes[MAX_FLIGHTS][2];
pipe principal:
* flight → controller

int pipe_ready[MAX_FLIGHTS][2];
pipe de sincronização:
* flight → controller (“estou pronto”)

pid_t pids[MAX_FLIGHTS];
FlightHistory history[MAX_FLIGHTS];
int flight_active[MAX_FLIGHTS];
estado global dos voos:
* PID
* histórico de posições
* estado ativo/inativo

## 5. fork() — criação de processos
pid_t pid = fork();
cria processo filho

### CHILD (flight process)
close(pipes[i][0]);
close(pipe_ready[i][0]);

filho fecha leitura (não precisa)

### run_flight(...)
inicia simulação do voo

### PARENT (controller)
pids[i] = pid;
close(pipes[i][1]);
close(pipe_ready[i][1]);

pai:
* guarda PID
* fecha escrita

## 6. MAIN LOOP DA SIMULAÇÃO
while (running && active_flights > 0)

corre enquanto:
* não foi interrompido (CTRL+C)
* existirem voos ativos

### controlo de tempo
if (time(NULL) - start >= duration)

termina simulação quando tempo acaba

## 7. SINCRONIZAÇÃO (US103)
read(pipe_ready[i][0], &ready, 1);

cada flight escreve 1 byte quando está pronto
✔ garante sincronização perfeita entre processos

kill(pids[i], SIGUSR2);
controlador libera TODOS os voos para o próximo step
✔ este é o “clock da simulação”

### receção de posições
read(pipes[i][0], &pos, sizeof(pos));
recebe posição calculada pelo flight

history[i].positions[history[i].count++] = pos;
guarda histórico do voo
✔ essencial para:
* deteção de colisões
* cálculo de velocidades
* relatórios finais

## 8. DETEÇÃO DE VIOLAÇÕES (US102)
capture_violation(...)
verifica:
* distância horizontal
* distância vertical
* sincronização temporal

### O que acontece numa violação:
* incrementa contador global
* regista evento
* escreve no log
* envia SIGUSR1 para flights
kill(pids[a], SIGUSR1);
flight recebe aviso de perigo

## 9. FINALIZAÇÃO DA SIMULAÇÃO
kill(pids[i], SIGTERM);
* termina todos os flights

waitpid(...)
* garante cleanup correto dos processos

---
# FLIGHT SIDE (flight.c)
estados globais
* g_stop = 0;
* g_next_step = 0;

controla execução do voo:
* parar
* avançar step

# 1. SIGUSR2 (autoriza step)
g_next_step = 1;
desbloqueia simulação
✔ base do sistema step-by-step

# 2. SIGTERM
g_stop = 1;
termina voo imediatamente

# 3. SIGUSR1
write(STDERR_FILENO, msg,...);
apenas informa:
“violação detetada”
✔ não interrompe execução

--- 
# EXECUÇÃO DO FLIGHT (run_flight)
## 1. prepara sinais
* SIGUSR1 → aviso
* SIGTERM → stop
* SIGUSR2 → step

## 2. bloqueia SIGUSR2
sigprocmask(SIG_BLOCK, &block_mask, &old_mask);
evita race conditions

## 3. loop de segmentos
Cada segmento:
* calcula distância
* calcula passos
* divide movimento

## 4. loop de steps
for (k = 0; k <= steps; k++)
Cada step:
### 1. avisa controller
write(ready_fd, &ready, 1);

### 2. espera autorização
sigsuspend(...)
bloqueia até SIGUSR2

### 3. calcula posição
pos.x = ...
pos.y = ...
pos.altitude = ...

### 4. envia ao controller
write(write_fd, &pos, sizeof(pos));

---
# check_fuel()
fuel_needed += dist * rate;
calcula combustível total necessário
taxas:
* climb: 6.0 kg/km
* cruise: 3.2 kg/km
* descend: 2.0 kg/km

## decisão final
if (plan->fuelQuantity < fuel_needed)
rejeita voo se não tiver combustível
✔ evita simulação impossível

---
# capture_violation()
lógica principal:
1. pega última posição do flight A
2. procura posição mais próxima do flight B
3. compara:
hdist < threshold
vdist < threshold
timestamp igual

## se violação:
* incrementa contador
* guarda evento
* envia SIGUSR1
* escreve log




