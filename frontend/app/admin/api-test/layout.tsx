import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
  title: "API 테스트 | 티켓-4-U 어드민",
  description: "API 엔드포인트를 테스트하고 응답을 확인할 수 있는 페이지입니다.",
}

interface ApiTestLayoutProps {
  children: React.ReactNode
}

export default function ApiTestLayout({ children }: ApiTestLayoutProps) {
  return <>{children}</>
}
