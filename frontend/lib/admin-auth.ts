"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"

const API_URL = process.env.NEXT_PUBLIC_API_URL

// Check if admin is authenticated
export function useAdminAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const router = useRouter()

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_URL}/api/v1/admin/check-auth`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json'
        }
      })

      if (response.ok) {
        setIsAuthenticated(true)
        return true
      } else {
        setIsAuthenticated(false)
        return false
      }
    } catch (error) {
      console.error("Auth check error:", error)
      setIsAuthenticated(false)
      return false
    }
  }

  const requireAdminAuth = async () => {
    const isAuth = await checkAuth()
    if (!isAuth) {
      router.replace("/admin/login")
      return false
    }
    return true
  }

  return { isAuthenticated, requireAdminAuth, checkAuth }
}

// CSRF 토큰 가져오기
async function getCsrfToken() {
  try {
    const response = await fetch(`${API_URL}/api/v1/admin/csrf`, {
      method: 'GET',
      credentials: 'include'
    })
    if (response.ok) {
      const token = await response.json()
      return token.token
    }
    return null
  } catch (error) {
    console.error('CSRF 토큰 가져오기 실패:', error)
    return null
  }
}

// Admin logout function
export async function adminLogout() {
  try {
    const csrfToken = await getCsrfToken()
    if (!csrfToken) {
      console.error('CSRF 토큰을 가져올 수 없습니다')
      return
    }
    
    const response = await fetch(`${API_URL}/api/v1/admin/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'X-XSRF-TOKEN': csrfToken
      }
    })

    if (response.ok) {
      window.location.href = '/admin/login'
    } else {
      console.error('로그아웃 실패')
    }
  } catch (error) {
    console.error('로그아웃 중 오류 발생:', error)
  }
}
