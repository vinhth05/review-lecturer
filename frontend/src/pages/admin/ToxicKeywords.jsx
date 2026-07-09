import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Edit, Trash2, FileWarning } from 'lucide-react';
import { toast } from 'sonner';

export default function ToxicKeywords() {
  const queryClient = useQueryClient();
  const [newKeyword, setNewKeyword] = useState('');
  
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

  const list = keywords || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Toxic Keywords</h1>
          <p className="text-muted-foreground">Manage words that trigger automatic review rejection.</p>
        </div>
      </div>

      <Card>
        <CardContent className="pt-6">
          <form onSubmit={handleAdd} className="flex gap-2 mb-6 max-w-md">
            <Input 
              placeholder="Enter new toxic keyword..." 
              value={newKeyword}
              onChange={(e) => setNewKeyword(e.target.value)}
              disabled={addMutation.isPending}
            />
            <Button type="submit" disabled={addMutation.isPending || !newKeyword.trim()}>
              <Plus className="h-4 w-4 mr-2" /> Add
            </Button>
          </form>

          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Keyword</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
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
                    <TableRow key={item.id}>
                      <TableCell className="font-medium text-destructive">{item.keyword}</TableCell>
                      <TableCell className="text-right space-x-2">
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
    </div>
  );
}
