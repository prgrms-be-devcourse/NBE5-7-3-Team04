export interface ReservationResponse {
  reservationId: number;
  title: string;
  venue: string;
  quantity: number;
  status: string;
  createdAt: string;
  expirationAt: string;
  ticketPrice: number;
  totalPrice: number;
}

export interface ReservationPageResponse {
  content: ReservationResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} 