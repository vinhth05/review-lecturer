import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/services/api/adminApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Trash2, AlertTriangle, MessageSquareOff } from 'lucide-react';
import { toast } from 'sonner';

export default function Reports() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  
  const { data: response, isLoading } = useQuery({
    queryKey: ['admin-reports', page],
    queryFn: () => adminApi.getReports({ page, size: 10 }),
  });

  const deleteReportMutation = useMutation({
    mutationFn: (id) => adminApi.deleteReport(id),
    onSuccess: () => {
      toast.success('Report resolved and deleted');
      queryClient.invalidateQueries(['admin-reports']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete report')
  });

  const deleteReviewMutation = useMutation({
    mutationFn: (id) => adminApi.deleteReview(id),
    onSuccess: () => {
      toast.success('Review deleted successfully');
      queryClient.invalidateQueries(['admin-reports']);
    },
    onError: (error) => toast.error(error.message || 'Failed to delete review')
  });

  const reports = response?.content || [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Reports Management</h1>
          <p className="text-muted-foreground">Manage user reports on reviews.</p>
        </div>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Reporter</TableHead>
                  <TableHead>Reason</TableHead>
                  <TableHead>Reported Review</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24">Loading reports...</TableCell>
                  </TableRow>
                ) : reports.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center h-24 flex-col gap-2">
                      <div className="flex flex-col items-center justify-center text-muted-foreground">
                        <AlertTriangle className="h-8 w-8 mb-2 opacity-50" />
                        No reports found.
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  reports.map((report) => (
                    <TableRow key={report.id}>
                      <TableCell className="font-medium">
                        {report.reporterName}
                        <div className="text-xs text-muted-foreground">{report.reporterEmail}</div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="text-orange-600 bg-orange-50 border-orange-200">
                          {report.reason}
                        </Badge>
                      </TableCell>
                      <TableCell className="max-w-[300px]">
                        <div className="truncate text-sm text-muted-foreground bg-muted p-2 rounded">
                          "{report.reviewComment}"
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="secondary">Pending</Badge>
                      </TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="text-destructive border-destructive hover:bg-destructive/10"
                          onClick={() => {
                            if (window.confirm('Delete the reported review? This will also remove the report.')) {
                              deleteReviewMutation.mutate(report.reviewId);
                            }
                          }}
                        >
                          <MessageSquareOff className="h-4 w-4 mr-2" /> Delete Review
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="text-muted-foreground"
                          onClick={() => {
                            if (window.confirm('Dismiss this report?')) {
                              deleteReportMutation.mutate(report.id);
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
    </div>
  );
}
