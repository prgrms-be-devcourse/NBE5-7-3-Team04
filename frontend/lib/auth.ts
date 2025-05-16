"use client"

// Authentication utilities
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

// Check if user is authenticated
export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [userRole, setUserRole] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("token")
      if (!token) {
        setIsAuthenticated(false)
        setUserRole(null)
        setIsLoading(false)
        return
      }

      // In a real app, you would verify the token with the server
      // For now, we'll just check if it exists and parse the user role from it
      try {
        // Mock implementation - in a real app, you would decode the JWT
        // This is just for demonstration purposes
        const userInfo = localStorage.getItem("userInfo")
        if (userInfo) {
          const user = JSON.parse(userInfo)
          setUserRole(user.role || "USER")
        } else {
          // Default to USER role if no specific role is found
          setUserRole("USER")
        }

        setIsAuthenticated(true)
      } catch (error) {
        console.error("Error parsing user info:", error)
        setIsAuthenticated(false)
        setUserRole(null)
      }

      setIsLoading(false)
    }

    checkAuth()

    // Listen for storage events (e.g., if token is removed in another tab)
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

  return { isAuthenticated, isLoading, userRole, requireAuth, requireRole }
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
    }),
  )

  // Trigger storage event for other tabs
  window.dispatchEvent(new Event("storage"))
}

// Logout function
export function logout() {
  localStorage.removeItem("token")
  localStorage.removeItem("userInfo")

  // Trigger storage event for other tabs
  window.dispatchEvent(new Event("storage"))
}
