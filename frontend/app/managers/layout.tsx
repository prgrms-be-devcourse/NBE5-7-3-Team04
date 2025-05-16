import type React from "react"
import type { Metadata } from "next"
import { ManagerLayoutClient } from "./ManagerLayoutClient"

export const metadata: Metadata = {
  title: "공연 관리자 | 티켓-4-U",
  description: "티켓-4-U 공연 관리자 페이지",
}

interface ManagerLayoutProps {
  children: React.ReactNode
}

export default function ManagerLayout({ children }: ManagerLayoutProps) {
  return <ManagerLayoutClient>{children}</ManagerLayoutClient>
}
