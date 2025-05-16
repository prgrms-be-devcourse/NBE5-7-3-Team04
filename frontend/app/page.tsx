"use client"

import { useState, useEffect, useRef, type MouseEvent, type TouchEvent } from "react"
import { Button } from "@/components/ui/button"
import { PerformanceCard } from "@/components/performance-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import Link from "next/link"
import Image from "next/image"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { getPerformances } from "../src/api/performance"
import { getImageUrl } from "../src/utils/image"
import type { PerformancePageResponse } from "../src/types/performance"
import { useRouter } from "next/navigation"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"

// 슬라이더 데이터
const sliderData = [
  {
    id: "1",
    category: "뮤지컬",
    title: "뮤지컬 라이온 킹",
    venue: "세종문화회관",
    date: "2025.06.15 - 2025.08.30",
    description: "브로드웨이 역사상 가장 성공적인 뮤지컬 중 하나인 '라이온 킹'이 한국에 찾아옵니다.",
    image: "/placeholder-ssak2.png",
  },
  {
    id: "2",
    category: "콘서트",
    title: "2025 여름 재즈 페스티벌",
    venue: "올림픽 공원",
    date: "2025.07.20 - 2025.07.22",
    description: "세계적인 재즈 아티스트들이 모여 특별한 여름 밤을 선사합니다.",
    image: "/lively-jazz-concert.png",
  },
  {
    id: "3",
    category: "클래식",
    title: "베토벤 교향곡 전곡 시리즈",
    venue: "예술의전당",
    date: "2025.09.10 - 2025.10.15",
    description: "세계적인 지휘자와 함께하는 베토벤 교향곡 전곡 시리즈를 감상하세요.",
    image: "/classical-orchestra-concert.png",
  },
]

