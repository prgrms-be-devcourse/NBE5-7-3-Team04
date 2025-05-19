"use client";

import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { getReservations } from "../../../../src/api/reservation";
import { getImageUrl } from "../../../../src/utils/image";
import type { ReservationResponse } from "../../../../src/types/reservation";
import { useRouter } from "next/navigation";

export default function ReservationsPage() {
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchReservations = async () => {
      try {
        setLoading(true);
        const { content } = await getReservations();
        setReservations(content);
      } catch (err) {
        console.error("Error fetching reservations:", err);
        setError("예약 목록을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchReservations();
  }, []);

  // 상세페이지와 동일한 한글 변환 함수 + 색상 클래스 반환
  const getStatusLabelAndClass = (status: string) => {
    switch (status) {
      case "PAYMENTS_PENDING":
        return {
          label: "예약 확정 대기",
          className: "bg-yellow-50 text-yellow-700 border-yellow-200",
        };
      case "PAYMENTS_CONFIRMED":
        return {
          label: "예약 확정",
          className: "bg-green-50 text-green-700 border-green-200",
        };
      case "CANCEL_PENDING":
      case "CANCEL_CONFIRMED":
        return {
          label: status === "CANCEL_PENDING" ? "취소 대기" : "취소 확정",
          className: "bg-red-50 text-red-700 border-red-200",
        };
      default:
        return { label: status, className: "" };
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">예약 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="container py-8">
      <h1 className="text-3xl font-bold mb-8">내 예약 목록</h1>
      <div className="grid gap-6">
        {reservations.length === 0 ? (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <p className="text-muted-foreground mb-4">
                아직 예약한 공연이 없습니다.
              </p>
              <Button onClick={() => router.push("/performances")}>
                공연 둘러보기
              </Button>
            </CardContent>
          </Card>
        ) : (
          reservations.map((reservation) => (
            <Card key={reservation.reservationId}>
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-xl">
                      {reservation.title}
                    </CardTitle>
                    <CardDescription className="mt-1">
                      {new Date(reservation.createdAt).toLocaleString()}
                    </CardDescription>
                  </div>
                  {(() => {
                    const { label, className } = getStatusLabelAndClass(
                      reservation.status
                    );
                    return (
                      <Badge variant="outline" className={className}>
                        {label}
                      </Badge>
                    );
                  })()}
                </div>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4">
                  <div className="flex-1">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm text-muted-foreground">공연장</p>
                        <p className="font-medium">{reservation.venue}</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">수량</p>
                        <p className="font-medium">{reservation.quantity}매</p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">
                          티켓 가격
                        </p>
                        <p className="font-medium">
                          {typeof reservation.ticketPrice === "number" &&
                          !isNaN(reservation.ticketPrice)
                            ? `${reservation.ticketPrice.toLocaleString()}원`
                            : "-"}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-muted-foreground">
                          총 결제 금액
                        </p>
                        <p className="font-medium">
                          {typeof reservation.totalPrice === "number" &&
                          !isNaN(reservation.totalPrice)
                            ? `${reservation.totalPrice.toLocaleString()}원`
                            : "-"}
                        </p>
                      </div>
                    </div>
                  </div>
                  <Separator />
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      onClick={() =>
                        router.push(
                          `/users/mypage/reservations/${reservation.reservationId}`
                        )
                      }
                    >
                      예매 내역 상세보기
                    </Button>
                    {reservation.status === "PENDING" && (
                      <Button variant="destructive">예약 취소</Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
}
