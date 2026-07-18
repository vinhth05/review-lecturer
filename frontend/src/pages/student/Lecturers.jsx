import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { lecturerApi } from '@/services/api/lecturerApi';
import { metadataApi } from '@/services/api/metadataApi';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Search, Star, Building2, BookOpen, ChevronRight } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';
import { motion } from 'framer-motion';

export default function Lecturers() {
  const [searchTerm, setSearchTerm] = useState('');
  const [faculty, setFaculty] = useState('all');

  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => metadataApi.getFaculties().then(res => res)
  });

  const { data: lecturersData, isLoading } = useQuery({
    queryKey: ['lecturers', faculty, searchTerm],
    queryFn: () => lecturerApi.getLecturersPage({
      facultyCode: faculty !== 'all' ? faculty : undefined,
      page: 0,
      size: 12
    })
  });

  const lecturers = lecturersData?.content || [];
  
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
    <div className="space-y-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row gap-4 items-center justify-between bg-card p-4 rounded-2xl border shadow-sm">
        <div className="relative w-full md:w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search lecturers by name..."
            className="pl-10 h-12 bg-background border-muted hover:border-primary focus-visible:ring-primary/20 transition-all rounded-xl"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="w-full md:w-72">
          <Select value={faculty} onValueChange={setFaculty}>
            <SelectTrigger className="h-12 bg-background border-muted hover:border-primary focus:ring-primary/20 rounded-xl">
              <SelectValue placeholder="All Faculties" />
            </SelectTrigger>
            <SelectContent className="rounded-xl">
              <SelectItem value="all" className="rounded-lg cursor-pointer">All Faculties</SelectItem>
              {faculties?.map(f => (
                <SelectItem key={f.code} value={f.code} className="rounded-lg cursor-pointer">{f.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array(8).fill(0).map((_, i) => (
            <Card key={i} className="overflow-hidden rounded-2xl border-border/50">
              <CardContent className="p-6">
                <div className="flex items-center space-x-4 mb-6">
                  <Skeleton className="h-14 w-14 rounded-full" />
                  <div className="space-y-2 flex-1">
                    <Skeleton className="h-5 w-3/4" />
                    <Skeleton className="h-4 w-1/2" />
                  </div>
                </div>
                <div className="space-y-3">
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-5/6" />
                  <Skeleton className="h-10 w-full mt-4 rounded-xl" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : lecturers.length === 0 ? (
        <motion.div 
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center py-20 bg-card rounded-3xl border border-dashed border-border"
        >
          <div className="mx-auto w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4">
            <Search className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-xl font-semibold text-foreground">No lecturers found</h3>
          <p className="text-muted-foreground mt-2 max-w-md mx-auto">We couldn't find any lecturers matching your search criteria. Try adjusting your filters.</p>
        </motion.div>
      ) : (
        <motion.div 
          variants={container}
          initial="hidden"
          animate="show"
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
        >
          {lecturers.filter(l => l.fullName.toLowerCase().includes(searchTerm.toLowerCase())).map((lecturer) => (
            <motion.div variants={item} key={lecturer.id}>
              <Card className="overflow-hidden h-full flex flex-col hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border-border/50 bg-card/50 backdrop-blur-sm group rounded-2xl relative">
                <div className="absolute inset-x-0 top-0 h-1 bg-gradient-to-r from-primary/40 via-primary to-primary/40 opacity-0 group-hover:opacity-100 transition-opacity" />
                <CardContent className="p-6 flex flex-col flex-1">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center space-x-4 w-full">
                      <div className="h-14 w-14 rounded-full bg-gradient-to-br from-primary/20 to-primary/5 text-primary flex items-center justify-center font-bold text-xl shadow-inner border border-primary/10">
                        {lecturer.fullName.charAt(0)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-semibold text-lg truncate group-hover:text-primary transition-colors">{lecturer.fullName}</h3>
                        <div className="flex items-center mt-1.5 bg-yellow-500/10 text-yellow-600 w-fit px-2 py-0.5 rounded-full text-xs font-semibold">
                          <Star className="h-3.5 w-3.5 mr-1 fill-yellow-500 text-yellow-500" />
                          <span>{lecturer.averageRating.toFixed(1)}</span>
                          <span className="text-yellow-600/70 ml-1 font-normal">({lecturer.reviewCount})</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="space-y-3 text-sm text-muted-foreground mt-4 mb-6 flex-1 bg-secondary/30 p-3 rounded-xl border border-border/50">
                    <div className="flex items-center">
                      <Building2 className="h-4 w-4 mr-3 text-primary/60 flex-shrink-0" />
                      <span className="line-clamp-1">{lecturer.facultyName}</span>
                    </div>
                    <div className="flex items-center">
                      <BookOpen className="h-4 w-4 mr-3 text-primary/60 flex-shrink-0" />
                      <span className="line-clamp-1">{lecturer.subjectName || 'Various subjects'}</span>
                    </div>
                  </div>
                  
                  <div className="mt-auto">
                    <Button variant="default" className="w-full rounded-xl group/btn overflow-hidden relative" asChild>
                      <Link to={`/student/lecturers/${lecturer.id}`}>
                        <span className="relative z-10 flex items-center">
                          View Profile
                          <ChevronRight className="h-4 w-4 ml-2 group-hover/btn:translate-x-1 transition-transform" />
                        </span>
                        <div className="absolute inset-0 bg-primary/20 translate-y-full group-hover/btn:translate-y-0 transition-transform duration-300 ease-out" />
                      </Link>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </motion.div>
      )}
    </div>
  );
}
