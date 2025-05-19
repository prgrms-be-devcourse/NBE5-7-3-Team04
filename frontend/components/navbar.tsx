"use client"

import type React from "react"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { Menu, User, LogIn, SearchIcon } from "lucide-react"
import { useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { cn } from "@/lib/utils"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import Image from "next/image"
import { Input } from "@/components/ui/input"
import { useAuth, logout } from "@/src/auth/user"

export function Navbar() {
  const [searchQuery, setSearchQuery] = useState("")
  const pathname = usePathname()
  const router = useRouter()
  const { isAuthenticated, userRole, user } = useAuth()

  // 검색 기능
  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      try {
        // 검색 결과 페이지로 이동
        router.push(`/performances?search=${encodeURIComponent(searchQuery)}`)
      } catch (error) {
        console.error("검색 오류:", error)
      }
    }
  }

  // 로그아웃 처리
  const handleLogout = () => {
    logout()
    router.push("/")
  }

  // 어드민 페이지에서는 네비게이션 바를 표시하지 않음
  if (pathname.startsWith("/admin")) {
    return null
  }

  const isManager = userRole === "MANAGER"
  const isAdmin = userRole === "ADMIN"

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/80 backdrop-blur-xl">
      <div className="container flex h-16 items-center justify-between px-4 x-8 md:px-6">
        <div className="flex items-center gap-2">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" size="icon" className="md:hidden">
                <Menu className="h-5 w-5" />
                <span className="sr-only">메뉴 열기</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-[240px] sm:w-[240px]">
              <nav className="grid gap-6 text-lg font-medium">
                <Link href="/" className="flex items-center gap-2 text-lg font-semibold">
                  <div className="flex items-center">
                    <Image src="/logo-icon.png" alt="TICKET4U" width={32} height={32} />
                    <Image src="/logo-text.png" alt="TICKET4U" width={120} height={24} className="ml-1" />
                  </div>
                </Link>
                <Link
                  href="/"
                  className={cn(
                    "hover:text-foreground/80",
                    pathname === "/" ? "text-foreground" : "text-foreground/60",
                  )}
                >
                  홈
                </Link>
                <Link
                  href="/performances"
                  className={cn(
                    "hover:text-foreground/80",
                    pathname.startsWith("/performances") ? "text-foreground" : "text-foreground/60",
                  )}
                >
                  공연 목록
                </Link>
                {isAuthenticated && (
                  <Link
                    href="/users/mypage/reservations"
                    className={cn(
                      "hover:text-foreground/80",
                      pathname.startsWith("/users/mypage") ? "text-foreground" : "text-foreground/60",
                    )}
                  >
                    마이페이지
                  </Link>
                )}
                {isManager && (
                  <Link
                    href="/managers"
                    className={cn(
                      "hover:text-foreground/80",
                      pathname.startsWith("/managers") ? "text-foreground" : "text-foreground/60",
                    )}
                  >
                    공연관리자
                  </Link>
                )}
              </nav>
            </SheetContent>
          </Sheet>
          <Link href="/" className="flex items-center gap-2">
            <div className="flex items-center">
              <Image src="/logo-icon.png" alt="TICKET4U" width={32} height={32} />
              <Image src="/logo-text.png" alt="TICKET4U" width={120} height={24} className="ml-1" />
            </div>
          </Link>
          <nav className="hidden md:flex items-center gap-6 text-sm ml-8">
            <Link
              href="/"
              className={cn(
                "transition-colors hover:text-foreground/80",
                pathname === "/" ? "text-foreground" : "text-foreground/60",
              )}
            >
              홈
            </Link>
            <Link
              href="/performances"
              className={cn(
                "transition-colors hover:text-foreground/80",
                pathname.startsWith("/performances") ? "text-foreground" : "text-foreground/60",
              )}
            >
              공연 목록
            </Link>
            {isManager && (
              <Link
                href="/managers"
                className={cn(
                  "transition-colors hover:text-foreground/80",
                  pathname.startsWith("/managers") ? "text-foreground" : "text-foreground/60",
                )}
              >
                공연관리자
              </Link>
            )}
          </nav>
        </div>
        <div className="flex items-center gap-4">
          <form onSubmit={handleSearch} className="relative hidden md:flex items-center">
            <Input
              type="search"
              placeholder="공연 검색..."
              className="w-[200px] lg:w-[300px] pl-8 rounded-full"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="absolute left-2.5 top-2.5 text-muted-foreground">
              <SearchIcon className="h-4 w-4" />
              <span className="sr-only">검색</span>
            </button>
          </form>

          {/* TODO: 테스트 완료 후 삭제 */}
          {/* 현재 로그인한 유저의 권한 표시 (임시) */}
          {userRole && (
            <span style={{ color: '#888', fontSize: 14 }}>(권한: {userRole})</span>
          )}
          {/* 매니저로 승인 임시 버튼 (USER일 때만 노출) */}
          {userRole === 'USER' && (
            <Button size="sm" variant="outline" onClick={async () => {
              try {
                await import("@/src/api/api-admin").then(mod => mod.approveMe());
                window.location.reload();
              } catch (e: any) {
                alert("승인 실패: " + (e?.message || e));
              }
            }} style={{ marginLeft: 8 }}>
              매니저로 승인
            </Button>
          )}
          {/* TODO: 테스트 완료 후 삭제 */}

          
          {isAuthenticated ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={user?.profileImage || "/default-avatar.png"} alt={user?.name || "사용자"} />
                    <AvatarFallback>
                      {user?.name?.[0]?.toUpperCase() || <User className="h-4 w-4" />}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel>{user?.name || "내 계정"}</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link href="/users/mypage/reservations">예매 내역</Link>
                </DropdownMenuItem>
                <DropdownMenuItem asChild>
                  <Link href="/users/mypage/bookmarks">찜한 공연</Link>
                </DropdownMenuItem>
                {!isManager && !isAdmin && (
                  <DropdownMenuItem asChild>
                    <Link href="/users/mypage/register">공연 관리자 신청</Link>
                  </DropdownMenuItem>
                )}
                {isManager && (
                  <DropdownMenuItem asChild>
                    <Link href="/managers">공연 관리</Link>
                  </DropdownMenuItem>
                )}
                {isAdmin && (
                  <DropdownMenuItem asChild>
                    <Link href="/admin">관리자 페이지</Link>
                  </DropdownMenuItem>
                )}
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>
                  로그아웃
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button asChild variant="ghost" size="sm" className="gap-2">
              <Link href="/login">
                <LogIn className="h-4 w-4" />
                로그인
              </Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  )
}
