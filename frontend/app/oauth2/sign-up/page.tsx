"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"

export default function OAuth2SignUpPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const accessToken = searchParams.get("accessToken")
    const refreshToken = searchParams.get("refreshToken")

    if (accessToken && refreshToken) {
      // 토큰 저장 (로컬스토리지 등)
      localStorage.setItem("token", accessToken)
      localStorage.setItem("refreshToken", refreshToken)
      // TODO: 사용자 정보 요청 및 회원가입 추가 처리 필요시 여기에

      // 메인 페이지로 이동 (강제 새로고침)
      window.location.replace("/")
    } else {
      // 토큰이 없으면 로그인 페이지로
      router.replace("/login")
    }
  }, [router, searchParams])

  return <div>로그인 처리 중입니다...</div>
} 