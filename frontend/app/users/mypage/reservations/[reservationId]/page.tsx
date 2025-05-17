import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, ArrowLeft, Ticket, CreditCard, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { Separator } from "@/components/ui/separator"
import { CancelReservationDialog } from "@/components/cancel-reservation-dialog"
import { PageProps } from "@/types/route"

interface ReservationParams {
  reservationId: string
}

export default async function ReservationDetailPage({ params }: PageProps<ReservationParams>) {
  const { reservationId } = await params
  
  // 실제 구현에서는 reservationId를 사용하여 API에서 데이터를 가져옵니다
  const reservation = {
    id: reservationId,
    performance: {
      id: "1",
      title: "2023 여름 재즈 페스티벌",
      date: "2023-07-15",
      time: "18:00 - 22:00",
      location: "서울 올림픽 공원",
      address: "서울특별시 송파구 올림픽로 424",
    },
    reservedAt: "2023-05-10 14:30:22",
    quantity: 2,
    amount: 100000,
    status: "예매완료",
    payment: {
      method: "무통장 입금",
      bank: "신한은행",
      accountNumber: "123-456-789012",
      accountHolder: "티켓-4-U",
      paidAt: "2023-05-10 15:45:33",
    },
    tickets: [
      { id: "T-2023-10001", seat: "A-15" },
      { id: "T-2023-10002", seat: "A-16" },
    ],
    refundPolicy: [
      "공연 7일 전까지: 전액 환불",
      "공연 3일 전까지: 70% 환불",
      "공연 1일 전까지: 50% 환불",
      "공연 당일: 환불 불가",
    ],
  }

  const statusVariant =
    reservation.status === "예매완료" ? "success" : reservation.status === "결제대기" ? "secondary" : "destructive"

  // 공연 날짜와 현재 날짜를 비교하여 취소 가능 여부 확인
  const performanceDate = new Date(reservation.performance.date)
  const today = new Date()
  const canCancel = performanceDate > today && reservation.status === "예매완료"

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
              예약번호: {reservation.id} | {reservation.reservedAt} 예매
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
                <h3 className="text-lg font-semibold">{reservation.performance.title}</h3>
                <div className="mt-2 grid gap-2 text-sm">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <span>{reservation.performance.date}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-muted-foreground" />
                    <span>{reservation.performance.time}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-muted-foreground" />
                    <div>
                      <div>{reservation.performance.location}</div>
                      <div className="text-xs text-muted-foreground">{reservation.performance.address}</div>
                    </div>
                  </div>
                </div>
              </div>

              <Separator />

              <div>
                <h3 className="font-medium">티켓 정보</h3>
                <div className="mt-2 space-y-2">
                  {reservation.tickets.map((ticket) => (
                    <div key={ticket.id} className="flex items-center justify-between rounded-md border p-2">
                      <div className="flex items-center gap-2">
                        <Ticket className="h-4 w-4 text-muted-foreground" />
                        <span>티켓 번호: {ticket.id}</span>
                      </div>
                      <Badge variant="outline">{ticket.seat}</Badge>
                    </div>
                  ))}
                </div>
              </div>
            </CardContent>
            <CardFooter className="flex justify-between">
              <Button variant="outline" asChild>
                <Link href={`/performances/${reservation.performance.id}`}>공연 상세보기</Link>
              </Button>
              {canCancel && <CancelReservationDialog reservationId={reservation.id} />}
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
                      {(reservation.amount / reservation.quantity).toLocaleString()}원 x {reservation.quantity}매
                    </span>
                  </div>
                  <div className="flex items-center justify-between font-bold">
                    <span>총 결제 금액</span>
                    <span>{reservation.amount.toLocaleString()}원</span>
                  </div>
                </div>

                <Separator />

                <div>
                  <h3 className="font-medium">결제 방법</h3>
                  <div className="mt-2 grid gap-2 text-sm">
                    <div className="flex items-center gap-2">
                      <CreditCard className="h-4 w-4 text-muted-foreground" />
                      <span>{reservation.payment.method}</span>
                    </div>
                    {reservation.payment.method === "무통장 입금" && (
                      <div className="rounded-md bg-muted p-3 text-xs">
                        <p>
                          입금 계좌: {reservation.payment.bank} {reservation.payment.accountNumber}
                        </p>
                        <p>예금주: {reservation.payment.accountHolder}</p>
                        <p>입금 확인 시간: {reservation.payment.paidAt}</p>
                      </div>
                    )}
                  </div>
                </div>
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
                    {reservation.refundPolicy.map((policy, index) => (
                      <li key={index}>{policy}</li>
                    ))}
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
