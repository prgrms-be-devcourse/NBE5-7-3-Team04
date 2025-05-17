"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
    MapPin,
    Users,
    ArrowLeft,
    Loader2,
    Star,
    MessageCircle,
    Calendar,
    Clock,
    Heart,
} from "lucide-react";
import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent } from "@/components/ui/card";
import { ReservationCalendarModalFixed } from "@/components/reservation-calendar-modal-fixed";
import { getPerformanceDetail, getReviews, createReview } from "@/src/api/api";
import { format, parseISO, addHours } from "date-fns";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Textarea } from "@/components/ui/textarea";
import { toast } from "@/hooks/use-toast";
import { Toaster } from "@/components/ui/toaster";
import { useAuth } from "@/src/auth/user";
import { ko } from "date-fns/locale";
import { CustomCalendar } from "@/components/custom-calendar";
import { cn, getPerformanceImageUrl } from "@/lib/utils";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle } from "lucide-react";

interface PerformanceSchedule {
    id: number;
    startTime: string;
    endTime: string;
    remainingSeats: number;
    isCanceled: boolean;
}

interface Performance {
    id: number;
    title: string;
    price: number;
    totalSeats: number;
    venue: string;
    description: string;
    status: string;
    fileUrl: string;
    startDate: string;
    endDate: string;
    bookmarked: boolean;
    schedules: PerformanceSchedule[];
    category?: string;
}

interface Review {
    id: number;
    userName: string;
    scheduledId: number;
    comment: string;
}

interface ReviewsResponse {
    content: Review[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export default function PerformanceDetailClient({
    performanceId,
}: {
    performanceId: string;
}) {
    const [performance, setPerformance] = useState<Performance | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [reviews, setReviews] = useState<Review[]>([]);
    const [reviewsLoading, setReviewsLoading] = useState(false);
    const [reviewsError, setReviewsError] = useState<string | null>(null);
    const [reviewsPage, setReviewsPage] = useState(0);
    const [reviewsTotal, setReviewsTotal] = useState(0);
    const [reviewsTotalPages, setReviewsTotalPages] = useState(0);
    const [reviewComment, setReviewComment] = useState("");
    const [reviewRating, setReviewRating] = useState(5);
    const [submittingReview, setSubmittingReview] = useState(false);
    const [isBookmarked, setIsBookmarked] = useState(false);
    const { isAuthenticated } = useAuth();
    const [selectedDate, setSelectedDate] = useState<Date | undefined>(
        undefined
    );
    const [isCalendarOpen, setIsCalendarOpen] = useState(false);
    const [selectedSchedule, setSelectedSchedule] =
        useState<PerformanceSchedule | null>(null);
    const [step, setStep] = useState<"schedule" | "payment">("schedule");
    const router = useRouter();

    useEffect(() => {
        const fetchPerformance = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getPerformanceDetail(performanceId);
                setPerformance(data);
                setIsBookmarked(data.bookmarked || false);
            } catch (err) {
                console.error("Error fetching performance:", err);
                setError("공연 정보를 불러오는 중 오류가 발생했습니다.");
            } finally {
                setLoading(false);
            }
        };

        fetchPerformance();
    }, [performanceId]);

    useEffect(() => {
        const fetchReviews = async () => {
            if (!performance) return;

            try {
                setReviewsLoading(true);
                setReviewsError(null);
                const data = await getReviews(performanceId, reviewsPage);
                console.log("받아온 리뷰 데이터:", data);
                setReviews(data.content);
                setReviewsTotal(data.totalElements);
                setReviewsTotalPages(data.totalPages);
            } catch (err) {
                console.error("Error fetching reviews:", err);
                setReviewsError("리뷰를 불러오는 중 오류가 발생했습니다.");
            } finally {
                setReviewsLoading(false);
            }
        };

        fetchReviews();
    }, [performance, reviewsPage, performanceId]);

    const handleReviewSubmit = async () => {
        if (!reviewComment.trim()) {
            toast({
                title: "리뷰 내용을 입력해주세요",
                variant: "destructive",
            });
            return;
        }

        try {
            setSubmittingReview(true);

            const scheduleId = performance?.schedules[0]?.id || 0;

            await createReview({
                performanceId: Number(performanceId),
                scheduledId: scheduleId,
                comments: reviewComment,
            });

            const data = await getReviews(performanceId, 0);
            setReviews(data.content);
            setReviewsTotal(data.totalElements);
            setReviewsTotalPages(data.totalPages);
            setReviewsPage(0);

            setReviewComment("");
            setReviewRating(5);

            toast({
                title: "리뷰가 등록되었습니다",
                description: "소중한 의견 감사합니다.",
            });
        } catch (err: any) {
            console.error("Error submitting review:", err);
            toast({
                title: "리뷰 등록 실패",
                description:
                    err.message ||
                    "리뷰를 등록하는 중 오류가 발생했습니다. 다시 시도해주세요.",
                variant: "destructive",
            });
        } finally {
            setSubmittingReview(false);
        }
    };

