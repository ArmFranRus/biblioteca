export default function Stars({ voto }: { voto: number }) {
  const pieni = Math.max(0, Math.min(5, Math.round(voto)));
  return (
    <span className="text-warning" aria-label={`${voto} su 5`}>
      {Array.from({ length: 5 }, (_, i) => (
        <i key={i} className={`bi ${i < pieni ? 'bi-star-fill' : 'bi-star'}`} />
      ))}
    </span>
  );
}
