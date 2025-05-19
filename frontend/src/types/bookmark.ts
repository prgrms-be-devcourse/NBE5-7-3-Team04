export interface BookmarkedPerformance {
  id: number;
  fileUrl: string;
  title: string;
  price: number;
  startDate: string; // ISO string
  endDate: string;   // ISO string
  venue: string;
  category: string;  // PerformanceCategory
  status: string;    // PerformanceStatus
  bookmarked: boolean;
} 