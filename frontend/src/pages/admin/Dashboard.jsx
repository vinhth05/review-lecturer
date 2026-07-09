import { useQuery } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Users, GraduationCap, Building2, BookOpen, MessageSquare, AlertTriangle, FileWarning } from 'lucide-react';

export default function Dashboard() {
  const { data: stats, isLoading, error } = useQuery({
    queryKey: ['admin-statistics'],
    queryFn: adminApi.getStatistics,
  });

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold tracking-tight">Dashboard Overview</h1>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(8)].map((_, i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <Skeleton className="h-4 w-[100px]" />
                <Skeleton className="h-4 w-4" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-[60px]" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-destructive/15 text-destructive p-4 rounded-md border border-destructive/50 flex flex-col gap-2">
        <div className="flex items-center gap-2 font-bold">
          <AlertTriangle className="h-4 w-4" />
          <span>Error</span>
        </div>
        <p className="text-sm">
          Failed to load dashboard statistics. Please try again later.
        </p>
      </div>
    );
  }

  const statCards = [
    { title: 'Total Users', value: stats?.totalUsers || 0, icon: Users, color: 'text-blue-500' },
    { title: 'Total Lecturers', value: stats?.totalLecturers || 0, icon: GraduationCap, color: 'text-green-500' },
    { title: 'Total Faculties', value: stats?.totalFaculties || 0, icon: Building2, color: 'text-purple-500' },
    { title: 'Total Subjects', value: stats?.totalSubjects || 0, icon: BookOpen, color: 'text-orange-500' },
    { title: 'Total Reviews', value: stats?.totalReviews || 0, icon: MessageSquare, color: 'text-pink-500' },
    { title: 'Pending Reviews', value: stats?.pendingReviews || 0, icon: MessageSquare, color: 'text-yellow-500' },
    { title: 'Total Reports', value: stats?.totalReports || 0, icon: AlertTriangle, color: 'text-red-500' },
    { title: 'Toxic Keywords', value: stats?.toxicKeywordsCount || 0, icon: FileWarning, color: 'text-red-700' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight">Dashboard Overview</h1>
        <p className="text-muted-foreground">Monitor the system's key metrics and status.</p>
      </div>
      
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat, index) => (
          <Card key={index}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value.toLocaleString()}</div>
            </CardContent>
          </Card>
        ))}
      </div>
      
      {/* Top Lecturers Section */}
      <div className="mt-8">
        <h2 className="text-xl font-bold tracking-tight mb-4">Top Rated Lecturers</h2>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {stats?.topLecturers?.map(lecturer => (
            <Card key={lecturer.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-2">
                <CardTitle className="text-base">{lecturer.fullName}</CardTitle>
                <div className="text-sm text-muted-foreground">{lecturer.facultyName}</div>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-1 font-medium">
                    <span className="text-yellow-500">★</span>
                    {lecturer.averageRating.toFixed(1)}
                  </div>
                  <span className="text-muted-foreground">{lecturer.reviewCount} reviews</span>
                </div>
              </CardContent>
            </Card>
          ))}
          {(!stats?.topLecturers || stats.topLecturers.length === 0) && (
            <div className="col-span-full text-center py-8 text-muted-foreground bg-card border rounded-lg">
              No lecturers have been reviewed yet.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
