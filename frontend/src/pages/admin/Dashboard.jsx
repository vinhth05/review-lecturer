import { useQuery } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Users, GraduationCap, Building2, BookOpen, MessageSquare, AlertTriangle, FileWarning, Star, TrendingUp } from 'lucide-react';

export default function Dashboard() {
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['admin-statistics'],
    queryFn: adminApi.getStatistics,
  });

  if (isLoading) {
    return (
      <div className="space-y-8 animate-in fade-in duration-500">
        <div className="flex flex-col gap-2">
          <h1 className="text-4xl font-extrabold tracking-tight text-gradient">Dashboard Overview</h1>
          <Skeleton className="h-5 w-64 rounded-full" />
        </div>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(8)].map((_, i) => (
            <Card key={i} className="glass-card overflow-hidden">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-[100px] rounded-full" />
                <Skeleton className="h-8 w-8 rounded-full" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-10 w-[80px] rounded-md mt-2" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-destructive/10 text-destructive p-6 rounded-xl border border-destructive/20 flex flex-col items-center justify-center gap-3 shadow-sm min-h-[300px]">
        <div className="bg-destructive/20 p-3 rounded-full">
          <AlertTriangle className="h-8 w-8 text-destructive" />
        </div>
        <h2 className="text-xl font-bold">Failed to load statistics</h2>
        <p className="text-sm text-center max-w-md">
          We encountered an error while fetching the dashboard data. Please try again later.
        </p>
      </div>
    );
  }

  const statCards = [
    { title: 'Total Students', value: stats?.totalStudents || 0, icon: Users, gradient: 'from-blue-500/20 to-blue-600/5', color: 'text-blue-500' },
    { title: 'Total Lecturers', value: stats?.totalLecturers || 0, icon: GraduationCap, gradient: 'from-emerald-500/20 to-emerald-600/5', color: 'text-emerald-500' },
    { title: 'Total Faculties', value: stats?.totalFaculties || 0, icon: Building2, gradient: 'from-purple-500/20 to-purple-600/5', color: 'text-purple-500' },
    { title: 'Total Subjects', value: stats?.totalSubjects || 0, icon: BookOpen, gradient: 'from-orange-500/20 to-orange-600/5', color: 'text-orange-500' },
    { title: 'Total Reviews', value: stats?.totalReviews || 0, icon: MessageSquare, gradient: 'from-pink-500/20 to-pink-600/5', color: 'text-pink-500' },
    { title: 'Pending Reviews', value: stats?.pendingReviews || 0, icon: MessageSquare, gradient: 'from-amber-500/20 to-amber-600/5', color: 'text-amber-500' },
    { title: 'Total Reports', value: stats?.totalReports || 0, icon: AlertTriangle, gradient: 'from-red-500/20 to-red-600/5', color: 'text-red-500' },
    { title: 'Toxic Keywords', value: stats?.toxicKeywordsCount || 0, icon: FileWarning, gradient: 'from-rose-500/20 to-rose-600/5', color: 'text-rose-600' },
  ];

  return (
    <div className="space-y-8 animate-in slide-in-from-bottom-4 duration-700 pb-10">
      <div className="flex flex-col gap-2">
        <h1 className="text-4xl font-extrabold tracking-tight text-gradient">Dashboard Overview</h1>
        <p className="text-muted-foreground text-lg">Monitor the system's key metrics and current status.</p>
      </div>
      
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat, index) => (
          <Card key={index} className={`glass-card overflow-hidden group border-t-4 border-t-transparent hover:border-t-primary transition-all duration-300 relative`}>
            <div className={`absolute inset-0 bg-gradient-to-br ${stat.gradient} opacity-0 group-hover:opacity-100 transition-opacity duration-500 -z-10`} />
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-semibold text-muted-foreground group-hover:text-foreground transition-colors">
                {stat.title}
              </CardTitle>
              <div className={`p-2 rounded-lg bg-background/50 backdrop-blur-sm border shadow-sm group-hover:scale-110 transition-transform duration-300`}>
                <stat.icon className={`h-5 w-5 ${stat.color}`} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-3xl font-black mt-2 tracking-tight">
                {stat.value.toLocaleString()}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
      
      {/* Top Lecturers Section */}
      <div className="mt-12 space-y-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <TrendingUp className="h-6 w-6 text-primary" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Top Rated Lecturers</h2>
        </div>
        
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {stats?.topLecturers?.map((lecturer, idx) => (
            <Card key={lecturer.id} className="glass-card hover:-translate-y-1 transition-all duration-300 relative overflow-hidden group">
              <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity pointer-events-none">
                <Star className="w-24 h-24 text-primary" />
              </div>
              <CardHeader className="pb-3">
                <div className="flex justify-between items-start">
                  <div>
                    <CardTitle className="text-lg font-bold line-clamp-1">{lecturer.fullName}</CardTitle>
                    <div className="text-sm text-primary font-medium mt-1">{lecturer.facultyName}</div>
                  </div>
                  <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary/10 text-primary font-bold text-sm">
                    #{idx + 1}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between mt-4">
                  <div className="flex items-center gap-2 bg-amber-500/10 px-3 py-1.5 rounded-full border border-amber-500/20">
                    <Star className="h-4 w-4 fill-amber-500 text-amber-500" />
                    <span className="font-bold text-amber-600 dark:text-amber-400">
                      {lecturer.averageRating.toFixed(1)}
                    </span>
                  </div>
                  <span className="text-sm font-medium text-muted-foreground bg-secondary/50 px-3 py-1.5 rounded-full">
                    {lecturer.reviewCount} reviews
                  </span>
                </div>
              </CardContent>
            </Card>
          ))}
          {(!stats?.topLecturers || stats.topLecturers.length === 0) && (
            <div className="col-span-full flex flex-col items-center justify-center py-16 text-muted-foreground glass-panel rounded-2xl border-dashed">
              <GraduationCap className="h-12 w-12 text-muted-foreground/50 mb-4" />
              <p className="text-lg font-medium">No lecturers have been reviewed yet</p>
              <p className="text-sm opacity-80">Check back later when students start reviewing.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
