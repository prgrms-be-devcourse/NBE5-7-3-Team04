import { Suspense } from "react"
import PerformanceDetailClient from "./client"
import { Metadata } from "next"

interface Props {
  params: { performanceId: string }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  return {
    title: `공연 상세 정보`,
  }
}

export default function PerformanceDetailPage({ params }: Props) {
    return (
    <Suspense fallback={
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
          <p className="mt-4 text-gray-600">공연 정보를 불러오고 있습니다...</p>
        </div>
      </div>
    }>
      <PerformanceDetailClient performanceId={params.performanceId} />
    </Suspense>
  )
}
