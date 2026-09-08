import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { AuthResponse, Ruolo } from './types';

interface AuthState {
  token: string | null;
  email: string | null;
  ruolo: Ruolo | null;
}

const STORAGE_KEY = 'biblioteca_auth';

function loadState(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      return JSON.parse(raw) as AuthState;
    }
  } catch {
    // stato non leggibile: si riparte da vuoto
  }
  return { token: null, email: null, ruolo: null };
}

const authSlice = createSlice({
  name: 'auth',
  initialState: loadState(),
  reducers: {
    setCredentials(state, action: PayloadAction<AuthResponse>) {
      state.token = action.payload.token;
      state.email = action.payload.email;
      state.ruolo = action.payload.ruolo;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    },
    logout(state) {
      state.token = null;
      state.email = null;
      state.ruolo = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
