import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { studentApi } from '@/services/api/studentApi';
import { metadataApi } from '@/services/api/metadataApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { Loader2, User } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';

const profileSchema = z.object({
  fullName: z.string().min(2, 'Name must be at least 2 characters'),
  facultyId: z.string().min(1, 'Please select a faculty'),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string()
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export default function Profile() {
  const queryClient = useQueryClient();
  const { user, login } = useAuth();
  
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  const { data: faculties } = useQuery({
    queryKey: ['faculties'],
    queryFn: () => metadataApi.getFaculties().then(res => res)
  });

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: () => studentApi.getProfile()
  });

  const { register, handleSubmit, setValue, formState: { errors } } = useForm({
    resolver: zodResolver(profileSchema),
    values: {
      fullName: profile?.fullName || '',
      facultyId: profile ? faculties?.find(f => f.name === profile.facultyName)?.id?.toString() || '' : '',
    }
  });

  const { register: registerPassword, handleSubmit: handlePasswordSubmit, reset: resetPasswordForm, formState: { errors: passwordErrors } } = useForm({
    resolver: zodResolver(passwordSchema)
  });

  const onUpdateProfile = async (data) => {
    try {
      setIsUpdatingProfile(true);
      const res = await studentApi.updateProfile({
        fullName: data.fullName,
        facultyId: parseInt(data.facultyId)
      });
      // Update local storage auth user
      login(res, localStorage.getItem('access_token'));
      queryClient.invalidateQueries(['profile']);
      toast.success("Profile updated successfully");
    } catch (error) {
      toast.error(error.message || "Failed to update profile");
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const onChangePassword = async (data) => {
    try {
      setIsChangingPassword(true);
      await studentApi.changePassword(data);
      toast.success("Password changed successfully");
      resetPasswordForm();
    } catch (error) {
      toast.error(error.message || "Failed to change password");
    } finally {
      setIsChangingPassword(false);
    }
  };

  if (isLoading) return <div className="flex justify-center p-10"><Loader2 className="animate-spin h-8 w-8" /></div>;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">Account Settings</h2>
        <p className="text-muted-foreground">Manage your profile and security preferences.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-1">
          <Card>
            <CardContent className="p-6 flex flex-col items-center text-center">
              <div className="h-24 w-24 rounded-full bg-primary/10 text-primary flex items-center justify-center font-bold text-3xl mb-4">
                {profile?.fullName?.charAt(0) || <User size={40} />}
              </div>
              <h3 className="font-semibold text-lg">{profile?.fullName}</h3>
              <p className="text-sm text-muted-foreground mb-1">{profile?.studentCode}</p>
              <p className="text-sm text-muted-foreground">{profile?.email}</p>
              
              <div className="mt-6 w-full flex items-center justify-between text-sm border-t pt-4">
                <span className="text-muted-foreground">Account Status</span>
                <span className={`font-medium ${profile?.verified ? 'text-green-500' : 'text-orange-500'}`}>
                  {profile?.verified ? 'Verified' : 'Pending'}
                </span>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="md:col-span-2 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Personal Information</CardTitle>
              <CardDescription>Update your basic profile details.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onUpdateProfile)} className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Full Name</label>
                  <Input {...register('fullName')} className={errors.fullName ? "border-destructive" : ""} />
                  {errors.fullName && <p className="text-sm text-destructive">{errors.fullName.message}</p>}
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Faculty</label>
                  <Select onValueChange={(value) => setValue('facultyId', value, { shouldValidate: true })} value={profile ? faculties?.find(f => f.name === profile.facultyName)?.id?.toString() : undefined}>
                    <SelectTrigger className={errors.facultyId ? "border-destructive" : ""}>
                      <SelectValue placeholder="Select faculty" />
                    </SelectTrigger>
                    <SelectContent>
                      {faculties?.map(f => (
                        <SelectItem key={f.id} value={f.id.toString()}>{f.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.facultyId && <p className="text-sm text-destructive">{errors.facultyId.message}</p>}
                </div>
                <Button type="submit" disabled={isUpdatingProfile}>
                  {isUpdatingProfile && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Save Changes
                </Button>
              </form>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Change Password</CardTitle>
              <CardDescription>Update your account security password.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handlePasswordSubmit(onChangePassword)} className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Current Password</label>
                  <Input type="password" {...registerPassword('currentPassword')} className={passwordErrors.currentPassword ? "border-destructive" : ""} />
                  {passwordErrors.currentPassword && <p className="text-sm text-destructive">{passwordErrors.currentPassword.message}</p>}
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">New Password</label>
                    <Input type="password" {...registerPassword('newPassword')} className={passwordErrors.newPassword ? "border-destructive" : ""} />
                    {passwordErrors.newPassword && <p className="text-sm text-destructive">{passwordErrors.newPassword.message}</p>}
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Confirm Password</label>
                    <Input type="password" {...registerPassword('confirmPassword')} className={passwordErrors.confirmPassword ? "border-destructive" : ""} />
                    {passwordErrors.confirmPassword && <p className="text-sm text-destructive">{passwordErrors.confirmPassword.message}</p>}
                  </div>
                </div>
                <Button type="submit" variant="secondary" disabled={isChangingPassword}>
                  {isChangingPassword && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Update Password
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
