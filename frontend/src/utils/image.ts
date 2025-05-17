const RANDOM_IMAGES = [
  'https://picsum.photos/400/300?random=1',
  'https://picsum.photos/400/300?random=2',
  'https://picsum.photos/400/300?random=3',
  'https://picsum.photos/400/300?random=4',
  'https://picsum.photos/400/300?random=5',
];

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

export const getImageUrl = (fileKey: string | null): string => {
  if (!fileKey) {
    const randomIndex = Math.floor(Math.random() * RANDOM_IMAGES.length);
    return RANDOM_IMAGES[randomIndex];
  }

  // 이미 전체 URL인 경우 그대로 반환
  if (fileKey.startsWith('http://') || fileKey.startsWith('https://')) {
    return fileKey;
  }

  const cloudFrontUrl = process.env.NEXT_PUBLIC_CLOUDFRONT_URL;
  if (!cloudFrontUrl) {
    console.error('CloudFront URL is not defined');
    return RANDOM_IMAGES[0];
  }

  // fileKey가 이미 CloudFront URL을 포함하고 있는지 확인
  if (fileKey.includes(cloudFrontUrl)) {
    return fileKey;
  }

  // filename.mimetype 형식의 fileKey를 그대로 사용
  return `${cloudFrontUrl}/${fileKey}`;
}; 