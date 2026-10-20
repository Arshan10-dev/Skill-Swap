import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import Layout from './components/Layout.jsx';
import Landing from './pages/Landing.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Profile from './pages/Profile.jsx';
import UserProfile from './pages/UserProfile.jsx';
import MySkills from './pages/MySkills.jsx';
import Matches from './pages/Matches.jsx';
import Search from './pages/Search.jsx';
import Requests from './pages/Requests.jsx';
import Chat from './pages/Chat.jsx';
import Sessions from './pages/Sessions.jsx';
import Notifications from './pages/Notifications.jsx';
import Settings from './pages/Settings.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminUsers from './pages/admin/AdminUsers.jsx';
import AdminSkills from './pages/admin/AdminSkills.jsx';
import AdminReports from './pages/admin/AdminReports.jsx';

function Private({ children, admin }) {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (admin && !isAdmin) return <Navigate to="/dashboard" replace />;
  return <Layout>{children}</Layout>;
}

export default function App() {
  const { user, isAdmin } = useAuth();
  const home = isAdmin ? '/admin' : '/dashboard';
  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to={home} replace /> : <Landing />} />
      <Route path="/login" element={user ? <Navigate to={home} replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to={home} replace /> : <Register />} />

      <Route path="/dashboard" element={<Private><Dashboard /></Private>} />
      <Route path="/profile" element={<Private><Profile /></Private>} />
      <Route path="/users/:id" element={<Private><UserProfile /></Private>} />
      <Route path="/skills" element={<Private><MySkills /></Private>} />
      <Route path="/matches" element={<Private><Matches /></Private>} />
      <Route path="/search" element={<Private><Search /></Private>} />
      <Route path="/requests" element={<Private><Requests /></Private>} />
      <Route path="/chat" element={<Private><Chat /></Private>} />
      <Route path="/chat/:requestId" element={<Private><Chat /></Private>} />
      <Route path="/sessions" element={<Private><Sessions /></Private>} />
      <Route path="/notifications" element={<Private><Notifications /></Private>} />
      <Route path="/settings" element={<Private><Settings /></Private>} />

      <Route path="/admin" element={<Private admin><AdminDashboard /></Private>} />
      <Route path="/admin/users" element={<Private admin><AdminUsers /></Private>} />
      <Route path="/admin/skills" element={<Private admin><AdminSkills /></Private>} />
      <Route path="/admin/reports" element={<Private admin><AdminReports /></Private>} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
