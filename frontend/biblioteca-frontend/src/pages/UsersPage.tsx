import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  useGetUsersQuery,
  useCreateStaffMutation,
  useSetUserEnabledMutation,
} from '../api';

export default function UsersPage() {
  const { data: utenti, isLoading, isError } = useGetUsersQuery();
  const [createStaff, { isLoading: creating }] = useCreateStaffMutation();
  const [setEnabled] = useSetUserEnabledMutation();

  const [q, setQ] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nome, setNome] = useState('');
  const [cognome, setCognome] = useState('');
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  const filtrati = (utenti ?? []).filter((u) => {
    const t = q.trim().toLowerCase();
    if (!t) return true;
    return (
      u.email.toLowerCase().includes(t) ||
      (u.nome ?? '').toLowerCase().includes(t) ||
      (u.cognome ?? '').toLowerCase().includes(t)
    );
  });

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setMsg(null);
    setErr(null);
    try {
      await createStaff({
        email: email.trim(),
        password,
        nome: nome.trim(),
        cognome: cognome.trim(),
      }).unwrap();
      setMsg('Account staff creato.');
      setEmail('');
      setPassword('');
      setNome('');
      setCognome('');
    } catch {
      setErr("Impossibile creare l'account (email già in uso o password troppo corta, minimo 8 caratteri).");
    }
  }

  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>
          Gestione
        </p>
        <h1 className="h3 fw-semibold mb-0">Utenti</h1>
      </div>

      <div className="card shadow-sm mb-3">
        <div className="card-body">
          <h2 className="h5 mb-3">Nuovo account staff</h2>
          <form onSubmit={onCreate}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Password</label>
                <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Nome</label>
                <input className="form-control" value={nome} onChange={(e) => setNome(e.target.value)} required />
              </div>
              <div className="col-md-6">
                <label className="form-label">Cognome</label>
                <input className="form-control" value={cognome} onChange={(e) => setCognome(e.target.value)} required />
              </div>
            </div>
            {msg && <div className="alert alert-success py-2 mt-3">{msg}</div>}
            {err && <div className="alert alert-danger py-2 mt-3">{err}</div>}
            <button className="btn btn-success mt-3" disabled={creating}>
              {creating ? 'Creazione...' : 'Crea account staff'}
            </button>
          </form>
        </div>
      </div>

      <div className="mb-3" style={{ maxWidth: 360 }}>
        <input className="form-control" placeholder="Cerca per nome o email" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      {isLoading && <p className="text-muted">Caricamento...</p>}
      {isError && <div className="alert alert-danger">Impossibile caricare gli utenti.</div>}
      {utenti && filtrati.length === 0 && (
        <div className="alert alert-light border text-center text-muted">Nessun utente trovato.</div>
      )}

      {filtrati.length > 0 && (
        <div className="table-responsive">
          <table className="table table-hover align-middle bg-white">
            <thead className="table-light">
              <tr>
                <th>Utente</th>
                <th>Email</th>
                <th>Ruolo</th>
                <th>Stato</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtrati.map((u) => (
                <tr key={u.id}>
                  <td>{`${u.nome ?? ''} ${u.cognome ?? ''}`.trim() || '-'}</td>
                  <td>{u.email}</td>
                  <td>
                    <span className={`badge ${u.ruolo === 'STAFF' ? 'text-bg-success' : 'text-bg-secondary'}`}>
                      {u.ruolo === 'STAFF' ? 'Staff' : 'Utente'}
                    </span>
                  </td>
                  <td>
                    {u.attivo ? (
                      <span className="badge text-bg-success">Attivo</span>
                    ) : (
                      <span className="badge text-bg-danger">Disattivato</span>
                    )}
                  </td>
                  <td className="text-end">
                    {u.attivo ? (
                      <button className="btn btn-outline-danger btn-sm" onClick={() => setEnabled({ id: u.id, enabled: false })}>
                        Disattiva
                      </button>
                    ) : (
                      <button className="btn btn-outline-success btn-sm" onClick={() => setEnabled({ id: u.id, enabled: true })}>
                        Riattiva
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