    const handleBookmark = async () => {
        console.log("Bookmark clicked, isAuthenticated:", isAuthenticated);

        if (!isAuthenticated) {
            console.log("User is not authenticated, showing login toast");
            toast({
                title: "로그인이 필요합니다",
                description: "찜 기능을 사용하려면 로그인해주세요.",
                variant: "destructive",
                duration: 3000,
            });
            return;
        }

        try {
            console.log(
                "Toggling bookmark state from:",
                isBookmarked,
                "to:",
                !isBookmarked
            );
            // TODO: 북마크 API 연동
            setIsBookmarked(!isBookmarked);
            toast({
                title: isBookmarked
                    ? "찜 목록에서 제거되었습니다."
                    : "찜 목록에 추가되었습니다.",
                description: isBookmarked
                    ? "찜 목록에서 제거되었습니다."
                    : "찜 목록에 추가되었습니다.",
                duration: 2000,
            });
        } catch (error) {
            console.error("Error toggling bookmark:", error);
            toast({
                title: "오류가 발생했습니다.",
                description: "잠시 후 다시 시도해주세요.",
                variant: "destructive",
                duration: 3000,
            });
        }
    };

    const handleDateSelect = (date: Date) => {
        setSelectedDate(date);
        setIsCalendarOpen(true);
    };

    const handleScheduleSelect = (schedule: PerformanceSchedule) => {
        setSelectedSchedule(schedule);
        setStep("payment");
    };

    const handlePayment = () => {
        if (!isAuthenticated) {
            router.push("/login");
            return;
        }
        if (!selectedSchedule) return;
        // 결제 페이지로 이동
        router.push(
            `/reservations/new?performanceId=${performanceId}&scheduleId=${selectedSchedule.id}`
        );
    };

    if (loading) {
        return (
            <div className="container py-8 flex items-center justify-center min-h-[50vh]">
                <div className="flex flex-col items-center">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    <p className="mt-4 text-muted-foreground">
                        공연 정보를 불러오는 중...
                    </p>
                </div>
            </div>
        );
    }

    if (error || !performance) {
        return (
            <div className="container py-8 flex items-center justify-center min-h-[50vh]">
                <div className="flex flex-col items-center">
                    <AlertCircle className="h-8 w-8 text-destructive" />
                    <p className="mt-4 text-destructive font-medium">
                        {error || "공연 정보를 불러올 수 없습니다."}
                    </p>
                    <Button variant="outline" className="mt-4" asChild>
                        <Link href="/performances">목록으로 돌아가기</Link>
                    </Button>
                </div>
            </div>
        );
    }

    // 공연 상태에 따른 배지 스타일 결정
    const getStatusVariant = (status: string) => {
        switch (status) {
            case "CONFIRMED":
                return "success";
            case "PENDING":
                return "secondary";
            case "REJECTED":
            case "CANCELLED":
                return "destructive";
            default:
                return "outline";
        }
    };

    const statusVariant = getStatusVariant(performance.status);
    const statusText =
        {
            PENDING: "승인 대기중",
            CONFIRMED: "예매가능",
            REJECTED: "거절됨",
            CANCELLED: "취소됨",
            COMPLETED: "종료됨",
        }[performance.status] || performance.status;

    // 총 회차 수와 매진된 회차 수 계산
    const activeSchedules = performance.schedules.filter(
        (schedule) => !schedule.isCanceled
    );
    const totalSessions = activeSchedules.length;
    const soldOutSessions = activeSchedules.filter(
        (session) => session.remainingSeats === 0
    ).length;

    // 카테고리 표시 (API에서 제공하는 경우)
    const categories = performance.category ? [performance.category] : [];

    // KST로 시간 변환
    const formatTimeToKST = (dateString: string) => {
        try {
            const date = parseISO(dateString);
            const kstDate = addHours(date, 9); // UTC to KST
            return format(kstDate, "yyyy.MM.dd HH:mm", { locale: ko });
        } catch (error) {
            return "날짜 정보 없음";
        }
    };

