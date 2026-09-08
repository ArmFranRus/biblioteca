export type Ruolo = 'PUBLIC' | 'STAFF';
export type StatoCopia = 'DISPONIBILE' | 'PRESTATA' | 'SMARRITA' | 'DANNEGGIATA';
export type StatoPrestito = 'ATTIVO' | 'RESTITUITO' | 'IN_RITARDO';

export interface AuthResponse {
  token: string;
  email: string;
  ruolo: Ruolo;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nome: string;
  cognome: string;
}

export interface Categoria {
  id: number;
  nome: string;
}

export interface Autore {
  id: number;
  nome: string;
}

export interface Copia {
  id: number;
  libroId: number;
  codiceInventario: string;
  stato: StatoCopia;
}

export interface Libro {
  id: number;
  titolo: string;
  isbn: string | null;
  editore: string | null;
  anno: number | null;
  categoria: Categoria | null;
  autori: Autore[];
  copieTotali: number;
  copieDisponibili: number;
  votoMedio: number | null;
  numeroRecensioni: number;
}

export interface LibroRequest {
  titolo: string;
  isbn?: string | null;
  editore?: string | null;
  anno?: number | null;
  categoriaId?: number | null;
  autoriIds: number[];
}

export interface Utente {
  id: number;
  email: string;
  nome: string | null;
  cognome: string | null;
  ruolo: Ruolo;
  attivo: boolean;
}

export interface Prestito {
  id: number;
  copiaId: number;
  codiceInventario: string;
  libroId: number;
  titoloLibro: string;
  utenteId: number;
  utenteEmail: string;
  utenteNome: string;
  dataPrestito: string;
  dataScadenza: string;
  dataRestituzione: string | null;
  stato: StatoPrestito;
  inRitardo: boolean;
  giorniRitardo: number;
}

export interface Recensione {
  id: number;
  libroId: number;
  utenteId: number;
  autore: string;
  voto: number;
  testo: string | null;
  data: string;
  mia: boolean;
}

export type StatoPrenotazione = 'IN_ATTESA' | 'PRONTA' | 'EVASA' | 'ANNULLATA' | 'SCADUTA';

export interface Prenotazione {
  id: number;
  libroId: number;
  titoloLibro: string;
  utenteId: number;
  utenteEmail: string;
  utenteNome: string;
  data: string;
  stato: StatoPrenotazione;
  scadenzaRitiro: string | null;
}

export interface TitoloPrestiti {
  libroId: number;
  titolo: string;
  prestiti: number;
}

export interface DashboardData {
  totaleLibri: number;
  totaleCopie: number;
  copieDisponibili: number;
  totaleUtenti: number;
  prestitiAttivi: number;
  prestitiInRitardo: number;
  prenotazioniInAttesa: number;
  prenotazioniPronte: number;
  titoliPiuPrestati: TitoloPrestiti[];
}
