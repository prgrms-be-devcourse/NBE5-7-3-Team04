"use client"

import { useEffect, useState } from "react"
import { useRouter, usePathname } from "next/navigation"

const API_URL = process.env.NEXT_PUBLIC_API_URL

export function useAdminAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await fetch(`${API_URL}/admin/check-auth`, {
          credentials: 'include'
        })
        
        if (response.ok) {
          setIsAuthenticated(true)
        } else {
          setIsAuthenticated(false)
          if (pathname?.startsWith("/admin") && pathname !== "/admin/login") {
            router.push("/admin/login")
          }
        }
      } catch (error) {
        console.error("Auth check error:", error)
        setIsAuthenticated(false)
      } finally {
        setIsLoading(false)
      }
    }

    checkAuth()
  }, [pathname, router])

  const requireAdminAuth = (callback?: () => void) => {
    if (!isLoading && !isAuthenticated) {
      router.push("/admin/login")
      return false
    }

    if (callback && !isLoading && isAuthenticated) {
      callback()
    }

    return isAuthenticated
  }

  return { isAuthenticated, isLoading, requireAdminAuth }
}

export async function adminLogout() {
  try {
    const response = await fetch(`${API_URL}/admin/logout`, {
      method: 'POST',
      credentials: 'include'
    })
    
    if (response.ok) {
      window.location.href = '/admin/login'
    } else {
      throw new Error('로그아웃 실패')
    }
  } catch (error) {
    console.error('로그아웃 중 오류 발생:', error)
    // 에러가 발생해도 로그인 페이지로 이동
    window.location.href = '/admin/login'
  }
} 