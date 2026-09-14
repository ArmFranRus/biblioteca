import { useState } from 'react';
import type { FormEvent } from 'react';
import {
  useGetCategoriesQuery,
  useCreateCategoryMutation,
  useDeleteCategoryMutation,
  useGetAuthorsQuery,
  useCreateAuthorMutation,
  useDeleteAuthorMutation,
  useCreateBookMutation,
} from '../api';

function CategoriePanel() {
  const { data: categorie } = useGetCategoriesQuery();
  const [createCategory] = useCreateCategoryMutation();
  const [deleteCategory] = useDeleteCategoryMutation();
  const [nome, setNome] = useState('');
  const [err, setErr] = useState<string | null>(null);

  async function add(e: FormEvent) {
    e.preventDefault();
    setErr(null);
    if (!nome.trim()) return;
    try {
      await createCategory({ nome: nome.trim() }).unwrap();
      setNome('');
    } catch {
      setErr('Categoria già esistente.');
    }
  }

  async function remove(id: number) {
    setErr(null);
    try {
      await deleteCategory(id).unwrap();
    } catch {
      setErr('Categoria in uso: impossibile eliminarla.');
    }
  }

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <h2 className="h5 mb-3">Categorie</h2>
        <div className="d-flex flex-wrap gap-2 mb-3">
          {categorie?.map((c) => (
            <span key={c.id} className="badge text-bg-light border d-inline-flex align-items-center gap-1">
              {c.nome}
              <button type="button" className="btn-close btn-close-sm" aria-label={`Elimina ${c.nome}`}
                style={{ fontSize: '.6rem' }} onClick={() => remove(c.id)} />
            </span>
          ))}
        </div>
        {err && <div className="alert alert-danger py-2">{err}</div>}
        <form className="input-group" onSubmit={add}>
          <input className="form-control" placeholder="Nuova categoria" value={nome} onChange={(e) => setNome(e.target.value)} />
          <button className="btn btn-success">Aggiungi</button>
        </form>
      </div>
    </div>
  );
}

function AutoriPanel() {
  const { data: autori } = useGetAuthorsQuery();
  const [createAuthor] = useCreateAuthorMutation();
  const [deleteAuthor] = useDeleteAuthorMutation();
  const [nome, setNome] = useState('');
  const [err, setErr] = useState<string | null>(null);

  async function add(e: FormEvent) {
    e.preventDefault();
    setErr(null);
    if (!nome.trim()) return;
    try {
      await createAuthor({ nome: nome.trim() }).unwrap();
      setNome('');
    } catch {
      setErr("Impossibile creare l'autore.");
    }
  }

  async function remove(id: number) {
    setErr(null);
    try {
      await deleteAuthor(id).unwrap();
    } catch {
      setErr('Autore associato a dei libri: impossibile eliminarlo.');
    }
  }

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <h2 className="h5 mb-3">Autori</h2>
        <div className="d-flex flex-wrap gap-2 mb-3">
          {autori?.map((a) => (
            <span key={a.id} className="badge text-bg-light border d-inline-flex align-items-center gap-1">
              {a.nome}
              <button type="button" className="btn-close btn-close-sm" aria-label={`Elimina ${a.nome}`}
                style={{ fontSize: '.6rem' }} onClick={() => remove(a.id)} />
            </span>
          ))}
        </div>
        {err && <div className="alert alert-danger py-2">{err}</div>}
        <form className="input-group" onSubmit={add}>
          <input className="form-control" placeholder="Nuovo autore" value={nome} onChange={(e) => setNome(e.target.value)} />
          <button className="btn btn-success">Aggiungi</button>
        </form>
      </div>
    </div>
  );
}

function NuovoLibroPanel() {
  const { data: categorie } = useGetCategoriesQuery();
  const { data: autori } = useGetAuthorsQuery();
  const [createBook, { isLoading }] = useCreateBookMutation();

  const [titolo, setTitolo] = useState('');
  const [isbn, setIsbn] = useState('');
  const [editore, setEditore] = useState('');
  const [anno, setAnno] = useState('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [autoriIds, setAutoriIds] = useState<number[]>([]);
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  function toggleAutore(id: number) {
    setAutoriIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setMsg(null);
    setErr(null);
    try {
      await createBook({
        titolo: titolo.trim(),
        isbn: isbn.trim() || null,
        editore: editore.trim() || null,
        anno: anno ? Number(anno) : null,
        categoriaId: categoriaId === '' ? null : categoriaId,
        autoriIds,
      }).unwrap();
      setMsg('Libro creato.');
      setTitolo('');
      setIsbn('');
      setEditore('');
      setAnno('');
      setCategoriaId('');
      setAutoriIds([]);
    } catch {
      setErr('Impossibile creare il libro. Controlla i dati (ISBN duplicato?).');
    }
  }

  return (
    <div className="card shadow-sm">
      <div className="card-body">
        <h2 className="h5 mb-3">Nuovo libro</h2>
        <form onSubmit={submit}>
          <div className="row g-3">
            <div className="col-12">
              <label className="form-label">Titolo</label>
              <input className="form-control" value={titolo} onChange={(e) => setTitolo(e.target.value)} required />
            </div>
            <div className="col-md-4">
              <label className="form-label">ISBN</label>
              <input className="form-control" value={isbn} onChange={(e) => setIsbn(e.target.value)} required />
            </div>
            <div className="col-md-5">
              <label className="form-label">Editore</label>
              <input className="form-control" value={editore} onChange={(e) => setEditore(e.target.value)} />
            </div>
            <div className="col-md-3">
              <label className="form-label">Anno</label>
              <input type="number" className="form-control" value={anno} onChange={(e) => setAnno(e.target.value)} />
            </div>
            <div className="col-md-6">
              <label className="form-label">Categoria</label>
              <select className="form-select" value={categoriaId}
                onChange={(e) => setCategoriaId(e.target.value === '' ? '' : Number(e.target.value))}>
                <option value="">-</option>
                {categorie?.map((c) => <option key={c.id} value={c.id}>{c.nome}</option>)}
              </select>
            </div>
            <div className="col-12">
              <label className="form-label d-block">Autori</label>
              {autori && autori.length > 0 ? (
                <div className="d-flex flex-wrap gap-2">
                  {autori.map((a) => (
                    <button type="button" key={a.id}
                      className={`btn btn-sm ${autoriIds.includes(a.id) ? 'btn-success' : 'btn-outline-secondary'}`}
                      onClick={() => toggleAutore(a.id)}>
                      {a.nome}
                    </button>
                  ))}
                </div>
              ) : (
                <span className="text-muted small">Crea prima un autore.</span>
              )}
            </div>
          </div>
          {msg && <div className="alert alert-success py-2 mt-3">{msg}</div>}
          {err && <div className="alert alert-danger py-2 mt-3">{err}</div>}
          <button className="btn btn-success mt-3" disabled={isLoading}>
            {isLoading ? 'Salvataggio...' : 'Crea libro'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default function StaffPage() {
  return (
    <section>
      <div className="mb-4">
        <p className="text-success text-uppercase fw-semibold small mb-1" style={{ letterSpacing: '.1em' }}>Gestione</p>
        <h1 className="h3 fw-semibold mb-0">Amministrazione del catalogo</h1>
      </div>

      <div className="row g-3 mb-3">
        <div className="col-md-6"><CategoriePanel /></div>
        <div className="col-md-6"><AutoriPanel /></div>
      </div>
      <NuovoLibroPanel />
    </section>
  );
}
