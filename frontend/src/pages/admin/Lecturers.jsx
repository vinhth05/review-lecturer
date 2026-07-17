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
import { Badge } from '@/components/ui/badge';
import { Search, EyeOff, Eye, Plus, Edit, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const lecturerSchema = z.object({
  lecturerCode: z.string().min(1, 'Code is required').max(50, 'Max 50 characters'),
  fullName: z.string().min(1, 'Name is required').max(255, 'Max 255 characters'),
  facultyId: z.string().min(1, 'Faculty is required'),
  subjectId: z.string().optional().nullable(),
});

export default function Lecturers() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  
  // Modal states
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedLecturer, setSelectedLecturer] = useState(null);

  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => metadataApi.getFaculties()
  });

  const { data: subjects } = useQuery({
    queryKey: ['subjects'],
    queryFn: () => metadataApi.getSubjects()
  });

  const { register: registerAdd, handleSubmit: handleAddSubmit, setValue: setAddValue, reset: resetAdd, formState: { errors: addErrors } } = useForm({
    resolver: zodResolver(lecturerSchema)
  });

  const { register: registerEdit, handleSubmit: handleEditSubmit, setValue: setEditValue, reset: resetEdit, formState: { errors: editErrors } } = useForm({
    resolver: zodResolver(lecturerSchema)
  });

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

  const createMutation = useMutation({
    mutationFn: (data) => adminApi.createLecturer({ 
      ...data, 
      facultyId: parseInt(data.facultyId), 
      subjectId: data.subjectId && data.subjectId !== 'none' ? parseInt(data.subjectId) : null 
    }),
    onSuccess: () => {
      toast.success('Lecturer created successfully');
      queryClient.invalidateQueries(['admin-lecturers']);
      setIsAddOpen(false);
      resetAdd();
    },
    onError: (error) => toast.error(error.message || 'Failed to create lecturer')
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => adminApi.updateLecturer(id, { 
      ...data, 
      facultyId: parseInt(data.facultyId), 
      subjectId: data.subjectId && data.subjectId !== 'none' ? parseInt(data.subjectId) : null,
      status: selectedLecturer.status
    }),
    onSuccess: () => {
      toast.success('Lecturer updated successfully');
      queryClient.invalidateQueries(['admin-lecturers']);
      setIsEditOpen(false);
      setSelectedLecturer(null);
    },
    onError: (error) => toast.error(error.message || 'Failed to update lecturer')
  });

  const openEditModal = (lecturer) => {
    setSelectedLecturer(lecturer);
    const facId = faculties?.find(f => f.name === lecturer.facultyName)?.id?.toString() || '';
    const subId = subjects?.find(s => s.name === lecturer.subjectName)?.id?.toString() || 'none';
    resetEdit({ 
      lecturerCode: lecturer.lecturerCode, 
      fullName: lecturer.fullName, 
      facultyId: facId,
      subjectId: subId
    });
    setIsEditOpen(true);
  };

  const lecturers = response?.content || [];

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-extrabold tracking-tight text-gradient">Lecturers Management</h1>
          <p className="text-muted-foreground">Manage and moderate university lecturers.</p>
        </div>
        <Button className="w-full sm:w-auto shadow-md hover:shadow-lg transition-all" onClick={() => setIsAddOpen(true)}>
          <Plus className="mr-2 h-4 w-4" /> Add Lecturer
        </Button>
      </div>

      <Card className="glass-card overflow-hidden border-t-4 border-t-emerald-500/50">
        <CardHeader className="bg-card/50 pb-4 border-b">
          <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
            <div className="relative w-full md:w-1/2">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search lecturers..."
                className="pl-9 bg-background/50 border-border/50 focus-visible:ring-emerald-500/50 transition-all rounded-xl"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0 sm:p-6 sm:pt-6">
          <div className="rounded-xl border bg-card/50 shadow-sm overflow-hidden border-t-0 sm:border-t">
            <Table>
              <TableHeader className="bg-muted/50">
                <TableRow className="hover:bg-transparent">
                  <TableHead className="font-semibold">Lecturer</TableHead>
                  <TableHead className="font-semibold">Code</TableHead>
                  <TableHead className="font-semibold">Rating</TableHead>
                  <TableHead className="font-semibold">Status</TableHead>
                  <TableHead className="text-right font-semibold">Actions</TableHead>
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
                    <TableRow key={lecturer.id} className="group hover:bg-emerald-500/5 transition-colors">
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium">{lecturer.fullName}</span>
                          <span className="text-xs text-muted-foreground">{lecturer.facultyName}</span>
                          {lecturer.subjectName && <span className="text-xs text-muted-foreground/80">{lecturer.subjectName}</span>}
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
                        <Button variant="ghost" size="icon" onClick={() => openEditModal(lecturer)}>
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-destructive"
                          onClick={() => {
                            if (window.confirm(`Are you sure you want to delete lecturer ${lecturer.fullName}?`)) {
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

      {/* Add Modal */}
      <Dialog open={isAddOpen} onOpenChange={setIsAddOpen}>
        <DialogContent className="sm:max-w-[500px] glass-panel border-border/50">
          <DialogHeader>
            <DialogTitle className="text-xl text-gradient">Add New Lecturer</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleAddSubmit((data) => createMutation.mutate(data))} className="space-y-5 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Lecturer Code</label>
              <Input {...registerAdd('lecturerCode')} placeholder="e.g. GV001" className={addErrors.lecturerCode ? 'border-destructive' : ''} />
              {addErrors.lecturerCode && <p className="text-sm text-destructive">{addErrors.lecturerCode.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Full Name</label>
              <Input {...registerAdd('fullName')} placeholder="e.g. Nguyen Van A" className={addErrors.fullName ? 'border-destructive' : ''} />
              {addErrors.fullName && <p className="text-sm text-destructive">{addErrors.fullName.message}</p>}
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
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject (Optional)</label>
              <input type="hidden" {...registerAdd('subjectId')} />
              <Select onValueChange={(val) => setAddValue('subjectId', val, { shouldValidate: true })}>
                <SelectTrigger className={addErrors.subjectId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select primary subject" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">None</SelectItem>
                  {subjects?.map(s => (
                    <SelectItem key={s.id} value={s.id.toString()}>{s.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsAddOpen(false)}>Cancel</Button>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Add Lecturer
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Edit Modal */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="sm:max-w-[500px] glass-panel border-border/50">
          <DialogHeader>
            <DialogTitle className="text-xl text-gradient">Edit Lecturer</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleEditSubmit((data) => updateMutation.mutate({ id: selectedLecturer?.id, data }))} className="space-y-5 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Lecturer Code</label>
              <Input {...registerEdit('lecturerCode')} className={editErrors.lecturerCode ? 'border-destructive' : ''} />
              {editErrors.lecturerCode && <p className="text-sm text-destructive">{editErrors.lecturerCode.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Full Name</label>
              <Input {...registerEdit('fullName')} className={editErrors.fullName ? 'border-destructive' : ''} />
              {editErrors.fullName && <p className="text-sm text-destructive">{editErrors.fullName.message}</p>}
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Faculty</label>
              <input type="hidden" {...registerEdit('facultyId')} />
              <Select defaultValue={selectedLecturer ? faculties?.find(f => f.name === selectedLecturer.facultyName)?.id?.toString() : undefined} onValueChange={(val) => setEditValue('facultyId', val, { shouldValidate: true })}>
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
            <div className="space-y-2">
              <label className="text-sm font-medium">Subject (Optional)</label>
              <input type="hidden" {...registerEdit('subjectId')} />
              <Select defaultValue={selectedLecturer?.subjectName ? subjects?.find(s => s.name === selectedLecturer.subjectName)?.id?.toString() : 'none'} onValueChange={(val) => setEditValue('subjectId', val, { shouldValidate: true })}>
                <SelectTrigger className={editErrors.subjectId ? 'border-destructive' : ''}>
                  <SelectValue placeholder="Select primary subject" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">None</SelectItem>
                  {subjects?.map(s => (
                    <SelectItem key={s.id} value={s.id.toString()}>{s.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
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
