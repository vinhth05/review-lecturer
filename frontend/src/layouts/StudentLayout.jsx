import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useTheme } from '@/contexts/ThemeContext';
import { Button } from '@/components/ui/button';
import { LayoutDashboard, Users, MessageSquare, Bookmark, Bell, Settings, User, LogOut, Moon, Sun, Menu } from 'lucide-react';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Sheet, SheetContent, SheetTrigger, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { motion, AnimatePresence } from 'framer-motion';
import { useState } from 'react';

export default function StudentLayout() {
  const { logout, user } = useAuth();
  const { theme, setTheme } = useTheme();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navigation = [
    { name: 'Dashboard', href: '/student', icon: LayoutDashboard },
    { name: 'Lecturers', href: '/student/lecturers', icon: Users },
    { name: 'My Reviews', href: '/student/reviews', icon: MessageSquare },
    { name: 'Favorites', href: '/student/favorites', icon: Bookmark },
  ];

  const NavigationMenu = ({ onClick }) => (
    <nav className="space-y-1">
      {navigation.map((item) => {
        const isActive = location.pathname === item.href || location.pathname.startsWith(`${item.href}/`);
        return (
          <Link
            key={item.name}
            to={item.href}
            onClick={onClick}
            className={`flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-200 group ${
              isActive
                ? 'bg-primary text-primary-foreground shadow-md shadow-primary/20'
                : 'text-muted-foreground hover:bg-primary/10 hover:text-primary'
            }`}
          >
            <item.icon className={`mr-3 h-5 w-5 transition-transform group-hover:scale-110 ${isActive ? 'text-primary-foreground' : 'text-muted-foreground group-hover:text-primary'}`} />
            {item.name}
          </Link>
        );
      })}
    </nav>
  );

  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar for Desktop */}
      <aside className="w-64 border-r border-border bg-card hidden md:flex flex-col z-20">
        <div className="flex h-16 items-center px-6 border-b border-border/50">
          <Link to="/" className="text-xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-primary to-primary/60 tracking-tight">
            Lecturer Review
          </Link>
        </div>
        <div className="flex-1 p-4 overflow-y-auto">
          <NavigationMenu />
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden relative">
        {/* Header */}
        <header className="flex h-16 items-center justify-between px-4 md:px-8 border-b border-border/50 bg-background/80 backdrop-blur-md sticky top-0 z-10 transition-colors">
          <div className="flex items-center gap-4">
            {/* Mobile Menu Trigger */}
            <Sheet open={isMobileMenuOpen} onOpenChange={setIsMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-72 p-0 border-r-0">
                <SheetHeader className="h-16 px-6 flex items-start justify-center border-b border-border/50">
                  <SheetTitle className="text-xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-primary to-primary/60">
                    Lecturer Review
                  </SheetTitle>
                </SheetHeader>
                <div className="p-4">
                  <NavigationMenu onClick={() => setIsMobileMenuOpen(false)} />
                </div>
              </SheetContent>
            </Sheet>
            
            <h1 className="text-xl font-semibold tracking-tight capitalize hidden sm:block">
              {location.pathname === '/student' ? 'Dashboard' : location.pathname.split('/').pop().replace(/-/g, ' ')}
            </h1>
          </div>
          
          <div className="flex items-center gap-2 sm:gap-4">
            <Button
              variant="ghost"
              size="icon"
              className="rounded-full hover:bg-primary/10 transition-colors"
              onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            >
              <AnimatePresence mode="wait">
                <motion.div
                  key={theme}
                  initial={{ opacity: 0, rotate: -90 }}
                  animate={{ opacity: 1, rotate: 0 }}
                  exit={{ opacity: 0, rotate: 90 }}
                  transition={{ duration: 0.2 }}
                >
                  {theme === 'dark' ? <Sun className="h-5 w-5 text-yellow-500" /> : <Moon className="h-5 w-5 text-indigo-500" />}
                </motion.div>
              </AnimatePresence>
            </Button>
            
            <Button variant="ghost" size="icon" className="rounded-full relative hover:bg-primary/10 transition-colors">
              <Bell className="h-5 w-5" />
              <span className="absolute top-1 right-1 h-2 w-2 rounded-full bg-destructive border-2 border-background"></span>
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-9 w-9 rounded-full ml-2 ring-2 ring-primary/20 hover:ring-primary transition-all">
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-gradient-to-br from-primary/20 to-primary/10 text-primary font-bold shadow-inner">
                    {user?.fullName?.charAt(0) || 'U'}
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56 mt-2 rounded-xl shadow-lg" align="end" forceMount>
                <DropdownMenuItem asChild className="cursor-pointer rounded-lg mb-1">
                  <Link to="/student/profile" className="flex items-center">
                    <User className="mr-2 h-4 w-4" />
                    <span>Profile</span>
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem asChild className="cursor-pointer rounded-lg mb-1">
                  <Link to="/student/settings" className="flex items-center">
                    <Settings className="mr-2 h-4 w-4" />
                    <span>Settings</span>
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={logout} className="cursor-pointer text-destructive focus:bg-destructive/10 focus:text-destructive rounded-lg">
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Log out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        {/* Scrollable Area */}
        <div className="flex-1 overflow-auto bg-secondary/20">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.2 }}
              className="h-full p-4 md:p-8"
            >
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </div>
      </main>
    </div>
  );
}
