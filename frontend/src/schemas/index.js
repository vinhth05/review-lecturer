import * as z from 'zod';

export const loginSchema = z.object({
  email: z.string().trim().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const registerSchema = z.object({
  fullName: z.string().trim().min(2, 'Name must be at least 2 characters'),
  studentCode: z.string().trim().min(5, 'Invalid student ID'),
  email: z.string().trim().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
  facultyId: z.string().min(1, 'Please select a faculty'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export const forgotPasswordSchema = z.object({
  email: z.string().trim().email('Invalid email address'),
});

export const resetPasswordSchema = z.object({
  otp: z.string().trim().length(6, 'OTP must be exactly 6 characters'),
  newPassword: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export const verifySchema = z.object({
  otp: z.string().trim().length(6, 'OTP must be exactly 6 characters'),
});

export const profileSchema = z.object({
  fullName: z.string().trim().min(2, 'Name must be at least 2 characters'),
  facultyId: z.string().min(1, 'Please select a faculty'),
});

export const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

export const reviewSchema = z.object({
  ratingClarity: z.number().min(1, 'Please provide a rating').max(5),
  ratingFairness: z.number().min(1, 'Please provide a rating').max(5),
  ratingPressure: z.number().min(1, 'Please provide a rating').max(5),
  ratingWorkload: z.number().min(1, 'Please provide a rating').max(5),
  ratingSupport: z.number().min(1, 'Please provide a rating').max(5),
  comment: z.string().trim().min(10, 'Review must be at least 10 characters').max(1000, 'Review is too long'),
  semester: z.string().min(1, 'Please select a semester'),
  academicYear: z.string().min(1, 'Please select an academic year'),
});

export const facultySchema = z.object({
  code: z.string().trim().min(1, 'Code is required').max(50, 'Max 50 characters'),
  name: z.string().trim().min(1, 'Name is required').max(255, 'Max 255 characters'),
});

export const lecturerSchema = z.object({
  lecturerCode: z.string().trim().min(1, 'Code is required').max(50, 'Max 50 characters'),
  fullName: z.string().trim().min(1, 'Name is required').max(255, 'Max 255 characters'),
  facultyId: z.string().min(1, 'Faculty is required'),
  subjectId: z.string().optional().nullable(),
});

export const subjectSchema = z.object({
  code: z.string().trim().min(1, 'Code is required').max(50, 'Max 50 characters'),
  name: z.string().trim().min(1, 'Name is required').max(255, 'Max 255 characters'),
  facultyId: z.string().min(1, 'Faculty is required'),
});

export const toxicKeywordSchema = z.object({
  keyword: z.string().trim().min(1, 'Keyword is required').max(100, 'Max 100 characters'),
});
