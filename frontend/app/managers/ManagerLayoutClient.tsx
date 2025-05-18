"use client"

import type React from "react"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/src/auth/user"
import { SidebarProvider } from "@/components/ui/sidebar"
import { ManagerSidebar } from "@/components/manager-sidebar"
import { Loader2 } from "lucide-react"

interface ManagerLayoutClientProps {
  children: React.ReactNode
}

export function ManagerLayoutClient({ children }: ManagerLayoutClientProps) {
  const router = useRouter()
  const { isLoading, requireRole } = useAuth()

  // Check if user has MANAGER role
  useEffect(() => {
    requireRole("MANAGER")
  }, [requireRole])

  if (isLoading) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <div className="flex flex-col items-center gap-2">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">로딩 중...</p>
        </div>
      </div>
    )
  }

  return (
    <SidebarProvider>
      <div className="flex min-h-screen">
        <ManagerSidebar />
        <main className="flex-1 overflow-auto">{children}</main>
      </div>
    </SidebarProvider>
  )
}
