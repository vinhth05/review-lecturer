import { useAuth } from '@/contexts/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { BookOpen, Users, MessageSquare, Star, ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';
import { reviewApi } from '@/services/api/reviewApi';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';

export default function Dashboard() {
  const { user } = useAuth();

  const { data: myReviews, isLoading: isLoadingReviews } = useQuery({
    queryKey: ['myReviews'],
    queryFn: () => reviewApi.getMyReviews()
  });

  const stats = [
    { title: 'Total Reviews', value: myReviews ? myReviews.length : '...', icon: MessageSquare, color: 'text-blue-500', bg: 'bg-blue-500/10' },
    { title: 'Pending Approval', value: '...', icon: BookOpen, color: 'text-orange-500', bg: 'bg-orange-500/10' },
    { title: 'Favorite Lecturers', value: '...', icon: Star, color: 'text-yellow-500', bg: 'bg-yellow-500/10' },
    { title: 'Total Lecturers', value: '...', icon: Users, color: 'text-green-500', bg: 'bg-green-500/10' },
  ];

  return (
    <div className="space-y-8 pb-8">
      {/* Header Section */}
      <div className="flex flex-col gap-2">
        <motion.h2 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          className="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-primary to-purple-600 bg-clip-text text-transparent"
        >
          Welcome back, {user?.fullName}! 👋
        </motion.h2>
        <motion.p 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
          className="text-muted-foreground text-lg"
        >
          Here is an overview of your activity and latest updates.
        </motion.p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <motion.div
            key={stat.title}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <Card className="hover:-translate-y-1 hover:shadow-lg transition-all duration-300 border-border/50 overflow-hidden relative group">
              <div className={`absolute inset-0 opacity-0 group-hover:opacity-10 transition-opacity duration-300 ${stat.bg}`} />
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  {stat.title}
                </CardTitle>
                <div className={`p-2 rounded-full ${stat.bg}`}>
                  <stat.icon className={`h-4 w-4 ${stat.color}`} />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-3xl font-bold">{stat.value}</div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>

      {/* Main Content Area */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4 border-border/50 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-xl">My Recent Reviews</CardTitle>
              <p className="text-sm text-muted-foreground mt-1">Your latest feedback for lecturers.</p>
            </div>
            <Button variant="ghost" size="sm" className="hidden sm:flex hover:bg-primary/10 hover:text-primary">
              View All <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </CardHeader>
          <CardContent>
            {isLoadingReviews ? (
              <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="flex flex-col gap-2 p-4 border rounded-lg">
                    <div className="flex justify-between"><Skeleton className="h-5 w-1/3" /><Skeleton className="h-5 w-12" /></div>
                    <Skeleton className="h-4 w-1/4" />
                    <Skeleton className="h-10 w-full mt-2" />
                  </div>
                ))}
              </div>
            ) : myReviews && myReviews.length > 0 ? (
              <div className="space-y-4">
                {myReviews.slice(0, 5).map((review, i) => (
                  <motion.div 
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: i * 0.1 }}
                    key={review.id} 
                    className="flex flex-col space-y-2 p-4 border rounded-xl hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-base">{review.lecturerName}</span>
                      <div className="flex items-center text-sm font-medium bg-yellow-500/10 text-yellow-600 dark:text-yellow-500 px-2 py-1 rounded-md">
                        <Star className="h-3.5 w-3.5 fill-current mr-1" />
                        <span>{review.averageRating.toFixed(1)}</span>
                      </div>
                    </div>
                    <span className="text-xs font-medium text-primary/80 bg-primary/10 w-fit px-2 py-0.5 rounded-full">
                      Semester {review.semester}, {review.academicYear}
                    </span>
                    <p className="text-sm text-foreground/90 line-clamp-2 mt-2 leading-relaxed">"{review.comment}"</p>
                  </motion.div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <div className="h-20 w-20 bg-muted rounded-full flex items-center justify-center mb-4">
                  <MessageSquare className="h-8 w-8 text-muted-foreground/50" />
                </div>
                <h3 className="text-lg font-medium">No reviews yet</h3>
                <p className="text-sm text-muted-foreground mt-1 max-w-sm">
                  You haven't written any reviews. Start by finding a lecturer you've studied with.
                </p>
                <Button className="mt-4" variant="outline">Find Lecturers</Button>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="col-span-3 border-border/50 shadow-sm relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-primary/5 rounded-full blur-3xl -mr-10 -mt-10 pointer-events-none" />
          <CardHeader>
            <CardTitle className="text-xl flex items-center gap-2">
              <Star className="h-5 w-5 text-yellow-500 fill-yellow-500" />
              Top Rated Lecturers
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">Highly recommended in your faculty.</p>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-12 text-center h-full">
              <Users className="h-12 w-12 text-muted-foreground/30 mb-4" />
              <p className="text-sm text-muted-foreground">
                Discover highly rated lecturers<br/>Feature coming soon...
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

