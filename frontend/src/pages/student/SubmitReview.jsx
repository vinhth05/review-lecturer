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
import { Star, ChevronLeft, Loader2, Info, GraduationCap, PenLine } from 'lucide-react';
import { toast } from 'sonner';
import { motion, AnimatePresence } from 'framer-motion';

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

const RatingStars = ({ value, onChange, error }) => {
  const [hoverValue, setHoverValue] = useState(0);

  return (
    <div className="flex flex-col items-end">
      <div className="flex gap-1.5" onMouseLeave={() => setHoverValue(0)}>
        {[1, 2, 3, 4, 5].map((star) => {
          const isFilled = star <= (hoverValue || value);
          return (
            <motion.div
              key={star}
              whileHover={{ scale: 1.2 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => onChange(star)}
              onMouseEnter={() => setHoverValue(star)}
              className="cursor-pointer"
            >
              <Star
                className={`h-7 w-7 transition-colors duration-200 ${
                  isFilled 
                    ? 'text-yellow-500 fill-yellow-500 drop-shadow-[0_0_8px_rgba(234,179,8,0.5)]' 
                    : 'text-muted-foreground/30 hover:text-yellow-500/50'
                }`}
              />
            </motion.div>
          );
        })}
      </div>
      <AnimatePresence>
        {error && (
          <motion.p 
            initial={{ opacity: 0, height: 0 }} 
            animate={{ opacity: 1, height: 'auto' }} 
            exit={{ opacity: 0, height: 0 }}
            className="text-xs text-destructive mt-1 font-medium"
          >
            {error}
          </motion.p>
        )}
      </AnimatePresence>
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
    { name: 'ratingClarity', label: 'Clarity of Teaching', desc: 'How well does the lecturer explain concepts?' },
    { name: 'ratingFairness', label: 'Fairness in Grading', desc: 'Is the grading criteria clear and fair?' },
    { name: 'ratingPressure', label: 'Academic Pressure', desc: 'How stressful is the class environment?' },
    { name: 'ratingWorkload', label: 'Course Workload', desc: 'Amount of assignments and studying required.' },
    { name: 'ratingSupport', label: 'Student Support', desc: 'Is the lecturer approachable and helpful outside class?' },
  ];

  if (isLoadingLecturer) return (
    <div className="flex flex-col justify-center items-center h-64 space-y-4">
      <Loader2 className="animate-spin h-10 w-10 text-primary" />
      <p className="text-muted-foreground font-medium animate-pulse">Loading lecturer data...</p>
    </div>
  );
  if (!lecturer) return <div className="text-center py-20 text-xl font-medium text-muted-foreground">Lecturer not found</div>;

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-3xl mx-auto space-y-6 pb-12"
    >
      <Button variant="ghost" asChild className="mb-2 hover:bg-transparent hover:text-primary transition-colors group">
        <Link to={`/student/lecturers/${id}`}>
          <ChevronLeft className="mr-2 h-4 w-4 group-hover:-translate-x-1 transition-transform" /> Back to Lecturer
        </Link>
      </Button>

      <div className="text-center space-y-2 mb-8">
        <h1 className="text-3xl font-extrabold tracking-tight">Review {lecturer.fullName}</h1>
        <p className="text-muted-foreground text-lg">Your feedback helps other students make informed decisions.</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        <Card className="rounded-2xl border-border/50 shadow-sm overflow-hidden border-t-4 border-t-primary">
          <CardHeader className="bg-secondary/10 border-b border-border/50 pb-4">
            <CardTitle className="text-lg flex items-center gap-2">
              <GraduationCap className="h-5 w-5 text-primary" /> Course Information
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2.5">
                <label className="text-sm font-semibold text-foreground">Semester <span className="text-destructive">*</span></label>
                <Controller
                  name="semester"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <SelectTrigger className={`h-12 rounded-xl transition-all ${errors.semester ? 'border-destructive focus:ring-destructive/20' : 'hover:border-primary focus:ring-primary/20'}`}>
                        <SelectValue placeholder="Select semester" />
                      </SelectTrigger>
                      <SelectContent className="rounded-xl">
                        <SelectItem value="1">Semester 1</SelectItem>
                        <SelectItem value="2">Semester 2</SelectItem>
                        <SelectItem value="3">Summer Semester</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.semester && <p className="text-sm text-destructive font-medium">{errors.semester.message}</p>}
              </div>

              <div className="space-y-2.5">
                <label className="text-sm font-semibold text-foreground">Academic Year <span className="text-destructive">*</span></label>
                <Controller
                  name="academicYear"
                  control={control}
                  render={({ field }) => (
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <SelectTrigger className={`h-12 rounded-xl transition-all ${errors.academicYear ? 'border-destructive focus:ring-destructive/20' : 'hover:border-primary focus:ring-primary/20'}`}>
                        <SelectValue placeholder="Select year" />
                      </SelectTrigger>
                      <SelectContent className="rounded-xl">
                        <SelectItem value="2024-2025">2024-2025</SelectItem>
                        <SelectItem value="2025-2026">2025-2026</SelectItem>
                      </SelectContent>
                    </Select>
                  )}
                />
                {errors.academicYear && <p className="text-sm text-destructive font-medium">{errors.academicYear.message}</p>}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="rounded-2xl border-border/50 shadow-sm overflow-hidden border-t-4 border-t-yellow-500">
          <CardHeader className="bg-secondary/10 border-b border-border/50 pb-4">
            <CardTitle className="text-lg flex items-center gap-2">
              <Star className="h-5 w-5 text-yellow-500 fill-yellow-500" /> Ratings
            </CardTitle>
            <CardDescription>Rate the lecturer on a scale of 1 to 5 stars.</CardDescription>
          </CardHeader>
          <CardContent className="pt-6 space-y-6">
            {ratingCategories.map((category, index) => (
              <motion.div 
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 * index }}
                key={category.name} 
                className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-xl hover:bg-secondary/20 transition-colors border border-transparent hover:border-border/50"
              >
                <div>
                  <label className="text-base font-semibold text-foreground block">{category.label} <span className="text-destructive">*</span></label>
                  <p className="text-sm text-muted-foreground mt-0.5">{category.desc}</p>
                </div>
                <Controller
                  name={category.name}
                  control={control}
                  render={({ field }) => (
                    <RatingStars 
                      value={field.value} 
                      onChange={field.onChange} 
                      error={errors[category.name]?.message} 
                    />
                  )}
                />
              </motion.div>
            ))}
          </CardContent>
        </Card>

        <Card className="rounded-2xl border-border/50 shadow-sm overflow-hidden border-t-4 border-t-indigo-500">
          <CardHeader className="bg-secondary/10 border-b border-border/50 pb-4">
            <CardTitle className="text-lg flex items-center gap-2">
              <PenLine className="h-5 w-5 text-indigo-500" /> Detailed Comment
            </CardTitle>
            <CardDescription>Share your honest experience (minimum 10 characters).</CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            <div className="space-y-3">
              <Textarea
                placeholder="How was the teaching style? Were the exams fair? Any advice for future students?"
                className={`min-h-[160px] resize-y rounded-xl p-4 text-base transition-all ${errors.comment ? "border-destructive focus-visible:ring-destructive/20" : "hover:border-primary focus-visible:ring-primary/20"}`}
                {...register('comment')}
              />
              <AnimatePresence>
                {errors.comment && (
                  <motion.p 
                    initial={{ opacity: 0, y: -10 }} 
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="text-sm text-destructive font-medium flex items-center gap-1"
                  >
                    <Info className="h-4 w-4" /> {errors.comment.message}
                  </motion.p>
                )}
              </AnimatePresence>
            </div>
          </CardContent>
          <CardFooter className="bg-secondary/5 border-t border-border/50 p-6 flex flex-col-reverse sm:flex-row justify-end gap-3">
            <Button variant="outline" type="button" className="rounded-xl h-12 px-6" onClick={() => navigate(`/student/lecturers/${id}`)}>
              Cancel
            </Button>
            <Button type="submit" className="rounded-xl h-12 px-8 font-semibold shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Submitting...
                </>
              ) : (
                'Submit Review'
              )}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </motion.div>
  );
}
