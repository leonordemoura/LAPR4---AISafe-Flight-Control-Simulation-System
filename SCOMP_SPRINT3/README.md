# AISafe — Flight Simulation System (SCOMP Sprint 3)

## Visão Geral

O AISafe é um sistema de simulação concorrente de tráfego aéreo desenvolvido em **C**, com foco em:

* Execução multiprocesso de voos
* Sincronização com **memória partilhada (SHM)**
* Coordenação via **semáforos POSIX**
* Deteção de violações em tempo real
* Sincronização step-by-step (**lockstep execution - US108**)
* Geração de relatórios detalhados (**US109**)

Cada voo é executado como **processo filho independente**, enquanto o controlador central gere:

* sincronização global
* deteção de conflitos
* logging
* geração de relatório final

---

#  Arquitetura do Sistema

```
controller (pai)
│
├── thread: violation detector (US106)
├── thread: report handler (US107)
│
├── fork() ──► flight 1 (processo filho)
├── fork() ──► flight 2
└── fork() ──► flight N
```
```
          ┌──────────────┐
          │ Controller    │
          │ (main process)│
          └──────┬───────┘
                 │
┌────────────────┼────────────────┐
│                │                │
violation        report          fork()
thread           thread          flights
│                │                │
└──────┬─────────┘                │
       │                          │
  pthread_mutex + cond            │
       │                          │
   SHARED MEMORY  ◄───────────────┘
       │   
SEM_MUTEX / SEM_STEP / SEM_DONE
```
---

## Comunicação e Sincronização

### Memória Partilhada (SHM)

Usada para estado global:

* posições atuais dos voos
* histórico completo
* flags de execução
* contadores de violações
* regras de segurança

---

### Semáforos POSIX

| Semáforo    | Função                          |
| ----------- | ------------------------------- |
| `SEM_MUTEX` | Proteção da SHM                 |
| `SEM_STEP`  | Sincronização de step (US108)   |
| `SEM_DONE`  | Confirmação de execução de step |

---

### Threads

| Thread           | Função                   | US    |
| ---------------- | ------------------------ | ----- |
| violation_thread | deteção de colisões      | US106 |
| report_thread    | logging + report trigger | US107 |

---

# US105 — Sistema Base (SHM + Processos + Sync)

## Objetivo

Criar infraestrutura concorrente segura:

* SHM partilhada entre processos
* semáforos POSIX
* criação de flights via fork
* controlo de estado global

---

## Fluxo principal

1. validação de configuração e regras
2. criação da SHM (`shm_open + mmap`)
3. inicialização de semáforos
4. criação de threads
5. fork dos processos flight
6. sincronização inicial (active flights)
7. execução da simulação

---

## Estados críticos

```c
shm->simulation_running = 1;
shm->current_step = 0;
shm->active[i]
```

---

# US106 — Deteção de Violações em Tempo Real

## Objetivo

Detetar conflitos entre aeronaves em execução.

---

## Funcionamento

A thread `violation_thread`:

1. copia estado da SHM
2. calcula distâncias:

```c
hdist = hypot(dx, dy)
vdist = fabs(dalt)
```

3. verifica regra:

```c
hdist < horizontal_separation &&
vdist < vertical_separation
```

---

## Ações em violação

Quando detetado conflito:

* incrementa `global_violation_count`
* regista evento (`SafetyViolationEvent`)
* ativa buffer de violação
* sinaliza thread de report
* atualiza histórico por flight

---

## Características

* deteção concorrente
* lock mínimo (copy + compute fora de mutex)
* proteção apenas em escrita

---

# US107 — Sistema de Report em Tempo Real

## Objetivo

Processar violações e gerar logs intermédios.

---

## Funcionamento

`report_thread`:

1. espera por `pthread_cond`
2. ativa quando há violação
3. copia buffer local
4. escreve log:

```
log/violations.log
```

---

## Conteúdo do log

* step da simulação
* flights ativos
* número de violações
* IDs de conflitos

---

## Sincronização

* mutex: proteção de flags
* condition variable: trigger de eventos

---

# ⏱US108 — Lockstep Synchronization

## Objetivo

Garantir execução sincronizada de todos os voos.

---

## Modelo

Cada step segue:

```
controller → libera STEP
flights → executam movimento
flights → enviam DONE
controller → espera todos DONE
```

---

## Semáforos

* `SEM_STEP` → desbloqueia execução
* `SEM_DONE` → confirmação de conclusão

---

## Ciclo do controller

```
for each step:
    post STEP para todos flights ativos
    wait DONE de todos
```

---

## Garantias

✔ todos os voos avançam simultaneamente
✔ sem race conditions em posições
✔ sincronização determinística

---

# US109 — Sistema de Relatórios

## Objetivo

Gerar relatório final completo da simulação.

---

## Geração

Executado no shutdown:

```c
generate_report(...)
```

---

## Ficheiro gerado

```
reports/report_YYYY-MM-DD_HH-MM-SS.txt
```

---

## Conteúdo do relatório

### 1. Sumário

* total de voos
* total de violações
* threshold

---

### 2. Flight histories

Para cada voo:

* número de posições
* número de violações
* performance score (%)

---

### 3. Histórico de posições

* coordenadas (x, y, altitude)
* velocidades (vx, vy, vz)

---

### 4. Violações de segurança

* flights envolvidos
* timestamp
* distância horizontal e vertical
* posições completas

---

### 5. Verdict final

| Condição           | Resultado |
| ------------------ | --------- |
| threshold atingido | FAILED    |
| shutdown normal    | SUCCEEDED |
| erro inesperado    | ABORTED   |

---

# Pipeline da Simulação

```
start_simulation()
    ↓
validations (config + rules + plans)
    ↓
SHM + semaphores init
    ↓
threads start (US106 + US107)
    ↓
fork flights
    ↓
lockstep loop (US108)
    ↓
violation detection (US106)
    ↓
logging (US107)
    ↓
shutdown
    ↓
generate_report (US109)
```

---

# Flight Process (flight.c)

Cada flight:

* recebe autorização por semáforo
* calcula posição incremental
* atualiza SHM
* guarda histórico (US109)
* sinaliza DONE

---

## Estados

```c
running → SIGTERM / SIGINT
step controlled via SEM_STEP
```

---

# Segurança e Robustez

* proteção com `sem_wait` seguro (EINTR handling)
* shutdown gracioso via SIGINT
* cleanup completo de:

    * SHM
    * semáforos
    * threads
    * processos filhos

---

# Output do Sistema

## Logs

```
log/violations.log
```

## Relatórios

```
reports/report_*.txt
```

---

# Dependências

| Biblioteca | Uso                   |
| ---------- | --------------------- |
| pthread    | threads               |
| semaphore  | sincronização IPC     |
| shm_open   | memória partilhada    |
| math.h     | cálculo de distância  |
| signal     | controlo de processos |

---

# US Implementadas

| US    | Nome                                                     |
| ----- | -------------------------------------------------------- |
| US105 | Infraestrutura concorrente (SHM + processos + semáforos) |
| US106 | Deteção de violações em tempo real                       |
| US107 | Logging + report thread                                  |
| US108 | Lockstep synchronization                                 |
| US109 | Geração de relatório final                               |

---

# Resultado Final

O sistema garante:

✔ simulação determinística
✔ concorrência segura
✔ deteção de colisões em tempo real
✔ sincronização forte entre processos
✔ relatório completo pós-simulação

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