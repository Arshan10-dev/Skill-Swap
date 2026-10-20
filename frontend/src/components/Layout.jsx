import { useEffect, useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import api from '../api/client';
import Avatar from './Avatar.jsx';
import Logo from './Logo.jsx';

const userNav = [
  { to: '/dashboard', icon: 'grid-1x2', label: 'Dashboard' },
  { to: '/matches', icon: 'stars', label: 'Matches' },
  { to: '/search', icon: 'search', label: 'Search' },
  { to: '/skills', icon: 'lightning-charge', label: 'My Skills' },
  { to: '/requests', icon: 'arrow-left-right', label: 'Requests' },
  { to: '/chat', icon: 'chat-dots', label: 'Messages' },
  { to: '/sessions', icon: 'calendar2-week', label: 'Sessions' },
  { to: '/notifications', icon: 'bell', label: 'Notifications', badge: true },
];

const adminNav = [
  { to: '/admin', icon: 'speedometer2', label: 'Analytics', end: true },
  { to: '/admin/users', icon: 'people', label: 'Users' },
  { to: '/admin/skills', icon: 'tags', label: 'Skills & Categories' },
  { to: '/admin/reports', icon: 'flag', label: 'Reports' },
  { to: '/notifications', icon: 'bell', label: 'Notifications', badge: true },
];

export default function Layout({ children }) {
  const { user, logout, isAdmin } = useAuth();
  const [unread, setUnread] = useState(0);
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    let alive = true;
    const load = () => api.get('/notifications/unread-count').then((r) => alive && setUnread(r.data.count)).catch(() => {});
    load();
    const t = setInterval(load, 15000);
    return () => { alive = false; clearInterval(t); };
  }, [location.pathname]);

  useEffect(() => setOpen(false), [location.pathname]);

  const nav = isAdmin ? adminNav : userNav;

  return (
    <div className="app-shell">
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <div className="sidebar-brand"><Logo /></div>
        <nav className="flex-grow-1">
          {nav.map((n) => (
            <NavLink key={n.to} to={n.to} end={n.end} className="side-link">
              <i className={`bi bi-${n.icon}`} />
              <span>{n.label}</span>
              {n.badge && unread > 0 && <span className="side-badge">{unread > 99 ? '99+' : unread}</span>}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-foot">
          <NavLink to={isAdmin ? '/settings' : '/profile'} className="me-chip">
            <Avatar name={user?.name} size={36} />
            <div className="overflow-hidden">
              <div className="fw-semibold text-truncate">{user?.name}</div>
              <div className="small opacity-75">{isAdmin ? 'Administrator' : `${user?.reputation ?? 0} rep`}</div>
            </div>
          </NavLink>
          <div className="d-flex gap-2 mt-2">
            <NavLink to="/settings" className="btn btn-sm btn-ghost flex-grow-1"><i className="bi bi-gear me-1" />Settings</NavLink>
            <button className="btn btn-sm btn-ghost" title="Log out" onClick={() => { logout(); navigate('/'); }}>
              <i className="bi bi-box-arrow-right" />
            </button>
          </div>
        </div>
      </aside>
      {open && <div className="sidebar-scrim" onClick={() => setOpen(false)} />}
      <main className="main">
        <div className="topbar d-lg-none">
          <button className="btn btn-light" onClick={() => setOpen(true)} aria-label="Menu"><i className="bi bi-list" /></button>
          <Logo small />
          <NavLink to="/notifications" className="btn btn-light position-relative">
            <i className="bi bi-bell" />
            {unread > 0 && <span className="dot" />}
          </NavLink>
        </div>
        <div className="page">{children}</div>
      </main>
    </div>
  );
}
