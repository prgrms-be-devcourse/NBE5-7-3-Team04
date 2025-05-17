"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Bookmark, BookmarkCheck } from "lucide-react"
import { addBookmark, removeBookmark } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"
import { useAuth } from "@/lib/auth"
import { useRouter } from "next/navigation"

interface BookmarkButtonProps {
  performanceId: number
  initialBookmarked?: boolean
  variant?: "default" | "outline" | "ghost"
  size?: "default" | "sm" | "lg" | "icon"
  className?: string
  onRemoved?: () => void
}

export function BookmarkButton({
  performanceId,
  initialBookmarked = false,
  variant = "outline",
  size = "icon",
  className,
  onRemoved,
}: BookmarkButtonProps) {
  const [isBookmarked, setIsBookmarked] = useState(initialBookmarked)
  const [isLoading, setIsLoading] = useState(false)
  const { toast } = useToast()
  const { isAuthenticated } = useAuth()
  const router = useRouter()

  const handleToggleBookmark = async () => {
    if (!isAuthenticated) {
      toast({
        title: "로그인이 필요합니다",
        description: "찜하기 기능은 로그인 후 이용 가능합니다.",
        variant: "destructive",
      })
      router.push("/login")
      return
    }

    setIsLoading(true)

    try {
      if (isBookmarked) {
        await removeBookmark(performanceId)
        toast({
          title: "찜 목록에서 삭제되었습니다",
          description: "공연이 찜 목록에서 삭제되었습니다.",
        })
        if (onRemoved) onRemoved();
      } else {
        await addBookmark(performanceId)
        toast({
          title: "찜 목록에 추가되었습니다",
          description: "공연이 찜 목록에 추가되었습니다.",
        })
      }
      setIsBookmarked(!isBookmarked)
    } catch (error) {
      console.error("북마크 처리 중 오류 발생:", error)
      toast({
        title: "오류가 발생했습니다",
        description: "잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Button
      variant={variant}
      size={size}
      className={className}
      onClick={handleToggleBookmark}
      disabled={isLoading}
      aria-label={isBookmarked ? "찜 목록에서 삭제" : "찜 목록에 추가"}
    >
      {isBookmarked ? <BookmarkCheck className="h-5 w-5 text-primary" /> : <Bookmark className="h-5 w-5" />}
    </Button>
  )
}
