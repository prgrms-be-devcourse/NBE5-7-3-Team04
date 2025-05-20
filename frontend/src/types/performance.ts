export enum PerformanceCategory {
  CLASSIC_DANCE = "CLASSIC_DANCE", // 클래식 + 무용
  EVENT_DISPLAY = "EVENT_DISPLAY", // 행사 + 전시
  CONCERT = "CONCERT", // 콘서트
  MUSICAL_OPERA = "MUSICAL_OPERA", // 뮤지컬 + 오페라
  THEATER = "THEATER", // 연극
  ETC = "ETC" // 기타
}

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