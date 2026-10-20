import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext.jsx';
import MatchCard from '../components/MatchCard.jsx';
import RequestModal from '../components/RequestModal.jsx';
import Avatar from '../components/Avatar.jsx';
import { Empty, Loading, fmtDate, timeAgo } from '../components/ui.jsx';

export default function Dashboard() {
  const { user, updateUser } = useAuth();
  const [data, setData] = useState(null);
  const [target, setTarget] = useState(null);

  const load = () => Promise.all([
    api.get('/users/me'), api.get('/matches?limit=4'), api.get('/requests/incoming'), api.get('/sessions'),
  ]).then(([me, matches, incoming, sessions]) => {
    setData({ me: me.data, matches: matches.data, incoming: incoming.data.filter((r) => r.status === 'PENDING'), sessions: sessions.data });
    updateUser({ reputation: me.data.reputation, averageRating: me.data.averageRating });
  });

  useEffect(() => { load(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  if (!data) return <Loading />;

  const upcoming = data.sessions.filter((s) => s.status === 'SCHEDULED').sort((a, b) => new Date(a.scheduledAt) - new Date(b.scheduledAt));
  const needsSkills = data.me.teaches.length === 0 || data.me.learns.length === 0;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Hi, {user.name.split(' ')[0]} 👋</h1>
          <p className="text-muted mb-0">Here's what's happening with your skill swaps.</p>
        </div>
        <Link to="/matches" className="btn btn-coral"><i className="bi bi-stars me-1" />Find partners</Link>
      </div>

      {needsSkills && (
        <div className="card card-pad mb-4 d-flex flex-row align-items-center gap-3" style={{ background: 'var(--coral-soft)', borderColor: '#FFD9CC' }}>
          <i className="bi bi-lightning-charge-fill fs-3" style={{ color: 'var(--coral)' }} />
          <div className="flex-grow-1">
            <div className="fw-bold">Complete your skills to get matched</div>
            <div className="small text-muted">Add at least one skill you can teach and one you want to learn.</div>
          </div>
          <Link to="/skills" className="btn btn-coral btn-sm">Add skills</Link>
        </div>
      )}

      <div className="row g-3 mb-4">
        {[
          ['award', 'ic-indigo', data.me.reputation, 'Reputation points'],
          ['star', 'ic-amber', data.me.averageRating ?? '–', 'Average rating'],
          ['check2-circle', 'ic-mint', data.me.completedSessions, 'Sessions completed'],
          ['patch-check', 'ic-coral', data.me.badges.length, 'Badges earned'],
        ].map(([icon, cls, v, l]) => (
          <div className="col-6 col-xl-3" key={l}>
            <div className="card stat"><div className={`stat-icon ${cls}`}><i className={`bi bi-${icon}`} /></div>
              <div><div className="stat-value">{v}</div><div className="stat-label">{l}</div></div></div>
          </div>
        ))}
      </div>

      <div className="row g-4">
        <div className="col-xl-8">
          <div className="section-title">Top matches for you <Link to="/matches">See all</Link></div>
          {data.matches.length === 0
            ? <div className="card"><Empty icon="stars" title="No matches yet">Add more skills to improve your recommendations.</Empty></div>
            : <div className="row g-3">{data.matches.map((m) => (
                <div className="col-md-6" key={m.user.id}><MatchCard m={m} compact onRequest={(x) => setTarget(x)} /></div>
              ))}</div>}
        </div>
        <div className="col-xl-4">
          <div className="section-title">Upcoming sessions <Link to="/sessions">All</Link></div>
          <div className="card mb-4">
            {upcoming.length === 0 ? <Empty icon="calendar2" title="Nothing scheduled">Schedule a session with an accepted partner.</Empty>
              : upcoming.slice(0, 3).map((s) => {
                const d = new Date(s.scheduledAt);
                return (
                  <div className="list-row" key={s.id}>
                    <div className="date-tile"><div className="d">{d.getDate()}</div><div className="m">{d.toLocaleString('en', { month: 'short' })}</div></div>
                    <div className="overflow-hidden">
                      <div className="fw-semibold text-truncate">{s.title}</div>
                      <div className="small text-muted">with {s.partner.name} · {fmtDate(s.scheduledAt)}</div>
                    </div>
                  </div>
                );
              })}
          </div>
          <div className="section-title">Pending requests <Link to="/requests">Manage</Link></div>
          <div className="card">
            {data.incoming.length === 0 ? <Empty icon="inbox" title="No new requests" />
              : data.incoming.slice(0, 4).map((r) => (
                <Link to="/requests" className="list-row text-decoration-none" style={{ color: 'inherit' }} key={r.id}>
                  <Avatar name={r.sender.name} size={38} />
                  <div className="overflow-hidden flex-grow-1">
                    <div className="fw-semibold">{r.sender.name}</div>
                    <div className="small text-muted text-truncate">wants {r.requestedSkill || 'to swap'} · {timeAgo(r.createdAt)}</div>
                  </div>
                  <span className="pill pill-primary">{r.compatibilityScore}%</span>
                </Link>
              ))}
          </div>
        </div>
      </div>
      <RequestModal show={!!target} target={target?.user} onClose={() => setTarget(null)} onSent={load}
        suggestLearn={target?.theyCanTeachYou} suggestTeach={target?.youCanTeachThem} />
    </>
  );
}
