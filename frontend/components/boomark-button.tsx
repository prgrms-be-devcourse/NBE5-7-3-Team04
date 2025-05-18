"use client"

import { useState } from "react"
import { Heart } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { addBookmark, removeBookmark } from "@/lib/api"
import { useToast } from "@/components/ui/use-toast"

interface BookmarkButtonProps {
  performanceId: number
  isBookmarked: boolean
  className?: string
  variant?: "default" | "outline" | "ghost"
  size?: "default" | "sm" | "lg" | "icon"
}

export function BookmarkButton({
  performanceId,
  isBookmarked: initialIsBookmarked,
  className,
  variant = "ghost",
  size = "icon",
}: BookmarkButtonProps) {
  const [isBookmarked, setIsBookmarked] = useState(initialIsBookmarked)
  const [isLoading, setIsLoading] = useState(false)
  const { toast } = useToast()

  const handleToggleBookmark = async () => {
    try {
      setIsLoading(true)

      if (isBookmarked) {
        await removeBookmark(performanceId)
        toast({
          title: "북마크가 삭제되었습니다.",
          variant: "default",
        })
      } else {
        await addBookmark(performanceId)
        toast({
          title: "북마크에 추가되었습니다.",
          variant: "default",
        })
      }

      setIsBookmarked(!isBookmarked)
    } catch (error) {
      console.error("북마크 처리 중 오류가 발생했습니다:", error)
      toast({
        title: "오류가 발생했습니다.",
        description: "북마크 처리 중 문제가 발생했습니다. 다시 시도해주세요.",
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
      className={cn(
        "group relative",
        isBookmarked ? "text-red-500 hover:text-red-600" : "text-muted-foreground hover:text-red-500",
        className,
      )}
      onClick={handleToggleBookmark}
      disabled={isLoading}
      aria-label={isBookmarked ? "북마크 삭제" : "북마크 추가"}
    >
      <Heart
        className={cn(
          "h-5 w-5 transition-all",
          isBookmarked ? "fill-current" : "fill-none group-hover:fill-current group-hover:scale-110",
          isLoading && "animate-pulse",
        )}
      />
    </Button>
  )
}
