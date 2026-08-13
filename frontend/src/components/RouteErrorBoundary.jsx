import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import ErrorBoundary from './ErrorBoundary';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { AlertTriangle, RefreshCw, ArrowLeft } from 'lucide-react';

export function RouteErrorBoundary({ children }) {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <ErrorBoundary
      resetKeys={[location.pathname]}
      fallback={({ error, resetErrorBoundary }) => (
        <div className="flex items-center justify-center min-h-[50vh] p-4">
          <Card className="w-full max-w-lg border-destructive/30 shadow-xl bg-card">
            <CardHeader className="text-center">
              <div className="mx-auto w-14 h-14 rounded-full bg-destructive/10 flex items-center justify-center mb-4">
                <AlertTriangle className="h-8 w-8 text-destructive" />
              </div>
              <CardTitle className="text-2xl font-bold">Page Error</CardTitle>
              <CardDescription>
                We ran into a problem loading this page view.
              </CardDescription>
            </CardHeader>
            {import.meta.env.DEV && error && (
              <CardContent>
                <div className="bg-destructive/5 border border-destructive/20 p-3 rounded-lg text-xs font-mono text-destructive overflow-auto max-h-40">
                  {error.message || error.toString()}
                </div>
              </CardContent>
            )}
            <CardFooter className="flex justify-center gap-3">
              <Button variant="outline" onClick={resetErrorBoundary}>
                <RefreshCw className="mr-2 h-4 w-4" /> Retry
              </Button>
              <Button onClick={() => navigate(-1)}>
                <ArrowLeft className="mr-2 h-4 w-4" /> Go Back
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}
    >
      {children}
    </ErrorBoundary>
  );
}

export default RouteErrorBoundary;
