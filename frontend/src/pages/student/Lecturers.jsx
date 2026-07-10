import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { lecturerApi } from '@/services/api/lecturerApi';
import { metadataApi } from '@/services/api/metadataApi';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Search, Star, Building2, BookOpen } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';

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

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search lecturers by name..."
            className="pl-8"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="w-full sm:w-64">
          <Select value={faculty} onValueChange={setFaculty}>
            <SelectTrigger>
              <SelectValue placeholder="All Faculties" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Faculties</SelectItem>
              {faculties?.map(f => (
                <SelectItem key={f.code} value={f.code}>{f.name}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array(8).fill(0).map((_, i) => (
            <Card key={i} className="overflow-hidden">
              <CardContent className="p-6">
                <div className="flex items-center space-x-4 mb-4">
                  <Skeleton className="h-12 w-12 rounded-full" />
                  <div className="space-y-2">
                    <Skeleton className="h-4 w-32" />
                    <Skeleton className="h-3 w-24" />
                  </div>
                </div>
                <div className="space-y-2">
                  <Skeleton className="h-3 w-full" />
                  <Skeleton className="h-3 w-4/5" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : lecturers.length === 0 ? (
        <div className="text-center py-12">
          <h3 className="text-lg font-medium text-muted-foreground">No lecturers found</h3>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {lecturers.filter(l => l.fullName.toLowerCase().includes(searchTerm.toLowerCase())).map((lecturer) => (
            <Card key={lecturer.id} className="overflow-hidden hover:shadow-lg transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center space-x-4">
                    <div className="h-12 w-12 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-lg">
                      {lecturer.fullName.charAt(0)}
                    </div>
                    <div>
                      <h3 className="font-semibold text-lg line-clamp-1">{lecturer.fullName}</h3>
                      <div className="flex items-center text-sm text-muted-foreground mt-1">
                        <Star className="h-4 w-4 text-yellow-500 mr-1 fill-yellow-500" />
                        <span className="font-medium text-foreground mr-1">{lecturer.averageRating.toFixed(1)}</span>
                        <span>({lecturer.reviewCount})</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="space-y-2 text-sm text-muted-foreground">
                  <div className="flex items-center">
                    <Building2 className="h-4 w-4 mr-2 flex-shrink-0" />
                    <span className="line-clamp-1">{lecturer.facultyName}</span>
                  </div>
                  <div className="flex items-center">
                    <BookOpen className="h-4 w-4 mr-2 flex-shrink-0" />
                    <span className="line-clamp-1">{lecturer.subjectName || 'Various subjects'}</span>
                  </div>
                </div>
                <div className="mt-6">
                  <Button variant="outline" className="w-full" asChild>
                    <Link to={`/student/lecturers/${lecturer.id}`}>View Profile</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
