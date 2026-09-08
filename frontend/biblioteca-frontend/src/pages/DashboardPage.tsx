import { useGetDashboardQuery } from '../api';

function Kpi({ label, value, warn }: { label: string; value: number; warn?: boolean }) {
  const alert = warn && value > 0;
  return (
    <div className="col">
      <div className={`card h-100 shadow-sm ${alert ? 'border-warning' : ''}`}>
        <div className="card-body">
          <div className={`display-6 fw-semibold ${alert ? 'text-warning' : ''}`}>{value}</div>
          <div className="text-muted small">{label}</div>
        </div>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { data, isLoading, isError } = useGetDashboardQuery();

  if (isLoading) return <p className="text-muted">Caricamento…</p>;
  if (isError || !data) return <div className="alert alert-danger">Impossibile caricare la dashboard.</div>;

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Gestione</p>
        <h1 className="h3 fw-semibold mb-0">Dashboard</h1>
      </div>

      <div className="row row-cols-2 row-cols-md-4 g-3 mb-4">
        <Kpi label="Libri a catalogo" value={data.totaleLibri} />
        <Kpi label="Copie totali" value={data.totaleCopie} />
        <Kpi label="Copie disponibili" value={data.copieDisponibili} />
        <Kpi label="Utenti registrati" value={data.totaleUtenti} />
        <Kpi label="Prestiti attivi" value={data.prestitiAttivi} />
        <Kpi label="Prestiti in ritardo" value={data.prestitiInRitardo} warn />
        <Kpi label="Prenotazioni in attesa" value={data.prenotazioniInAttesa} />
        <Kpi label="Prenotazioni pronte" value={data.prenotazioniPronte} />
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          <h2 className="h5 mb-3">Titoli più prestati</h2>
          {data.titoliPiuPrestati.length === 0 ? (
            <p className="text-muted mb-0">Nessun prestito registrato finora.</p>
          ) : (
            <table className="table table-sm align-middle mb-0">
              <thead className="table-light">
                <tr><th>Titolo</th><th className="text-end">Prestiti</th></tr>
              </thead>
              <tbody>
                {data.titoliPiuPrestati.map((t) => (
                  <tr key={t.libroId}>
                    <td>{t.titolo}</td>
                    <td className="text-end">{t.prestiti}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}
