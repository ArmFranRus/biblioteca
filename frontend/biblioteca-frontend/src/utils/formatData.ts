export default function formatData(iso: string | null): string {
  if (!iso) return "-";
  const d = new Date(iso);
  return isNaN(d.getTime()) ? iso : d.toLocaleDateString("it-IT");
}
