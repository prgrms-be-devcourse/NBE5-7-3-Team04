"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Search, Eye } from "lucide-react"
import Link from "next/link"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useEffect, useState } from "react"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"
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
  startDate: string
  endDate: string
  description: string
  schedules: PerformanceSchedule[]
}

const categoryMap: Record<string, string> = {
  OPERA: "오페라",
  DANCING: "춤",
  SINGING: "노래"
};

export default function PerformanceApprovalPage() {
  const [performances, setPerformances] = useState<Performance[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const { toast } = useToast()

  const fetchPerformances = async (page: number) => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/performances?page=${page}&size=5`, {
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
    if ((currentPage + 1) * 5 < totalCount) {
      setCurrentPage(prev => prev + 1)
    }
  }

  useEffect(() => {
    fetchPerformances(currentPage)
  }, [currentPage])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">공연 등록 승인</h1>
        <p className="text-muted-foreground">공연 등록 또는 수정 승인을 기다리는 공연 목록입니다.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>공연 승인 목록</CardTitle>
          <CardDescription>총 {totalCount}개의 승인 요청이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="py-10 text-center text-muted-foreground">로딩 중...</div>
          ) : performances.length === 0 ? (
            <div className="py-10 text-center text-muted-foreground">승인 대기 중인 공연이 없습니다</div>
          ) : (
            <div className="space-y-6">
              {performances.map((performance) => (
                <div key={performance.id} className="flex flex-col gap-4 border rounded-lg p-4 bg-muted/50">
                  {/* 상단: 이미지 + 정보 */}
                  <div className="flex flex-col sm:flex-row gap-6">
                    {/* 이미지 */}
                    <div className="flex-shrink-0 flex items-center justify-center w-full sm:w-48">
                      <div className="relative w-40 h-40 bg-white rounded-md overflow-hidden border">
                        <Image
                          src={performance.fileUrl || "/logo-icon.png"}
                          alt={performance.title}
                          fill
                          className="object-cover"
                        />
                      </div>
                    </div>
                    {/* 정보 블럭 */}
                    <div className="flex-1 flex flex-col gap-2">
                      <div className="text-lg font-bold">{performance.title}</div>
                      <div className="text-sm text-muted-foreground">관리자: {performance.performanceManagerName}</div>
                      <div className="text-sm">장소: {performance.venue}</div>
                      <div className="text-sm">가격: {performance.price.toLocaleString()}원</div>
                      <div className="text-sm">좌석 수: {performance.totalSeats}석</div>
                      <div className="text-sm">카테고리: {categoryMap[performance.category] || performance.category}</div>
                      <div className="text-sm">기간: {new Date(performance.startDate).toLocaleDateString()} ~ {new Date(performance.endDate).toLocaleDateString()}</div>
                      <div className="text-sm">설명: {performance.description}</div>
                      <div className="text-sm mt-2 font-semibold">회차</div>
                      <div className="flex flex-col gap-1 text-[1.2em]">
                        {(performance.schedules && performance.schedules.length > 0) ? (
                          performance.schedules.map((schedule, idx) => (
                            <div key={schedule.id} className="text-xs text-muted-foreground pl-2">
                              {idx + 1}회차: {new Date(schedule.startTime).toLocaleString()} ~ {new Date(schedule.endTime).toLocaleString()}
                            </div>
                          ))
                        ) : (
                          <span className="text-xs text-muted-foreground pl-2">회차 없음</span>
                        )}
                      </div>
                    </div>
                  </div>
                  {/* 하단: 버튼 */}
                  <div className="flex flex-row gap-2 justify-end mt-4">
                    <Button 
                      size="sm" 
                      variant="outline" 
                      className="h-8 w-8 p-0"
                      onClick={() => handleApprove(performance.id)}
                    >
                      <CheckCircle className="h-4 w-4 text-green-500" />
                      <span className="sr-only">승인</span>
                    </Button>
                    <Button 
                      size="sm" 
                      variant="outline" 
                      className="h-8 w-8 p-0"
                      onClick={() => handleReject(performance.id)}
                    >
                      <XCircle className="h-4 w-4 text-red-500" />
                      <span className="sr-only">거절</span>
                    </Button>
                  </div>
                </div>
              ))}
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
              disabled={(currentPage + 1) * 5 >= totalCount}
            >
              다음
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
