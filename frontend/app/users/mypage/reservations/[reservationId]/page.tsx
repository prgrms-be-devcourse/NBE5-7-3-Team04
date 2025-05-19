"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { getReservationDetail, cancelReservation } from "@/src/api/api";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Calendar,
  Clock,
  MapPin,
  ArrowLeft,
  AlertTriangle,
  Ticket,
} from "lucide-react";
import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import { CancelReservationDialog } from "@/components/cancel-reservation-dialog";
import { getPerformanceImageUrl } from "@/src/utils/image";
import { formatKSTDateTime } from "@/src/utils/date";

export default function ReservationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const reservationId = params?.reservationId as string;
  const [reservation, setReservation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!reservationId) return;
    setLoading(true);
    getReservationDetail(reservationId)
      .then((data) => {
        console.log("reservation detail:", JSON.stringify(data, null, 2));
        setReservation(data);
      })
      .catch(() => setError("예매 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [reservationId]);

  if (loading) return <div className="p-8 text-center">로딩 중...</div>;
  if (error || !reservation)
    return (
      <div className="p-8 text-center text-red-500">
        {error || "예매 정보가 없습니다."}
      </div>
    );

  const statusVariant =
    reservation.status === "PAYMENTS_CONFIRMED"
      ? "success"
      : reservation.status === "PAYMENTS_PENDING"
      ? "secondary"
      : "destructive";

  // 상태 한글 변환 함수
  const getStatusLabel = (status: string) => {
    switch (status) {
      case "PAYMENTS_PENDING":
        return "예약 확정 대기";
      case "PAYMENTS_CONFIRMED":
        return "예약 확정";
      case "CANCEL_PENDING":
        return "취소 대기";
      case "CANCEL_CONFIRMED":
        return "취소 확정";
      default:
        return status;
    }
  };

  // 취소 버튼 비활성화 조건
  const isCancelDisabled =
    reservation.status === "CANCEL_PENDING" ||
    reservation.status === "CANCEL_CONFIRMED";

  return (
    <div className="container">
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-4">
          <Button variant="outline" size="icon" asChild>
            <Link href="/users/mypage/reservations">
              <ArrowLeft className="h-4 w-4" />
              <span className="sr-only">뒤로 가기</span>
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">
              예매 상세 정보
            </h1>
            <p className="text-muted-foreground">
              예약번호: {reservation.reservationId} |{" "}
              {formatKSTDateTime(reservation.createdAt)} 예매
            </p>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          {/* 공연 정보(설명) Card - 빨간 테두리 */}
          <Card>
            <CardHeader>
              <div className="flex flex-col gap-2">
                {/* 공연 이미지 */}
                <img
                  src={getPerformanceImageUrl(reservation.fileUrl)}
                  alt={reservation.title}
                  className="w-full h-48 object-cover rounded-md mb-2"
                />
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>
                      <a
                        href={`/performances/${reservation.performanceId}`}
                        className="hover:underline text-lg text-blue-700 font-bold"
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {reservation.title}
                      </a>
                    </CardTitle>
                    <CardDescription>예매한 공연 정보</CardDescription>
                  </div>
                  <Badge variant={statusVariant}>
                    {getStatusLabel(reservation.status)}
                  </Badge>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {reservation.description && (
                <div
                  className="mb-2 text-gray-700 text-sm truncate"
                  style={{ maxWidth: "100%" }}
                  title={reservation.description}
                >
                  {reservation.description}
                </div>
              )}
              <div className="mt-2 grid gap-2 text-sm">
                {/* 회차 시작/종료 시간 */}
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span>
                    {reservation.startTime
                      ? `시작: ${formatKSTDateTime(reservation.startTime)}`
                      : ""}
                    {reservation.endTime
                      ? ` ~ 종료: ${formatKSTDateTime(reservation.endTime)}`
                      : ""}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <MapPin className="h-4 w-4 text-muted-foreground" />
                  <div>
                    <div>{reservation.venue}</div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="flex flex-col gap-6">
            <Card>
              <CardHeader>
                <CardTitle>결제 정보</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-2 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">티켓 가격</span>
                    <span>
                      {reservation.ticketPrice?.toLocaleString()}원 x{" "}
                      {reservation.quantity}매
                    </span>
                  </div>
                  <div className="flex items-center justify-between font-bold">
                    <span>총 결제 금액</span>
                    <span>{reservation.totalPrice?.toLocaleString()}원</span>
                  </div>
                </div>
                {/* 결제 방법 등 추가 */}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>환불 정책</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center gap-2 text-amber-500">
                    <AlertTriangle className="h-4 w-4" />
                    <span className="text-sm font-medium">
                      취소 시 아래 정책에 따라 환불됩니다.
                    </span>
                  </div>
                  <ul className="list-inside list-disc text-sm text-muted-foreground">
                    <li>공연 7일 전까지: 전액 환불</li>
                    <li>공연 3일 전까지: 70% 환불</li>
                    <li>공연 1일 전까지: 50% 환불</li>
                    <li>공연 당일: 환불 불가</li>
                  </ul>
                </div>
              </CardContent>
              <CardFooter className="flex justify-end">
                {reservation.status === "PAYMENTS_PENDING" ? (
                  <Button
                    variant="destructive"
                    disabled={isCancelDisabled}
                    onClick={async () => {
                      if (window.confirm("정말 예매를 취소하시겠습니까?")) {
                        try {
                          await cancelReservation(reservation.reservationId);
                          window.location.reload();
                        } catch (e) {
                          alert(
                            "예매 취소 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                          );
                        }
                      }
                    }}
                  >
                    예매 취소
                  </Button>
                ) : (
                  <CancelReservationDialog
                    reservationId={reservation.reservationId}
                    disabled={isCancelDisabled}
                  />
                )}
              </CardFooter>
            </Card>
          </div>
        </div>
        {/* 티켓 정보 Card - 파란 테두리, 하단 단독 배치 */}
        <div className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">티켓 정보</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-row gap-3 overflow-x-auto">
                {reservation.ticketNumbers?.map(
                  (ticketNumber: string, idx: number) => (
                    <div
                      key={ticketNumber}
                      className="flex items-center gap-2 rounded-md border px-4 py-3 min-w-[160px] bg-gray-50 shadow-sm"
                    >
                      <Ticket className="h-5 w-5 text-muted-foreground" />
                      <span className="font-medium">
                        티켓 번호: {ticketNumber}
                      </span>
                    </div>
                  )
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
