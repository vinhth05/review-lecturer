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
import { Loader2, User, Camera, ShieldCheck, Clock, CheckCircle2 } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { motion } from 'framer-motion';

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
  const { user, updateUser } = useAuth();
  
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
      // Update local storage auth user without erasing tokens
      updateUser({ fullName: res.fullName, facultyName: res.facultyName });
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

  if (isLoading) return (
    <div className="flex flex-col justify-center items-center h-64 space-y-4">
      <Loader2 className="animate-spin h-10 w-10 text-primary" />
      <p className="text-muted-foreground font-medium animate-pulse">Loading profile...</p>
    </div>
  );

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-5xl mx-auto space-y-8 pb-12"
    >
      <div className="flex flex-col gap-2">
        <h2 className="text-3xl font-extrabold tracking-tight">Account Settings</h2>
        <p className="text-muted-foreground text-lg">Manage your profile information and security preferences.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 space-y-6">
          <Card className="rounded-2xl border-border/50 shadow-sm overflow-hidden relative">
            <div className="h-24 bg-gradient-to-r from-primary/80 to-primary/40"></div>
            <CardContent className="p-6 flex flex-col items-center text-center -mt-12 relative z-10">
              <div className="relative group cursor-pointer">
                <div className="h-28 w-28 rounded-full bg-card flex items-center justify-center border-4 border-background shadow-lg overflow-hidden relative">
                  <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-primary/5"></div>
                  <span className="font-extrabold text-4xl text-primary relative z-10">
                    {profile?.fullName?.charAt(0) || <User size={48} />}
                  </span>
                  <div className="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-20">
                    <Camera className="text-white h-8 w-8" />
                  </div>
                </div>
              </div>
              <h3 className="font-bold text-xl mt-4 text-foreground">{profile?.fullName}</h3>
              <p className="text-primary font-medium mt-1">{profile?.studentCode}</p>
              <p className="text-sm text-muted-foreground mt-1">{profile?.email}</p>
              
              <div className="mt-8 w-full space-y-3">
                <div className="flex items-center justify-between text-sm p-3 bg-secondary/30 rounded-xl border border-border/50">
                  <span className="text-muted-foreground flex items-center gap-2"><ShieldCheck className="h-4 w-4" /> Status</span>
                  <span className={`font-semibold flex items-center gap-1.5 ${profile?.verified ? 'text-green-500' : 'text-orange-500'}`}>
                    {profile?.verified ? <><CheckCircle2 className="h-4 w-4" /> Verified</> : <><Clock className="h-4 w-4" /> Pending</>}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="lg:col-span-2 space-y-8">
          <Card className="rounded-2xl border-border/50 shadow-sm">
            <CardHeader className="bg-secondary/10 border-b border-border/50 pb-6">
              <CardTitle className="text-xl">Personal Information</CardTitle>
              <CardDescription className="text-sm">Update your basic profile details.</CardDescription>
            </CardHeader>
            <CardContent className="pt-6">
              <form onSubmit={handleSubmit(onUpdateProfile)} className="space-y-6">
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-foreground">Full Name</label>
                  <Input 
                    {...register('fullName')} 
                    className={`h-12 rounded-xl transition-all ${errors.fullName ? "border-destructive focus-visible:ring-destructive/20" : "hover:border-primary focus-visible:ring-primary/20"}`} 
                  />
                  {errors.fullName && <p className="text-sm text-destructive font-medium">{errors.fullName.message}</p>}
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-foreground">Faculty</label>
                  <Select onValueChange={(value) => setValue('facultyId', value, { shouldValidate: true })} value={profile ? faculties?.find(f => f.name === profile.facultyName)?.id?.toString() : undefined}>
                    <SelectTrigger className={`h-12 rounded-xl transition-all ${errors.facultyId ? "border-destructive focus:ring-destructive/20" : "hover:border-primary focus:ring-primary/20"}`}>
                      <SelectValue placeholder="Select faculty" />
                    </SelectTrigger>
                    <SelectContent className="rounded-xl">
                      {faculties?.map(f => (
                        <SelectItem key={f.id} value={f.id.toString()}>{f.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.facultyId && <p className="text-sm text-destructive font-medium">{errors.facultyId.message}</p>}
                </div>
                <div className="pt-2 flex justify-end">
                  <Button type="submit" disabled={isUpdatingProfile} className="rounded-xl h-12 px-8 font-semibold shadow-lg shadow-primary/20 hover:shadow-primary/30 transition-all">
                    {isUpdatingProfile ? (
                      <><Loader2 className="mr-2 h-5 w-5 animate-spin" /> Saving Changes...</>
                    ) : (
                      'Save Changes'
                    )}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>

          <Card className="rounded-2xl border-border/50 shadow-sm">
            <CardHeader className="bg-secondary/10 border-b border-border/50 pb-6">
              <CardTitle className="text-xl">Change Password</CardTitle>
              <CardDescription className="text-sm">Update your account security password.</CardDescription>
            </CardHeader>
            <CardContent className="pt-6">
              <form onSubmit={handlePasswordSubmit(onChangePassword)} className="space-y-6">
                <div className="space-y-2">
                  <label className="text-sm font-semibold text-foreground">Current Password</label>
                  <Input 
                    type="password" 
                    {...registerPassword('currentPassword')} 
                    className={`h-12 rounded-xl transition-all ${passwordErrors.currentPassword ? "border-destructive focus-visible:ring-destructive/20" : "hover:border-primary focus-visible:ring-primary/20"}`} 
                  />
                  {passwordErrors.currentPassword && <p className="text-sm text-destructive font-medium">{passwordErrors.currentPassword.message}</p>}
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-sm font-semibold text-foreground">New Password</label>
                    <Input 
                      type="password" 
                      {...registerPassword('newPassword')} 
                      className={`h-12 rounded-xl transition-all ${passwordErrors.newPassword ? "border-destructive focus-visible:ring-destructive/20" : "hover:border-primary focus-visible:ring-primary/20"}`} 
                    />
                    {passwordErrors.newPassword && <p className="text-sm text-destructive font-medium">{passwordErrors.newPassword.message}</p>}
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-semibold text-foreground">Confirm Password</label>
                    <Input 
                      type="password" 
                      {...registerPassword('confirmPassword')} 
                      className={`h-12 rounded-xl transition-all ${passwordErrors.confirmPassword ? "border-destructive focus-visible:ring-destructive/20" : "hover:border-primary focus-visible:ring-primary/20"}`} 
                    />
                    {passwordErrors.confirmPassword && <p className="text-sm text-destructive font-medium">{passwordErrors.confirmPassword.message}</p>}
                  </div>
                </div>

                <div className="pt-2 flex justify-end">
                  <Button type="submit" variant="secondary" disabled={isChangingPassword} className="rounded-xl h-12 px-8 font-semibold border border-border/50 hover:bg-secondary/80 transition-all">
                    {isChangingPassword ? (
                      <><Loader2 className="mr-2 h-5 w-5 animate-spin" /> Updating...</>
                    ) : (
                      'Update Password'
                    )}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </motion.div>
  );
}
