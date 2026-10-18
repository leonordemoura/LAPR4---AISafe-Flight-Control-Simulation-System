# US090 — Guia para a equipa: Remote Accesses Logging Server (UDP)

Como correr o **Logging Server** (`logging_server` em C), receber eventos de autenticação através de **UDP** e verificar o armazenamento de eventos e sessões ativas.

---

## 1. O que é o quê (não confundir componentes)

| Componente          | O que é                | Onde corre                |
|:--------------------|:-----------------------|:--------------------------|
| **Logging Server**  | `logging_server` (C)   | VM DEI, Kali ou Linux     |
| **UDP Service**     | Recebe eventos remotos | Porta **5000**            |
| **Clientes TCP**    | US044, US078, etc.     | Kali ou outras máquinas   |
| **Repository**      | Estruturas em memória  | Logging Server            |

---

## 2. Objetivo da US90

A US90 implementa um servidor central responsável por:

* Receber eventos de login/logout.
* Receber eventos de falha de autenticação.
* Registar desconexões inesperadas.
* Manter histórico dos eventos.
* Manter lista de sessões ativas.
* Disponibilizar os dados para consulta pela US91.

---

## 3. Arquitetura

```text
┌──────────────────┐
│ Weather Client   │
└────────┬─────────┘
         │ UDP
         ▼
┌──────────────────┐
│ Logging Server   │
│ UDP Port 5000    │
└────────┬─────────┘
         ▲
         │ UDP
┌────────┴─────────┐
│ ATCC Client      │
└──────────────────┘
```

Todos os clientes enviam eventos para o mesmo servidor.

---

## 4. Compilar

Na pasta `RCOMP`:

```bash
make logging_server
```

Ou:

```bash
make all
```

Recompilar:

```bash
make clean
make all
```

---

## 5. Executar o servidor

```bash
./logging_server
```

Resultado esperado:

```text
=== Remote Accesses Logging Server ===

UDP port : 5000
HTTP port: 8080

[UDP] A escutar na porta 5000
[HTTP] A escutar na porta 8080
```

Embora a US90 utilize apenas UDP, o executável também inicia o servidor HTTP utilizado pela US91.

---

## 6. Eventos suportados

### LOGIN_OK

Indica autenticação com sucesso.

Exemplo:

```text
LOGIN_OK
```

Ação:

* Regista evento.
* Cria sessão ativa.

---

### LOGIN_FAIL

Indica autenticação falhada.

Exemplo:

```text
LOGIN_FAIL
```

Ação:

* Regista evento.
* Não cria sessão ativa.

---

### LOGOUT

Indica logout explícito.

Exemplo:

```text
LOGOUT
```

Ação:

* Regista evento.
* Remove sessão ativa.

---

### DISCONNECT

Indica quebra inesperada da ligação.

Exemplo:

```text
DISCONNECT
```

Ação:

* Regista evento.
* Remove sessão ativa.

---

## 7. Estruturas mantidas pelo servidor

### Histórico de eventos

Contém:

| Campo     |
| --------- |
| Tipo      |
| Timestamp |
| Username  |
| IP        |
| Porta     |
| Serviço   |

Exemplo:

```text
LOGIN_OK | 2026-06-05 15:00:00 | weather@aisafe.local
```

---

### Sessões ativas

Contém:

| Campo      |
| ---------- |
| Username   |
| IP         |
| Porta      |
| Serviço    |
| Login Time |

Exemplo:

```text
weather@aisafe.local
127.0.0.1
9000
Weather
```

---

## 8. Fluxo de funcionamento

### Login

```text
Cliente
   │
   ├─ LOGIN_OK
   ▼
Logging Server
   │
   ├─ Guarda evento
   └─ Cria sessão ativa
```

---

### Logout

```text
Cliente
   │
   ├─ LOGOUT
   ▼
Logging Server
   │
   ├─ Guarda evento
   └─ Remove sessão ativa
```

---

### Desconexão inesperada

```text
Cliente
   │
   ├─ DISCONNECT
   ▼
Logging Server
   │
   ├─ Guarda evento
   └─ Remove sessão ativa
```

---

## 9. Cenário de demonstração

### Máquina 1 (VM DEI)

```bash
./logging_server
```

---

### Máquina 2 (Kali)

Executar clientes:

```bash
./weather_client
```

ou

```bash
./atcc_client
```

---

### Operações

1. Login.
2. Logout.
3. Login inválido.
4. Encerrar cliente abruptamente.

O Logging Server deverá receber e processar todos os eventos.

---

## 10. Integração com a US91

A US90 não apresenta interface gráfica.

Os dados recolhidos ficam disponíveis para a US91 através das APIs:

```text
/api/events
/api/active
```

A US91 utiliza esses dados para construir o dashboard web.

---

## 11. Problemas frequentes

### Porta UDP ocupada

Verificar:

```bash
ss -lunp | grep 5000
```

---

### Eventos não aparecem

Verificar:

* IP correto do Logging Server.
* Porta UDP correta.
* Firewall da VM.
* Cliente configurado para enviar eventos.

---

### Sessões não desaparecem

Verificar se o cliente envia:

```text
LOGOUT
```

ou

```text
DISCONNECT
```

antes de terminar.

---

## 12. Demonstração da US90

1. Arrancar:

```bash
./logging_server
```

2. Arrancar um cliente remoto.

3. Efetuar login.

4. Efetuar logout.

5. Verificar que o evento foi registado e a sessão ativa foi atualizada.

6. Repetir com autenticação inválida e desconexão inesperada.