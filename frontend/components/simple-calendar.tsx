"use client"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { DayPicker } from "react-day-picker"
import { ko } from "date-fns/locale"
import { format } from "date-fns"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

interface SimpleCalendarProps {
  selectedDate?: Date
  onSelect?: (date: Date | undefined) => void
  availableDates?: Date[]
  className?: string
}

export function SimpleCalendar({ selectedDate, onSelect, availableDates = [], className }: SimpleCalendarProps) {
  return (
    <div className={cn("p-3 w-full max-w-sm mx-auto", className)}>
      <div className="space-y-4">
        {/* 캘린더 헤더 */}
        <div className="flex items-center justify-between">
          <Button
            variant="outline"
            size="icon"
            className="h-7 w-7"
            onClick={() => {
              const dayPicker = document.querySelector(".rdp") as any
              if (dayPicker && dayPicker.components && dayPicker.components.IconLeft) {
                dayPicker.components.IconLeft.props.onClick()
              }
            }}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="text-sm font-medium">
            {selectedDate ? format(selectedDate, "yyyy년 MM월", { locale: ko }) : "날짜 선택"}
          </div>
          <Button
            variant="outline"
            size="icon"
            className="h-7 w-7"
            onClick={() => {
              const dayPicker = document.querySelector(".rdp") as any
              if (dayPicker && dayPicker.components && dayPicker.components.IconRight) {
                dayPicker.components.IconRight.props.onClick()
              }
            }}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>

        {/* 요일 헤더 */}
        <div className="grid grid-cols-7 gap-1 text-center">
          {["일", "월", "화", "수", "목", "금", "토"].map((day) => (
            <div key={day} className="text-xs font-medium text-muted-foreground h-8 flex items-center justify-center">
              {day}
            </div>
          ))}
        </div>

        {/* 캘린더 본체 - 실제 DayPicker는 숨기고 스타일링된 버튼으로 대체 */}
        <div className="relative">
          <DayPicker
            mode="single"
            selected={selectedDate}
            onSelect={onSelect}
            locale={ko}
            className="absolute opacity-0 pointer-events-none"
            disabled={(date) => {
              return !availableDates.some(
                (availableDate) => format(availableDate, "yyyy-MM-dd") === format(date, "yyyy-MM-dd"),
              )
            }}
          />

          {/* 커스텀 캘린더 UI */}
          <div className="grid grid-cols-7 gap-1">
            {Array.from({ length: 35 }, (_, i) => {
              const date = new Date()
              date.setDate(1) // 현재 월의 1일로 설정
              date.setDate(date.getDate() + i - date.getDay()) // 첫 주 일요일부터 시작하도록 조정

              const isAvailable = availableDates.some(
                (availableDate) => format(availableDate, "yyyy-MM-dd") === format(date, "yyyy-MM-dd"),
              )
              const isSelected = selectedDate && format(selectedDate, "yyyy-MM-dd") === format(date, "yyyy-MM-dd")
              const isCurrentMonth = date.getMonth() === new Date().getMonth()

              return (
                <Button
                  key={i}
                  variant={isSelected ? "default" : "ghost"}
                  size="icon"
                  className={cn(
                    "h-8 w-8 p-0 font-normal rounded-full",
                    !isCurrentMonth && "text-muted-foreground opacity-50",
                    isAvailable && !isSelected && "bg-green-50 text-green-600 font-medium",
                    !isAvailable && "opacity-30 cursor-not-allowed",
                  )}
                  disabled={!isAvailable}
                  onClick={() => onSelect && onSelect(isAvailable ? date : undefined)}
                >
                  {date.getDate()}
                </Button>
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}
