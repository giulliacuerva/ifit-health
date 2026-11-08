# iFit Health

Sistema web para gerenciamento de atividades de um centro esportivo, com áreas específicas para administradores e alunos.

## Requisitos

Antes de executar o projeto, instale:

- **Java JDK 21**
- **Node.js** (versão compatível com Vite 8: Node 20.19+ ou 22.12+)
- **npm** (instalado junto com o Node.js)
- **IntelliJ IDEA** (opcional, para executar o backend pela IDE)


## Estrutura dos diretórios

Clone os repositórios do backend e do frontend em uma mesma pasta, mantendo esta estrutura:

```text
ifit-health-project/
├── ifit-health/     # Backend - Java / Spring Boot
└── ifit-front/      # Frontend - React / TypeScript
```
---

## 1. Como executar o backend


### Opção A: executar pelo IntelliJ IDEA

1. Abra o IntelliJ IDEA.
2. Selecione **Open** e escolha a pasta `ifit-health`
3. Aguarde o IntelliJ importar o projeto Maven e baixar as dependências.
4. Configure o **Project SDK** para Java 21, caso ainda não esteja selecionado.
5. Localize a classe principal:

   ```text
   src/main/java/br/ifsp/demo/DemoAuthAppApplication.java
   ```

6. Abra a classe `DemoAuthAppApplication` e clique no ícone de execução ao lado do método `main` ou utilize **Run 'DemoAuthAppApplication'**.


### Opção B: executar pelo terminal

Abra um terminal **na pasta `ifit-health`**:

```bash
cd ifit-health
```

Inicie o backend:

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

### Banco de dados

O projeto utiliza SQLite. O arquivo `database.db` acompanha o backend e contém a estrutura e os dados iniciais utilizados pela aplicação.

O Hibernate está configurado para validar o schema existente (`ddl-auto=validate`). Portanto, não exclua o arquivo `database.db`.

Mantenha o backend em execução enquanto utilizar o frontend.

---

## 2. Como configurar e executar o frontend

O frontend foi desenvolvido com **React**, **TypeScript**, **TanStack Start** e **Vite**.

Abra um segundo terminal na pasta `ifit-front`:

```bash
cd ifit-front
```

### Instalar as dependências

Execute:

```bash
npm install
```

Esse comando instala as dependências necessárias para executar o projeto.

### Iniciar o frontend

Após a instalação, execute:

```bash
npm run dev
```

O servidor de desenvolvimento será iniciado na porta **3000**. Acesse a aplicação pelo navegador:

```text
http://localhost:3000
```

O frontend utiliza o backend em `http://localhost:8080` por padrão. Portanto, mantenha os dois serviços em execução: backend na porta **8080** e frontend na porta **3000**.

---

## 3. Contas e autenticação

O sistema possui dois perfis de acesso: **Administrador (`ADMIN`)** e **Aluno (`USER`)**.

### Administrador

A conta administrativa é fixa e já está cadastrada no banco de dados:

| E-mail | Senha | Perfil |
|---|---|---|
| `admin@ifit.com` | `admin` | `ADMIN` |

Utilize essas credenciais na tela de login para acessar a área administrativa.

### Aluno

A conta de aluno pode ser criada normalmente pela opção **Cadastre-se**, disponível na tela inicial. Após preencher os dados solicitados e concluir o cadastro, o usuário poderá entrar no sistema com as credenciais cadastradas.

O cadastro público cria uma conta com perfil `USER`.

---

## 4. Documentação Swagger

Com o backend em execução, a documentação da API pode ser acessada pelo Swagger UI:

**Swagger UI:**

```text
http://localhost:8080/api/v1/api-docs
```

A especificação OpenAPI em formato JSON está disponível em:

```text
http://localhost:8080/api/v1/openapi
```

Pelo Swagger é possível consultar os endpoints, visualizar os parâmetros e modelos das requisições e testar as operações da API.

---

## 5. Funcionalidades

### Área administrativa (ADMIN)

Após realizar o login como administrador, o usuário tem acesso a:

- **Gerenciamento de atividades:** visualizar as atividades cadastradas.
- **Criar atividade:** cadastrar uma atividade informando modalidade, sala, professor, dias da semana, horário, capacidade e mensalidade.
- **Editar atividade:** consultar e alterar os dados de uma atividade existente.
- **Relatórios:** consultar informações das atividades em um período, com filtros opcionais por modalidade e dia da semana. Os resultados apresentam quantidade de matriculados, receita bruta e ocupação média.

### Área do aluno (USER)

Após realizar o login como aluno, o usuário tem acesso a:

- **Atividades disponíveis:** consultar as atividades oferecidas, com informações de modalidade, professor, sala, dias, horários, mensalidade e vagas disponíveis.
- **Matrícula:** selecionar uma ou mais atividades e confirmar a matrícula por meio de um modal.
- **Minhas atividades:** consultar as matrículas ativas e o histórico de atividades encerradas.
- **Cancelamento:** solicitar o cancelamento de uma matrícula ativa por meio de um modal de confirmação.

---

## 6. Visão geral do fluxo da aplicação

A autenticação identifica o perfil do usuário e direciona cada pessoa para sua área. O administrador gerencia as atividades e acompanha os resultados nos relatórios; o aluno consulta as opções disponíveis, realiza matrículas e pode cancelar sua participação. As duas áreas compartilham os dados de atividades e matrículas, de modo que as alterações administrativas e as ações dos alunos refletem na disponibilidade de vagas e nos relatórios.

```text
                         AUTENTICAÇÃO
                              |
                 +------------+------------+
                 |                         |
               ADMIN                      ALUNO
                 |                         |
        +--------+--------+       +--------+---------+
        |                 |       |                  |
    ATIVIDADES         RELATÓRIOS  DISPONÍVEIS   MINHAS ATIVIDADES
        |                         |                  |
    +---+---+                     |             +----+-----+
    |       |                     |             |          |
  CRIAR   EDITAR               MATRÍCULA     CONSULTAR  CANCELAR
                                [modal]                   [modal]
```

O fluxo mantém as responsabilidades separadas por perfil, enquanto atividades, disponibilidade de vagas, matrículas e informações utilizadas nos relatórios permanecem integradas.
