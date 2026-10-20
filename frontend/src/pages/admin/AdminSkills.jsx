import { useEffect, useState } from 'react';
import api, { errMsg } from '../../api/client';
import { useToast } from '../../context/ToastContext.jsx';
import { Loading, PageHead } from '../../components/ui.jsx';

export default function AdminSkills() {
  const toast = useToast();
  const [cats, setCats] = useState(null);
  const [skills, setSkills] = useState([]);
  const [active, setActive] = useState(null);
  const [newCat, setNewCat] = useState('');
  const [newSkill, setNewSkill] = useState('');
  const [editing, setEditing] = useState(null);

  const load = () => {
    api.get('/categories').then((r) => { setCats(r.data); if (!active && r.data[0]) setActive(r.data[0].id); });
    api.get('/skills').then((r) => setSkills(r.data));
  };
  useEffect(() => { load(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const run = async (fn, msg) => { try { await fn(); toast(msg); load(); } catch (e) { toast(errMsg(e), 'danger'); } };

  if (!cats) return <Loading />;
  const list = skills.filter((s) => s.categoryId === active);

  return (
    <>
      <PageHead title="Skills & categories" sub="Organise the skill catalogue members pick from." />
      <div className="row g-4">
        <div className="col-lg-4">
          <div className="card">
            <div className="card-pad pb-2"><h6>Categories</h6>
              <form className="d-flex gap-2" onSubmit={(e) => { e.preventDefault(); run(() => api.post('/admin/categories', { name: newCat }), 'Category added').then(() => setNewCat('')); }}>
                <input className="form-control form-control-sm" placeholder="New category" required value={newCat} onChange={(e) => setNewCat(e.target.value)} />
                <button className="btn btn-primary btn-sm"><i className="bi bi-plus-lg" /></button>
              </form>
            </div>
            {cats.map((c) => (
              <div key={c.id} className={`chat-item ${active === c.id ? 'active' : ''}`} onClick={() => setActive(c.id)}>
                <div className="flex-grow-1 fw-semibold">{c.name}</div>
                <span className="pill pill-secondary">{c.skillCount}</span>
                {c.skillCount === 0 && <i className="bi bi-trash text-danger" role="button"
                  onClick={(e) => { e.stopPropagation(); run(() => api.delete(`/admin/categories/${c.id}`), 'Category deleted'); }} />}
              </div>
            ))}
          </div>
        </div>
        <div className="col-lg-8">
          <div className="card">
            <div className="card-pad pb-2">
              <h6>Skills in {cats.find((c) => c.id === active)?.name}</h6>
              <form className="d-flex gap-2" onSubmit={(e) => { e.preventDefault(); run(() => api.post('/admin/skills', { name: newSkill, categoryId: active }), 'Skill added').then(() => setNewSkill('')); }}>
                <input className="form-control form-control-sm" placeholder="New skill name" required value={newSkill} onChange={(e) => setNewSkill(e.target.value)} />
                <button className="btn btn-primary btn-sm text-nowrap"><i className="bi bi-plus-lg me-1" />Add skill</button>
              </form>
            </div>
            {list.map((s) => (
              <div className="list-row py-2" key={s.id}>
                {editing?.id === s.id ? (
                  <form className="d-flex gap-2 flex-grow-1" onSubmit={(e) => { e.preventDefault(); run(() => api.put(`/admin/skills/${s.id}`, { name: editing.name, categoryId: +editing.categoryId }), 'Skill updated').then(() => setEditing(null)); }}>
                    <input className="form-control form-control-sm" value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
                    <select className="form-select form-select-sm" value={editing.categoryId} onChange={(e) => setEditing({ ...editing, categoryId: e.target.value })}>
                      {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                    <button className="btn btn-primary btn-sm">Save</button>
                    <button type="button" className="btn btn-outline-secondary btn-sm" onClick={() => setEditing(null)}>Cancel</button>
                  </form>
                ) : <>
                  <div className="flex-grow-1 fw-semibold">{s.name}</div>
                  <button className="btn btn-light btn-sm" onClick={() => setEditing({ id: s.id, name: s.name, categoryId: s.categoryId })}><i className="bi bi-pencil" /></button>
                  <button className="btn btn-light btn-sm text-danger" onClick={() => run(() => api.delete(`/admin/skills/${s.id}`), 'Skill deleted')}><i className="bi bi-trash" /></button>
                </>}
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}
