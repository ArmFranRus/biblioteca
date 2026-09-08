import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Il backend Spring Boot gira su http://localhost:7979 (vedi server.port).
// Il proxy inoltra le chiamate a /api al backend, evitando problemi di CORS
// durante lo sviluppo. Se cambi la porta del backend, aggiornala qui.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:7979',
        changeOrigin: true,
      },
    },
  },
});
