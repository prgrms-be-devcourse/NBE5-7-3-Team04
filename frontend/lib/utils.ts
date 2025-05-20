import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// CloudFront URL
const CLOUDFRONT_URL = process.env.NEXT_PUBLIC_CLOUDFRONT_URL

/**
 * 공연 이미지 URL을 생성하는 함수
 * @param fileUrl - 공연 이미지 파일 URL
 * @param fallbackUrl - 이미지가 없을 경우 사용할 기본 이미지 URL
 * @returns 완성된 이미지 URL
 */
export function getPerformanceImageUrl(fileUrl: string | null | undefined, fallbackUrl: string = '/placeholder-image.jpg'): string {
  if (!fileUrl) return fallbackUrl
  
  // CloudFront URL이 이미 포함되어 있는 경우
  if (fileUrl.startsWith('http')) {
    return fileUrl
  }
  
  // 상대 경로인 경우 CloudFront URL과 결합
  const cleanFileUrl = fileUrl.replace(/^\/+/, '') // 시작 부분의 모든 슬래시 제거
  return `${CLOUDFRONT_URL}${cleanFileUrl}`
}
