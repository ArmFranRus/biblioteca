# BiblioTech — Frontend

Interfaccia web della piattaforma biblioteca, sviluppata con **React + TypeScript** (Vite) e **Redux Toolkit / RTK Query** per il consumo delle API REST.

## Requisiti

- Node.js 18+
- Il backend Spring Boot attivo (per impostazione predefinita su `http://localhost:7979`)

## Avvio

```bash
npm install
npm run dev
```

L'app parte su `http://localhost:5173`. Le chiamate a `/api` vengono inoltrate al backend tramite il **proxy** configurato in `vite.config.ts`, così non servono configurazioni CORS lato server durante lo sviluppo. Se il backend gira su una porta diversa dalla 7979, aggiorna il `target` del proxy.

## Cosa si può fare

- **Catalogo** (pubblico): ricerca dei libri per titolo, autore e categoria, con disponibilità delle copie; scheda di dettaglio del libro.
- **Autenticazione**: registrazione (crea utenti `PUBLIC`) e login; il token JWT viene conservato in `localStorage` e allegato automaticamente alle richieste.
- **Gestione** (solo staff): creazione ed eliminazione di categorie e autori, creazione di libri, gestione delle copie (aggiunta, cambio stato, eliminazione) ed eliminazione dei libri.

Per provare le funzioni staff: accedi con l'utente amministratore creato dal backend (`admin@biblioteca.it` / `admin1234`).

## Struttura

```
src/
├── main.tsx, App.tsx        # bootstrap e rotte
├── index.css                # tema e stili
├── store.ts, hooks.ts       # store Redux e hook tipizzati
├── api.ts                   # definizione API con RTK Query
├── authSlice.ts             # stato di autenticazione (token, ruolo)
├── types.ts                 # tipi condivisi
├── components/              # Navbar, RequireStaff
└── pages/                   # Login, Register, Books, BookDetail, Staff
```

## Prestiti (aggiunto)

- **Registra prestito** (staff): dal dettaglio di un libro con copie disponibili, scegli la copia, cerca l'utente per nome/email e conferma.
- **Prestiti** (staff): pagina `/prestiti` con elenco filtrabile per stato e pulsante *Restituisci*.
- **I miei prestiti** (utente autenticato): pagina `/miei-prestiti` con lo storico personale e lo stato (in corso / in ritardo / restituito).

Gli endpoint usati seguono la convenzione parlante del backend (`loans/create`, `loans/return/{id}`, `loans/all`, `loans/mine`, `users/search`).

## Recensioni (aggiunto)

- Nel dettaglio di un libro: voto medio e numero di recensioni in testata, elenco delle recensioni, e - per gli utenti autenticati - un form per pubblicarne una (voto 1–5 e commento). Ogni utente può recensire una sola volta.
- Ognuno può eliminare le **proprie** recensioni; lo staff può eliminarle tutte (moderazione).
- Nel catalogo, le card mostrano il voto medio quando presente.

Endpoint usati: `reviews/all/{libroId}`, `reviews/create`, `reviews/delete/{id}`.

## Dashboard e prenotazioni (aggiunto)

- **Dashboard** (staff, `/dashboard`): indicatori sintetici (libri, copie, copie disponibili, utenti, prestiti attivi e in ritardo, prenotazioni in attesa e pronte) e i titoli più prestati.
- **Prenota** (dettaglio libro): quando non ci sono copie disponibili, un utente autenticato può prenotare il titolo; se è già in coda o pronto al ritiro, viene mostrato lo stato.
- **Le mie prenotazioni** (`/mie-prenotazioni`): stato delle proprie prenotazioni con possibilità di annullamento.
- **Prenotazioni** (staff, `/prenotazioni`): elenco filtrabile per stato.

## Interfaccia con Bootstrap (aggiornato)

La UI usa **Bootstrap 5** e **Bootstrap Icons** (importati in `src/main.tsx`);

- Il routing usa il **data router** di React Router (`createBrowserRouter` in `main.tsx`).
- `src/App.tsx` è il guscio dell'applicazione: rende `<Layout><Outlet /></Layout>`.
- `src/components/Layout.tsx` definisce la struttura globale (Header + contenuto + Footer) e mostra una barra di avanzamento durante le navigazioni (`useNavigation`).
- `src/components/Header.tsx` è la navbar; `src/components/Footer.tsx` il piè di pagina.
- Le singole pagine sono stilizzate con le classi di utilità e i componenti Bootstrap (card, table, badge, form-control, ecc.).
