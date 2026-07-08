import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useQuery } from '@tanstack/react-query';
import { lecturerApi } from '@/services/api/lecturerApi';
import { reviewApi } from '@/services/api/reviewApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Star, ChevronLeft, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const reviewSchema = z.object({
  ratingClarity: z.number().min(1, 'Please provide a rating').max(5),
  ratingFairness: z.number().min(1, 'Please provide a rating').max(5),
  ratingPressure: z.number().min(1, 'Please provide a rating').max(5),
  ratingWorkload: z.number().min(1, 'Please provide a rating').max(5),
  ratingSupport: z.number().min(1, 'Please provide a rating').max(5),
  comment: z.string().min(10, 'Review must be at least 10 characters').max(1000, 'Review is too long'),
  semester: z.string().min(1, 'Please select a semester'),
  academicYear: z.string().min(1, 'Please select an academic year'),
});

const RatingStars = ({ value, onChange }) => {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`h-6 w-6 cursor-pointer transition-colors ${
            star <= value ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground'
          }`}
          onClick={() => onChange(star)}
        />
      ))}
    </div>
  );
};

export default function SubmitReview() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { data: lecturer, isLoading: isLoadingLecturer } = useQuery({
    queryKey: ['lecturer', id],
    queryFn: () => lecturerApi.getLecturerDetails(id)
  });

  const { register, handleSubmit, control, formState: { errors } } = useForm({
    resolver: zodResolver(reviewSchema),
    defaultValues: {
      ratingClarity: 0,
      ratingFairness: 0,
      ratingPressure: 0,
      ratingWorkload: 0,
      ratingSupport: 0,
      comment: '',
      semester: '1',
      academicYear: '2025-2026'
    }
  });

  const onSubmit = async (data) => {
    try {
      setIsSubmitting(true);
      await reviewApi.submitReview({
        ...data,
        lecturerId: parseInt(id)
      });
      toast.success("Review submitted successfully! It is pending approval.");
      navigate(`/student/lecturers/${id}`);
    } catch (error) {
      toast.error(error.message || "Failed to submit review");
    } finally {
      setIsSubmitting(false);
    }
  };

  const ratingCategories = [
    { name: 'ratingClarity', label: 'Clarity of Teaching' },
    { name: 'ratingFairness', label: 'Fairness in Grading' },
    { name: 'ratingPressure', label: 'Academic Pressure' },
    { name: 'ratingWorkload', label: 'Course Workload' },
    { name: 'ratingSupport', label: 'Student Support' },
  ];

  if (isLoadingLecturer) return <div className="flex justify-center p-10"><Loader2 className="animate-spin h-8 w-8" /></div>;
  if (!lecturer) return <div>Lecturer not found</div>;

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <Button variant="ghost" asChild className="mb-4">
        <Link to={`/student/lecturers/${id}`}>
          <ChevronLeft className="mr-2 h-4 w-4" /> Back to Lecturer
        </Link>
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Write a Review for {lecturer.fullName}</CardTitle>
          <CardDescription>
            Your feedback helps other students make informed decisions. Please be honest and constructive.
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit(onSubmit)}>
          <CardContent className="space-y-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-medium">Semester</label>
                <Controller
                  name="semester"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select semester" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="1">Semester 1</SelectItem>
                        <SelectItem value="2">Semester 2</SelectItem>
                        <SelectItem value="3">Summer Semester</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.semester && <p className="text-sm text-destructive">{errors.semester.message}</p>}
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Academic Year</label>
                <Controller
                  name="academicYear"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select year" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="2024-2025">2024-2025</SelectItem>
                        <SelectItem value="2025-2026">2025-2026</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.academicYear && <p className="text-sm text-destructive">{errors.academicYear.message}</p>}
              </div>
            </div>

            <div className="space-y-6">
              <h3 className="font-semibold text-lg border-b pb-2">Rate the Lecturer</h3>
              {ratingCategories.map(category => (
                <div key={category.name} className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <label className="text-sm font-medium">{category.label}</label>
                  <Controller
                    name={category.name}
                    control={control}
                    render={({ field }) => (
                      <div className="flex flex-col items-end">
                        <RatingStars value={field.value} onChange={field.onChange} />
                        {errors[category.name] && <p className="text-xs text-destructive mt-1">{errors[category.name].message}</p>}
                      </div>
                    )}
                  />
                </div>
              ))}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Detailed Comment</label>
              <Textarea
                placeholder="Share your experience taking this lecturer's class..."
                className={`min-h-[150px] ${errors.comment ? "border-destructive" : ""}`}
                {...register('comment')}
              />
              {errors.comment && <p className="text-sm text-destructive">{errors.comment.message}</p>}
            </div>
          </CardContent>
          <CardFooter className="flex justify-end gap-4">
            <Button variant="outline" type="button" onClick={() => navigate(`/student/lecturers/${id}`)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Submit Review
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
