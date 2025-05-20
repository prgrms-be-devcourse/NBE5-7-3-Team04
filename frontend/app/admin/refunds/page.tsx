"use client"

import { useEffect, useState, useRef } from "react"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"
import Image from "next/image"
import { formatKSTDateTime, formatKSTDate } from "@/src/api/utils/date"
import { getPerformanceImageUrl } from "@/lib/utils"

interface RefundDetail {
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
  createdDate: string
  updatedDate: string
}

const statusMap: Record<string, string> = {
  'PENDING': '계좌입력대기',
  'READY': '환불대기',
  'CONFIRMED': '환불완료'
}

export default function RefundsPage() {
  const [refunds, setRefunds] = useState<RefundDetail[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL")
  const [isLoading, setIsLoading] = useState(true)
  const [selectedRefund, setSelectedRefund] = useState<RefundDetail | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { toast } = useToast()
  const [userNames, setUserNames] = useState<Record<number, string>>({})
  const loadingUserIds = useRef<Set<number>>(new Set())

  const fetchUserName = async (userId: number) => {
    if (userNames[userId] || loadingUserIds.current.has(userId)) return
    loadingUserIds.current.add(userId)
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/user/${userId}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const name = await response.text()
        setUserNames(prev => ({ ...prev, [userId]: name }))
      } else {
        setUserNames(prev => ({ ...prev, [userId]: '이름 조회 실패' }))
      }
    } catch {
      setUserNames(prev => ({ ...prev, [userId]: '이름 조회 실패' }))
    } finally {
      loadingUserIds.current.delete(userId)
    }
  }

  const fetchRefunds = async (page: number) => {
    try {
      const statusQuery = selectedStatus === 'ALL' ? '' : `&status=${selectedStatus}`;
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/refunds?page=${page}&size=10${statusQuery}`, {
        credentials: 'include'
      })
      
      if (!response.ok) throw new Error('환불 데이터를 가져오는데 실패했습니다')
      
      const data = await response.json()
      setTotalCount(data.totalElements)

      // userId 목록 추출 및 중복 제거
      const userIds = Array.from(new Set(data.content.map((refund: RefundDetail) => refund.userId))) as number[]
      // 아직 이름이 없는 userId만 조회
      const userIdToFetch = userIds.filter(id => !(id in userNames))
      if (userIdToFetch.length > 0) {
        const results = await Promise.all(userIdToFetch.map(async (userId) => {
          try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/user/${userId}`, { credentials: 'include' })
            if (res.ok) {
              const name = await res.text()
              return [userId, name]
            } else {
              return [userId, '이름 조회 실패']
            }
          } catch {
            return [userId, '이름 조회 실패']
          }
        }))
        const namesObj = Object.fromEntries(results)
        setUserNames(prev => ({ ...prev, ...namesObj }))
      }
      setRefunds(data.content)
    } catch (error) {
      console.error('Error fetching refunds:', error)
      toast({
        title: "오류",
        description: "환불 목록을 가져오는데 실패했습니다",
        variant: "destructive"
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleStatusChange = (value: string) => {
    setSelectedStatus(value)
    setCurrentPage(0)
  }

  const handleConfirmRefund = async (refundId: number) => {
    try {
      const csrfToken = await getCsrfToken()
      if (!csrfToken) {
        console.error('CSRF 토큰을 가져올 수 없습니다')
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/admin/refunds/${refundId}/confirm`,
        {
          method: 'PATCH',
          credentials: 'include',
          headers: {
            'Accept': 'application/json',
            'X-XSRF-TOKEN': csrfToken
          }
        }
      )

      if (!response.ok) throw new Error('환불 승인에 실패했습니다')

      toast({
        title: "성공",
        description: "환불이 승인되었습니다"
      })
      
      setIsModalOpen(false)
      fetchRefunds(currentPage)
    } catch (error) {
      console.error('Error confirming refund:', error)
      toast({
        title: "오류",
        description: "환불 승인에 실패했습니다",
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
    setIsLoading(true)
    fetchRefunds(currentPage)
  }, [currentPage, selectedStatus])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">환불 관리</h1>
        <p className="text-muted-foreground">환불 요청을 관리하고 승인할 수 있습니다.</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>환불 목록</CardTitle>
              <CardDescription>총 {totalCount}개의 환불 요청이 있습니다.</CardDescription>
            </div>
            <Select value={selectedStatus} onValueChange={handleStatusChange}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="상태 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">전체</SelectItem>
                <SelectItem value="PENDING">계좌입력대기</SelectItem>
                <SelectItem value="READY">환불대기</SelectItem>
                <SelectItem value="CONFIRMED">환불완료</SelectItem>
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
                <TableHead>금액</TableHead>
                <TableHead>신청일</TableHead>
                <TableHead>환불일</TableHead>
                <TableHead className="text-right">상세</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">로딩 중...</TableCell>
                </TableRow>
              ) : refunds.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">환불 요청이 없습니다</TableCell>
                </TableRow>
              ) : (
                refunds.map((refund) => (
                  <TableRow key={refund.refundId}>
                    <TableCell className="font-medium">{refund.title}</TableCell>
                    <TableCell>{userNames[refund.userId] ?? ''}</TableCell>
                    <TableCell>
                      <Badge variant={
                        refund.refundStatus === 'CONFIRMED' ? 'default' :
                        refund.refundStatus === 'READY' ? 'outline' : 'secondary'
                      }>
                        {statusMap[refund.refundStatus]}
                      </Badge>
                    </TableCell>
                    <TableCell>{(refund.price * refund.quantity).toLocaleString()}원</TableCell>
                    <TableCell>{formatKSTDate(refund.createdDate)}</TableCell>
                    <TableCell>{refund.refundStatus === 'CONFIRMED' ? formatKSTDate(refund.updatedDate) : ''}</TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setSelectedRefund(refund)
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
            총 {totalCount}개 중 {refunds.length}개 표시
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
        {selectedRefund && (
          <DialogContent className="max-w-3xl">
            <DialogHeader>
              <DialogTitle>환불 상세 정보</DialogTitle>
              <DialogDescription>
                환불 신청 번호: {selectedRefund.refundId}
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-6">
              <div className="flex gap-4">
                <div className="flex-1">
                  <h3 className="text-lg font-semibold">{selectedRefund.title}</h3>
                  <p className="text-sm text-muted-foreground mt-1">{selectedRefund.description}</p>
                  <div className="grid grid-cols-2 gap-4 mt-4">
                    <div>
                      <p className="text-sm font-medium">공연 장소</p>
                      <p className="text-sm text-muted-foreground">{selectedRefund.venue}</p>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium">신청일시</p>
                        <p className="text-sm text-muted-foreground">
                          {formatKSTDateTime(selectedRefund.createdDate)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm font-medium">환불일시</p>
                        <p className="text-sm text-muted-foreground">
                          {selectedRefund.refundStatus === 'CONFIRMED' ? formatKSTDateTime(selectedRefund.updatedDate) : '-'}
                        </p>
                      </div>
                    </div>
                    <div>
                      <p className="text-sm font-medium">예매 수량</p>
                      <p className="text-sm text-muted-foreground">{selectedRefund.quantity}매</p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">환불 금액</p>
                      <p className="text-sm text-muted-foreground">
                        {(selectedRefund.price * selectedRefund.quantity).toLocaleString()}원
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm font-medium">환불 계좌</p>
                  <p className="text-sm text-muted-foreground">{selectedRefund.bank} {selectedRefund.account}</p>
                </div>
                <div>
                  <p className="text-sm font-medium">예금주</p>
                  <p className="text-sm text-muted-foreground">{selectedRefund.depositorName}</p>
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  variant="outline"
                  onClick={() => setIsModalOpen(false)}
                >
                  닫기
                </Button>
                {selectedRefund.refundStatus === 'READY' && (
                  <Button
                    onClick={() => handleConfirmRefund(selectedRefund.refundId)}
                  >
                    환불 승인
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
