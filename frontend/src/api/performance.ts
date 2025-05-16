import { PerformancePageResponse } from '../types/performance';

interface GetPerformancesParams {
  page?: number;
  size?: number;
}

interface GetPerformancesResponse {
  content: PerformancePageResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getPerformances = async ({ page = 0, size = 20 }: GetPerformancesParams = {}) => {
  try {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/api/v1/users/performances?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch performances: ${response.statusText}`);
    }

    return response.json() as Promise<GetPerformancesResponse>;
  } catch (error) {
    console.error('Error fetching performances:', error);
    throw error;
  }
};