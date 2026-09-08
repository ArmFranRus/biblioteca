import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  useGetBookQuery,
  useGetCopiesQuery,
  useAddCopyMutation,
  useUpdateCopyStateMutation,
  useDeleteCopyMutation,
  useDeleteBookMutation,
  useSearchUsersQuery,
  useCreateLoanMutation,
  useGetReviewsQuery,
  useCreateReviewMutation,
  useDeleteReviewMutation,
  useCreateReservationMutation,
  useGetMyReservationsQuery,
} from '../api';
import { useAppSelector } from '../hooks';
import type { StatoCopia, Utente } from '../types';

const STATI_MODIFICABILI: StatoCopia[] = ['DISPONIBILE', 'SMARRITA', 'DANNEGGIATA'];

const ETICHETTE: Record<StatoCopia, string> = {
  DISPONIBILE: 'Disponibile',
  PRESTATA: 'In prestito',
  SMARRITA: 'Smarrita',
  DANNEGGIATA: 'Danneggiata',
};

const BADGE_STATO: Record<StatoCopia, string> = {
  DISPONIBILE: 'text-bg-success',
  PRESTATA: 'text-bg-warning',
  SMARRITA: 'text-bg-danger',
  DANNEGGIATA: 'text-bg-danger',
};

function Stars({ voto }: { voto: number }) {
  const pieni = Math.max(0, Math.min(5, Math.round(voto)));
  return (
    <span className="text-warning" aria-label={`${voto} su 5`}>
      {Array.from({ length: 5 }, (_, i) => (
        <i key={i} className={`bi ${i < pieni ? 'bi-star-fill' : 'bi-star'}`} />
      ))}
    </span>
  );
}

function formatData(iso: string): string {
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleDateString('it-IT');
}

export default function BookDetailPage() {
  const { id } = useParams();
  const bookId = Number(id);
  const navigate = useNavigate();
  const ruolo = useAppSelector((s) => s.auth.ruolo);
  const token = useAppSelector((s) => s.auth.token);
  const isStaff = ruolo === "STAFF";
  const isLogged = !!token;

  const { data: libro, isLoading, isError } = useGetBookQuery(bookId);
  const { data: copie } = useGetCopiesQuery(bookId);
  const [addCopy] = useAddCopyMutation();
  const [updateCopyState] = useUpdateCopyStateMutation();
  const [deleteCopy] = useDeleteCopyMutation();
  const [deleteBook] = useDeleteBookMutation();

  const [nuovoCodice, setNuovoCodice] = useState('');
  const [errore, setErrore] = useState<string | null>(null);

  if (isLoading) return <p className="text-muted">Caricamento…</p>;
  if (isError || !libro) return <div className="alert alert-danger">Libro non trovato.</div>;

  const copieDisponibili = (copie ?? []).filter((c) => c.stato === "DISPONIBILE");

  async function onAddCopy(e: FormEvent) {
    e.preventDefault();
    setErrore(null);
    if (!nuovoCodice.trim()) return;
    try {
      await addCopy({ libroId: bookId, codiceInventario: nuovoCodice.trim() }).unwrap();
      setNuovoCodice('');
    } catch {
      setErrore("Codice inventario già esistente o non valido.");
    }
  }

  async function onDeleteBook() {
    setErrore(null);
    if (!window.confirm("Eliminare definitivamente questo libro?")) return;
    try {
      await deleteBook(bookId).unwrap();
      navigate('/catalogo');
    } catch {
      setErrore("Impossibile eliminare: ci sono copie attualmente in prestito.");
    }
  }

  return (
    <article style={{ maxWidth: 820 }}>
      <Link to="/catalogo" className="text-muted small"><i className="bi bi-chevron-left" /> Catalogo</Link>

      <header className="mt-2 mb-4">
        <h1 className="h2 mb-1">{libro.titolo}</h1>
        <p className="text-muted mb-2">{libro.autori.map((a) => a.nome).join(', ') || 'Autore non indicato'}</p>

        <div className="mb-2">
          {libro.numeroRecensioni > 0 ? (
            <span className="d-inline-flex align-items-center gap-2">
              <Stars voto={libro.votoMedio ?? 0} />
              <span className="text-muted small">
                {(libro.votoMedio ?? 0).toFixed(1)} · {libro.numeroRecensioni}{' '}
                {libro.numeroRecensioni === 1 ? 'recensione' : 'recensioni'}
              </span>
            </span>
          ) : (
            <span className="text-muted small">Nessuna recensione</span>
          )}
        </div>

        <div className="d-flex flex-wrap gap-2 align-items-center mb-2">
          {libro.categoria && <span className="badge text-bg-light border">{libro.categoria.nome}</span>}
          {libro.editore && <span className="text-muted small">{libro.editore}</span>}
          {libro.anno && <span className="text-muted small">{libro.anno}</span>}
          {libro.isbn && <span className="text-muted small font-monospace">ISBN {libro.isbn}</span>}
        </div>

        <p className="fw-semibold text-success mb-0">
          {libro.copieDisponibili} di {libro.copieTotali} copie disponibili
        </p>
        {libro.copieDisponibili === 0 && <PrenotaAction libroId={bookId} isLogged={isLogged} />}
      </header>

      {errore && <div className="alert alert-danger">{errore}</div>}

      {isStaff && copieDisponibili.length > 0 && (
        <PrestaPanel libroId={bookId} copieDisponibili={copieDisponibili} />
      )}

      <div className="card shadow-sm mb-3">
        <div className="card-body">
          <h2 className="h5 mb-3">Copie</h2>
          {copie && copie.length === 0 && <p className="text-muted">Nessuna copia registrata.</p>}
          <ul className="list-group list-group-flush mb-3">
            {copie?.map((c) => (
              <li key={c.id} className="list-group-item d-flex align-items-center gap-3 px-0">
                <span className="font-monospace flex-grow-1">{c.codiceInventario}</span>
                {isStaff && c.stato !== "PRESTATA" ? (
                  <select className="form-select form-select-sm w-auto"
                    value={c.stato}
                    onChange={(e) => updateCopyState({ id: c.id, libroId: bookId, stato: e.target.value as StatoCopia })}>
                    {STATI_MODIFICABILI.map((s) => <option key={s} value={s}>{ETICHETTE[s]}</option>)}
                  </select>
                ) : (
                  <span className={`badge ${BADGE_STATO[c.stato]}`}>{ETICHETTE[c.stato]}</span>
                )}
                {isStaff && c.stato !== "PRESTATA" && (
                  <button className="btn btn-outline-danger btn-sm" onClick={() => deleteCopy({ id: c.id, libroId: bookId })}>
                    Elimina
                  </button>
                )}
              </li>
            ))}
          </ul>

          {isStaff && (
            <form className="input-group" onSubmit={onAddCopy}>
              <input className="form-control" placeholder="Codice inventario nuova copia"
                value={nuovoCodice} onChange={(e) => setNuovoCodice(e.target.value)} />
              <button className="btn btn-success">Aggiungi copia</button>
            </form>
          )}
        </div>
      </div>

      <RecensioniPanel libroId={bookId} isLogged={isLogged} isStaff={isStaff} />

      {isStaff && (
        <div className="card border-danger shadow-sm">
          <div className="card-body">
            <h2 className="h5 mb-3">Area staff</h2>
            <button className="btn btn-outline-danger" onClick={onDeleteBook}>Elimina libro</button>
          </div>
        </div>
      )}
    </article>
  );
}

