import { useEffect, useState } from 'react';
import api, { errMsg } from '../../api/client';
import { useToast } from '../../context/ToastContext.jsx';
import Avatar from '../../components/Avatar.jsx';
import { Loading, PageHead, Stars } from '../../components/ui.jsx';

export default function AdminUsers() {
  const toast = useToast();
  const [q, setQ] = useState('');
  const [users, setUsers] = useState(null);

  const load = () => api.get(`/admin/users?q=${encodeURIComponent(q)}`).then((r) => setUsers(r.data));
  useEffect(() => { const t = setTimeout(load, 250); return () => clearTimeout(t); }, [q]); // eslint-disable-line react-hooks/exhaustive-deps

  const toggle = async (u) => {
    try {
      await api.post(`/admin/users/${u.id}/${u.active ? 'suspend' : 'activate'}`);
      toast(`${u.name} ${u.active ? 'suspended' : 'reactivated'}`);
      load();
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  return (
    <>
      <PageHead title="Users" sub="Search, suspend or reactivate member accounts.">
        <input className="form-control" style={{ width: 260 }} placeholder="Search name or email" value={q} onChange={(e) => setQ(e.target.value)} />
      </PageHead>
      {!users ? <Loading /> : (
        <div className="card">
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead><tr><th>User</th><th>Role</th><th>Location</th><th>Reputation</th><th>Rating</th><th>Status</th><th /></tr></thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td><div className="d-flex gap-2 align-items-center"><Avatar name={u.name} size={34} />
                      <div><div className="fw-semibold">{u.name}</div><div className="small text-muted">{u.email}</div></div></div></td>
                    <td><span className={`pill ${u.role === 'ADMIN' ? 'pill-primary' : 'pill-secondary'}`}>{u.role}</span></td>
                    <td className="small">{u.location || '–'}</td>
                    <td className="fw-semibold">{u.reputation}</td>
                    <td><Stars value={u.averageRating} size=".8rem" /></td>
                    <td><span className={`pill ${u.active ? 'pill-success' : 'pill-danger'}`}>{u.active ? 'Active' : 'Suspended'}</span></td>
                    <td className="text-end">{u.role !== 'ADMIN' &&
                      <button className={`btn btn-sm ${u.active ? 'btn-outline-danger' : 'btn-outline-success'}`} onClick={() => toggle(u)}>
                        {u.active ? 'Suspend' : 'Reactivate'}</button>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  );
}
