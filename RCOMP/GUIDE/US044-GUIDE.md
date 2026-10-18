# US044 — Guia para a equipa: cliente Weather Person (TCP)

Como correr o **servidor TCP** (Java) e o **cliente** (`weather_client` em C), fazer **login** e testar operações meteorológicas.

Design: `docs/UserStories/US044/readme.md`

---

## 1. O que é o quê (não confundir máquinas)


| Componente                 | O que é                                  | Onde corre na prática                           |
| -------------------------- | ----------------------------------------- | ------------------------------------------------ |
| **Servidor TCP**           | `RemoteAccessMain` (Java, porta **9000**) | VM DEI**ou** PC com IntelliJ                     |
| **Cliente C**              | `RCOMP/weather_client`                    | **Kali (Linux)** ou **terminal Linux na VM DEI** |
| **O teu PC (Windows/Mac)** | Só para programar e**SSH** para a VM DEI | Não é o cliente C (salvo WSL, que é Linux)    |

Regra: o cliente **nunca** acede à base de dados — só fala com o servidor por TCP.

---

## 2. Escolhe UM cenário


| Cenário              | Quando usar                                  | Servidor                      | Cliente                     |
| --------------------- | -------------------------------------------- | ----------------------------- | --------------------------- |
| **[A] VM DEI**        | Teste oficial / demo do grupo (mais simples) | VM DEI (Linux)                | 2.ª SSH à VM              |
| **[B] Local (casa)**  | Servidor no teu PC                           | Windows/Mac (IntelliJ / Java) | **Kali**                    |
| **[C] Kali + VM DEI** | Demo “cliente remoto” (servidor na cloud)  | VM DEI                        | **Kali** (túnel + cliente) |

**Na VM DEI e no Kali os comandos são Linux — iguais para toda a equipa.**

**Windows vs Mac** só importa para **SSH ao servidor** na VM (secção 3). **Túnel e cliente C** → sempre **Kali/Linux** (cenário C) ou **VM Linux** (cenário A).

---

## 3. Só no teu portátil: ligar à VM DEI (Windows ou Mac)

Isto é **único** passo que muda entre colegas com Windows ou Mac.

### 3.1 Configurar atalho SSH (fazer uma vez)

Criar/editar o ficheiro de config:


| Sistema       | Caminho                             |
| ------------- | ----------------------------------- |
| Windows       | `C:\Users\<O_TEU_USER>\.ssh\config` |
| macOS / Linux | `~/.ssh/config`                     |

Conteúdo:

```text
Host dei-vm
    HostName vsgate-ssh.dei.isep.ipp.pt
    Port 10419
    User root
```

**Windows:** se `ssh` não existir → Definições → Aplicações → **Cliente OpenSSH** → Instalar.

### 3.2 Abrir ligação

```bash
ssh dei-vm
```

Password: a que o portal DEI mostra para o utilizador `root` da vossa VM.

Para **2 terminais** na VM: abre **duas** janelas (PowerShell, Terminal ou PuTTY) e corre `ssh dei-vm` em cada uma.

---

## 4. Preparação inicial (VM DEI ou PC — Linux)

Fazer **uma vez** na máquina onde vais correr o **servidor** (VM DEI ou WSL/PC com Java).

### 4.1 Clonar / atualizar código

```bash
cd /opt/sem4pi2526-sem4pi2526_2de3   # ajusta ao vosso caminho na VM
git pull
```

### 4.2 Compilar o projeto Java

Na **raiz** do repositório (onde está o `pom.xml` principal):

```bash
mvn install -DskipTests
```