    // 리뷰 날짜 포맷팅
    const formatReviewDate = (dateString: string | undefined) => {
        console.log("리뷰 날짜 원본:", dateString);

        if (!dateString) {
            console.log("날짜 문자열이 없음");
            return "날짜 정보 없음";
        }

        try {
            const date = parseISO(dateString);
            console.log("파싱된 날짜:", date);
            const formattedDate = format(date, "yyyy년 MM월 dd일", {
                locale: ko,
            });
            console.log("포맷된 날짜:", formattedDate);
            return formattedDate;
        } catch (error) {
            console.error("날짜 파싱 오류:", error);
            return "날짜 정보 없음";
        }
    };

    // 러닝타임 계산
    const calculateRunningTime = (startTime: string, endTime: string) => {
        try {
            const start = parseISO(startTime);
            const end = parseISO(endTime);
            const diffMinutes = Math.round(
                (end.getTime() - start.getTime()) / (1000 * 60)
            );
            return `${diffMinutes}분`;
        } catch (error) {
            return "시간 정보 없음";
        }
    };

    // 별점 렌더링 함수
    const renderStars = (rating: number) => {
        return Array(5)
            .fill(0)
            .map((_, i) => (
                <Star
                    key={i}
                    className={`h-4 w-4 ${
                        i < rating
                            ? "fill-yellow-400 text-yellow-400"
                            : "text-gray-300"
                    }`}
                />
            ));
    };

    // 랜덤 이미지 URL 생성
    const getRandomImage = () => {
        const images = [
            "/placeholder-1.jpg",
            "/placeholder-2.jpg",
            "/placeholder-3.jpg",
        ];
        return images[Math.floor(Math.random() * images.length)];
    };

