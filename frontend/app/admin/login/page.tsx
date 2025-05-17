"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAdminAuth } from "@/lib/admin-auth"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"

const API_URL = process.env.NEXT_PUBLIC_API_URL

export default function AdminLogin() {
  const [adminId, setAdminId] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const router = useRouter()
  const { checkAuth } = useAdminAuth()

  useEffect(() => {
    const checkSession = async () => {
      const isAuth = await checkAuth()
      if (isAuth) {
        router.replace("/admin")
      }
    }
    checkSession()
  }, [checkAuth, router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")

    try {
      const response = await fetch(`${API_URL}/api/v1/admin/login`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          adminId: adminId,
          password: password
        })
      })

      if (response.ok) {
        router.replace("/admin")
      } else {
        setError("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.")
      }
    } catch (error) {
      setError("로그인 중 오류가 발생했습니다.")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            관리자 로그인
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="adminId" className="sr-only">
                아이디
              </label>
              <input
                id="adminId"
                name="adminId"
                type="text"
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="아이디"
                value={adminId}
                onChange={(e) => setAdminId(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">
                비밀번호
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {error && (
            <div className="text-red-500 text-sm text-center">{error}</div>
          )}

          <div>
            <button
              type="submit"
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            >
              로그인
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
