import { ReservationPageResponse } from '../types/reservation';
import { api } from "./api";

interface GetReservationsParams {
  page?: number;
  size?: number;
}

export const getReservations = async ({ page = 0, size = 10 }: GetReservationsParams = {}) => {
  try {
    const response = await api.get(`/reservations/me?page=${page}&size=${size}`);
    return response.data as ReservationPageResponse;
  } catch (error) {
    console.error('Error fetching reservations:', error);
    throw error;
  }
}; 