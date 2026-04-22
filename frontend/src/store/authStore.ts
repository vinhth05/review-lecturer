import { create } from 'zustand';

export type Role = 'STUDENT' | 'ADMIN' | 'SUPER_ADMIN';

export interface AuthUser {
  token: string;
  role: Role;
  verified: boolean;
  fullName: string;
  facultyName: string;
}

interface AuthState {
  user: AuthUser | null;
  hydrate: () => void;
  setUser: (user: AuthUser | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  hydrate: () => {
    const raw = localStorage.getItem('ctu_user');
    if (raw) {
      set({ user: JSON.parse(raw) as AuthUser });
    }
  },
  setUser: (user) => {
    if (user) {
      localStorage.setItem('ctu_user', JSON.stringify(user));
      localStorage.setItem('ctu_token', user.token);
    } else {
      localStorage.removeItem('ctu_user');
      localStorage.removeItem('ctu_token');
    }
    set({ user });
  },
  logout: () => {
    localStorage.removeItem('ctu_user');
    localStorage.removeItem('ctu_token');
    set({ user: null });
  }
}));
