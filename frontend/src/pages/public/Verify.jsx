import { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authApi } from '@/services/api/authApi';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';

const verifySchema = z.object({
  otp: z.string().length(6, { message: "OTP must be exactly 6 characters" }),
});

export default function Verify() {
  const [isLoading, setIsLoading] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  
  // Get email from router state (passed from Register page)
  const email = location.state?.email || '';

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(verifySchema),
  });

  const onSubmit = async (data) => {
    if (!email) {
      toast.error("Email not found. Please try registering again.");
      return;
    }

    try {
      setIsLoading(true);
      const response = await authApi.verify({
        email: email,
        otp: data.otp
      });
      
      // Auto login after verification
      login(response);
      toast.success("Account verified successfully!");
      
      // Redirect based on role
      if (response.role === 'ADMIN' || response.role === 'SUPER_ADMIN') {
        navigate('/admin');
      } else {
        navigate('/student');
      }
    } catch (error) {
      toast.error(error.message || "Invalid or expired OTP. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  if (!email) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <Card className="w-full max-w-md text-center p-6">
          <CardTitle className="mb-4">Session Expired</CardTitle>
          <CardDescription className="mb-6">We couldn't find your registration session.</CardDescription>
          <Button onClick={() => navigate('/register')}>Go back to Register</Button>
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
            <CardTitle className="text-2xl font-bold tracking-tight">Verify Email</CardTitle>
            <CardDescription className="text-muted-foreground">
              We sent a 6-digit verification code to <strong>{email}</strong>
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
            </CardContent>
            <CardFooter className="flex flex-col space-y-4">
              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Verify Account
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
