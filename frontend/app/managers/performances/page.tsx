"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getManagerPerformances, searchManagerPerformances } from "@/src/api/api"
import { Loader2, Search, Plus, Calendar, MapPin } from "lucide-react"
import Link from "next/link"
import { format, parseISO } from "date-fns"
import { useAuth } from "@/src/auth/user"

export default function ManagerPerformancesPage() {
  const searchParams = useSearchParams()
  const initialSearchQuery = searchParams.get("search") || ""

  const [searchQuery, setSearchQuery] = useState(initialSearchQuery)
  const [status, setStatus] = useState("all")
  const [performances, setPerformances] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const { requireRole } = useAuth()

  useEffect(() => {
    requireRole("MANAGER")

    if (initialSearchQuery) {
      handleSearch()
    } else {
      fetchPerformances()
    }
  }, [initialSearchQuery, requireRole])

  const fetchPerformances = async () => {
    try {
      setLoading(true)
      setError(null)

      const data = await getManagerPerformances(page)

      setPerformances(data.content || [])
      setTotalPages(data.totalPages || 1)
    } catch (err) {
      console.error("공연 목록 가져오기 오류:", err)
      setError("공연 목록을 불러오는 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault()

    try {
      setLoading(true)
      setError(null)

      const params: any = {
        page: 0,
        size: 12,
      }

      if (searchQuery) params.title = searchQuery
      if (status !== "all") params.status = status

      const data = await searchManagerPerformances(params)

      setPerformances(data.content || [])
      setTotalPages(data.totalPages || 1)
      setPage(0)
    } catch (err) {
      console.error("검색 오류:", err)
      setError("검색 중 오류가 발생했습니다.")
    } finally {
      setLoading(false)
    }
  }

  const handlePageChange = (newPage: number) => {
    setPage(newPage)
    window.scrollTo(0, 0)
  }

  // 상태 변경 시 자동 검색
  useEffect(() => {
    if (status !== "all") {
      handleSearch()
    }
  }, [status])

  // 날짜 포맷팅 함수
  const formatDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), "yyyy.MM.dd")
    } catch (error) {
      return "날짜 정보 없음"
    }
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">공연 목록</h1>
            <p className="text-muted-foreground mt-1">등록한 공연 목록을 관리합니다.</p>
          </div>
          <Button asChild>
            <Link href="/managers/register">
              <Plus className="mr-2 h-4 w-4" />
              공연 등록
            </Link>
          </Button>
        </div>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle>검색</CardTitle>
            <CardDescription>공연명, 상태로 검색할 수 있습니다.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="공연명 검색..."
                  className="pl-8"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <Select value={status} onValueChange={setStatus}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="상태" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">모든 상태</SelectItem>
                  <SelectItem value="PENDING">대기중</SelectItem>
                  <SelectItem value="CONFIRMED">승인됨</SelectItem>
                  <SelectItem value="REJECTED">거절됨</SelectItem>
                  <SelectItem value="CANCELLED">취소됨</SelectItem>
                  <SelectItem value="COMPLETED">완료됨</SelectItem>
                </SelectContent>
              </Select>
              <Button type="submit">검색</Button>
            </form>
          </CardContent>
        </Card>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <span className="ml-2">공연 정보를 불러오는 중...</span>
          </div>
        ) : error ? (
          <div className="text-center py-12 text-destructive">{error}</div>
        ) : performances.length === 0 ? (
          <div className="text-center py-12 text-muted-foreground">
            검색 결과가 없습니다. 다른 검색어로 시도해보세요.
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {performances.map((performance) => (
                <Card key={performance.id} className="overflow-hidden">
                  <div className="aspect-video w-full overflow-hidden bg-muted">
                    <img
                      src={performance.fileUrl || "/placeholder.svg?height=300&width=400"}
                      alt={performance.title}
                      className="h-full w-full object-cover"
                      onError={(e) => {
                        e.currentTarget.src = "/placeholder.svg?height=300&width=400"
                      }}
                    />
                  </div>
                  <CardContent className="p-6">
                    <div className="space-y-4">
                      <div>
                        <div className="flex items-center justify-between">
                          <span
                            className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
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
                          <span className="text-xs text-muted-foreground">
                            {performance.category === "OPERA"
                              ? "오페라"
                              : performance.category === "DANCING"
                                ? "무용"
                                : "콘서트"}
                          </span>
                        </div>
                        <h3 className="mt-2 text-lg font-semibold">{performance.title}</h3>
                      </div>
                      <div className="space-y-2 text-sm text-muted-foreground">
                        <div className="flex items-center">
                          <MapPin className="mr-1 h-3.5 w-3.5" />
                          <span>{performance.venue}</span>
                        </div>
                        <div className="flex items-center">
                          <Calendar className="mr-1 h-3.5 w-3.5" />
                          <span>
                            {formatDate(performance.startDate)} - {formatDate(performance.endDate)}
                          </span>
                        </div>
                      </div>
                      <div className="flex justify-end">
                        <Button variant="outline" size="sm" asChild>
                          <Link href={`/managers/performances/${performance.id}`}>상세 보기</Link>
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center mt-8 gap-2">
                <Button variant="outline" onClick={() => handlePageChange(page - 1)} disabled={page === 0}>
                  이전
                </Button>
                {Array.from({ length: totalPages }, (_, i) => (
                  <Button key={i} variant={i === page ? "default" : "outline"} onClick={() => handlePageChange(i)}>
                    {i + 1}
                  </Button>
                ))}
                <Button variant="outline" onClick={() => handlePageChange(page + 1)} disabled={page === totalPages - 1}>
                  다음
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
