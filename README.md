# File API

API para upload, listagem e download de arquivos (imagens PNG, JPEG, GIF e PDFs) com Spring Boot.

---

## Pré-requisitos

* Java 17+ (ou JDK 21 no seu Dockerfile)
* Maven
* PostgreSQL (local ou via serviço como Render)
* (Opcional) Docker e Docker Compose para rodar banco localmente

---

## Configuração

1. Clone o repositório

```bash
git clone https://github.com/PedroNhamirre/spring-file-storage.git
cd file-api
```

2. Configure o banco PostgreSQL (local ou remoto)

* Para rodar local com Docker Compose (exemplo):

```yaml
services:
  postgres:
    image: postgres
    environment:
      POSTGRES_DB: filedb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
volumes:
  postgres-data:
```

Salve em `docker-compose.yml` e rode:

```bash
docker-compose up -d
```

3. Configure o arquivo `application.properties` com as variáveis do banco:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/filedb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

file.upload-dir=uploads
```

---

## Rodando a aplicação

Via Maven:

```bash
mvn spring-boot:run
```

Ou construa o JAR e execute:

```bash
mvn clean package
java -jar target/file-api.jar
```

---

## Endpoints

### Upload

`POST /files`

Form-data:

* `file` (multipart file)

Resposta JSON com dados do arquivo, incluindo URL para download.

---

### Dados do arquivo

`GET /files/{id}`

Retorna os dados do arquivo (nome, tamanho, tipo, extensão ,URL, data upload).

---

### Download do arquivo

`GET /files/{id}/download`

Faz download do arquivo.

---

## Observações

* Tamanho máximo do arquivo: 50MB
* Arquivos são salvos localmente na pasta configurada em `file.upload-dir`.

---
