import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import { useToast } from '../context/ToastContext.jsx';
import { Empty, Loading, PageHead, levelLabel } from '../components/ui.jsx';

function AddForm({ type, skills, cats, onAdded }) {
  const toast = useToast();
  const blank = { skillId: '', newSkillName: '', categoryId: '', level: type === 'TEACH' ? 'INTERMEDIATE' : 'BEGINNER', yearsExperience: 0 };
  const [f, setF] = useState(blank);
  const [custom, setCustom] = useState(false);

  const add = async (e) => {
    e.preventDefault();
    try {
      await api.post('/me/skills', {
        type, level: f.level, yearsExperience: +f.yearsExperience,
        ...(custom ? { newSkillName: f.newSkillName, categoryId: +f.categoryId } : { skillId: +f.skillId }),
      });
      toast('Skill added');
      setF(blank); setCustom(false);
      onAdded();
    } catch (err) { toast(errMsg(err), 'danger'); }
  };

  const grouped = cats.map((c) => ({ ...c, skills: skills.filter((s) => s.categoryId === c.id) }));

  return (
    <form onSubmit={add} className="row g-2 align-items-end">
      <div className="col-md-5">
        <label className="form-label">Skill</label>
        {custom ? (
          <div className="d-flex gap-2">
            <input className="form-control" placeholder="New skill name" required maxLength={80} value={f.newSkillName}
              onChange={(e) => setF({ ...f, newSkillName: e.target.value })} />
            <select className="form-select" required value={f.categoryId} onChange={(e) => setF({ ...f, categoryId: e.target.value })}>
              <option value="">Category</option>{cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
        ) : (
          <select className="form-select" required value={f.skillId} onChange={(e) => setF({ ...f, skillId: e.target.value })}>
            <option value="">Choose a skill…</option>
            {grouped.map((g) => <optgroup key={g.id} label={g.name}>{g.skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}</optgroup>)}
          </select>
        )}
      </div>
      <div className="col-6 col-md-3">
        <label className="form-label">{type === 'TEACH' ? 'My level' : 'Current level'}</label>
        <select className="form-select" value={f.level} onChange={(e) => setF({ ...f, level: e.target.value })}>
          <option value="BEGINNER">Beginner</option><option value="INTERMEDIATE">Intermediate</option><option value="EXPERT">Expert</option>
        </select>
      </div>
      {type === 'TEACH' && (
        <div className="col-6 col-md-2">
          <label className="form-label">Years</label>
          <input type="number" min={0} max={60} className="form-control" value={f.yearsExperience}
            onChange={(e) => setF({ ...f, yearsExperience: e.target.value })} />
        </div>
      )}
      <div className="col-md-2"><button className="btn btn-primary w-100"><i className="bi bi-plus-lg" /> Add</button></div>
      <div className="col-12">
        <button type="button" className="btn btn-link btn-sm px-0" onClick={() => setCustom(!custom)}>
          {custom ? 'Pick from the list instead' : "Can't find your skill? Add a new one"}
        </button>
      </div>
    </form>
  );
}

export default function MySkills() {
  const toast = useToast();
  const [params] = useSearchParams();
  const [mine, setMine] = useState(null);
  const [skills, setSkills] = useState([]);
  const [cats, setCats] = useState([]);

  const load = () => {
    api.get('/me/skills').then((r) => setMine(r.data));
    api.get('/skills').then((r) => setSkills(r.data));
  };
  useEffect(() => { load(); api.get('/categories').then((r) => setCats(r.data)); }, []);

  const remove = async (id) => {
    await api.delete(`/me/skills/${id}`);
    toast('Skill removed');
    load();
  };

  if (!mine) return <Loading />;

  const section = (type, title, sub, chip) => {
    const list = mine.filter((s) => s.type === type);
    return (
      <div className="card card-pad mb-4">
        <div className="d-flex align-items-center gap-2 mb-1">
          <div className={`stat-icon ${type === 'TEACH' ? 'ic-mint' : 'ic-coral'}`} style={{ width: 36, height: 36, fontSize: '1rem' }}>
            <i className={`bi bi-${type === 'TEACH' ? 'mortarboard' : 'lightbulb'}`} />
          </div>
          <h5 className="mb-0">{title}</h5>
        </div>
        <p className="text-muted small">{sub}</p>
        <div className="mb-3">
          {list.length === 0 ? <Empty icon={type === 'TEACH' ? 'mortarboard' : 'lightbulb'} title="Nothing added yet" />
            : list.map((s) => (
              <span key={s.id} className={`chip ${chip}`} style={{ fontSize: '.85rem', padding: '.4rem .8rem' }}>
                {s.skillName}
                <span className="opacity-75 fw-normal">· {levelLabel(s.level)}{s.yearsExperience ? `, ${s.yearsExperience}y` : ''}</span>
                <i className="bi bi-x-lg x ms-1" onClick={() => remove(s.id)} title="Remove" />
              </span>
            ))}
        </div>
        <AddForm type={type} skills={skills} cats={cats} onAdded={load} />
      </div>
    );
  };

  return (
    <>
      <PageHead title="My skills" sub="What you can teach and what you want to learn. This drives your matches." />
      {params.get('welcome') && (
        <div className="alert alert-primary">Welcome to SkillSwap! Start by adding a few skills below, then check your matches.</div>
      )}
      {section('TEACH', 'Skills I can teach', 'Your level and experience help learners trust you.', 'chip-teach')}
      {section('LEARN', 'Skills I want to learn', 'We will find people who can teach these.', 'chip-learn')}
    </>
  );
}
