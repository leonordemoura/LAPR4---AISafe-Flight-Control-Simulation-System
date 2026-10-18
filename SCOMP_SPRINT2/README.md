# AISafe — Sistema de Simulação de Voos (Componente SCOMP)

## Contexto

Este repositório contém a implementação em **C** do subsistema de simulação de voos desenvolvido no âmbito do projeto integrador AISafe (ISEP LEI — 4.º semestre, 2025/2026). O foco é a simulação multiprocesso de voos, deteção de violações de segurança em tempo real e geração de relatórios.

O sistema simula múltiplos voos em simultâneo dentro de uma área geográfica definida, deteta violações de segurança entre aeronaves e produz um relatório estruturado no final de cada execução.

---

## Arquitetura Geral

processo principal (controlador)
│
├── fork() ──► processo de voo #1 (pipe → posições)
├── fork() ──► processo de voo #2 (pipe → posições)
└── fork() ──► processo de voo #N (pipe → posições)


Cada voo corre como um processo filho independente e envia estruturas `FlightPosition` ao processo pai através de um pipe dedicado. O processo pai lê as posições, guarda o histórico, verifica violações de separação e termina os filhos de forma controlada.

---

## Estrutura do Projeto

├── src/
│ ├── main.c              # Ponto de entrada — menu e execução da simulação
│ ├── simulation.c        # Ciclo principal, gestão de processos, sinais
│ ├── flight.c            # Lógica do processo filho — emissão de posições
│ ├── safetyViolation.c   # Deteção de violações e verificação de combustível
│ ├── validator.c         # Validação de configuração e regras
│ ├── config_loader.c     # Parser de ficheiros .cfg (key=value)
│ ├── json_loader.c       # Parser de planos de voo JSON (cJSON)
│ ├── reportGeneration.c  # Geração de relatórios da simulação
│ └── util.c              # Utilitários: timestamps, Haversine, velocidade
│
├── include/
│ ├── common.h        # Estruturas de dados partilhadas
│ ├── controller.h    # API pública da simulação e validação
│ ├── config_loader.h
│ ├── flight.h
│ └── util.h
│
├── config/
│ ├── simulation.cfg     # Parâmetros da simulação
│ └── safety_rules.cfg   # Regras de separação e limites
│
├── src/
│ ├── Flight_Plan_v0c.json   # Plano de voo único
│ ├── Flight_Plan_v2.json    # Dois voos idênticos
│ ├── Flight_Plan_v3.json    # Cenário de falta de combustível
├ ── Flight_Plan_v4.json     # Voos sobrepostos (violação) - apenas 2 voos
│ └── Flight_Plan_v3.json    # Voos sobrepostos (violação) - número elevado de voos
│
├── reports/               # Relatórios gerados automaticamente
├── log/                   # Ficheiro de log da simulação
├── tests/                 # Testes
├── documentation/         # Documentação
├── Makefile
└── setup.sh     # Script de instalação de dependências (cJSON)

---

## Dependências

| Dependência                                  | Finalidade                           |
|---------------------------------------------|--------------------------------------|
| `cJSON`                                     | Parsing de planos de voo JSON        |
| `libm`                                      | `hypot`, `sqrt`, `atan2` (Haversine) |
| POSIX (`unistd.h`, `signal.h`, `sys/wait.h`)| Processos, pipes e sinais            |

## Instalação do cJSON (Ubuntu/Debian)

```bash
./setup.sh
```

---

## Build

```bash
make
```

Isto gera o executável simulation na raiz do projeto.

Para limpar ficheiros de compilação:

```bash
make clean
```

Para correr testes:

```bash
make tests
```

---

## Configuração

### `config/simulation.cfg`

```
startTime       = 0
endTime         = 300
minX            = -90.0
maxX            = 90.0
minY            = -180.0
maxY            = 180.0
weatherCondition = WEATHER_CLEAR
simulationSpeed  = 0.0
updateInterval   = 1.0
```

| Chave                         | Descrição                                  |
| ----------------------------- | ------------------------------------------ |
| `startTime` / `endTime`       | Duração da simulação em segundos           |
| `minX`–`maxX` / `minY`–`maxY` | Limites geográficos (coordenadas)          |
| `weatherCondition`            | WEATHER_CLEAR, WEATHER_RAIN, WEATHER_STORM |
| `simulationSpeed`             | Tempo de pausa entre passos                |
| `updateInterval`              | Intervalo de atualização de posição        |

