# BiblioTech — Backend

Backend REST della piattaforma di gestione biblioteca, sviluppato con **Spring Boot 3** e **Java 17**, con persistenza su **MySQL 8** tramite Spring Data JPA e autenticazione **JWT**.

Moduli implementati: autenticazione, catalogo (libri, autori, categorie, copie), prestiti e gestione utenti.

## Requisiti

- JDK 17+
- Maven 3.9+
- MySQL 8 (in locale o via Docker)

## Avvio

Database via Docker (opzionale):

```bash
docker compose up -d
```

Applicazione:

```bash
mvn spring-boot:run
```

Il server parte sulla porta **7979**. Con `ddl-auto: update` Hibernate crea le tabelle al primo avvio.

- Verifica: `GET http://localhost:7979/api/ping`
- Swagger UI: `http://localhost:7979/swagger-ui.html`

## Configurazione (`application.yml`)

| Variabile | Default | Descrizione |
|---|---|---|
| `DB_HOST` / `DB_PORT` | `localhost` / `3307` | Host e porta MySQL |
| `DB_NAME` | `biblioteca` | Nome database |
| `DB_USER` / `DB_PASSWORD` | `root` / `password` | Credenziali |
| `JWT_SECRET` | *(valore di sviluppo)* | Chiave di firma JWT |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@biblioteca.it` / `admin1234` | Admin creato al primo avvio |
| `LOAN_MAX_ACTIVE` | `5` | Prestiti attivi massimi per utente |

## Convenzione degli endpoint

Tutti gli endpoint sono sotto `/api`. La lettura del catalogo è **pubblica** (metodo `GET`); le scritture e la gestione di prestiti e utenti sono riservate allo **staff**. L'autorizzazione si basa sul metodo HTTP e sul prefisso della risorsa.

### Autenticazione

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/auth/register` | pubblico | Registra un utente (ruolo `PUBLIC`) |
| POST | `/api/auth/login` | pubblico | Login, restituisce il token JWT |

### Catalogo — libri

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/books/find` | pubblico | Ricerca (`?titolo=&categoriaId=&autore=`) |
| GET | `/api/books/find/{id}` | pubblico | Scheda libro con disponibilità |
| POST | `/api/books/create` | staff | Crea libro |
| PUT | `/api/books/update/{id}` | staff | Modifica libro |
| DELETE | `/api/books/delete/{id}` | staff | Elimina libro (se nessuna copia in prestito) |

### Catalogo — categorie e autori

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/categories/all` | pubblico | Elenco categorie |
| GET | `/api/categories/find/{id}` | pubblico | Dettaglio categoria |
| POST | `/api/categories/create` | staff | Crea categoria |
| PUT | `/api/categories/update/{id}` | staff | Modifica categoria |
| DELETE | `/api/categories/delete/{id}` | staff | Elimina categoria (se non usata) |
| GET | `/api/authors/all` | pubblico | Elenco autori |
| GET | `/api/authors/find/{id}` | pubblico | Dettaglio autore |
| POST | `/api/authors/create` | staff | Crea autore |
| PUT | `/api/authors/update/{id}` | staff | Modifica autore |
| DELETE | `/api/authors/delete/{id}` | staff | Elimina autore (se non associato) |

### Catalogo — copie

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/books/all/{libroId}/copies` | pubblico | Copie di un libro |
| POST | `/api/books/add/{libroId}/copies` | staff | Aggiunge una copia |
| PATCH | `/api/copies/update/{id}` | staff | Aggiorna lo stato di una copia |
| DELETE | `/api/copies/delete/{id}` | staff | Elimina una copia (se non in prestito) |

### Prestiti

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/loans/create` | staff | Registra un prestito (`{ copiaId, utenteId }`) |
| POST | `/api/loans/return/{id}` | staff | Registra la restituzione |
| GET | `/api/loans/all` | staff | Elenco prestiti (`?utenteId=&stato=`) |
| GET | `/api/loans/mine` | autenticato | I prestiti dell'utente autenticato |

Regole: la copia dev'essere `DISPONIBILE`; scadenza a 1 mese; massimo `LOAN_MAX_ACTIVE` prestiti attivi per utente; alla restituzione la copia torna `DISPONIBILE`. La risposta include `inRitardo` e `giorniRitardo`; un task notturno porta i prestiti scaduti da `ATTIVO` a `IN_RITARDO`.

### Gestione utenti

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/users/all` | staff | Elenco utenti |
| GET | `/api/users/search?q=` | staff | Ricerca per email, nome o cognome |
| GET | `/api/users/find/{id}` | staff | Dettaglio utente |
| POST | `/api/users/create` | staff | Crea un account `STAFF` |
| PATCH | `/api/users/enable/{id}` | staff | Riattiva un account |
| PATCH | `/api/users/disable/{id}` | staff | Disattiva un account |

## Autenticazione e ruoli

Login e registrazione restituiscono un token JWT da inviare come `Authorization: Bearer <token>`. In Swagger UI usa il pulsante **Authorize** per incollarlo. Un utente non autenticato riceve **401**; autenticato ma senza ruolo `STAFF`, **403**. La registrazione pubblica crea solo utenti `PUBLIC`; gli account `STAFF` si creano da `/api/users/create`. Un utente disattivato non può autenticarsi né ricevere prestiti.

## Test

```bash
mvn test
```

I test usano un database **H2** in memoria (profilo `test`) e coprono autenticazione, catalogo, prestiti e gestione utenti.

## Struttura

```
src/main/java/com/biblioteca
├── BibliotecaApplication.java
├── config/        # sicurezza, OpenAPI, seeder admin, scheduler ritardi
├── model/         # entità JPA
│   └── enums/     # ruoli e stati
├── repository/    # repository Spring Data
├── dto/           # oggetti di richiesta/risposta
├── service/       # logica applicativa
└── web/           # controller REST
```
