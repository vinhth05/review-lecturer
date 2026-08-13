import { useCallback, useMemo, useState, useEffect } from "react"
import { AuthContext } from './auth-context'
import { authApi } from '@/services/api/authApi'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const login = useCallback((userData) => {
    setUser(userData)
    if (userData.token) {
      localStorage.setItem("access_token", userData.token)
    }
    if (userData.refreshToken) {
      localStorage.setItem("refresh_token", userData.refreshToken)
    }
    localStorage.setItem("user", JSON.stringify(userData))
  }, [])

  const updateUser = useCallback((newUserData) => {
    setUser((currentUser) => {
      const updatedUser = { ...currentUser, ...newUserData };
      localStorage.setItem("user", JSON.stringify(updatedUser));
      return updatedUser;
    });
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem("refresh_token")
    setUser(null)
    localStorage.removeItem("access_token")
    localStorage.removeItem("refresh_token")
    localStorage.removeItem("user")
    if (refreshToken) {
      try {
        await authApi.logout({ refreshToken })
      } catch {
        // Local logout must still succeed if the session is already invalid.
      }
    }
  }, [])

  useEffect(() => {
    // Check local storage for existing session on initial load
    const token = localStorage.getItem("access_token")
    const storedUser = localStorage.getItem("user")

    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser))
      } catch {
        localStorage.removeItem("access_token")
        localStorage.removeItem("refresh_token")
        localStorage.removeItem("user")
      }
    }
    setLoading(false)

    const handleUnauthorized = () => {
      logout();
    };
    window.addEventListener('unauthorized', handleUnauthorized);
    return () => window.removeEventListener('unauthorized', handleUnauthorized);
  }, [logout])

  const value = useMemo(() => ({
    user,
    loading,
    login,
    logout,
    updateUser,
    isAuthenticated: !!user,
    isStudent: user?.role === 'STUDENT',
    isAdmin: user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'
  }), [loading, login, logout, updateUser, user])

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  )
}
