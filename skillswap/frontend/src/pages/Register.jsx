import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { errMsg } from '../api/client';
import AuthArt from './AuthArt.jsx';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirm) return setError('Passwords do not match');
    setBusy(true); setError('');
    try {
      await register(form.name, form.email, form.password);
      navigate('/skills?welcome=1');
    } catch (err) { setError(errMsg(err)); } finally { setBusy(false); }
  };

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  return (
    <div className="auth-wrap">
      <AuthArt />
      <div className="auth-form">
        <div>
          <h1 className="h2 mb-1">Create your account</h1>
          <p className="text-muted mb-4">It takes a minute. Then add your skills and get matched.</p>
          {error && <div className="alert alert-danger py-2">{error}</div>}
          <form onSubmit={submit}>
            <div className="mb-3"><label className="form-label">Full name</label>
              <input className="form-control" required maxLength={100} value={form.name} onChange={set('name')} /></div>
            <div className="mb-3"><label className="form-label">Email</label>
              <input type="email" className="form-control" required value={form.email} onChange={set('email')} /></div>
            <div className="row g-3 mb-4">
              <div className="col"><label className="form-label">Password</label>
                <input type="password" className="form-control" required minLength={6} value={form.password} onChange={set('password')} /></div>
              <div className="col"><label className="form-label">Confirm</label>
                <input type="password" className="form-control" required minLength={6} value={form.confirm} onChange={set('confirm')} /></div>
            </div>
            <button className="btn btn-primary w-100 py-2" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
          </form>
          <p className="text-center mt-4">Already a member? <Link to="/login">Log in</Link></p>
        </div>
      </div>
    </div>
  );
}
