import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../api/client';
import { Loading, PageHead } from '../../components/ui.jsx';

function Bars({ rows, color, enumNames }) {
  const max = Math.max(1, ...rows.map((r) => r.count));
  if (rows.length === 0) return <div className="text-muted small">No data yet</div>;
  return rows.map((r) => (
    <div className="hbar" key={r.name}>
      <div className="name text-truncate">{enumNames ? r.name.charAt(0) + r.name.slice(1).toLowerCase() : r.name}</div>
      <div className="track"><div className="fill" style={{ width: `${(r.count / max) * 100}%`, background: color }} /></div>
      <div className="n">{r.count}</div>
    </div>
  ));
}

export default function AdminDashboard() {
  const [a, setA] = useState(null);
  useEffect(() => { api.get('/admin/analytics').then((r) => setA(r.data)); }, []);
  if (!a) return <Loading />;

  const stats = [
    ['people', 'ic-indigo', a.totalUsers, 'Total users', `${a.newUsersLast7Days} new this week`],
    ['person-check', 'ic-mint', a.activeUsers, 'Active users', `${a.suspendedUsers} suspended`],
    ['tags', 'ic-amber', a.totalSkills, 'Skills listed', 'across all categories'],
    ['arrow-left-right', 'ic-coral', a.totalRequests, 'Swap requests', `${a.pendingRequests} pending`],
    ['check2-circle', 'ic-mint', a.completedSessions, 'Sessions completed', `${a.scheduledSessions} scheduled`],
    ['star', 'ic-amber', a.averageRating ?? '–', 'Average rating', 'platform-wide'],
    ['flag', 'ic-coral', a.openReports, 'Open reports', 'needs review'],
    ['handshake', 'ic-indigo', a.acceptedRequests, 'Active exchanges', 'accepted requests'],
  ];

  return (
    <>
      <PageHead title="Platform analytics" sub="Users, exchanges and activity across SkillSwap.">
        {a.openReports > 0 && <Link to="/admin/reports" className="btn btn-coral"><i className="bi bi-flag me-1" />Review {a.openReports} report{a.openReports > 1 ? 's' : ''}</Link>}
      </PageHead>
      <div className="row g-3 mb-4">
        {stats.map(([icon, cls, v, l, sub]) => (
          <div className="col-6 col-xl-3" key={l}>
            <div className="card stat">
              <div className={`stat-icon ${cls}`}><i className={`bi bi-${icon === 'handshake' ? 'people-fill' : icon}`} /></div>
              <div><div className="stat-value">{v}</div><div className="stat-label">{l}</div><div className="small text-muted" style={{ fontSize: '.72rem' }}>{sub}</div></div>
            </div>
          </div>
        ))}
      </div>
      <div className="row g-4">
        <div className="col-lg-4"><div className="card card-pad h-100"><h6 className="mb-3">Most taught skills</h6><Bars rows={a.topTaughtSkills} color="#10B981" /></div></div>
        <div className="col-lg-4"><div className="card card-pad h-100"><h6 className="mb-3">Most wanted skills</h6><Bars rows={a.topWantedSkills} color="#FF7A59" /></div></div>
        <div className="col-lg-4"><div className="card card-pad h-100"><h6 className="mb-3">Requests by status</h6><Bars rows={a.requestsByStatus} color="#6366F1" enumNames /></div></div>
      </div>
    </>
  );
}
