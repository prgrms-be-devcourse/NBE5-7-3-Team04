'use client'

import { useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { saveToken } from '@/src/auth/user'

export default function OAuthCallback() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const accessToken = searchParams.get('accessToken')
    const refreshToken = searchParams.get('refreshToken')
    const isNewUser = searchParams.get('isNewUser') === 'true'

    if (accessToken && refreshToken) {
      // 토큰 저장
      saveToken(accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      // 사용자 정보 파싱 (JWT 토큰에서)
      try {
        const base64Url = accessToken.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
        )

        const { sub: id, role } = JSON.parse(jsonPayload)
        
        // 사용자 정보 저장
        localStorage.setItem('userInfo', JSON.stringify({
          id,
          role: role.replace('ROLE_', ''),
          // 필요한 경우 추가 정보는 API를 통해 가져올 수 있습니다
        }))

        // 가입 여부에 따라 리다이렉션
        if (isNewUser) {
          router.replace('/onboard')
        } else {
          router.replace('/')
        }
      } catch (error) {
        console.error('Error parsing token:', error)
        router.replace('/login?error=invalid_token')
      }
    } else {
      router.replace('/login?error=missing_token')
    }
  }, [router, searchParams])

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
        <p className="mt-4 text-gray-600">로그인 처리 중...</p>
      </div>
    </div>
  )
} 