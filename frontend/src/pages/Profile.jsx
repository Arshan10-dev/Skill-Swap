import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ProfileView from './ProfileView.jsx';
import { Loading, Modal, DAYS } from '../components/ui.jsx';

export default function Profile() {
  const { user, updateUser } = useAuth();
  const toast = useToast();
  const [p, setP] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [edit, setEdit] = useState(null);

  const load = () => api.get('/users/me').then((r) => {
    setP(r.data);
    api.get(`/users/${r.data.id}/reviews`).then((x) => setReviews(x.data));
  });
  useEffect(() => { load(); }, []);

  const open = () => setEdit({
    name: p.name, bio: p.bio || '', location: p.location || '',
    days: (p.availableDays || '').split(',').filter(Boolean),
    preferredTime: p.preferredTime || 'FLEXIBLE', preferredMode: p.preferredMode || 'ONLINE',
  });

  const save = async () => {
    try {
      const r = await api.put('/users/me', {
        name: edit.name, bio: edit.bio, location: edit.location, availableDays: edit.days.join(','),
        preferredTime: edit.preferredTime, preferredMode: edit.preferredMode,
      });
      setP(r.data);
      updateUser({ name: r.data.name, location: r.data.location });
      setEdit(null);
      toast('Profile updated');
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  if (!p) return <Loading />;
  const toggleDay = (d) => setEdit({ ...edit, days: edit.days.includes(d) ? edit.days.filter((x) => x !== d) : [...edit.days, d] });

  return (
    <>
      <ProfileView p={p} reviews={reviews}
        actions={<button className="btn btn-coral btn-sm" onClick={open}><i className="bi bi-pencil me-1" />Edit profile</button>} />
      <Modal show={!!edit} title="Edit profile" onClose={() => setEdit(null)}
        footer={<><button className="btn btn-outline-secondary" onClick={() => setEdit(null)}>Cancel</button>
          <button className="btn btn-primary" onClick={save}>Save changes</button></>}>
        {edit && <>
          <div className="mb-3"><label className="form-label">Name</label>
            <input className="form-control" value={edit.name} onChange={(e) => setEdit({ ...edit, name: e.target.value })} /></div>
          <div className="mb-3"><label className="form-label">Bio</label>
            <textarea className="form-control" rows={3} maxLength={1000} value={edit.bio} onChange={(e) => setEdit({ ...edit, bio: e.target.value })} /></div>
          <div className="mb-3"><label className="form-label">City</label>
            <input className="form-control" value={edit.location} onChange={(e) => setEdit({ ...edit, location: e.target.value })} /></div>
          <div className="mb-3"><label className="form-label d-block">Available days</label>
            <div className="day-toggle">{DAYS.map((d) => (
              <button type="button" key={d} className={edit.days.includes(d) ? 'on' : ''} onClick={() => toggleDay(d)}>{d}</button>
            ))}</div></div>
          <div className="row g-3">
            <div className="col"><label className="form-label">Preferred time</label>
              <select className="form-select" value={edit.preferredTime} onChange={(e) => setEdit({ ...edit, preferredTime: e.target.value })}>
                {['MORNING', 'AFTERNOON', 'EVENING', 'FLEXIBLE'].map((t) => <option key={t}>{t}</option>)}
              </select></div>
            <div className="col"><label className="form-label">Mode</label>
              <select className="form-select" value={edit.preferredMode} onChange={(e) => setEdit({ ...edit, preferredMode: e.target.value })}>
                <option value="ONLINE">Online</option><option value="IN_PERSON">In person</option><option value="BOTH">Both</option>
              </select></div>
          </div>
        </>}
      </Modal>
      {user && null}
    </>
  );
}
