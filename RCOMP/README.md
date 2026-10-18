# RCOMP Sprint 3 — Projeto 2

## Planning & Arquitetura de Rede

> Projeto de Redes de Computadores (RCOMP) — Desenvolvimento de aplicações distribuídas usando Berkeley Sockets API.

---

## Como correr a US044 (cliente + servidor)

**Guia passo a passo para a equipa (Windows / macOS / Linux / VM DEI):** [`US044-GUIA.md`](GUIDE/US044-GUIDE.md)

---

# Objetivo do Sprint

Nesta Sprint 3, o foco principal é o desenvolvimento de aplicações de rede para acesso remoto ao sistema AISafe utilizando:

* **TCP sockets** para comunicação cliente-servidor;
* **UDP datagrams** para logging distribuído;
* **HTTP + AJAX** para visualização remota de logs;
* **Autenticação e autorização** no acesso remoto.

---

# User Stories Envolvidas

| User Story  | Descrição                                             |
|-------------|-------------------------------------------------------|
| US44        | Remote access para Weather Person                     |
| US78        | Remote access para Air Transport Company Collaborator |
| US90        | External logging de acessos remotos                   |
| US91        | Visualização web dos logs remotos                     |

---

# Tecnologias e Decisões Técnicas

## Linguagem de Programação

### Escolha: **C**

### Justificação

* Utilização direta da Berkeley Sockets API;
* Maior controlo sobre comunicação TCP/UDP;
* Melhor compreensão do funcionamento de sockets e protocolos;
* Linguagem tradicionalmente utilizada em RCOMP/SCOMP;
* Maior proximidade ao funcionamento do sistema operativo e da rede.

---

# Arquitetura Geral


+------------------------+
| Weather Person Client  |
|        (TCP)           |
+-----------+------------+
            |
            v
+------------------------+
|     AISafe Server      |
|  TCP Server Embedded   |
+-----------+------------+
            |
            | UDP Logs
            v
+------------------------+
| Remote Logging Server  |
|    UDP + HTTP Server   |
+-----------+------------+
            |
            v
+------------------------+
| Web Browser (AJAX)     |
+------------------------+


---

# Planeamento do Projeto

# Fase 1 — Desenho do Protocolo de Aplicação

## Objetivo

Definir o protocolo de comunicação entre clientes TCP e o servidor AISafe.

---

## Decisão TCP — Sincronização de Mensagens

### Escolha: **Prefixação de tamanho**

Cada mensagem TCP será enviada no seguinte formato:

```text
[4 bytes - tamanho][payload]
```

### Exemplo

```text
[00000025]LOGIN;user1;password
```

### Vantagens

* Evita problemas de fragmentação TCP;
* Permite mensagens variáveis;
* Mais robusto que delimitadores;
* Fácil implementação em C usando `recv()` e buffers dinâmicos.

---

## Formato das Mensagens TCP

### Estrutura Geral

```text
COMMAND;param1;param2;...
```

### Exemplos

#### Login

```text
LOGIN;username;password
```

#### Upload de Weather Data

```text
UPLOAD_WEATHER;porto;20C;clear
```

#### Logout

```text
LOGOUT;username
```

---

## Formato dos Logs UDP (US90)

### Estrutura do Datagrama

```text
timestamp;username;ip;port;service;event
```

### Exemplo

```text
2026-05-19T14:22:10Z;john;192.168.1.10;52341;US44;LOGIN_SUCCESS
```

---

# Fase 2 — Implementação do Servidor Central

## Responsabilidades

O servidor AISafe deve:

* Aceitar múltiplas conexões TCP;
* Autenticar utilizadores;
* Autorizar acessos por role;
* Processar comandos remotos;
* Enviar logs UDP para o Logging Server.

---

## Componentes Principais

### TCP Server

Responsável por:

* aceitar clientes;
* criar processos/threads por conexão;
* gerir sessões.

### Authentication Service

Responsável por:

* validar credenciais;
* validar permissões.

### UDP Logging Client

Responsável por:

* enviar eventos remotos para o servidor de logs.

---

# Fase 3 — Clientes TCP

# US44 — Weather Person Client

## Funcionalidades

* Login remoto;
* Upload de weather data;
* Consulta de dados;
* Logout.

