import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Search, EyeOff, Eye, Plus, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

export default function Lecturers() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  
  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-lecturers', page, keyword],
    queryFn: () => adminApi.getLecturers({ 
      page, 
      size: 10,
      keyword: keyword || undefined
    }),
  });

  const toggleHideMutation = useMutation({
    mutationFn: ({ id, hidden }) => hidden ? adminApi.unhideLecturer(id) : adminApi.hideLecturer(id),
    onSuccess: (data) => {
      toast.success(`Lecturer ${data.hidden ? 'hidden' : 'unhidden'} successfully`);
      queryClient.invalidateQueries(['admin-lecturers']);
    },
    onError: (error) => toast.error(error.message || 'Failed to update lecturer status')
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminApi.deleteLecturer(id),
    onSuccess: () => {
      toast.success('Lecturer deleted successfully');
      queryClient.invalidateQueries(['admin-lecturers']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete lecturer')
  });

  const lecturers = response?.content || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Lecturers Management</h1>
          <p className="text-muted-foreground">Manage and moderate university lecturers.</p>
        </div>
        <Button className="w-full sm:w-auto">
          <Plus className="mr-2 h-4 w-4" /> Add Lecturer
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
            <div className="relative w-full md:w-1/2">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search lecturers..."
                className="pl-8"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Lecturer</TableHead>
                  <TableHead>Code</TableHead>
                  <TableHead>Rating</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24">Loading lecturers...</TableCell>
                  </TableRow>
                ) : lecturers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24 text-muted-foreground">No lecturers found.</TableCell>
                  </TableRow>
                ) : (
                  lecturers.map((lecturer) => (
                    <TableRow key={lecturer.id}>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium">{lecturer.fullName}</span>
                          <span className="text-xs text-muted-foreground">{lecturer.facultyName}</span>
                        </div>
                      </TableCell>
                      <TableCell>{lecturer.lecturerCode}</TableCell>
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium flex items-center gap-1">
                            <span className="text-yellow-500">★</span>
                            {lecturer.averageRating?.toFixed(1) || '0.0'}
                          </span>
                          <span className="text-xs text-muted-foreground">{lecturer.reviewCount || 0} reviews</span>
                        </div>
                      </TableCell>
                      <TableCell>
                        {lecturer.status === 'HIDDEN' ? (
                          <Badge variant="destructive">Hidden</Badge>
                        ) : (
                          <Badge variant="outline" className="text-green-600">Active</Badge>
                        )}
                      </TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => toggleHideMutation.mutate({ id: lecturer.id, hidden: lecturer.status === 'ACTIVE' })}
                        >
                          {lecturer.status === 'HIDDEN' ? <Eye className="h-4 w-4" /> : <EyeOff className="h-4 w-4" />}
                        </Button>
                        <Button variant="ghost" size="icon">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-destructive"
                          onClick={() => {
                            if (window.confirm('Are you sure you want to delete this lecturer?')) {
                              deleteMutation.mutate(lecturer.id);
                            }
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
          
          <div className="flex items-center justify-between mt-4">
            <div className="text-sm text-muted-foreground">
              Showing page {page + 1} of {response?.totalPages || 1}
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p + 1)}
                disabled={page >= (response?.totalPages || 1) - 1}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
