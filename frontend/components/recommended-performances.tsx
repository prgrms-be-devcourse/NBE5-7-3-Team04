'use client'

import { useRef, useState, useEffect } from 'react'
import Link from 'next/link'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Performance } from '@/src/api/performance'
import { getPerformanceImageUrl } from '@/lib/utils'
import { formatKSTDate } from "@/src/api/utils/date"

interface RecommendedPerformancesProps {
  categoryPerformances: {
    SINGING: Performance[]
    DANCING: Performance[]
    OPERA: Performance[]
  }
}

export function RecommendedPerformances({ categoryPerformances }: RecommendedPerformancesProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [startX, setStartX] = useState(0)
  const [scrollLeft, setScrollLeft] = useState(0)
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL')
  const [randomPerformances, setRandomPerformances] = useState<Performance[]>([])
  const carouselRef = useRef<HTMLDivElement>(null)

  // 랜덤 공연 목록 생성
  useEffect(() => {
    const allPerformances = [
      ...categoryPerformances.SINGING,
      ...categoryPerformances.DANCING,
      ...categoryPerformances.OPERA
    ]
    setRandomPerformances(allPerformances.sort(() => Math.random() - 0.5).slice(0, 10))
  }, [categoryPerformances])

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true)
    setStartX(e.pageX - (carouselRef.current?.offsetLeft || 0))
    setScrollLeft(carouselRef.current?.scrollLeft || 0)
  }

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return
    e.preventDefault()
    const x = e.pageX - (carouselRef.current?.offsetLeft || 0)
    const walk = (x - startX) * 2
    if (carouselRef.current) {
      carouselRef.current.scrollLeft = scrollLeft - walk
    }
  }

  const handleMouseUp = () => {
    setIsDragging(false)
  }

  const handleMouseLeave = () => {
    setIsDragging(false)
  }

  const handleTouchStart = (e: React.TouchEvent) => {
    setIsDragging(true)
    setStartX(e.touches[0].pageX - (carouselRef.current?.offsetLeft || 0))
    setScrollLeft(carouselRef.current?.scrollLeft || 0)
  }

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isDragging) return
    const x = e.touches[0].pageX - (carouselRef.current?.offsetLeft || 0)
    const walk = (x - startX) * 2
    if (carouselRef.current) {
      carouselRef.current.scrollLeft = scrollLeft - walk
    }
  }

  const handleTouchEnd = () => {
    setIsDragging(false)
  }

  const scrollCarousel = (direction: 'left' | 'right') => {
    if (carouselRef.current) {
      const scrollAmount = 300
      const newScrollLeft = direction === 'left' 
        ? carouselRef.current.scrollLeft - scrollAmount
        : carouselRef.current.scrollLeft + scrollAmount
      carouselRef.current.scrollTo({
        left: newScrollLeft,
        behavior: 'smooth'
      })
    }
  }

  const categories = [
    { id: 'ALL', label: '전체' },
    { id: 'SINGING', label: '콘서트' },
    { id: 'DANCING', label: '무용' },
    { id: 'OPERA', label: '오페라' }
  ]

  const filteredPerformances = selectedCategory === 'ALL'
    ? randomPerformances
    : categoryPerformances[selectedCategory as keyof typeof categoryPerformances]

  return (
    <section className="relative py-3 overflow-hidden -mt-16">
      <div className="absolute inset-0 bg-gradient-to-br from-purple-50 via-white to-indigo-50"></div>
      <div className="container relative z-10 px-4 md:px-6">
        <div className="flex flex-col gap-8">
          <div className="flex flex-col gap-2 text-center">
            <h2 className="text-4xl font-bold tracking-tight bg-gradient-to-r text-black bg-clip-text text-transparent">
              추천 공연
            </h2>
            <p className="text-lg text-muted-foreground/80">
              TICKET4U가 엄선한 <span className="font-medium text-purple-600">다양한 공연</span>을 만나보세요
            </p>
            
            {/* 카테고리 필터 */}
            <div className="flex justify-center gap-2 mt-4">
              {categories.map((category) => (
                <Button
                  key={category.id}
                  variant={selectedCategory === category.id ? "default" : "outline"}
                  className={`rounded-full px-4 ${
                    selectedCategory === category.id 
                      ? 'bg-[#894def] hover:bg-[#7a3fd8] text-white' 
                      : 'hover:bg-purple-50'
                  }`}
                  onClick={() => setSelectedCategory(category.id)}
                >
                  {category.label}
                </Button>
              ))}
            </div>
          </div>

          <div className="relative">
            {filteredPerformances.length > 0 ? (
              <>
                <div
                  ref={carouselRef}
                  className="flex gap-6 overflow-x-auto pb-8 scrollbar-hide snap-x snap-mandatory px-7"
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
                  {filteredPerformances.map((performance) => (
                    <Link 
                      key={performance.id} 
                      href={`/performances/${performance.id}`}
                      className="min-w-[300px] max-w-[300px] snap-center flex-shrink-0"
                      onClick={(e) => {
                        if (isDragging) {
                          e.preventDefault()
                        }
                      }}
                    >
                      <Card className="overflow-hidden hover:shadow-lg transition-all duration-300 hover:-translate-y-1 h-full">
                        <div className="aspect-[2/3] relative overflow-hidden">
                          <img
                            src={getPerformanceImageUrl(performance.fileUrl)}
                            alt={performance.title}
                            className="object-cover w-full h-full transition-transform duration-300 hover:scale-105"
                          />
                          <div className="absolute top-3 left-3">
                            <Badge variant="secondary" className="bg-purple-100 text-purple-700 border-purple-200">
                              {performance.category === "SINGING" ? "콘서트" : 
                               performance.category === "DANCING" ? "무용" : "오페라"}
                            </Badge>
                          </div>
                        </div>
                        <CardContent className="p-4">
                          <div className="space-y-2">
                            <h3 className="text-lg font-semibold line-clamp-1 bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent hover:from-purple-600 hover:to-indigo-600 transition-all duration-300">
                              {performance.title}
                            </h3>
                            <div className="space-y-1">
                              <div className="text-sm text-muted-foreground/80">
                                <span className="font-medium truncate">{performance.venue}</span>
                              </div>
                              <div className="text-sm text-muted-foreground/80">
                                <span>{formatKSTDate(performance.startDate)} ~ {formatKSTDate(performance.endDate)}</span>
                              </div>
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
              </>
            ) : (
              <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
                <div className="text-gray-400 mb-4">
                  <svg className="w-16 h-16 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-600 mb-2">아직 등록된 공연이 없습니다</h3>
                <p className="text-gray-500">곧 새로운 공연이 등록될 예정입니다. 조금만 기다려주세요!</p>
              </div>
            )}
          </div>

          {/* 모든 공연 보러가기 버튼 */}
          <div className="flex justify-center pt-4">
            <Button 
              size="lg" 
              className="rounded-full px-8 py-6 text-lg bg-[#894def] hover:bg-[#7a3fd8] shadow-lg hover:shadow-xl transition-all duration-300"
              asChild
            >
              <Link href="/performances">모든 공연 보러가기</Link>
            </Button>
          </div>

          {/* 공연 관리자 신청 카드 */}
          <div className="mt-8">
            <div className="bg-gray-50 rounded-xl p-[40px]">
              <div className="bg-white rounded-lg p-8 shadow-sm">
                <div className="flex flex-col md:flex-row justify-between items-center gap-6">
                  <div>
                    <h2 className="text-2xl font-bold mb-2">공연 관리자가 되어보세요</h2>
                    <p className="text-muted-foreground">
                      당신의 공연을 TICKET4U에서 홍보하고 티켓을 판매하세요. 간단한 신청 절차를 통해 시작할 수 있습니다.
                    </p>
                  </div>
                  <Button className="whitespace-nowrap bg-[#894def] hover:bg-[#7a3fd8]" asChild>
                    <Link href="/users/mypage/register">지금 신청하기</Link>
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
} 