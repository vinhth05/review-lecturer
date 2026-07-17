import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Plus, Edit, Trash2, FileWarning, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const keywordSchema = z.object({
  keyword: z.string().min(1, 'Keyword is required').max(100, 'Max 100 characters'),
});

export default function ToxicKeywords() {
  const queryClient = useQueryClient();
  const [newKeyword, setNewKeyword] = useState('');
  
  // Edit modal states
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedKeyword, setSelectedKeyword] = useState(null);

  const { register: registerEdit, handleSubmit: handleEditSubmit, reset: resetEdit, formState: { errors: editErrors } } = useForm({
    resolver: zodResolver(keywordSchema)
  });

  const { data: keywords, isLoading } = useQuery({
    queryKey: ['admin-toxic-keywords'],
    queryFn: () => adminApi.getToxicKeywords(),
  });

  const addMutation = useMutation({
    mutationFn: (data) => adminApi.addToxicKeyword(data),
    onSuccess: () => {
      toast.success('Toxic keyword added');
      setNewKeyword('');
      queryClient.invalidateQueries(['admin-toxic-keywords']);
    },
    onError: (error) => toast.error(error.message || 'Failed to add keyword')
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => adminApi.updateToxicKeyword(id, data),
    onSuccess: () => {
      toast.success('Toxic keyword updated');
      queryClient.invalidateQueries(['admin-toxic-keywords']);
      setIsEditOpen(false);
      setSelectedKeyword(null);
    },
    onError: (error) => toast.error(error.message || 'Failed to update keyword')
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => adminApi.deleteToxicKeyword(id),
    onSuccess: () => {
      toast.success('Toxic keyword deleted');
      queryClient.invalidateQueries(['admin-toxic-keywords']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete keyword')
  });

  const handleAdd = (e) => {
    e.preventDefault();
    if (!newKeyword.trim()) return;
    addMutation.mutate({ keyword: newKeyword.trim() });
  };

  const openEditModal = (item) => {
    setSelectedKeyword(item);
    resetEdit({ keyword: item.keyword });
    setIsEditOpen(true);
  };

  const list = keywords || [];

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-extrabold tracking-tight text-gradient">Toxic Keywords</h1>
          <p className="text-muted-foreground">Manage words that trigger automatic review rejection.</p>
        </div>
      </div>

      <Card className="glass-card overflow-hidden border-t-4 border-t-rose-600/50">
        <CardContent className="pt-6 p-0 sm:p-6">
          <form onSubmit={handleAdd} className="flex gap-2 mb-6 max-w-md p-4 sm:p-0">
            <Input 
              placeholder="Enter new toxic keyword..." 
              value={newKeyword}
              onChange={(e) => setNewKeyword(e.target.value)}
              disabled={addMutation.isPending}
              className="bg-background/50 border-border/50 focus-visible:ring-rose-500/50 transition-all rounded-xl"
            />
            <Button type="submit" disabled={addMutation.isPending || !newKeyword.trim()} className="shadow-md hover:shadow-lg transition-all rounded-xl">
              {addMutation.isPending ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Plus className="h-4 w-4 mr-2" />} Add
            </Button>
          </form>

          <div className="rounded-xl border bg-card/50 shadow-sm overflow-hidden border-t-0 sm:border-t">
            <Table>
              <TableHeader className="bg-muted/50">
                <TableRow className="hover:bg-transparent">
                  <TableHead className="font-semibold">Keyword</TableHead>
                  <TableHead className="text-right font-semibold">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={2} className="text-center h-24">Loading keywords...</TableCell>
                  </TableRow>
                ) : list.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={2} className="text-center h-24 text-muted-foreground flex-col gap-2">
                      <div className="flex flex-col items-center justify-center">
                        <FileWarning className="h-8 w-8 mb-2 opacity-50" />
                        No toxic keywords configured.
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  list.map((item) => (
                    <TableRow key={item.id} className="group hover:bg-rose-500/5 transition-colors">
                      <TableCell className="font-medium text-destructive">{item.keyword}</TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-muted-foreground hover:text-foreground"
                          onClick={() => openEditModal(item)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-muted-foreground hover:text-destructive"
                          onClick={() => {
                            if (window.confirm('Delete this toxic keyword?')) {
                              deleteMutation.mutate(item.id);
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
        </CardContent>
      </Card>

      {/* Edit Modal */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="sm:max-w-[425px] glass-panel border-border/50">
          <DialogHeader>
            <DialogTitle className="text-xl text-gradient">Edit Toxic Keyword</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleEditSubmit((data) => updateMutation.mutate({ id: selectedKeyword?.id, data }))} className="space-y-5 mt-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Keyword</label>
              <Input {...registerEdit('keyword')} className={editErrors.keyword ? 'border-destructive' : ''} />
              {editErrors.keyword && <p className="text-sm text-destructive">{editErrors.keyword.message}</p>}
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