---

# US78 — Air Transport Company App

## Funcionalidades

* Login remoto;
* Gestão de operações aéreas;
* Acesso às funcionalidades existentes;
* Logout.

---

## Regras Importantes

### O cliente NÃO pode:

* aceder diretamente à base de dados;
* executar lógica de negócio localmente.

### Toda a comunicação deve:

* passar exclusivamente pelo socket TCP.

---

# Fase 4 — Logging Server + HTTP Visualization

# US90 — Remote Accesses Logging Server

## Funcionalidades

* Receber datagramas UDP;
* Guardar logs em memória ou ficheiro;
* Registar:

    * logins válidos;
    * logins inválidos;
    * disconnects;
    * logouts.

---

# US91 — HTTP Server + AJAX

## Páginas Necessárias

### `/events`

Lista dos últimos eventos registados.

### `/active-users`

Lista de utilizadores atualmente ativos.

---

## Atualização em Tempo Real

### Tecnologia Escolhida

* HTML + CSS básico;
* JavaScript `fetch()`;
* AJAX polling periódico.

---

## Exemplo AJAX

```javascript
setInterval(async () => {
    const response = await fetch('/events-data');
    const data = await response.text();
    document.getElementById('events').innerHTML = data;
}, 3000);
```

---

# Fase 5 — Deployment e Demonstração

## Objetivos

* Deploy dos servidores numa cloud;
* Testes de conectividade remota;
* Demonstração distribuída.

---

## Infraestrutura

| Componente        | Localização         |
| ----------------- | ------------------- |
| AISafe TCP Server | Cloud VM            |
| Logging Server    | Cloud VM            |
| Clientes TCP      | Computadores locais |
| Browser HTTP      | Computador local    |

---

# Distribuição da Equipa

| Membro   | Responsabilidade                    |
| -------- | ----------------------------------- |
| Membro 1 | Protocolo TCP + Authentication      |
| Membro 2 | Weather Person Client (US44)        |
| Membro 3 | Air Transport Company Client (US78) |
| Membro 4 | UDP Logging Server (US90)           |
| Membro 5 | HTTP + AJAX Visualization (US91)    |

---

# Segurança

## Autenticação

* Login obrigatório;
* Sessão associada ao socket TCP;
* Verificação de roles.

---

## Autorização

| Role             | Permissões           |
| ---------------- | -------------------- |
| Weather Person   | Weather operations   |
| ATC Collaborator | Airline operations   |
| Administrator    | Visualização de logs |

---

# Estrutura Recomendada do Projeto

```text
/src
 ├── server
 │    ├── tcp
 │    ├── auth
 │    ├── logging
 │    └── http
 │
 ├── client
 │    ├── weather
 │    └── atc
 │
 ├── protocol
 │
 └── common
```

---

# Estratégia de Demonstração

## Cenário Demonstrado

1. Cliente Weather faz login;
2. Upload de weather data;
3. Servidor envia log UDP;
4. Logging Server recebe evento;
5. Página web atualiza automaticamente;
6. Logout;
7. Novo log aparece em tempo real.

---

# Critérios Importantes do Enunciado

## Obrigatórios

* Comunicação TCP cliente-servidor;
* Uso obrigatório de Berkeley Sockets API;
* Sem acesso direto à base de dados;
* Logging remoto via UDP;
* HTTP server no logging server;
* AJAX sem refresh da página;
* Demonstração distribuída em diferentes nós de rede.

---

# Riscos Técnicos

| Risco                  | Mitigação                     |
| ---------------------- | ----------------------------- |
| Fragmentação TCP       | Prefixação de tamanho         |
| Concorrência           | Threads/processos por cliente |
| Perda de logs UDP      | Buffer + retries opcionais    |
| Sessões inválidas      | Timeout e heartbeat           |
| Falhas de autenticação | Logging obrigatório           |

---

# Conclusão

Esta Sprint introduz:

* comunicação distribuída;
* protocolos aplicacionais;
* sockets TCP/UDP;
* sistemas cliente-servidor;
* visualização web em tempo real.

O foco principal será garantir:

* robustez da comunicação;
* segurança;
* sincronização correta;
* demonstração funcional em ambiente distribuído.

```
```
