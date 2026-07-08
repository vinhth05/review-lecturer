import { ThemeProvider } from '@/contexts/ThemeContext'
import { AuthProvider } from '@/contexts/AuthContext'
import AppRoutes from '@/routes'
import { Toaster } from '@/components/ui/sonner'

function App() {
  return (
    <ThemeProvider defaultTheme="system" storageKey="vite-ui-theme">
      <AuthProvider>
        <AppRoutes />
        <Toaster />
      </AuthProvider>
    </ThemeProvider>
  )
}

export default App
