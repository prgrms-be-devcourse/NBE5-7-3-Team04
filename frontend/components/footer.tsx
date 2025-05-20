"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import Image from "next/image"
import { Button } from '@/components/ui/button'

export function Footer() {
  const pathname = usePathname()

  // 어드민 페이지에서는 푸터를 표시하지 않음
  if (pathname.startsWith("/admin") || pathname.startsWith("/managers")) {
    return null
  }

  return (
    <footer className="border-t">
      <div className="container px-4 md:px-6">
        <div className="flex flex-col gap-4 py-10 md:py-16">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="flex flex-col gap-2">
              <h3 className="text-lg font-semibold">TICKET4U</h3>
              <p className="text-sm text-muted-foreground">
                당신의 특별한 순간을 위한 티켓 예매 서비스
              </p>
            </div>
            <div className="flex flex-col gap-2 md:items-end">
              <div className="flex gap-4">
                <Button variant="link" className="px-0" asChild>
                  <Link href="/about">회사 소개</Link>
                </Button>
                <Button variant="link" className="px-0" asChild>
                  <Link href="/terms">이용약관</Link>
                </Button>
                <Button variant="link" className="px-0" asChild>
                  <Link href="/privacy">개인정보처리방침</Link>
                </Button>
              </div>
            <p className="text-sm text-muted-foreground">
                © 2024 TICKET4U. All rights reserved.
            </p>
          </div>
          </div>
        </div>
      </div>
    </footer>
  )
}
