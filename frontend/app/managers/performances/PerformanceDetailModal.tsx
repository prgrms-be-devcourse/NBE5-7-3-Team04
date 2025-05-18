"use client"
import { useEffect, useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { getManagerPerformanceDetails } from "@/src/api/api"
import { Loader2, AlertCircle } from "lucide-react"
import { format, parseISO } from "date-fns"

export function PerformanceDetailModal({ open, onOpenChange, performanceId }: { open: boolean, onOpenChange: (v: boolean) => void, performanceId: string | null }) {
  const [performance, setPerformance] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!performanceId) return
    setLoading(true)
    setError(null)
    getManagerPerformanceDetails(performanceId)
      .then(setPerformance)
      .catch(() => setError("공연 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false))
  }, [performanceId])

  // 날짜 포맷 함수
  const formatDateTime = (dateString: string) => {
    try {
      return format(parseISO(dateString), "yyyy년 MM월 dd일 HH시 mm분")
    } catch {
      return dateString
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>공연 상세 정보</DialogTitle>
        </DialogHeader>
        {loading ? (
          <div className="flex flex-col items-center py-8">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <p className="mt-4 text-muted-foreground">불러오는 중...</p>
          </div>
        ) : error ? (
          <div className="flex flex-col items-center py-8">
            <AlertCircle className="h-8 w-8 text-destructive" />
            <p className="mt-4 text-destructive font-medium">{error}</p>
          </div>
        ) : performance ? (
          <div className="space-y-4">
            <div>
              <h2 className="text-2xl font-bold mb-1">{performance.title}</h2>
              <div className="text-sm text-muted-foreground mb-2">{performance.venue}</div>
              <div className="flex flex-col gap-1 text-sm">
                <div>
                  <span className="font-medium">공연 기간:</span> {formatDateTime(performance.startDate)} ~ {formatDateTime(performance.endDate)}
                </div>
                <div><span className="font-medium">총 좌석 수:</span> {performance.totalSeats}석</div>
                {performance.status && (
                  <div><span className="font-medium">상태:</span> {performance.status}</div>
                )}
              </div>
            </div>
            {/* 포스터는 하단에 작게 */}
            {performance.fileUrl && (
              <div className="mt-4">
                <img src={performance.fileUrl} alt={performance.title} className="w-40 h-auto rounded mx-auto border" />
              </div>
            )}
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  )
} 