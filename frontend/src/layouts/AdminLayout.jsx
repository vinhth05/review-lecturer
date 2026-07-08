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
    <div className="space-y-1">
      {navigation.map((item) => {
        const isActive = location.pathname === item.href || (item.href !== '/admin' && location.pathname.startsWith(item.href));
        return (
          <Button
            key={item.name}
            variant={isActive ? "secondary" : "ghost"}
            className={`w-full justify-start ${isActive ? 'bg-secondary' : ''}`}
            asChild
            onClick={() => setMobileMenuOpen(false)}
          >
            <Link to={item.href}>
              <item.icon className="mr-2 h-4 w-4" />
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
      <aside className="hidden md:flex w-64 flex-col border-r bg-card">
        <div className="h-16 flex items-center px-6 border-b">
          <Link to="/admin" className="font-bold text-xl flex items-center gap-2">
            <span className="bg-primary text-primary-foreground p-1 rounded">CTU</span>
            Admin Portal
          </Link>
        </div>
        <div className="flex-1 py-6 px-4 overflow-y-auto overflow-x-hidden">
          <NavLinks />
        </div>
        <div className="p-4 border-t">
          <Button variant="ghost" className="w-full justify-start text-destructive hover:text-destructive hover:bg-destructive/10" onClick={logout}>
            <LogOut className="mr-2 h-4 w-4" />
            Sign Out
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0">
        <header className="h-16 flex items-center justify-between px-4 md:px-6 border-b bg-card">
          <div className="flex items-center md:hidden">
            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-64 p-0">
                <div className="h-16 flex items-center px-6 border-b">
                  <span className="font-bold text-xl">Admin Portal</span>
                </div>
                <div className="py-6 px-4">
                  <NavLinks />
                </div>
              </SheetContent>
            </Sheet>
          </div>
          
          <div className="flex-1" />

          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={toggleTheme}>
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" className="flex items-center gap-2">
                  <div className="h-6 w-6 rounded-full bg-primary/20 flex items-center justify-center text-xs font-bold">
                    {user?.fullName?.charAt(0)}
                  </div>
                  <span className="hidden md:inline">{user?.fullName}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={logout} className="text-destructive">
                  <LogOut className="mr-2 h-4 w-4" /> Sign out
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>
        
        <div className="flex-1 p-4 md:p-6 bg-muted/30 overflow-y-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
