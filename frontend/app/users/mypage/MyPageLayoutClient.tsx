"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import Link from "next/link"
import { User, Ticket, Heart, UserCog, Loader2 } from "lucide-react"

import { cn } from "@/lib/utils"
import { getUserProfile } from "@/lib/api"
import { Button } from "@/components/ui/button"

interface NavItem {
  title: string
  href: string
  icon: React.ElementType
}

const navItems: NavItem[] = [
  {
    title: "내 정보",
    href: "/users/mypage",
    icon: User,
  },
  {
    title: "예매 내역",
    href: "/users/mypage/reservations",
    icon: Ticket,
  },
  {
    title: "찜한 공연",
    href: "/users/mypage/bookmarks",
    icon: Heart,
  },
  {
    title: "공연 관리자 신청",
    href: "/users/mypage/register",
    icon: UserCog,
  },
]

export function MyPageLayoutClient({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await getUserProfile()
        setIsAuthenticated(true)
      } catch (error) {
        console.error("인증 확인 중 오류:", error)
        router.push("/login")
      } finally {
        setIsLoading(false)
      }
    }

    checkAuth()
  }, [router])

  if (isLoading) {
    return (
      <div className="flex h-[calc(100vh-200px)] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return null // 인증되지 않은 경우 로그인 페이지로 리디렉션됨
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">마이페이지</h1>

      <div className="flex flex-col md:flex-row gap-8">
        {/* 사이드바 */}
        <aside className="w-full md:w-64 shrink-0">
          <nav className="space-y-2">
            {navItems.map((item) => (
              <Link key={item.href} href={item.href} passHref>
                <Button
                  variant="ghost"
                  className={cn(
                    "w-full justify-start gap-2 text-left",
                    pathname === item.href && "bg-muted font-medium",
                  )}
                >
                  <item.icon className="h-5 w-5" />
                  {item.title}
                </Button>
              </Link>
            ))}
          </nav>
        </aside>

        {/* 메인 콘텐츠 */}
        <main className="flex-1 bg-card rounded-lg p-6 border shadow-sm">{children}</main>
      </div>
    </div>
  )
}
