# BasicCRUD

Aplicação fullstack de gerenciamento de pessoas, desenvolvida como desafio técnico.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3.5 |
| Frontend | Vue 3 + Vite + Pinia |
| Banco de dados | PostgreSQL 16 |
| Autenticação | JWT (Bearer token) |
| Containerização | Docker + Docker Compose |
| CI/CD | GitHub Actions + GHCR |
| Deploy | DigitalOcean Droplet |

---

## Arquitetura

```
[Vue 3 SPA (porta 3000)]
        ↓ HTTP (Axios)
[Spring Boot API (porta 8080)]
        ↓ JPA
[PostgreSQL (porta 5432)]
```

O backend segue a estrutura MVC em camadas: `controller → service → repository`, com DTOs separando a camada de apresentação da entidade de domínio.

---

## Como rodar localmente

### Pré-requisitos

- Docker e Docker Compose instalados
- Node.js 20+ (para o frontend em modo dev)

### 1. Backend + Banco (Docker)

```bash
# Na raiz do projeto
docker compose up
```

O backend estará disponível em `http://localhost:8080`.

### 2. Frontend (modo dev com hot-reload)

```bash
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173`.

> O arquivo `frontend/.env.development` já aponta para `http://localhost:8080`.

### Rodar tudo junto (sem hot-reload)

```bash
docker compose up --build
```

Frontend em `http://localhost:3000` | Backend em `http://localhost:8080`.

---

## Variáveis de ambiente

Crie um arquivo `.env` na raiz baseado no `.env.example`:

| Variável | Descrição | Padrão dev |
|---|---|---|
| `DB_NAME` | Nome do banco | `basiccrud` |
| `DB_USER` | Usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha do PostgreSQL | — |
| `JWT_SECRET` | Segredo para assinar tokens JWT | — |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` (24h) |

---

## Endpoints da API

Todos os endpoints de `/pessoa*` exigem o header:
```
Authorization: Bearer <token>
```

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Cadastrar usuário |
| `POST` | `/auth/login` | Autenticar e obter token JWT |

### Pessoas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/pessoa` | Criar uma pessoa |
| `POST` | `/pessoas/lote` | Criar múltiplas pessoas em lote |
| `GET` | `/pessoas` | Listar com paginação (`?page=0&size=10`) |
| `GET` | `/pessoa/{id}` | Buscar por ID |
| `PUT` | `/pessoa/{id}` | Atualizar pessoa |
| `DELETE` | `/pessoa/{id}` | Remover pessoa |

#### Exemplo — criar em lote (`POST /pessoas/lote`)

```json
{
  "pessoas": [
    { "nome": "Ana Silva", "email": "ana@email.com", "dataNascimento": "1990-05-20" },
    { "nome": "Bruno Costa", "email": "bruno@email.com", "dataNascimento": "1985-11-03" }
  ]
}
```

---

## Testes

```bash
cd backend
./mvnw test
```

Cobertura:
- **Testes unitários** — `PessoaServiceImplTest`: valida lógica de negócio com mocks
- **Testes de integração** — `PessoaControllerTest` e `AuthControllerTest`: valida endpoints com MockMvc

---

## CI/CD e Deploy

O pipeline no GitHub Actions (`.github/workflows/deploy.yml`) executa a cada push na `main`:

1. **Build** — compila o backend (Maven) e o frontend (Vite), gera as imagens Docker e faz push para o GitHub Container Registry (GHCR)
2. **Deploy** — via SSH no Droplet da DigitalOcean: puxa as novas imagens e reinicia os containers

Para configurar o deploy em um servidor novo, veja `scripts/setup-droplet.sh`.

### Secrets necessários no GitHub

| Secret | Descrição |
|---|---|
| `DO_SSH_HOST` | IP do servidor |
| `DO_SSH_USER` | Usuário SSH (ex: `root`) |
| `DO_SSH_PRIVATE_KEY` | Chave SSH privada |
| `GHCR_TOKEN` | PAT do GitHub com escopo `read:packages` |
| `DB_PASSWORD` | Senha do banco em produção |
| `JWT_SECRET` | Segredo JWT em produção |
| `VITE_API_URL` | URL pública do backend (ex: `http://<IP>:8080`) |

---

## Decisões técnicas

- **JWT stateless** — sem sessão no servidor; o token carrega as informações do usuário e é validado a cada requisição pelo filtro `JwtAuthenticationFilter`
- **DTOs em todas as camadas** — a entidade `Pessoa` nunca é exposta diretamente; `PessoaRequestDTO` valida a entrada e `PessoaResponseDTO` controla a saída
- **Criação em lote transacional** — `POST /pessoas/lote` valida duplicatas dentro do próprio lote e no banco antes de persistir tudo em uma única transação
- **Paginação server-side** — a listagem usa `Pageable` do Spring Data para não carregar todos os registros em memória
- **GlobalExceptionHandler** — centraliza o tratamento de erros retornando respostas padronizadas em JSON para todos os endpoints

