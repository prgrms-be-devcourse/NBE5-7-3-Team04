const RANDOM_IMAGES = [
  'https://picsum.photos/400/300?random=1',
  'https://picsum.photos/400/300?random=2',
  'https://picsum.photos/400/300?random=3',
  'https://picsum.photos/400/300?random=4',
  'https://picsum.photos/400/300?random=5',
];

export const getImageUrl = (fileKey: string | null): string => {
  if (!fileKey) {
    const randomIndex = Math.floor(Math.random() * RANDOM_IMAGES.length);
    return RANDOM_IMAGES[randomIndex];
  }

  const cloudFrontUrl = process.env.NEXT_PUBLIC_CLOUDFRONT_URL;
  if (!cloudFrontUrl) {
    console.error('CloudFront URL is not defined');
    return RANDOM_IMAGES[0];
  }

  return `${cloudFrontUrl}/${fileKey}`;
}; 