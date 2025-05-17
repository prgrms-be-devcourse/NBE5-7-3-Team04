"use client"

import { useState, useEffect, useRef, type MouseEvent, type TouchEvent } from "react"
import { Button } from "@/components/ui/button"
import { PerformanceCard } from "@/components/performance-card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import Link from "next/link"
import Image from "next/image"
import { ChevronLeft, ChevronRight } from "lucide-react"

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

// 샘플 데이터
const performances = [
  {
    id: "1",
    title: "클래식 오케스트라 - 베토벤 시리즈",
    date: "2025-09-15",
    time: "18:00 - 22:00",
    location: "롯데콘서트홀",
    price: 50000,
    image: "/placeholder.svg?height=300&width=400",
    category: "클래식",
    status: "예매가능" as const,
  },
  {
    id: "2",
    title: "재즈 페스티벌 2025",
    date: "2025-08-22",
    time: "19:30 - 21:30",
    location: "올림픽공원",
    price: 70000,
    image: "/placeholder.svg?height=300&width=400",
    category: "재즈",
    status: "매진임박" as const,
  },
  {
    id: "3",
    title: "국립국악원 특별공연",
    date: "2025-10-01",
    time: "20:00 - 23:00",
    location: "국립국악원",
    price: 35000,
    image: "/placeholder.svg?height=300&width=400",
    category: "국악",
    status: "예매가능" as const,
  },
  {
    id: "4",
    title: "현대무용 - 빛과 그림자",
    date: "2025-11-05",
    time: "17:00 - 19:00",
    location: "LG아트센터",
    price: 40000,
    image: "/placeholder.svg?height=300&width=400",
    category: "무용",
    status: "예매가능" as const,
  },
  {
    id: "5",
    title: "팝 콘서트",
    date: "2023-07-25",
    time: "19:00 - 22:00",
    location: "고척 스카이돔",
    price: 60000,
    image: "/placeholder.svg?height=300&width=400",
    category: "팝",
    status: "매진" as const,
  },
  {
    id: "6",
    title: "록 페스티벌",
    date: "2023-08-05",
    time: "14:00 - 22:00",
    location: "난지 한강공원",
    price: 55000,
    image: "/placeholder.svg?height=300&width=400",
    category: "록",
    status: "예매가능" as const,
  },
  {
    id: "7",
    title: "힙합 공연",
    date: "2023-07-10",
    time: "20:00 - 23:00",
    location: "YES24 라이브홀",
    price: 45000,
    image: "/placeholder.svg?height=300&width=400",
    category: "힙합",
    status: "예매가능" as const,
  },
  {
    id: "8",
    title: "뮤지컬 '레미제라블'",
    date: "2023-09-01",
    time: "19:30 - 22:30",
    location: "블루스퀘어",
    price: 120000,
    image: "/placeholder.svg?height=300&width=400",
    category: "뮤지컬",
    status: "매진임박" as const,
  },
]

