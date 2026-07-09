import { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authApi } from '@/services/api/authApi';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';

const resetSchema = z.object({
  otp: z.string().length(6, { message: "OTP must be exactly 6 characters" }),
  newPassword: z.string().min(6, { message: "Password must be at least 6 characters" }),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export default function ResetPassword() {
  const [isLoading, setIsLoading] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  
  // Get email from router state (passed from ForgotPassword page)
  const email = location.state?.email || '';

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(resetSchema),
  });

  const onSubmit = async (data) => {
    if (!email) {
      toast.error("Email not found. Please request a new reset link.");
      return;
    }

    try {
      setIsLoading(true);
      await authApi.resetPassword({
        email: email,
        otp: data.otp,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword
      });
      
      toast.success("Password reset successfully! You can now login.");
      navigate('/login');
    } catch (error) {
      toast.error(error.message || "Failed to reset password. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  if (!email) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Card className="w-full max-w-md text-center p-6">
          <CardTitle className="mb-4">Session Expired</CardTitle>
          <CardDescription className="mb-6">We couldn't find your password reset session.</CardDescription>
          <Button onClick={() => navigate('/forgot-password')}>Go back</Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-background py-10">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md p-4"
      >
        <Card className="w-full shadow-lg border-border/50">
          <CardHeader className="space-y-1 text-center">
            <CardTitle className="text-2xl font-bold tracking-tight">Reset Password</CardTitle>
            <CardDescription className="text-muted-foreground">
              Enter the OTP sent to <strong>{email}</strong> and your new password.
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit(onSubmit)}>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium leading-none" htmlFor="otp">
                  Verification Code (OTP)
                </label>
                <Input
                  id="otp"
                  placeholder="123456"
                  maxLength={6}
                  {...register('otp')}
                  className={`text-center tracking-[0.5em] font-mono text-lg ${errors.otp ? "border-destructive" : ""}`}
                />
                {errors.otp && <p className="text-sm text-destructive">{errors.otp.message}</p>}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium leading-none" htmlFor="newPassword">
                  New Password
                </label>
                <Input
                  id="newPassword"
                  type="password"
                  {...register('newPassword')}
                  className={errors.newPassword ? "border-destructive" : ""}
                />
                {errors.newPassword && <p className="text-sm text-destructive">{errors.newPassword.message}</p>}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium leading-none" htmlFor="confirmPassword">
                  Confirm Password
                </label>
                <Input
                  id="confirmPassword"
                  type="password"
                  {...register('confirmPassword')}
                  className={errors.confirmPassword ? "border-destructive" : ""}
                />
                {errors.confirmPassword && <p className="text-sm text-destructive">{errors.confirmPassword.message}</p>}
              </div>
            </CardContent>
            <CardFooter className="flex flex-col space-y-4">
              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Reset Password
              </Button>
              <div className="text-center text-sm text-muted-foreground">
                <Link to="/login" className="font-medium text-primary hover:underline">
                  Back to login
                </Link>
              </div>
            </CardFooter>
          </form>
        </Card>
      </motion.div>
    </div>
  );
}
