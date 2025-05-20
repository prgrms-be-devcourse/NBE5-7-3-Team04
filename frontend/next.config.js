/** @type {import('next').NextConfig} */
const nextConfig = {
  images: {
    domains: ['d290kzvpuy1puq.cloudfront.net', 'd3l7tgeznk7sw9.cloudfront.net'],
  },
  env: {
    TZ: 'Asia/Seoul'
  }
}

module.exports = nextConfig 