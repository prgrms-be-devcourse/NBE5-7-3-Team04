"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"
import { formatKSTDateTime } from "@/src/api/utils/date"

interface Reservation {
  reservationId: number
  performanceId: number
  performanceScheduleId: number
  name: string
  title: string
  price: number
  quantity: number
  totalPrice: number
  status: 'PAYMENTS_PENDING' | 'PAYMENTS_CONFIRMED' | 'CANCEL_PENDING' | 'CANCEL_CONFIRMED'
  createdAt: string
}

const statusMap: Record<string, string> = {
  'PAYMENTS_PENDING': '결제 대기',
  'PAYMENTS_CONFIRMED': '예매 완료',
  'CANCEL_PENDING': '취소 대기',
  'CANCEL_CONFIRMED': '취소 완료'
}

export default function ReservationsPage() {
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL")
  const [isLoading, setIsLoading] = useState(true)
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { toast } = useToast()

  const fetchReservations = async (page: number, status?: string) => {
    try {
      const statusQuery = status && status !== "ALL" ? `reservationStatus=${status}&` : ''
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/admin/reservations/search?${statusQuery}page=${page}&size=10`,
        { credentials: 'include' }
      )
      
      if (!response.ok) throw new Error('예매 데이터를 가져오는데 실패했습니다')
      
      const data = await response.json()
      setReservations(data.content)
      setTotalCount(data.totalElements)
    } catch (error) {
      console.error('Error fetching reservations:', error)
      toast({
        title: "오류",
        description: "예매 목록을 가져오는데 실패했습니다",
        variant: "destructive"
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleStatusChange = (value: string) => {
    setSelectedStatus(value)
    setCurrentPage(0)
    fetchReservations(0, value || undefined)
  }

  const handleConfirmReservation = async (reservationId: number) => {
    try {
      const csrfToken = await getCsrfToken()
      if (!csrfToken) {
        console.error('CSRF 토큰을 가져올 수 없습니다')
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/admin/reservations/${reservationId}`,
        {
          method: 'PATCH',
          credentials: 'include',
          headers: {
            'Accept': 'application/json',
            'X-XSRF-TOKEN': csrfToken
          }
        }
      )

      if (!response.ok) throw new Error('예매 확정에 실패했습니다')

      toast({
        title: "성공",
        description: "예매가 확정되었습니다"
      })
      
      setIsModalOpen(false)
      fetchReservations(currentPage, selectedStatus || undefined)
    } catch (error) {
      console.error('Error confirming reservation:', error)
      toast({
        title: "오류",
        description: "예매 확정에 실패했습니다",
        variant: "destructive"
      })
    }
  }

  const handlePrevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(prev => prev - 1)
    }
  }

  const handleNextPage = () => {
    if ((currentPage + 1) * 10 < totalCount) {
      setCurrentPage(prev => prev + 1)
    }
  }

  useEffect(() => {
    fetchReservations(currentPage, selectedStatus || undefined)
  }, [currentPage])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">예매 관리</h1>
        <p className="text-muted-foreground">예매 내역을 관리하고 확정할 수 있습니다.</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>예매 목록</CardTitle>
              <CardDescription>총 {totalCount}개의 예매가 있습니다.</CardDescription>
            </div>
            <Select value={selectedStatus} onValueChange={handleStatusChange}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="상태 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">전체</SelectItem>
                <SelectItem value="PAYMENTS_PENDING">결제 대기</SelectItem>
                <SelectItem value="PAYMENTS_CONFIRMED">예매 완료</SelectItem>
                <SelectItem value="CANCEL_PENDING">취소 대기</SelectItem>
                <SelectItem value="CANCEL_CONFIRMED">취소 완료</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>공연명</TableHead>
                <TableHead>예매자</TableHead>
                <TableHead>상태</TableHead>
                <TableHead>수량</TableHead>
                <TableHead>금액</TableHead>
                <TableHead>예매일</TableHead>
                <TableHead className="text-right">상세</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center">로딩 중...</TableCell>
                </TableRow>
              ) : reservations.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center">예매 내역이 없습니다</TableCell>
                </TableRow>
              ) : (
                reservations.map((reservation) => (
                  <TableRow key={reservation.reservationId}>
                    <TableCell className="font-medium">{reservation.title}</TableCell>
                    <TableCell>{reservation.name}</TableCell>
                    <TableCell>
                      <Badge variant={
                        reservation.status === 'PAYMENTS_CONFIRMED' ? 'default' :
                        reservation.status === 'PAYMENTS_PENDING' ? 'outline' :
                        reservation.status === 'CANCEL_PENDING' ? 'secondary' : 'destructive'
                      }>
                        {statusMap[reservation.status]}
                      </Badge>
                    </TableCell>
                    <TableCell>{reservation.quantity}매</TableCell>
                    <TableCell>{reservation.totalPrice.toLocaleString()}원</TableCell>
                    <TableCell>{formatKSTDateTime(reservation.createdAt)}</TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setSelectedReservation(reservation)
                          setIsModalOpen(true)
                        }}
                      >
                        상세보기
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            총 {totalCount}개 중 {reservations.length}개 표시
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handlePrevPage}
              disabled={currentPage === 0}
            >
              이전
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={handleNextPage}
              disabled={(currentPage + 1) * 10 >= totalCount}
            >
              다음
            </Button>
          </div>
        </CardFooter>
      </Card>

      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        {selectedReservation && (
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>예매 상세 정보</DialogTitle>
              <DialogDescription>
                예매 번호: {selectedReservation.reservationId}
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4">
              <div>
                <h3 className="text-lg font-semibold">{selectedReservation.title}</h3>
                <div className="grid grid-cols-2 gap-4 mt-4">
                  <div>
                    <p className="text-sm font-medium">예매자</p>
                    <p className="text-sm text-muted-foreground">{selectedReservation.name}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">예매 상태</p>
                    <p className="text-sm text-muted-foreground">
                      {statusMap[selectedReservation.status]}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">예매 수량</p>
                    <p className="text-sm text-muted-foreground">{selectedReservation.quantity}매</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">결제 금액</p>
                    <p className="text-sm text-muted-foreground">
                      {selectedReservation.totalPrice.toLocaleString()}원
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">예매일</p>
                    <p className="text-sm text-muted-foreground">
                      {formatKSTDateTime(selectedReservation.createdAt)}
                    </p>
                  </div>
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  variant="outline"
                  onClick={() => setIsModalOpen(false)}
                >
                  닫기
                </Button>
                {selectedReservation.status === 'PAYMENTS_PENDING' && (
                  <Button
                    onClick={() => handleConfirmReservation(selectedReservation.reservationId)}
                  >
                    예매 확정
                  </Button>
                )}
              </div>
            </div>
          </DialogContent>
        )}
      </Dialog>
    </div>
  )
}
