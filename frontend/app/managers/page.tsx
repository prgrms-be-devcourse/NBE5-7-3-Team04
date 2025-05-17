"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { getManagerPerformances } from "@/lib/api-manager"
import { Loader2, AlertCircle, Calendar, Users, Ticket, CreditCard } from "lucide-react"
import Link from "next/link"
import { useAuth } from "@/lib/auth"
import { useRouter } from "next/navigation"

export default function ManagerDashboardPage() {
  const [performances, setPerformances] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { isLoading: authLoading, userRole } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (authLoading) return;
    if (userRole !== "MANAGER") {
      router.push("/login")
      return;
    }
    const fetchPerformances = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getManagerPerformances(0, 5)
        setPerformances(data.content || [])
      } catch (err) {
        console.error("공연 목록 가져오기 오류:", err)
        setError("공연 목록을 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }
    fetchPerformances()
  }, [authLoading, userRole])

  // 공연 상태별 개수 계산
  const pendingCount = performances.filter((p) => p.status === "PENDING").length
  const confirmedCount = performances.filter((p) => p.status === "CONFIRMED").length
  const cancelledCount = performances.filter((p) => p.status === "CANCELLED").length
  const completedCount = performances.filter((p) => p.status === "COMPLETED").length

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">공연 관리자 대시보드</h1>
          <p className="text-muted-foreground mt-1">공연 등록 및 관리, 정산 신청을 할 수 있습니다.</p>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">등록된 공연</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{performances.length}</div>
              <p className="text-xs text-muted-foreground">총 등록된 공연 수</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">승인 대기 중</CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{pendingCount}</div>
              <p className="text-xs text-muted-foreground">승인 대기 중인 공연 수</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">진행 중</CardTitle>
              <Ticket className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{confirmedCount}</div>
              <p className="text-xs text-muted-foreground">현재 진행 중인 공연 수</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">완료/취소</CardTitle>
              <CreditCard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{completedCount + cancelledCount}</div>
              <p className="text-xs text-muted-foreground">완료 또는 취소된 공연 수</p>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Card className="col-span-2">
            <CardHeader>
              <CardTitle>최근 공연</CardTitle>
              <CardDescription>최근에 등록한 공연 목록입니다.</CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin text-primary" />
                </div>
              ) : error ? (
                <div className="flex items-center gap-2 rounded-lg border p-4 text-sm text-destructive">
                  <AlertCircle className="h-4 w-4" />
                  <p>{error}</p>
                </div>
              ) : performances.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-8 text-center">
                  <p className="text-muted-foreground mb-4">등록된 공연이 없습니다.</p>
                  <Button asChild>
                    <Link href="/managers/register">공연 등록하기</Link>
                  </Button>
                </div>
              ) : (
                <div className="space-y-4">
                  <Tabs defaultValue="all">
                    <TabsList className="grid w-full grid-cols-4">
                      <TabsTrigger value="all">전체</TabsTrigger>
                      <TabsTrigger value="pending">대기중</TabsTrigger>
                      <TabsTrigger value="confirmed">승인됨</TabsTrigger>
                      <TabsTrigger value="others">기타</TabsTrigger>
                    </TabsList>
                    <TabsContent value="all" className="mt-4">
                      <div className="space-y-2">
                        {performances.map((performance) => (
                          <div key={performance.id} className="flex items-center justify-between rounded-lg border p-3">
                            <div className="flex flex-col">
                              <span className="font-medium">{performance.title}</span>
                              <span className="text-sm text-muted-foreground">{performance.venue}</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <span
                                className={`text-xs px-2 py-1 rounded-full ${
                                  performance.status === "PENDING"
                                    ? "bg-yellow-100 text-yellow-800"
                                    : performance.status === "CONFIRMED"
                                      ? "bg-green-100 text-green-800"
                                      : performance.status === "REJECTED"
                                        ? "bg-red-100 text-red-800"
                                        : performance.status === "CANCELLED"
                                          ? "bg-gray-100 text-gray-800"
                                          : "bg-blue-100 text-blue-800"
                                }`}
                              >
                                {performance.status === "PENDING"
                                  ? "대기중"
                                  : performance.status === "CONFIRMED"
                                    ? "승인됨"
                                    : performance.status === "REJECTED"
                                      ? "거절됨"
                                      : performance.status === "CANCELLED"
                                        ? "취소됨"
                                        : "완료됨"}
                              </span>
                              <Button variant="outline" size="sm" asChild>
                                <Link href={`/managers/performances/${performance.id}`}>상세</Link>
                              </Button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </TabsContent>
                    <TabsContent value="pending" className="mt-4">
                      <div className="space-y-2">
                        {performances
                          .filter((p) => p.status === "PENDING")
                          .map((performance) => (
                            <div
                              key={performance.id}
                              className="flex items-center justify-between rounded-lg border p-3"
                            >
                              <div className="flex flex-col">
                                <span className="font-medium">{performance.title}</span>
                                <span className="text-sm text-muted-foreground">{performance.venue}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-1 rounded-full">
                                  대기중
                                </span>
                                <Button variant="outline" size="sm" asChild>
                                  <Link href={`/managers/performances/${performance.id}`}>상세</Link>
                                </Button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </TabsContent>
                    <TabsContent value="confirmed" className="mt-4">
                      <div className="space-y-2">
                        {performances
                          .filter((p) => p.status === "CONFIRMED")
                          .map((performance) => (
                            <div
                              key={performance.id}
                              className="flex items-center justify-between rounded-lg border p-3"
                            >
                              <div className="flex flex-col">
                                <span className="font-medium">{performance.title}</span>
                                <span className="text-sm text-muted-foreground">{performance.venue}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <span className="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full">
                                  승인됨
                                </span>
                                <Button variant="outline" size="sm" asChild>
                                  <Link href={`/managers/performances/${performance.id}`}>상세</Link>
                                </Button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </TabsContent>
                    <TabsContent value="others" className="mt-4">
                      <div className="space-y-2">
                        {performances
                          .filter((p) => p.status !== "PENDING" && p.status !== "CONFIRMED")
                          .map((performance) => (
                            <div
                              key={performance.id}
                              className="flex items-center justify-between rounded-lg border p-3"
                            >
                              <div className="flex flex-col">
                                <span className="font-medium">{performance.title}</span>
                                <span className="text-sm text-muted-foreground">{performance.venue}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <span
                                  className={`text-xs px-2 py-1 rounded-full ${
                                    performance.status === "REJECTED"
                                      ? "bg-red-100 text-red-800"
                                      : performance.status === "CANCELLED"
                                        ? "bg-gray-100 text-gray-800"
                                        : "bg-blue-100 text-blue-800"
                                  }`}
                                >
                                  {performance.status === "REJECTED"
                                    ? "거절됨"
                                    : performance.status === "CANCELLED"
                                      ? "취소됨"
                                      : "완료됨"}
                                </span>
                                <Button variant="outline" size="sm" asChild>
                                  <Link href={`/managers/performances/${performance.id}`}>상세</Link>
                                </Button>
                              </div>
                            </div>
                          ))}
                      </div>
                    </TabsContent>
                  </Tabs>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>빠른 메뉴</CardTitle>
              <CardDescription>자주 사용하는 기능에 빠르게 접근하세요.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button className="w-full" asChild>
                <Link href="/managers/register">공연 등록하기</Link>
              </Button>
              <Button className="w-full" variant="outline" asChild>
                <Link href="/managers/performances">공연 목록 보기</Link>
              </Button>
              <Button className="w-full" variant="outline" asChild>
                <Link href="/managers/settlements/request">정산 신청하기</Link>
              </Button>
              <Button className="w-full" variant="outline" asChild>
                <Link href="/managers/settlements/history">정산 내역 보기</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
