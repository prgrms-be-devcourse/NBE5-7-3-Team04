"use client"

import { useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { saveToken } from "@/lib/auth"

export default function AuthCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // URL에서 토큰 파라미터 가져오기
        const token = searchParams.get("token")
        
        if (token) {
          // 토큰 저장
          saveToken(token)
          
          // 홈페이지로 리다이렉트
          router.push("/")
        } else {
          console.error("토큰이 없습니다.")
          router.push("/login")
        }
      } catch (error) {
        console.error("로그인 처리 중 오류 발생:", error)
        router.push("/login")
      }
    }

    handleCallback()
  }, [router, searchParams])

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <h1 className="text-2xl font-bold mb-4">로그인 처리 중...</h1>
        <p className="text-muted-foreground">잠시만 기다려주세요.</p>
      </div>
    </div>
  )
} 