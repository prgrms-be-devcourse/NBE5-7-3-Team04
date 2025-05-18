"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { FileCheck, CreditCard, RotateCcw } from "lucide-react"
import Link from "next/link"

interface Performance {
  id: number
  fileUrl: string
  performanceManagerName: string
  title: string
  venue: string
  price: number
  totalSeats: number
  category: string
  startDate: string
  endDate: string
  description: string
}

interface Settlement {
  settlementId: number
  title: string
  totalAmount: number
  settledAt: string | null
  status: string
}

interface Refund {
  refundId: number
  userId: number
  reservationId: number
  account: string
  bank: string
  depositorName: string
  refundStatus: 'PENDING' | 'READY' | 'CONFIRMED'
  quantity: number
  startTime: string
  fileId: number
  title: string
  venue: string
  price: number
  category: string
  performanceDate: string
  description: string
}

interface Reservation {
  reservationId: number
  title: string
  memberName: string
  reservationStatus: string
  totalPrice: number
  quantity: number
  reservedAt: string
}

export default function AdminDashboard() {
  const [pendingPerformances, setPendingPerformances] = useState<Performance[]>([])
  const [pendingCount, setPendingCount] = useState(0)
  const [totalPerformances, setTotalPerformances] = useState(0)
  const [totalReservations, setTotalReservations] = useState(0)
  const [pendingRefunds, setPendingRefunds] = useState(0)
  const [pendingSettlements, setPendingSettlements] = useState<Settlement[]>([])
  const [readyRefunds, setReadyRefunds] = useState<Refund[]>([])
  const [pendingReservations, setPendingReservations] = useState<Reservation[]>([])

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // 승인 대기 공연 데이터 가져오기
        const approvalResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/performances?page=0&size=2&status=PENDING`, {
          credentials: 'include'
        })
        if (!approvalResponse.ok) throw new Error('승인 대기 공연 데이터를 가져오지 못했습니다')
        const approvalData = await approvalResponse.json()
        setPendingPerformances(approvalData.content)
        setPendingCount(approvalData.totalElements)

        // 정산 대기 공연 데이터 가져오기
        const performanceResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/settlements?status=PENDING&page=0&size=2`, {
          credentials: 'include'
        })
        if (!performanceResponse.ok) throw new Error('정산 대기 공연 데이터를 가져오지 못했습니다')
        const performanceData = await performanceResponse.json()
        setTotalPerformances(performanceData.totalElements)
        setPendingSettlements(performanceData.content)

        // 결제 대기 예매 수 가져오기
        const reservationResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/reservations/search?reservationStatus=PAYMENTS_PENDING&page=0&size=2`, {
          credentials: 'include'
        })
        if (!reservationResponse.ok) throw new Error('예매 데이터를 가져오지 못했습니다')
        const reservationData = await reservationResponse.json()
        setTotalReservations(reservationData.totalElements)
        setPendingReservations(reservationData.content)

        // 환불 대기 수 가져오기
        const refundResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/refunds?status=READY&page=0&size=2`, {
          credentials: 'include'
        })
        if (!refundResponse.ok) throw new Error('환불 데이터를 가져오지 못했습니다')
        const refundData = await refundResponse.json()
        setPendingRefunds(refundData.totalElements)
        setReadyRefunds(refundData.content)

      } catch (e) {
        setPendingPerformances([])
        setPendingCount(0)
        setTotalPerformances(0)
        setTotalReservations(0)
        setPendingRefunds(0)
        setPendingReservations([])
        setReadyRefunds([])
        setPendingSettlements([])
      }
    }
    fetchDashboardData()
  }, [])

  return (
    <div className="flex flex-col gap-4">
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">승인 대기 공연</CardTitle>
            <FileCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingCount}건</div>
            <p className="text-xs text-muted-foreground">승인 대기 중인 공연</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">정산 대기 공연</CardTitle>
            <FileCheck className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalPerformances}건</div>
            <p className="text-xs text-muted-foreground">정산 대기 중인 공연</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">결제 대기 예매</CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalReservations}매</div>
            <p className="text-xs text-muted-foreground">결제 대기 중인 예매</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">환불 대기</CardTitle>
            <RotateCcw className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingRefunds}건</div>
            <p className="text-xs text-muted-foreground">환불 대기 중인 예매</p>
          </CardContent>
        </Card>
      </div>
      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger value="analytics">분석</TabsTrigger>
          <TabsTrigger value="reports">보고서</TabsTrigger>
        </TabsList>
        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-2">
            <Link href="/admin/approve/performances">
              <Card className="transition-colors hover:bg-muted/50">
                <CardHeader>
                  <CardTitle>최근 승인 대기 공연</CardTitle>
                  <CardDescription>최근 2개의 승인 대기 공연</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4 min-h-[90px]">
                    {pendingPerformances.length === 0 ? (
                      <div className="text-muted-foreground text-sm">최근 승인 대기 공연이 없습니다.</div>
                    ) : (
                      pendingPerformances.map((performance) => (
                        <div key={performance.id} className="flex items-center justify-between">
                          <div className="space-y-1">
                            <p className="text-sm font-medium leading-none">{performance.title}</p>
                            <p className="text-sm text-muted-foreground">
                              관리자: {performance.performanceManagerName}
                            </p>
                          </div>
                          <Badge variant="outline">승인대기</Badge>
                        </div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </Link>
            <Link href="/admin/settlements">
              <Card className="transition-colors hover:bg-muted/50">
                <CardHeader>
                  <CardTitle>최근 정산 대기 공연</CardTitle>
                  <CardDescription>최근 2개의 정산 대기 공연</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4 min-h-[90px]">
                    {pendingSettlements.length === 0 ? (
                      <div className="text-muted-foreground text-sm">최근 정산 대기 공연이 없습니다.</div>
                    ) : (
                      pendingSettlements.map((settlement) => (
                        <div key={settlement.settlementId} className="flex items-center justify-between">
                          <div className="space-y-1">
                            <p className="text-sm font-medium leading-none">{settlement.title}</p>
                            <p className="text-sm text-muted-foreground">
                              정산 금액: {settlement.totalAmount.toLocaleString()}원
                            </p>
                          </div>
                          <Badge variant="outline">정산대기</Badge>
                        </div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </Link>
          </div>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-2">
            <Link href="/admin/reservations">
              <Card className="transition-colors hover:bg-muted/50">
                <CardHeader>
                  <CardTitle>최근 결제 대기 예매</CardTitle>
                  <CardDescription>최근 2개의 결제 대기 예매</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4 min-h-[90px]">
                    {pendingReservations.length === 0 ? (
                      <div className="text-muted-foreground text-sm">최근 결제 대기 예매가 없습니다.</div>
                    ) : (
                      pendingReservations.map((reservation) => (
                        <div key={reservation.reservationId} className="flex items-center justify-between">
                          <div className="space-y-1">
                            <p className="text-sm font-medium leading-none">{reservation.title}</p>
                            <p className="text-sm text-muted-foreground">
                              {reservation.memberName} • {reservation.quantity}매 • {reservation.totalPrice.toLocaleString()}원
                            </p>
                          </div>
                          <Badge variant="outline">결제대기</Badge>
                        </div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </Link>
            <Link href="/admin/refunds">
              <Card className="transition-colors hover:bg-muted/50">
                <CardHeader>
                  <CardTitle>최근 환불 요청</CardTitle>
                  <CardDescription>최근 2개의 환불 요청</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4 min-h-[90px]">
                    {readyRefunds.length === 0 ? (
                      <div className="text-muted-foreground text-sm">최근 환불 요청이 없습니다.</div>
                    ) : (
                      readyRefunds.map((refund) => (
                        <div key={refund.refundId} className="flex items-center justify-between">
                          <div className="space-y-1">
                            <p className="text-sm font-medium leading-none">{refund.title}</p>
                            <p className="text-sm text-muted-foreground">
                              {refund.depositorName} • {(refund.price * refund.quantity).toLocaleString()}원
                            </p>
                          </div>
                          <Badge variant="outline">환불대기</Badge>
                        </div>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </Link>
          </div>
        </TabsContent>
        <TabsContent value="analytics" className="space-y-4">
          <div className="h-[400px] w-full bg-muted/20 flex items-center justify-center text-muted-foreground">
            분석 데이터 영역 (실제 구현 시 차트 및 데이터 시각화 구현)
          </div>
        </TabsContent>
        <TabsContent value="reports" className="space-y-4">
          <div className="h-[400px] w-full bg-muted/20 flex items-center justify-center text-muted-foreground">
            보고서 영역 (실제 구현 시 보고서 목록 및 다운로드 기능 구현)
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
