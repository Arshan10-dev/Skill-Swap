import Avatar from '../components/Avatar.jsx';
import { Stars, fmtDay, levelLabel, timeAgo, Empty } from '../components/ui.jsx';

const badgeIcon = {
  FIRST_SWAP: 'flag', REGULAR: 'arrow-repeat', VETERAN: 'trophy', FIVE_STAR: 'star', TOP_RATED: 'gem',
  MULTI_TALENT: 'lightning', FEEDBACK_HERO: 'chat-heart', TRUSTED: 'shield-check',
};

/** Shared read-only profile layout used by my profile and other users' profiles. */
export default function ProfileView({ p, reviews, actions }) {
  return (
    <>
      <div className="profile-hero mb-4">
        <div className="d-flex gap-4 align-items-center flex-wrap position-relative" style={{ zIndex: 1 }}>
          <Avatar name={p.name} size={84} />
          <div className="flex-grow-1">
            <h2 className="mb-1">{p.name}</h2>
            <div className="small mb-2">
              <i className="bi bi-geo-alt me-1" />{p.location || 'Location not set'} · Member since {fmtDay(p.memberSince)}
            </div>
            <Stars value={p.averageRating} />
          </div>
          <div className="d-flex gap-4">
            <div className="hero-stat"><div className="v">{p.reputation}</div><div className="l">Reputation</div></div>
            <div className="hero-stat"><div className="v">{p.completedSessions}</div><div className="l">Sessions</div></div>
            <div className="hero-stat"><div className="v">{p.reviewCount}</div><div className="l">Reviews</div></div>
          </div>
          {actions && <div className="d-flex gap-2 flex-wrap w-100">{actions}</div>}
        </div>
      </div>
      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card card-pad mb-4">
            <h6>About</h6>
            <p className="mb-3">{p.bio || <span className="text-muted">No bio yet.</span>}</p>
            <div className="d-flex gap-4 flex-wrap small">
              <div><span className="text-muted">Available:</span> <b>{p.availableDays?.split(',').join(', ') || '–'}</b></div>
              <div><span className="text-muted">Time:</span> <b>{p.preferredTime ? levelLabel(p.preferredTime) : '–'}</b></div>
              <div><span className="text-muted">Mode:</span> <b>{p.preferredMode ? levelLabel(p.preferredMode.replace('_', ' ')) : '–'}</b></div>
            </div>
          </div>
          <div className="card card-pad mb-4">
            <h6>Can teach</h6>
            <div>{p.teaches.length === 0 ? <div className="text-muted small">None listed</div> : p.teaches.map((s) => (
              <span key={s.id} className="chip chip-teach">{s.skillName} · {levelLabel(s.level)}{s.yearsExperience ? ` · ${s.yearsExperience}y` : ''}</span>
            ))}</div>
            <h6 className="mt-3">Wants to learn</h6>
            <div>{p.learns.length === 0 ? <div className="text-muted small">None listed</div> : p.learns.map((s) => (
              <span key={s.id} className="chip chip-learn">{s.skillName}</span>
            ))}</div>
          </div>
          <div className="card">
            <div className="card-pad pb-2"><h6 className="mb-0">Reviews ({reviews.length})</h6></div>
            {reviews.length === 0 ? <Empty icon="chat-square-quote" title="No reviews yet" /> : reviews.map((r) => (
              <div className="list-row align-items-start" key={r.id}>
                <Avatar name={r.reviewer.name} size={38} />
                <div className="flex-grow-1">
                  <div className="d-flex justify-content-between flex-wrap gap-2">
                    <span className="fw-semibold">{r.reviewer.name}</span>
                    <span className="small text-muted">{timeAgo(r.createdAt)}</span>
                  </div>
                  <Stars value={r.rating} size=".8rem" />
                  <div className="small text-muted">{r.sessionTitle}</div>
                  {r.comment && <div className="mt-1">{r.comment}</div>}
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="col-lg-5">
          <div className="card card-pad">
            <h6>Badges & achievements</h6>
            {p.badges.length === 0 ? <div className="text-muted small">Complete sessions and earn reviews to unlock badges.</div> : (
              <div className="d-grid gap-2">
                {p.badges.map((b) => (
                  <div className="badge-tile" key={b.code}>
                    <i className={`bi bi-${badgeIcon[b.code] || 'award'}-fill`} />
                    <div><div className="fw-bold">{b.name}</div><div className="small text-muted">{b.description}</div></div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
