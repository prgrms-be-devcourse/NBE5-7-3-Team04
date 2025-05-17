export interface ReservationResponse {
  id: number;
  performanceId: number;
  performanceTitle: string;
  performanceDate: string;
  performanceTime: string;
  performanceVenue: string;
  performanceImageUrl: string | null;
  seatNumber: string;
  price: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}

export interface ReservationPageResponse {
  content: ReservationResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} 