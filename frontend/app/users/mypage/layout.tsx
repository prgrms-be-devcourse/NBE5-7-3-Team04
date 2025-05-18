import type React from "react"
import type { Metadata } from "next"
import MyPageTabs from "./MyPageTabs"

export const metadata: Metadata = {
  title: "마이페이지 | 티켓-4-U",
  description: "티켓-4-U 마이페이지",
}

interface MyPageLayoutProps {
  children: React.ReactNode
}

export default function MyPageLayout({ children }: MyPageLayoutProps) {
  return (
    <div className="flex flex-col min-h-screen">
      <MyPageTabs />
      <div className="flex-1 py-8">{children}</div>
    </div>
  )
}
