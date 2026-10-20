import { Link } from 'react-router-dom';
import Avatar from './Avatar.jsx';
import { ScoreRing, Stars } from './ui.jsx';

export default function MatchCard({ m, onRequest, compact }) {
  return (
    <div className="card match-card">
      <div className="d-flex gap-3 align-items-center">
        <Avatar name={m.user.name} size={50} />
        <div className="flex-grow-1 overflow-hidden">
          <Link to={`/users/${m.user.id}`} className="fw-bold text-decoration-none d-block text-truncate" style={{ color: 'var(--ink)' }}>
            {m.user.name}
          </Link>
          <div className="small text-muted text-truncate"><i className="bi bi-geo-alt me-1" />{m.user.location || 'Online'}</div>
          <Stars value={m.user.averageRating} size=".8rem" />
        </div>
        <ScoreRing score={m.score} />
      </div>
      {m.mutual && <div className="mt-3"><span className="mutual-tag"><i className="bi bi-arrow-left-right me-1" />MUTUAL SWAP</span></div>}
      {m.theyCanTeachYou.length > 0 && <>
        <div className="swap-line">Can teach you</div>
        <div>{m.theyCanTeachYou.map((s) => <span key={s} className="chip chip-teach">{s}</span>)}</div>
      </>}
      {m.youCanTeachThem.length > 0 && <>
        <div className="swap-line">Wants to learn from you</div>
        <div>{m.youCanTeachThem.map((s) => <span key={s} className="chip chip-learn">{s}</span>)}</div>
      </>}
      {!compact && m.reasons.length > 0 && (
        <div className="mt-2">
          {m.reasons.slice(0, 3).map((r) => <div key={r} className="reason"><i className="bi bi-check2 me-1" />{r}</div>)}
        </div>
      )}
      <div className="d-flex gap-2 mt-auto pt-3">
        <Link to={`/users/${m.user.id}`} className="btn btn-outline-secondary btn-sm flex-grow-1">View profile</Link>
        <button className="btn btn-primary btn-sm flex-grow-1" onClick={() => onRequest(m)}>
          <i className="bi bi-send me-1" />Request swap
        </button>
      </div>
    </div>
  );
}
