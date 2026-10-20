const palette = ['#FF7A59', '#6366F1', '#10B981', '#F59E0B', '#EC4899', '#0EA5E9', '#8B5CF6', '#14B8A6'];

export default function Avatar({ name = '?', size = 40 }) {
  const initials = name.split(' ').filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');
  const color = palette[[...name].reduce((a, c) => a + c.charCodeAt(0), 0) % palette.length];
  return (
    <div className="avatar" style={{ width: size, height: size, background: color, fontSize: size * 0.38 }}>
      {initials}
    </div>
  );
}
