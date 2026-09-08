import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../hooks';
import { logout } from '../authSlice';

export default function Header() {
  const { email, ruolo } = useAppSelector((s) => s.auth);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const isStaff = ruolo === 'STAFF';

  const linkClass = ({ isActive }: { isActive: boolean }) => 'nav-link' + (isActive ? ' active' : '');

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-success shadow-sm">
      <div className="container">
        <Link to="/catalogo" className="navbar-brand d-flex align-items-center gap-2 fw-semibold">
          <i className="bi bi-book-half" />
          BiblioApp
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navmenu"
          aria-controls="navmenu"
          aria-expanded="false"
          aria-label="Apri il menu"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse" id="navmenu">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item"><NavLink to="/catalogo" className={linkClass}>Catalogo</NavLink></li>
            {email && <li className="nav-item"><NavLink to="/miei-prestiti" className={linkClass}>I miei prestiti</NavLink></li>}
            {email && <li className="nav-item"><NavLink to="/mie-prenotazioni" className={linkClass}>Le mie prenotazioni</NavLink></li>}
            {isStaff && <li className="nav-item"><NavLink to="/prestiti" className={linkClass}>Prestiti</NavLink></li>}
            {isStaff && <li className="nav-item"><NavLink to="/prenotazioni" className={linkClass}>Prenotazioni</NavLink></li>}
            {isStaff && <li className="nav-item"><NavLink to="/dashboard" className={linkClass}>Dashboard</NavLink></li>}
            {isStaff && <li className="nav-item"><NavLink to="/utenti" className={linkClass}>Utenti</NavLink></li>}
            {isStaff && <li className="nav-item"><NavLink to="/staff" className={linkClass}>Gestione</NavLink></li>}
          </ul>

          <div className="d-flex align-items-center gap-2">
            {email ? (
              <>
                <span className="navbar-text text-white-50 small d-none d-lg-inline">{email}</span>
                <span className={`badge ${isStaff ? 'text-bg-light' : 'text-bg-secondary'}`}>
                  {isStaff ? 'Staff' : 'Utente'}
                </span>
                <button
                  className="btn btn-outline-light btn-sm"
                  onClick={() => {
                    dispatch(logout());
                    navigate('/catalogo');
                  }}
                >
                  Esci
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-outline-light btn-sm">Accedi</Link>
                <Link to="/registrazione" className="btn btn-light btn-sm">Registrati</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
