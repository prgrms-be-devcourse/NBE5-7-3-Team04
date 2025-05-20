"use client"

import { useState, useEffect } from 'react'
import { searchPerformances, getMe } from '@/src/api/api'
import { Performance } from '@/src/api/performance'
import { Hero } from '@/components/hero'
import { RecommendedPerformances } from '@/components/recommended-performances'
import { getPerformanceImageUrl } from '@/lib/utils'
import { formatKSTDateTime } from "@/src/api/utils/date";
import { Calendar } from "lucide-react";
import { CardContent } from "@/components/ui/card";
import { useRouter } from 'next/navigation';

interface PerformanceWithImage extends Performance {
  image: string
}

export default function Home() {
  const router = useRouter();
  const [performances, setPerformances] = useState<PerformanceWithImage[]>([])
  const [recommendedPerformances, setRecommendedPerformances] = useState<PerformanceWithImage[]>([])
  const [heroPerformances, setHeroPerformances] = useState<PerformanceWithImage[]>([])
  const [categoryPerformances, setCategoryPerformances] = useState<{
    CLASSIC_DANCE: PerformanceWithImage[];
    EVENT_DISPLAY: PerformanceWithImage[];
    CONCERT: PerformanceWithImage[];
    MUSICAL_OPERA: PerformanceWithImage[];
    THEATER: PerformanceWithImage[];
    ETC: PerformanceWithImage[];
  }>({
    CLASSIC_DANCE: [],
    EVENT_DISPLAY: [],
    CONCERT: [],
    MUSICAL_OPERA: [],
    THEATER: [],
    ETC: []
  })
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function fetchPerformances() {
      setIsLoading(true)
      setError(null)
      try {
        // 1. 카테고리별 10개씩 최신순으로 가져오기
        const [classicDance, eventDisplay, concert, musicalOpera, theater, etc] = await Promise.all([
          searchPerformances({ category: 'CLASSIC_DANCE', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'EVENT_DISPLAY', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'CONCERT', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'MUSICAL_OPERA', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'THEATER', size: 10, sort: ['startDate,desc'] }),
          searchPerformances({ category: 'ETC', size: 10, sort: ['startDate,desc'] }),
        ])

        // 2. 이미지 URL 매핑
        const addImageUrl = (p: Performance): PerformanceWithImage => ({
          ...p,
          image: getPerformanceImageUrl(p.fileUrl)
        })

        const classicDanceWithImages = (classicDance.content || []).map(addImageUrl)
        const eventDisplayWithImages = (eventDisplay.content || []).map(addImageUrl)
        const concertWithImages = (concert.content || []).map(addImageUrl)
        const musicalOperaWithImages = (musicalOpera.content || []).map(addImageUrl)
        const theaterWithImages = (theater.content || []).map(addImageUrl)
        const etcWithImages = (etc.content || []).map(addImageUrl)

        // 3. 카테고리별 데이터 설정
        setCategoryPerformances({
          CLASSIC_DANCE: classicDanceWithImages,
          EVENT_DISPLAY: eventDisplayWithImages,
          CONCERT: concertWithImages,
          MUSICAL_OPERA: musicalOperaWithImages,
          THEATER: theaterWithImages,
          ETC: etcWithImages
        })

        // 4. 전체 데이터 합치기
        const allPerformances = [
          ...classicDanceWithImages,
          ...eventDisplayWithImages,
          ...concertWithImages,
          ...musicalOperaWithImages,
          ...theaterWithImages,
          ...etcWithImages
        ]
        
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

  useEffect(() => {
    let isMounted = true;

    const checkUserStatus = async () => {
      console.log("[Home Page] checkUserStatus 시작");
      const token = localStorage.getItem("token");
      console.log("[Home Page] Current token:", token);

      if (!token) {
        console.log("[Home Page] 토큰이 없음 - 함수 종료");
        return;
      }

      try {
        console.log("[Home Page] getMe() 호출 시작");
        const userData = await getMe();
        console.log("[Home Page] getMe() 응답:", userData);

        if (isMounted && userData && (!userData.phoneNumber || !userData.email)) {
          console.log("[Home Page] 온보딩 필요 - phoneNumber 또는 email 없음");
          console.log("[Home Page] /oauth2/sign-up로 리다이렉션 시작");
          router.push("/oauth2/sign-up");
        } else {
          console.log("[Home Page] 온보딩 불필요 - 정상 상태");
        }
      } catch (error: any) {
        console.error("[Home Page] 에러 발생:", {
          status: error?.response?.status,
          message: error?.message,
          data: error?.response?.data
        });
        
        if (isMounted && error?.response?.status !== 401) {
          console.log("[Home Page] 401이 아닌 에러로 인한 토큰 제거");
          localStorage.removeItem("token");
        } else {
          console.log("[Home Page] 401 에러 발생 - 토큰 유지");
        }
      }
    };

    console.log("[Home Page] useEffect 실행");
    checkUserStatus();

    return () => {
      console.log("[Home Page] useEffect cleanup");
      isMounted = false;
    };
  }, [router]);

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
