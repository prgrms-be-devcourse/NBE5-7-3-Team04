"use client"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"

function MyPageTabs() {
  const pathname = usePathname()

  return (
    <div className="border-b">
      <div className="container flex h-16 items-center gap-4 md:gap-8">
        <Link
          href="/users/mypage/reservations"
          className={cn(
            "flex h-full items-center border-b-2 border-transparent px-2 text-sm font-medium transition-colors hover:border-foreground/50",
            pathname === "/users/mypage/reservations" && "border-foreground",
          )}
        >
          예매 내역
        </Link>
        <Link
          href="/users/mypage/bookmarks"
          className={cn(
            "flex h-full items-center border-b-2 border-transparent px-2 text-sm font-medium transition-colors hover:border-foreground/50",
            pathname === "/users/mypage/bookmarks" && "border-foreground",
          )}
        >
          찜한 공연
        </Link>
        <Link
          href="/users/mypage/register"
          className={cn(
            "flex h-full items-center border-b-2 border-transparent px-2 text-sm font-medium transition-colors hover:border-foreground/50",
            pathname === "/users/mypage/register" && "border-foreground",
          )}
        >
          공연 관리자 신청
        </Link>
      
      </div>
    </div>
  )
}

export default MyPageTabs
