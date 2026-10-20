import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { errMsg } from '../api/client';
import AuthArt from './AuthArt.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(params.get('expired') ? 'Your session expired. Please log in again.' : '');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true); setError('');
    try {
      const u = await login(form.email, form.password);
      navigate(u.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (err) { setError(errMsg(err)); } finally { setBusy(false); }
  };

  const fill = (email, password) => setForm({ email, password });

  return (
    <div className="auth-wrap">
      <AuthArt />
      <div className="auth-form">
        <div>
          <h1 className="h2 mb-1">Welcome back</h1>
          <p className="text-muted mb-4">Log in to continue swapping skills.</p>
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <form onSubmit={submit}>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className="form-control" required value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div className="mb-4">
              <label className="form-label">Password</label>
              <input type="password" className="form-control" required value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <button className="btn btn-primary w-100 py-2" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
          </form>
          <p className="text-center mt-4 mb-4">New here? <Link to="/register">Create an account</Link></p>
          <div className="demo-box">
            <div className="fw-bold mb-1">Demo accounts</div>
            <div className="d-flex flex-wrap gap-2">
              <button type="button" className="btn btn-sm btn-light" onClick={() => fill('arshan@demo.com', 'password123')}>Arshan (user)</button>
              <button type="button" className="btn btn-sm btn-light" onClick={() => fill('rashid@demo.com', 'password123')}>Rashid (user)</button>
              <button type="button" className="btn btn-sm btn-light" onClick={() => fill('admin@skillswap.com', 'admin123')}>Admin</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
