"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import Image from "next/image"

export function Footer() {
  const pathname = usePathname()

  // 어드민 페이지에서는 푸터를 표시하지 않음
  if (pathname.startsWith("/admin") || pathname.startsWith("/managers")) {
    return null
  }

  return (
    <footer className="w-full border-t bg-background py-8">
      <div className="container px-8 mx-8 md:px-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="flex flex-col gap-4">
            <Link href="/" className="flex items-center">
              <div className="flex items-center">
                <Image src="/logo-icon.png" alt="TICKET4U" width={32} height={32} />
                <Image src="/logo-text.png" alt="TICKET4U" width={120} height={24} className="ml-1" />
              </div>
            </Link>
            <p className="text-sm text-muted-foreground">
              TICKET4U는 다양한 공연 티켓을 예매할 수 있는 플랫폼입니다. 특별한 순간을 위한 최고의 선택이 되어드립니다.
            </p>
          </div>

          <div className="flex flex-col gap-2">
            <h3 className="text-base font-medium mb-2">서비스</h3>
            <Link href="/performances" className="text-sm text-muted-foreground hover:text-foreground">
              공연 예매
            </Link>
            <Link href="/users/mypage/register" className="text-sm text-muted-foreground hover:text-foreground">
              공연 관리자 신청
            </Link>
            <Link href="/faq" className="text-sm text-muted-foreground hover:text-foreground">
              고객 지원
            </Link>
          </div>

          <div className="flex flex-col gap-2">
            <h3 className="text-base font-medium mb-2">회사 정보</h3>
            <Link href="/about" className="text-sm text-muted-foreground hover:text-foreground">
              회사 소개
            </Link>
            <Link href="/terms" className="text-sm text-muted-foreground hover:text-foreground">
              이용약관
            </Link>
            <Link href="/privacy" className="text-sm text-muted-foreground hover:text-foreground">
              개인정보처리방침
            </Link>
            <Link href="/contact" className="text-sm text-muted-foreground hover:text-foreground">
              고객센터
            </Link>
          </div>

          <div className="flex flex-col gap-2">
            <h3 className="text-base font-medium mb-2">문의</h3>
            <p className="text-sm text-muted-foreground">이메일: support@ticket4u.com</p>
            <p className="text-sm text-muted-foreground">전화: 02-123-4567</p>
            <p className="text-sm text-muted-foreground">주소: 서울특시 강남구 테헤란로 123</p>
          </div>
        </div>

        <div className="mt-8 pt-4 border-t text-center text-sm text-muted-foreground">
          &copy; {new Date().getFullYear()} TICKET4U. All rights reserved.
        </div>
      </div>
    </footer>
  )
}
