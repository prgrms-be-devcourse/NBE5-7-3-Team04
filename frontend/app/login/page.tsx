"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { getSocialLoginUrl } from "@/src/api/api"

export default function LoginPage() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)

  const handleSocialLogin = async (provider: string) => {
    setIsLoading(true)
    try {
      // 소셜 로그인 리다이렉트 URL 가져오기
      const redirectUrl = getSocialLoginUrl(provider)
      
      // 소셜 로그인 페이지로 리다이렉트
      window.location.href = redirectUrl
    } catch (error) {
      console.error("로그인 오류:", error)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-muted/40 py-12">
      <Card className="mx-auto w-full max-w-md">
        <CardHeader className="space-y-2 text-center">
          <div className="flex justify-center mb-4">
            <Image src="/logo-icon.png" alt="TICKET4U" width={48} height={48} />
          </div>
          <CardTitle className="text-2xl font-bold">로그인</CardTitle>
          <CardDescription>소셜 계정으로 간편하게 로그인하세요.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button
            variant="outline"
            className="w-full h-12 relative"
            onClick={() => handleSocialLogin("google")}
            disabled={isLoading}
          >
            <div className="absolute left-4 flex items-center justify-center">
              <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  fill="#4285F4"
                />
                <path
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  fill="#34A853"
                />
                <path
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                  fill="#FBBC05"
                />
                <path
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                  fill="#EA4335"
                />
              </svg>
            </div>
            Google 계정으로 로그인
          </Button>

          <Button
            variant="outline"
            className="w-full h-12 relative"
            onClick={() => handleSocialLogin("naver")}
            disabled={isLoading}
          >
            <div className="absolute left-4 flex items-center justify-center">
              <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <rect width="24" height="24" fill="#03C75A" />
                <path d="M16.273 12.845L7.376 0H0V24H7.727V11.155L16.624 24H24V0H16.273V12.845Z" fill="white" />
              </svg>
            </div>
            네이버 계정으로 로그인
          </Button>

          <Button
            variant="outline"
            className="w-full h-12 relative"
            onClick={() => handleSocialLogin("kakao")}
            disabled={isLoading}
          >
            <div className="absolute left-4 flex items-center justify-center">
              <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <rect width="24" height="24" fill="#FEE500" />
                <path
                  d="M12 5.5C7.03125 5.5 3 8.7207 3 12.7083C3 15.3136 4.79799 17.5783 7.47396 18.7803C7.30078 19.3807 6.60677 21.8698 6.54818 22.1647C6.47786 22.5246 6.75 22.5246 6.91146 22.4219C7.03646 22.3444 9.95833 20.3438 10.8333 19.7656C11.2161 19.8099 11.6068 19.8333 12 19.8333C16.9688 19.8333 21 16.6126 21 12.625C21 8.7207 16.9688 5.5 12 5.5Z"
                  fill="#3A1D1D"
                />
              </svg>
            </div>
            카카오 계정으로 로그인
          </Button>
        </CardContent>
        <CardFooter className="flex flex-col">
          <Separator className="my-4" />
          <p className="text-xs text-center text-muted-foreground">
            로그인 시 TICKET4U의{" "}
            <a href="/terms" className="underline">
              이용약관
            </a>
            과{" "}
            <a href="/privacy" className="underline">
              개인정보처리방침
            </a>
            에 동의하게 됩니다.
          </p>
        </CardFooter>
      </Card>
    </div>
  )
}
