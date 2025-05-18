import { api } from "./api";

export interface ManagerPerformance {
  id: number;
  fileUrl: string;
  title: string;
  startDate: string;
  endDate: string;
  venue: string;
  status: string;
  category: string;
}

export interface ManagerPerformancePageResponse {
  totalElements: number;
  totalPages: number;
  size: number;
  content: ManagerPerformance[];
  number: number;
}

// 공연 상세 타입
export interface ManagerPerformanceDetail {
  id: number;
  fileUrl: string;
  title: string;
  venue: string;
  status: string;
  totalSeats: number;
  startDate: string;
  endDate: string;
  schedules: {
    id: number;
    startTime: string;
    endTime: string;
    remainingSeats: number;
    isCanceled: boolean;
  }[];
}

/**
 * 공연관리자용 공연 목록 조회 (v1)
 * GET /api/v1/managers/performances
 * @param page 0부터 시작하는 페이지 번호
 * @param size 페이지 크기
 * @param sort 정렬 (예: ["createdAt,desc"])
 * @param status 상태 필터 (예: "PENDING", "CONFIRMED", "REJECTED,CANCELLED")
 */
export async function getManagerPerformancesV1(
  page: number = 0,
  size: number = 20,
  sort: string[] = ["startDate,desc"],
  status?: string
): Promise<ManagerPerformancePageResponse> {
  const params = new URLSearchParams();
  params.append("page", page.toString());
  params.append("size", size.toString());
  sort.forEach((s) => params.append("sort", s));
  if (status) params.append("status", status);

  const response = await api.get(`/managers/performances?${params.toString()}`);
  return response.data;
}

/**
 * 공연관리자용 공연 상세 조회 (v1)
 * GET /api/v1/managers/performances/{performanceId}
 */
export async function getManagerPerformanceDetailV1(performanceId: number | string): Promise<ManagerPerformanceDetail> {
  const response = await api.get(`/managers/performances/${performanceId}`);
  return response.data;
}

/**
 * 공연관리자용 공연 검색 (search)
 * GET /api/v1/managers/performances/search
 * @param params 검색 파라미터 (title, venue, start, end, status, page, size, sort)
 */
export async function searchManagerPerformances(params: {
  title?: string;
  venue?: string;
  start?: string; // ISO string
  end?: string;   // ISO string
  status?: string;
  page?: number;
  size?: number;
  sort?: string[];
} = {}): Promise<ManagerPerformancePageResponse> {
  const searchParams = new URLSearchParams();
  if (params.title) searchParams.append('title', params.title);
  if (params.venue) searchParams.append('venue', params.venue);
  if (params.start) searchParams.append('start', params.start);
  if (params.end) searchParams.append('end', params.end);
  if (params.status) searchParams.append('status', params.status);
  if (params.page !== undefined) searchParams.append('page', params.page.toString());
  if (params.size !== undefined) searchParams.append('size', params.size.toString());
  if (params.sort) params.sort.forEach((s) => searchParams.append('sort', s));

  const response = await api.get(`/managers/performances/search?${searchParams.toString()}`);
  return response.data;
} 