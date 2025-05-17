"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, AlertCircle, CheckCircle, Copy, Check } from "lucide-react"
import { api } from "@/src/api/api"
import { ApiEndpointSelector } from "@/components/api-endpoint-selector"

// 미리 정의된 API 엔드포인트 목록 부분에서 중복된 키를 수정합니다.
// 북마크 추가와 취소가 동일한 value를 가지고 있어 문제가 발생했습니다.
// 각 엔드포인트에 고유한 id를 추가하여 해결합니다.

const predefinedEndpoints = [
  {
    id: "performances-list",
    value: "/users/performances",
    label: "공연 목록 조회",
    method: "GET",
    description: "페이지네이션 지원 (page, size)",
  },
  {
    id: "performance-detail",
    value: "/users/performances/1",
    label: "공연 상세 조회",
    method: "GET",
    description: "ID로 공연 상세 정보 조회",
  },
  {
    id: "performances-search",
    value: "/users/search",
    label: "공연 검색",
    method: "GET",
    description: "제목, 장소, 날짜, 카테고리로 검색",
  },
  {
    id: "reservation-create",
    value: "/reservations",
    label: "예약 생성",
    method: "POST",
    description: "scheduleId, quantity 필요",
  },
  {
    id: "my-reservations",
    value: "/reservations/me",
    label: "내 예약 목록",
    method: "GET",
    description: "페이지네이션 지원",
  },
  {
    id: "bookmark-list",
    value: "/bookmark",
    label: "북마크 목록",
    method: "GET",
    description: "사용자의 북마크한 공연 목록",
  },
  {
    id: "user-info",
    value: "/users/me",
    label: "내 정보 조회",
    method: "GET",
    description: "현재 로그인한 사용자 정보",
  },
  {
    id: "review-create",
    value: "/reviews",
    label: "리뷰 작성",
    method: "POST",
    description: "performanceId, scheduledId, comments 필요",
  },
  {
    id: "bookmark-add",
    value: "/bookmark/1",
    label: "북마크 추가",
    method: "POST",
    description: "공연 ID 필요",
  },
  {
    id: "bookmark-remove",
    value: "/bookmark/1",
    label: "북마크 취소",
    method: "PATCH",
    description: "공연 ID 필요",
  },
  {
    id: "reservation-cancel",
    value: "/reservations/1/cancel",
    label: "예약 취소",
    method: "POST",
    description: "예약 ID 필요",
  },
  {
    id: "manager-request",
    value: "/users/manager-request",
    label: "공연 관리자 신청",
    method: "POST",
  },
]

// 메소드별 예제 요청 본문
const exampleRequestBodies: Record<string, Record<string, string>> = {
  "/reservations": {
    POST: JSON.stringify(
      {
        scheduleId: 1,
        quantity: 2,
      },
      null,
      2,
    ),
  },
  "/reviews": {
    POST: JSON.stringify(
      {
        performanceId: 1,
        scheduledId: 1,
        comments: "공연이 정말 좋았습니다!",
      },
      null,
      2,
    ),
  },
  "/users/onboarding": {
    POST: JSON.stringify(
      {
        phoneNumber: "010-1234-5678",
        email: "user@example.com",
      },
      null,
      2,
    ),
  },
  "/refunds": {
    PATCH: JSON.stringify(
      {
        refundId: 1,
        account: "123-456-789",
        bank: "신한은행",
        depositorName: "홍길동",
      },
      null,
      2,
    ),
  },
}

