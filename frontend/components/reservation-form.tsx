"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { useRouter } from "next/navigation"
import { Loader2, Calendar, Clock } from "lucide-react"

interface ReservationFormProps {
  performanceId: string
  sessionId: string
  price: number
  sessionName: string
  sessionTime: string
  sessionDate: string
}

export function ReservationForm({
  performanceId,
  sessionId,
  price,
  sessionName,
  sessionTime,
  sessionDate,
}: ReservationFormProps) {
  const [quantity, setQuantity] = useState(1)
  const [paymentMethod, setPaymentMethod] = useState("bank")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { toast } = useToast()
  const router = useRouter()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    try {
      // 실제 구현에서는 API 호출
      await new Promise((resolve) => setTimeout(resolve, 1500))

      toast({
        title: "예약이 완료되었습니다!",
        description: "예약 내역은 마이페이지에서 확인하실 수 있습니다.",
      })

      // 예약 완료 후 예약 내역 페이지로 이동
      router.push("/users/mypage/reservations")
    } catch (error) {
      toast({
        title: "예약 중 오류가 발생했습니다.",
        description: "잠시 후 다시 시도해주세요.",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const totalPrice = price * quantity

  return (
    <Card>
      <CardHeader>
        <CardTitle>예매하기</CardTitle>
      </CardHeader>
      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-4">
          <div className="rounded-md bg-muted p-3 space-y-2">
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="font-medium">{sessionDate}</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <span className="font-medium">
                {sessionName} ({sessionTime})
              </span>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="quantity">예매 수량</Label>
            <Select
              value={quantity.toString()}
              onValueChange={(value) => setQuantity(Number.parseInt(value))}
              disabled={isSubmitting}
            >
              <SelectTrigger id="quantity">
                <SelectValue placeholder="수량 선택" />
              </SelectTrigger>
              <SelectContent>
                {[1, 2, 3, 4, 5].map((num) => (
                  <SelectItem key={num} value={num.toString()}>
                    {num}매
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="payment">결제 방법</Label>
            <Select value={paymentMethod} onValueChange={setPaymentMethod} disabled={isSubmitting}>
              <SelectTrigger id="payment">
                <SelectValue placeholder="결제 방법 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="bank">무통장 입금</SelectItem>
                <SelectItem value="card">신용카드</SelectItem>
                <SelectItem value="phone">휴대폰 결제</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {paymentMethod === "bank" && (
            <div className="rounded-md bg-muted p-3 text-sm">
              <p className="font-medium">입금 계좌 정보</p>
              <p className="text-muted-foreground">신한은행 123-456-789012</p>
              <p className="text-muted-foreground">예금주: 티켓-4-U</p>
              <p className="mt-2 text-xs text-muted-foreground">
                * 입금 후 자동으로 확인되며, 24시간 이내 미입금 시 자동 취소됩니다.
              </p>
            </div>
          )}

          <div className="rounded-md bg-muted p-3">
            <div className="flex items-center justify-between">
              <span>티켓 가격</span>
              <span>
                {price.toLocaleString()}원 x {quantity}매
              </span>
            </div>
            <div className="mt-2 flex items-center justify-between font-bold">
              <span>총 결제 금액</span>
              <span>{totalPrice.toLocaleString()}원</span>
            </div>
          </div>
        </CardContent>
        <CardFooter>
          <Button type="submit" className="w-full" disabled={isSubmitting}>
            {isSubmitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                예매 처리 중...
              </>
            ) : (
              "예매하기"
            )}
          </Button>
        </CardFooter>
      </form>
    </Card>
  )
}
