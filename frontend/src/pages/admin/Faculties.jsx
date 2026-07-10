import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Search, Plus, Edit, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const facultySchema = z.object({
  code: z.string().min(1, 'Code is required').max(50, 'Max 50 characters'),
  name: z.string().min(1, 'Name is required').max(255, 'Max 255 characters'),
});

export default function Faculties() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  
  // Modal states
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedFaculty, setSelectedFaculty] = useState(null);
  
  const { register: registerAdd, handleSubmit: handleAddSubmit, reset: resetAdd, formState: { errors: addErrors } } = useForm({
    resolver: zodResolver(facultySchema)
  });

  const { register: registerEdit, handleSubmit: handleEditSubmit, reset: resetEdit, formState: { errors: editErrors } } = useForm({
    resolver: zodResolver(facultySchema)
  });

  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-faculties', page],
    queryFn: () => adminApi.getFaculties({ page, size: 10 }),
  });

  const createMutation = useMutation({
    mutationFn: (data) => adminApi.createFaculty(data),
    onSuccess: () => {
      toast.success('Faculty created successfully');
      queryClient.invalidateQueries(['admin-faculties']);
      setIsAddOpen(false);
      resetAdd();
    },
    onError: (error) => toast.error(error.message || 'Failed to create faculty')
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => adminApi.updateFaculty(id, data),
    onSuccess: () => {
      toast.success('Faculty updated successfully');
      queryClient.invalidateQueries(['admin-faculties']);
      setIsEditOpen(false);
      setSelectedFaculty(null);
    },
    onError: (error) => toast.error(error.message || 'Failed to update faculty')
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminApi.deleteFaculty(id),
    onSuccess: () => {
      toast.success('Faculty deleted successfully');
      queryClient.invalidateQueries(['admin-faculties']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete faculty')
  });

  const openEditModal = (faculty) => {
    setSelectedFaculty(faculty);
    resetEdit({ code: faculty.code, name: faculty.name });
    setIsEditOpen(true);
  };

  const faculties = response?.content || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Faculties Management</h1>
          <p className="text-muted-foreground">Manage university faculties.</p>
        </div>
        <Button className="w-full sm:w-auto" onClick={() => setIsAddOpen(true)}>
          <Plus className="mr-2 h-4 w-4" /> Add Faculty
        </Button>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Code</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={3} className="text-center h-24">Loading faculties...</TableCell>
                  </TableRow>
                ) : faculties.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} className="text-center h-24 text-muted-foreground">No faculties found.</TableCell>
                  </TableRow>
                ) : (
                  faculties.map((faculty) => (
                    <TableRow key={faculty.id}>
                      <TableCell className="font-medium">{faculty.code}</TableCell>
                      <TableCell>{faculty.name}</TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button variant="ghost" size="icon" onClick={() => openEditModal(faculty)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-destructive"
                          onClick={() => {
                            if (window.confirm(`Are you sure you want to delete faculty ${faculty.code}?`)) {
                              deleteMutation.mutate(faculty.id);
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

      {/* Add Modal */}
      <Dialog open={isAddOpen} onOpenChange={setIsAddOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New Faculty</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAddSubmit((data) => createMutation.mutate(data))} className="space-y-4 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty Code</label>
              <Input {...registerAdd('code')} placeholder="e.g. DI" className={addErrors.code ? 'border-destructive' : ''} />
              {addErrors.code && <p className="text-sm text-destructive">{addErrors.code.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty Name</label>
              <Input {...registerAdd('name')} placeholder="e.g. College of Information and Communication Technology" className={addErrors.name ? 'border-destructive' : ''} />
              {addErrors.name && <p className="text-sm text-destructive">{addErrors.name.message}</p>}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsAddOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Add Faculty
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Edit Modal */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Faculty</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleEditSubmit((data) => updateMutation.mutate({ id: selectedFaculty?.id, data }))} className="space-y-4 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty Code</label>
              <Input {...registerEdit('code')} className={editErrors.code ? 'border-destructive' : ''} />
              {editErrors.code && <p className="text-sm text-destructive">{editErrors.code.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty Name</label>
              <Input {...registerEdit('name')} className={editErrors.name ? 'border-destructive' : ''} />
              {editErrors.name && <p className="text-sm text-destructive">{editErrors.name.message}</p>}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsEditOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={updateMutation.isPending}>
                {updateMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Save Changes
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