**VM DEI (primeira vez):** pode ser preciso:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven build-essential
```

### 4.3 Bootstrap (utilizador de teste)

Na raiz do projeto:

```bash
cd aisafe.app.bootstrap
mvn dependency:copy-dependencies -DskipTests
java -cp "target/aisafe.app.bootstrap-1.0-SNAPSHOT.jar:target/dependency/*" aiSafe.app.bootstrap.AisafeBootstrap
cd ..
```

Credenciais criadas:


| Email                  | Password     |
| ---------------------- | ------------ |
| `weather@aisafe.local` | `Weather123` |

Área de teste: `LPPT`

---

# CENÁRIO A — Tudo na VM DEI (recomendado para a equipa)

Servidor e cliente na **mesma VM Linux**. Comandos **iguais para todos** (só repetem o SSH do passo 3).

## A.1 Terminal 1 — Servidor (deixar aberto)

```bash
ssh dei-vm
cd /opt/sem4pi2526-sem4pi2526_2de3/aisafe.app.backoffice.console
java -cp "target/classes:target/dependency/*" aiSafe.RemoteAccessMain
```

Deves ver:

```text
[US044] Remote TCP server listening on port 9000
Remote access server running. Press Enter to stop.
```

**Não carregues Enter.** Não feches esta janela.

## A.2 Terminal 2 — Cliente (outra SSH à mesma VM)

```bash
ssh dei-vm
cd /opt/sem4pi2526-sem4pi2526_2de3/RCOMP
make clean && make
chmod +x weather_client
./weather_client 127.0.0.1 9000
```

## A.3 Login e teste

1. Opção **1** — Login → `weather@aisafe.local` / `Weather123`
2. Opção **2** — Registar meteorologia (ver tabela na secção 6)
3. Opção **5** — Logout
4. Opção **0** — Sair

Se `Unable to connect` → o Terminal 1 não tem o servidor a correr.

### Verificar porta (Terminal 2)

```bash
nc -zv 127.0.0.1 9000
```

---

# CENÁRIO B — Local em casa (servidor no Windows + cliente no Kali)

**Servidor:** no PC Windows (IntelliJ ou terminal).
**Cliente:** no **Kali (VMware)** — **Linux, igual para todos** os colegas.

## B.1 Servidor no Windows

1. Em `aisafe.app.backoffice.console/src/main/resources/application.properties`:

   ```properties
   remote.tcp.port=9000
   ```
2. Correr bootstrap (secção 4.3) se ainda não existir o utilizador weather.
3. **Opção fácil — IntelliJ:** run `aiSafe.RemoteAccessMain`
   **Opção terminal — PowerShell** (na raiz do projeto, após `mvn install`):

   ```powershell
   cd aisafe.app.backoffice.console
   mvn dependency:copy-dependencies -DskipTests
   cd ..
   $cp = "aisafe.app.backoffice.console\target\aisafe.app.backoffice.console-1.0-SNAPSHOT.jar;aisafe.app.backoffice.console\target\dependency\*"
   java -cp $cp aiSafe.RemoteAccessMain
   ```
4. No Windows, confirmar porta:

   ```powershell
   Test-NetConnection 127.0.0.1 -Port 9000
   ```

## B.2 Cliente no Kali (todos igual)

```bash
cd /mnt/hgfs/Projeto/RCOMP    # ou o caminho da pasta partilhada do projeto
make clean && make
./weather_client <IP_DO_WINDOWS> 9000
```

**Importante:** no Kali **não** uses `127.0.0.1` se o servidor está no Windows — usa o IP da rede VMware, por exemplo `192.168.x.x` (no Windows: `ipconfig` → adaptador VMware).

## B.3 Login

Igual à secção A.3 (`weather@aisafe.local` / `Weather123`).

---

# CENÁRIO C — Servidor na VM DEI + cliente no Kali (demo remota)

Simula um Weather Person em **Kali (Linux)** a ligar-se ao servidor na **VM DEI**.


| #     | Onde                           | Função                         |
| ----- | ------------------------------ | -------------------------------- |
| **1** | Portátil (Windows/Mac) — SSH | Só arrancar o**servidor** na VM |
| **2** | **Kali** — terminal 1         | **Túnel SSH** (deixar aberto)   |
| **3** | **Kali** — terminal 2         | **Cliente** `weather_client`     |

**Túnel e cliente correm sempre no Kali (Linux).** Windows/Mac servem apenas para o passo 1.

> **Erro comum:** dentro da VM (`root@vs419`) **não** voltes a correr `ssh dei-vm`. O atalho `dei-vm` só existe no portátil (secção 3), não na VM.

## C.1 Janela 1 — Servidor na VM (portátil → `ssh dei-vm`)

No **PowerShell** (Windows) ou **Terminal** (Mac):

```bash
ssh dei-vm
```

Já com `root@vs419`:

```bash
cd /opt/sem4pi2526-sem4pi2526_2de3/aisafe.app.backoffice.console
java -cp "target/classes:target/dependency/*" aiSafe.RemoteAccessMain
```

Deves ver `listening on port 9000`. **Não feches** esta janela. **Não carregues Enter.**

Confirma na VM (outra SSH ou antes de arrancar o cliente no Kali):

```bash
nc -zv 127.0.0.1 9000
```

## C.2 Janela 2 — Túnel SSH no **Kali** (deixar aberto)

No **Kali**, terminal 1:

```bash
ssh -p 10419 -L 9000:127.0.0.1:9000 root@vsgate-ssh.dei.isep.ipp.pt
```

Password: root do portal DEI. Ficas em `root@vs419` — **não feches** este terminal.

*(Opcional: no Kali, em `~/.ssh/config`, podes usar o mesmo bloco `Host dei-vm` da secção 3 e então `ssh dei-vm -L 9000:127.0.0.1:9000`.)*

## C.3 Janela 3 — Cliente no **Kali** (outro terminal)

No **Kali**, terminal 2 (novo separador):

```bash
nc -zv 127.0.0.1 9000
```

Tem de dar **succeeded**. Depois:

```bash
cd /mnt/hgfs/Projeto/RCOMP    # ajusta ao teu caminho
make clean && make
./weather_client 127.0.0.1 9000
```

Aqui `127.0.0.1` é correto: o túnel do **Kali** encaminha para a porta 9000 da VM.

Login: igual A.3 (`weather@aisafe.local` / `Weather123`).

---

## 5. Menu do cliente

```text
1. Login
2. Register weather data
3. Consult weather data
4. Import bulk weather data
5. Logout
0. Exit
```

---

## 6. Exemplo — registar meteorologia (opção 2)


| Campo                  | Exemplo            |
| ---------------------- | ------------------ |
| Area                   | `LPPT`             |
| Start                  | `2026-05-28T10:00` |
| End                    | `2026-05-28T11:00` |
| Min / Max latitude     | `38.5` / `39.0`    |
| Min / Max longitude    | `-9.5` / `-9.0`    |
| Min / Max altitude (m) | `100` / `5000`     |
| Source                 | `IPMA`             |
| Wind dir / speed       | `180` / `12`       |
| Wind unit              | `MPS`              |
| Temp / unit            | `18` / `C`         |
| Pressure / unit        | `1013` / `HPA`     |

Sucesso: `Success: OK;WEATHER_REGISTERED;...`

---

## 7. Problemas frequentes


| Erro                                      | O que fazer                                                                            |
| ----------------------------------------- | -------------------------------------------------------------------------------------- |
| `Unable to connect`                       | Servidor parado (C.1) ou túnel Kali fechado (C.2); no C usa sempre`127.0.0.1` no Kali |
| `ERR;AUTH_FAILED`                         | Correr bootstrap (secção 4.3)                                                        |
| `ERR;SESSION_REQUIRED`                    | Fazer login (opção 1) primeiro                                                       |
| `Permission denied` no `./weather_client` | `chmod +x weather_client` ou `make clean && make` na VM/Kali                           |
| Kali não liga a`127.0.0.1` (cenário C)  | Túnel tem de estar no**Kali** (C.2), não no Windows; `nc -zv 127.0.0.1 9000` no Kali |
| Kali não liga (cenário B)               | Servidor no PC: usar IP do Windows (`ipconfig`), não `127.0.0.1` no Kali              |
| `ssh dei-vm` dentro da VM                 | Só no portátil; na VM usa`cd` e `java` diretamente                                   |
| `make: command not found` na VM           | `sudo apt install -y build-essential`                                                  |
| SSH pede password sempre                  | Normal; o ficheiro`config` só poupa escrever host/porta                               |

---

## 8. Ficheiros do US044

```text
RCOMP/
  US044-GUIA.md
  Makefile
  include/protocol.h
  src/common/protocol.c
  src/client/weather/weather_client.c

aisafe.app.backoffice.console/
  src/main/java/aiSafe/RemoteAccessMain.java
  src/main/java/aiSafe/infrastructure/remote/
  src/main/resources/application.properties
```
