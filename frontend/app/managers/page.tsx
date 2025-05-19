"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Loader2, AlertCircle, Calendar, Users, Ticket, CreditCard } from "lucide-react"
import Link from "next/link"
import { useAuth } from "@/src/auth/user"
import { useRouter } from "next/navigation"
import { getManagerPerformancesV1, ManagerPerformance, getManagerPerformanceDetailV1, ManagerPerformanceDetail } from "@/src/api/api-manager"
import { format } from "date-fns"

export default function ManagerDashboardPage() {
  const [performances, setPerformances] = useState<ManagerPerformance[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState("all")
  const { isLoading: authLoading, userRole } = useAuth()
  const router = useRouter()
  const [detail, setDetail] = useState<ManagerPerformanceDetail | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  // 최근 3개월 공연만 필터링
  const now = new Date()
  const threeMonthsAgo = new Date(now)
  threeMonthsAgo.setMonth(now.getMonth() - 3)
  const filterRecent = (list: ManagerPerformance[]) =>
    list.filter((p) => {
      const createdAt = p.startDate ? new Date(p.startDate) : null
      return createdAt && createdAt >= threeMonthsAgo
    })

  // 탭별 상태값 매핑
  const statusMap: Record<string, string | undefined> = {
    all: undefined,
    pending: "PENDING",
    confirmed: "CONFIRMED",
    completed: "COMPLETED",
    others: "REJECTED,CANCELLED",
  }

  // 공연 상태별 필터링 함수
  const filterByTab = (list: ManagerPerformance[], tab: string) => {
    if (tab === "all") return list;
    if (tab === "pending") return list.filter(p => p.status === "PENDING");
    if (tab === "confirmed") return list.filter(p => p.status === "CONFIRMED");
    if (tab === "completed") return list.filter(p => p.status === "COMPLETED");
    if (tab === "others") return list.filter(p => ["CANCELLED", "REJECTED"].includes(p.status));
    return [];
  };

  useEffect(() => {
    if (authLoading) return;
    if (userRole !== "MANAGER") {
      router.push("/login");
      return;
    }
    const fetchPerformances = async () => {
      try {
        setLoading(true);
        setError(null);
        const res = await getManagerPerformancesV1(0, 100, ["startDate,desc"]);
        setPerformances(filterRecent(res.content || []));
      } catch (err) {
        setError("공연 목록을 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };
    fetchPerformances();
  }, [authLoading, userRole]);

  // 최신순 정렬 (startDate)
  const sortedPerformances = [...performances].sort((a, b) => {
    const aDate = a.startDate ? new Date(a.startDate) : new Date(0);
    const bDate = b.startDate ? new Date(b.startDate) : new Date(0);
    return bDate.getTime() - aDate.getTime();
  });

  // 공연 상태별 개수 계산
  const pendingCount = performances.filter((p) => p.status === "PENDING").length
  const confirmedCount = performances.filter((p) => p.status === "CONFIRMED").length
  const cancelledCount = performances.filter((p) => p.status === "CANCELLED").length
  const completedCount = performances.filter((p) => p.status === "COMPLETED").length

  // 상세 보기 핸들러
  const handleShowDetail = async (performanceId: number | string) => {
    setDetailLoading(true)
    setDetailError(null)
    try {
      const res = await getManagerPerformanceDetailV1(performanceId)
      setDetail(res)
    } catch (e) {
      setDetailError("상세 정보를 불러오는 중 오류가 발생했습니다.")
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <div className="container py-4 overflow-y-hidden">
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">공연 관리자 대시보드</h1>
          <p className="text-muted-foreground" style={{marginTop: '10px'}}>공연 등록 및 관리, 정산 신청을 할 수 있습니다.</p>
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
              <CardDescription>최근 3개월간 등록한 공연 목록입니다.</CardDescription>
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
              ) : (
                <div className="space-y-4">
                  <Tabs value={tab} onValueChange={setTab} defaultValue="all">
                    <TabsList className="grid w-full grid-cols-5">
                      <TabsTrigger value="all">전체</TabsTrigger>
                      <TabsTrigger value="pending">승인대기중</TabsTrigger>
                      <TabsTrigger value="confirmed">승인완료</TabsTrigger>
                      <TabsTrigger value="completed">공연완료</TabsTrigger>
                      <TabsTrigger value="others">기타</TabsTrigger>
                    </TabsList>
                    {['all', 'pending', 'confirmed', 'completed', 'others'].map(tabKey => {
                      const tabPerformances = filterByTab(sortedPerformances, tabKey);
                      return (
                        <TabsContent value={tabKey} className="mt-4" key={tabKey}>
                          <div style={{ height: '300px', overflowY: 'auto' }}>
                            {tabPerformances.length === 0 ? (
                              <div className="flex flex-col items-center justify-center py-8 text-center text-muted-foreground">
                                <p>해당 조건에 맞는 공연이 없습니다.</p>
                              </div>
                            ) : (
                              <div className="space-y-2">
                                {tabPerformances.map((performance) => (
                                  <div key={performance.id} className="flex items-center justify-between rounded-lg border p-3">
                                    <div className="flex flex-row items-center gap-4 w-full">
                                      <span className="font-medium min-w-[120px]">{performance.title}</span>
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
                                        style={{ marginLeft: 8 }}
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
                                      <div className="flex flex-col text-xs text-muted-foreground ml-4">
                                        <span>{performance.venue}</span>
                                        <span>{format(new Date(performance.startDate), "yyyy.MM.dd HH:mm")}</span>
                                      </div>
                                    </div>
                                    <div className="flex items-center gap-2">
                                      <Button variant="outline" size="sm" onClick={() => handleShowDetail(performance.id)}>
                                        상세
                                      </Button>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        </TabsContent>
                      )
                    })}
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

      {/* 상세 모달 */}
      {detail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>{detail.title}</CardTitle>
              <CardDescription>{detail.venue}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="mb-2 text-xs text-muted-foreground">
                공연ID: {detail.id} 
              </div>
              <div className="mb-2 text-xs text-muted-foreground">
                기간: {format(new Date(detail.startDate), "yyyy.MM.dd HH:mm")} ~ {format(new Date(detail.endDate), "yyyy.MM.dd HH:mm")}
              </div>
              <div className="mb-2 text-xs text-muted-foreground">
                상태: {detail.status === 'PENDING' ? '대기중'
                  : detail.status === 'CONFIRMED' ? '승인됨'
                  : detail.status === 'REJECTED' ? '거절됨'
                  : detail.status === 'CANCELLED' ? '취소됨'
                  : detail.status === 'COMPLETED' ? '완료됨'
                  : detail.status}
              </div>
            </CardContent>
            <div className="flex justify-end p-4">
              <Button variant="outline" onClick={() => setDetail(null)}>닫기</Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  )
}
