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

interface Settlement {
  settlementId: number
  totalAmount: number
  settledAt: string | null
  account: string
  bank: string
  status: 'PENDING' | 'CONFIRMED'
  title: string
}

const statusMap: Record<string, string> = {
  'PENDING': '정산대기',
  'CONFIRMED': '정산완료'
}

export default function SettlementsPage() {
  const [settlements, setSettlements] = useState<Settlement[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedStatus, setSelectedStatus] = useState<string>("ALL")
  const [isLoading, setIsLoading] = useState(true)
  const [selectedSettlement, setSelectedSettlement] = useState<Settlement | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { toast } = useToast()

  const fetchSettlements = async (page: number, status?: string) => {
    try {
      const statusQuery = status && status !== "ALL" ? `status=${status}&` : ''
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/admin/settlements?${statusQuery}page=${page}&size=10`,
        { credentials: 'include' }
      )
      
      if (!response.ok) throw new Error('정산 데이터를 가져오는데 실패했습니다')
      
      const data = await response.json()
      setSettlements(data.content)
      setTotalCount(data.totalElements)
    } catch (error) {
      console.error('Error fetching settlements:', error)
      toast({
        title: "오류",
        description: "정산 목록을 가져오는데 실패했습니다",
        variant: "destructive"
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleStatusChange = (value: string) => {
    setSelectedStatus(value)
    setCurrentPage(0)
    fetchSettlements(0, value || undefined)
  }

  const handleConfirmSettlement = async (settlementId: number) => {
    try {
      const csrfToken = await getCsrfToken()
      if (!csrfToken) {
        console.error('CSRF 토큰을 가져올 수 없습니다')
        return
      }

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/admin/settlements/${settlementId}/confirm`,
        {
          method: 'PATCH',
          credentials: 'include',
          headers: {
            'Accept': 'application/json',
            'X-XSRF-TOKEN': csrfToken
          }
        }
      )

      if (!response.ok) throw new Error('정산 승인에 실패했습니다')

      const updatedSettlement = await response.json()
      setSelectedSettlement(updatedSettlement)

      toast({
        title: "성공",
        description: "정산이 승인되었습니다"
      })
      
      fetchSettlements(currentPage, selectedStatus || undefined)
    } catch (error) {
      console.error('Error confirming settlement:', error)
      toast({
        title: "오류",
        description: "정산 승인에 실패했습니다",
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
    fetchSettlements(currentPage, selectedStatus || undefined)
  }, [currentPage])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">정산 관리</h1>
        <p className="text-muted-foreground">정산 신청을 관리하고 승인할 수 있습니다.</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>정산 목록</CardTitle>
              <CardDescription>총 {totalCount}개의 정산 신청이 있습니다.</CardDescription>
            </div>
            <Select value={selectedStatus} onValueChange={handleStatusChange}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="상태 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">전체</SelectItem>
                <SelectItem value="PENDING">정산대기</SelectItem>
                <SelectItem value="CONFIRMED">정산완료</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>공연명</TableHead>
                <TableHead>정산 금액</TableHead>
                <TableHead>상태</TableHead>
                <TableHead>계좌정보</TableHead>
                <TableHead>정산일</TableHead>
                <TableHead className="text-right">상세</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">로딩 중...</TableCell>
                </TableRow>
              ) : settlements.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">정산 신청이 없습니다</TableCell>
                </TableRow>
              ) : (
                settlements.map((settlement) => (
                  <TableRow key={settlement.settlementId}>
                    <TableCell className="font-medium">{settlement.title}</TableCell>
                    <TableCell>{settlement.totalAmount.toLocaleString()}원</TableCell>
                    <TableCell>
                      <Badge variant={settlement.status === 'CONFIRMED' ? 'default' : 'outline'}>
                        {statusMap[settlement.status]}
                      </Badge>
                    </TableCell>
                    <TableCell>{settlement.bank} {settlement.account}</TableCell>
                    <TableCell>
                      {settlement.settledAt ? formatKSTDateTime(settlement.settledAt) : '-'}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setSelectedSettlement(settlement)
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
            총 {totalCount}개 중 {settlements.length}개 표시
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
        {selectedSettlement && (
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>정산 상세 정보</DialogTitle>
              <DialogDescription>
                정산 번호: {selectedSettlement.settlementId}
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4">
              <div>
                <h3 className="text-lg font-semibold">{selectedSettlement.title}</h3>
                <div className="grid grid-cols-2 gap-4 mt-4">
                  <div>
                    <p className="text-sm font-medium">정산 상태</p>
                    <p className="text-sm text-muted-foreground">
                      {statusMap[selectedSettlement.status]}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">정산 금액</p>
                    <p className="text-sm text-muted-foreground">
                      {selectedSettlement.totalAmount.toLocaleString()}원
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">은행</p>
                    <p className="text-sm text-muted-foreground">{selectedSettlement.bank}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium">계좌번호</p>
                    <p className="text-sm text-muted-foreground">{selectedSettlement.account}</p>
                  </div>
                  {selectedSettlement.settledAt && (
                    <div>
                      <p className="text-sm font-medium">정산일</p>
                      <p className="text-sm text-muted-foreground">
                        {formatKSTDateTime(selectedSettlement.settledAt)}
                      </p>
                    </div>
                  )}
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <Button
                  variant="outline"
                  onClick={() => setIsModalOpen(false)}
                >
                  닫기
                </Button>
                {selectedSettlement.status === 'PENDING' && (
                  <Button
                    onClick={() => handleConfirmSettlement(selectedSettlement.settlementId)}
                  >
                    정산하기
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
