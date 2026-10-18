# US091 — Guia para a equipa: Remote Accesses Logging Server (UDP + HTTP)

Como correr o **Logging Server** (`logging_server` em C), receber eventos de login/logout via **UDP** e visualizar os dados através da interface **HTTP/Web**.

---

## 1. O que é o quê (não confundir componentes)

| Componente           | O que é                        | Onde corre              |
|:---------------------|:-------------------------------|:------------------------|
| **Logging Server**   | `logging_server` (C)           | Kali, VM DEI ou Linux   |
| **UDP Service**      | Recebe eventos de login/logout | Porta **5000**          |
| **HTTP Service**     | Disponibiliza dashboard web    | Porta **8080**          |
| **Web Browser**      | Visualização dos logs          | Browser local           |

---

## 2. Objetivo da US91

A US91 implementa um servidor independente responsável por:

1. Receber eventos de autenticação através de UDP.
2. Armazenar os eventos em memória.
3. Manter uma lista de sessões ativas.
4. Disponibilizar os dados através de uma API HTTP.
5. Apresentar os dados numa página web dinâmica.

Arquitetura:

```text
┌──────────────────┐
│ TCP Clients      │
│ (US044, US078…)  │
└────────┬─────────┘
         │
         │ UDP
         ▼
┌──────────────────┐
│ Logging Server   │
│ UDP Port 5000    │
└────────┬─────────┘
         │
         │ API HTTP
         ▼
┌──────────────────┐
│ Browser          │
│ HTTP Port 8080   │
└──────────────────┘
```

---

## 3. Compilar

Na pasta `RCOMP`:

```bash
make logging_server
```

Ou:

```bash
make all
```

Limpar e recompilar:

```bash
make clean
make all
```

---

## 4. Executar o servidor

Na pasta `RCOMP`:

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

---

## 5. Estrutura da solução

O servidor executa duas threads:

### Thread UDP

Escuta eventos recebidos na porta:

```text
5000/UDP
```

Exemplos:

```text
LOGIN_OK
LOGIN_FAIL
LOGOUT
DISCONNECT
```

Cada evento é guardado no repositório interno.

---

### Thread HTTP

Escuta pedidos HTTP na porta:

```text
8080/TCP
```

Disponibiliza:

```text
/
/index.html
/api/events
/api/active
```

---

## 6. API REST

### 6.1 Eventos

URL:

```text
http://127.0.0.1:8080/api/events
```

Resposta:

```json
[
  {
    "type":"LOGIN_OK",
    "timestamp":"2026-06-05 15:20:00",
    "username":"weather@aisafe.local",
    "ip":"127.0.0.1",
    "port":9000,
    "service":"Weather"
  }
]
```

Contém os últimos eventos recebidos.

---

### 6.2 Sessões Ativas

URL:

```text
http://127.0.0.1:8080/api/active
```

Resposta:

```json
[
  {
    "username":"weather@aisafe.local",
    "ip":"127.0.0.1",
    "port":9000,
    "service":"Weather",
    "login_time":"2026-06-05 15:20:00"
  }
]
```

Contém apenas utilizadores atualmente autenticados.

---

## 7. Interface Web

URL:

```text
http://127.0.0.1:8080
```

ou

```text
http://127.0.0.1:8080/index.html
```

A página web não contém dados fixos.

Os dados são carregados dinamicamente através de JavaScript.

Fluxo:

```text
Browser
   │
   ├── GET /index.html
   │
   ├── GET /api/events
   │
   └── GET /api/active
```

---

## 8. Onde é usado AJAX?

No ficheiro JavaScript da página.

Exemplo:

```javascript
fetch("/api/events")
    .then(r => r.json())
    .then(data => updateEvents(data));

fetch("/api/active")
    .then(r => r.json())
    .then(data => updateActive(data));
```

O browser envia pedidos HTTP assíncronos para a API.

Não existe refresh da página.

Apenas os dados são atualizados.

---

## 9. Como testar sem clientes

Mesmo sem os clientes dos meus colegas, é possível testar a API.

### Testar eventos

```bash
curl http://127.0.0.1:8080/api/events
```

### Testar sessões ativas

```bash
curl http://127.0.0.1:8080/api/active
```

---

## 10. Demonstração da US91

1. Arrancar:

```bash
./logging_server
```

2. Abrir:

```text
http://127.0.0.1:8080
```

3. Executar clientes remotos (US044, US078, etc.).

4. Realizar logins e logouts.

5. Verificar atualização automática dos eventos e sessões ativas no dashboard web.

---

## 11. Integração com o servidor TCP (US044/US078/US086)

O `RemoteAccessMain` envia eventos UDP (`LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `DISCONNECT`) para este servidor.

Em `aisafe.app.backoffice.console/src/main/resources/application.properties`:

```properties
remote.udp.logging.host=127.0.0.1
remote.udp.logging.port=5000
```

| Property | Valor | Notas |
|----------|-------|-------|
| `remote.udp.logging.host` | `127.0.0.1` na mesma VM | vazio = logs desactivados |
| `remote.udp.logging.port` | `5000` | tem de coincidir com `UDP_PORT` em `RCOMP/include/logging_server.h` |

**Ordem de arranque (mesma VM):**

1. `./logging_server` (secção 4)
2. `RemoteAccessMain` (Java, TCP 9000)
3. Cliente remoto (`weather_client`, `atcc_client`, `pilot_client`, …)
4. Browser: `http://127.0.0.1:8080`

Após alterar `application.properties`, recompilar o módulo backoffice e reiniciar `RemoteAccessMain`.
