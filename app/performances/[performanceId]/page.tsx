"use client"

import { use, useState, useEffect } from "react"
import { getPerformanceDetail } from "@/lib/api"
import type { Performance } from "@/types"

export default function PerformanceDetailPage({ params }: { params: { performanceId: string } }) {
  const unwrappedParams = use(params)
  const [performance, setPerformance] = useState<Performance | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchPerformance = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getPerformanceDetail(unwrappedParams.performanceId)
        setPerformance(data)
      } catch (err) {
        console.error("Error fetching performance:", err)
        setError("공연 정보를 불러오는데 실패했습니다.")
      } finally {
        setLoading(false)
      }
    }

    fetchPerformance()
  }, [unwrappedParams.performanceId])

  if (loading) return <div>로딩 중...</div>
  if (error) return <div>{error}</div>
  if (!performance) return <div>공연 정보를 찾을 수 없습니다.</div>

  return (
    <div>
      <h1>{performance.title}</h1>
      {/* 나머지 공연 상세 정보 표시 */}
    </div>
  )
} 