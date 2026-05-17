# Guia de Configuracao do Ambiente

Este guia conduz qualquer pessoa a executar o projeto localmente, do zero.

---

## Pre-requisitos

| Ferramenta | Versao minima | Verificacao |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Git | qualquer | `git --version` |
| Conta Google | - | Para criar projeto Firebase |

> **Alternativa ao Maven instalado:** o projeto inclui o Maven Wrapper (`mvnw`). Use `./mvnw` no lugar de `mvn` em todos os comandos abaixo.

---

## 1. Clonar o repositorio

```bash
git clone https://github.com/Pedro-Iranldo/Squad-20---Arena-Pernambuco.git
cd Squad-20---Arena-Pernambuco
```

---

## 2. Configurar o Firebase

O projeto usa **Firebase Realtime Database** como banco de dados. Siga os passos abaixo.

### 2.1 Criar o projeto no Firebase

1. Acesse [console.firebase.google.com](https://console.firebase.google.com)
2. Clique em **Adicionar projeto**
3. Escolha um nome (ex: `arena-pernambuco-local`)
4. Desative o Google Analytics se nao precisar e conclua a criacao

### 2.2 Ativar o Realtime Database

1. No menu lateral, va em **Compilacao > Realtime Database**
2. Clique em **Criar banco de dados**
3. Escolha a regiao (recomendado: `us-central1`)
4. Inicie no **modo de teste** (regras abertas por 30 dias — suficiente para desenvolvimento)
5. Copie a **URL do banco** (formato: `https://<projeto>-default-rtdb.firebaseio.com`)

### 2.3 Gerar a chave de conta de servico

1. Va em **Configuracoes do projeto** (engrenagem no canto superior esquerdo)
2. Aba **Contas de servico**
3. Clique em **Gerar nova chave privada**
4. Salve o arquivo `.json` baixado

---

## 3. Configurar as credenciais localmente

Voce tem duas formas de fornecer as credenciais ao projeto. Escolha uma.

### Opcao A — Arquivo no classpath (mais simples)

Renomeie o arquivo `.json` baixado para:

```
projeto-arena-pernambuco-firebase-adminsdk-fbsvc-6910d1e348.json
```

Coloque-o em:

```
src/main/resources/
```

> Este arquivo esta no `.gitignore` e nao sera comitado.

### Opcao B — Variavel de ambiente (recomendado para CI/CD)

Defina a variavel de ambiente `FIREBASE_CREDENTIALS_JSON` com o conteudo **completo** do arquivo `.json`:

**Linux/macOS:**
```bash
export FIREBASE_CREDENTIALS_JSON=$(cat /caminho/para/sua-chave.json)
```

**Windows (PowerShell):**
```powershell
$env:FIREBASE_CREDENTIALS_JSON = Get-Content "C:\caminho\para\sua-chave.json" -Raw
```

---

## 4. Configurar o arquivo de propriedades

Abra `src/main/resources/application.properties` e ajuste a URL do banco:

```properties
firebase.database.url=https://<seu-projeto>-default-rtdb.firebaseio.com
```

Se usou a **Opcao A** de credenciais, mantenha tambem:

```properties
firebase.credentials.path=/projeto-arena-pernambuco-firebase-adminsdk-fbsvc-6910d1e348.json
```

O perfil `firebase` ja e o padrao (`spring.profiles.active=firebase`). Nao e necessario alterar.

---

## 5. Executar o projeto

### Com Firebase (perfil padrao)

```bash
mvn spring-boot:run
```

ou com o wrapper:

```bash
./mvnw spring-boot:run
```

### Sem Firebase (perfil `memory` — banco em memoria, sem credenciais)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=memory
```

Ideal para testes rapidos sem configurar Firebase.

---

## 6. Acessar a aplicacao

Com o servidor rodando, acesse no navegador:

```
http://localhost:8080
```

### Credenciais de demonstracao

| Papel | E-mail | Senha |
|---|---|---|
| Participante | `participante@arena.com` | `senha123` |
| Administrador | `admin@arena.com` | `admin123` |

O visitante nao precisa de login — basta navegar pelo portal.

---

## 7. Executar os testes

```bash
mvn test
```

Os testes usam o perfil `memory` automaticamente e nao exigem Firebase configurado.

---

## 8. Build para producao

```bash
mvn clean package -DskipTests
java -jar target/arena-pernambuco-0.0.1-SNAPSHOT.jar
```

---

## Estrutura de perfis Spring

| Perfil | Banco usado | Quando usar |
|---|---|---|
| `firebase` (padrao) | Firebase Realtime Database | Desenvolvimento e producao |
| `memory` | Banco em memoria | Testes locais sem Firebase |
| `debug` | Firebase + endpoints de debug | Diagnostico local |

---

## Problemas comuns

**`IllegalStateException: Credenciais Firebase nao encontradas`**
- Verifique se o arquivo `.json` esta em `src/main/resources/` com o nome correto, ou
- Confirme que a variavel `FIREBASE_CREDENTIALS_JSON` esta definida corretamente.

**`java.lang.UnsupportedClassVersionError`**
- Sua JVM e anterior ao Java 17. Instale o JDK 17+.

**Porta 8080 em uso**
- Passe a porta como variavel: `PORT=9090 mvn spring-boot:run`

**Banco vazio apos primeiro acesso**
- Normal. O `FirebaseSeeder` popula automaticamente na primeira inicializacao quando o banco esta vazio.
