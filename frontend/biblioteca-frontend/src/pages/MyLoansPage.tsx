import { useGetMyLoansQuery } from '../api';
import formatData from '../utils/formatData';
import StatoBadgePrestito from '../utils/StatoBadgePrestito';

export default function MyLoansPage() {
  const { data: prestiti, isLoading, isError } = useGetMyLoansQuery();

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Area personale</p>
        <h1 className="h3 fw-semibold mb-0">I miei prestiti</h1>
      </div>

      {isLoading && <p className="text-muted">Caricamento...</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare i tuoi prestiti.</div>}
      {prestiti && prestiti.length === 0 && <div className="alert alert-light border text-center text-muted">Non hai prestiti registrati.</div>}

      {prestiti && prestiti.length > 0 && (
        <div className="table-responsive">
          <table className="table table-hover align-middle bg-white">
            <thead className="table-light">
              <tr><th>Libro</th><th>Prestato</th><th>Scadenza</th><th>Restituito</th><th>Stato</th></tr>
            </thead>
            <tbody>
              {prestiti.map((p) => (
                <tr key={p.id}>
                  <td>{p.titoloLibro}<span className="d-block text-muted small font-monospace">{p.codiceInventario}</span></td>
                  <td>{formatData(p.dataPrestito)}</td>
                  <td>{formatData(p.dataScadenza)}</td>
                  <td>{formatData(p.dataRestituzione)}</td>
                  <td><StatoBadgePrestito prestito={p} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