export default function Home() {
  const [currentSlide, setCurrentSlide] = useState(0)
  const [isAnimating, setIsAnimating] = useState(false)
  const [performances, setPerformances] = useState<Array<{
    id: number;
    title: string;
    category: string;
    venue: string;
    startDate: string;
    endDate: string;
    price: number;
    fileUrl: string | null;
    imageUrl: string;
  }>>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [recommendedPerformances, setRecommendedPerformances] = useState<Array<{
    id: number;
    title: string;
    category: string;
    venue: string;
    startDate: string;
    price: number;
    fileUrl: string | null;
    imageUrl: string;
  }>>([])
  const [heroPerformances, setHeroPerformances] = useState<Array<{
    id: number;
    category: string;
    title: string;
    venue: string;
    date: string;
    description: string;
    image: string;
  }>>([])
  const carouselRef = useRef<HTMLDivElement>(null)
  const router = useRouter()

  // 드래그 관련 상태
  const [isDragging, setIsDragging] = useState(false)
  const [startX, setStartX] = useState(0)
  const [scrollLeft, setScrollLeft] = useState(0)
  const [clickStartTime, setClickStartTime] = useState(0)
  const [clickStartPosition, setClickStartPosition] = useState({ x: 0, y: 0 })

  // 공연 목록 가져오기
  useEffect(() => {
    const fetchPerformances = async () => {
      try {
        setLoading(true)
        const { content } = await getPerformances()
        
        // 이미지 URL을 미리 계산하여 저장
        const performancesWithImages = content.map(performance => ({
          ...performance,
          imageUrl: getImageUrl(performance.fileUrl)
        }))
        setPerformances(performancesWithImages)
        
        // 추천 공연 선택 (랜덤으로 4-6개)
        const randomCount = Math.floor(Math.random() * 3) + 4
        const shuffled = [...performancesWithImages].sort(() => 0.5 - Math.random())
        setRecommendedPerformances(shuffled.slice(0, randomCount))

        // 히어로 섹션용 공연 데이터 선택 (최신 공연 3개)
        const heroData = performancesWithImages.slice(0, 3).map(performance => ({
          id: performance.id,
          category: performance.category === "SINGING" ? "콘서트" : 
                    performance.category === "DANCING" ? "무용" : "오페라",
          title: performance.title,
          venue: performance.venue,
          date: `${new Date(performance.startDate).toLocaleDateString()} - ${new Date(performance.endDate).toLocaleDateString()}`,
          description: `${performance.venue}에서 펼쳐지는 특별한 공연`,
          image: performance.imageUrl
        }))
        setHeroPerformances(heroData)
      } catch (err) {
        console.error('Error fetching performances:', err)
        setError('공연 목록을 불러오는 중 오류가 발생했습니다.')
      } finally {
        setLoading(false)
      }
    }

    fetchPerformances()
  }, [])

  // 자동 슬라이드 기능
  useEffect(() => {
    const interval = setInterval(() => {
      nextSlide()
    }, 5000) // 5초마다 슬라이드 변경

    return () => clearInterval(interval)
  }, [currentSlide])

  const nextSlide = () => {
    if (isAnimating || heroPerformances.length === 0) return

    setIsAnimating(true)
    setCurrentSlide((prev) => (prev === heroPerformances.length - 1 ? 0 : prev + 1))

    // 애니메이션 완료 후 상태 초기화
    setTimeout(() => {
      setIsAnimating(false)
    }, 500)
  }

  const prevSlide = () => {
    if (isAnimating || heroPerformances.length === 0) return

    setIsAnimating(true)
    setCurrentSlide((prev) => (prev === 0 ? heroPerformances.length - 1 : prev - 1))

    // 애니메이션 완료 후 상태 초기화
    setTimeout(() => {
      setIsAnimating(false)
    }, 500)
  }

  // 추천 공연 캐러셀 스크롤
  const scrollCarousel = (direction: "left" | "right") => {
    if (!carouselRef.current) return

    const cardWidth = 300 // 카드 너비
    const gap = 24 // gap-6 = 1.5rem = 24px
    const scrollAmount = cardWidth + gap // 카드 너비 + 간격

    const currentScroll = carouselRef.current.scrollLeft
    const maxScroll = carouselRef.current.scrollWidth - carouselRef.current.clientWidth

    let targetScroll
    if (direction === "left") {
      targetScroll = Math.max(0, currentScroll - scrollAmount)
    } else {
      targetScroll = Math.min(maxScroll, currentScroll + scrollAmount)
    }

    carouselRef.current.scrollTo({
      left: targetScroll,
      behavior: "smooth"
    })
  }

  // 마우스 드래그 이벤트 핸들러
  const handleMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (!carouselRef.current) return

    // 마우스 왼쪽 버튼 클릭일 때만 드래그 시작
    if (e.button !== 0) return

    setIsDragging(false)
    setStartX(e.pageX - carouselRef.current.offsetLeft)
    setScrollLeft(carouselRef.current.scrollLeft)
    setClickStartTime(Date.now())
    setClickStartPosition({ x: e.pageX, y: e.pageY })
  }

  const handleMouseMove = (e: MouseEvent<HTMLDivElement>) => {
    if (!carouselRef.current || !isDragging) return

    const x = e.pageX - carouselRef.current.offsetLeft
    const walk = (x - startX) * 2
    
    // 드래그 거리가 30px 이상일 때만 스크롤
    if (Math.abs(walk) > 30) {
      e.preventDefault()
      carouselRef.current.scrollLeft = scrollLeft - walk
    }
  }

  const handleMouseUp = (e: MouseEvent<HTMLDivElement>) => {
    if (!isDragging) return

    const clickDuration = Date.now() - clickStartTime
    const moveDistance = Math.sqrt(
      Math.pow(e.pageX - clickStartPosition.x, 2) + Math.pow(e.pageY - clickStartPosition.y, 2)
    )

    // 드래그가 아니고 짧은 시간 내에 적은 움직임이 있었다면 클릭으로 간주
    if (clickDuration < 300 && moveDistance < 30) {
      const target = e.target as HTMLElement
      const card = target.closest('a')
      if (card) {
        const href = card.getAttribute('href')
        if (href) {
          router.push(href)
        }
      }
    }

    setIsDragging(false)
  }

  const handleMouseLeave = () => {
    if (isDragging) {
      setIsDragging(false)
    }
  }

  // 터치 이벤트 핸들러
  const handleTouchStart = (e: TouchEvent<HTMLDivElement>) => {
    if (!carouselRef.current || e.touches.length !== 1) return

    setIsDragging(false)
    setStartX(e.touches[0].pageX - carouselRef.current.offsetLeft)
    setScrollLeft(carouselRef.current.scrollLeft)
    setClickStartTime(Date.now())
    setClickStartPosition({ x: e.touches[0].pageX, y: e.touches[0].pageY })
  }

  const handleTouchMove = (e: TouchEvent<HTMLDivElement>) => {
    if (!carouselRef.current || e.touches.length !== 1) return

    const x = e.touches[0].pageX - carouselRef.current.offsetLeft
    const walk = (x - startX) * 2

    // 드래그 거리가 30px 이상일 때만 드래그로 간주
    if (Math.abs(walk) > 30) {
      setIsDragging(true)
      carouselRef.current.scrollLeft = scrollLeft - walk
    }
  }

  const handleTouchEnd = (e: TouchEvent<HTMLDivElement>) => {
    if (e.changedTouches.length === 1) {
      const touchDuration = Date.now() - clickStartTime
      const moveDistance = Math.sqrt(
        Math.pow(e.changedTouches[0].pageX - clickStartPosition.x, 2) +
        Math.pow(e.changedTouches[0].pageY - clickStartPosition.y, 2)
      )

      // 드래그가 아니고 짧은 시간 내에 적은 움직임이 있었다면 탭으로 간주
      if (!isDragging && touchDuration < 300 && moveDistance < 30) {
        const target = e.changedTouches[0].target as HTMLElement
        const card = target.closest('a')
        if (card) {
          const href = card.getAttribute('href')
          if (href) {
            router.push(href)
          }
        }
      }
    }

    setIsDragging(false)
  }

  // 공연 데이터 포맷팅
  const formatPerformance = (performance: typeof performances[0]) => ({
    id: performance.id.toString(),
    title: performance.title,
    date: new Date(performance.startDate).toLocaleDateString(),
    time: `${new Date(performance.startDate).toLocaleDateString()} ~ ${new Date(performance.endDate).toLocaleDateString()}`,
    location: performance.venue,
    price: performance.price,
    image: performance.imageUrl,
    category: performance.category === "SINGING" ? "콘서트" :
              performance.category === "DANCING" ? "무용" :
              performance.category === "OPERA" ? "오페라" : "공연",
    status: "예매가능" as const,
  })

  if (loading) {
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
      {/* 히어로 섹션 */}
      <section className="relative min-h-[600px] w-full overflow-hidden bg-gradient-to-br from-purple-100 via-indigo-50 to-slate-50">
        {/* 배경 효과 */}
        <div className="absolute inset-0 bg-gradient-to-tr from-purple-200/30 via-indigo-100/20 to-transparent"></div>
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_80%_20%,_rgba(124,58,237,0.1)_0%,_transparent_50%)]"></div>
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_80%,_rgba(79,70,229,0.1)_0%,_transparent_50%)]"></div>
        
        {/* 미묘한 그리드 라인 */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,_rgba(0,0,0,0.02)_1px,_transparent_1px),linear-gradient(to_bottom,_rgba(0,0,0,0.02)_1px,_transparent_1px)] bg-[size:40px_40px]"></div>

        <div className="container relative z-10 flex h-full flex-col items-center justify-center gap-4 text-slate-800 px-4 md:px-6 py-12">
          <h1 className="text-4xl font-bold sm:text-5xl md:text-6xl text-center bg-gradient-to-r from-purple-600 via-indigo-500 to-purple-600 bg-clip-text text-transparent">TICKET4U</h1>
          <p className="text-lg sm:text-xl text-center mb-8 text-slate-600">당신의 특별한 순간을 위한 티켓 예매 서비스</p>

          <div className="w-full max-w-4xl bg-white/90 backdrop-blur-sm rounded-lg overflow-hidden shadow-lg border border-slate-100">
            <div className="relative">
              <div className="aspect-[16/9] bg-gray-200 flex items-center justify-center overflow-hidden">
                {heroPerformances.map((slide, index) => (
                  <div
                    key={slide.id}
                    className={`absolute inset-0 transition-opacity duration-500 ease-in-out ${
                      index === currentSlide ? "opacity-100" : "opacity-0 pointer-events-none"
                    }`}
                  >
                    <Link href={`/performances/${slide.id}`} className="block w-full h-full">
                      <Image
                        src={slide.image}
                        alt={slide.title}
                        width={900}
                        height={500}
                        className="w-full h-full object-cover"
                      />

                      <div className="absolute inset-0 flex flex-col justify-end p-8 bg-gradient-to-t from-black/80 via-black/50 to-transparent">
                        <div className="bg-white/10 backdrop-blur-sm text-white text-xs px-2 py-1 rounded-md w-fit mb-2">
                          {slide.category}
                        </div>
                        <h2 className="text-3xl font-bold text-white mb-2">{slide.title}</h2>
                        <p className="text-sm text-white/90 mb-2">
                          {slide.venue} | {slide.date}
                        </p>
                        <p className="text-sm text-white/80 mb-4">{slide.description}</p>
                        <Button className="w-fit bg-white/10 hover:bg-white/20 text-white border-white/20">예매하기</Button>
                      </div>
                    </Link>
                  </div>
                ))}
              </div>

              {/* 슬라이드 인디케이터 */}
              <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 z-10">
                {heroPerformances.map((_, index) => (
                  <button
                    key={index}
                    className={`w-2 h-2 rounded-full transition-colors ${
                      index === currentSlide ? "bg-white" : "bg-white/40"
                    }`}
                    onClick={(e) => {
                      e.stopPropagation()
                      setCurrentSlide(index)
                    }}
                    aria-label={`슬라이드 ${index + 1}로 이동`}
                  />
                ))}
              </div>

              <div className="absolute top-1/2 -translate-y-1/2 left-0 flex justify-between w-full px-4">
                <Button
                  size="icon"
                  variant="outline"
                  className="bg-black/20 border-white/20 text-white rounded-full h-10 w-10 hover:bg-black/30"
                  onClick={(e) => {
                    e.stopPropagation()
                    prevSlide()
                  }}
                  disabled={isAnimating}
                >
                  <ChevronLeft className="h-5 w-5" />
                  <span className="sr-only">이전</span>
                </Button>
                <Button
                  size="icon"
                  variant="outline"
                  className="bg-black/20 border-white/20 text-white rounded-full h-10 w-10 hover:bg-black/30"
                  onClick={(e) => {
                    e.stopPropagation()
                    nextSlide()
                  }}
                  disabled={isAnimating}
                >
                  <ChevronRight className="h-5 w-5" />
                  <span className="sr-only">다음</span>
                </Button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 추천 공연 섹션 */}
      <section className="relative py-16 overflow-hidden -mt-16">
        <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-white to-indigo-50"></div>
        <div className="container relative z-10 px-4 md:px-6">
          <div className="flex flex-col gap-8">
            <div className="flex flex-col gap-2 text-center">
              <h2 className="text-4xl font-bold tracking-tight bg-gradient-to-r text-black bg-clip-text text-transparent">
                추천 공연
              </h2>
              <p className="text-lg text-muted-foreground/80">
                TICKET4U가 엄선한 <span className="font-medium text-purple-600">특별한 공연</span>을 만나보세요
              </p>
            </div>

            <div className="relative">
              <div
                ref={carouselRef}
                className="flex gap-6 overflow-x-auto pb-8 scrollbar-hide snap-x snap-mandatory"
                style={{ 
                  scrollbarWidth: "none", 
                  msOverflowStyle: "none",
                  scrollBehavior: "smooth",
                  WebkitOverflowScrolling: "touch"
                }}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseLeave}
                onTouchStart={handleTouchStart}
                onTouchMove={handleTouchMove}
                onTouchEnd={handleTouchEnd}
              >
                {recommendedPerformances.map((performance) => (
                  <Link 
                    key={performance.id} 
                    href={`/performances/${performance.id}`}
                    className="min-w-[300px] snap-center flex-shrink-0"
                    onClick={(e) => {
                      if (isDragging) {
                        e.preventDefault()
                      }
                    }}
                  >
                    <Card className="overflow-hidden hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                      <div className="aspect-[4/3] relative overflow-hidden">
                        <img
                          src={performance.imageUrl}
                          alt={performance.title}
                          className="object-cover w-full h-full transition-transform duration-300 hover:scale-105"
                        />
                        <div className="absolute top-3 left-3">
                          <Badge variant="secondary" className="bg-purple-100 text-purple-700 border-purple-200">
                            {performance.category === "SINGING" ? "콘서트" : performance.category === "DANCING" ? "무용" : "오페라"}
                          </Badge>
                        </div>
                      </div>
                      <CardContent className="p-4">
                        <div className="space-y-2">
                          <h3 className="text-lg font-semibold line-clamp-2 bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent hover:from-purple-600 hover:to-indigo-600 transition-all duration-300">
                            {performance.title}
                          </h3>
                          <div className="flex items-center gap-2 text-sm text-muted-foreground/80">
                            <span className="font-medium">{performance.venue}</span>
                            <span className="text-purple-400">•</span>
                            <span>{new Date(performance.startDate).toLocaleDateString()}</span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </Link>
                ))}
              </div>

              <div className="absolute top-1/2 -translate-y-1/2 left-0 right-0 flex justify-between pointer-events-none">
                <Button
                  size="icon"
                  variant="outline"
                  className="rounded-full bg-white/80 backdrop-blur-sm border-white/20 text-gray-700 hover:bg-white pointer-events-auto ml-4 shadow-lg"
                  onClick={() => scrollCarousel("left")}
                >
                  <ChevronLeft className="h-4 w-4" />
                  <span className="sr-only">이전</span>
                </Button>
                <Button
                  size="icon"
                  variant="outline"
                  className="rounded-full bg-white/80 backdrop-blur-sm border-white/20 text-gray-700 hover:bg-white pointer-events-auto mr-4 shadow-lg"
                  onClick={() => scrollCarousel("right")}
                >
                  <ChevronRight className="h-4 w-4" />
                  <span className="sr-only">다음</span>
                </Button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 공연 목록 섹션 */}
      <section className="container py-8 px-4 md:px-6">
        <div className="flex flex-col gap-6">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold tracking-tight">공연 목록</h2>
            <Button variant="outline" asChild>
              <Link href="/performances">모든 공연 보기</Link>
            </Button>
          </div>

          <Tabs defaultValue="all" className="w-full">
            <TabsList className="mb-6">
              <TabsTrigger value="all">전체</TabsTrigger>
              <TabsTrigger value="concert">콘서트</TabsTrigger>
              <TabsTrigger value="dance">무용</TabsTrigger>
              <TabsTrigger value="opera">오페라</TabsTrigger>
            </TabsList>
            <TabsContent value="all" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances.slice(0, 8).map((performance) => (
                  <div key={performance.id} className="cursor-pointer" onClick={() => router.push(`/performances/${performance.id}`)}>
                    <PerformanceCard performance={formatPerformance(performance)} />
                  </div>
                ))}
              </div>
            </TabsContent>
            <TabsContent value="concert" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => p.category === "SINGING")
                  .slice(0, 8)
                  .map((performance) => (
                    <div key={performance.id} className="cursor-pointer" onClick={() => router.push(`/performances/${performance.id}`)}>
                      <PerformanceCard performance={formatPerformance(performance)} />
                    </div>
                  ))}
              </div>
            </TabsContent>
            <TabsContent value="dance" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => p.category === "DANCING")
                  .slice(0, 8)
                  .map((performance) => (
                    <div key={performance.id} className="cursor-pointer" onClick={() => router.push(`/performances/${performance.id}`)}>
                      <PerformanceCard performance={formatPerformance(performance)} />
                    </div>
                  ))}
              </div>
            </TabsContent>
            <TabsContent value="opera" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => p.category === "OPERA")
                  .slice(0, 8)
                  .map((performance) => (
                    <div key={performance.id} className="cursor-pointer" onClick={() => router.push(`/performances/${performance.id}`)}>
                      <PerformanceCard performance={formatPerformance(performance)} />
                    </div>
                  ))}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </section>

      {/* 공연 관리자 신청 섹션 */}
      <section className="bg-gray-50 py-12">
        <div className="container px-4 md:px-6">
          <div className="bg-white rounded-lg p-8 shadow-sm">
            <div className="flex flex-col md:flex-row justify-between items-center gap-6">
              <div>
                <h2 className="text-2xl font-bold mb-2">공연 관리자가 되어보세요</h2>
                <p className="text-muted-foreground">
                  당신의 공연을 TICKET4U에서 홍보하고 티켓을 판매하세요. 간단한 신청 절차를 통해 시작할 수 있습니다.
                </p>
              </div>
              <Button className="whitespace-nowrap bg-purple-500 hover:bg-purple-600" asChild>
                <Link href="/users/mypage/register">지금 신청하기</Link>
              </Button>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
