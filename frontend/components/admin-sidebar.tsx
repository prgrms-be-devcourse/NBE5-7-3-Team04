"use client"

import { usePathname } from "next/navigation"
import Link from "next/link"
import Image from "next/image"
import { BarChart3, CheckSquare, CreditCard, FileText, Home, LogOut, Settings, Users } from "lucide-react"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
} from "@/components/ui/sidebar"
import { useAdminAuth } from "@/src/auth/admin"
import { useRouter } from "next/navigation"

export function AdminSidebar() {
  const pathname = usePathname()
  const router = useRouter()

  const handleLogout = () => {
    adminLogout()
    router.push("/admin/login")
  }

  return (
    <Sidebar>
      <SidebarHeader className="flex h-14 items-center px-4">
        <Link href="/admin" className="flex items-center gap-2">
          <Image src="/logo-icon.png" alt="TICKET4U" width={28} height={28} />
          <span className="font-semibold">TICKET4U 관리자</span>
        </Link>
      </SidebarHeader>
      <SidebarSeparator />
      <SidebarContent>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname === "/admin"}>
              <Link href="/admin">
                <Home className="h-4 w-4" />
                <span>대시보드</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/approve")}>
              <Link href="/admin/approve/roles">
                <CheckSquare className="h-4 w-4" />
                <span>승인 관리</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/reservations")}>
              <Link href="/admin/reservations">
                <FileText className="h-4 w-4" />
                <span>예매 관리</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/refunds")}>
              <Link href="/admin/refunds">
                <CreditCard className="h-4 w-4" />
                <span>환불 관리</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/settlements")}>
              <Link href="/admin/settlements">
                <BarChart3 className="h-4 w-4" />
                <span>정산 관리</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/users")}>
              <Link href="/admin/users">
                <Users className="h-4 w-4" />
                <span>회원 관리</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <SidebarMenuItem>
            <SidebarMenuButton asChild isActive={pathname?.startsWith("/admin/settings")}>
              <Link href="/admin/settings">
                <Settings className="h-4 w-4" />
                <span>시스템 설정</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarContent>

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton onClick={handleLogout}>
              <LogOut className="h-4 w-4" />
              <span>로그아웃</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  )
}
