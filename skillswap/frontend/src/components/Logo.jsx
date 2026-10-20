export default function Logo({ small, light }) {
  return (
    <div className={`logo ${small ? 'logo-sm' : ''} ${light ? 'logo-light' : ''}`}>
      <img src="/logo.svg" alt="" />
      <span>Skill<b>Swap</b></span>
    </div>
  );
}
