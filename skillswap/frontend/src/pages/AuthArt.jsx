import Logo from '../components/Logo.jsx';

export default function AuthArt() {
  return (
    <div className="auth-art">
      <div className="orbit" style={{ width: 520, height: 520, right: -180, top: -120 }} />
      <div className="orbit" style={{ width: 340, height: 340, right: -90, top: -30 }} />
      <Logo />
      <div style={{ position: 'relative' }}>
        <h2>Every person you meet<br />knows something you don't.</h2>
        <p className="mt-3" style={{ maxWidth: 420 }}>Swap skills one-on-one with learners and mentors near you or online.</p>
      </div>
      <div className="small opacity-75">React · Spring Boot · MySQL</div>
    </div>
  );
}
