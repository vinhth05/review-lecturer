import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { lecturerApi } from '@/services/api/lecturerApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Star, Building2, BookOpen, ChevronLeft, MessageSquarePlus, Bookmark } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { motion } from 'framer-motion';

export default function LecturerDetail() {
  const { id } = useParams();

  const { data: lecturer, isLoading } = useQuery({
    queryKey: ['lecturer', id],
    queryFn: () => lecturerApi.getLecturerDetails(id)
  });

  if (isLoading) {
    return (
      <div className="space-y-6 max-w-5xl mx-auto">
        <Skeleton className="h-48 w-full rounded-3xl" />
        <div className="flex flex-col md:flex-row gap-6 items-start px-8">
          <Skeleton className="w-32 h-32 rounded-full -mt-16 border-4 border-background" />
          <div className="space-y-4 flex-1 mt-4">
            <Skeleton className="h-10 w-1/3" />
            <Skeleton className="h-4 w-1/4" />
          </div>
        </div>
        <Skeleton className="h-64 w-full rounded-2xl mt-8" />
      </div>
    );
  }

  if (!lecturer) return <div className="text-center py-20 text-xl font-medium text-muted-foreground">Lecturer not found</div>;

  return (
    <div className="space-y-8 max-w-5xl mx-auto pb-12">
      <Button variant="ghost" asChild className="mb-2 hover:bg-transparent hover:text-primary transition-colors group">
        <Link to="/student/lecturers">
          <ChevronLeft className="mr-2 h-4 w-4 group-hover:-translate-x-1 transition-transform" /> Back to Lecturers
        </Link>
      </Button>

      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="relative"
      >
        {/* Banner */}
        <div className="h-48 rounded-3xl bg-gradient-to-r from-primary/80 via-primary/40 to-primary/10 overflow-hidden relative">
          <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-20 mix-blend-overlay"></div>
          <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-transparent"></div>
        </div>

        <div className="flex flex-col md:flex-row gap-6 items-start px-6 md:px-10 -mt-16 relative z-10">
          <div className="flex-shrink-0">
            <motion.div 
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ type: "spring", delay: 0.1 }}
              className="h-32 w-32 rounded-full bg-card text-primary flex items-center justify-center font-extrabold text-5xl shadow-2xl border-4 border-background relative overflow-hidden"
            >
              <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-primary/5"></div>
              <span className="relative z-10">{lecturer.fullName.charAt(0)}</span>
            </motion.div>
          </div>
          
          <div className="flex-1 space-y-4 pt-4 md:pt-16 w-full">
            <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
              <div>
                <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-foreground">{lecturer.fullName}</h1>
                <div className="flex items-center gap-2 mt-2 text-muted-foreground font-medium">
                  <Building2 className="h-5 w-5 text-primary/70" />
                  <span>{lecturer.facultyName}</span>
                </div>
              </div>
              
              <div className="flex items-center gap-4 bg-card/80 backdrop-blur-sm p-4 rounded-2xl border shadow-sm">
                <div className="flex flex-col items-center">
                  <div className="flex items-center gap-1.5">
                    <Star className="h-8 w-8 text-yellow-500 fill-yellow-500" />
                    <span className="text-3xl font-black text-foreground">{(lecturer.averageRating || 0).toFixed(1)}</span>
                  </div>
                  <span className="text-sm font-medium text-muted-foreground mt-1">{lecturer.reviewCount || 0} reviews</span>
                </div>
              </div>
            </div>

            <div className="pt-2 flex flex-wrap gap-3">
              <Button asChild size="lg" className="rounded-xl shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all">
                <Link to={`/student/lecturers/${id}/review`}>
                  <MessageSquarePlus className="mr-2 h-5 w-5" /> Write a Review
                </Link>
              </Button>
              <Button variant="outline" size="lg" className="rounded-xl border-2 hover:bg-primary/5">
                <Bookmark className="mr-2 h-5 w-5" /> Bookmark
              </Button>
            </div>
          </div>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="mt-12"
      >
        <Tabs defaultValue="overview" className="w-full">
          <TabsList className="w-full justify-start border-b-2 border-border/50 rounded-none h-auto p-0 bg-transparent gap-8">
            {['overview', 'reviews', 'subjects'].map((tab) => (
              <TabsTrigger 
                key={tab}
                value={tab} 
                className="data-[state=active]:border-primary data-[state=active]:text-primary data-[state=active]:bg-transparent border-b-2 border-transparent rounded-none py-4 px-2 text-base font-semibold text-muted-foreground transition-colors hover:text-foreground capitalize relative"
              >
                {tab}
              </TabsTrigger>
            ))}
          </TabsList>
          
          <div className="mt-8">
            <TabsContent value="overview">
              <Card className="rounded-2xl border-border/50 shadow-sm overflow-hidden">
                <CardHeader className="bg-secondary/20 border-b border-border/50">
                  <CardTitle className="text-xl">Rating Distribution</CardTitle>
                </CardHeader>
                <CardContent className="p-8">
                  <div className="space-y-4 max-w-xl mx-auto">
                    {[5, 4, 3, 2, 1].map((star, index) => {
                      const dist = lecturer.distribution?.find(d => d.score === star);
                      const count = dist ? dist.count : 0;
                      const total = lecturer.reviewCount > 0 ? lecturer.reviewCount : 1;
                      const percentage = (count / total) * 100;
                      return (
                        <div key={star} className="flex items-center gap-6 text-sm font-medium">
                          <div className="flex items-center w-20 gap-1 text-muted-foreground">
                            <span>{star}</span>
                            <Star className="h-4 w-4 fill-current" />
                          </div>
                          <div className="flex-1 h-3 bg-secondary rounded-full overflow-hidden">
                            <motion.div 
                              initial={{ width: 0 }}
                              animate={{ width: `${percentage}%` }}
                              transition={{ duration: 1, delay: 0.3 + index * 0.1, ease: "easeOut" }}
                              className="h-full bg-yellow-500 rounded-full" 
                            />
                          </div>
                          <span className="w-12 text-right text-muted-foreground">{count}</span>
                        </div>
                      );
                    })}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="reviews">
              <Card className="rounded-2xl border-border/50 shadow-sm">
                <CardContent className="p-6 md:p-8">
                  {lecturer.latestReviews && lecturer.latestReviews.length > 0 ? (
                    <div className="space-y-6">
                      {lecturer.latestReviews.map((review, i) => (
                        <motion.div 
                          initial={{ opacity: 0, y: 10 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ delay: 0.1 * i }}
                          key={review.id} 
                          className="bg-card border rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow"
                        >
                          <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-4 gap-2">
                            <div className="flex items-center gap-3">
                              <div className="flex items-center gap-1.5 bg-yellow-500/10 text-yellow-600 px-3 py-1 rounded-full font-bold">
                                <Star className="h-4 w-4 fill-current" />
                                <span>{review.averageRating.toFixed(1)}</span>
                              </div>
                              <span className="text-sm font-medium text-primary bg-primary/10 px-3 py-1 rounded-full">
                                Semester {review.semester}, {review.academicYear}
                              </span>
                            </div>
                            <span className="text-sm text-muted-foreground font-medium">
                              {new Date(review.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}
                            </span>
                          </div>
                          
                          <p className="mt-4 text-foreground/90 whitespace-pre-wrap leading-relaxed text-[15px]">{review.comment}</p>
                          
                          <div className="mt-6 flex flex-wrap gap-2 text-xs">
                            {[
                              { label: 'Clarity', val: review.ratingClarity },
                              { label: 'Fairness', val: review.ratingFairness },
                              { label: 'Pressure', val: review.ratingPressure },
                              { label: 'Workload', val: review.ratingWorkload },
                              { label: 'Support', val: review.ratingSupport }
                            ].map((item, idx) => (
                              <div key={idx} className="flex items-center bg-secondary/50 px-3 py-1.5 rounded-lg border border-border/50">
                                <span className="text-muted-foreground mr-2">{item.label}:</span>
                                <span className={`font-bold ${item.val >= 4 ? 'text-green-600' : item.val <= 2 ? 'text-red-500' : 'text-yellow-600'}`}>
                                  {item.val}
                                </span>
                              </div>
                            ))}
                          </div>
                        </motion.div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-16">
                      <div className="mx-auto w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4">
                        <MessageSquarePlus className="h-8 w-8 text-muted-foreground" />
                      </div>
                      <h3 className="text-xl font-semibold">No reviews yet</h3>
                      <p className="text-muted-foreground mt-2 mb-6">Be the first to share your experience with this lecturer!</p>
                      <Button asChild className="rounded-xl">
                        <Link to={`/student/lecturers/${id}/review`}>
                          Write a Review
                        </Link>
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="subjects">
              <Card className="rounded-2xl border-border/50 shadow-sm">
                <CardContent className="p-8">
                  <h3 className="text-lg font-semibold mb-6 flex items-center gap-2">
                    <BookOpen className="h-5 w-5 text-primary" /> Taught Subjects
                  </h3>
                  <ul className="space-y-3">
                    {lecturer.subjectName ? (
                      <li className="flex items-center gap-3 p-4 rounded-xl bg-secondary/30 border border-border/50">
                        <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-bold">
                          {lecturer.subjectName.charAt(0)}
                        </div>
                        <span className="font-medium text-[15px]">{lecturer.subjectName}</span>
                      </li>
                    ) : (
                      <li className="text-muted-foreground italic p-4 bg-muted/50 rounded-xl">No specific subjects listed for this lecturer.</li>
                    )}
                  </ul>
                </CardContent>
              </Card>
            </TabsContent>
          </div>
        </Tabs>
      </motion.div>
    </div>
  );
}
