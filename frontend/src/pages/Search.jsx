import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import Avatar from '../components/Avatar.jsx';
import RequestModal from '../components/RequestModal.jsx';
import { Empty, Loading, PageHead, Stars, DAYS } from '../components/ui.jsx';

export default function Search() {
  const [cats, setCats] = useState([]);
  const [q, setQ] = useState({ q: '', categoryId: '', level: '', location: '', day: '', type: 'TEACH' });
  const [results, setResults] = useState(null);
  const [target, setTarget] = useState(null);

  useEffect(() => { api.get('/categories').then((r) => setCats(r.data)); }, []);
  useEffect(() => {
    const t = setTimeout(() => {
      const p = new URLSearchParams(Object.entries(q).filter(([, v]) => v));
      api.get(`/search?${p}`).then((r) => setResults(r.data));
    }, 250);
    return () => clearTimeout(t);
  }, [q]);

  const set = (k) => (e) => setQ({ ...q, [k]: e.target.value });

  return (
    <>
      <PageHead title="Search members" sub="Find people by skill, category, level, location or availability." />
      <div className="card card-pad mb-4">
        <div className="row g-3">
          <div className="col-lg-4">
            <div className="input-group">
              <span className="input-group-text bg-white"><i className="bi bi-search" /></span>
              <input className="form-control" placeholder="Skill name, e.g. Python" value={q.q} onChange={set('q')} />
            </div>
          </div>
          <div className="col-6 col-lg-2">
            <select className="form-select" value={q.type} onChange={set('type')}>
              <option value="TEACH">who teach it</option><option value="LEARN">who want to learn it</option>
            </select>
          </div>
          <div className="col-6 col-lg-2">
            <select className="form-select" value={q.categoryId} onChange={set('categoryId')}>
              <option value="">All categories</option>
              {cats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="col-4 col-lg-1">
            <select className="form-select" value={q.level} onChange={set('level')}>
              <option value="">Level</option><option>BEGINNER</option><option>INTERMEDIATE</option><option>EXPERT</option>
            </select>
          </div>
          <div className="col-4 col-lg-2"><input className="form-control" placeholder="City" value={q.location} onChange={set('location')} /></div>
          <div className="col-4 col-lg-1">
            <select className="form-select" value={q.day} onChange={set('day')}>
              <option value="">Day</option>{DAYS.map((d) => <option key={d}>{d}</option>)}
            </select>
          </div>
        </div>
      </div>
      {!results ? <Loading /> : results.length === 0 ? <div className="card"><Empty icon="search" title="No members found">Try a broader search.</Empty></div> : (
        <div className="card">
          {results.map((r) => (
            <div className="list-row flex-wrap" key={r.user.id}>
              <Avatar name={r.user.name} size={46} />
              <div style={{ minWidth: 180 }}>
                <Link to={`/users/${r.user.id}`} className="fw-bold text-decoration-none" style={{ color: 'var(--ink)' }}>{r.user.name}</Link>
                <div className="small text-muted"><i className="bi bi-geo-alt me-1" />{r.user.location || 'Online'} · {r.user.reputation} rep</div>
                <Stars value={r.user.averageRating} size=".78rem" />
              </div>
              <div className="flex-grow-1">
                <div>{r.teaches.map((s) => <span key={s} className="chip chip-teach">{s}</span>)}</div>
                <div>{r.learns.map((s) => <span key={s} className="chip chip-learn">{s}</span>)}</div>
              </div>
              <button className="btn btn-soft btn-sm" onClick={() => setTarget(r.user)}><i className="bi bi-send me-1" />Request</button>
            </div>
          ))}
        </div>
      )}
      <RequestModal show={!!target} target={target} onClose={() => setTarget(null)} />
    </>
  );
}
