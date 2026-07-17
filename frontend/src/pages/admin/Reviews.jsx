import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Check, X, Trash2, ShieldAlert } from 'lucide-react';
import { toast } from 'sonner';

export default function Reviews() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  
  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-reviews-pending', page],
    queryFn: () => adminApi.getPendingReviews(),
  });

  const approveMutation = useMutation({
    mutationFn: (id) => adminApi.approveReview(id),
    onSuccess: () => {
      toast.success('Review approved');
      queryClient.invalidateQueries(['admin-reviews-pending']);
    },
    onError: (error) => toast.error(error.message || 'Failed to approve review')
  });

  const rejectMutation = useMutation({
    mutationFn: (id) => adminApi.rejectReview(id),
    onSuccess: () => {
      toast.success('Review rejected');
      queryClient.invalidateQueries(['admin-reviews-pending']);
    },
    onError: (error) => toast.error(error.message || 'Failed to reject review')
  });

  // the endpoint getPendingReviews returns a List instead of a Page based on the docs and API
  const reviews = response || [];

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-extrabold tracking-tight text-gradient">Review Moderation</h1>
          <p className="text-muted-foreground">Approve or reject student reviews.</p>
        </div>
      </div>

      {isLoading ? (
        <div className="text-center py-12 text-muted-foreground border rounded-lg bg-card">Loading pending reviews...</div>
      ) : reviews.length === 0 ? (
        <div className="text-center py-12 border rounded-lg bg-card flex flex-col items-center">
          <ShieldAlert className="h-12 w-12 text-muted-foreground mb-4 opacity-50" />
          <h3 className="text-lg font-medium">All caught up!</h3>
          <p className="text-muted-foreground">There are no pending reviews to moderate.</p>
        </div>
      ) : (
        <div className="grid gap-6">
          {reviews.map((review) => (
            <Card key={review.id} className="glass-card overflow-hidden hover:-translate-y-1 transition-all duration-300 border-l-4 border-l-amber-500">
              <CardContent className="p-0">
                <div className="flex flex-col md:flex-row">
                  <div className="p-6 md:w-3/4 flex flex-col gap-4">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-lg">{review.lecturerName}</span>
                      <span className="text-muted-foreground mx-2">•</span>
                      <span className="text-sm font-medium">{review.studentCode || 'Anonymous'}</span>
                      <Badge variant="outline" className="ml-auto">{review.semester} - {review.academicYear}</Badge>
                    </div>
                    
                    <div className="relative">
                      <div className="absolute top-0 left-0 w-1 h-full bg-primary/20 rounded-l-md"></div>
                      <p className="text-foreground whitespace-pre-wrap bg-background/50 backdrop-blur-sm p-4 pl-6 rounded-md border shadow-inner text-sm leading-relaxed">
                        {review.comment}
                      </p>
                    </div>

                    <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 text-sm">
                      <div className="bg-muted rounded px-2 py-1 text-center">
                        <div className="text-xs text-muted-foreground mb-1">Clarity</div>
                        <div className="font-medium text-yellow-600">★ {review.ratingClarity}</div>
                      </div>
                      <div className="bg-muted rounded px-2 py-1 text-center">
                        <div className="text-xs text-muted-foreground mb-1">Fairness</div>
                        <div className="font-medium text-yellow-600">★ {review.ratingFairness}</div>
                      </div>
                      <div className="bg-muted rounded px-2 py-1 text-center">
                        <div className="text-xs text-muted-foreground mb-1">Pressure</div>
                        <div className="font-medium text-yellow-600">★ {review.ratingPressure}</div>
                      </div>
                      <div className="bg-muted rounded px-2 py-1 text-center">
                        <div className="text-xs text-muted-foreground mb-1">Workload</div>
                        <div className="font-medium text-yellow-600">★ {review.ratingWorkload}</div>
                      </div>
                      <div className="bg-muted rounded px-2 py-1 text-center">
                        <div className="text-xs text-muted-foreground mb-1">Support</div>
                        <div className="font-medium text-yellow-600">★ {review.ratingSupport}</div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="bg-muted/20 backdrop-blur-sm p-6 md:w-1/4 flex flex-row md:flex-col items-center justify-center gap-3 border-t md:border-t-0 md:border-l relative overflow-hidden">
                    <div className="absolute inset-0 bg-gradient-to-b from-transparent to-background/50 pointer-events-none"></div>
                    <Button 
                      className="w-full bg-green-600 hover:bg-green-500 shadow-md hover:shadow-lg transition-all relative z-10" 
                      onClick={() => approveMutation.mutate(review.id)}
                    >
                      <Check className="mr-2 h-4 w-4" /> Approve
                    </Button>
                    <Button 
                      variant="outline" 
                      className="w-full border-destructive text-destructive hover:bg-destructive hover:text-white transition-all relative z-10 shadow-sm"
                      onClick={() => rejectMutation.mutate(review.id)}
                    >
                      <X className="mr-2 h-4 w-4" /> Reject
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
