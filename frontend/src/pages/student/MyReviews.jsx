import { useQuery } from '@tanstack/react-query';
import { reviewApi } from '@/services/api/reviewApi';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Star, ExternalLink, Calendar, BookOpen, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';

export default function MyReviews() {
  const { data: myReviews, isLoading } = useQuery({
    queryKey: ['myReviews'],
    queryFn: () => reviewApi.getMyReviews()
  });

  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  return (
    <div className="space-y-8 max-w-6xl mx-auto pb-12">
      <div className="flex flex-col gap-2 bg-card p-8 rounded-3xl border border-border/50 shadow-sm relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
        <h2 className="text-3xl font-extrabold tracking-tight relative z-10 text-foreground">My Reviews</h2>
        <p className="text-muted-foreground text-lg relative z-10">History of your lecturer reviews and ratings.</p>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i} className="rounded-2xl border-border/50">
              <CardContent className="p-6">
                <Skeleton className="h-6 w-3/4 mb-4" />
                <Skeleton className="h-20 w-full mb-4" />
                <div className="flex gap-2">
                  <Skeleton className="h-8 w-20" />
                  <Skeleton className="h-8 w-20" />
                  <Skeleton className="h-8 w-20" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : myReviews && myReviews.length > 0 ? (
        <motion.div 
          variants={container}
          initial="hidden"
          animate="show"
          className="grid grid-cols-1 lg:grid-cols-2 gap-6"
        >
          {myReviews.map((review) => (
            <motion.div variants={item} key={review.id}>
              <Card className="h-full flex flex-col rounded-2xl border-border/50 shadow-sm hover:shadow-md transition-all group overflow-hidden bg-card/50 backdrop-blur-sm relative">
                <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-primary via-primary/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <CardContent className="p-6 md:p-8 flex flex-col flex-1">
                  <div className="flex items-start justify-between mb-4 gap-4">
                    <div className="flex-1 min-w-0">
                      <Link 
                        to={`/student/lecturers/${review.lecturerId}`}
                        className="font-bold text-xl hover:text-primary transition-colors flex items-center gap-2 group/link truncate"
                        title={review.lecturerName}
                      >
                        <span className="truncate">{review.lecturerName}</span>
                        <ExternalLink className="h-4 w-4 text-muted-foreground group-hover/link:text-primary flex-shrink-0" />
                      </Link>
                      <div className="flex flex-wrap items-center text-sm text-muted-foreground mt-2 gap-x-4 gap-y-2">
                        <span className="flex items-center gap-1.5 bg-secondary/50 px-2 py-1 rounded-md">
                          <BookOpen className="h-3.5 w-3.5" />
                          Sem {review.semester}, {review.academicYear}
                        </span>
                        <span className="flex items-center gap-1.5 bg-secondary/50 px-2 py-1 rounded-md">
                          <Calendar className="h-3.5 w-3.5" />
                          {new Date(review.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}
                        </span>
                      </div>
                    </div>
                    <div className="flex flex-col items-center justify-center bg-yellow-500/10 px-3 py-2 rounded-xl">
                      <div className="flex items-center text-yellow-600 font-black text-xl">
                        <Star className="h-5 w-5 fill-current mr-1" />
                        <span>{review.averageRating.toFixed(1)}</span>
                      </div>
                      <span className="text-[10px] uppercase font-bold text-yellow-600/70 tracking-wider mt-0.5">Rating</span>
                    </div>
                  </div>
                  
                  <div className="flex-1 my-4 bg-background/50 rounded-xl p-4 border border-border/30">
                    <p className="text-foreground/90 whitespace-pre-wrap text-[15px] leading-relaxed line-clamp-4 group-hover:line-clamp-none transition-all duration-300 relative">
                      {review.comment}
                    </p>
                  </div>
                  
                  <div className="flex flex-wrap gap-2 text-xs mt-auto">
                    {[
                      { label: 'Clarity', val: review.ratingClarity },
                      { label: 'Fairness', val: review.ratingFairness },
                      { label: 'Pressure', val: review.ratingPressure },
                      { label: 'Workload', val: review.ratingWorkload },
                      { label: 'Support', val: review.ratingSupport }
                    ].map((item, idx) => (
                      <div key={idx} className="flex items-center bg-secondary/30 px-2.5 py-1.5 rounded-lg border border-border/50">
                        <span className="text-muted-foreground mr-1.5">{item.label}:</span>
                        <span className={`font-bold ${item.val >= 4 ? 'text-green-600' : item.val <= 2 ? 'text-red-500' : 'text-yellow-600'}`}>
                          {item.val}
                        </span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      ) : (
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center py-20 bg-card rounded-3xl border border-dashed border-border"
        >
          <div className="mx-auto w-20 h-20 bg-muted/50 rounded-full flex items-center justify-center mb-6">
            <Star className="h-10 w-10 text-muted-foreground/50" />
          </div>
          <h3 className="text-2xl font-semibold text-foreground">No reviews yet</h3>
          <p className="text-muted-foreground mt-2 mb-8 max-w-sm mx-auto">You haven't written any lecturer reviews yet. Your feedback is valuable!</p>
          <Button asChild size="lg" className="rounded-xl shadow-md hover:shadow-lg transition-all">
            <Link to="/student/lecturers">
              Browse Lecturers to Review
            </Link>
          </Button>
        </motion.div>
      )}
    </div>
  );
}
