import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api, { errMsg } from '../api/client';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ProfileView from './ProfileView.jsx';
import RequestModal from '../components/RequestModal.jsx';
import { Loading, Modal } from '../components/ui.jsx';

const REASONS = ['Inappropriate behaviour', 'Harassment', 'Spam or scam', 'No-show for sessions', 'Fake profile', 'Other'];

export default function UserProfile() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [p, setP] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [req, setReq] = useState(false);
  const [report, setReport] = useState(null);

  const load = () => {
    api.get(`/users/${id}`).then((r) => setP(r.data)).catch(() => navigate('/search'));
    api.get(`/users/${id}/reviews`).then((r) => setReviews(r.data));
  };
  useEffect(() => {
    if (String(user.id) === id) { navigate('/profile', { replace: true }); return; }
    load();
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const toggleBlock = async () => {
    try {
      if (p.blockedByMe) { await api.delete(`/blocks/${id}`); toast(`${p.name} unblocked`); }
      else { await api.post(`/blocks/${id}`); toast(`${p.name} blocked. They won't appear in your matches.`); }
      load();
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  const sendReport = async () => {
    try {
      await api.post('/reports', { reportedUserId: +id, reason: report.reason, details: report.details });
      toast('Report submitted. Our moderators will review it.');
      setReport(null);
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  if (!p) return <Loading />;

  return (
    <>
      <ProfileView p={p} reviews={reviews} actions={<>
        {!p.blockedByMe && <button className="btn btn-coral btn-sm" onClick={() => setReq(true)}><i className="bi bi-send me-1" />Request swap</button>}
        <button className="btn btn-ghost btn-sm" onClick={toggleBlock}>
          <i className={`bi bi-${p.blockedByMe ? 'unlock' : 'slash-circle'} me-1`} />{p.blockedByMe ? 'Unblock' : 'Block'}
        </button>
        <button className="btn btn-ghost btn-sm" onClick={() => setReport({ reason: REASONS[0], details: '' })}>
          <i className="bi bi-flag me-1" />Report
        </button>
      </>} />
      <RequestModal show={req} target={p} onClose={() => setReq(false)} />
      <Modal show={!!report} title={`Report ${p.name}`} onClose={() => setReport(null)}
        footer={<><button className="btn btn-outline-secondary" onClick={() => setReport(null)}>Cancel</button>
          <button className="btn btn-danger" onClick={sendReport}>Submit report</button></>}>
        {report && <>
          <div className="mb-3"><label className="form-label">Reason</label>
            <select className="form-select" value={report.reason} onChange={(e) => setReport({ ...report, reason: e.target.value })}>
              {REASONS.map((r) => <option key={r}>{r}</option>)}
            </select></div>
          <div><label className="form-label">Details (optional)</label>
            <textarea className="form-control" rows={3} maxLength={1000} value={report.details}
              onChange={(e) => setReport({ ...report, details: e.target.value })} /></div>
        </>}
      </Modal>
    </>
  );
}