export default function ApiTestClient() {
  const [endpoint, setEndpoint] = useState("/users/performances")
  const [method, setMethod] = useState("GET")
  const [params, setParams] = useState("")
  const [requestBody, setRequestBody] = useState("")
  const [response, setResponse] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [copied, setCopied] = useState(false)
  const [activeTab, setActiveTab] = useState("request")

  // 엔드포인트 변경 시 메소드 및 예제 요청 본문 업데이트
  const handleEndpointChange = (value: string, methodValue: string) => {
    setEndpoint(value)
    setMethod(methodValue)

    // 예제 요청 본문 설정
    if (exampleRequestBodies[value] && exampleRequestBodies[value][methodValue]) {
      setRequestBody(exampleRequestBodies[value][methodValue])
    } else {
      setRequestBody("")
    }
  }

  const handleSendRequest = async () => {
    setLoading(true)
    setError(null)
    setSuccess(false)
    setResponse(null)

    try {
      // URL 파라미터 처리
      let finalEndpoint = endpoint
      if (params && method === "GET") {
        finalEndpoint = `${endpoint}?${params}`
      }

      // 요청 옵션 설정
      const options: RequestInit = {
        method,
      }

      // POST, PUT, PATCH 요청에 대한 body 추가
      if (["POST", "PUT", "PATCH"].includes(method) && requestBody) {
        try {
          // JSON 형식 검증
          JSON.parse(requestBody)
          options.body = requestBody
        } catch (e) {
          setError("요청 본문이 유효한 JSON 형식이 아닙니다.")
          setLoading(false)
          return
        }
      }

      // API 요청 실행
      const result = await api.request({
        url: endpoint,
        method: method,
        data: ["POST", "PUT", "PATCH"].includes(method) ? requestBody : undefined
      })
      setResponse(result)
      setSuccess(true)
      setActiveTab("response") // 응답 탭으로 자동 전환
    } catch (err) {
      setError(err instanceof Error ? err.message : "API 요청 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  // JSON 문자열 포맷팅
  const formatJSON = (json: any) => {
    try {
      return JSON.stringify(json, null, 2)
    } catch (e) {
      return "유효하지 않은 JSON 형식입니다."
    }
  }

  // 응답 복사
  const copyResponse = () => {
    if (response) {
      navigator.clipboard.writeText(formatJSON(response))
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold tracking-tight">API 테스트</h1>
          <p className="text-muted-foreground">API 엔드포인트를 테스트하고 응답을 확인할 수 있습니다.</p>
        </div>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList>
            <TabsTrigger value="request">요청</TabsTrigger>
            <TabsTrigger value="response">응답</TabsTrigger>
          </TabsList>
          <TabsContent value="request" className="space-y-4 py-4">
            <Card>
              <CardHeader>
                <CardTitle>API 요청 설정</CardTitle>
                <CardDescription>테스트할 API 엔드포인트와 파라미터를 설정하세요.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="md:col-span-3">
                    <Label htmlFor="endpoint" className="mb-2 block">
                      엔드포인트
                    </Label>
                    <ApiEndpointSelector
                      endpoints={predefinedEndpoints}
                      value={endpoint}
                      onSelect={handleEndpointChange}
                    />
                  </div>
                  <div>
                    <Label htmlFor="method" className="mb-2 block">
                      메소드
                    </Label>
                    <Select value={method} onValueChange={setMethod}>
                      <SelectTrigger>
                        <SelectValue placeholder="메소드 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="GET">GET</SelectItem>
                        <SelectItem value="POST">POST</SelectItem>
                        <SelectItem value="PUT">PUT</SelectItem>
                        <SelectItem value="PATCH">PATCH</SelectItem>
                        <SelectItem value="DELETE">DELETE</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                {method === "GET" && (
                  <div>
                    <Label htmlFor="params" className="mb-2 block">
                      URL 파라미터 (예: page=0&size=10)
                    </Label>
                    <Input
                      id="params"
                      value={params}
                      onChange={(e) => setParams(e.target.value)}
                      placeholder="page=0&size=10"
                    />
                  </div>
                )}

                {["POST", "PUT", "PATCH"].includes(method) && (
                  <div>
                    <Label htmlFor="requestBody" className="mb-2 block">
                      요청 본문 (JSON)
                    </Label>
                    <Textarea
                      id="requestBody"
                      value={requestBody}
                      onChange={(e) => setRequestBody(e.target.value)}
                      placeholder='{"key": "value"}'
                      className="font-mono h-40"
                    />
                  </div>
                )}
              </CardContent>
              <CardFooter>
                <Button onClick={handleSendRequest} disabled={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      요청 중...
                    </>
                  ) : (
                    "요청 보내기"
                  )}
                </Button>
              </CardFooter>
            </Card>

            {error && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            {success && (
              <Alert className="bg-green-50 text-green-800 border-green-200">
                <CheckCircle className="h-4 w-4 text-green-500" />
                <AlertDescription>요청이 성공적으로 처리되었습니다.</AlertDescription>
              </Alert>
            )}
          </TabsContent>

          <TabsContent value="response" className="py-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <div>
                  <CardTitle>API 응답</CardTitle>
                  <CardDescription>API 요청에 대한 응답 결과입니다.</CardDescription>
                </div>
                {response && (
                  <Button variant="outline" size="sm" onClick={copyResponse} className="ml-auto">
                    {copied ? (
                      <>
                        <Check className="mr-2 h-4 w-4" />
                        복사됨
                      </>
                    ) : (
                      <>
                        <Copy className="mr-2 h-4 w-4" />
                        복사
                      </>
                    )}
                  </Button>
                )}
              </CardHeader>
              <CardContent>
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                    <span className="ml-2">응답을 기다리는 중...</span>
                  </div>
                ) : response ? (
                  <pre className="bg-muted p-4 rounded-md overflow-auto max-h-[500px] text-sm">
                    {formatJSON(response)}
                  </pre>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">요청을 보내면 여기에 응답이 표시됩니다.</div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}
