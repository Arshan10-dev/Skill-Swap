import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import { useToast } from '../context/ToastContext.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import Avatar from '../components/Avatar.jsx';
import { Empty, PageHead, fmtDay } from '../components/ui.jsx';

export default function Settings() {
  const toast = useToast();
  const { user, isAdmin } = useAuth();
  const [blocked, setBlocked] = useState([]);
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' });

  const load = () => api.get('/blocks').then((r) => setBlocked(r.data));
  useEffect(() => { if (!isAdmin) load(); }, [isAdmin]);

  const change = async (e) => {
    e.preventDefault();
    if (pw.newPassword !== pw.confirm) return toast('New passwords do not match', 'danger');
    try {
      await api.put('/users/me/password', { currentPassword: pw.currentPassword, newPassword: pw.newPassword });
      toast('Password changed');
      setPw({ currentPassword: '', newPassword: '', confirm: '' });
    } catch (err) { toast(errMsg(err), 'danger'); }
  };

  const unblock = async (id) => { await api.delete(`/blocks/${id}`); toast('User unblocked'); load(); };

  return (
    <>
      <PageHead title="Settings" sub={user.email} />
      <div className="row g-4">
        <div className="col-lg-6">
          <div className="card card-pad">
            <h6 className="mb-3">Change password</h6>
            <form onSubmit={change}>
              <div className="mb-3"><label className="form-label">Current password</label>
                <input type="password" className="form-control" required value={pw.currentPassword} onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} /></div>
              <div className="mb-3"><label className="form-label">New password</label>
                <input type="password" className="form-control" required minLength={6} value={pw.newPassword} onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} /></div>
              <div className="mb-3"><label className="form-label">Confirm new password</label>
                <input type="password" className="form-control" required minLength={6} value={pw.confirm} onChange={(e) => setPw({ ...pw, confirm: e.target.value })} /></div>
              <button className="btn btn-primary">Update password</button>
            </form>
          </div>
        </div>
        {!isAdmin && (
          <div className="col-lg-6">
            <div className="card">
              <div className="card-pad pb-2"><h6 className="mb-0">Blocked users</h6>
                <div className="small text-muted">Blocked users can't message you, send requests, or appear in your matches.</div></div>
              {blocked.length === 0 ? <Empty icon="slash-circle" title="No blocked users" /> : blocked.map((b) => (
                <div className="list-row" key={b.id}>
                  <Avatar name={b.name} size={36} />
                  <div className="flex-grow-1"><div className="fw-semibold">{b.name}</div><div className="small text-muted">Blocked {fmtDay(b.blockedAt)}</div></div>
                  <button className="btn btn-outline-secondary btn-sm" onClick={() => unblock(b.id)}>Unblock</button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </>
  );
}
