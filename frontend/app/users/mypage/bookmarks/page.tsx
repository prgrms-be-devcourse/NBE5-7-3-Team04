import { Suspense } from "react"
import Link from "next/link"
import { Loader2 } from "lucide-react"
import { getUserBookmarks } from "@/lib/api"
import { PerformanceCard } from "@/components/performance-card"
import { Button } from "@/components/ui/button"

async function BookmarksContent() {
  const bookmarks = await getUserBookmarks()

  if (bookmarks.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <h2 className="text-2xl font-bold mb-2">찜한 공연이 없습니다</h2>
        <p className="text-muted-foreground mb-6">관심 있는 공연을 찜하여 쉽게 찾아보세요.</p>
        <Link href="/performances" passHref>
          <Button>공연 둘러보기</Button>
        </Link>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">찜한 공연</h2>
        <p className="text-muted-foreground">총 {bookmarks.length}개의 공연</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {bookmarks.map((bookmark) => (
          <PerformanceCard key={bookmark.performance.id} performance={bookmark.performance} isBookmarked={true} />
        ))}
      </div>
    </div>
  )
}

export default function BookmarksPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-40 items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      }
    >
      <BookmarksContent />
    </Suspense>
  )
}
