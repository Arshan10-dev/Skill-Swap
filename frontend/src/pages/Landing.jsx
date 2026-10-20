import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/client';
import Logo from '../components/Logo.jsx';
import Avatar from '../components/Avatar.jsx';
import { ScoreRing } from '../components/ui.jsx';

const features = [
  ['stars', 'ic-indigo', 'Intelligent matching', 'A compatibility score built from what you teach, what you want to learn, skill level and availability.'],
  ['arrow-left-right', 'ic-coral', 'Two-way swaps', 'Send a request, get accepted, and both of you learn. No fees, no courses, just peers.'],
  ['chat-dots', 'ic-mint', 'Built-in chat', 'Talk to matched partners and plan your first session without leaving the platform.'],
  ['calendar2-week', 'ic-amber', 'Session scheduling', 'Schedule online or in-person sessions, get reminders, and keep a full history.'],
  ['award', 'ic-indigo', 'Reputation & badges', 'Ratings, reviews, reputation points and badges show who is a reliable partner.'],
  ['shield-check', 'ic-coral', 'Safe by design', 'Report or block anyone, with an admin team moderating the community.'],
];

export default function Landing() {
  const [stats, setStats] = useState(null);
  useEffect(() => { api.get('/stats/public').then((r) => setStats(r.data)).catch(() => {}); }, []);

  return (
    <div>
      <section className="hero pb-5">
        <div className="container">
          <div className="landing-nav">
            <Logo />
            <div className="d-flex gap-2">
              <Link to="/login" className="btn btn-ghost">Log in</Link>
              <Link to="/register" className="btn btn-coral">Join free</Link>
            </div>
          </div>
          <div className="row align-items-center py-5 g-5">
            <div className="col-lg-6">
              <span className="badge rounded-pill mb-3" style={{ background: 'rgba(255,122,89,.15)', color: '#FFB49F' }}>
                Peer-to-peer learning
              </span>
              <h1>Teach what you know.<br /><em>Learn</em> what you don't.</h1>
              <p className="lead mt-4 mb-4" style={{ maxWidth: 520 }}>
                SkillSwap connects you with people who want to learn your skills and can teach you theirs.
                Get matched, chat, schedule a session and swap.
              </p>
              <div className="d-flex gap-2 flex-wrap">
                <Link to="/register" className="btn btn-coral btn-lg px-4">Find my match</Link>
                <Link to="/login" className="btn btn-ghost btn-lg px-4">I have an account</Link>
              </div>
              {stats && (
                <div className="d-flex gap-4 mt-5">
                  <div><div className="h3 text-white mb-0">{stats.users}</div><div className="small">members</div></div>
                  <div><div className="h3 text-white mb-0">{stats.skills}</div><div className="small">skills listed</div></div>
                  <div><div className="h3 text-white mb-0">{stats.completedSessions}</div><div className="small">sessions completed</div></div>
                </div>
              )}
            </div>
            <div className="col-lg-6 position-relative" style={{ minHeight: 360 }}>
              <div className="hero-card float-a" style={{ maxWidth: 380 }}>
                <div className="d-flex align-items-center gap-3">
                  <Avatar name="Kabir Mehta" size={48} />
                  <div className="flex-grow-1">
                    <div className="fw-bold text-white">Kabir Mehta</div>
                    <div className="small">Bengaluru · 5.0 ★</div>
                  </div>
                  <ScoreRing score={88} size={58} />
                </div>
                <div className="swap-line" style={{ color: '#A5B4FC' }}>Teaches you</div>
                <span className="chip chip-teach">Spring Boot</span><span className="chip chip-teach">SQL</span>
                <div className="swap-line" style={{ color: '#A5B4FC' }}>Learns from you</div>
                <span className="chip chip-learn">Guitar</span>
              </div>
              <div className="hero-card float-b ms-auto mt-4" style={{ maxWidth: 330 }}>
                <div className="d-flex align-items-center gap-2 mb-2">
                  <i className="bi bi-calendar-check text-success" />
                  <span className="fw-semibold text-white">Session confirmed</span>
                </div>
                <div className="small">Spring Data JPA &amp; MySQL · Sat, 6:30 PM · Online</div>
              </div>
              <div className="hero-card float-a mt-4" style={{ maxWidth: 300 }}>
                <div className="d-flex gap-2 align-items-center">
                  <i className="bi bi-award-fill" style={{ color: '#FBBF24' }} />
                  <span className="text-white fw-semibold">Badge earned: Five-Star Mentor</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="container py-5">
        <div className="text-center mb-5">
          <h2 className="fw-bold">Everything a skill exchange needs</h2>
          <p className="text-muted">From the first match to the final review.</p>
        </div>
        <div className="row g-4">
          {features.map(([icon, cls, title, text]) => (
            <div className="col-md-6 col-lg-4" key={title}>
              <div className="card feature">
                <div className={`stat-icon ${cls} mb-3`}><i className={`bi bi-${icon}`} /></div>
                <h5>{title}</h5>
                <p className="text-muted mb-0">{text}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="container pb-5">
        <div className="card card-pad p-lg-5">
          <div className="row g-4 align-items-center">
            <div className="col-lg-4">
              <h2 className="fw-bold">How it works</h2>
              <p className="text-muted">Five steps from sign-up to your first swap.</p>
            </div>
            <div className="col-lg-8">
              {['Create a profile with skills you can teach and want to learn',
                'Our matching engine scores compatible partners for you',
                'Send a swap request and get accepted',
                'Chat, then schedule a session online or in person',
                'Rate each other and build your reputation'].map((s, i) => (
                <div className="d-flex gap-3 align-items-center mb-3" key={s}>
                  <div className="step-num">{i + 1}</div><div className="fw-semibold">{s}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <footer className="text-center text-muted small py-4">SkillSwap · A skill exchange and matching platform</footer>
    </div>
  );
}
