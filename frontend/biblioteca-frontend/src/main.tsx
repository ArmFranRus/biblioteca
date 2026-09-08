import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './index.css';

import { store } from './store';
import App from './App';
import BooksPage from './pages/BooksPage';
import BookDetailPage from './pages/BookDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import StaffPage from './pages/StaffPage';
import LoansPage from './pages/LoansPage';
import MyLoansPage from './pages/MyLoansPage';
import ReservationsPage from './pages/ReservationsPage';
import MyReservationsPage from './pages/MyReservationsPage';
import DashboardPage from './pages/DashboardPage';
import RequireAuth from './components/RequireAuth';
import RequireStaff from './components/RequireStaff';
import UsersPage from './pages/UsersPage';

const router = createBrowserRouter([
  {
    element: <App />,
    children: [
      { index: true, element: <Navigate to="/catalogo" replace /> },
      { path: 'utenti', element: <RequireStaff><UsersPage /></RequireStaff> },
      { path: 'catalogo', element: <BooksPage /> },
      { path: 'catalogo/:id', element: <BookDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'registrazione', element: <RegisterPage /> },
      { path: 'miei-prestiti', element: <RequireAuth><MyLoansPage /></RequireAuth> },
      { path: 'mie-prenotazioni', element: <RequireAuth><MyReservationsPage /></RequireAuth> },
      { path: 'staff', element: <RequireStaff><StaffPage /></RequireStaff> },
      { path: 'prestiti', element: <RequireStaff><LoansPage /></RequireStaff> },
      { path: 'prenotazioni', element: <RequireStaff><ReservationsPage /></RequireStaff> },
      { path: 'dashboard', element: <RequireStaff><DashboardPage /></RequireStaff> },
      { path: '*', element: <Navigate to="/catalogo" replace /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
    <Provider store={store}>
      <RouterProvider router={router} />
    </Provider>
);
