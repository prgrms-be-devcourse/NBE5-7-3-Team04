"use client"

import { useState, useEffect } from 'react'
import { searchPerformances } from '@/src/api/api'
import { Performance } from '@/src/api/performance'
import { Hero } from '@/components/hero'
import { RecommendedPerformances } from '@/components/recommended-performances'
import { getPerformanceImageUrl } from '@/lib/utils'
import { formatKSTDateTime } from "@/src/api/utils/date";
import { Calendar } from "lucide-react";
import { CardContent } from "@/components/ui/card";

interface PerformanceWithImage extends Performance {
  image: string
}

export default function Home() {
  const [performances, setPerformances] = useState<PerformanceWithImage[]>([])
  const [recommendedPerformances, setRecommendedPerformances] = useState<PerformanceWithImage[]>([])
  const [heroPerformances, setHeroPerformances] = useState<PerformanceWithImage[]>([])
  const [categoryPerformances, setCategoryPerformances] = useState<{
    SINGING: PerformanceWithImage[];
    DANCING: PerformanceWithImage[];
    OPERA: PerformanceWithImage[];
  }>({
    SINGING: [],
    DANCING: [],
    OPERA: []
  })
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function fetchPerformances() {
      setIsLoading(true)
      setError(null)
      try {
        // 1. 카테고리별 10개씩 최신순으로 가져오기
        const [singing, dancing, opera] = await Promise.all([
          searchPerformances({ category: 'SINGING', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'DANCING', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'OPERA', size: 10, sort: ['startDate,desc'] }),
        ])

        // 2. 이미지 URL 매핑
        const addImageUrl = (p: Performance): PerformanceWithImage => ({
          ...p,
          image: getPerformanceImageUrl(p.fileUrl)
        })

        const singingWithImages = (singing.content || []).map(addImageUrl)
        const dancingWithImages = (dancing.content || []).map(addImageUrl)
        const operaWithImages = (opera.content || []).map(addImageUrl)

        // 3. 카테고리별 데이터 설정
        setCategoryPerformances({
          SINGING: singingWithImages,
          DANCING: dancingWithImages,
          OPERA: operaWithImages
        })

        // 4. 전체 데이터 합치기
        const allPerformances = [...singingWithImages, ...dancingWithImages, ...operaWithImages]
        
        // 5. 각 용도별 데이터 설정
        setPerformances(allPerformances)
        setHeroPerformances(allPerformances.slice(0, 3)) // 최신순 3개
        setRecommendedPerformances(allPerformances.sort(() => Math.random() - 0.5).slice(0, 10)) // 랜덤 10개

      } catch (err) {
        setError('공연 정보를 불러오는 중 오류가 발생했습니다.')
      } finally {
        setIsLoading(false)
  }
    }

    fetchPerformances()
  }, [])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">공연 정보를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center text-red-500">{error}</div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-8">
      <Hero />
      <RecommendedPerformances categoryPerformances={categoryPerformances} />
    </div>
  )
}
