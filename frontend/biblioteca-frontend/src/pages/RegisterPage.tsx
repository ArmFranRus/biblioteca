import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useRegisterMutation } from '../api';
import { useAppDispatch } from '../hooks';
import { setCredentials } from '../authSlice';

export default function RegisterPage() {
  const [nome, setNome] = useState('');
  const [cognome, setCognome] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [register, { isLoading }] = useRegisterMutation();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const res = await register({ nome, cognome, email, password }).unwrap();
      dispatch(setCredentials(res));
      navigate('/catalogo');
    } catch {
      setError("Registrazione non riuscita. L'email potrebbe essere già in uso o la password troppo corta (min. 8 caratteri).");
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-12 col-md-7 col-lg-6">
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <h1 className="h4 mb-1">Registrati</h1>
            <p className="text-muted">Crea un account per prenotare libri e lasciare recensioni.</p>
            <form onSubmit={onSubmit}>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Nome</label>
                  <input className="form-control" value={nome} onChange={(e) => setNome(e.target.value)} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Cognome</label>
                  <input className="form-control" value={cognome} onChange={(e) => setCognome(e.target.value)} required />
                </div>
                <div className="col-12">
                  <label className="form-label">Email</label>
                  <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div className="col-12">
                  <label className="form-label">Password</label>
                  <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
                </div>
              </div>
              {error && <div className="alert alert-danger py-2 mt-3">{error}</div>}
              <button className="btn btn-success w-100 mt-3" disabled={isLoading}>
                {isLoading ? 'Creazione...' : 'Crea account'}
              </button>
            </form>
            <p className="text-muted small mt-3 mb-0">
              Hai già un account? <Link to="/login">Accedi</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
