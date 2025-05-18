"use client"

import * as React from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import {
  format,
  addMonths,
  subMonths,
  startOfMonth,
  endOfMonth,
  eachDayOfInterval,
  isSameMonth,
  isSameDay,
} from "date-fns"
import { ko } from "date-fns/locale"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

interface CustomCalendarProps {
  selectedDate?: Date
  onSelect?: (date: Date | undefined) => void
  availableDates?: Date[]
  className?: string
}

export function CustomCalendar({ selectedDate, onSelect, availableDates = [], className }: CustomCalendarProps) {
  const [currentMonth, setCurrentMonth] = React.useState(new Date())

  // 현재 월의 모든 날짜 가져오기
  const getDaysInMonth = () => {
    const start = startOfMonth(currentMonth)
    const end = endOfMonth(currentMonth)
    return eachDayOfInterval({ start, end })
  }

  // 달력에 표시할 날짜 배열 생성 (이전 달, 현재 달, 다음 달 포함)
  const getCalendarDays = () => {
    const days = getDaysInMonth()
    const firstDay = days[0].getDay()

    // 이전 달의 날짜 추가
    const prevMonthDays = []
    if (firstDay > 0) {
      const prevMonth = subMonths(currentMonth, 1)
      const prevMonthLastDay = endOfMonth(prevMonth).getDate()
      for (let i = firstDay - 1; i >= 0; i--) {
        const date = new Date(prevMonth)
        date.setDate(prevMonthLastDay - i)
        prevMonthDays.push(date)
      }
    }

    // 다음 달의 날짜 추가
    const nextMonthDays = []
    const lastDay = days[days.length - 1].getDay()
    if (lastDay < 6) {
      const nextMonth = addMonths(currentMonth, 1)
      for (let i = 1; i <= 6 - lastDay; i++) {
        const date = new Date(nextMonth)
        date.setDate(i)
        nextMonthDays.push(date)
      }
    }

    return [...prevMonthDays, ...days, ...nextMonthDays]
  }

  const calendarDays = getCalendarDays()

  const nextMonth = () => {
    setCurrentMonth(addMonths(currentMonth, 1))
  }

  const prevMonth = () => {
    setCurrentMonth(subMonths(currentMonth, 1))
  }

  const isDateAvailable = (date: Date) => {
    return availableDates.some((availableDate) => format(availableDate, "yyyy-MM-dd") === format(date, "yyyy-MM-dd"))
  }

  return (
    <div className={cn("p-3 w-full max-w-sm mx-auto", className)}>
      <div className="space-y-4">
        {/* 캘린더 헤더 */}
        <div className="flex items-center justify-between">
          <Button variant="outline" size="icon" className="h-7 w-7" onClick={prevMonth}>
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="text-sm font-medium">{format(currentMonth, "yyyy년 MM월", { locale: ko })}</div>
          <Button variant="outline" size="icon" className="h-7 w-7" onClick={nextMonth}>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
        <div style={{height: '20px'}} />
        {/* 요일 헤더 */}
        <div className="grid grid-cols-7 gap-1 text-center">
          {["일", "월", "화", "수", "목", "금", "토"].map((day) => (
            <div key={day} className="text-xs font-medium text-muted-foreground h-8 flex items-center justify-center">
              {day}
            </div>
          ))}
        </div>

        {/* 캘린더 본체 */}
        <div className="grid grid-cols-7 gap-1">
          {calendarDays.map((date, i) => {
            const isAvailable = isDateAvailable(date)
            const isSelected = selectedDate && isSameDay(selectedDate, date)
            const isCurrentMonthDay = isSameMonth(date, currentMonth)

            return (
              <Button
                key={i}
                variant={isSelected ? "default" : "ghost"}
                size="icon"
                className={cn(
                  "h-8 w-8 p-0 font-normal rounded-full",
                  !isCurrentMonthDay && "text-muted-foreground opacity-50",
                  isAvailable && !isSelected && isCurrentMonthDay && "bg-green-50 text-green-600 font-medium",
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
  )
}
