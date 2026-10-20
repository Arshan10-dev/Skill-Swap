import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import { useToast } from '../context/ToastContext.jsx';
import Avatar from '../components/Avatar.jsx';
import { Empty, Loading, PageHead, StatusPill, Stars, timeAgo } from '../components/ui.jsx';

export default function Requests() {
  const toast = useToast();
  const [tab, setTab] = useState('incoming');
  const [data, setData] = useState(null);

  const load = () => Promise.all([api.get('/requests/incoming'), api.get('/requests/outgoing')])
    .then(([a, b]) => setData({ incoming: a.data, outgoing: b.data }));
  useEffect(() => { load(); }, []);

  const act = async (id, action, msg) => {
    try { await api.post(`/requests/${id}/${action}`); toast(msg); load(); }
    catch (e) { toast(errMsg(e), 'danger'); }
  };

  if (!data) return <Loading />;
  const list = data[tab];
  const pending = data.incoming.filter((r) => r.status === 'PENDING').length;

  return (
    <>
      <PageHead title="Swap requests" sub="Accept, reject and track your skill-exchange requests.">
        <div className="ss-tabs">
          <button className={tab === 'incoming' ? 'active' : ''} onClick={() => setTab('incoming')}>
            Received{pending > 0 && <span className="count">{pending}</span>}
          </button>
          <button className={tab === 'outgoing' ? 'active' : ''} onClick={() => setTab('outgoing')}>Sent</button>
        </div>
      </PageHead>
      <div className="card">
        {list.length === 0 ? <Empty icon="arrow-left-right" title={tab === 'incoming' ? 'No requests received' : 'No requests sent'}>
          {tab === 'outgoing' && <Link to="/matches">Find a partner</Link>}
        </Empty> : list.map((r) => {
          const other = tab === 'incoming' ? r.sender : r.receiver;
          const iTeach = tab === 'incoming' ? r.requestedSkill : r.offeredSkill;
          const iLearn = tab === 'incoming' ? r.offeredSkill : r.requestedSkill;
          return (
            <div className="list-row flex-wrap align-items-start" key={r.id}>
              <Avatar name={other.name} size={48} />
              <div className="flex-grow-1" style={{ minWidth: 220 }}>
                <div className="d-flex align-items-center gap-2 flex-wrap">
                  <Link to={`/users/${other.id}`} className="fw-bold text-decoration-none" style={{ color: 'var(--ink)' }}>{other.name}</Link>
                  <StatusPill status={r.status} />
                  <span className="pill pill-primary">{r.compatibilityScore}% match</span>
                  <span className="small text-muted">{timeAgo(r.createdAt)}</span>
                </div>
                <Stars value={other.averageRating} size=".78rem" />
                <div className="mt-2">
                  {iLearn && <span className="chip chip-teach"><i className="bi bi-arrow-down-left" />You learn {iLearn}</span>}
                  {iTeach && <span className="chip chip-learn"><i className="bi bi-arrow-up-right" />You teach {iTeach}</span>}
                </div>
                {r.message && <div className="small mt-1 p-2 rounded" style={{ background: '#F6F7FB' }}>"{r.message}"</div>}
              </div>
              <div className="d-flex gap-2 flex-wrap">
                {tab === 'incoming' && r.status === 'PENDING' && <>
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => act(r.id, 'reject', 'Request declined')}>Decline</button>
                  <button className="btn btn-primary btn-sm" onClick={() => act(r.id, 'accept', `You and ${other.name} are now partners!`)}>
                    <i className="bi bi-check-lg me-1" />Accept
                  </button>
                </>}
                {tab === 'outgoing' && r.status === 'PENDING' &&
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => act(r.id, 'cancel', 'Request cancelled')}>Cancel</button>}
                {r.status === 'ACCEPTED' && <>
                  <Link to={`/chat/${r.id}`} className="btn btn-soft btn-sm"><i className="bi bi-chat-dots me-1" />Chat</Link>
                  <Link to={`/sessions?request=${r.id}`} className="btn btn-primary btn-sm"><i className="bi bi-calendar-plus me-1" />Schedule</Link>
                </>}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
