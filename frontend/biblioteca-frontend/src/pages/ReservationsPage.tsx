import { useState } from 'react';
import { useGetReservationsQuery } from '../api';
import type { Prenotazione, StatoPrenotazione } from '../types';

const FILTRI: { value: '' | StatoPrenotazione; label: string }[] = [
  { value: '', label: 'Tutte' },
  { value: 'IN_ATTESA', label: 'In attesa' },
  { value: 'PRONTA', label: 'Pronte' },
  { value: 'EVASA', label: 'Evase' },
  { value: 'SCADUTA', label: 'Scadute' },
  { value: 'ANNULLATA', label: 'Annullate' },
];

function formatData(iso: string | null): string {
  if (!iso) return '-';
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleDateString('it-IT');
}

function StatoBadge({ p }: { p: Prenotazione }) {
  switch (p.stato) {
    case 'IN_ATTESA': return <span className="badge text-bg-success">In attesa</span>;
    case 'PRONTA': return <span className="badge text-bg-primary">Pronta</span>;
    case 'EVASA': return <span className="badge text-bg-secondary">Evasa</span>;
    case 'ANNULLATA': return <span className="badge text-bg-secondary">Annullata</span>;
    case 'SCADUTA': return <span className="badge text-bg-danger">Scaduta</span>;
  }
}

export default function ReservationsPage() {
  const [stato, setStato] = useState<'' | StatoPrenotazione>('');
  const { data: prenotazioni, isLoading, isError } = useGetReservationsQuery(stato ? { stato } : undefined);

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Gestione</p>
        <h1 className="h3 fw-semibold mb-0">Prenotazioni</h1>
      </div>

      <div className="mb-3" style={{ maxWidth: 260 }}>
        <select className="form-select" value={stato} onChange={(e) => setStato(e.target.value as '' | StatoPrenotazione)}>
          {FILTRI.map((f) => <option key={f.value} value={f.value}>{f.label}</option>)}
        </select>
      </div>

      {isLoading && <p className="text-muted">Caricamento...</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare le prenotazioni.</div>}
      {prenotazioni && prenotazioni.length === 0 && <div className="alert alert-light border text-center text-muted">Nessuna prenotazione.</div>}

      {prenotazioni && prenotazioni.length > 0 && (
        <div className="table-responsive">
          <table className="table table-hover align-middle bg-white">
            <thead className="table-light">
              <tr><th>Libro</th><th>Utente</th><th>Data</th><th>Ritiro entro</th><th>Stato</th></tr>
            </thead>
            <tbody>
              {prenotazioni.map((p) => (
                <tr key={p.id}>
                  <td>{p.titoloLibro}</td>
                  <td>{p.utenteNome || p.utenteEmail}<span className="d-block text-muted small">{p.utenteEmail}</span></td>
                  <td>{formatData(p.data)}</td>
                  <td>{p.stato === 'PRONTA' ? formatData(p.scadenzaRitiro) : '-'}</td>
                  <td><StatoBadge p={p} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
