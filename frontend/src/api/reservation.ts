import { ReservationPageResponse } from '../types/reservation';

interface GetReservationsParams {
  page?: number;
  size?: number;
}

export const getReservations = async ({ page = 0, size = 10 }: GetReservationsParams = {}) => {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/users/reservations?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch reservations: ${response.statusText}`);
    }

    return response.json() as Promise<ReservationPageResponse>;
  } catch (error) {
    console.error('Error fetching reservations:', error);
    throw error;
  }
}; 