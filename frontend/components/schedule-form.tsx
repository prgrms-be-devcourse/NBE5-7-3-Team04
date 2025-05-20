"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { formatKSTDateTime } from "@/src/api/utils/date"
import { CalendarIcon, Clock } from "lucide-react"
import { cn } from "@/lib/utils"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DialogFooter } from "@/components/ui/dialog"

interface ScheduleFormProps {
  onSubmit: (data: { startTime: string; endTime: string }) => void
}

export function ScheduleForm({ onSubmit }: ScheduleFormProps) {
  const [date, setDate] = useState<Date | undefined>(new Date())
  const [startHour, setStartHour] = useState("18")
  const [startMinute, setStartMinute] = useState("00")
  const [endHour, setEndHour] = useState("20")
  const [endMinute, setEndMinute] = useState("00")
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = () => {
    if (!date) {
      setError("날짜를 선택해주세요.")
      return
    }

    // 시작 시간과 종료 시간 생성
    const startDate = new Date(date)
    startDate.setHours(Number.parseInt(startHour), Number.parseInt(startMinute), 0, 0)

    const endDate = new Date(date)
    endDate.setHours(Number.parseInt(endHour), Number.parseInt(endMinute), 0, 0)

    // 종료 시간이 시작 시간보다 이후인지 확인
    if (endDate <= startDate) {
      setError("종료 시간은 시작 시간보다 이후여야 합니다.")
      return
    }

    // ISO 문자열로 변환
    const startTime = startDate.toISOString()
    const endTime = endDate.toISOString()

    onSubmit({ startTime, endTime })
  }

  return (
    <div className="space-y-4 py-4">
      <div className="space-y-2">
        <div className="grid w-full gap-1.5">
          <label
            htmlFor="date"
            className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
          >
            날짜
          </label>
          <Popover>
            <PopoverTrigger asChild>
              <Button
                id="date"
                variant={"outline"}
                className={cn("w-full justify-start text-left font-normal", !date && "text-muted-foreground")}
              >
                <CalendarIcon className="mr-2 h-4 w-4" />
                {date ? formatKSTDateTime(date.toISOString()) : <span>날짜 선택</span>}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0">
              <Calendar mode="single" selected={date} onSelect={setDate} initialFocus />
            </PopoverContent>
          </Popover>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <label htmlFor="startTime" className="text-sm font-medium leading-none">
              시작 시간
            </label>
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <Select value={startHour} onValueChange={setStartHour}>
                <SelectTrigger className="w-[80px]">
                  <SelectValue placeholder="시" />
                </SelectTrigger>
                <SelectContent>
                  {Array.from({ length: 24 }, (_, i) => (
                    <SelectItem key={i} value={i.toString().padStart(2, "0")}>
                      {i.toString().padStart(2, "0")}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <span>:</span>
              <Select value={startMinute} onValueChange={setStartMinute}>
                <SelectTrigger className="w-[80px]">
                  <SelectValue placeholder="분" />
                </SelectTrigger>
                <SelectContent>
                  {["00", "15", "30", "45"].map((minute) => (
                    <SelectItem key={minute} value={minute}>
                      {minute}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <label htmlFor="endTime" className="text-sm font-medium leading-none">
              종료 시간
            </label>
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <Select value={endHour} onValueChange={setEndHour}>
                <SelectTrigger className="w-[80px]">
                  <SelectValue placeholder="시" />
                </SelectTrigger>
                <SelectContent>
                  {Array.from({ length: 24 }, (_, i) => (
                    <SelectItem key={i} value={i.toString().padStart(2, "0")}>
                      {i.toString().padStart(2, "0")}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <span>:</span>
              <Select value={endMinute} onValueChange={setEndMinute}>
                <SelectTrigger className="w-[80px]">
                  <SelectValue placeholder="분" />
                </SelectTrigger>
                <SelectContent>
                  {["00", "15", "30", "45"].map((minute) => (
                    <SelectItem key={minute} value={minute}>
                      {minute}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </div>
      </div>

      {error && <div className="text-sm text-destructive">{error}</div>}

      <DialogFooter>
        <Button type="button" onClick={handleSubmit}>
          일정 추가
        </Button>
      </DialogFooter>
    </div>
  )
}
