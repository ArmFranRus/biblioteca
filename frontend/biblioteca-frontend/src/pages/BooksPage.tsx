import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useGetBooksQuery, useGetCategoriesQuery } from '../api';

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

export default function BooksPage() {
  const [titolo, setTitolo] = useState('');
  const [autore, setAutore] = useState('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');

  const { data: libri, isLoading, isError } = useGetBooksQuery({
    titolo: titolo || undefined,
    autore: autore || undefined,
    categoriaId: categoriaId === '' ? undefined : categoriaId,
  });
  const { data: categorie } = useGetCategoriesQuery();

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Catalogo</p>
        <h1 className="h3 fw-semibold mb-0">Sfoglia la collezione</h1>
      </div>

      <div className="row g-2 mb-4">
        <div className="col-12 col-md">
          <input className="form-control" placeholder="Cerca per titolo" value={titolo} onChange={(e) => setTitolo(e.target.value)} />
        </div>
        <div className="col-12 col-md">
          <input className="form-control" placeholder="Autore" value={autore} onChange={(e) => setAutore(e.target.value)} />
        </div>
        <div className="col-12 col-md">
          <select className="form-select" value={categoriaId} onChange={(e) => setCategoriaId(e.target.value === '' ? '' : Number(e.target.value))}>
            <option value="">Tutte le categorie</option>
            {categorie?.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
          </select>
        </div>
      </div>

      {isLoading && <p className="text-muted">Caricamento…</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare il catalogo. Verifica che il server sia attivo.</div>}
      {libri && libri.length === 0 && (
        <div className="alert alert-light border text-center text-muted">
          Nessun libro trovato. Prova a modificare i filtri o aggiungine uno dall'area Gestione.
        </div>
      )}

      <div className="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-3">
        {libri?.map((l) => (
          <div className="col" key={l.id}>
            <Link to={`/catalogo/${l.id}`} className="text-reset">
              <div className="card h-100 border-start border-success border-4 shadow-sm">
                <div className="card-body">
                  <h2 className="h6 card-title mb-1">{l.titolo}</h2>
                  <p className="card-subtitle text-muted small mb-2">
                    {l.autori.map((a) => a.nome).join(', ') || 'Autore non indicato'}
                  </p>
                  {l.categoria && <span className="badge text-bg-light border">{l.categoria.nome}</span>}
                  {l.numeroRecensioni > 0 && (
                    <div className="mt-2 d-flex align-items-center gap-2">
                      <Stars voto={l.votoMedio ?? 0} />
                      <span className="text-muted small">{(l.votoMedio ?? 0).toFixed(1)} · {l.numeroRecensioni}</span>
                    </div>
                  )}
                </div>
                <div className="card-footer bg-transparent small">
                  {l.copieTotali === 0 ? (
                    <span className="text-muted">Nessuna copia</span>
                  ) : l.copieDisponibili > 0 ? (
                    <span className="text-success fw-semibold">{l.copieDisponibili} di {l.copieTotali} disponibili</span>
                  ) : (
                    <span className="text-warning fw-semibold">Tutte in prestito</span>
                  )}
                </div>
              </div>
            </Link>
          </div>
        ))}
      </div>
    </section>
  );
}
