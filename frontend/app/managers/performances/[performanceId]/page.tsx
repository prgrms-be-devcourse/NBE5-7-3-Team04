"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import {
  getManagerPerformanceDetails,
  cancelPerformance,
  cancelPerformanceSchedule,
  registerPerformanceSchedule,
} from "@/src/api/api"
import { format, parseISO } from "date-fns"
import { Loader2, AlertCircle, Calendar, Users, Clock, ArrowLeft, Plus, Ban, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { useAuth } from "@/src/auth/user"
import { ScheduleForm } from "@/components/schedule-form"
import { PageProps } from "@/types/route"
import dynamic from "next/dynamic"
import { formatKSTDate, formatKSTDateTime } from "@/src/api/utils/date";

interface PerformanceParams {
  performanceId: string
}

export default async function PerformanceDetailPage({ params }: PageProps<PerformanceParams>) {
  const { performanceId } = await params
  const [performance, setPerformance] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false)
  const [cancelScheduleId, setCancelScheduleId] = useState<number | null>(null)
  const [cancelScheduleDialogOpen, setCancelScheduleDialogOpen] = useState(false)
  const [addScheduleDialogOpen, setAddScheduleDialogOpen] = useState(false)
  const [cancelLoading, setCancelLoading] = useState(false)
  const router = useRouter()
  const { requireRole } = useAuth()

  useEffect(() => {
    requireRole("MANAGER")

    const fetchPerformance = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getManagerPerformanceDetails(performanceId)
        setPerformance(data)
      } catch (err) {
        console.error("공연 상세 정보 가져오기 오류:", err)
        setError("공연 정보를 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchPerformance()
  }, [performanceId, requireRole])

  const handleCancelPerformance = async () => {
    try {
      setCancelLoading(true)
      await cancelPerformance(performanceId)
      setCancelDialogOpen(false)
      // 성공 후 데이터 다시 불러오기
      const data = await getManagerPerformanceDetails(performanceId)
      setPerformance(data)
    } catch (err) {
      console.error("공연 취소 오류:", err)
      setError("공연 취소 중 오류가 발생했습니다.")
    } finally {
      setCancelLoading(false)
    }
  }

  const handleCancelSchedule = async () => {
    if (!cancelScheduleId) return

    try {
      setCancelLoading(true)
      await cancelPerformanceSchedule(performanceId, cancelScheduleId)
      setCancelScheduleDialogOpen(false)
      setCancelScheduleId(null)
      // 성공 후 데이터 다시 불러오기
      const data = await getManagerPerformanceDetails(performanceId)
      setPerformance(data)
    } catch (err) {
      console.error("공연 일정 취소 오류:", err)
      setError("공연 일정 취소 중 오류가 발생했습니다.")
    } finally {
      setCancelLoading(false)
    }
  }

  const handleAddSchedule = async (data: { startTime: string; endTime: string }) => {
    try {
      await registerPerformanceSchedule(performanceId, data)
      setAddScheduleDialogOpen(false)
      // 성공 후 데이터 다시 불러오기
      const updatedData = await getManagerPerformanceDetails(performanceId)
      setPerformance(updatedData)
    } catch (err) {
      console.error("공연 일정 추가 오류:", err)
      setError("공연 일정 추가 중 오류가 발생했습니다.")
    }
  }

  // 공연 상태에 따른 배지 스타일 결정
  const getStatusVariant = (status: string) => {
    switch (status) {
      case "CONFIRMED":
        return "success"
      case "PENDING":
        return "secondary"
      case "REJECTED":
      case "CANCELLED":
        return "destructive"
      case "COMPLETED":
        return "outline"
      default:
        return "outline"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "PENDING":
        return "승인 대기중"
      case "CONFIRMED":
        return "승인됨"
      case "REJECTED":
        return "거절됨"
      case "CANCELLED":
        return "취소됨"
      case "COMPLETED":
        return "완료됨"
      default:
        return status
    }
  }

  if (loading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="mt-4 text-muted-foreground">공연 정보를 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error || !performance) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <AlertCircle className="h-8 w-8 text-destructive" />
          <p className="mt-4 text-destructive font-medium">{error || "공연 정보를 불러올 수 없습니다."}</p>
          <Button variant="outline" className="mt-4" asChild>
            <Link href="/managers/performances">목록으로 돌아가기</Link>
          </Button>
        </div>
      </div>
    )
  }

  const canCancel = performance.status === "PENDING" || performance.status === "CONFIRMED"
  const canAddSchedule = performance.status === "PENDING" || performance.status === "CONFIRMED"
  const canCancelSchedule = performance.status === "CONFIRMED"

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-8">
        <div className="flex items-center gap-4">
          <Button variant="outline" size="icon" asChild>
            <Link href="/managers/performances">
              <ArrowLeft className="h-4 w-4" />
              <span className="sr-only">뒤로 가기</span>
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight md:text-3xl">{performance.title}</h1>
            <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
              <Badge variant={getStatusVariant(performance.status)}>{getStatusText(performance.status)}</Badge>
            </div>
          </div>
        </div>

        {performance.status === "REJECTED" && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertTitle>공연이 거절되었습니다</AlertTitle>
            <AlertDescription>
              관리자에 의해 공연 등록이 거절되었습니다. 자세한 내용은 관리자에게 문의하세요.
            </AlertDescription>
          </Alert>
        )}

        {performance.status === "CANCELLED" && (
          <Alert variant="default">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>공연이 취소되었습니다</AlertTitle>
            <AlertDescription>이 공연은 취소되었습니다. 취소된 공연은 다시 활성화할 수 없습니다.</AlertDescription>
          </Alert>
        )}

        <div className="grid gap-8 md:grid-cols-3">
          <div className="md:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>공연 정보</CardTitle>
                <CardDescription>등록된 공연의 상세 정보입니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">공연 제목</p>
                    <p>{performance.title}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">공연 장소</p>
                    <p>{performance.venue}</p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">공연 기간</p>
                    <p>
                      {formatKSTDateTime(performance.startDate).split(" ")[0]} ~{" "}
                      {formatKSTDateTime(performance.endDate).split(" ")[0]}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">총 좌석 수</p>
                    <p>{performance.totalSeats}석</p>
                  </div>
                </div>

                <Separator />

                <div>
                  <p className="text-sm font-medium text-muted-foreground mb-2">공연 포스터</p>
                  <div className="relative aspect-video overflow-hidden rounded-lg">
                    <img
                      src={performance.fileUrl || "/placeholder.svg?height=400&width=800"}
                      alt={performance.title}
                      className="h-full w-full object-cover"
                      onError={(e) => {
                        e.currentTarget.src = "/placeholder.svg?height=400&width=800"
                      }}
                    />
                  </div>
                </div>
              </CardContent>
              <CardFooter className="flex justify-between">
                <Button variant="outline" asChild>
                  <Link href={`/managers/edit?id=${performance.id}`}>공연 수정</Link>
                </Button>
                {canCancel && (
                  <Button variant="destructive" onClick={() => setCancelDialogOpen(true)}>
                    공연 취소
                  </Button>
                )}
              </CardFooter>
            </Card>

            <Tabs defaultValue="schedules" className="mt-8">
              <TabsList className="w-full grid grid-cols-2">
                <TabsTrigger value="schedules">공연 일정</TabsTrigger>
                <TabsTrigger value="actions">관리 옵션</TabsTrigger>
              </TabsList>

              <TabsContent value="schedules" className="mt-4 space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold">공연 일정 목록</h3>
                  {canAddSchedule && (
                    <Button size="sm" onClick={() => setAddScheduleDialogOpen(true)}>
                      <Plus className="mr-2 h-4 w-4" />
                      일정 추가
                    </Button>
                  )}
                </div>

                {performance.schedules.length === 0 ? (
                  <div className="rounded-lg border border-dashed p-8 text-center">
                    <p className="text-muted-foreground">등록된 공연 일정이 없습니다.</p>
                    {canAddSchedule && (
                      <Button className="mt-4" onClick={() => setAddScheduleDialogOpen(true)}>
                        <Plus className="mr-2 h-4 w-4" />
                        일정 추가하기
                      </Button>
                    )}
                  </div>
                ) : (
                  <div className="space-y-4">
                    {performance.schedules.map((schedule: any) => (
                      <Card key={schedule.id}>
                        <CardContent className="p-4">
                          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                            <div className="space-y-1">
                              <div className="flex items-center gap-2">
                                <Calendar className="h-4 w-4 text-muted-foreground" />
                                <span className="font-medium">{formatKSTDateTime(schedule.startTime)}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <Clock className="h-4 w-4 text-muted-foreground" />
                                <span className="text-sm text-muted-foreground">
                                  {format(parseISO(schedule.startTime), "HH:mm")} ~{" "}
                                  {format(parseISO(schedule.endTime), "HH:mm")}
                                </span>
                              </div>
                              <div className="flex items-center gap-2">
                                <Users className="h-4 w-4 text-muted-foreground" />
                                <span className="text-sm text-muted-foreground">
                                  잔여 좌석: {schedule.remainingSeats}석
                                </span>
                              </div>
                            </div>
                            <div className="flex items-center gap-2">
                              {schedule.isCanceled ? (
                                <Badge variant="destructive">취소됨</Badge>
                              ) : (
                                <>
                                  <Badge variant="outline">활성</Badge>
                                  {canCancelSchedule && (
                                    <Button
                                      variant="outline"
                                      size="sm"
                                      onClick={() => {
                                        setCancelScheduleId(schedule.id)
                                        setCancelScheduleDialogOpen(true)
                                      }}
                                    >
                                      <Ban className="mr-2 h-4 w-4" />
                                      취소
                                    </Button>
                                  )}
                                </>
                              )}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </TabsContent>

              <TabsContent value="actions" className="mt-4">
                <Card>
                  <CardHeader>
                    <CardTitle>관리 옵션</CardTitle>
                    <CardDescription>공연 관리를 위한 추가 옵션입니다.</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="rounded-lg border p-4">
                      <div className="flex items-start gap-4">
                        <div className="flex-1">
                          <h4 className="font-semibold">공연 수정</h4>
                          <p className="text-sm text-muted-foreground">공연 정보를 수정합니다.</p>
                        </div>
                        <Button variant="outline" asChild>
                          <Link href={`/managers/edit?id=${performance.id}`}>수정하기</Link>
                        </Button>
                      </div>
                    </div>

                    {canCancel && (
                      <div className="rounded-lg border p-4">
                        <div className="flex items-start gap-4">
                          <div className="flex-1">
                            <h4 className="font-semibold text-destructive">공연 취소</h4>
                            <p className="text-sm text-muted-foreground">
                              공연을 취소합니다. 이 작업은 되돌릴 수 없습니다.
                            </p>
                          </div>
                          <Button variant="destructive" onClick={() => setCancelDialogOpen(true)}>
                            취소하기
                          </Button>
                        </div>
                      </div>
                    )}

                    <div className="rounded-lg border p-4">
                      <div className="flex items-start gap-4">
                        <div className="flex-1">
                          <h4 className="font-semibold">정산 신청</h4>
                          <p className="text-sm text-muted-foreground">이 공연에 대한 정산을 신청합니다.</p>
                        </div>
                        <Button variant="outline" asChild>
                          <Link href={`/managers/settlements/request?performanceId=${performance.id}`}>정산 신청</Link>
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          </div>

          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>공연 상태</CardTitle>
                <CardDescription>현재 공연의 상태 정보입니다.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium">상태</span>
                  <Badge variant={getStatusVariant(performance.status)}>{getStatusText(performance.status)}</Badge>
                </div>
                <Separator />
                <div className="space-y-1">
                  <span className="text-sm font-medium">총 좌석 수</span>
                  <p>{performance.totalSeats}석</p>
                </div>
                <div className="space-y-1">
                  <span className="text-sm font-medium">등록된 일정</span>
                  <p>{performance.schedules.length}개</p>
                </div>
                <div className="space-y-1">
                  <span className="text-sm font-medium">취소된 일정</span>
                  <p>{performance.schedules.filter((s: any) => s.isCanceled).length}개</p>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>빠른 작업</CardTitle>
                <CardDescription>자주 사용하는 기능에 빠르게 접근하세요.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-2">
                {canAddSchedule && (
                  <Button className="w-full" onClick={() => setAddScheduleDialogOpen(true)}>
                    <Plus className="mr-2 h-4 w-4" />
                    일정 추가
                  </Button>
                )}
                <Button variant="outline" className="w-full" asChild>
                  <Link href={`/managers/edit?id=${performance.id}`}>공연 수정</Link>
                </Button>
                <Button variant="outline" className="w-full" asChild>
                  <Link href={`/managers/settlements/request?performanceId=${performance.id}`}>정산 신청</Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      {/* 공연 취소 확인 다이얼로그 */}
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>공연 취소</DialogTitle>
            <DialogDescription>
              정말로 이 공연을 취소하시겠습니까? 이 작업은 되돌릴 수 없으며, 모든 예매가 취소됩니다.
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Alert variant="destructive">
              <AlertTriangle className="h-4 w-4" />
              <AlertTitle>주의</AlertTitle>
              <AlertDescription>
                공연을 취소하면 모든 예매가 자동으로 취소되고 환불 처리됩니다. 이 작업은 되돌릴 수 없습니다.
              </AlertDescription>
            </Alert>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>
              취소
            </Button>
            <Button variant="destructive" onClick={handleCancelPerformance} disabled={cancelLoading}>
              {cancelLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              공연 취소
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 공연 일정 취소 확인 다이얼로그 */}
      <Dialog open={cancelScheduleDialogOpen} onOpenChange={setCancelScheduleDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>공연 일정 취소</DialogTitle>
            <DialogDescription>
              정말로 이 공연 일정을 취소하시겠습니까? 이 작업은 되돌릴 수 없으며, 해당 일정의 모든 예매가 취소됩니다.
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <Alert variant="destructive">
              <AlertTriangle className="h-4 w-4" />
              <AlertTitle>주의</AlertTitle>
              <AlertDescription>
                공연 일정을 취소하면 해당 일정의 모든 예매가 자동으로 취소되고 환불 처리됩니다. 이 작업은 되돌릴 수
                없습니다.
              </AlertDescription>
            </Alert>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelScheduleDialogOpen(false)}>
              취소
            </Button>
            <Button variant="destructive" onClick={handleCancelSchedule} disabled={cancelLoading}>
              {cancelLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              일정 취소
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 공연 일정 추가 다이얼로그 */}
      <Dialog open={addScheduleDialogOpen} onOpenChange={setAddScheduleDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>공연 일정 추가</DialogTitle>
            <DialogDescription>새로운 공연 일정을 추가합니다. 시작 시간과 종료 시간을 입력하세요.</DialogDescription>
          </DialogHeader>
          <ScheduleForm onSubmit={handleAddSchedule} />
        </DialogContent>
      </Dialog>
    </div>
  )
}
