"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Search, Eye } from "lucide-react"
import { useEffect, useState } from "react"
import { useToast } from "@/components/ui/use-toast"
import { getCsrfToken } from "@/lib/admin-auth"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

interface ManagerRequest {
  id: number
  userId: number
  userName: string
  phoneNumber: string
  organizationName: string
  organizationContact: string
  experience: string
  reason: string
}

export default function RoleApprovalPage() {
  const [requests, setRequests] = useState<ManagerRequest[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [selectedRequest, setSelectedRequest] = useState<ManagerRequest | null>(null)
  const [isDetailOpen, setIsDetailOpen] = useState(false)
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
        phoneNumber: item.phoneNumber,
        organizationName: item.organizationName,
        organizationContact: item.organizationContact,
        experience: item.experience,
        reason: item.reason
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
      setIsDetailOpen(false)
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
      setIsDetailOpen(false)
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

  const handleViewDetails = (request: ManagerRequest) => {
    setSelectedRequest(request)
    setIsDetailOpen(true)
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
                <TableHead className="text-right">작업</TableHead>
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
                          onClick={() => handleViewDetails(request)}
                        >
                          <Eye className="h-4 w-4" />
                          <span className="sr-only">상세보기</span>
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

      <Dialog open={isDetailOpen} onOpenChange={setIsDetailOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>공연 매니저 권한 신청 상세</DialogTitle>
            <DialogDescription>
              신청자의 상세 정보를 확인하고 승인 또는 거절할 수 있습니다.
            </DialogDescription>
          </DialogHeader>
          {selectedRequest && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h4 className="font-medium mb-1">신청자 정보</h4>
                  <p className="text-sm text-muted-foreground">이름: {selectedRequest.userName}</p>
                  <p className="text-sm text-muted-foreground">전화번호: {selectedRequest.phoneNumber}</p>
                </div>
                <div>
                  <h4 className="font-medium mb-1">단체 정보</h4>
                  <p className="text-sm text-muted-foreground">단체명: {selectedRequest.organizationName}</p>
                  <p className="text-sm text-muted-foreground">연락처: {selectedRequest.organizationContact}</p>
                </div>
              </div>
              <div>
                <h4 className="font-medium mb-1">공연 경험</h4>
                <p className="text-sm text-muted-foreground whitespace-pre-wrap">{selectedRequest.experience}</p>
              </div>
              <div>
                <h4 className="font-medium mb-1">신청 사유</h4>
                <p className="text-sm text-muted-foreground whitespace-pre-wrap">{selectedRequest.reason}</p>
              </div>
              <div className="flex justify-end gap-2">
                <Button 
                  variant="outline" 
                  onClick={() => handleReject(selectedRequest.id)}
                >
                  거절
                </Button>
                <Button 
                  onClick={() => handleApprove(selectedRequest.id)}
                >
                  승인
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
