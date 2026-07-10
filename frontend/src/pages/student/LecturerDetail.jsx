import { useQuery } from '@tanstack/react-query';
import { useParams, Link } from 'react-router-dom';
import { lecturerApi } from '@/services/api/lecturerApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Star, Building2, BookOpen, ChevronLeft, MessageSquarePlus } from 'lucide-react';
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
      <div className="space-y-6">
        <div className="flex space-x-6 items-start">
          <Skeleton className="w-32 h-32 rounded-full" />
          <div className="space-y-4 flex-1">
            <Skeleton className="h-8 w-1/3" />
            <Skeleton className="h-4 w-1/4" />
            <Skeleton className="h-4 w-1/4" />
          </div>
        </div>
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (!lecturer) return <div>Lecturer not found</div>;

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      <Button variant="ghost" asChild className="mb-4">
        <Link to="/student/lecturers">
          <ChevronLeft className="mr-2 h-4 w-4" /> Back to Lecturers
        </Link>
      </Button>

      <div className="flex flex-col md:flex-row gap-8 items-start">
        <div className="flex-shrink-0">
          <div className="h-32 w-32 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-4xl shadow-md border-4 border-background">
            {lecturer.fullName.charAt(0)}
          </div>
        </div>
        <div className="flex-1 space-y-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{lecturer.fullName}</h1>
            <div className="flex items-center gap-2 mt-2 text-muted-foreground">
              <Building2 className="h-4 w-4" />
              <span>{lecturer.facultyName}</span>
            </div>
          </div>
          
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <Star className="h-6 w-6 text-yellow-500 fill-yellow-500" />
              <span className="text-2xl font-bold">{(lecturer.averageRating || 0).toFixed(1)}</span>
              <span className="text-muted-foreground">({lecturer.reviewCount || 0} reviews)</span>
            </div>
          </div>

          <div className="pt-4 flex gap-4">
            <Button asChild>
              <Link to={`/student/lecturers/${id}/review`}>
                <MessageSquarePlus className="mr-2 h-4 w-4" /> Write a Review
              </Link>
            </Button>
            <Button variant="outline">
              Bookmark
            </Button>
          </div>
        </div>
      </div>

      <Tabs defaultValue="overview" className="w-full">
        <TabsList className="w-full justify-start border-b rounded-none h-auto p-0 bg-transparent">
          <TabsTrigger value="overview" className="data-[state=active]:border-b-2 data-[state=active]:border-primary rounded-none py-3 px-6">
            Overview
          </TabsTrigger>
          <TabsTrigger value="reviews" className="data-[state=active]:border-b-2 data-[state=active]:border-primary rounded-none py-3 px-6">
            Reviews
          </TabsTrigger>
          <TabsTrigger value="subjects" className="data-[state=active]:border-b-2 data-[state=active]:border-primary rounded-none py-3 px-6">
            Subjects
          </TabsTrigger>
        </TabsList>
        <TabsContent value="overview" className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Rating Distribution</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {[5, 4, 3, 2, 1].map((star) => {
                  const dist = lecturer.distribution?.find(d => d.score === star);
                  const count = dist ? dist.count : 0;
                  const total = lecturer.reviewCount > 0 ? lecturer.reviewCount : 1;
                  const percentage = (count / total) * 100;
                  return (
                    <div key={star} className="flex items-center gap-4 text-sm">
                      <span className="w-16">{star} Stars</span>
                      <div className="flex-1 h-2 bg-secondary rounded-full overflow-hidden">
                        <div className="h-full bg-yellow-500 rounded-full" style={{ width: `${percentage}%` }} />
                      </div>
                      <span className="w-10 text-right text-muted-foreground">{count}</span>
                    </div>
                  );
                })}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="reviews" className="mt-6">
          <Card>
            <CardContent className="p-6">
              {lecturer.latestReviews && lecturer.latestReviews.length > 0 ? (
                <div className="space-y-6">
                  {lecturer.latestReviews.map((review) => (
                    <div key={review.id} className="border-b pb-6 last:border-0 last:pb-0">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <Star className="h-4 w-4 text-yellow-500 fill-yellow-500" />
                          <span className="font-bold">{review.averageRating.toFixed(1)}</span>
                          <span className="text-sm text-muted-foreground ml-2">
                            Semester {review.semester}, {review.academicYear}
                          </span>
                        </div>
                        <span className="text-sm text-muted-foreground">
                          {new Date(review.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      <p className="mt-2 text-foreground whitespace-pre-wrap">{review.comment}</p>
                      <div className="mt-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2 text-xs text-muted-foreground">
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
                <p className="text-muted-foreground">No reviews yet. Be the first to review!</p>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="subjects" className="mt-6">
          <Card>
            <CardContent className="p-6">
              <ul className="space-y-2">
                {lecturer.subjectName ? (
                  <li className="flex items-center gap-2">
                    <BookOpen className="h-4 w-4 text-primary" />
                    <span>{lecturer.subjectName}</span>
                  </li>
                ) : (
                  <li className="text-muted-foreground">No specific subjects listed.</li>
                )}
              </ul>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
