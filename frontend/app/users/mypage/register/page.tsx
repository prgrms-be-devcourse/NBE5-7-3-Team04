"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { canRequestManagerRole, submitManagerRequest } from "@/src/api/user"
import { useRouter } from "next/navigation"
import { InfoIcon } from "lucide-react"

export default function RegisterPage() {
  const [canRequest, setCanRequest] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    const checkManagerStatus = async () => {
      try {
        const response = await canRequestManagerRole()
        setCanRequest(response)
        setError(null)
      } catch (error) {
        console.error("Error checking manager status:", error)
        setError("공연 관리자 신청은 로그인 후 가능합니다.")
      } finally {
        setLoading(false)
      }
    }

    checkManagerStatus()
  }, [])

  const handleSubmit = async () => {
    try {
      await submitManagerRequest()
      alert("공연 관리자 신청이 완료되었습니다.")
      router.push("/users/mypage/reservations")
    } catch (error) {
      console.error("Error submitting manager request:", error)
      alert("신청 중 오류가 발생했습니다.")
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">확인 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container py-8">
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="container py-8">
      <Alert className="mb-6">
        <InfoIcon className="h-4 w-4" />
        <AlertTitle>신청 전 안내사항</AlertTitle>
        <AlertDescription>
          공연 관리자 권한은 실제 공연을 주최하거나 관리하는 개인 또는 단체에게 부여됩니다. 신청 후 관리자 검토를 거쳐
          승인됩니다. 승인까지 1-2일이 소요될 수 있습니다.
        </AlertDescription>
      </Alert>

      <Card>
        <CardHeader>
          <CardTitle>공연 관리자 신청</CardTitle>
          <CardDescription>공연을 등록하고 관리하기 위한 관리자 권한을 신청합니다.</CardDescription>
        </CardHeader>
        <CardContent>
          {!canRequest ? (
            <div className="text-center py-4 text-red-500">
              이미 공연 관리자 이거나 승인 대기중입니다.
            </div>
          ) : (
            <div className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="name">이름 (개인 또는 단체명)</Label>
                <Input id="name" placeholder="이름(개인 또는 단체명)을 입력하세요" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">연락처</Label>
                <Input id="email" type="email" placeholder="연락 가능한 전화번호를 입력하세요" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phone">공연 기획/운영 경험</Label>
                <Input id="phone" placeholder="이전 공연 기획 또는 운영 경험을 간략히 설명해주세요" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="reason">신청 사유</Label>
                <Textarea id="reason" placeholder="공연 관리자 권한이 필요한 이유를 설명해주세요" />
              </div>
              <Button 
                className="w-full" 
                onClick={handleSubmit}
              >
                신청하기
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
