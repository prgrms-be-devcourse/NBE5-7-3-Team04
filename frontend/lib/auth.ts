"use client"

// Authentication utilities
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

interface User {
  id: number
  name: string
  email: string
  role: string
  profileImage?: string
}

// Check if user is authenticated
export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [userRole, setUserRole] = useState<string | null>(null)
  const [user, setUser] = useState<User | null>(null)
  const router = useRouter()

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("token")
      const userInfo = localStorage.getItem("userInfo")
      
      if (token && userInfo) {
        try {
          const parsedUser = JSON.parse(userInfo)
          setUser(parsedUser)
          setUserRole(parsedUser.role || "USER")
          setIsAuthenticated(true)
        } catch (error) {
          console.error("Error parsing user info:", error)
          setUser(null)
          setUserRole(null)
          setIsAuthenticated(false)
        }
      } else {
        setUser(null)
        setUserRole(null)
        setIsAuthenticated(false)
      }
      setIsLoading(false)
    }

    // 초기 체크
    checkAuth()

    // storage 이벤트 리스너
    const handleStorageChange = () => {
      checkAuth()
    }

    window.addEventListener("storage", handleStorageChange)
    return () => {
      window.removeEventListener("storage", handleStorageChange)
    }
  }, [])

  const requireAuth = (callback?: () => void) => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login")
      return false
    }

    if (callback && !isLoading && isAuthenticated) {
      callback()
    }

    return isAuthenticated
  }

  const requireRole = (role: string | string[], callback?: () => void) => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login")
      return false
    }

    const roles = Array.isArray(role) ? role : [role]
    const hasRole = userRole && roles.includes(userRole)

    if (!isLoading && !hasRole) {
      router.push("/")
      return false
    }

    if (callback && !isLoading && hasRole) {
      callback()
    }

    return hasRole
  }

  return { isAuthenticated, isLoading, userRole, user, requireAuth, requireRole }
}

// Mock login function for testing
export function mockLogin(role = "USER") {
  localStorage.setItem("token", "mock_token_" + Math.random().toString(36).substring(2, 15))
  localStorage.setItem(
    "userInfo",
    JSON.stringify({
      id: Math.floor(Math.random() * 1000),
      name: "사용자" + Math.floor(Math.random() * 100),
      email: "user" + Math.floor(Math.random() * 100) + "@example.com",
      role: role,
      profileImage: "/default-avatar.png"
    }),
  )

  // Trigger storage event for other tabs
  window.dispatchEvent(new Event("storage"))
}

// Logout function
export const logout = () => {
  localStorage.removeItem("token")
  localStorage.removeItem("userInfo")

  // Trigger storage event for other tabs
  window.dispatchEvent(new Event("storage"))

  window.location.href = "/login"
}

// 토큰 저장 함수
export const saveToken = (token: string) => {
  localStorage.setItem("token", token)
}

// 토큰 가져오기 함수
export const getToken = () => {
  return localStorage.getItem("token")
}

// 토큰이 유효한지 확인하는 함수
export const isTokenValid = (token: string) => {
  try {
    // JWT 토큰 디코딩
    const base64Url = token.split(".")[1]
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    )

    const { exp } = JSON.parse(jsonPayload)
    const currentTime = Math.floor(Date.now() / 1000)

    return exp > currentTime
  } catch {
    return false
  }
}
