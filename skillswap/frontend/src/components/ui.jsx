// Small shared UI pieces
export function Stars({ value, size = '1rem' }) {
  if (value == null) return <span className="text-muted small no-rating">No ratings yet</span>;
  return (
    <span className="stars" style={{ fontSize: size }}>
      {[1, 2, 3, 4, 5].map((i) => (
        <i key={i} className={`bi ${value >= i ? 'bi-star-fill' : value >= i - 0.5 ? 'bi-star-half' : 'bi-star'}`} />
      ))}
      <span className="ms-1 fw-semibold">{value.toFixed(1)}</span>
    </span>
  );
}

export function ScoreRing({ score, size = 64 }) {
  const r = 26, c = 2 * Math.PI * r;
  const color = score >= 75 ? '#10B981' : score >= 50 ? '#6366F1' : '#F59E0B';
  return (
    <div className="score-ring" style={{ width: size, height: size }}>
      <svg viewBox="0 0 64 64" width={size} height={size}>
        <circle cx="32" cy="32" r={r} fill="none" stroke="#EEF0F6" strokeWidth="6" />
        <circle cx="32" cy="32" r={r} fill="none" stroke={color} strokeWidth="6" strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c * (1 - score / 100)} transform="rotate(-90 32 32)" />
      </svg>
      <span style={{ color }}>{score}</span>
    </div>
  );
}

export function Empty({ icon = 'inbox', title, children }) {
  return (
    <div className="empty">
      <i className={`bi bi-${icon}`} />
      <h6 className="mt-3 mb-1">{title}</h6>
      <div className="text-muted small">{children}</div>
    </div>
  );
}

export function Loading() {
  return <div className="text-center py-5"><div className="spinner-border text-primary" /></div>;
}

export function PageHead({ title, sub, children }) {
  return (
    <div className="page-head">
      <div>
        <h1>{title}</h1>
        {sub && <p className="text-muted mb-0">{sub}</p>}
      </div>
      <div className="d-flex gap-2 flex-wrap">{children}</div>
    </div>
  );
}

export function StatusPill({ status }) {
  const map = {
    PENDING: 'warning', ACCEPTED: 'success', REJECTED: 'secondary', CANCELLED: 'secondary', COMPLETED: 'primary',
    SCHEDULED: 'info', OPEN: 'danger', RESOLVED: 'success', DISMISSED: 'secondary',
  };
  return <span className={`pill pill-${map[status] || 'secondary'}`}>{status.charAt(0) + status.slice(1).toLowerCase()}</span>;
}

export function Modal({ show, title, onClose, children, footer }) {
  if (!show) return null;
  return (
    <div className="ss-modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="ss-modal card">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="mb-0">{title}</h5>
          <button className="btn-close" onClick={onClose} />
        </div>
        {children}
        {footer && <div className="d-flex justify-content-end gap-2 mt-4">{footer}</div>}
      </div>
    </div>
  );
}

export const levelLabel = (l) => l.charAt(0) + l.slice(1).toLowerCase();

export const fmtDate = (d) => new Date(d).toLocaleString('en-IN', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' });
export const fmtDay = (d) => new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
export const timeAgo = (d) => {
  const s = (Date.now() - new Date(d).getTime()) / 1000;
  if (s < 60) return 'just now';
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
};

export const DAYS = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
