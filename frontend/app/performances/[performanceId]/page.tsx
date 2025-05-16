import { Suspense } from "react"
import dynamic from "next/dynamic"

const PerformanceDetailClient = dynamic(() => import("./client"), {
  loading: () => (
    <div className="container py-8 flex items-center justify-center min-h-[50vh]">
      <div className="flex flex-col items-center">
        <p className="text-muted-foreground">공연 정보를 불러오는 중...</p>
      </div>
    </div>
  ),
})

export default async function PerformanceDetailPage({ params }: { params: { performanceId: string } }) {
  const performanceId = await Promise.resolve(params.performanceId)
  
  return (
    <Suspense fallback={
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center">
          <p className="text-muted-foreground">공연 정보를 불러오는 중...</p>
        </div>
      </div>
    }>
      <PerformanceDetailClient performanceId={performanceId} />
    </Suspense>
  )
}
