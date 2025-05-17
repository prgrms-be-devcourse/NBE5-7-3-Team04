export type PerformanceCategory = 'OPERA' | 'DANCING' | 'SINGING';

export interface PerformancePageResponse {
  id: number;
  fileUrl: string | null;
  title: string;
  price: number;
  startDate: string;
  endDate: string;
  venue: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  category: PerformanceCategory;
} 