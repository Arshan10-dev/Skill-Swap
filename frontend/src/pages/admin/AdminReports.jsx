import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errMsg } from '../../api/client';
import { useToast } from '../../context/ToastContext.jsx';
import Avatar from '../../components/Avatar.jsx';
import { Empty, Loading, Modal, PageHead, StatusPill, timeAgo } from '../../components/ui.jsx';

export default function AdminReports() {
  const toast = useToast();
  const [status, setStatus] = useState('OPEN');
  const [list, setList] = useState(null);
  const [resolve, setResolve] = useState(null);

  const load = () => api.get(`/admin/reports${status ? `?status=${status}` : ''}`).then((r) => setList(r.data));
  useEffect(() => { setList(null); load(); }, [status]); // eslint-disable-line react-hooks/exhaustive-deps

  const submit = async () => {
    try {
      await api.post(`/admin/reports/${resolve.report.id}/resolve`, { status: resolve.status, adminNote: resolve.adminNote, suspendUser: resolve.suspend });
      toast('Report updated');
      setResolve(null);
      load();
    } catch (e) { toast(errMsg(e), 'danger'); }
  };

  return (
    <>
      <PageHead title="Reports & moderation" sub="Review reports from members and take action.">
        <div className="ss-tabs">
          {[['OPEN', 'Open'], ['RESOLVED', 'Resolved'], ['DISMISSED', 'Dismissed'], ['', 'All']].map(([v, l]) => (
            <button key={l} className={status === v ? 'active' : ''} onClick={() => setStatus(v)}>{l}</button>
          ))}
        </div>
      </PageHead>
      {!list ? <Loading /> : (
        <div className="card">
          {list.length === 0 ? <Empty icon="shield-check" title="Nothing here">No reports in this view.</Empty> : list.map((r) => (
            <div className="list-row flex-wrap align-items-start" key={r.id}>
              <Avatar name={r.reported.name} size={44} />
              <div className="flex-grow-1" style={{ minWidth: 240 }}>
                <div className="d-flex gap-2 align-items-center flex-wrap">
                  <Link to={`/users/${r.reported.id}`} className="fw-bold">{r.reported.name}</Link>
                  <StatusPill status={r.status} />
                  {r.reportsAgainstUser > 1 && <span className="pill pill-danger">{r.reportsAgainstUser} reports total</span>}
                </div>
                <div className="fw-semibold mt-1">{r.reason}</div>
                {r.details && <div className="small">{r.details}</div>}
                <div className="small text-muted mt-1">Reported by {r.reporter.name} · {timeAgo(r.createdAt)}</div>
                {r.adminNote && <div className="small mt-1"><i className="bi bi-shield me-1" />{r.adminNote}</div>}
              </div>
              {r.status === 'OPEN' &&
                <button className="btn btn-primary btn-sm" onClick={() => setResolve({ report: r, status: 'RESOLVED', adminNote: '', suspend: false })}>Take action</button>}
            </div>
          ))}
        </div>
      )}
      <Modal show={!!resolve} title="Resolve report" onClose={() => setResolve(null)}
        footer={<><button className="btn btn-outline-secondary" onClick={() => setResolve(null)}>Cancel</button>
          <button className="btn btn-primary" onClick={submit}>Save decision</button></>}>
        {resolve && <>
          <div className="mb-3"><label className="form-label">Outcome</label>
            <select className="form-select" value={resolve.status} onChange={(e) => setResolve({ ...resolve, status: e.target.value })}>
              <option value="RESOLVED">Resolved: action taken</option><option value="DISMISSED">Dismissed: no violation</option>
            </select></div>
          <div className="mb-3"><label className="form-label">Note</label>
            <textarea className="form-control" rows={2} maxLength={500} value={resolve.adminNote} onChange={(e) => setResolve({ ...resolve, adminNote: e.target.value })} /></div>
          <div className="form-check">
            <input className="form-check-input" type="checkbox" id="susp" checked={resolve.suspend} onChange={(e) => setResolve({ ...resolve, suspend: e.target.checked })} />
            <label className="form-check-label" htmlFor="susp">Suspend {resolve.report.reported.name}'s account</label>
          </div>
        </>}
      </Modal>
    </>
  );
}
