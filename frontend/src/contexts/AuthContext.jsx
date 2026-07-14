import { createContext, useContext, useMemo, useState, useEffect } from "react"

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check local storage for existing session on initial load
    const token = localStorage.getItem("access_token")
    const storedUser = localStorage.getItem("user")
    
    if (token && storedUser) {
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)

    const handleUnauthorized = () => {
      logout();
    };
    window.addEventListener('unauthorized', handleUnauthorized);
    return () => window.removeEventListener('unauthorized', handleUnauthorized);
  }, [])

  const login = (userData) => {
    setUser(userData)
    if (userData.token) {
      localStorage.setItem("access_token", userData.token)
    }
    if (userData.refreshToken) {
      localStorage.setItem("refresh_token", userData.refreshToken)
    }
    localStorage.setItem("user", JSON.stringify(userData))
  }

  const updateUser = (newUserData) => {
    const updatedUser = { ...user, ...newUserData };
    setUser(updatedUser);
    localStorage.setItem("user", JSON.stringify(updatedUser));
  }

  const logout = () => {
    setUser(null)
    localStorage.removeItem("access_token")
    localStorage.removeItem("refresh_token")
    localStorage.removeItem("user")
  }

  const value = useMemo(() => ({
    user,
    loading,
    login,
    logout,
    updateUser,
    isAuthenticated: !!user,
    isStudent: user?.role === 'STUDENT',
    isAdmin: user?.role === 'ADMIN' || user?.role === 'SUPER_ADMIN'
  }), [loading, user])

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
