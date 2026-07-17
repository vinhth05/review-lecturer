import { useState } from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useTheme } from '@/contexts/ThemeContext';
import { Button } from '@/components/ui/button';
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  Building2,
  BookOpen,
  MessageSquare,
  AlertTriangle,
  FileWarning,
  LogOut,
  Menu,
  Moon,
  Sun,
} from 'lucide-react';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';

const navigation = [
  { name: 'Dashboard', href: '/admin', icon: LayoutDashboard },
  { name: 'Users', href: '/admin/users', icon: Users },
  { name: 'Lecturers', href: '/admin/lecturers', icon: GraduationCap },
  { name: 'Faculties', href: '/admin/faculties', icon: Building2 },
  { name: 'Subjects', href: '/admin/subjects', icon: BookOpen },
  { name: 'Reviews', href: '/admin/reviews', icon: MessageSquare },
  { name: 'Reports', href: '/admin/reports', icon: AlertTriangle },
  { name: 'Toxic Keywords', href: '/admin/toxic-keywords', icon: FileWarning },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const NavLinks = () => (
    <div className="space-y-2">
      {navigation.map((item) => {
        const isActive = location.pathname === item.href || (item.href !== '/admin' && location.pathname.startsWith(item.href));
        return (
          <Button
            key={item.name}
            variant={isActive ? "secondary" : "ghost"}
            className={`w-full justify-start transition-all duration-300 relative overflow-hidden group ${isActive ? 'bg-primary/10 text-primary hover:bg-primary/20 font-medium' : 'hover:bg-background/50 hover:translate-x-1'}`}
            asChild
            onClick={() => setMobileMenuOpen(false)}
          >
            <Link to={item.href}>
              {isActive && (
                <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary rounded-r-full" />
              )}
              <item.icon className={`mr-3 h-5 w-5 ${isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-foreground transition-colors'}`} />
              {item.name}
            </Link>
          </Button>
        )
      })}
    </div>
  );

  return (
    <div className="min-h-screen bg-background text-foreground flex">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex w-72 flex-col border-r border-border/50 bg-background/40 backdrop-blur-xl shadow-lg relative z-20">
        <div className="h-20 flex items-center px-8 border-b border-border/50">
          <Link to="/admin" className="font-bold text-2xl flex items-center gap-3 hover:opacity-80 transition-opacity">
            <div className="bg-gradient-to-br from-primary to-purple-600 text-white p-2 rounded-xl shadow-md">
              <span className="font-black">CTU</span>
            </div>
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-foreground to-foreground/70">Admin</span>
          </Link>
        </div>
        <div className="flex-1 py-8 px-5 overflow-y-auto overflow-x-hidden custom-scrollbar">
          <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-4 px-3">Menu</div>
          <NavLinks />
        </div>
        <div className="p-5 border-t border-border/50">
          <Button variant="ghost" className="w-full justify-start text-destructive hover:text-destructive hover:bg-destructive/10 transition-colors rounded-xl" onClick={logout}>
            <LogOut className="mr-3 h-5 w-5" />
            Sign Out
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 bg-muted/10 relative">
        <div className="absolute top-0 left-0 w-full h-64 bg-gradient-to-b from-primary/5 to-transparent pointer-events-none -z-10" />
        
        <header className="h-20 flex items-center justify-between px-6 md:px-10 border-b border-border/50 bg-background/60 backdrop-blur-md sticky top-0 z-10 shadow-sm">
          <div className="flex items-center md:hidden">
            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="mr-2">
                  <Menu className="h-6 w-6" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-72 p-0 glass-panel border-r-0">
                <div className="h-20 flex items-center px-8 border-b border-border/50">
                  <div className="bg-gradient-to-br from-primary to-purple-600 text-white p-2 rounded-xl shadow-md mr-3">
                    <span className="font-black text-sm">CTU</span>
                  </div>
                  <span className="font-bold text-xl">Admin</span>
                </div>
                <div className="py-8 px-5">
                  <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-4 px-3">Menu</div>
                  <NavLinks />
                </div>
              </SheetContent>
            </Sheet>
          </div>
          
          <div className="flex-1" />

          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={toggleTheme} className="rounded-full bg-background/50 border shadow-sm hover:bg-background/80">
              {theme === 'dark' ? <Sun className="h-5 w-5 text-amber-500" /> : <Moon className="h-5 w-5 text-indigo-500" />}
            </Button>
            
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" className="flex items-center gap-3 rounded-full pl-2 pr-4 h-10 border-border/50 bg-background/50 hover:bg-background/80 shadow-sm transition-all">
                  <div className="h-7 w-7 rounded-full bg-gradient-to-br from-primary to-purple-600 flex items-center justify-center text-xs font-bold text-white shadow-inner">
                    {user?.fullName?.charAt(0) || 'A'}
                  </div>
                  <span className="hidden md:inline font-medium">{user?.fullName || 'Admin'}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56 glass-panel">
                <div className="flex items-center justify-start gap-2 p-2">
                  <div className="flex flex-col space-y-1 leading-none">
                    <p className="font-medium">{user?.fullName}</p>
                    <p className="w-[200px] truncate text-sm text-muted-foreground">
                      {user?.email}
                    </p>
                  </div>
                </div>
                <DropdownMenuItem onClick={logout} className="text-destructive focus:text-destructive focus:bg-destructive/10 cursor-pointer mt-2">
                  <LogOut className="mr-2 h-4 w-4" /> Sign out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>
        
        <div className="flex-1 p-4 md:p-8 overflow-y-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