function PrestaPanel({
  libroId,
  copieDisponibili,
}: {
  libroId: number;
  copieDisponibili: { id: number; codiceInventario: string }[];
}) {
  const [copiaId, setCopiaId] = useState<number>(copieDisponibili[0]?.id ?? 0);
  const [query, setQuery] = useState('');
  const [utente, setUtente] = useState<Utente | null>(null);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const { data: utenti } = useSearchUsersQuery(query.trim(), { skip: query.trim().length < 2 });
  const [createLoan, { isLoading }] = useCreateLoanMutation();

  async function conferma() {
    setMsg(null);
    setErr(null);
    if (!copiaId || !utente) return;
    try {
      await createLoan({ copiaId, utenteId: utente.id, libroId }).unwrap();
      setMsg(`Prestito registrato a ${utente.email}.`);
      setUtente(null);
      setQuery('');
    } catch {
      setErr("Impossibile registrare il prestito (copia non disponibile o limite prestiti raggiunto).");
    }
  }

  return (
    <div className="card shadow-sm mb-3">
      <div className="card-body">
        <h2 className="h5 mb-3">Registra un prestito</h2>
        <div className="row g-3" style={{ maxWidth: 520 }}>
          <div className="col-12">
            <label className="form-label">Copia</label>
            <select className="form-select" value={copiaId} onChange={(e) => setCopiaId(Number(e.target.value))}>
              {copieDisponibili.map((c) => <option key={c.id} value={c.id}>{c.codiceInventario}</option>)}
            </select>
          </div>
          <div className="col-12">
            <label className="form-label">Utente</label>
            {utente ? (
              <div className="d-flex align-items-center gap-2">
                <span>{utente.email}</span>
                <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => setUtente(null)}>Cambia</button>
              </div>
            ) : (
              <input className="form-control" placeholder="Cerca per nome o email (min. 2 caratteri)"
                value={query} onChange={(e) => setQuery(e.target.value)} />
            )}
            {!utente && query.trim().length >= 2 && utenti && utenti.length > 0 && (
              <ul className="list-group mt-1">
                {utenti.map((u) => (
                  <li key={u.id}>
                    <button type="button" className="list-group-item list-group-item-action w-100 text-start" onClick={() => setUtente(u)}>
                      {(u.nome || u.cognome) ? `${u.nome ?? ''} ${u.cognome ?? ''}`.trim() + ' — ' : ''}{u.email}
                    </button>
                  </li>
                ))}
              </ul>
            )}
            {!utente && query.trim().length >= 2 && utenti && utenti.length === 0 && (
              <p className="text-muted small mt-1 mb-0">Nessun utente trovato.</p>
            )}
          </div>
          {msg && <div className="col-12"><div className="alert alert-success py-2 mb-0">{msg}</div></div>}
          {err && <div className="col-12"><div className="alert alert-danger py-2 mb-0">{err}</div></div>}
          <div className="col-12">
            <button className="btn btn-success" disabled={isLoading || !utente || !copiaId} onClick={conferma}>
              {isLoading ? "Registrazione…" : "Registra prestito"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function PrenotaAction({ libroId, isLogged }: { libroId: number; isLogged: boolean }) {
  const { data: mie } = useGetMyReservationsQuery(undefined, { skip: !isLogged });
  const [createReservation, { isLoading }] = useCreateReservationMutation();
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  if (!isLogged) {
    return <p className="text-muted small mt-2 mb-0">Nessuna copia disponibile. Accedi per prenotare il titolo.</p>;
  }

  const attiva = (mie ?? []).find(
    (r) => r.libroId === libroId && (r.stato === "IN_ATTESA" || r.stato === "PRONTA"),
  );
  if (attiva) {
    return (
      <p className="fw-semibold text-success mt-2 mb-0">
        {attiva.stato === "PRONTA" ? "La tua prenotazione è pronta per il ritiro." : "Sei in coda per questo titolo."}
      </p>
    );
  }

  async function prenota() {
    setMsg(null);
    setErr(null);
    try {
      await createReservation({ libroId }).unwrap();
      setMsg("Prenotazione registrata: sei in coda.");
    } catch {
      setErr("Impossibile prenotare (forse è tornata disponibile una copia o hai già una prenotazione attiva).");
    }
  }

  return (
    <div className="mt-2">
      <button className="btn btn-success" onClick={prenota} disabled={isLoading}>
        <i className="bi bi-bookmark-plus me-1" />
        {isLoading ? "Prenotazione…" : "Prenota questo titolo"}
      </button>
      {msg && <div className="alert alert-success py-2 mt-2 mb-0">{msg}</div>}
      {err && <div className="alert alert-danger py-2 mt-2 mb-0">{err}</div>}
    </div>
  );
}

function RecensioniPanel({
  libroId,
  isLogged,
  isStaff,
}: {
  libroId: number;
  isLogged: boolean;
  isStaff: boolean;
}) {
  const { data: recensioni } = useGetReviewsQuery(libroId);
  const [createReview, { isLoading }] = useCreateReviewMutation();
  const [deleteReview] = useDeleteReviewMutation();

  const [voto, setVoto] = useState(5);
  const [testo, setTesto] = useState('');
  const [err, setErr] = useState<string | null>(null);

  const giaRecensito = (recensioni ?? []).some((r) => r.mia);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setErr(null);
    try {
      await createReview({ libroId, voto, testo: testo.trim() }).unwrap();
      setTesto("");
      setVoto(5);
    } catch {
      setErr("Impossibile inviare la recensione. Forse ne hai già scritta una per questo libro.");
    }
  }

  return (
    <div className="card shadow-sm mb-3">
      <div className="card-body">
        <h2 className="h5 mb-3">Recensioni</h2>

        {recensioni && recensioni.length === 0 && (
          <p className="text-muted">Ancora nessuna recensione. Sii il primo a scriverne una.</p>
        )}

        <div className="vstack gap-3 mb-3">
          {recensioni?.map((r) => (
            <div key={r.id} className="border rounded p-3">
              <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <span className="fw-semibold">{r.autore}</span>
                <span className="d-flex align-items-center gap-2">
                  <Stars voto={r.voto} />
                  <span className="text-muted small">{formatData(r.data)}</span>
                  {(r.mia || isStaff) && (
                    <button className="btn btn-outline-danger btn-sm" onClick={() => deleteReview({ id: r.id, libroId })}>
                      Elimina
                    </button>
                  )}
                </span>
              </div>
              {r.testo && <p className="mb-0 mt-2">{r.testo}</p>}
            </div>
          ))}
        </div>

        {isLogged ? (
          giaRecensito ? (
            <p className="text-muted small mb-0">Hai già recensito questo libro.</p>
          ) : (
            <form onSubmit={submit} style={{ maxWidth: 520 }}>
              <div className="mb-3" style={{ maxWidth: 200 }}>
                <label className="form-label">Voto</label>
                <select className="form-select" value={voto} onChange={(e) => setVoto(Number(e.target.value))}>
                  {[5, 4, 3, 2, 1].map((v) => <option key={v} value={v}>{v} {v === 1 ? "stella" : "stelle"}</option>)}
                </select>
              </div>
              <div className="mb-3">
                <label className="form-label">Commento (facoltativo)</label>
                <textarea className="form-control" rows={3} value={testo} onChange={(e) => setTesto(e.target.value)} maxLength={2000} />
              </div>
              {err && <div className="alert alert-danger py-2">{err}</div>}
              <button className="btn btn-success" disabled={isLoading}>
                {isLoading ? "Invio..." : "Pubblica recensione"}
              </button>
            </form>
          )
        ) : (
          <p className="text-muted small mb-0">Accedi per lasciare una recensione.</p>
        )}
      </div>
    </div>
  );
}
