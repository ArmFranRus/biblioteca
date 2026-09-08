import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLoginMutation } from '../api';
import { useAppDispatch } from '../hooks';
import { setCredentials } from '../authSlice';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [login, { isLoading }] = useLoginMutation();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const res = await login({ email, password }).unwrap();
      dispatch(setCredentials(res));
      navigate('/catalogo');
    } catch {
      setError('Email o password non corretti.');
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-12 col-md-6 col-lg-5">
        <div className="card shadow-sm">
          <div className="card-body p-4">
            <h1 className="h4 mb-1">Accedi</h1>
            <p className="text-muted">Entra per gestire il catalogo o consultare i tuoi dati.</p>
            <form onSubmit={onSubmit}>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
              </div>
              <div className="mb-3">
                <label className="form-label">Password</label>
                <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required />
              </div>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <button className="btn btn-success w-100" disabled={isLoading}>
                {isLoading ? 'Accesso in corso...' : 'Accedi'}
              </button>
            </form>
            <p className="text-muted small mt-3 mb-0">
              Non hai un account? <Link to="/registrazione">Registrati</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
