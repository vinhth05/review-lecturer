import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Search, Shield, Lock, Unlock, CheckCircle, XCircle, Edit, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

export default function Users() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [role, setRole] = useState('ALL');
  
  // Modal states
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedRole, setSelectedRole] = useState('STUDENT');

  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-users', page, keyword, role],
    queryFn: () => adminApi.getUsers({ 
      page, 
      size: 10, 
      keyword: keyword || undefined,
      role: role === 'ALL' ? undefined : role
    }),
  });

  const toggleLockMutation = useMutation({
    mutationFn: ({ id, locked }) => locked ? adminApi.unlockUser(id) : adminApi.lockUser(id),
    onSuccess: (data) => {
      toast.success(`User ${data.locked ? 'locked' : 'unlocked'} successfully`);
      queryClient.invalidateQueries(['admin-users']);
    },
    onError: (error) => toast.error(error.message || 'Failed to update user status')
  });

  const updateRoleMutation = useMutation({
    mutationFn: ({ id, role }) => adminApi.updateUserRole(id, { role }),
    onSuccess: () => {
      toast.success('User role updated successfully');
      queryClient.invalidateQueries(['admin-users']);
      setIsEditOpen(false);
      setSelectedUser(null);
    },
    onError: (error) => toast.error(error.message || 'Failed to update user role')
  });

  const openEditModal = (user) => {
    setSelectedUser(user);
    setSelectedRole(user.role);
    setIsEditOpen(true);
  };

  const users = response?.content || [];

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-extrabold tracking-tight text-gradient">Users Management</h1>
          <p className="text-muted-foreground">Manage student and admin accounts.</p>
        </div>
      </div>

      <Card className="glass-card overflow-hidden border-t-4 border-t-blue-500/50">
        <CardHeader className="bg-card/50 pb-4 border-b">
          <div className="flex flex-col md:flex-row gap-4 items-center justify-between">
            <div className="flex items-center gap-2 w-full md:w-1/2">
              <div className="relative w-full">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search by name, email, or student code..."
                  className="pl-9 bg-background/50 border-border/50 focus-visible:ring-blue-500/50 transition-all rounded-xl"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
            </div>
            <div className="w-full md:w-1/4">
              <Select value={role} onValueChange={setRole}>
                <SelectTrigger className="bg-background/50 border-border/50 rounded-xl">
                  <SelectValue placeholder="Filter by Role" />
                </SelectTrigger>
                <SelectContent className="glass-panel">
                  <SelectItem value="ALL">All Roles</SelectItem>
                  <SelectItem value="STUDENT">Student</SelectItem>
                  <SelectItem value="ADMIN">Admin</SelectItem>
                  <SelectItem value="SUPER_ADMIN">Super Admin</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-0 sm:p-6 sm:pt-6">
          <div className="rounded-xl border bg-card/50 shadow-sm overflow-hidden border-t-0 sm:border-t">
            <Table>
              <TableHeader className="bg-muted/50">
                <TableRow className="hover:bg-transparent">
                  <TableHead className="font-semibold">User</TableHead>
                  <TableHead className="font-semibold">Role</TableHead>
                  <TableHead className="font-semibold">Status</TableHead>
                  <TableHead className="font-semibold">Faculty</TableHead>
                  <TableHead className="text-right font-semibold">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24">Loading users...</TableCell>
                  </TableRow>
                ) : users.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24 text-muted-foreground">No users found.</TableCell>
                  </TableRow>
                ) : (
                  users.map((user) => (
                    <TableRow key={user.id} className="group hover:bg-blue-500/5 transition-colors">
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium">{user.fullName}</span>
                          <span className="text-xs text-muted-foreground">{user.email}</span>
                          {user.studentCode && <span className="text-xs text-muted-foreground">ID: {user.studentCode}</span>}
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant={user.role === 'SUPER_ADMIN' ? 'destructive' : user.role === 'ADMIN' ? 'default' : 'secondary'}>
                          {user.role}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-col gap-1">
                          {user.verified ? (
                            <Badge variant="outline" className="text-green-600 w-fit"><CheckCircle className="mr-1 h-3 w-3" /> Verified</Badge>
                          ) : (
                            <Badge variant="outline" className="text-yellow-600 w-fit"><XCircle className="mr-1 h-3 w-3" /> Unverified</Badge>
                          )}
                          {user.locked && (
                            <Badge variant="destructive" className="w-fit"><Lock className="mr-1 h-3 w-3" /> Locked</Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm">{user.facultyName || 'N/A'}</span>
                      </TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          disabled={user.role === 'SUPER_ADMIN'}
                          onClick={() => openEditModal(user)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          disabled={user.role === 'SUPER_ADMIN'}
                          onClick={() => toggleLockMutation.mutate({ id: user.id, locked: user.locked })}
                        >
                          {user.locked ? (
                            <><Unlock className="mr-2 h-4 w-4" /> Unlock</>
                          ) : (
                            <><Lock className="mr-2 h-4 w-4 text-destructive" /> Lock</>
                          )}
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

      {/* Edit Role Modal */}
      <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
        <DialogContent className="sm:max-w-[425px] glass-panel border-border/50">
          <DialogHeader>
            <DialogTitle className="text-xl text-gradient">Edit User Role</DialogTitle>
          </DialogHeader>
          <div className="space-y-5 mt-4">
            <div>
              <p className="text-sm text-muted-foreground mb-4">
                Update role for <span className="font-medium text-foreground">{selectedUser?.email}</span>
              </p>
              <label className="text-sm font-medium">Role</label>
              <Select value={selectedRole} onValueChange={setSelectedRole}>
                <SelectTrigger className="mt-1">
                  <SelectValue placeholder="Select role" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="STUDENT">Student</SelectItem>
                  <SelectItem value="ADMIN">Admin</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsEditOpen(false)}>Cancel</Button>
              <Button 
                type="button" 
                onClick={() => updateRoleMutation.mutate({ id: selectedUser?.id, role: selectedRole })}
                disabled={updateRoleMutation.isPending || selectedRole === selectedUser?.role}
              >
                {updateRoleMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Save Changes
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
