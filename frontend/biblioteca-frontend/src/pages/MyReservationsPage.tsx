import { useGetMyReservationsQuery, useCancelReservationMutation } from '../api';
import type { Prenotazione } from '../types';

function formatData(iso: string | null): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleDateString('it-IT');
}

function StatoBadge({ p }: { p: Prenotazione }) {
  switch (p.stato) {
    case 'IN_ATTESA': return <span className="badge text-bg-success">In coda</span>;
    case 'PRONTA': return <span className="badge text-bg-primary">Pronta al ritiro</span>;
    case 'EVASA': return <span className="badge text-bg-secondary">Evasa</span>;
    case 'ANNULLATA': return <span className="badge text-bg-secondary">Annullata</span>;
    case 'SCADUTA': return <span className="badge text-bg-danger">Scaduta</span>;
  }
}

export default function MyReservationsPage() {
  const { data: prenotazioni, isLoading, isError } = useGetMyReservationsQuery();
  const [cancel, { isLoading: annullando }] = useCancelReservationMutation();

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Area personale</p>
        <h1 className="h3 fw-semibold mb-0">Le mie prenotazioni</h1>
      </div>

      {isLoading && <p className="text-muted">Caricamento...</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare le tue prenotazioni.</div>}
      {prenotazioni && prenotazioni.length === 0 && <div className="alert alert-light border text-center text-muted">Non hai prenotazioni.</div>}

      {prenotazioni && prenotazioni.length > 0 && (
        <div className="table-responsive">
          <table className="table table-hover align-middle bg-white">
            <thead className="table-light">
              <tr><th>Libro</th><th>Data</th><th>Ritiro entro</th><th>Stato</th><th></th></tr>
            </thead>
            <tbody>
              {prenotazioni.map((p) => (
                <tr key={p.id}>
                  <td>{p.titoloLibro}</td>
                  <td>{formatData(p.data)}</td>
                  <td>{p.stato === 'PRONTA' ? formatData(p.scadenzaRitiro) : '—'}</td>
                  <td><StatoBadge p={p} /></td>
                  <td className="text-end">
                    {(p.stato === 'IN_ATTESA' || p.stato === 'PRONTA') && (
                      <button className="btn btn-outline-danger btn-sm" disabled={annullando} onClick={() => cancel(p.id)}>
                        Annulla
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
