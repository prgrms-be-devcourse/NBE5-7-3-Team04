const RANDOM_IMAGES = [
  'https://picsum.photos/400/300?random=1',
  'https://picsum.photos/400/300?random=2',
  'https://picsum.photos/400/300?random=3',
  'https://picsum.photos/400/300?random=4',
  'https://picsum.photos/400/300?random=5',
];

// CloudFront URL
const CLOUDFRONT_URL = process.env.NEXT_PUBLIC_CLOUDFRONT_URL

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