"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Search } from "lucide-react"
import { useEffect, useState } from "react"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"

interface ManagerRequest {
  id: number
  userId: number
  userName: string
  phoneNumber: string
}

export default function RoleApprovalPage() {
  const [requests, setRequests] = useState<ManagerRequest[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const { toast } = useToast()

  const fetchRequests = async (page: number) => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/pending-manager-requests?page=${page}&size=10`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('데이터를 가져오는데 실패했습니다')
      }

      const data = await response.json()
      setRequests(data.content.map((item: any) => ({
        id: item.id,
        userId: item.userId,
        userName: item.userName,
        phoneNumber: item.phoneNumber
      })))
      setTotalCount(data.totalElements)
    } catch (error) {
      console.error('Error fetching requests:', error)
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/manager-requests/${id}/approve`, {
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
        description: "공연자 권한이 승인되었습니다"
      })
      fetchRequests(currentPage)
    } catch (error) {
      console.error('Error approving request:', error)
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

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/admin/manager-requests/${id}/reject`, {
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
        description: "공연자 권한 요청이 거절되었습니다"
      })
      fetchRequests(currentPage)
    } catch (error) {
      console.error('Error rejecting request:', error)
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
    fetchRequests(currentPage)
  }, [currentPage])

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">공연 매니저 권한 승인</h1>
        <p className="text-muted-foreground">
          공연 매니저 권한을 신청한 사용자 목록입니다. 승인 또는 거절할 수 있습니다.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>권한 신청 사용자 목록</CardTitle>
          <CardDescription>총 {totalCount}개의 신청이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>신청자</TableHead>
                <TableHead>전화번호</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">승인 / 거부</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={4} className="text-center">로딩 중...</TableCell>
                </TableRow>
              ) : requests.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="text-center">신청 내역이 없습니다</TableCell>
                </TableRow>
              ) : (
                requests.map((request) => (
                  <TableRow key={request.id}>
                    <TableCell className="font-medium">{request.userName}</TableCell>
                    <TableCell>{request.phoneNumber}</TableCell>
                    <TableCell>
                      <Badge variant="outline">대기중</Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button 
                          size="sm" 
                          variant="outline" 
                          className="h-8 w-8 p-0"
                          onClick={() => handleApprove(request.id)}
                        >
                          <CheckCircle className="h-4 w-4 text-green-500" />
                          <span className="sr-only">승인</span>
                        </Button>
                        <Button 
                          size="sm" 
                          variant="outline" 
                          className="h-8 w-8 p-0"
                          onClick={() => handleReject(request.id)}
                        >
                          <XCircle className="h-4 w-4 text-red-500" />
                          <span className="sr-only">거절</span>
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">총 {totalCount}개 중 {requests.length}개 표시</div>
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
  )
}
