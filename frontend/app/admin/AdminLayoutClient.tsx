"use client"

import { useEffect } from "react"
import type React from "react"
import { useRouter, usePathname } from "next/navigation"
import { useAdminAuth } from "@/src/auth/admin"
import { SidebarProvider } from "@/components/ui/sidebar"
import { AdminSidebar } from "@/components/admin-sidebar"

interface AdminLayoutClientProps {
  children: React.ReactNode
}

export default function AdminLayoutClient({ children }: AdminLayoutClientProps) {
  const { isAuthenticated, isLoading } = useAdminAuth()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    // Skip redirect for login page
    if (pathname === "/admin/login") return

    // Redirect to login if not authenticated
    if (!isLoading && !isAuthenticated) {
      router.push("/admin/login")
    }
  }, [isAuthenticated, isLoading, router, pathname])

  // Show nothing while checking authentication
  if (isLoading) {
    return <div className="flex h-screen items-center justify-center">로딩 중...</div>
  }

  // For login page, just render the children without the admin layout
  if (pathname === "/admin/login") {
    return <>{children}</>
  }

  // If not authenticated and not on login page, don't render anything
  // (useEffect will handle the redirect)
  if (!isAuthenticated && pathname !== "/admin/login") {
    return null
  }

  return (
    <SidebarProvider>
      <div className="flex min-h-screen">
        <AdminSidebar />
        <div className="flex-1">
          <main className="container mx-auto p-4 md:p-6">{children}</main>
        </div>
      </div>
    </SidebarProvider>
  )
}
