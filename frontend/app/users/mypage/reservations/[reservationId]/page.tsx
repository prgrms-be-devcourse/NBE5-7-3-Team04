"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { getReservationDetail } from "@/src/api/api"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, ArrowLeft, AlertTriangle, Ticket } from "lucide-react"
import Link from "next/link"
import { Separator } from "@/components/ui/separator"

export default function ReservationDetailPage() {
  const params = useParams()
  const router = useRouter()
  const reservationId = params?.reservationId as string
  const [reservation, setReservation] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!reservationId) return
    setLoading(true)
    getReservationDetail(reservationId)
      .then(setReservation)
      .catch(() => setError("예매 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false))
  }, [reservationId])

  if (loading) return <div className="p-8 text-center">로딩 중...</div>
  if (error || !reservation) return <div className="p-8 text-center text-red-500">{error || "예매 정보가 없습니다."}</div>

  const statusVariant =
    reservation.status === "PAYMENTS_CONFIRMED"
      ? "success"
      : reservation.status === "PAYMENTS_PENDING"
      ? "secondary"
      : "destructive"

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
            <h1 className="text-2xl font-bold tracking-tight">예매 상세 정보</h1>
            <p className="text-muted-foreground">
              예약번호: {reservation.reservationId} | {reservation.createdAt} 예매
            </p>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>공연 정보</CardTitle>
                  <CardDescription>예매한 공연 정보</CardDescription>
                </div>
                <Badge variant={statusVariant}>{reservation.status}</Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold">{reservation.title}</h3>
                <div className="mt-2 grid gap-2 text-sm">
                  {/* 공연 날짜, 시간, 장소 등은 reservation에 필드가 있으면 표시 */}
                  {/* <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <span>{reservation.performanceDate}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-muted-foreground" />
                    <span>{reservation.performanceTime}</span>
                  </div> */}
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-muted-foreground" />
                    <div>
                      <div>{reservation.venue}</div>
                    </div>
                  </div>
                </div>
              </div>
              <Separator />
              {/* 티켓 정보: 예매 수량만큼 반복 출력 */}
              <div>
                <h3 className="font-medium">티켓 정보</h3>
                <div className="mt-2 space-y-2">
                  {reservation.ticketNumbers?.map((ticketNumber: string, idx: number) => (
                    <div key={ticketNumber} className="flex items-center justify-between rounded-md border p-2">
                      <div className="flex items-center gap-2">
                        <Ticket className="h-4 w-4 text-muted-foreground" />
                        <span>티켓 번호: {ticketNumber}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-between">
              {/* 공연 상세보기 버튼은 필요시 추가 */}
              {/* {canCancel && <CancelReservationDialog reservationId={reservation.reservationId} />} */}
            </CardFooter>
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
                      {reservation.ticketPrice?.toLocaleString()}원 x {reservation.quantity}매
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
                    <span className="text-sm font-medium">취소 시 아래 정책에 따라 환불됩니다.</span>
                  </div>
                  <ul className="list-inside list-disc text-sm text-muted-foreground">
                    <li>공연 7일 전까지: 전액 환불</li>
                    <li>공연 3일 전까지: 70% 환불</li>
                    <li>공연 1일 전까지: 50% 환불</li>
                    <li>공연 당일: 환불 불가</li>
                  </ul>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
