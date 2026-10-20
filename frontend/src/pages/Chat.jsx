import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import Avatar from '../components/Avatar.jsx';
import { Empty, Loading, PageHead } from '../components/ui.jsx';

export default function Chat() {
  const { requestId } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [convos, setConvos] = useState(null);
  const [msgs, setMsgs] = useState([]);
  const [text, setText] = useState('');
  const bodyRef = useRef(null);
  const lastId = useRef(null);

  const loadConvos = () => api.get('/requests/active').then((r) => setConvos(r.data));
  useEffect(() => { loadConvos(); }, []);

  useEffect(() => {
    if (!requestId) return;
    let alive = true;
    lastId.current = null;
    setMsgs([]);
    const poll = async () => {
      try {
        const q = lastId.current ? `?after=${lastId.current}` : '';
        const r = await api.get(`/requests/${requestId}/messages${q}`);
        if (!alive || r.data.length === 0) return;
        lastId.current = r.data[r.data.length - 1].id;
        setMsgs((m) => [...m, ...r.data.filter((x) => !m.some((y) => y.id === x.id))]);
      } catch (e) { if (alive) toast(errMsg(e), 'danger'); }
    };
    poll();
    const t = setInterval(poll, 3000);
    return () => { alive = false; clearInterval(t); };
  }, [requestId]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => { bodyRef.current?.scrollTo(0, bodyRef.current.scrollHeight); }, [msgs]);

  const send = async (e) => {
    e.preventDefault();
    if (!text.trim()) return;
    try {
      const r = await api.post(`/requests/${requestId}/messages`, { content: text });
      setMsgs((m) => [...m, r.data]);
      lastId.current = r.data.id;
      setText('');
    } catch (err) { toast(errMsg(err), 'danger'); }
  };

  if (!convos) return <Loading />;
  const current = convos.find((c) => String(c.id) === requestId);
  const partner = (c) => (c.sender.id === user.id ? c.receiver : c.sender);

  return (
    <>
      <PageHead title="Messages" sub="Chat with partners from accepted swap requests." />
      {convos.length === 0 ? (
        <div className="card"><Empty icon="chat-dots" title="No conversations yet">Chat opens once a swap request is accepted. <Link to="/matches">Find partners</Link></Empty></div>
      ) : (
        <div className="card chat-wrap">
          <div className={`chat-list ${requestId ? 'hide-sm' : ''}`}>
            {convos.map((c) => {
              const o = partner(c);
              return (
                <Link to={`/chat/${c.id}`} key={c.id} className={`chat-item ${String(c.id) === requestId ? 'active' : ''}`}>
                  <Avatar name={o.name} size={42} />
                  <div className="overflow-hidden flex-grow-1">
                    <div className="fw-semibold">{o.name}</div>
                    <div className="small text-muted text-truncate">{[c.offeredSkill, c.requestedSkill].filter(Boolean).join(' ⇄ ')}</div>
                  </div>
                  {c.unreadMessages > 0 && String(c.id) !== requestId && <span className="side-badge">{c.unreadMessages}</span>}
                </Link>
              );
            })}
          </div>
          <div className={`chat-main ${!requestId ? 'hide-sm' : ''}`}>
            {!current ? <Empty icon="chat-left-text" title="Select a conversation" /> : <>
              <div className="chat-head">
                <button className="btn btn-light btn-sm d-md-none" onClick={() => navigate('/chat')}><i className="bi bi-arrow-left" /></button>
                <Avatar name={partner(current).name} size={40} />
                <div className="flex-grow-1">
                  <Link to={`/users/${partner(current).id}`} className="fw-bold text-decoration-none" style={{ color: 'var(--ink)' }}>{partner(current).name}</Link>
                  <div className="small text-muted">{current.status === 'COMPLETED' ? 'Exchange completed' : 'Active exchange'}</div>
                </div>
                {current.status === 'ACCEPTED' &&
                  <Link to={`/sessions?request=${current.id}`} className="btn btn-soft btn-sm"><i className="bi bi-calendar-plus me-1" />Schedule</Link>}
              </div>
              <div className="chat-body" ref={bodyRef}>
                {msgs.length === 0 && <div className="text-center text-muted small my-auto">Say hello and plan your first session 👋</div>}
                {msgs.map((m) => (
                  <div key={m.id} className={`bubble ${m.senderId === user.id ? 'me' : 'them'}`}>
                    {m.content}
                    <div className="t">{new Date(m.sentAt).toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' })}</div>
                  </div>
                ))}
              </div>
              {current.status === 'ACCEPTED' ? (
                <form className="chat-input" onSubmit={send}>
                  <input className="form-control" placeholder="Type a message…" value={text} maxLength={2000} onChange={(e) => setText(e.target.value)} />
                  <button className="btn btn-primary"><i className="bi bi-send" /></button>
                </form>
              ) : <div className="chat-input text-muted small justify-content-center">This exchange is closed.</div>}
            </>}
          </div>
        </div>
      )}
    </>
  );
}
