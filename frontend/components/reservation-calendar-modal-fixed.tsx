"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { useRouter } from "next/navigation"
import { Loader2, CalendarIcon, Clock, Ticket, ChevronRight, ChevronLeft } from "lucide-react"
import { format, parseISO } from "date-fns"
import { ko } from "date-fns/locale"
import { cn } from "@/lib/utils"
import { Badge } from "@/components/ui/badge"
import { CustomCalendar } from "@/components/custom-calendar"
import { createReservation, getPerformanceDetail } from "@/src/api/api"
import { Calendar } from "@/components/ui/calendar"

interface PerformanceSchedule {
  id: number
  startTime: string
  endTime: string
  remainingSeats: number
  isCanceled: boolean
}

interface Performance {
  id: number
  title: string
  price: number
  totalSeats: number
  venue: string
  description: string
  status: string
  fileUrl: string
  startDate: string
  endDate: string
  bookmarked: boolean
  schedules: PerformanceSchedule[]
}

interface ReservationCalendarModalFixedProps {
  isOpen: boolean
  onClose: () => void
  onDateSelect: (date: Date) => void
  availableDates?: Date[]
}

export function ReservationCalendarModalFixed({
  isOpen,
  onClose,
  onDateSelect,
  availableDates = []
}: ReservationCalendarModalFixedProps) {
  const [selectedDate, setSelectedDate] = useState<Date>()

  const handleDateSelect = (date: Date | undefined) => {
    if (date) {
      setSelectedDate(date)
    }
  }

  const handleConfirm = () => {
    if (selectedDate) {
      onDateSelect(selectedDate)
      onClose()
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>공연 날짜 선택</DialogTitle>
        </DialogHeader>
        <div className="py-4">
          <Calendar
            mode="single"
            selected={selectedDate}
            onSelect={handleDateSelect}
            disabled={(date) => {
              const today = new Date()
              today.setHours(0, 0, 0, 0)
              return date < today || !availableDates.some(
                availableDate => 
                  availableDate.getFullYear() === date.getFullYear() &&
                  availableDate.getMonth() === date.getMonth() &&
                  availableDate.getDate() === date.getDate()
              )
            }}
            locale={ko}
            className="rounded-md border"
          />
        </div>
        <div className="flex justify-end gap-2">
          <Button variant="outline" onClick={onClose}>
            취소
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedDate}>
            선택
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
