"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle2, Ticket, Calendar, Clock, MapPin } from "lucide-react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { formatKSTDateTime } from "@/src/api/utils/date"

interface ReservationData {
  reservationId: number;
  title: string;
  venue: string;
  quantity: number;
  status: string;
  createdAt: string;
  expirationAt: string;
  ticketPrice: number;
  totalPrice: number;
  ticketNumbers: string[];
}

export default function ReservationCompletePage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [reservationData, setReservationData] = useState<ReservationData | null>(null);

  useEffect(() => {
    const data = searchParams.get("data");
    if (data) {
      try {
        setReservationData(JSON.parse(decodeURIComponent(data)));
      } catch (error) {
        console.error("Failed to parse reservation data:", error);
      }
    }
  }, [searchParams]);

  if (!reservationData) {
    return (
      <div className="container mx-auto py-8">
        <div className="text-center">예약 정보를 불러오는 중...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center mb-8">
          <CheckCircle2 className="w-16 h-16 text-green-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold mb-2">예약이 완료되었습니다!</h1>
          <p className="text-muted-foreground">
            예약 내역은 마이페이지에서도 확인하실 수 있습니다.
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>예약 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-2">
              <Ticket className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="font-medium">{reservationData.title}</p>
                <p className="text-sm text-muted-foreground">
                  예약 번호: {reservationData.reservationId}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <MapPin className="w-5 h-5 text-muted-foreground" />
              <p>{reservationData.venue}</p>
            </div>

            <div className="flex items-center gap-2">
              <Calendar className="w-5 h-5 text-muted-foreground" />
              <p>
                {formatKSTDateTime(reservationData.createdAt)}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-muted-foreground" />
              <p>
                {formatKSTDateTime(reservationData.expirationAt)} 까지 결제
              </p>
            </div>

            <div className="border-t pt-4">
              <div className="flex justify-between mb-2">
                <span>티켓 가격</span>
                <span>{reservationData.ticketPrice.toLocaleString()}원</span>
              </div>
              <div className="flex justify-between mb-2">
                <span>수량</span>
                <span>{reservationData.quantity}매</span>
              </div>
              <div className="flex justify-between font-bold">
                <span>총 결제 금액</span>
                <span>{reservationData.totalPrice.toLocaleString()}원</span>
              </div>
            </div>

            <div className="pt-4">
              <p className="font-medium mb-2">티켓 번호</p>
              <div className="space-y-1">
                {reservationData.ticketNumbers.map((number, index) => (
                  <p key={index} className="text-sm text-muted-foreground">
                    {number}
                  </p>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="mt-8 flex justify-center gap-4">
          <Button variant="outline" onClick={() => router.push("/")}>
            메인으로
          </Button>
          <Button onClick={() => router.push("/users/mypage/reservations")}>
            예약 내역 보기
          </Button>
        </div>
      </div>
    </div>
  );
} 