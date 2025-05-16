"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { PerformanceCard } from "@/components/performance-card"
import { searchPerformances, getPerformances } from "@/lib/api"
import { Loader2, Search } from "lucide-react"

export default function PerformancesPage() {
  const searchParams = useSearchParams()
  const initialSearchQuery = searchParams.get("search") || ""

  const [searchQuery, setSearchQuery] = useState(initialSearchQuery)
  const [category, setCategory] = useState("all")
  const [performances, setPerformances] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)

  // 검색 파라미터가 있으면 검색 실행
  useEffect(() => {
    if (initialSearchQuery) {
      handleSearch()
    } else {
      fetchPerformances()
    }
  }, [initialSearchQuery])

  const fetchPerformances = async () => {
    try {
      setLoading(true)
      setError(null)

      const data = await getPerformances(page)

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
      if (category !== "all") params.category = category

      const data = await searchPerformances(params)

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

  // 카테고리 변경 시 자동 검색
  useEffect(() => {
    if (category !== "all") {
      handleSearch()
    }
  }, [category])

  // 공연 데이터 포맷팅
  const formatPerformances = (data: any[]) => {
    return data.map((item) => ({
      id: item.id,
      title: item.title,
      date: new Date(item.startDate).toLocaleDateString(),
      time: "상세 페이지에서 확인",
      location: item.venue,
      price: item.price,
      image: item.fileUrl || "/placeholder.svg?height=300&width=400",
      category:
        item.category === "SINGING"
          ? "콘서트"
          : item.category === "DANCING"
            ? "무용"
            : item.category === "OPERA"
              ? "오페라"
              : "공연",
      status: "예매가능" as const,
    }))
  }

  return (
    <div className="container py-8 px-4 md:px-6">
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">공연 목록</h1>
          <p className="text-muted-foreground">다양한 공연을 검색하고 예매하세요.</p>
        </div>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle>검색</CardTitle>
            <CardDescription>공연명, 장소, 카테고리로 검색할 수 있습니다.</CardDescription>
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
              <Select value={category} onValueChange={setCategory}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="카테고리" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">모든 카테고리</SelectItem>
                  <SelectItem value="SINGING">콘서트</SelectItem>
                  <SelectItem value="DANCING">무용</SelectItem>
                  <SelectItem value="OPERA">오페라</SelectItem>
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
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
              {formatPerformances(performances).map((performance) => (
                <PerformanceCard key={performance.id} performance={performance} />
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
