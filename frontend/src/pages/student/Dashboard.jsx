import { useAuth } from '@/contexts/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { BookOpen, Users, MessageSquare, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import { reviewApi } from '@/services/api/reviewApi';
import { Skeleton } from '@/components/ui/skeleton';

export default function Dashboard() {
  const { user } = useAuth();

  const { data: myReviews, isLoading: isLoadingReviews } = useQuery({
    queryKey: ['myReviews'],
    queryFn: () => reviewApi.getMyReviews()
  });

  const stats = [
    { title: 'Total Reviews', value: myReviews ? myReviews.length : '...', icon: MessageSquare, color: 'text-blue-500' },
    { title: 'Pending Approval', value: '...', icon: BookOpen, color: 'text-orange-500' },
    { title: 'Favorite Lecturers', value: '...', icon: Star, color: 'text-yellow-500' },
    { title: 'Total Lecturers', value: '...', icon: Users, color: 'text-green-500' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Welcome back, {user?.fullName}!</h2>
        <p className="text-muted-foreground">Here is an overview of your activity.</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <motion.div
            key={stat.title}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  {stat.title}
                </CardTitle>
                <stat.icon className={`h-4 w-4 ${stat.color}`} />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stat.value}</div>
              </CardContent>
            </Card>
          </motion.div>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>My Recent Reviews</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoadingReviews ? (
              <div className="space-y-4">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : myReviews && myReviews.length > 0 ? (
              <div className="space-y-4">
                {myReviews.slice(0, 5).map(review => (
                  <div key={review.id} className="flex flex-col space-y-1 border-b pb-4 last:border-0 last:pb-0">
                    <div className="flex items-center justify-between">
                      <span className="font-medium">{review.lecturerName}</span>
                      <div className="flex items-center text-sm text-yellow-500">
                        <Star className="h-3 w-3 fill-current mr-1" />
                        <span>{review.averageRating.toFixed(1)}</span>
                      </div>
                    </div>
                    <span className="text-xs text-muted-foreground">Semester {review.semester}, {review.academicYear}</span>
                    <p className="text-sm text-foreground line-clamp-2 mt-1">{review.comment}</p>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">
                You haven't written any reviews yet.
              </div>
            )}
          </CardContent>
        </Card>
        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Top Rated Lecturers</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-sm text-muted-foreground">
              Discover highly rated lecturers in your faculty.
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
