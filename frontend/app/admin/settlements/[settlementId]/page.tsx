import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  ArrowLeft,
  CheckCircle,
  Calendar,
  Clock,
  User,
  Ticket,
  Building,
  BanknoteIcon as BankIcon,
  Users,
  Percent,
  Calculator,
} from "lucide-react"
import Link from "next/link"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Progress } from "@/components/ui/progress"

export default function SettlementDetailPage({ params }: { params: { settlementId: string } }) {
  // 실제 구현에서는 params.settlementId를 사용하여 API에서 데이터를 가져옵니다
  const settlement = {
    id: params.settlementId,
    performance: {
      id: "P-2023-1001",
      title: "2023 여름 재즈 페스티벌",
      date: "2023-07-15",
      time: "18:00 - 22:00",
      location: "서울 올림픽 공원",
      totalSeats: 500,
      soldSeats: 425,
      ticketPrice: 50000,
      totalSales: 21250000,
    },
    manager: {
      name: "김재즈",
      email: "kim@example.com",
      phone: "010-1234-5678",
      company: "재즈 프로덕션",
    },
    bankInfo: {
      bank: "신한은행",
      accountNumber: "110-123-456789",
      accountHolder: "김재즈",
    },
    settlement: {
      platformFee: 20, // 플랫폼 수수료 비율 (%)
      platformFeeAmount: 4250000, // 플랫폼 수수료 금액
      settlementAmount: 17000000, // 정산 금액
      status: "정산 대기중",
      requestedAt: "2023-07-16 09:00:00",
      completedAt: null,
    },
  }

  // 판매율 계산
  const salesRate = (settlement.performance.soldSeats / settlement.performance.totalSeats) * 100

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-4">
        <Button variant="outline" size="icon" asChild>
          <Link href="/admin/settlements">
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">뒤로 가기</span>
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">정산 상세 정보</h1>
          <p className="text-muted-foreground">
            정산 ID: {settlement.id} | 공연: {settlement.performance.title}
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>정산 정보</CardTitle>
                <CardDescription>정산 상세 내역 및 처리 상태</CardDescription>
              </div>
              <Badge
                variant={
                  settlement.settlement.status === "정산 완료"
                    ? "success"
                    : settlement.settlement.status === "정산 대기중"
                      ? "outline"
                      : settlement.settlement.status === "처리중"
                        ? "secondary"
                        : "default"
                }
              >
                {settlement.settlement.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div>
              <h3 className="font-medium mb-2">정산 금액 정보</h3>
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <Ticket className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">티켓 가격:</span>{" "}
                    {settlement.performance.ticketPrice.toLocaleString()}원
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Users className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">판매 좌석:</span> {settlement.performance.soldSeats}석 /{" "}
                    {settlement.performance.totalSeats}석
                    <div className="mt-1">
                      <Progress value={salesRate} className="h-2" />
                      <div className="text-xs text-muted-foreground mt-1">판매율: {salesRate.toFixed(1)}%</div>
                    </div>
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Calculator className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">총 매출액:</span> {settlement.performance.totalSales.toLocaleString()}
                    원
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Percent className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">플랫폼 수수료:</span> {settlement.settlement.platformFee}% (
                    {settlement.settlement.platformFeeAmount.toLocaleString()}원)
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <BankIcon className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">최종 정산액:</span>{" "}
                    <span className="text-lg font-bold text-green-600">
                      {settlement.settlement.settlementAmount.toLocaleString()}원
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <Separator />

            <div>
              <h3 className="font-medium mb-2">정산 계좌 정보</h3>
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <BankIcon className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <div className="font-medium">입금 계좌:</div>
                    <div className="pl-2 text-muted-foreground">
                      <div>은행: {settlement.bankInfo.bank}</div>
                      <div>계좌번호: {settlement.bankInfo.accountNumber}</div>
                      <div>예금주: {settlement.bankInfo.accountHolder}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <Alert>
              <AlertTitle>정산 처리 안내</AlertTitle>
              <AlertDescription>
                정산 처리 시 해당 계좌로 입금 후 상태를 변경해주세요. 정산 완료 후에는 상태를 변경할 수 없습니다.
              </AlertDescription>
            </Alert>
          </CardContent>
          <CardFooter className="flex justify-between">
            <Button variant="outline" asChild>
              <Link href="/admin/settlements">취소</Link>
            </Button>
            <div className="flex gap-2">
              <Button>
                <CheckCircle className="mr-2 h-4 w-4" />
                정산 완료 처리
              </Button>
            </div>
          </CardFooter>
        </Card>

        <div className="flex flex-col gap-6">
          <Card>
            <CardHeader>
              <CardTitle>공연 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <Ticket className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연명:</span> {settlement.performance.title}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연일:</span> {settlement.performance.date}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연시간:</span> {settlement.performance.time}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Building className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">공연장소:</span> {settlement.performance.location}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>공연 관리자 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 gap-3 text-sm">
                <div className="flex items-start gap-2">
                  <User className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">이름:</span> {settlement.manager.name}
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
                    <span className="font-medium">이메일:</span> {settlement.manager.email}
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
                    <span className="font-medium">전화번호:</span> {settlement.manager.phone}
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <Building className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div>
                    <span className="font-medium">소속:</span> {settlement.manager.company}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>정산 처리 이력</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm">정산 요청일시:</span>
                  </div>
                  <span className="text-sm font-medium">{settlement.settlement.requestedAt}</span>
                </div>
                {settlement.settlement.completedAt && (
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      <span className="text-sm">정산 완료일시:</span>
                    </div>
                    <span className="text-sm font-medium">{settlement.settlement.completedAt}</span>
                  </div>
                )}
                {!settlement.settlement.completedAt && (
                  <div className="text-sm text-muted-foreground italic">아직 정산이 완료되지 않았습니다.</div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