export default function Home() {
  const [currentSlide, setCurrentSlide] = useState(0)
  const [isAnimating, setIsAnimating] = useState(false)
  const [recommendedPerformances, setRecommendedPerformances] = useState<typeof performances>([])
  const carouselRef = useRef<HTMLDivElement>(null)

  // 드래그 관련 상태
  const [isDragging, setIsDragging] = useState(false)
  const [startX, setStartX] = useState(0)
  const [scrollLeft, setScrollLeft] = useState(0)
  const [clickStartTime, setClickStartTime] = useState(0)
  const [clickStartPosition, setClickStartPosition] = useState({ x: 0, y: 0 })

  // 추천 공연 선택 (랜덤)
  useEffect(() => {
    // 공연 목록에서 랜덤으로 4-6개 선택
    const shuffled = [...performances].sort(() => 0.5 - Math.random())
    const selected = shuffled.slice(0, Math.floor(Math.random() * 3) + 4) // 4-6개 선택
    setRecommendedPerformances(selected)
  }, [])

  // 자동 슬라이드 기능
  useEffect(() => {
    const interval = setInterval(() => {
      nextSlide()
    }, 5000) // 5초마다 슬라이드 변경

    return () => clearInterval(interval)
  }, [currentSlide])

  const nextSlide = () => {
    if (isAnimating) return

    setIsAnimating(true)
    setCurrentSlide((prev) => (prev === sliderData.length - 1 ? 0 : prev + 1))

    // 애니메이션 완료 후 상태 초기화
    setTimeout(() => {
      setIsAnimating(false)
    }, 500)
  }

  const prevSlide = () => {
    if (isAnimating) return

    setIsAnimating(true)
    setCurrentSlide((prev) => (prev === 0 ? sliderData.length - 1 : prev - 1))

    // 애니메이션 완료 후 상태 초기화
    setTimeout(() => {
      setIsAnimating(false)
    }, 500)
  }

  // 추천 공연 캐러셀 스크롤
  const scrollCarousel = (direction: "left" | "right") => {
    if (!carouselRef.current) return

    const scrollAmount = 300 // 스크롤 양
    const currentScroll = carouselRef.current.scrollLeft

    carouselRef.current.scrollTo({
      left: direction === "left" ? currentScroll - scrollAmount : currentScroll + scrollAmount,
      behavior: "smooth",
    })
  }

  // 마우스 드래그 이벤트 핸들러
  const handleMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (!carouselRef.current) return

    setIsDragging(true)
    setStartX(e.pageX - carouselRef.current.offsetLeft)
    setScrollLeft(carouselRef.current.scrollLeft)
    setClickStartTime(Date.now())
    setClickStartPosition({ x: e.pageX, y: e.pageY })
  }

  const handleMouseMove = (e: MouseEvent<HTMLDivElement>) => {
    if (!isDragging || !carouselRef.current) return

    e.preventDefault()
    const x = e.pageX - carouselRef.current.offsetLeft
    const walk = (x - startX) * 2 // 스크롤 속도 조절
    carouselRef.current.scrollLeft = scrollLeft - walk
  }

  const handleMouseUp = (e: MouseEvent<HTMLDivElement>) => {
    setIsDragging(false)

    // 클릭 이벤트 처리 (드래그가 아닌 경우에만)
    const clickDuration = Date.now() - clickStartTime
    const moveDistance = Math.sqrt(
      Math.pow(e.pageX - clickStartPosition.x, 2) + Math.pow(e.pageY - clickStartPosition.y, 2),
    )

    // 짧은 시간 내에 적은 움직임이 있었다면 클릭으로 간주
    if (clickDuration < 200 && moveDistance < 10) {
      // 클릭 이벤트 처리는 각 요소의 onClick에서 처리됨
    }
  }

  const handleMouseLeave = () => {
    setIsDragging(false)
  }

  // 터치 이벤트 핸들러
  const handleTouchStart = (e: TouchEvent<HTMLDivElement>) => {
    if (!carouselRef.current || e.touches.length !== 1) return

    setIsDragging(true)
    setStartX(e.touches[0].pageX - carouselRef.current.offsetLeft)
    setScrollLeft(carouselRef.current.scrollLeft)
    setClickStartTime(Date.now())
    setClickStartPosition({ x: e.touches[0].pageX, y: e.touches[0].pageY })
  }

  const handleTouchMove = (e: TouchEvent<HTMLDivElement>) => {
    if (!isDragging || !carouselRef.current || e.touches.length !== 1) return

    const x = e.touches[0].pageX - carouselRef.current.offsetLeft
    const walk = (x - startX) * 2
    carouselRef.current.scrollLeft = scrollLeft - walk
  }

  const handleTouchEnd = (e: TouchEvent<HTMLDivElement>) => {
    setIsDragging(false)

    // 터치 클릭 이벤트 처리 (드래그가 아닌 경우에만)
    if (e.changedTouches.length === 1) {
      const touchDuration = Date.now() - clickStartTime
      const moveDistance = Math.sqrt(
        Math.pow(e.changedTouches[0].pageX - clickStartPosition.x, 2) +
          Math.pow(e.changedTouches[0].pageY - clickStartPosition.y, 2),
      )

      // 짧은 시간 내에 적은 움직임이 있었다면 탭으로 간주
      if (touchDuration < 200 && moveDistance < 10) {
        // 탭 이벤트 처리는 각 요소의 onClick에서 처리됨
      }
    }
  }

  return (
    <div className="flex flex-col gap-8">
      {/* 히어로 섹션 */}
      <section className="relative w-full py-[30px] overflow-hidden">
        {/* 흰색+연한 분홍/보라 그라데이션 배경 */}
        {/* <div className="absolute inset-0 bg-white z-0" /> */}
        {/* <div className="absolute inset-0 bg-gradient-to-tr from-white via-[#fff0f3] to-[#f3e8ff] z-10" /> */}
        {/* (포인트) 좌하단/우상단 원형 그라데이션 */}
        <div
          className="absolute left-[-200px] bottom-[-150px] w-[600px] h-[500px] rounded-full opacity-60 blur-3xl pointer-events-none z-0"
          style={{ background: 'radial-gradient(circle,rgb(215, 210, 232),rgb(164, 138, 210),rgb(109, 49, 199))' }}
        />
        <div
          className="absolute right-[-150px] top-[-110px] w-[600px] h-[500px] rounded-full opacity-40 blur-3xl pointer-events-none z-0"
          style={{ background: 'radial-gradient(circle,rgb(213, 190, 229),rgb(200, 158, 216),rgb(134, 53, 168))' }}
        />
        <div className="container relative flex flex-col md:flex-row items-center justify-center gap-8 z-10" style={{ fontFamily: 'Pretendard' }}>
          
          {/* 캐러셀 */}
          <div className="flex-[4] min-w-[320px] w-full max-w-[800px] mx-auto my-0">
            <div className="relative w-full aspect-[16/9] bg-gray-200 flex items-center justify-center overflow-hidden rounded-xl shadow-lg backdrop-blur-lg bg-white/10" style={{ maxWidth: '800px', minHeight: '300px' }}>
              {sliderData.map((slide, index) => (
                <div
                  key={slide.id}
                  className={`absolute inset-0 transition-opacity duration-500 ease-in-out ${
                    index === currentSlide ? "opacity-100" : "opacity-0 pointer-events-none"
                  }`}
                >
                  <Link href={`/performances/${slide.id}`} className="block w-full h-full">
                    <Image
                      src={slide.image || "/placeholder.svg"}
                      alt={slide.title}
                      width={800}
                      height={450}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 flex flex-col justify-end p-4 bg-gradient-to-t from-black/70 to-transparent">
                      <div className="bg-purple-500 text-white text-[10px] px-2 py-1 rounded-md w-fit mb-1">
                        {slide.category}
                      </div>
                      <h2 className="text-xl font-bold text-white mb-1">{slide.title}</h2>
                      <p className="text-xs text-white mb-1">
                        {slide.venue} | {slide.date}
                      </p>
                      <p className="text-xs text-white mb-2">{slide.description}</p>
                      <Button className="w-fit text-xs py-1 px-3 h-7">예매하기</Button>
                    </div>
                  </Link>
                </div>
              ))}
              {/* 캐러셀 인디케이터 */}
              <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
                {sliderData.map((_, index) => (
                  <button
                    key={index}
                    className={`w-1.5 h-1.5 rounded-full transition-colors ${
                      index === currentSlide ? "bg-white" : "bg-white/40"
                    }`}
                    onClick={() => setCurrentSlide(index)}
                    aria-label={`슬라이드 ${index + 1}로 이동`}
                  />
                ))}
              </div>
            </div>
          </div>
          {/* 캐러셀 버튼 */}
          <div className="absolute top-1/2 -translate-y-1/2 left-0 flex justify-between w-full px-4">
            <Button
              size="icon"
              variant="outline"
              className="bg-black/20 border-white/20 text-white rounded-full h-8 w-8"
              onClick={(e) => {
                e.stopPropagation()
                prevSlide()
              }}
              disabled={isAnimating}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="h-4 w-4"
              >
                <path d="m15 18-6-6 6-6" />
              </svg>
              <span className="sr-only">이전</span>
            </Button>
            <Button
              size="icon"
              variant="outline"
              className="bg-black/20 border-white/20 text-white rounded-full h-8 w-8"
              onClick={(e) => {
                e.stopPropagation()
                nextSlide()
              }}
              disabled={isAnimating}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="h-4 w-4"
              >
                <path d="m9 18 6-6-6-6" />
              </svg>
              <span className="sr-only">다음</span>
            </Button>
          </div>
        </div>
      </section>
      {/* 추천 공연 섹션 */}
      <section className="container py-8 px-4 md:px-6">
        <div className="flex flex-col gap-6">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold tracking-tight">추천 공연</h2>
            <div className="flex gap-2">
              <Button size="icon" variant="outline" className="rounded-full" onClick={() => scrollCarousel("left")}> <ChevronLeft className="h-4 w-4" /> <span className="sr-only">이전</span> </Button>
              <Button size="icon" variant="outline" className="rounded-full" onClick={() => scrollCarousel("right")}> <ChevronRight className="h-4 w-4" /> <span className="sr-only">다음</span> </Button>
            </div>
          </div>
          <div
            ref={carouselRef}
            className={`flex gap-4 overflow-x-auto pb-4 scrollbar-hide snap-x snap-mandatory select-none ${isDragging ? "cursor-grabbing" : "cursor-grab"}`}
            style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseLeave}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
          >
            {recommendedPerformances.map((performance) => (
              <div
                key={performance.id}
                className="min-w-[280px] snap-start"
                onClick={(e) => {
                  if (isDragging) {
                    e.preventDefault()
                  }
                }}
              >
                <PerformanceCard performance={performance} />
              </div>
            ))}
          </div>
        </div>
      </section>
      {/* 공연 목록 섹션 */}
      <section className="container py-8 px-4 md:px-6 pb-0">
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
              <TabsTrigger value="musical">뮤지컬</TabsTrigger>
              <TabsTrigger value="classic">클래식</TabsTrigger>
              <TabsTrigger value="festival">페스티벌</TabsTrigger>
            </TabsList>
            <TabsContent value="all" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances.slice(0, 4).map((performance) => (
                  <PerformanceCard key={performance.id} performance={performance} />
                ))}
              </div>
            </TabsContent>
            <TabsContent value="concert" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => ["팝", "록", "힙합", "인디"].includes(p.category))
                  .map((performance) => (
                    <PerformanceCard key={performance.id} performance={performance} />
                  ))}
              </div>
            </TabsContent>
            <TabsContent value="musical" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => p.category === "뮤지컬")
                  .map((performance) => (
                    <PerformanceCard key={performance.id} performance={performance} />
                  ))}
              </div>
            </TabsContent>
            <TabsContent value="classic" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => ["클래식", "국악"].includes(p.category))
                  .map((performance) => (
                    <PerformanceCard key={performance.id} performance={performance} />
                  ))}
              </div>
            </TabsContent>
            <TabsContent value="festival" className="mt-0">
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
                {performances
                  .filter((p) => p.category === "재즈")
                  .map((performance) => (
                    <PerformanceCard key={performance.id} performance={performance} />
                  ))}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </section>
      {/* 공연 관리자 신청 섹션 */}
      <section className="py-12 pt-0">
        <div className="container px-4 md:px-6">
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
      </section>
    </div>
  );
}