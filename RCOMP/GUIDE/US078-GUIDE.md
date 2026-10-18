# US078 — Guia para a equipa: cliente ATC Collaborator (TCP)

Como correr o **servidor TCP** (Java) e o **cliente** (`atcc_client` em C), fazer **login** e gerir frotas, rotas e pilotos.

---

## 1. O que é o quê (não confundir máquinas)

| Componente       | O que é                                   | Onde corre na prática                            |
|:-----------------|:------------------------------------------|:-------------------------------------------------|
| **Servidor TCP** | `RemoteAccessMain` (Java, porta **9000**) | VM DEI ou PC com IntelliJ                        |
| **Cliente C**    | `RCOMP/atcc_client`                       | **Kali (Linux)** ou **terminal Linux na VM DEI** |

---

## 2. Escolhe UM cenário

Consultar o guia da US044 (`US044-GUIDE.md`) para detalhes sobre os cenários **A**, **B** ou **C**. Os comandos de SSH e túneis são idênticos, mudando apenas o nome do executável do cliente.

---

## 3. Preparação inicial (VM DEI ou PC — Linux)

### 3.1 Credenciais de Teste (Bootstrap)
Para esta US, deves usar um colaborador de companhia aérea:

| Email                       | Password          | Role   |
|:----------------------------|:------------------|:-------|
| `collaborator@aisafe.local` | `Collaborator123` | ATCC   |

---

## 4. Execução (Cenário A — Tudo na VM DEI)

### A.1 Terminal 1 — Servidor
```bash
ssh dei-vm
cd /opt/sem4pi2526-sem4pi2526_2de3/aisafe.app.bootstrap
mvn dependency:copy-dependencies -DskipTests
java -cp "target/aisafe.app.bootstrap-1.0-SNAPSHOT.jar:target/dependency/*" aiSafe.app.bootstrap.AisafeBootstrap
cd /opt/sem4pi2526-sem4pi2526_2de3/aisafe.app.backoffice.console
mvn dependency:copy-dependencies -DskipTests
java -cp "target/classes:target/dependency/*" aiSafe.RemoteAccessMain
```

### A.2 Terminal 2 — Cliente
```bash
ssh dei-vm
cd /opt/sem4pi2526-sem4pi2526_2de3/RCOMP
make clean && make
./atcc_client 127.0.0.1 9000
```

---

## 5. Menu do cliente

```text
  1. Login
  2. Add Aircraft
  3. Decommission Aircraft
  4. List Fleet
  5. Register Flight Route
  6. Deactivate Flight Route
  7. Add Pilot to Company
  8. List Pilot Roster
  9. Remove Pilot
  10. Logout
  0. Exit
```

---

## 6. Exemplos de Utilização

### 6.1 Adicionar Aeronave (Opção 2)
| Campo               | Exemplo             |
|:--------------------|:--------------------|
| Registration        | `CS-TESTE`          |
| Model ID            | `A320-001`          |
| Engines             | `CFM56-7B`          |
| Seats (1st/Biz/Eco) | `12` / `36` / `120` |
| Year                | `2024`              |
| Country / Code      | `Portugal` / `351`  |
| Status              | `ACTIVE`            |
| Company IATA        | `TP`                |
*Sucesso:* `SUCCESS: AIRCRAFT_ADDED;CS-TESTE`

### 6.2 Desativar Aeronave (Opção 3)
| Campo        | Exemplo     |
|:-------------|:------------|
| Registration | `CS-TESTE`  |
*Sucesso:* `SUCCESS: AIRCRAFT_DECOMMISSIONED;CS-TESTE`

### 6.3 Listar Frota (Opção 4)
| Campo                                         | Exemplo    |
|:----------------------------------------------|:-----------|
| Company IATA code                             | `TP`       |
 *Sucesso:* `SUCCESS: FLEET;CS-TUA,A320,ACTIVE;AIRCRAFT_DECOMMISSIONED,CS-TESTE `       

### 6.4 Registar Rota de Voo (Opção 5)
| Campo            | Exemplo  |
|:-----------------|:---------|
| Route name       | `TP123`  |
| Origin IATA      | `LIS`    |
| Destination IATA | `OPO`    |
*Sucesso:* `SUCCESS: ROUTE_REGISTERED;TP123`

### 6.5 Desativar Rota (Opção 6)
| Campo                      | Exemplo               |
|:---------------------------|:----------------------|
| Route name                 | `TP123`               |
| Date (yyyy-MM-ddTHH:mm:ss) | `2026-12-31T23:59:59` |
*Sucesso:* `SUCCESS: ROUTE_DEACTIVATED;TP123`

### 6.6 Adicionar Piloto (Opção 7)
| Campo              | Exemplo                |
|:-------------------|:-----------------------|
| Pilot ID           | `16`                   |
| Email              | `pilot@aisafe.local`   |
| Certs (Model,Date) | `A320-001,2024-01-01`  |
*Sucesso:* `SUCCESS: PILOT_ADDED;16`

### 6.7 Listar Roster de Pilotos (Opção 8)
Sem inputs necessários.
*Sucesso:* `SUCCESS: PILOT_ROSTER;16,pilot@aisafe.local,true,A320,B737|...`

### 6.8 Remover Piloto (Opção 9)
| Campo    | Exemplo |
|:---------|:--------|
| Pilot ID | `16`    |
*Sucesso:* `SUCCESS: PILOT_DEACTIVATED;16`

---

