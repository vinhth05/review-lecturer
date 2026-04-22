import { ButtonHTMLAttributes, ReactNode } from 'react';
import clsx from 'clsx';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  tone?: 'primary' | 'secondary' | 'ghost' | 'danger';
}

export function Button({ children, tone = 'primary', className, ...props }: ButtonProps) {
  return (
    <button
      className={clsx('btn', `btn-${tone}`, className)}
      {...props}
    >
      {children}
    </button>
  );
}
