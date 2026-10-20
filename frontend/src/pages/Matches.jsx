import { useEffect, useState } from 'react';
import api from '../api/client';
import MatchCard from '../components/MatchCard.jsx';
import RequestModal from '../components/RequestModal.jsx';
import { Empty, Loading, PageHead } from '../components/ui.jsx';
import { Link } from 'react-router-dom';

export default function Matches() {
  const [matches, setMatches] = useState(null);
  const [skills, setSkills] = useState([]);
  const [filter, setFilter] = useState({ skillId: '', minScore: 0, mutualOnly: false });
  const [target, setTarget] = useState(null);

  useEffect(() => { api.get('/skills').then((r) => setSkills(r.data)); }, []);
  useEffect(() => {
    setMatches(null);
    const p = new URLSearchParams({ minScore: filter.minScore, limit: 60 });
    if (filter.skillId) p.set('skillId', filter.skillId);
    api.get(`/matches?${p}`).then((r) => setMatches(r.data));
  }, [filter.skillId, filter.minScore]);

  const shown = matches?.filter((m) => !filter.mutualOnly || m.mutual);

  return (
    <>
      <PageHead title="Recommended partners" sub="Ranked by compatibility: skills, level, availability and preferences." />
      <div className="card card-pad mb-4">
        <div className="row g-3 align-items-end">
          <div className="col-md-5">
            <label className="form-label">They can teach</label>
            <select className="form-select" value={filter.skillId} onChange={(e) => setFilter({ ...filter, skillId: e.target.value })}>
              <option value="">Any of my wanted skills</option>
              {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Minimum score: {filter.minScore}</label>
            <input type="range" className="form-range" min={0} max={90} step={10} value={filter.minScore}
              onChange={(e) => setFilter({ ...filter, minScore: +e.target.value })} />
          </div>
          <div className="col-md-3">
            <div className="form-check form-switch mb-2">
              <input className="form-check-input" type="checkbox" id="mutual" checked={filter.mutualOnly}
                onChange={(e) => setFilter({ ...filter, mutualOnly: e.target.checked })} />
              <label className="form-check-label fw-semibold" htmlFor="mutual">Mutual swaps only</label>
            </div>
          </div>
        </div>
      </div>
      {!shown ? <Loading /> : shown.length === 0 ? (
        <div className="card"><Empty icon="stars" title="No matches found">
          Try lowering the minimum score, or <Link to="/skills">add more skills</Link> to your profile.
        </Empty></div>
      ) : (
        <div className="row g-3">
          {shown.map((m) => <div className="col-md-6 col-xl-4" key={m.user.id}><MatchCard m={m} onRequest={setTarget} /></div>)}
        </div>
      )}
      <RequestModal show={!!target} target={target?.user} onClose={() => setTarget(null)}
        suggestLearn={target?.theyCanTeachYou} suggestTeach={target?.youCanTeachThem} />
    </>
  );
}
