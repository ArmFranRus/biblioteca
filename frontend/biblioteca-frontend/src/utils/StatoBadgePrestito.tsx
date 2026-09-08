import { Prestito } from "../types";

export default function StatoBadgePrestito({ prestito }: { prestito: Prestito }) {
  if (prestito.stato === 'RESTITUITO') return <span className="badge text-bg-secondary">Restituito</span>;
  if (prestito.inRitardo) return <span className="badge text-bg-danger">In ritardo - {prestito.giorniRitardo}g</span>;
  return <span className="badge text-bg-success">In corso</span>;
}