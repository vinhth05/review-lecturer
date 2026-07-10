import { useQuery } from '@tanstack/react-query';
import { reviewApi } from '@/services/api/reviewApi';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Star, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function MyReviews() {
  const { data: myReviews, isLoading } = useQuery({
    queryKey: ['myReviews'],
    queryFn: () => reviewApi.getMyReviews()
  });

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">My Reviews</h2>
        <p className="text-muted-foreground">History of your lecturer reviews.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Reviews</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-6">
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-24 w-full" />
              <Skeleton className="h-24 w-full" />
            </div>
          ) : myReviews && myReviews.length > 0 ? (
            <div className="space-y-8">
              {myReviews.map((review) => (
                <div key={review.id} className="flex flex-col space-y-3 border-b pb-6 last:border-0 last:pb-0">
                  <div className="flex items-start justify-between">
                    <div>
                      <Link 
                        to={`/student/lecturers/${review.lecturerId}`}
                        className="font-semibold text-lg hover:underline flex items-center gap-2"
                      >
                        {review.lecturerName}
                        <ExternalLink className="h-4 w-4 text-muted-foreground" />
                      </Link>
                      <div className="flex items-center text-sm text-muted-foreground mt-1 gap-2">
                        <span>Semester {review.semester}, {review.academicYear}</span>
                        <span>•</span>
                        <span>{new Date(review.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                    <div className="flex items-center text-yellow-500 bg-yellow-500/10 px-3 py-1 rounded-full font-bold">
                      <Star className="h-4 w-4 fill-current mr-1" />
                      <span>{review.averageRating.toFixed(1)}</span>
                    </div>
                  </div>
                  
                  <p className="text-foreground whitespace-pre-wrap">{review.comment}</p>
                  
                  <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 text-xs text-muted-foreground mt-2 bg-secondary/50 p-3 rounded-md">
                    <div>Clarity: <span className="font-medium text-foreground">{review.ratingClarity}</span></div>
                    <div>Fairness: <span className="font-medium text-foreground">{review.ratingFairness}</span></div>
                    <div>Pressure: <span className="font-medium text-foreground">{review.ratingPressure}</span></div>
                    <div>Workload: <span className="font-medium text-foreground">{review.ratingWorkload}</span></div>
                    <div>Support: <span className="font-medium text-foreground">{review.ratingSupport}</span></div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-10">
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-secondary text-muted-foreground mb-4">
                <Star className="h-8 w-8" />
              </div>
              <h3 className="text-lg font-medium">No reviews yet</h3>
              <p className="text-muted-foreground mt-1 mb-4">You haven't written any lecturer reviews.</p>
              <Link to="/student/lecturers" className="text-primary hover:underline">
                Browse lecturers to review
              </Link>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
