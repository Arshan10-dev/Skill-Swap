import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import { useToast } from '../context/ToastContext.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import Avatar from '../components/Avatar.jsx';
import { Empty, Loading, Modal, PageHead, StatusPill, fmtDate } from '../components/ui.jsx';

const toLocalInput = (d) => {
  const x = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
  return x.toISOString().slice(0, 16);
};

export default function Sessions() {
  const toast = useToast();
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const [sessions, setSessions] = useState(null);
  const [partners, setPartners] = useState([]);
  const [tab, setTab] = useState('upcoming');
  const [form, setForm] = useState(null);
  const [review, setReview] = useState(null);

  const load = () => {
    api.get('/sessions').then((r) => setSessions(r.data));
    api.get('/requests/active').then((r) => setPartners(r.data.filter((x) => x.status === 'ACCEPTED')));
  };
  useEffect(() => { load(); }, []);

  const openForm = (requestId) => {
    const t = new Date(Date.now() + 86400000); t.setMinutes(0, 0, 0); t.setHours(18);
    setForm({ id: null, requestId: requestId || '', title: '', scheduledAt: toLocalInput(t), durationMinutes: 60, mode: 'ONLINE', locationOrLink: '', notes: '' });
  };

  useEffect(() => {
    const rid = params.get('request');
    if (rid) { openForm(rid); setParams({}, { replace: true }); }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const save = async () => {
    try {
      const body = { ...form, requestId: +form.requestId, durationMinutes: +form.durationMinutes, scheduledAt: form.scheduledAt + ':00' };
      if (form.id) await api.put(`/sessions/${form.id}`, body);
      else await api.post('/sessions', body);
      toast(form.id ? 'Session updated' : 'Session scheduled. Your partner has been notified.');
      setForm(null);
      load();
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  const act = async (id, action, msg) => {
    try { await api.post(`/sessions/${id}/${action}`); toast(msg); load(); }
    catch (e) { toast(errMsg(e), 'danger'); }
  };

  const submitReview = async () => {
    try {
      await api.post('/reviews', { sessionId: review.session.id, rating: review.rating, comment: review.comment });
      toast('Thanks! Your review was posted.');
      setReview(null);
      load();
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  if (!sessions) return <Loading />;
  const now = Date.now();
  const upcoming = sessions.filter((s) => s.status === 'SCHEDULED').sort((a, b) => new Date(a.scheduledAt) - new Date(b.scheduledAt));
  const history = sessions.filter((s) => s.status !== 'SCHEDULED');
  const list = tab === 'upcoming' ? upcoming : history;
  const toReview = history.filter((s) => s.status === 'COMPLETED' && !s.reviewedByMe).length;

  return (
    <>
      <PageHead title="Sessions" sub="Schedule skill-exchange sessions and keep a record of every one.">
        <div className="ss-tabs">
          <button className={tab === 'upcoming' ? 'active' : ''} onClick={() => setTab('upcoming')}>Upcoming ({upcoming.length})</button>
          <button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}>
            History{toReview > 0 && <span className="count">{toReview}</span>}
          </button>
        </div>
        <button className="btn btn-primary" onClick={() => openForm()} disabled={partners.length === 0}>
          <i className="bi bi-calendar-plus me-1" />Schedule
        </button>
      </PageHead>
      {partners.length === 0 && upcoming.length === 0 && (
        <div className="alert alert-light border">You need an accepted swap request before scheduling. <Link to="/matches">Find a partner</Link></div>
      )}
      <div className="card">
        {list.length === 0 ? <Empty icon="calendar2-week" title={tab === 'upcoming' ? 'No upcoming sessions' : 'No past sessions yet'} /> : list.map((s) => {
          const d = new Date(s.scheduledAt);
          const started = d.getTime() <= now;
          return (
            <div className="list-row flex-wrap" key={s.id}>
              <div className={`date-tile ${s.status !== 'SCHEDULED' ? 'past' : ''}`}>
                <div className="d">{d.getDate()}</div><div className="m">{d.toLocaleString('en', { month: 'short' })}</div>
              </div>
              <div className="flex-grow-1" style={{ minWidth: 220 }}>
                <div className="d-flex gap-2 align-items-center flex-wrap">
                  <span className="fw-bold">{s.title}</span><StatusPill status={s.status} />
                </div>
                <div className="small text-muted">
                  <i className="bi bi-clock me-1" />{fmtDate(s.scheduledAt)} · {s.durationMinutes} min ·{' '}
                  <i className={`bi bi-${s.mode === 'ONLINE' ? 'camera-video' : 'geo-alt'} me-1`} />{s.mode === 'ONLINE' ? 'Online' : 'In person'}
                </div>
                <div className="small d-flex align-items-center gap-2 mt-1">
                  <Avatar name={s.partner.name} size={22} /> with <Link to={`/users/${s.partner.id}`}>{s.partner.name}</Link>
                  {s.locationOrLink && (s.locationOrLink.startsWith('http')
                    ? <a href={s.locationOrLink} target="_blank" rel="noreferrer" className="ms-2"><i className="bi bi-link-45deg" />Join link</a>
                    : <span className="ms-2 text-muted">{s.locationOrLink}</span>)}
                </div>
                {s.notes && <div className="small text-muted mt-1">{s.notes}</div>}
              </div>
              <div className="d-flex gap-2 flex-wrap">
                {s.status === 'SCHEDULED' && <>
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => act(s.id, 'cancel', 'Session cancelled')}>Cancel</button>
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => setForm({ ...s, scheduledAt: toLocalInput(d), requestId: s.requestId })}>Reschedule</button>
                  <button className="btn btn-primary btn-sm" disabled={!started} title={started ? '' : 'Available after the session starts'}
                    onClick={() => act(s.id, 'complete', 'Session completed! +20 reputation')}>
                    <i className="bi bi-check2-circle me-1" />Mark complete
                  </button>
                </>}
                {s.status === 'COMPLETED' && !s.reviewedByMe &&
                  <button className="btn btn-coral btn-sm" onClick={() => setReview({ session: s, rating: 5, comment: '' })}><i className="bi bi-star me-1" />Rate {s.partner.name.split(' ')[0]}</button>}
                {s.status === 'COMPLETED' && s.reviewedByMe && <span className="small text-muted"><i className="bi bi-check2 me-1" />Reviewed</span>}
              </div>
            </div>
          );
        })}
      </div>

      <Modal show={!!form} title={form?.id ? 'Reschedule session' : 'Schedule a session'} onClose={() => setForm(null)}
        footer={<><button className="btn btn-outline-secondary" onClick={() => setForm(null)}>Cancel</button>
          <button className="btn btn-primary" onClick={save} disabled={!form?.requestId || !form?.title}>{form?.id ? 'Save' : 'Schedule'}</button></>}>
        {form && <>
          {!form.id && <div className="mb-3"><label className="form-label">Partner</label>
            <select className="form-select" value={form.requestId} onChange={(e) => setForm({ ...form, requestId: e.target.value })}>
              <option value="">Choose a partner…</option>
              {partners.map((p) => {
                const other = p.sender.id === user.id ? p.receiver : p.sender;
                return <option key={p.id} value={p.id}>{other.name} · {[p.offeredSkill, p.requestedSkill].filter(Boolean).join(' ⇄ ')}</option>;
              })}
            </select></div>}
          {!form.id && <div className="mb-3"><label className="form-label">Title</label>
            <input className="form-control" maxLength={120} placeholder="e.g. Intro to Spring Boot" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>}
          <div className="row g-3 mb-3">
            <div className="col-7"><label className="form-label">Date & time</label>
              <input type="datetime-local" className="form-control" value={form.scheduledAt} onChange={(e) => setForm({ ...form, scheduledAt: e.target.value })} /></div>
            <div className="col-5"><label className="form-label">Duration</label>
              <select className="form-select" value={form.durationMinutes} onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })}>
                {[30, 45, 60, 90, 120].map((m) => <option key={m} value={m}>{m} min</option>)}
              </select></div>
          </div>
          <div className="mb-3"><label className="form-label">Mode</label>
            <div className="ss-tabs w-100">
              {['ONLINE', 'IN_PERSON'].map((m) => <button type="button" key={m} className={`flex-grow-1 ${form.mode === m ? 'active' : ''}`}
                onClick={() => setForm({ ...form, mode: m })}>{m === 'ONLINE' ? 'Online' : 'In person'}</button>)}
            </div></div>
          <div className="mb-3"><label className="form-label">{form.mode === 'ONLINE' ? 'Meeting link' : 'Location'}</label>
            <input className="form-control" maxLength={300} placeholder={form.mode === 'ONLINE' ? 'https://meet.google.com/…' : 'Café, library…'}
              value={form.locationOrLink || ''} onChange={(e) => setForm({ ...form, locationOrLink: e.target.value })} /></div>
          <div><label className="form-label">Notes</label>
            <textarea className="form-control" rows={2} maxLength={1000} value={form.notes || ''} onChange={(e) => setForm({ ...form, notes: e.target.value })} /></div>
        </>}
      </Modal>

      <Modal show={!!review} title={`Rate ${review?.session.partner.name}`} onClose={() => setReview(null)}
        footer={<><button className="btn btn-outline-secondary" onClick={() => setReview(null)}>Later</button>
          <button className="btn btn-primary" onClick={submitReview}>Post review</button></>}>
        {review && <>
          <div className="text-muted small mb-2">{review.session.title}</div>
          <div className="rating-input mb-3">
            {[1, 2, 3, 4, 5].map((i) => <i key={i} className={`bi bi-star${review.rating >= i ? '-fill' : ''}`} onClick={() => setReview({ ...review, rating: i })} />)}
          </div>
          <textarea className="form-control" rows={3} maxLength={1000} placeholder="How was the session?" value={review.comment}
            onChange={(e) => setReview({ ...review, comment: e.target.value })} />
        </>}
      </Modal>
    </>
  );
}
