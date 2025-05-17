"use client"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { MapPin, Users, ArrowLeft, Loader2, Star, MessageCircle } from "lucide-react"
import Link from "next/link"
import { Separator } from "@/components/ui/separator"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Card, CardContent } from "@/components/ui/card"
import { ReservationCalendarModalFixed } from "@/components/reservation-calendar-modal-fixed"
import { getPerformanceDetail, getReviews, createReview } from "@/lib/api"
import { format, parseISO } from "date-fns"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/components/ui/use-toast"
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import type { Performance } from "@/types"

interface PerformanceSchedule {
  id: number
  startTime: string
  endTime: string
  remainingSeats: number
  isCanceled: boolean
}

interface Review {
  id: number
  userId: number
  userName: string
  userProfileImage?: string
  rating: number
  comment: string
  createdAt: string
}

interface ReviewsResponse {
  content: Review[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export default function PerformanceDetailPage({ params }: { params: { performanceId: string } }) {
  const unwrappedParams = use(params)
  const [performance, setPerformance] = useState<Performance | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [reviewsLoading, setReviewsLoading] = useState(false)
  const [reviewsError, setReviewsError] = useState<string | null>(null)
  const [reviewsPage, setReviewsPage] = useState(0)
  const [reviewsTotal, setReviewsTotal] = useState(0)
  const [reviewsTotalPages, setReviewsTotalPages] = useState(0)
  const [reviewComment, setReviewComment] = useState("")
  const [reviewRating, setReviewRating] = useState(5)
  const [submittingReview, setSubmittingReview] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    const fetchPerformance = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getPerformanceDetail(unwrappedParams.performanceId)
        setPerformance(data)
      } catch (err) {
        console.error("Error fetching performance:", err)
        setError("공연 정보를 불러오는데 실패했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchPerformance()
  }, [unwrappedParams.performanceId])

  useEffect(() => {
    const fetchReviews = async () => {
      try {
        setReviewsLoading(true)
        setReviewsError(null)
        const data = await getReviews(unwrappedParams.performanceId, reviewsPage)
        setReviews(data.content)
        setReviewsTotal(data.totalElements)
        setReviewsTotalPages(data.totalPages)
      } catch (err) {
        console.error("Error fetching reviews:", err)
        setReviewsError("리뷰를 불러오는 중 오류가 발생했습니다.")
      } finally {
        setReviewsLoading(false)
      }
    }

    if (!loading && performance) {
      fetchReviews()
    }
  }, [unwrappedParams.performanceId, reviewsPage, loading, performance])

  const handleReviewSubmit = async () => {
    if (!reviewComment.trim()) {
      toast({
        title: "리뷰 내용을 입력해주세요",
        variant: "destructive",
      })
      return
    }

    try {
      setSubmittingReview(true)

      // 리뷰 작성 API 호출
      // 실제 API에서는 scheduleId가 필요할 수 있으므로 첫 번째 스케줄 ID를 사용하거나 사용자가 선택하도록 할 수 있음
      const scheduleId = performance?.schedules[0]?.id || 0

      await createReview({
        performanceId: Number(unwrappedParams.performanceId),
        scheduledId: scheduleId,
        comments: reviewComment,
        // API에 따라 rating 필드가 필요할 수 있음
        // rating: reviewRating
      })

      // 리뷰 작성 성공 후 리뷰 목록 새로고침
      const data = await getReviews(unwrappedParams.performanceId, 0)
      setReviews(data.content)
      setReviewsTotal(data.totalElements)
      setReviewsTotalPages(data.totalPages)
      setReviewsPage(0)

      // 입력 필드 초기화
      setReviewComment("")
      setReviewRating(5)

      toast({
        title: "리뷰가 등록되었습니다",
        description: "소중한 의견 감사합니다.",
      })
    } catch (err) {
      console.error("Error submitting review:", err)
      toast({
        title: "리뷰 등록 실패",
        description: "리뷰를 등록하는 중 오류가 발생했습니다. 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setSubmittingReview(false)
    }
  }

  if (loading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="mt-4 text-muted-foreground">공연 정보를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error || !performance) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <p className="text-destructive font-medium">{error || "공연 정보를 불러올 수 없습니다."}</p>
          <Button variant="outline" className="mt-4" asChild>
            <Link href="/">홈으로 돌아가기</Link>
          </Button>
        </div>
      </div>
    )
  }

  // 공연 상태에 따른 배지 스타일 결정
  const getStatusVariant = (status: string) => {
    switch (status) {
      case "CONFIRMED":
        return "success"
      case "PENDING":
        return "secondary"
      case "REJECTED":
      case "CANCELLED":
        return "destructive"
      default:
        return "outline"
    }
  }

  const statusVariant = getStatusVariant(performance.status)
  const statusText =
    {
      PENDING: "승인 대기중",
      CONFIRMED: "예매가능",
      REJECTED: "거절됨",
      CANCELLED: "취소됨",
      COMPLETED: "종료됨",
    }[performance.status] || performance.status

  // 총 회차 수와 매진된 회차 수 계산
  const activeSchedules = performance.schedules.filter((schedule) => !schedule.isCanceled)
  const totalSessions = activeSchedules.length
  const soldOutSessions = activeSchedules.filter((session) => session.remainingSeats === 0).length

  // 카테고리 표시 (API에서 제공하는 경우)
  const categories = performance.category ? [performance.category] : []

  // 날짜 포맷팅 함수 - 유효한 날짜 문자열인지 확인
  const formatDateSafely = (dateString: string) => {
    try {
      return format(parseISO(dateString), "yyyy년 MM월 dd일")
    } catch (error) {
      console.error("Invalid date format:", dateString)
      return "날짜 정보 없음"
    }
  }

  // 리뷰 날짜 포맷팅
  const formatReviewDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), "yyyy.MM.dd")
    } catch (error) {
      return "날짜 정보 없음"
    }
  }

  // 별점 렌더링 함수
  const renderStars = (rating: number) => {
    return Array(5)
      .fill(0)
      .map((_, i) => (
        <Star key={i} className={`h-4 w-4 ${i < rating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"}`} />
      ))
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-8">
        <div className="flex items-center gap-4">
          <Button variant="outline" size="icon" asChild>
            <Link href="/">
              <ArrowLeft className="h-4 w-4" />
              <span className="sr-only">뒤로 가기</span>
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight md:text-3xl">{performance.title}</h1>
            <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
              <Badge variant={statusVariant}>{statusText}</Badge>
              {categories.map((category) => (
                <Badge key={category} variant="outline">
                  {category}
                </Badge>
              ))}
            </div>
          </div>
        </div>

        <div className="grid gap-8 md:grid-cols-3">
          <div className="md:col-span-2">
            <div className="relative aspect-video overflow-hidden rounded-lg">
              <img
                src={performance.fileUrl || "/placeholder.svg?height=400&width=800"}
                alt={performance.title}
                className="h-full w-full object-cover"
                onError={(e) => {
                  e.currentTarget.src = "/placeholder.svg?height=400&width=800"
                }}
              />
            </div>

            <Tabs defaultValue="info" className="mt-8">
              <TabsList className="w-full grid grid-cols-4">
                <TabsTrigger value="info">공연 정보</TabsTrigger>
                <TabsTrigger value="notice">유의사항</TabsTrigger>
                <TabsTrigger value="refund">환불 정책</TabsTrigger>
                <TabsTrigger value="reviews">리뷰 ({reviewsTotal})</TabsTrigger>
              </TabsList>
              <TabsContent value="info" className="mt-4 space-y-6">
                <div>
                  <h3 className="text-lg font-semibold">공연 소개</h3>
                  <p className="mt-2 text-muted-foreground">{performance.description}</p>
                </div>
                <Separator />
                <div>
                  <h3 className="text-lg font-semibold">공연 일정</h3>
                  <div className="mt-2 text-muted-foreground">
                    <p>시작일: {formatDateSafely(performance.startDate)}</p>
                    <p>종료일: {formatDateSafely(performance.endDate)}</p>
                  </div>
                </div>
              </TabsContent>
              <TabsContent value="notice" className="mt-4">
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-semibold">공연 유의사항</h3>
                    <ul className="mt-2 list-inside list-disc text-muted-foreground">
                      <li>공연 시작 30분 전부터 입장 가능합니다.</li>
                      <li>지정 좌석제로 운영됩니다.</li>
                      <li>음식물 반입이 불가합니다.</li>
                      <li>공연 중 사진 및 동영상 촬영이 금지됩니다.</li>
                      <li>미취학 아동은 입장이 제한됩니다.</li>
                    </ul>
                  </CardContent>
                </Card>
              </TabsContent>
              <TabsContent value="refund" className="mt-4">
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-semibold">환불 정책</h3>
                    <ul className="mt-2 list-inside list-disc text-muted-foreground">
                      <li>공연 7일 전까지: 전액 환불</li>
                      <li>공연 3일 전까지: 70% 환불</li>
                      <li>공연 1일 전까지: 50% 환불</li>
                      <li>공연 당일: 환불 불가</li>
                    </ul>
                  </CardContent>
                </Card>
              </TabsContent>
              <TabsContent value="reviews" className="mt-4">
                <Card>
                  <CardContent className="pt-6">
                    <h3 className="text-lg font-semibold mb-4">관람객 리뷰</h3>

                    {/* 리뷰 작성 폼 */}
                    <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                      <h4 className="text-sm font-medium mb-2">리뷰 작성</h4>
                      <div className="flex items-center mb-2">
                        <div className="flex mr-2">
                          {Array(5)
                            .fill(0)
                            .map((_, i) => (
                              <Star
                                key={i}
                                className={`h-5 w-5 cursor-pointer ${
                                  i < reviewRating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"
                                }`}
                                onClick={() => setReviewRating(i + 1)}
                              />
                            ))}
                        </div>
                        <span className="text-sm text-muted-foreground">{reviewRating}/5</span>
                      </div>
                      <Textarea
                        placeholder="공연에 대한 리뷰를 작성해주세요."
                        className="mb-2"
                        value={reviewComment}
                        onChange={(e) => setReviewComment(e.target.value)}
                      />
                      <Button
                        onClick={handleReviewSubmit}
                        disabled={submittingReview || !reviewComment.trim()}
                        className="w-full"
                      >
                        {submittingReview ? (
                          <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            등록 중...
                          </>
                        ) : (
                          "리뷰 등록하기"
                        )}
                      </Button>
                    </div>

                    {/* 리뷰 목록 */}
                    {reviewsLoading ? (
                      <div className="flex justify-center py-8">
                        <Loader2 className="h-6 w-6 animate-spin text-primary" />
                      </div>
                    ) : reviewsError ? (
                      <div className="text-center py-8 text-destructive">{reviewsError}</div>
                    ) : reviews.length === 0 ? (
                      <div className="text-center py-8 text-muted-foreground">
                        <MessageCircle className="mx-auto h-8 w-8 mb-2 opacity-50" />
                        <p>아직 작성된 리뷰가 없습니다.</p>
                        <p className="text-sm">첫 번째 리뷰를 작성해보세요!</p>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {reviews.map((review) => (
                          <div key={review.id} className="pb-4 border-b last:border-0">
                            <div className="flex items-start gap-3">
                              <Avatar className="h-8 w-8">
                                <AvatarImage
                                  src={review.userProfileImage || "/placeholder.svg"}
                                  alt={review.userName}
                                />
                                <AvatarFallback>{review.userName.substring(0, 2)}</AvatarFallback>
                              </Avatar>
                              <div className="flex-1">
                                <div className="flex items-center justify-between">
                                  <div>
                                    <p className="text-sm font-medium">{review.userName}</p>
                                    <div className="flex items-center gap-2">
                                      <div className="flex">{renderStars(review.rating)}</div>
                                      <span className="text-xs text-muted-foreground">
                                        {formatReviewDate(review.createdAt)}
                                      </span>
                                    </div>
                                  </div>
                                </div>
                                <p className="mt-2 text-sm">{review.comment}</p>
                              </div>
                            </div>
                          </div>
                        ))}

                        {/* 페이지네이션 */}
                        {reviewsTotalPages > 1 && (
                          <Pagination className="mt-6">
                            <PaginationContent>
                              <PaginationItem>
                                <PaginationPrevious
                                  onClick={() => setReviewsPage(Math.max(0, reviewsPage - 1))}
                                  className={reviewsPage === 0 ? "pointer-events-none opacity-50" : ""}
                                />
                              </PaginationItem>
                              {Array.from({ length: Math.min(5, reviewsTotalPages) }, (_, i) => {
                                // 현재 페이지 주변의 페이지 번호만 표시
                                const pageNum =
                                  reviewsPage < 2
                                    ? i
                                    : reviewsPage > reviewsTotalPages - 3
                                      ? reviewsTotalPages - 5 + i
                                      : reviewsPage - 2 + i

                                if (pageNum >= 0 && pageNum < reviewsTotalPages) {
                                  return (
                                    <PaginationItem key={pageNum}>
                                      <PaginationLink
                                        isActive={pageNum === reviewsPage}
                                        onClick={() => setReviewsPage(pageNum)}
                                      >
                                        {pageNum + 1}
                                      </PaginationLink>
                                    </PaginationItem>
                                  )
                                }
                                return null
                              })}
                              <PaginationItem>
                                <PaginationNext
                                  onClick={() => setReviewsPage(Math.min(reviewsTotalPages - 1, reviewsPage + 1))}
                                  className={
                                    reviewsPage === reviewsTotalPages - 1 ? "pointer-events-none opacity-50" : ""
                                  }
                                />
                              </PaginationItem>
                            </PaginationContent>
                          </Pagination>
                        )}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>

          <div className="flex flex-col gap-6">
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-4">
                  <div className="flex items-center gap-2">
                    <MapPin className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <div className="text-sm font-medium">공연 장소</div>
                      <div className="text-sm text-muted-foreground">{performance.venue}</div>
                    </div>
                  </div>

                  <Separator />

                  <div className="flex items-center gap-2">
                    <Users className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <div className="text-sm font-medium">회차 정보</div>
                      <div className="text-sm text-muted-foreground">
                        총 {totalSessions}회차 중 {soldOutSessions}회차 매진
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        * 예매 버튼을 클릭하여 날짜와 회차를 선택해주세요.
                      </div>
                    </div>
                  </div>

                  <Separator />

                  <div className="flex items-center gap-2">
                    <div className="h-5 w-5 text-muted-foreground flex items-center justify-center">₩</div>
                    <div>
                      <div className="text-sm font-medium">티켓 가격</div>
                      <div className="text-sm text-muted-foreground">{performance.price.toLocaleString()}원</div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {performance.status === "CONFIRMED" ? (
              <ReservationCalendarModalFixed performanceId={performance.id} />
            ) : (
              <Button className="w-full" disabled>
                {performance.status === "COMPLETED" ? "종료된 공연" : "예매 불가능한 공연"}
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
