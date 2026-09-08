import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAppSelector } from '../hooks';

export default function RequireStaff({ children }: { children: ReactNode }) {
  const ruolo = useAppSelector((s) => s.auth.ruolo);
  if (ruolo !== 'STAFF') {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}
