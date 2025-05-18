"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { getManagerPerformancesV1, createSettlement, getManagerPerformanceDetailV1 } from "@/src/api/api-manager"
import { Loader2 } from "lucide-react"
import { useAuth } from "@/src/auth/user"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import dayjs from "dayjs"

export default function SettlementRequestPage() {
  const searchParams = useSearchParams()
  const initialPerformanceId = searchParams.get("performanceId")
  const router = useRouter()
  const { isLoading: authLoading, userRole } = useAuth()

  const [performances, setPerformances] = useState<any[]>([])
  const [selectedPerformanceId, setSelectedPerformanceId] = useState<string>(initialPerformanceId || "")
  const [account, setAccount] = useState("")
  const [bank, setBank] = useState("")
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

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
        const data = await getManagerPerformancesV1()
        // 7일 전 날짜 계산
        const sevenDaysAgo = dayjs().subtract(7, "day").endOf("day")
        // status가 COMPLETED이고 endDate가 7일 이전인 공연만 필터링
        const filteredPerformances = data.content.filter((p: any) =>
          p.status === "COMPLETED" &&
          dayjs(p.endDate).isBefore(sevenDaysAgo)
        )
        setPerformances(filteredPerformances)
      } catch (err) {
        console.error("공연 목록 가져오기 오류:", err)
        setError("공연 목록을 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }
    fetchPerformances()
  }, [authLoading, userRole])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      // 필수 필드 검증
      if (!selectedPerformanceId || !account || !bank) {
        setError("모든 필드를 입력해주세요.")
        setSubmitting(false)
        return
      }

      // 공연 디테일 정보 조회 및 로그 출력
      try {
        const detail = await getManagerPerformanceDetailV1(Number(selectedPerformanceId));
        console.log('선택한 공연 디테일:', detail);
      } catch (detailErr) {
        console.error('공연 디테일 조회 오류:', detailErr);
      }

      // 정산 신청
      const data = {
        performanceId: Number(selectedPerformanceId),
        account,
        bank,
      }

      console.log('정산 신청 데이터:', data);
      const settlementId = await createSettlement(data);
      console.log('정산 신청 성공 - ID:', settlementId);
      
      setSuccess(true)

      // 성공 후 3초 후에 정산 내역 페이지로 이동
      setTimeout(() => {
        router.push("/managers/settlements/history")
      }, 3000)
    } catch (err) {
      console.error("정산 신청 오류:", err)
      setError("정산 신청 중 오류가 발생했습니다.")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6 max-w-4xl mx-auto">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">정산 신청</h1>
          <p className="text-muted-foreground" style={{marginTop: '10px'}}>완료된 공연에 대한 정산을 신청합니다.</p>
          <p className="text-sm text-primary mt-2">공연의 마지막 날짜로부터 7일 이상이 지난 후부터 정산을 신청할 수 있습니다.</p>
        </div>

        {success ? (
          <Alert className="bg-green-50 text-green-800 border-green-200">
            <AlertTitle>정산 신청 성공</AlertTitle>
            <AlertDescription>
              정산이 성공적으로 신청되었습니다. 잠시 후 정산 내역 페이지로 이동합니다.
            </AlertDescription>
          </Alert>
        ) : (
          <form onSubmit={handleSubmit}>
            <Card>
              <CardHeader>
                <CardTitle>정산 정보</CardTitle>
                <CardDescription>정산 받을 공연과 계좌 정보를 입력하세요.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="performance">공연 선택</Label>
                  {loading ? (
                    <div className="flex items-center gap-2">
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span className="text-sm text-muted-foreground">공연 목록을 불러오는 중...</span>
                    </div>
                  ) : performances.length === 0 ? (
                    <div className="text-sm text-muted-foreground">정산 가능한 완료된 공연이 없습니다.</div>
                  ) : (
                    <Select value={selectedPerformanceId} onValueChange={setSelectedPerformanceId} required>
                      <SelectTrigger id="performance">
                        <SelectValue placeholder="공연 선택" />
                      </SelectTrigger>
                      <SelectContent>
                        {performances.map((performance) => (
                          <SelectItem key={performance.id} value={performance.id.toString()}>
                            <div className="flex flex-row items-center justify-between w-full">
                              <span>{performance.title}</span>
                              <span className="text-xs text-muted-foreground ml-4 whitespace-nowrap">
                                {dayjs(performance.startDate).format('YYYY.MM.DD')} ~ {dayjs(performance.endDate).format('YYYY.MM.DD')}
                              </span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="bank">은행명</Label>
                  <Input
                    id="bank"
                    placeholder="은행명을 입력하세요"
                    value={bank}
                    onChange={(e) => setBank(e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="account">계좌번호</Label>
                  <Input
                    id="account"
                    placeholder="계좌번호를 입력하세요"
                    value={account}
                    onChange={(e) => setAccount(e.target.value)}
                    required
                  />
                </div>
              </CardContent>
              <CardFooter className="flex justify-between">
                <Button variant="outline" type="button" onClick={() => router.back()}>
                  취소
                </Button>
                <Button type="submit" disabled={submitting || loading || performances.length === 0}>
                  {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  정산 신청
                </Button>
              </CardFooter>
            </Card>

            {error && <div className="mt-4 p-4 bg-red-50 text-red-800 rounded-lg">{error}</div>}
          </form>
        )}
      </div>
    </div>
  )
}
