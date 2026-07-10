import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { adminApi } from '@/services/api/adminApi';
import { metadataApi } from '@/services/api/metadataApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Plus, Edit, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const subjectSchema = z.object({
  code: z.string().min(1, 'Code is required').max(50, 'Max 50 characters'),
  name: z.string().min(1, 'Name is required').max(255, 'Max 255 characters'),
  facultyId: z.string().min(1, 'Faculty is required')
});

export default function Subjects() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  
  // Modal states
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedSubject, setSelectedSubject] = useState(null);
  
  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => metadataApi.getFaculties()
  });

  const { register: registerAdd, handleSubmit: handleAddSubmit, setValue: setAddValue, reset: resetAdd, formState: { errors: addErrors } } = useForm({
    resolver: zodResolver(subjectSchema)
  });

  const { register: registerEdit, handleSubmit: handleEditSubmit, setValue: setEditValue, reset: resetEdit, formState: { errors: editErrors } } = useForm({
    resolver: zodResolver(subjectSchema)
  });

  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-subjects', page],
    queryFn: () => adminApi.getSubjects({ page, size: 10 }),
  });

  const createMutation = useMutation({
    mutationFn: (data) => adminApi.createSubject({ ...data, facultyId: parseInt(data.facultyId) }),
    onSuccess: () => {
      toast.success('Subject created successfully');
      queryClient.invalidateQueries(['admin-subjects']);
      setIsAddOpen(false);
      resetAdd();
    },
    onError: (error) => toast.error(error.message || 'Failed to create subject')
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => adminApi.updateSubject(id, { ...data, facultyId: parseInt(data.facultyId) }),
    onSuccess: () => {
      toast.success('Subject updated successfully');
      queryClient.invalidateQueries(['admin-subjects']);
      setIsEditOpen(false);
      setSelectedSubject(null);
    },
    onError: (error) => toast.error(error.message || 'Failed to update subject')
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminApi.deleteSubject(id),
    onSuccess: () => {
      toast.success('Subject deleted successfully');
      queryClient.invalidateQueries(['admin-subjects']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete subject')
  });

  const openEditModal = (subject) => {
    setSelectedSubject(subject);
    const facId = faculties?.find(f => f.name === subject.facultyName)?.id?.toString() || '';
    resetEdit({ code: subject.code, name: subject.name, facultyId: facId });
    setIsEditOpen(true);
  };

  const subjects = response?.content || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Subjects Management</h1>
          <p className="text-muted-foreground">Manage university subjects.</p>
        </div>
        <Button className="w-full sm:w-auto" onClick={() => setIsAddOpen(true)}>
          <Plus className="mr-2 h-4 w-4" /> Add Subject
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
                  <TableHead>Faculty</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center h-24">Loading subjects...</TableCell>
                  </TableRow>
                ) : subjects.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center h-24 text-muted-foreground">No subjects found.</TableCell>
                  </TableRow>
                ) : (
                  subjects.map((subject) => (
                    <TableRow key={subject.id}>
                      <TableCell className="font-medium">{subject.code}</TableCell>
                      <TableCell>{subject.name}</TableCell>
                      <TableCell>{subject.facultyName}</TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button variant="ghost" size="icon" onClick={() => openEditModal(subject)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-destructive"
                          onClick={() => {
                            if (window.confirm(`Are you sure you want to delete subject ${subject.code}?`)) {
                              deleteMutation.mutate(subject.id);
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
            <DialogTitle>Add New Subject</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAddSubmit((data) => createMutation.mutate(data))} className="space-y-4 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject Code</label>
              <Input {...registerAdd('code')} placeholder="e.g. CT173" className={addErrors.code ? 'border-destructive' : ''} />
              {addErrors.code && <p className="text-sm text-destructive">{addErrors.code.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject Name</label>
              <Input {...registerAdd('name')} placeholder="e.g. Architecture of Computers" className={addErrors.name ? 'border-destructive' : ''} />
              {addErrors.name && <p className="text-sm text-destructive">{addErrors.name.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty</label>
              <input type="hidden" {...registerAdd('facultyId')} />
              <Select onValueChange={(val) => setAddValue('facultyId', val, { shouldValidate: true })}>
                <SelectTrigger className={addErrors.facultyId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select faculty" />
                </SelectTrigger>
                <SelectContent>
                  {faculties?.map(f => (
                    <SelectItem key={f.id} value={f.id.toString()}>{f.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {addErrors.facultyId && <p className="text-sm text-destructive">{addErrors.facultyId.message}</p>}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsAddOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Add Subject
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Edit Modal */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Subject</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleEditSubmit((data) => updateMutation.mutate({ id: selectedSubject?.id, data }))} className="space-y-4 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject Code</label>
              <Input {...registerEdit('code')} className={editErrors.code ? 'border-destructive' : ''} />
              {editErrors.code && <p className="text-sm text-destructive">{editErrors.code.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject Name</label>
              <Input {...registerEdit('name')} className={editErrors.name ? 'border-destructive' : ''} />
              {editErrors.name && <p className="text-sm text-destructive">{editErrors.name.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty</label>
              <input type="hidden" {...registerEdit('facultyId')} />
              <Select defaultValue={selectedSubject ? faculties?.find(f => f.name === selectedSubject.facultyName)?.id?.toString() : undefined} onValueChange={(val) => setEditValue('facultyId', val, { shouldValidate: true })}>
                <SelectTrigger className={editErrors.facultyId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select faculty" />
                </SelectTrigger>
                <SelectContent>
                  {faculties?.map(f => (
                    <SelectItem key={f.id} value={f.id.toString()}>{f.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {editErrors.facultyId && <p className="text-sm text-destructive">{editErrors.facultyId.message}</p>}
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
