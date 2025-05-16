"use client"

import { useEffect, useState } from "react"
import { useRouter, usePathname } from "next/navigation"

// Check if admin is authenticated
export function useAdminAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("adminToken")
      if (!token) {
        setIsAuthenticated(false)
        setIsLoading(false)

        // Redirect to login if on admin page
        if (pathname?.startsWith("/admin") && pathname !== "/admin/login") {
          router.push("/admin/login")
        }
        return
      }

      // In a real app, you would verify the token with the server
      setIsAuthenticated(true)
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

// Admin logout function
export function adminLogout() {
  localStorage.removeItem("adminToken")
  localStorage.removeItem("adminInfo")

  // Trigger storage event for other tabs
  window.dispatchEvent(new Event("storage"))
}
