"use client"

import { useState } from "react"
import Image from "next/image"
import { useRouter } from "next/navigation"
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
    <div className="container flex h-screen w-screen flex-col items-center justify-center">
      <div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px]">
        <div className="flex flex-col space-y-2 text-center">
          <div className="flex justify-center mb-6">
            <Image src="/logo-icon.png" alt="Ticket4U 로고" width={64} height={64} />
          </div>
          <h1 className="text-2xl font-semibold tracking-tight">Ticket4U에 오신 것을 환영합니다</h1>
          <p className="text-sm text-muted-foreground">소셜 계정으로 로그인하세요</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>로그인</CardTitle>
            <CardDescription>아래 소셜 계정 중 하나를 선택하여 로그인하세요</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button 
              className="w-full bg-[#4285F4] hover:bg-[#4285F4]/90" 
              onClick={() => handleSocialLogin('google')}
              disabled={isLoading}
            >
              <div className="bg-white p-1 rounded-full mr-2">
                <svg width="18" height="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
                  <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                  <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                  <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                  <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                </svg>
              </div>
              구글로 로그인
            </Button>
            <Button 
              className="w-full bg-[#03C75A] hover:bg-[#03C75A]/90" 
              onClick={() => handleSocialLogin('naver')}
              disabled={isLoading}
            >
              <div className="mr-2 text-white font-bold">N</div>
              네이버로 로그인
            </Button>
            <Button 
              className="w-full bg-[#FEE500] hover:bg-[#FEE500]/90 text-black" 
              onClick={() => handleSocialLogin('kakao')}
              disabled={isLoading}
            >
              <div className="mr-2">
                <svg width="18" height="18" viewBox="0 0 256 256" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#000000" d="M128 36C70.562 36 24 72.713 24 118c0 29.279 19.466 54.97 48.748 69.477-1.593 5.494-10.237 35.344-10.581 37.689c0 0-.207 1.762.934 2.434s2.483.15 2.483.15c3.272-.457 37.943-24.811 43.944-29.04 5.995.849 12.168 1.29 18.472 1.29 57.438 0 104-36.712 104-82c0-45.287-46.562-82-104-82z"/>
                </svg>
              </div>
              카카오로 로그인
            </Button>
          </CardContent>
          <CardFooter className="flex justify-center">
            <p className="text-xs text-muted-foreground">
              로그인하면 Ticket4U의 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.
            </p>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
