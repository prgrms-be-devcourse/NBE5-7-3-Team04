"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Calendar } from "@/components/ui/calendar"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { useRouter } from "next/navigation"
import { Loader2, CalendarIcon, Clock, Ticket, ChevronRight, ChevronLeft } from "lucide-react"
import { format } from "date-fns"
import { ko } from "date-fns/locale"
import { cn } from "@/lib/utils"
import { Badge } from "@/components/ui/badge"

interface Session {
  id: string
  date: string
  time: string
  name: string
  capacity: number
  remainingSeats: number
  price: number
}

interface Performance {
  id: string
  title: string
  sessions: Session[]
}

interface ReservationCalendarModalProps {
  performance: Performance
}

export function ReservationCalendarModal({ performance }: ReservationCalendarModalProps) {
  const [open, setOpen] = useState(false)
  const [step, setStep] = useState(1)
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(undefined)
  const [selectedSessionId, setSelectedSessionId] = useState<string | undefined>(undefined)
  const [quantity, setQuantity] = useState(1)
  const [paymentMethod, setPaymentMethod] = useState("bank")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { toast } = useToast()
  const router = useRouter()

  // 날짜별로 세션 그룹화
  const sessionsByDate = performance.sessions.reduce(
    (acc, session) => {
      if (!acc[session.date]) {
        acc[session.date] = []
      }
      acc[session.date].push(session)
      return acc
    },
    {} as Record<string, Session[]>,
  )

  // 캘린더에 표시할 날짜들 (세션이 있는 날짜만)
  const sessionDates = Object.keys(sessionsByDate).map((dateStr) => new Date(dateStr))

  // 선택한 날짜에 해당하는 세션들
  const sessionsForSelectedDate = selectedDate ? sessionsByDate[format(selectedDate, "yyyy-MM-dd")] || [] : []

  // 선택한 세션
  const selectedSession = selectedSessionId ? performance.sessions.find((s) => s.id === selectedSessionId) : undefined

  const totalPrice = selectedSession ? selectedSession.price * quantity : 0

  const handleSubmit = async () => {
    if (!selectedSession) return

    setIsSubmitting(true)

    try {
      // 실제 구현에서는 API 호출
      await new Promise((resolve) => setTimeout(resolve, 1500))

      toast({
        title: "예약이 완료되었습니다!",
        description: "예약 내역은 마이페이지에서 확인하실 수 있습니다.",
      })

      setOpen(false)
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

  const nextStep = () => {
    if (step === 1 && !selectedDate) return
    if (step === 2 && !selectedSessionId) return
    setStep(step + 1)
  }

  const prevStep = () => {
    setStep(step - 1)
  }

  const resetModal = () => {
    setStep(1)
    setSelectedDate(undefined)
    setSelectedSessionId(undefined)
    setQuantity(1)
    setPaymentMethod("bank")
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(newOpen) => {
        setOpen(newOpen)
        if (!newOpen) resetModal()
      }}
    >
      <Button className="w-full" onClick={() => setOpen(true)}>
        예매하기
      </Button>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{performance.title} 예매하기</DialogTitle>
          <DialogDescription>
            {step === 1 && "공연 날짜를 선택해주세요."}
            {step === 2 && "회차를 선택해주세요."}
            {step === 3 && "예매 수량과 결제 방법을 선택해주세요."}
          </DialogDescription>
        </DialogHeader>

        {/* 스텝 인디케이터 */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div
              className={cn(
                "flex h-8 w-8 items-center justify-center rounded-full border text-xs font-medium",
                step >= 1 ? "border-primary bg-primary text-primary-foreground" : "border-muted text-muted-foreground",
              )}
            >
              1
            </div>
            <div className={cn("h-0.5 w-8", step >= 2 ? "bg-primary" : "bg-muted")} />
            <div
              className={cn(
                "flex h-8 w-8 items-center justify-center rounded-full border text-xs font-medium",
                step >= 2 ? "border-primary bg-primary text-primary-foreground" : "border-muted text-muted-foreground",
              )}
            >
              2
            </div>
            <div className={cn("h-0.5 w-8", step >= 3 ? "bg-primary" : "bg-muted")} />
            <div
              className={cn(
                "flex h-8 w-8 items-center justify-center rounded-full border text-xs font-medium",
                step >= 3 ? "border-primary bg-primary text-primary-foreground" : "border-muted text-muted-foreground",
              )}
            >
              3
            </div>
          </div>
        </div>

        {/* 스텝 1: 날짜 선택 */}
        {step === 1 && (
          <div className="py-4">
            <Calendar
              mode="single"
              selected={selectedDate}
              onSelect={setSelectedDate}
              className="mx-auto"
              locale={ko}
              disabled={(date) => {
                // 세션이 있는 날짜만 선택 가능
                return !sessionDates.some(
                  (sessionDate) =>
                    sessionDate.getFullYear() === date.getFullYear() &&
                    sessionDate.getMonth() === date.getMonth() &&
                    sessionDate.getDate() === date.getDate(),
                )
              }}
              modifiers={{
                available: sessionDates,
              }}
              modifiersClassNames={{
                available: "bg-green-50 text-green-600 font-medium",
              }}
            />
            <p className="text-center text-sm text-muted-foreground mt-2">
              {sessionDates.length > 0 ? "초록색으로 표시된 날짜만 공연이 있습니다." : "예매 가능한 공연이 없습니다."}
            </p>
          </div>
        )}

        {/* 스텝 2: 회차 선택 */}
        {step === 2 && (
          <div className="py-4">
            <div className="flex items-center gap-2 mb-4">
              <CalendarIcon className="h-5 w-5 text-muted-foreground" />
              <span className="font-medium">{selectedDate && format(selectedDate, "PPP (eee)", { locale: ko })}</span>
            </div>

            {sessionsForSelectedDate.length > 0 ? (
              <RadioGroup value={selectedSessionId} onValueChange={setSelectedSessionId} className="space-y-3">
                {sessionsForSelectedDate.map((session) => {
                  const soldOut = session.remainingSeats === 0
                  const almostSoldOut = session.remainingSeats <= session.capacity * 0.1

                  return (
                    <div
                      key={session.id}
                      className={cn(
                        "flex items-center space-x-2 rounded-md border p-3",
                        soldOut ? "opacity-50" : "",
                        selectedSessionId === session.id ? "border-primary" : "",
                      )}
                    >
                      <RadioGroupItem value={session.id} id={`session-${session.id}`} disabled={soldOut} />
                      <Label htmlFor={`session-${session.id}`} className="flex-1 cursor-pointer">
                        <div className="flex justify-between items-center">
                          <span className="font-medium">{session.name}</span>
                          <span className="text-sm font-medium">{session.price.toLocaleString()}원</span>
                        </div>
                        <div className="text-xs text-muted-foreground flex justify-between items-center mt-1">
                          <span>{session.time}</span>
                          <div className="flex items-center gap-1">
                            {soldOut ? (
                              <Badge variant="destructive">매진</Badge>
                            ) : almostSoldOut ? (
                              <Badge variant="secondary">매진임박</Badge>
                            ) : (
                              <Badge variant="outline">잔여: {session.remainingSeats}석</Badge>
                            )}
                          </div>
                        </div>
                      </Label>
                    </div>
                  )
                })}
              </RadioGroup>
            ) : (
              <div className="text-center py-8 text-muted-foreground">선택한 날짜에 예매 가능한 회차가 없습니다.</div>
            )}
          </div>
        )}

        {/* 스텝 3: 수량 및 결제 방법 선택 */}
        {step === 3 && selectedSession && (
          <div className="py-4 space-y-4">
            <div className="rounded-md bg-muted p-3 space-y-2">
              <div className="flex items-center gap-2 text-sm">
                <CalendarIcon className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">
                  {format(new Date(selectedSession.date), "PPP (eee)", { locale: ko })}
                </span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <span className="font-medium">
                  {selectedSession.name} ({selectedSession.time})
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
                  {Array.from({ length: Math.min(5, selectedSession.remainingSeats) }, (_, i) => i + 1).map((num) => (
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
                  {selectedSession.price.toLocaleString()}원 x {quantity}매
                </span>
              </div>
              <div className="mt-2 flex items-center justify-between font-bold">
                <span>총 결제 금액</span>
                <span>{totalPrice.toLocaleString()}원</span>
              </div>
            </div>
          </div>
        )}

        <DialogFooter className="flex items-center justify-between">
          {step > 1 ? (
            <Button type="button" variant="outline" onClick={prevStep}>
              <ChevronLeft className="mr-2 h-4 w-4" />
              이전
            </Button>
          ) : (
            <div></div>
          )}

          {step < 3 ? (
            <Button
              type="button"
              onClick={nextStep}
              disabled={(step === 1 && !selectedDate) || (step === 2 && !selectedSessionId)}
            >
              다음
              <ChevronRight className="ml-2 h-4 w-4" />
            </Button>
          ) : (
            <Button type="button" onClick={handleSubmit} disabled={isSubmitting || !selectedSessionId}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  예매 처리 중...
                </>
              ) : (
                <>
                  <Ticket className="mr-2 h-4 w-4" />
                  예매하기
                </>
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
