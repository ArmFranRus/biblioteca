import { Prenotazione } from "../types";

export default function StatoBadgePrenotazione({ p }: { p: Prenotazione }) {
  switch (p.stato) {
    case 'IN_ATTESA': return <span className="badge text-bg-success">In coda</span>;
    case 'PRONTA': return <span className="badge text-bg-primary">Pronta al ritiro</span>;
    case 'EVASA': return <span className="badge text-bg-secondary">Evasa</span>;
    case 'ANNULLATA': return <span className="badge text-bg-secondary">Annullata</span>;
    case 'SCADUTA': return <span className="badge text-bg-danger">Scaduta</span>;
  }
}