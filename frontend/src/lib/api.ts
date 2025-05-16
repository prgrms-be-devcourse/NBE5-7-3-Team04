import type { PerformancePageResponse } from '../types/performance';

interface GetPerformancesResponse {
  content: PerformancePageResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getPerformances = async ({ page = 0, size = 20 }: { page?: number; size?: number } = {}): Promise<GetPerformancesResponse> => {
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
    throw new Error('Failed to fetch performances');
  }

  return response.json();
}; 