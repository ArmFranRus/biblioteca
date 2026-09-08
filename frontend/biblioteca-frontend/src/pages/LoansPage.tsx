import { useState } from 'react';
import { useGetLoansQuery, useReturnLoanMutation } from '../api';
import type { Prestito, StatoPrestito } from '../types';

const FILTRI: { value: '' | StatoPrestito; label: string }[] = [
  { value: '', label: 'Tutti' },
  { value: 'ATTIVO', label: 'Attivi' },
  { value: 'IN_RITARDO', label: 'In ritardo' },
  { value: 'RESTITUITO', label: 'Restituiti' },
];

function formatData(iso: string | null): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleDateString('it-IT');
}

function StatoBadge({ prestito }: { prestito: Prestito }) {
  if (prestito.stato === 'RESTITUITO') return <span className="badge text-bg-secondary">Restituito</span>;
  if (prestito.inRitardo) return <span className="badge text-bg-danger">In ritardo - {prestito.giorniRitardo}g</span>;
  return <span className="badge text-bg-success">Attivo</span>;
}

export default function LoansPage() {
  const [stato, setStato] = useState<'' | StatoPrestito>('');
  const { data: prestiti, isLoading, isError } = useGetLoansQuery(stato ? { stato } : undefined);
  const [returnLoan, { isLoading: restituendo }] = useReturnLoanMutation();

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Gestione</p>
        <h1 className="h3 fw-semibold mb-0">Prestiti</h1>
      </div>

      <div className="mb-3" style={{ maxWidth: 260 }}>
        <select className="form-select" value={stato} onChange={(e) => setStato(e.target.value as '' | StatoPrestito)}>
          {FILTRI.map((f) => <option key={f.value} value={f.value}>{f.label}</option>)}
        </select>
      </div>

      {isLoading && <p className="text-muted">Caricamento...</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare i prestiti.</div>}
      {prestiti && prestiti.length === 0 && <div className="alert alert-light border text-center text-muted">Nessun prestito da mostrare.</div>}

      {prestiti && prestiti.length > 0 && (
        <div className="table-responsive">
          <table className="table table-hover align-middle bg-white">
            <thead className="table-light">
              <tr><th>Libro</th><th>Utente</th><th>Prestato</th><th>Scadenza</th><th>Stato</th><th></th></tr>
            </thead>
            <tbody>
              {prestiti.map((p) => (
                <tr key={p.id}>
                  <td>{p.titoloLibro}<span className="d-block text-muted small font-monospace">{p.codiceInventario}</span></td>
                  <td>{p.utenteNome || p.utenteEmail}<span className="d-block text-muted small">{p.utenteEmail}</span></td>
                  <td>{formatData(p.dataPrestito)}</td>
                  <td>{formatData(p.dataScadenza)}</td>
                  <td><StatoBadge prestito={p} /></td>
                  <td className="text-end">
                    {p.dataRestituzione === null && (
                      <button className="btn btn-success btn-sm" disabled={restituendo}
                        onClick={() => returnLoan({ id: p.id, libroId: p.libroId })}>
                        <i className="bi bi-arrow-return-left me-1" />Restituisci
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
