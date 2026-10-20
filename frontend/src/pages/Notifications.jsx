import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { Empty, Loading, PageHead, timeAgo } from '../components/ui.jsx';

const icons = {
  REQUEST: ['arrow-left-right', 'ic-indigo'], REQUEST_UPDATE: ['check2-circle', 'ic-mint'], MESSAGE: ['chat-dots', 'ic-indigo'],
  SESSION: ['calendar2-week', 'ic-amber'], REMINDER: ['alarm', 'ic-coral'], REVIEW: ['star', 'ic-amber'],
  BADGE: ['award', 'ic-coral'], SYSTEM: ['shield-check', 'ic-indigo'],
};

export default function Notifications() {
  const [list, setList] = useState(null);
  const navigate = useNavigate();
  const load = () => api.get('/notifications').then((r) => setList(r.data));
  useEffect(() => { load(); }, []);

  const open = async (n) => {
    if (!n.read) await api.post(`/notifications/${n.id}/read`);
    if (n.link) navigate(n.link); else load();
  };
  const readAll = async () => { await api.post('/notifications/read-all'); load(); };

  if (!list) return <Loading />;
  return (
    <>
      <PageHead title="Notifications" sub="Requests, sessions, reminders, reviews and badges.">
        {list.some((n) => !n.read) && <button className="btn btn-soft" onClick={readAll}><i className="bi bi-check2-all me-1" />Mark all read</button>}
      </PageHead>
      <div className="card">
        {list.length === 0 ? <Empty icon="bell" title="You're all caught up" /> : list.map((n) => {
          const [ic, cls] = icons[n.type] || icons.SYSTEM;
          return (
            <div key={n.id} className="list-row" style={{ cursor: 'pointer', background: n.read ? undefined : '#FAFAFF' }} onClick={() => open(n)}>
              <div className={`stat-icon ${cls}`} style={{ width: 40, height: 40, fontSize: '1.05rem' }}><i className={`bi bi-${ic}`} /></div>
              <div className="flex-grow-1">
                <div className={n.read ? '' : 'fw-semibold'}>{n.message}</div>
              </div>
              {!n.read && <span style={{ width: 9, height: 9, borderRadius: '50%', background: 'var(--coral)' }} />}
            </div>
          );
        })}
      </div>
    </>
  );
}
