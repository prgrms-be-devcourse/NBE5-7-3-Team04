'use client'

import { useState, useEffect } from 'react'
import Image from 'next/image'

interface Banner {
  id: number
  image: string
  alt: string
}

interface BannerProps {
  banners: Banner[]
  interval?: number
}

export function Banner({ banners, interval = 3000 }: BannerProps) {
  const [currentBanner, setCurrentBanner] = useState(0)

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentBanner((prev) => (prev + 1) % banners.length)
    }, interval)
    return () => clearInterval(timer)
  }, [banners.length, interval])

  return (
    <div className="w-full">
      {banners.map((banner, idx) => (
        <div
          key={banner.id}
          className={`relative w-full transition-opacity duration-500 ease-in-out ${
            idx === currentBanner ? 'opacity-100' : 'opacity-0 hidden'
          }`}
        >
          <div className="relative w-full" style={{ paddingTop: '33.33%' }}>
            <Image
              src={banner.image}
              alt={banner.alt}
              fill
              className="object-cover"
              priority={idx === 0}
              sizes="(max-width: 1280px) 100vw, 1280px"
            />
          </div>
        </div>
      ))}
    </div>
  )
} 