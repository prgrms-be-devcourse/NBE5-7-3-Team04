"use client"

import { useEffect, useState } from "react"
import type React from "react"
import { useRouter, usePathname } from "next/navigation"
import { useAdminAuth } from "@/lib/admin-auth"
import { SidebarProvider } from "@/components/ui/sidebar"
import { AdminSidebar } from "@/components/admin-sidebar"

interface AdminLayoutClientProps {
  children: React.ReactNode
}

export default function AdminLayoutClient({ children }: AdminLayoutClientProps) {
  const { requireAdminAuth } = useAdminAuth()
  const router = useRouter()
  const pathname = usePathname()
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkAuth = async () => {
      const isAuth = await requireAdminAuth()
      setIsLoading(false)
    }
    checkAuth()
  }, [requireAdminAuth])

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
      </div>
    )
  }

  // 로그인 페이지면 레이아웃 없이 렌더링
  if (pathname === "/admin/login") {
    return <>{children}</>
  }

  // 관리자 레이아웃으로 렌더링
  return (
    <SidebarProvider>
      <div className="flex h-screen">
        <AdminSidebar />
        <main className="flex-1 overflow-y-auto p-8">
          {children}
        </main>
      </div>
    </SidebarProvider>
  )
}