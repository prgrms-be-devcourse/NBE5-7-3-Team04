import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, MapPin, Eye } from "lucide-react"
import Link from "next/link"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

// 샘플 데이터
const reservations = [
  {
    id: "R-2023-1001",
    performance: {
      title: "2023 여름 재즈 페스티벌",
      date: "2023-07-15",
      time: "18:00 - 22:00",
      location: "서울 올림픽 공원",
    },
    reservedAt: "2023-05-10",
    quantity: 2,
    amount: 100000,
    status: "예매완료",
  },
  {
    id: "R-2023-1002",
    performance: {
      title: "클래식 오케스트라 공연",
      date: "2023-08-20",
      time: "19:30 - 21:30",
      location: "예술의전당",
    },
    reservedAt: "2023-05-11",
    quantity: 1,
    amount: 70000,
    status: "결제대기",
  },
  {
    id: "R-2023-1003",
    performance: {
      title: "인디 밴드 콘서트",
      date: "2023-06-30",
      time: "20:00 - 23:00",
      location: "홍대 라이브 클럽",
    },
    reservedAt: "2023-05-12",
    quantity: 3,
    amount: 105000,
    status: "예매완료",
  },
  {
    id: "R-2023-1004",
    performance: {
      title: "국악 한마당",
      date: "2023-09-10",
      time: "17:00 - 19:00",
      location: "국립국악원",
    },
    reservedAt: "2023-05-13",
    quantity: 2,
    amount: 80000,
    status: "취소완료",
  },
]

export default function ReservationsPage() {
  return (
    <div className="container">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold tracking-tight">예매 내역</h1>
          <p className="text-muted-foreground">예매한 공연 목록을 확인하고 관리할 수 있습니다.</p>
        </div>

        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div className="relative flex-1 w-full sm:max-w-sm">
            <Input type="search" placeholder="공연명 검색..." className="w-full" />
          </div>
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Select defaultValue="all">
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="예매 상태" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">모든 상태</SelectItem>
                <SelectItem value="completed">예매완료</SelectItem>
                <SelectItem value="pending">결제대기</SelectItem>
                <SelectItem value="canceled">취소완료</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="outline">검색</Button>
          </div>
        </div>

        <Tabs defaultValue="all" className="w-full">
          <TabsList>
            <TabsTrigger value="all">전체</TabsTrigger>
            <TabsTrigger value="upcoming">예정된 공연</TabsTrigger>
            <TabsTrigger value="past">지난 공연</TabsTrigger>
            <TabsTrigger value="canceled">취소된 공연</TabsTrigger>
          </TabsList>
          <TabsContent value="all" className="mt-6">
            <div className="grid gap-6">
              {reservations.map((reservation) => (
                <ReservationCard key={reservation.id} reservation={reservation} />
              ))}
            </div>
          </TabsContent>
          <TabsContent value="upcoming" className="mt-6">
            <div className="grid gap-6">
              {reservations
                .filter((r) => r.status !== "취소완료")
                .map((reservation) => (
                  <ReservationCard key={reservation.id} reservation={reservation} />
                ))}
            </div>
          </TabsContent>
          <TabsContent value="past" className="mt-6">
            <div className="grid gap-6">
              {/* 실제 구현에서는 날짜 비교 로직 필요 */}
              <div className="text-center py-8 text-muted-foreground">지난 공연이 없습니다.</div>
            </div>
          </TabsContent>
          <TabsContent value="canceled" className="mt-6">
            <div className="grid gap-6">
              {reservations
                .filter((r) => r.status === "취소완료")
                .map((reservation) => (
                  <ReservationCard key={reservation.id} reservation={reservation} />
                ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  )
}

interface ReservationCardProps {
  reservation: (typeof reservations)[0]
}

function ReservationCard({ reservation }: ReservationCardProps) {
  const statusVariant =
    reservation.status === "예매완료" ? "success" : reservation.status === "결제대기" ? "secondary" : "destructive"

  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg">{reservation.performance.title}</CardTitle>
            <CardDescription>예약번호: {reservation.id}</CardDescription>
          </div>
          <Badge variant={statusVariant}>{reservation.status}</Badge>
        </div>
      </CardHeader>
      <CardContent className="pb-2">
        <div className="grid gap-2 text-sm">
          <div className="flex items-center gap-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <span>{reservation.performance.date}</span>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-muted-foreground" />
            <span>{reservation.performance.time}</span>
          </div>
          <div className="flex items-center gap-2">
            <MapPin className="h-4 w-4 text-muted-foreground" />
            <span>{reservation.performance.location}</span>
          </div>
          <div className="mt-2 flex items-center justify-between">
            <span className="text-muted-foreground">
              {reservation.quantity}매 | {reservation.reservedAt} 예매
            </span>
            <span className="font-bold">{reservation.amount.toLocaleString()}원</span>
          </div>
        </div>
      </CardContent>
      <CardFooter className="flex justify-end gap-2">
        {reservation.status !== "취소완료" && (
          <Button variant="outline" size="sm" asChild>
            <Link href={`/users/mypage/reservations/${reservation.id}`}>
              <Eye className="mr-2 h-4 w-4" />
              상세보기
            </Link>
          </Button>
        )}
      </CardFooter>
    </Card>
  )
}
