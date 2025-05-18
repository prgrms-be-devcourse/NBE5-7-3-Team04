"use client"

import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Eye } from "lucide-react"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import Image from "next/image"

interface PerformanceSchedule {
  id: number
  startTime: string
  endTime: string
}

interface Performance {
  id: number
  fileUrl: string
  performanceManagerName: string
  title: string
  venue: string
  price: number
  totalSeats: number
  category: string
  status: string
  startDate: string
  endDate: string
  description: string
  schedules: PerformanceSchedule[]
}

const categoryMap: Record<string, string> = {
  OPERA: "오페라",
  DANCING: "춤",
  SINGING: "노래"
}

const statusMap: Record<string, string> = {
  PENDING: "승인 대기",
  CONFIRMED: "공연 승인",
  REJECTED: "공연 거부",
  CANCELLED: "공연 취소",
  COMPLETED: "공연 완료"
}

const getStatusBadgeVariant = (status: string) => {
  switch (status) {
    case "PENDING":
      return "secondary"
    case "CONFIRMED":
      return "success"
    case "REJECTED":
      return "destructive"
    case "CANCELLED":
      return "outline"
    case "COMPLETED":
      return "default"
    default:
      return "outline"
  }
}

export default function PerformanceApprovalPage() {
  const [performances, setPerformances] = useState<Performance[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedStatus, setSelectedStatus] = useState("ALL")
  const [selectedPerformance, setSelectedPerformance] = useState<Performance | null>(null)
  const { toast } = useToast()

  const fetchPerformances = async (page: number) => {
    try {
      const statusQuery = selectedStatus === 'ALL' ? '' : `&status=${selectedStatus}`;
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/performances?page=${page}&size=10${statusQuery}`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('데이터를 가져오는데 실패했습니다')
      }

      const data = await response.json()
      setPerformances(data.content)
      setTotalCount(data.totalElements)
    } catch (error) {
      console.error('Error fetching performances:', error)
      toast({
        title: "오류",
        description: "데이터를 가져오는데 실패했습니다",
        variant: "destructive"
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleApprove = async (id: number) => {
    try {
      const csrfToken = await getCsrfToken()
      if (!csrfToken) {
        console.error('CSRF 토큰을 가져올 수 없습니다')
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/performances/${id}/confirm`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      })

      if (!response.ok) {
        throw new Error('승인 처리에 실패했습니다')
      }

      toast({
        title: "성공",
        description: "공연이 승인되었습니다"
      })
      fetchPerformances(currentPage)
      setSelectedPerformance(null)
    } catch (error) {
      console.error('Error approving performance:', error)
      toast({
        title: "오류",
        description: "승인 처리에 실패했습니다",
        variant: "destructive"
      })
    }
  }

  const handleReject = async (id: number) => {
    try {
      const csrfToken = await getCsrfToken()
      if (!csrfToken) {
        console.error('CSRF 토큰을 가져올 수 없습니다')
        return
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/performances/${id}/reject`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Accept': 'application/json',
          'X-XSRF-TOKEN': csrfToken
        }
      })

      if (!response.ok) {
        throw new Error('거절 처리에 실패했습니다')
      }

      toast({
        title: "성공",
        description: "공연이 거절되었습니다"
      })
      fetchPerformances(currentPage)
      setSelectedPerformance(null)
    } catch (error) {
      console.error('Error rejecting performance:', error)
      toast({
        title: "오류",
        description: "거절 처리에 실패했습니다",
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
    fetchPerformances(currentPage)
  }, [currentPage, selectedStatus])

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold tracking-tight">공연 승인 관리</h1>
          <p className="text-muted-foreground">공연 등록 요청을 관리하고 승인할 수 있습니다.</p>
        </div>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <div>
              <CardTitle>공연 승인 목록</CardTitle>
              <CardDescription>총 {totalCount}개의 공연이 있습니다.</CardDescription>
            </div>
            <div className="w-[200px]">
              <Select value={selectedStatus} onValueChange={setSelectedStatus}>
                <SelectTrigger>
                  <SelectValue placeholder="상태 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">전체</SelectItem>
                  <SelectItem value="PENDING">승인 대기</SelectItem>
                  <SelectItem value="CONFIRMED">공연 승인</SelectItem>
                  <SelectItem value="REJECTED">공연 거부</SelectItem>
                  <SelectItem value="CANCELLED">공연 취소</SelectItem>
                  <SelectItem value="COMPLETED">공연 완료</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="py-10 text-center text-muted-foreground">로딩 중...</div>
            ) : performances.length === 0 ? (
              <div className="py-10 text-center text-muted-foreground">공연이 없습니다</div>
            ) : (
              <div className="relative w-full overflow-auto">
                <table className="w-full caption-bottom text-sm">
                  <thead className="border-b">
                    <tr className="text-left">
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">공연명</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">관리자</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">장소</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">가격</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">좌석 수</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">공연 기간</th>
                      <th className="h-10 px-2 align-middle font-medium whitespace-nowrap">상태</th>
                      <th className="h-10 px-2 align-middle font-medium text-right whitespace-nowrap">상세</th>
                    </tr>
                  </thead>
                  <tbody>
                    {performances.map((performance) => (
                      <tr key={performance.id} className="border-b transition-colors hover:bg-muted/50">
                        <td className="p-2 align-middle whitespace-nowrap">{performance.title}</td>
                        <td className="p-2 align-middle whitespace-nowrap">{performance.performanceManagerName}</td>
                        <td className="p-2 align-middle whitespace-nowrap">{performance.venue}</td>
                        <td className="p-2 align-middle whitespace-nowrap">{performance.price.toLocaleString()}원</td>
                        <td className="p-2 align-middle whitespace-nowrap">{performance.totalSeats}석</td>
                        <td className="p-2 align-middle whitespace-nowrap">
                          {new Date(performance.startDate).toLocaleDateString()} ~ {new Date(performance.endDate).toLocaleDateString()}
                        </td>
                        <td className="p-2 align-middle whitespace-nowrap">
                          <Badge variant={getStatusBadgeVariant(performance.status)}>
                            {statusMap[performance.status]}
                          </Badge>
                        </td>
                        <td className="p-2 align-middle text-right whitespace-nowrap">
                          <Button 
                            size="sm" 
                            variant="outline"
                            onClick={() => setSelectedPerformance(performance)}
                          >
                            상세보기
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
          <CardFooter className="flex items-center justify-between">
            <div className="text-sm text-muted-foreground">총 {totalCount}개 중 {performances.length}개 표시</div>
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
      </div>

      {/* 상세 정보 모달 */}
      <Dialog open={!!selectedPerformance} onOpenChange={() => setSelectedPerformance(null)}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>공연 상세 정보</DialogTitle>
            <DialogDescription>공연의 상세 정보를 확인하고 승인 여부를 결정할 수 있습니다.</DialogDescription>
          </DialogHeader>
          {selectedPerformance && (
            <div className="space-y-6">
              <div className="flex gap-6">
                <div className="relative w-40 h-52 bg-white rounded-md overflow-hidden border">
                  <Image
                    src={selectedPerformance.fileUrl || "/logo-icon.png"}
                    alt={selectedPerformance.title}
                    fill
                    className="object-cover"
                  />
                </div>
                <div className="flex-1 space-y-4">
                  <div>
                    <h3 className="text-xl font-semibold">{selectedPerformance.title}</h3>
                    <p className="text-sm text-muted-foreground">관리자: {selectedPerformance.performanceManagerName}</p>
                  </div>
                  <div className="grid gap-2 text-sm">
                    <div>장소: {selectedPerformance.venue}</div>
                    <div>가격: {selectedPerformance.price.toLocaleString()}원</div>
                    <div>좌석 수: {selectedPerformance.totalSeats}석</div>
                    <div>카테고리: {categoryMap[selectedPerformance.category] || selectedPerformance.category}</div>
                    <div>기간: {new Date(selectedPerformance.startDate).toLocaleDateString()} ~ {new Date(selectedPerformance.endDate).toLocaleDateString()}</div>
                  </div>
                </div>
              </div>

              <div>
                <h4 className="font-medium mb-2">공연 설명</h4>
                <p className="text-sm whitespace-pre-wrap">{selectedPerformance.description}</p>
              </div>

              <div>
                <h4 className="font-medium mb-2">공연 회차</h4>
                <div className="space-y-2">
                  {selectedPerformance.schedules.map((schedule, idx) => (
                    <div key={schedule.id} className="text-sm">
                      {idx + 1}회차: {new Date(schedule.startTime).toLocaleString()} ~ {new Date(schedule.endTime).toLocaleString()}
                    </div>
                  ))}
                </div>
              </div>

              {selectedPerformance.status === "PENDING" && (
                <div className="flex justify-end gap-2 pt-4">
                  <Button variant="outline" onClick={() => setSelectedPerformance(null)}>
                    취소
                  </Button>
                  <Button variant="destructive" onClick={() => handleReject(selectedPerformance.id)}>
                    거부
                  </Button>
                  <Button onClick={() => handleApprove(selectedPerformance.id)}>
                    승인
                  </Button>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