### `config/safety_rules.cfg`

```
horizontal_separation = 0.5
vertical_separation   = 300.0
violation_threshold   = 3
```

| Chave                   | Descrição                                                 |
| ----------------------- | --------------------------------------------------------- |
| `horizontal_separation` | Distância mínima horizontal (graus)                       |
| `vertical_separation`   | Distância mínima vertical (pés ou metros, conforme dados) |
| `violation_threshold`   | Número de violações para paragem antecipada               |

---

## Execução

```bash
./app
```

---

## Formato do Plano de Voo (JSON)

```json
{
  "Flights": [
    {
      "ID": 1,
      "Route": "LIS-JFK",
      "DepartureTime": "08:00",
      "Leg": [
        {
          "Departure": "LIS",
          "Arrival": "JFK",
          "Fuel": { "Quantity": 50000 },
          "Segments": [
            {
              "Mode": "climb",
              "Start": { "Latitude": 38.7, "Longitude": -9.1, "Altitude": { "Quantity": 0, "Unit": "ft" } },
              "End":   { "Latitude": 39.0, "Longitude": -10.0, "Altitude": { "Quantity": 35000, "Unit": "ft" } }
            }
          ]
        }
      ]
    }
  ]
}
```

Modos suportados: `climb`, `cruise`, `descend`.

---

## Output

Após cada simulação são gerados automaticamente:

**`log/simulation.txt`** — log em tempo real com eventos e violações.

**`reports/report_YYYY-MM-DD_HH:MM:SS.txt`** — relatório completo contendo:
* Total de voos e violações
* Histórico de posições por voo
* Detalhes de violações (distâncias, posições, velocidade)
* Pontuação por voo
* Resultado final (SUCESSO / FALHADO / ABORTADO)

---

## Gestão de Sinais

| Sinal     | Direção          | Handler                         | Efeito                                           |
| --------- | ---------------- | ------------------------------- | ------------------------------------------------ |
| `SIGUSR1` | Pai → Filho      | `flight.c :: handle_SIGUSR1`    | Aviso para stderr; bloqueio de sinais no handler |
| `SIGUSR2` | Pai → Filho      | `flight.c :: handler_SIGUSR2`   | Ativa `g_next_step = 1`                          |
| `SIGTERM` | Pai → Filho      | `flight.c :: handler_SIGTERM`   | Termina voo de forma controlada                  |
| `SIGINT`  | Utilizador → Pai | `simulation.c :: handle_SIGINT` | Encerramento gracioso da simulação               |

---

## User Stories Implementadas

| US    | Título                                       |
| ----- | -------------------------------------------- |
| US100 | Simular voos numa área definida              |
| US101 | Capturar e processar movimentos dos voos     |
| US102 | Detetar violações de segurança em tempo real |
| US103 | Sincronizar execução com passos de tempo     |
| US109 | Gerar relatório da simulação                 |


---

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

---
# Auto-avaliação da contribuição de cada elemento do grupo no desenvolvimento da solução (0 – 100%)

## Elementos do grupo
- Leonor De Moura - 1240914
- Ana Sofia Carvalho - 1240898
- Francisco Sá - 1241341
- Mariana Moreira - 1240905

Soma = 100%
---

## 1. Autoavaliação

### Leonor De Moura
- Autoavaliação: 25%

### Ana Sofia Carvalho
- Autoavaliação: 25%

### Francisco Sá
- Autoavaliação: 25%

### Mariana Moreira
- Autoavaliação: 25%

---

## 2. Avaliação entre pares

### Leonor De Moura
- Ana Sofia Carvalho → 25%
- Francisco Sá → 25%
- Mariana Moreira → 25%

**Média atribuída a Leonor De Moura:** 25%

---

### Ana Sofia Carvalho
- Leonor De Moura → 25%
- Francisco Sá → 25%
- Mariana Moreira → 25%

**Média atribuída a Ana Sofia Carvalho:** 25%

---

### Francisco Sá
- Leonor De Moura → 25%
- Ana Sofia Carvalho → 25%
- Mariana Moreira → 25%

**Média atribuída a Francisco Sá:** 25%

---

### Mariana Moreira
- Leonor De Moura → 25%
- Ana Sofia Carvalho → 25%
- Francisco Sá → 25%

**Média atribuída a Mariana Moreira:** 25%

---