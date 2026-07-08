import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { lecturerApi } from '@/services/api/lecturerApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Star, Building2, BookOpen, GraduationCap, ChevronRight } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Landing() {
  const { data: response, isLoading } = useQuery({
    queryKey: ['landing-lecturers'],
    queryFn: () => lecturerApi.getLecturersPage({ page: 0, size: 8 }) // Fetch some lecturers to feature
  });

  const featuredLecturers = (response?.content || []).sort((a, b) => b.averageRating - a.averageRating);

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      {/* Navbar */}
      <header className="h-16 flex items-center justify-between px-6 md:px-12 lg:px-24 border-b bg-card/80 backdrop-blur-md sticky top-0 z-50">
        <Link to="/" className="font-bold text-xl flex items-center gap-2">
          <span className="bg-primary text-primary-foreground p-1.5 rounded-md shadow-sm">CTU</span>
          <span className="tracking-tight">Review</span>
        </Link>
        <div className="flex items-center gap-4">
          <Button variant="ghost" asChild>
            <Link to="/login">Sign In</Link>
          </Button>
          <Button asChild>
            <Link to="/register">Get Started</Link>
          </Button>
        </div>
      </header>

      {/* Hero Section */}
      <section className="px-6 py-24 md:py-32 flex flex-col items-center text-center space-y-8 bg-gradient-to-b from-background to-muted/50">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-3xl space-y-6"
        >
          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight">
            Discover and Review <br className="hidden md:block"/>
            <span className="text-primary">CTU Lecturers</span>
          </h1>
          <p className="text-lg md:text-xl text-muted-foreground">
            Make informed decisions about your classes. Read authentic reviews from fellow students and share your own academic experiences.
          </p>
          <div className="flex items-center justify-center gap-4 pt-4">
            <Button size="lg" asChild className="h-12 px-8">
              <Link to="/register">
                Join the Community <ChevronRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" asChild className="h-12 px-8">
              <Link to="/login">
                Browse Lecturers
              </Link>
            </Button>
          </div>
        </motion.div>
      </section>

      {/* Featured Lecturers */}
      <section className="px-6 py-20 md:px-12 lg:px-24">
        <div className="max-w-7xl mx-auto space-y-10">
          <div className="text-center space-y-4">
            <h2 className="text-3xl font-bold tracking-tight">Featured Lecturers</h2>
            <p className="text-muted-foreground max-w-2xl mx-auto">
              Check out some of the most reviewed lecturers at Can Tho University. Sign in to view their detailed ratings and read student comments.
            </p>
          </div>

          {isLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[...Array(8)].map((_, i) => (
                <Card key={i} className="animate-pulse h-[200px] bg-muted/50" />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {featuredLecturers.map((lecturer, index) => (
                <motion.div
                  key={lecturer.id}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1 }}
                >
                  <Card className="h-full hover:shadow-md transition-shadow group relative overflow-hidden">
                    {index < 3 && (
                      <div className="absolute top-0 right-0 bg-yellow-500 text-white text-xs font-bold px-3 py-1 rounded-bl-lg z-10 shadow-sm">
                        Top Rated
                      </div>
                    )}
                    <CardContent className="p-6 flex flex-col items-center text-center h-full space-y-4">
                      <div className="h-16 w-16 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-xl group-hover:scale-110 transition-transform">
                        {lecturer.fullName.charAt(0)}
                      </div>
                      <div className="space-y-2">
                        <h3 className="font-semibold text-lg line-clamp-1">{lecturer.fullName}</h3>
                        <div className="text-sm text-muted-foreground flex items-center justify-center gap-1">
                          <Building2 className="h-3 w-3" />
                          <span className="line-clamp-1">{lecturer.facultyName}</span>
                        </div>
                        <div className="text-sm text-muted-foreground flex items-center justify-center gap-1">
                          <BookOpen className="h-3 w-3" />
                          <span className="line-clamp-1">{lecturer.subjectName || 'Various Subjects'}</span>
                        </div>
                      </div>
                      <div className="flex w-full items-center justify-between mt-auto pt-4 border-t text-sm font-medium">
                        <div className="flex items-center gap-1">
                          <Star className="h-4 w-4 text-yellow-500 fill-yellow-500" />
                          <span>{lecturer.averageRating.toFixed(1)}</span>
                        </div>
                        <span className="text-muted-foreground font-normal">{lecturer.reviewCount} reviews</span>
                      </div>
                    </CardContent>
                  </Card>
                </motion.div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-auto py-8 border-t px-6 md:px-12 lg:px-24 flex flex-col md:flex-row items-center justify-between text-sm text-muted-foreground">
        <div className="flex items-center gap-2">
          <GraduationCap className="h-5 w-5" />
          <span>© 2026 CTU Review System. All rights reserved.</span>
        </div>
        <div className="flex gap-4 mt-4 md:mt-0">
          <Link to="#" className="hover:text-foreground transition-colors">Privacy Policy</Link>
          <Link to="#" className="hover:text-foreground transition-colors">Terms of Service</Link>
          <Link to="#" className="hover:text-foreground transition-colors">Contact</Link>
        </div>
      </footer>
    </div>
  );
}
