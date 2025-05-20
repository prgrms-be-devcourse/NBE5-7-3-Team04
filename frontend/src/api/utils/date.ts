import { parseISO } from "date-fns";
import { ko } from "date-fns/locale";
import { formatInTimeZone } from 'date-fns-tz';

const TIMEZONE = 'Asia/Seoul';

/**
 * UTC ISO 문자열을 KST로 변환해 'yyyy년 MM월 dd일 HH:mm' 포맷으로 반환
 */
export function formatKSTDateTime(dateString: string): string {
  try {
    return formatInTimeZone(parseISO(dateString), TIMEZONE, "yyyy년 MM월 dd일 HH:mm", { locale: ko });
  } catch {
    return dateString;
  }
}

/**
 * UTC ISO 문자열을 KST로 변환해 'yyyy.MM.dd' 포맷으로 반환
 */
export function formatKSTDate(dateString: string): string {
  try {
    return formatInTimeZone(parseISO(dateString), TIMEZONE, "yyyy.MM.dd", { locale: ko });
  } catch {
    return dateString;
  }
}

/**
 * UTC ISO 문자열을 KST로 변환해 'HH:mm' 포맷으로 반환
 */
export function formatKSTTime(dateString: string): string {
  try {
    return formatInTimeZone(parseISO(dateString), TIMEZONE, "HH:mm", { locale: ko });
  } catch {
    return dateString;
  }
} 