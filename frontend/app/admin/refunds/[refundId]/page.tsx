import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  ArrowLeft,
  CheckCircle,
  XCircle,
  Calendar,
  Clock,
  CreditCard,
  User,
  Ticket,
  Building,
  BanknoteIcon as BankIcon,
} from "lucide-react"
import Link from "next/link"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"

export default function RefundDetailPage({ params }: { params: { refundId: string } }) {
  // 실제 구현에서는 params.refundId를 사용하여 API에서 데이터를 가져옵니다
  const refund = {
    id: params.refundId,
    reservationId: "R-2023-2001",
    user: {
      name: "김환불",
      email: "kim@example.com",
      phone: "010-1234-5678",
    },
    performance: {
      title: "2023 여름 재즈 페스티벌",
      date: "2023-07-15",
      time: "18:00 - 22:00",
      location: "서울 올림픽 공원",
    },
    payment: {
      amount: 50000,
      method: "무통장 입금",
      bank: "신한은행",
      accountNumber: "110-123-456789",
      accountHolder: "김환불",
      approvedAt: "2023-05-10 14:30:22",
    },
    refund: {
      requestedAt: "2023-05-15 09:12:45",
      reason: "개인 일정 변경으로 인한 취소",
      status: "환불 대기중",
      bankInfo: {
        bank: "국민은행",
        accountNumber: "123-45-6789012",
        accountHolder: "김환불",
      },
    },
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-4">
        <Button variant="outline" size="icon" asChild>
          <Link href="/admin/refunds">
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">뒤로 가기</span>
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">환불 상세 정보</h1>
          <p className="text-muted-foreground">
            환불 ID: {refund.id} | 예약번호: {refund.reservationId}
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>환불 정보</CardTitle>
                <CardDescription>환불 요청 상세 정보</CardDescription>
              </div>
              <Badge
                variant={
                  refund.refund.status === "환불 완료"
                    ? "success"
                    : refund.refund.status === "환불 대기중"
                      ? "outline"
                      : refund.refund.status === "처리중"
                        ? "secondary"
                        : refund.refund.status === "환불 거절"
                          ? "destructive"
                          : "default"
                }
              >
                {refund.refund.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <h3 className="font-medium mb-2">환불 요청 정보</h3>
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">요청일시:</span> {refund.refund.requestedAt}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <CreditCard className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">환불 금액:</span> {refund.payment.amount.toLocaleString()}원
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <BankIcon className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <div className="font-medium">환불 계좌 정보:</div>
                    <div className="pl-2 text-muted-foreground">
                      <div>은행: {refund.refund.bankInfo.bank}</div>
                      <div>계좌번호: {refund.refund.bankInfo.accountNumber}</div>
                      <div>예금주: {refund.refund.bankInfo.accountHolder}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <Separator />

            <div>
              <h3 className="font-medium mb-2">취소 사유</h3>
              <p className="text-sm text-muted-foreground">{refund.refund.reason}</p>
            </div>

            <Alert>
              <AlertTitle>환불 처리 안내</AlertTitle>
              <AlertDescription>
                환불 처리 시 해당 계좌로 입금 후 상태를 변경해주세요. 환불 완료 후에는 상태를 변경할 수 없습니다.
              </AlertDescription>
            </Alert>
          </CardContent>
          <CardFooter className="flex justify-between">
            <Button variant="outline" asChild>
              <Link href="/admin/refunds">취소</Link>
            </Button>
            <div className="flex gap-2">
              <Button variant="destructive">
                <XCircle className="mr-2 h-4 w-4" />
                환불 거절
              </Button>
              <Button>
                <CheckCircle className="mr-2 h-4 w-4" />
                환불 완료 처리
              </Button>
            </div>
          </CardFooter>
        </Card>

        <div className="flex flex-col gap-6">
          <Card>
            <CardHeader>
              <CardTitle>사용자 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <User className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">이름:</span> {refund.user.name}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="h-4 w-4 text-muted-foreground mt-0.5"
                  >
                    <rect width="20" height="16" x="2" y="4" rx="2" />
                    <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                  </svg>
                  <div>
                    <span className="font-medium">이메일:</span> {refund.user.email}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="h-4 w-4 text-muted-foreground mt-0.5"
                  >
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
                  </svg>
                  <div>
                    <span className="font-medium">전화번호:</span> {refund.user.phone}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>공연 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <Ticket className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연명:</span> {refund.performance.title}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연일:</span> {refund.performance.date}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연시간:</span> {refund.performance.time}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Building className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연장소:</span> {refund.performance.location}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>결제 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <CreditCard className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">결제 방법:</span> {refund.payment.method}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <BankIcon className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <div className="font-medium">입금 정보:</div>
                    <div className="pl-2 text-muted-foreground">
                      <div>은행: {refund.payment.bank}</div>
                      <div>계좌번호: {refund.payment.accountNumber}</div>
                      <div>예금주: {refund.payment.accountHolder}</div>
                    </div>
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">결제 승인일시:</span> {refund.payment.approvedAt}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