    return (
        <>
            <Toaster />
            <div className="container mx-auto px-4 py-8">
                <div className="flex gap-8">
                    {/* 좌측 + 중앙 섹션 */}
                    <div className="flex-1 min-w-0">
                        <div className="flex gap-8">
                            {/* 좌측 섹션 - 이미지 */}
                            <div className="w-[300px] flex-shrink-0">
                                <div className="relative aspect-[2/3] w-full">
                                    <img
                                        src={getPerformanceImageUrl(
                                            performance.fileUrl
                                        )}
                                        alt={performance.title}
                                        className="object-cover rounded-lg w-full h-full"
                                    />
                                </div>
                            </div>

                            {/* 중앙 섹션 - 정보 */}
                            <div className="flex-1 min-w-0">
                                <div className="space-y-6">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <h2 className="text-2xl font-bold">
                                                {performance.title}
                                            </h2>
                                            <div className="flex items-center gap-2 mt-2">
                                                <Badge
                                                    variant={getStatusVariant(
                                                        performance.status
                                                    )}
                                                >
                                                    {performance.status ===
                                                    "CONFIRMED"
                                                        ? "예매중"
                                                        : performance.status ===
                                                          "COMPLETED"
                                                        ? "종료"
                                                        : "예매예정"}
                                                </Badge>
                                                {performance.category && (
                                                    <Badge variant="outline">
                                                        {performance.category}
                                                    </Badge>
                                                )}
                                            </div>
                                        </div>
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={handleBookmark}
                                            className={cn(
                                                "h-10 w-10",
                                                isBookmarked &&
                                                    "text-primary hover:text-primary/90"
                                            )}
                                        >
                                            <Heart
                                                className={cn(
                                                    "h-5 w-5",
                                                    isBookmarked
                                                        ? "fill-current"
                                                        : "fill-none"
                                                )}
                                            />
                                        </Button>
                                    </div>

                                    <Card>
                                        <CardContent className="pt-6">
                                            <div className="space-y-4">
                                                <div className="flex items-center gap-2">
                                                    <div className="h-5 w-5 text-muted-foreground flex items-center justify-center">
                                                        <Calendar className="h-4 w-4" />
                                                    </div>
                                                    <div>
                                                        <div className="text-sm font-medium">
                                                            공연 기간
                                                        </div>
                                                        <div className="text-sm text-muted-foreground">
                                                            {formatTimeToKST(
                                                                performance.startDate
                                                            )}{" "}
                                                            ~{" "}
                                                            {formatTimeToKST(
                                                                performance.endDate
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-2">
                                                    <div className="h-5 w-5 text-muted-foreground flex items-center justify-center">
                                                        <Clock className="h-4 w-4" />
                                                    </div>
                                                    <div>
                                                        <div className="text-sm font-medium">
                                                            러닝타임
                                                        </div>
                                                        <div className="text-sm text-muted-foreground">
                                                            {performance
                                                                .schedules[0] &&
                                                                calculateRunningTime(
                                                                    performance
                                                                        .schedules[0]
                                                                        .startTime,
                                                                    performance
                                                                        .schedules[0]
                                                                        .endTime
                                                                )}
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-2">
                                                    <div className="h-5 w-5 text-muted-foreground flex items-center justify-center">
                                                        <MapPin className="h-4 w-4" />
                                                    </div>
                                                    <div>
                                                        <div className="text-sm font-medium">
                                                            공연장
                                                        </div>
                                                        <div className="text-sm text-muted-foreground">
                                                            {performance.venue}
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-2">
                                                    <div className="h-5 w-5 text-muted-foreground flex items-center justify-center">
                                                        ₩
                                                    </div>
                                                    <div>
                                                        <div className="text-sm font-medium">
                                                            티켓 가격
                                                        </div>
                                                        <div className="text-sm text-muted-foreground">
                                                            {performance.price.toLocaleString()}
                                                            원
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </CardContent>
                                    </Card>
                                </div>
                            </div>
                        </div>

                        {/* 하단부 섹션 */}
                        <Tabs defaultValue="info" className="mt-8">
                            <TabsList className="w-full grid grid-cols-4">
                                <TabsTrigger value="info">
                                    공연 정보
                                </TabsTrigger>
                                <TabsTrigger value="notice">
                                    유의사항
                                </TabsTrigger>
                                <TabsTrigger value="refund">
                                    환불 정책
                                </TabsTrigger>
                                <TabsTrigger value="reviews">
                                    리뷰 ({reviewsTotal})
                                </TabsTrigger>
                            </TabsList>
                            <TabsContent
                                value="info"
                                className="mt-4 space-y-6"
                            >
                                <div>
                                    <h3 className="text-lg font-semibold">
                                        공연 소개
                                    </h3>
                                    <p className="mt-2 text-muted-foreground">
                                        {performance.description}
                                    </p>
                                </div>
                                <Separator />
                                <div>
                                    <h3 className="text-lg font-semibold">
                                        공연 일정
                                    </h3>
                                    <div className="mt-2 text-muted-foreground">
                                        <p>
                                            시작일:{" "}
                                            {formatTimeToKST(
                                                performance.startDate
                                            )}
                                        </p>
                                        <p>
                                            종료일:{" "}
                                            {formatTimeToKST(
                                                performance.endDate
                                            )}
                                        </p>
                                    </div>
                                </div>
                                <Separator />
                                <div>
                                    <h3 className="text-lg font-semibold">
                                        회차 정보
                                    </h3>
                                    <div className="mt-2 space-y-2">
                                        {performance.schedules.map(
                                            (schedule) => (
                                                <div
                                                    key={schedule.id}
                                                    className="flex items-center justify-between p-2 bg-gray-50 rounded"
                                                >
                                                    <div>
                                                        <p className="font-medium">
                                                            {formatTimeToKST(
                                                                schedule.startTime
                                                            )}
                                                        </p>
                                                        <p className="text-sm text-muted-foreground">
                                                            잔여석:{" "}
                                                            {
                                                                schedule.remainingSeats
                                                            }
                                                            석
                                                        </p>
                                                    </div>
                                                    {schedule.isCanceled && (
                                                        <Badge variant="destructive">
                                                            취소됨
                                                        </Badge>
                                                    )}
                                                </div>
                                            )
                                        )}
                                    </div>
                                </div>
                            </TabsContent>
                            <TabsContent value="notice" className="mt-4">
                                <Card>
                                    <CardContent className="pt-6">
                                        <h3 className="text-lg font-semibold">
                                            공연 유의사항
                                        </h3>
                                        <ul className="mt-2 list-inside list-disc text-muted-foreground">
                                            <li>
                                                공연 시작 30분 전부터 입장
                                                가능합니다.
                                            </li>
                                            <li>지정 좌석제로 운영됩니다.</li>
                                            <li>음식물 반입이 불가합니다.</li>
                                            <li>
                                                공연 중 사진 및 동영상 촬영이
                                                금지됩니다.
                                            </li>
                                            <li>
                                                미취학 아동은 입장이 제한됩니다.
                                            </li>
                                        </ul>
                                    </CardContent>
                                </Card>
                            </TabsContent>
                            <TabsContent value="refund" className="mt-4">
                                <Card>
                                    <CardContent className="pt-6">
                                        <h3 className="text-lg font-semibold">
                                            환불 정책
                                        </h3>
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
                                        <h3 className="text-lg font-semibold mb-4">
                                            관람객 리뷰
                                        </h3>

                                        {/* 리뷰 작성 폼 - 로그인한 사용자만 볼 수 있음 */}
                                        {isAuthenticated && (
                                            <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                                                <h4 className="text-sm font-medium mb-2">
                                                    리뷰 작성
                                                </h4>
                                                <Textarea
                                                    placeholder="공연에 대한 리뷰를 작성해주세요."
                                                    className="mb-2"
                                                    value={reviewComment}
                                                    onChange={(e) =>
                                                        setReviewComment(
                                                            e.target.value
                                                        )
                                                    }
                                                />
                                                <Button
                                                    onClick={handleReviewSubmit}
                                                    disabled={
                                                        submittingReview ||
                                                        !reviewComment.trim()
                                                    }
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
                                        )}

                                        {/* 리뷰 목록 */}
                                        {reviewsLoading ? (
                                            <div className="flex justify-center py-8">
                                                <Loader2 className="h-6 w-6 animate-spin text-primary" />
                                            </div>
                                        ) : reviewsError ? (
                                            <div className="text-center py-8 text-destructive">
                                                {reviewsError}
                                            </div>
                                        ) : reviews.length === 0 ? (
                                            <div className="text-center py-8 text-muted-foreground">
                                                <MessageCircle className="mx-auto h-8 w-8 mb-2 opacity-50" />
                                                <p>
                                                    아직 작성된 리뷰가 없습니다.
                                                </p>
                                                <p className="text-sm">
                                                    첫 번째 리뷰를 작성해보세요!
                                                </p>
                                            </div>
                                        ) : (
                                            <div className="space-y-4">
                                                {reviews.map((review) => (
                                                    <div
                                                        key={review.id}
                                                        className="pb-4 border-b last:border-0"
                                                    >
                                                        <div className="flex items-start gap-3">
                                                            <Avatar className="h-8 w-8">
                                                                <AvatarFallback>
                                                                    {review.userName.substring(
                                                                        0,
                                                                        2
                                                                    )}
                                                                </AvatarFallback>
                                                            </Avatar>
                                                            <div className="flex-1">
                                                                <div className="flex items-center justify-between">
                                                                    <div>
                                                                        <p className="text-sm font-medium">
                                                                            {
                                                                                review.userName
                                                                            }
                                                                        </p>
                                                                    </div>
                                                                </div>
                                                                <p className="mt-2 text-sm">
                                                                    {
                                                                        review.comment
                                                                    }
                                                                </p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            </TabsContent>
                        </Tabs>
                    </div>

                    {/* 우측 예매 섹션 */}
                    <div className="w-[400px] flex-shrink-0">
                        <Card>
                            <CardContent className="pt-6">
                                <div className="space-y-4">
                                    <div>
                                        <h3 className="text-sm font-medium mb-2">
                                            날짜 선택
                                        </h3>
                                        <div className="border rounded-lg p-4">
                                            <CustomCalendar
                                                selectedDate={selectedDate}
                                                onSelect={setSelectedDate}
                                                availableDates={performance.schedules.map(
                                                    (schedule) =>
                                                        parseISO(
                                                            schedule.startTime
                                                        )
                                                )}
                                                className="mx-auto"
                                            />
                                            <p className="text-center text-sm text-muted-foreground mt-2">
                                                초록색으로 표시된 날짜만 공연이
                                                있습니다.
                                            </p>
                                        </div>
                                    </div>

                                    {performance.status === "CONFIRMED" ? (
                                        <Button
                                            className="w-full"
                                            onClick={() =>
                                                setIsCalendarOpen(true)
                                            }
                                            disabled={!selectedDate}
                                        >
                                            {selectedDate
                                                ? "예매하기"
                                                : "날짜를 선택해주세요"}
                                        </Button>
                                    ) : (
                                        <Button className="w-full" disabled>
                                            {performance.status === "COMPLETED"
                                                ? "종료된 공연"
                                                : "예매 불가능한 공연"}
                                        </Button>
                                    )}
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </div>
            </div>

            <ReservationCalendarModalFixed
                performanceId={performanceId}
                selectedDate={selectedDate}
                isOpen={isCalendarOpen}
                onClose={() => setIsCalendarOpen(false)}
                onDateSelect={handleDateSelect}
                availableDates={performance.schedules
                    .filter((schedule) => !schedule.isCanceled)
                    .map((schedule) => parseISO(schedule.startTime))}
            />
        </>
    );
}
