"use client"

import { useEffect, useState } from "react"
import { getMyBookmarks } from "@/src/api/bookmark"
import type { BookmarkedPerformance } from "@/src/types/bookmark"
import { PerformanceCard } from "@/components/performance-card"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"

const mapStatus = (status: string) => {
  switch (status) {
    case "AVAILABLE":
      return "예매가능"
    case "ALMOST_SOLD_OUT":
      return "매진임박"
    case "SOLD_OUT":
      return "매진"
    case "ENDED":
      return "종료"
    default:
      return "예매가능"
  }
}

export default function BookmarksPage() {
  const [bookmarks, setBookmarks] = useState<BookmarkedPerformance[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    const fetchBookmarks = async () => {
      try {
        setLoading(true)
        const data = await getMyBookmarks()
        setBookmarks(data)
      } catch (err) {
        setError("찜한 공연을 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }
    fetchBookmarks()
  }, [])

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">찜한 공연을 불러오는 중...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center text-red-500">{error}</div>
      </div>
    )
  }

  return (
    <div className="flex flex-col min-h-screen">
      <div className="container py-8 flex-grow">
        <h1 className="text-3xl font-bold mb-8">내가 찜한 공연</h1>

        {bookmarks.length === 0 ? (
          <div>
            <Card className="w-full">
                <CardContent className="flex flex-col items-center justify-center py-12">
                <p className="text-muted-foreground mb-4">아직 찜한 공연이 없습니다.</p>
                <Button onClick={() => router.push('/performances')}>공연 둘러보기</Button>
              </CardContent>
            </Card>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {bookmarks.map((performance) => (
              <PerformanceCard
                key={performance.id}
                performance={{
                  id: String(performance.id),
                  title: performance.title,
                  date: performance.startDate.split('T')[0],
                  time: performance.startDate.split('T')[1]?.slice(0, 5) || '',
                  location: performance.venue,
                  price: performance.price,
                  image: performance.fileUrl,
                  category: performance.category,
                  status: mapStatus(performance.status),
                }}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
