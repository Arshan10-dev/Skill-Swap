import { createContext, useContext, useState, useCallback } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

function readUser() {
  try { return JSON.parse(localStorage.getItem('ss_user')); } catch { return null; }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);

  const save = (data) => {
    localStorage.setItem('ss_token', data.token);
    localStorage.setItem('ss_user', JSON.stringify(data.user));
    setUser(data.user);
    return data.user;
  };

  const login = async (email, password) => save((await api.post('/auth/login', { email, password })).data);
  const register = async (name, email, password) => save((await api.post('/auth/register', { name, email, password })).data);

  const logout = useCallback(() => {
    localStorage.removeItem('ss_token');
    localStorage.removeItem('ss_user');
    setUser(null);
  }, []);

  const updateUser = (patch) => {
    const next = { ...user, ...patch };
    localStorage.setItem('ss_user', JSON.stringify(next));
    setUser(next);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, updateUser, isAdmin: user?.role === 'ADMIN' }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
