import { useAuthStore } from '@/store/authStore';

export function useAuth() {
  return useAuthStore((state) => ({
    user: state.user,
    setUser: state.setUser,
    logout: state.logout
  }));
}
