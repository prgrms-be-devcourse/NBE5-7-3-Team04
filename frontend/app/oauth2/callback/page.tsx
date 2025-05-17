"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"

export default function OAuth2CallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const accessToken = searchParams.get("accessToken")
    const refreshToken = searchParams.get("refreshToken")

    if (accessToken && refreshToken) {
      localStorage.setItem("token", accessToken)
      localStorage.setItem("refreshToken", refreshToken)
      // TODO: 사용자 정보 요청 및 회원가입 추가 처리 필요시 여기에

      window.location.replace("/")
    } else {
      router.replace("/login")
    }
  }, [router, searchParams])

  return <div>로그인 처리 중입니다...</div>
} 