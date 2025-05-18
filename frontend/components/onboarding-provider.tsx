"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { usePathname } from "next/navigation"
import { UserOnboardingModal } from "@/components/user-onboarding-modal"
import { checkOnboardingStatus } from "@/lib/api"

export function OnboardingProvider({ children }: { children: React.ReactNode }) {
  const [needsOnboarding, setNeedsOnboarding] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const pathname = usePathname()

  // 로그인 페이지나 관리자 페이지에서는 온보딩 체크를 하지 않음
  const shouldCheckOnboarding =
    !pathname.startsWith("/login") && !pathname.startsWith("/admin") && !pathname.includes("callback")

  useEffect(() => {
    if (!shouldCheckOnboarding) {
      setIsLoading(false)
      return
    }

    const checkStatus = async () => {
      try {
        const status = await checkOnboardingStatus()

        if (status.needsOnboarding) {
          setNeedsOnboarding(true)
          setIsModalOpen(true)
        }
      } catch (error) {
        // 로그인하지 않은 상태이거나 API 오류 - 온보딩 필요 없음
        console.log("온보딩 체크 중 오류 또는 로그인되지 않음:", error)
      } finally {
        setIsLoading(false)
      }
    }

    checkStatus()
  }, [shouldCheckOnboarding, pathname])

  const handleOnboardingComplete = () => {
    setNeedsOnboarding(false)
    setIsModalOpen(false)
  }

  if (isLoading) {
    return <>{children}</>
  }

  return (
    <>
      {children}

      {needsOnboarding && (
        <UserOnboardingModal open={isModalOpen} onOpenChange={setIsModalOpen} onComplete={handleOnboardingComplete} />
      )}
    </>
  )
}
