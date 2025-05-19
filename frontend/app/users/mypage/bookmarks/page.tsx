"use client"

import { useEffect, useState } from "react"
import { getMyBookmarks } from "@/src/api/bookmark"
import type { BookmarkedPerformance } from "@/src/types/bookmark"
import { PerformanceCard } from "@/components/performance-card"
import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useRouter } from "next/navigation"
import { Badge } from "@/components/ui/badge"
import { MapPin, Calendar } from "lucide-react"

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

const getStatusBadge = (category: string) => {
  switch (category) {
    case "SINGING":
      return (
        <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">콘서트</Badge>
      );
    case "DANCING":
      return (
        <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-200">무용</Badge>
      );
    case "OPERA":
      return (
        <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200">오페라</Badge>
      );
    default:
      return null;
  }
};

const formatDate = (date: string) => {
  const formattedDate = new Date(date).toLocaleDateString();
  return formattedDate.split(", ").join(" ");
};

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
    <div className="min-h-screen bg-gradient-to-b from-purple-50/50 to-white">
      <div className="container py-12 px-4 md:px-6 flex-grow">
        <h1 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent mb-8 text-center">
          내가 찜한 공연
        </h1>

        {bookmarks.length === 0 ? (
          <div className="flex justify-center items-center min-h-[300px]">
            <Card className="w-full max-w-lg mx-auto">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <p className="text-muted-foreground mb-4">아직 찜한 공연이 없습니다.</p>
                <Button onClick={() => router.push('/performances')}>공연 둘러보기</Button>
              </CardContent>
            </Card>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {bookmarks.map((performance) => (
              <Card
                key={performance.id}
                className="group cursor-pointer hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border-none bg-white/80 backdrop-blur-sm"
                onClick={() => router.push(`/performances/${performance.id}`)}
              >
                <div className="aspect-[3/4] relative overflow-hidden">
                  <img
                    src={performance.fileUrl || "/placeholder.svg?height=300&width=400"}
                    alt={performance.title}
                    className="object-cover w-full h-full transition-transform duration-500 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  <div className="absolute top-4 left-4">
                    {getStatusBadge(performance.category)}
                  </div>
                  <div className="absolute bottom-0 left-0 right-0 p-4 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <h3 className="text-xl font-bold mb-2 line-clamp-2">{performance.title}</h3>
                    <div className="space-y-2 text-sm text-muted-foreground">
                      <div className="flex items-center">
                        <MapPin className="mr-1 h-3.5 w-3.5" />
                        <span className="text-white">{performance.venue}</span>
                      </div>
                      <div className="flex items-center">
                        <Calendar className="mr-1 h-3.5 w-3.5" />
                        <span className="text-white">
                          {formatDate(performance.startDate)} ~ {formatDate(performance.endDate)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <CardContent className="p-4">
                  <div className="space-y-2">
                    <h3 className="font-semibold line-clamp-1">{performance.title}</h3>
                    <div className="flex flex-col gap-1 text-sm">
                      <span>{performance.venue}</span>
                      <span className="text-muted-foreground">
                        {formatDate(performance.startDate)} ~ {formatDate(performance.endDate)}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
