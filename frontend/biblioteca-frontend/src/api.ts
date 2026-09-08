import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { RootState } from './store';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Categoria,
  Autore,
  Libro,
  Copia,
  LibroRequest,
  StatoCopia,
  StatoPrestito,
  Prestito,
  Utente,
  Recensione,
  Prenotazione,
  StatoPrenotazione,
  DashboardData,
} from './types';

export interface BooksFilter {
  titolo?: string;
  categoriaId?: number;
  autore?: string;
}

export const api = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.token;
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Books', 'Book', 'Categories', 'Authors', 'Copies', 'Loans', 'Reviews', 'Reservations', 'Dashboard'],
  endpoints: (builder) => ({
    login: builder.mutation<AuthResponse, LoginRequest>({
      query: (body) => ({ url: 'auth/login', method: 'POST', body }),
    }),
    register: builder.mutation<AuthResponse, RegisterRequest>({
      query: (body) => ({ url: 'auth/register', method: 'POST', body }),
    }),

    getBooks: builder.query<Libro[], BooksFilter | void>({
      query: (filter) => ({ url: 'books/find', params: filter ?? undefined }),
      providesTags: ['Books'],
    }),
    getBook: builder.query<Libro, number>({
      query: (id) => `books/find/${id}`,
      providesTags: (_res, _err, id) => [{ type: 'Book', id }],
    }),
    createBook: builder.mutation<Libro, LibroRequest>({
      query: (body) => ({ url: 'books/create', method: 'POST', body }),
      invalidatesTags: ['Books'],
    }),
    updateBook: builder.mutation<Libro, { id: number; body: LibroRequest }>({
      query: ({ id, body }) => ({ url: `books/update/${id}`, method: 'PUT', body }),
      invalidatesTags: (_res, _err, { id }) => ['Books', { type: 'Book', id }],
    }),
    deleteBook: builder.mutation<void, number>({
      query: (id) => ({ url: `books/delete/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Books'],
    }),

    getCategories: builder.query<Categoria[], void>({
      query: () => 'categories/all',
      providesTags: ['Categories'],
    }),
    createCategory: builder.mutation<Categoria, { nome: string }>({
      query: (body) => ({ url: 'categories/create', method: 'POST', body }),
      invalidatesTags: ['Categories'],
    }),
    deleteCategory: builder.mutation<void, number>({
      query: (id) => ({ url: `categories/delete/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Categories'],
    }),

    getAuthors: builder.query<Autore[], void>({
      query: () => 'authors/all',
      providesTags: ['Authors'],
    }),
    createAuthor: builder.mutation<Autore, { nome: string }>({
      query: (body) => ({ url: 'authors/create', method: 'POST', body }),
      invalidatesTags: ['Authors'],
    }),
    deleteAuthor: builder.mutation<void, number>({
      query: (id) => ({ url: `authors/delete/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Authors'],
    }),

    getCopies: builder.query<Copia[], number>({
      query: (libroId) => `books/all/${libroId}/copies`,
      providesTags: (_res, _err, libroId) => [{ type: 'Copies', id: libroId }],
    }),
    addCopy: builder.mutation<Copia, { libroId: number; codiceInventario: string }>({
      query: ({ libroId, codiceInventario }) => ({
        url: `books/add/${libroId}/copies`,
        method: 'POST',
        body: { codiceInventario },
      }),
      invalidatesTags: (_res, _err, { libroId }) => [
        { type: 'Copies', id: libroId },
        { type: 'Book', id: libroId },
        'Books',
      ],
    }),
    updateCopyState: builder.mutation<Copia, { id: number; libroId: number; stato: StatoCopia }>({
      query: ({ id, stato }) => ({ url: `copies/update/${id}`, method: 'PATCH', body: { stato } }),
      invalidatesTags: (_res, _err, { libroId }) => [
        { type: 'Copies', id: libroId },
        { type: 'Book', id: libroId },
        'Books',
      ],
    }),
    deleteCopy: builder.mutation<void, { id: number; libroId: number }>({
      query: ({ id }) => ({ url: `copies/delete/${id}`, method: 'DELETE' }),
      invalidatesTags: (_res, _err, { libroId }) => [
        { type: 'Copies', id: libroId },
        { type: 'Book', id: libroId },
        'Books',
      ],
    }),

    // ---- Utenti (staff) ----
    searchUsers: builder.query<Utente[], string>({
      query: (q) => ({ url: 'users/search', params: q ? { q } : undefined }),
    }),

    // ---- Prestiti ----
    getLoans: builder.query<Prestito[], { utenteId?: number; stato?: StatoPrestito } | void>({
      query: (filter) => ({ url: 'loans/all', params: filter ?? undefined }),
      providesTags: ['Loans'],
    }),
    getMyLoans: builder.query<Prestito[], void>({
      query: () => 'loans/mine',
      providesTags: ['Loans'],
    }),
    createLoan: builder.mutation<Prestito, { copiaId: number; utenteId: number; libroId: number }>({
      query: ({ copiaId, utenteId }) => ({
        url: 'loans/create',
        method: 'POST',
        body: { copiaId, utenteId },
      }),
      invalidatesTags: (_res, _err, { libroId }) => [
        'Loans',
        'Books',
        { type: 'Book', id: libroId },
        { type: 'Copies', id: libroId },
      ],
    }),
    returnLoan: builder.mutation<Prestito, { id: number; libroId: number }>({
      query: ({ id }) => ({ url: `loans/return/${id}`, method: 'POST' }),
      invalidatesTags: (_res, _err, { libroId }) => [
        'Loans',
        'Books',
        { type: 'Book', id: libroId },
        { type: 'Copies', id: libroId },
      ],
    }),

    // ---- Recensioni ----
    getReviews: builder.query<Recensione[], number>({
      query: (libroId) => `reviews/all/${libroId}`,
      providesTags: (_res, _err, libroId) => [{ type: 'Reviews', id: libroId }],
    }),
    createReview: builder.mutation<Recensione, { libroId: number; voto: number; testo: string }>({
      query: (body) => ({ url: 'reviews/create', method: 'POST', body }),
      invalidatesTags: (_res, _err, { libroId }) => [
        { type: 'Reviews', id: libroId },
        { type: 'Book', id: libroId },
        'Books',
      ],
    }),
    deleteReview: builder.mutation<void, { id: number; libroId: number }>({
      query: ({ id }) => ({ url: `reviews/delete/${id}`, method: 'DELETE' }),
      invalidatesTags: (_res, _err, { libroId }) => [
        { type: 'Reviews', id: libroId },
        { type: 'Book', id: libroId },
        'Books',
      ],
    }),

    // ---- Prenotazioni ----
    createReservation: builder.mutation<Prenotazione, { libroId: number }>({
      query: (body) => ({ url: 'reservations/create', method: 'POST', body }),
      invalidatesTags: ['Reservations', 'Dashboard'],
    }),
    cancelReservation: builder.mutation<void, number>({
      query: (id) => ({ url: `reservations/cancel/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Reservations', 'Dashboard'],
    }),
    getMyReservations: builder.query<Prenotazione[], void>({
      query: () => 'reservations/mine',
      providesTags: ['Reservations'],
    }),
    getReservations: builder.query<Prenotazione[], { stato?: StatoPrenotazione } | void>({
      query: (filter) => ({ url: 'reservations/all', params: filter ?? undefined }),
      providesTags: ['Reservations'],
    }),

    // ---- Dashboard ----
    getDashboard: builder.query<DashboardData, void>({
      query: () => 'dashboard',
      providesTags: ['Dashboard'],
    }),
  }),
});

export const {
  useLoginMutation,
  useRegisterMutation,
  useGetBooksQuery,
  useGetBookQuery,
  useCreateBookMutation,
  useUpdateBookMutation,
  useDeleteBookMutation,
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  useDeleteCategoryMutation,
  useGetAuthorsQuery,
  useCreateAuthorMutation,
  useDeleteAuthorMutation,
  useGetCopiesQuery,
  useAddCopyMutation,
  useUpdateCopyStateMutation,
  useDeleteCopyMutation,
  useSearchUsersQuery,
  useGetLoansQuery,
  useGetMyLoansQuery,
  useCreateLoanMutation,
  useReturnLoanMutation,
  useGetReviewsQuery,
  useCreateReviewMutation,
  useDeleteReviewMutation,
  useCreateReservationMutation,
  useCancelReservationMutation,
  useGetMyReservationsQuery,
  useGetReservationsQuery,
  useGetDashboardQuery,
} = api;
