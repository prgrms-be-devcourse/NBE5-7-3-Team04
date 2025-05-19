import { parseISO, addHours, format } from "date-fns";

/**
 * UTC ISO 문자열을 KST로 변환해 'yyyy년 MM월 dd일 HH:mm' 포맷으로 반환
 */
export function formatKSTDateTime(dateString: string): string {
  try {
    const date = parseISO(dateString);
    const kstDate = addHours(date, 9); // UTC to KST
    return format(kstDate, "yyyy년 MM월 dd일 HH:mm");
  } catch {
    return dateString;
  }
} 