import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../hooks';
import { logout } from '../authSlice';

export default function Navbar() {
  const { email, ruolo } = useAppSelector((s) => s.auth);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  return (
    <header className="nav">
      <div>
        <Link to="/catalogo" className="brand">
          <span>Bib</span>
          <span>BiblioAPP</span>
        </Link>

        <nav >
          <NavLink to="/catalogo" className={"navlink"}>Catalogo</NavLink>
          {email && <NavLink to="/miei-prestiti" className={"navlink"}>I miei prestiti</NavLink>}
          {ruolo === 'STAFF' && <NavLink to="/prestiti" className={"navlink"}>Prestiti</NavLink>}
          {ruolo === 'STAFF' && <NavLink to="/staff" className={"navlink"}>Gestione</NavLink>}
        </nav>

        <div>
          {email ? (
            <>
              <span>
                {email}
                <span>
                  {ruolo === 'STAFF' ? 'Staff' : 'Utente'}
                </span>
              </span>
              <button
                className="btn"
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
              <Link to="/login">Accedi</Link>
              <Link to="/registrazione">Registrati</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
