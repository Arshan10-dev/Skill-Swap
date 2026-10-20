import { useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import { Modal } from './ui.jsx';
import { useToast } from '../context/ToastContext.jsx';

/** Send a skill-swap request to `target` ({id, name}). Loads the target's teach list and my teach list. */
export default function RequestModal({ target, show, onClose, onSent, suggestLearn = [], suggestTeach = [] }) {
  const toast = useToast();
  const [mine, setMine] = useState([]);
  const [theirs, setTheirs] = useState([]);
  const [form, setForm] = useState({ offeredSkillId: '', requestedSkillId: '', message: '' });
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!show || !target) return;
    Promise.all([api.get('/me/skills'), api.get(`/users/${target.id}`)]).then(([a, b]) => {
      const myTeach = a.data.filter((s) => s.type === 'TEACH');
      setMine(myTeach);
      setTheirs(b.data.teaches);
      const off = myTeach.find((s) => suggestTeach.includes(s.skillName)) || myTeach[0];
      const req = b.data.teaches.find((s) => suggestLearn.includes(s.skillName)) || b.data.teaches[0];
      setForm({
        offeredSkillId: off?.skillId || '',
        requestedSkillId: req?.skillId || '',
        message: `Hi ${target.name.split(' ')[0]}! I'd love to swap skills with you.`,
      });
    });
  }, [show, target]); // eslint-disable-line react-hooks/exhaustive-deps

  const send = async () => {
    setBusy(true);
    try {
      await api.post('/requests', {
        receiverId: target.id,
        offeredSkillId: form.offeredSkillId || null,
        requestedSkillId: form.requestedSkillId || null,
        message: form.message,
      });
      toast(`Request sent to ${target.name}`);
      onSent?.();
      onClose();
    } catch (e) { toast(errMsg(e), 'danger'); } finally { setBusy(false); }
  };

  return (
    <Modal show={show} onClose={onClose} title={`Swap with ${target?.name ?? ''}`}
      footer={<>
        <button className="btn btn-outline-secondary" onClick={onClose}>Cancel</button>
        <button className="btn btn-primary" onClick={send} disabled={busy || (!form.offeredSkillId && !form.requestedSkillId)}>
          <i className="bi bi-send me-1" />Send request
        </button>
      </>}>
      <div className="mb-3">
        <label className="form-label">I want to learn</label>
        <select className="form-select" value={form.requestedSkillId} onChange={(e) => setForm({ ...form, requestedSkillId: e.target.value })}>
          <option value="">Nothing specific</option>
          {theirs.map((s) => <option key={s.skillId} value={s.skillId}>{s.skillName} ({s.level.toLowerCase()})</option>)}
        </select>
      </div>
      <div className="mb-3">
        <label className="form-label">I can teach in return</label>
        <select className="form-select" value={form.offeredSkillId} onChange={(e) => setForm({ ...form, offeredSkillId: e.target.value })}>
          <option value="">Nothing specific</option>
          {mine.map((s) => <option key={s.skillId} value={s.skillId}>{s.skillName}</option>)}
        </select>
        {mine.length === 0 && <div className="form-text">Add skills you can teach on the My Skills page to offer something.</div>}
      </div>
      <div>
        <label className="form-label">Message</label>
        <textarea className="form-control" rows={3} maxLength={500} value={form.message}
          onChange={(e) => setForm({ ...form, message: e.target.value })} />
      </div>
    </Modal>
  );
}
