import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Search, Eye } from "lucide-react"
import Link from "next/link"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DatePicker } from "@/components/ui/date-picker"

export default function ReservationsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">결제 처리/확인</h1>
        <p className="text-muted-foreground">모든 예약 및 결제 내역을 확인하고 관리할 수 있습니다.</p>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
        <div className="relative flex-1 w-full sm:max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="예약번호 또는 사용자 검색..." className="pl-8 w-full" />
        </div>
        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="결제 상태" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 상태</SelectItem>
              <SelectItem value="completed">결제 완료</SelectItem>
              <SelectItem value="pending">결제 대기중</SelectItem>
              <SelectItem value="failed">결제 실패</SelectItem>
              <SelectItem value="refunded">환불됨</SelectItem>
            </SelectContent>
          </Select>
          <DatePicker />
          <Button variant="outline">검색</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>예약 목록</CardTitle>
          <CardDescription>총 120개의 예약이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>예약번호</TableHead>
                <TableHead>사용자</TableHead>
                <TableHead>공연명</TableHead>
                <TableHead>예약일</TableHead>
                <TableHead>금액</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                {
                  id: "R-2023-1001",
                  user: "김예약",
                  performance: "2023 여름 재즈 페스티벌",
                  date: "2023-05-10",
                  amount: 50000,
                  status: "결제 완료",
                },
                {
                  id: "R-2023-1002",
                  user: "이티켓",
                  performance: "클래식 오케스트라 공연",
                  date: "2023-05-11",
                  amount: 70000,
                  status: "결제 대기중",
                },
                {
                  id: "R-2023-1003",
                  user: "박관람",
                  performance: "인디 밴드 콘서트",
                  date: "2023-05-12",
                  amount: 35000,
                  status: "결제 완료",
                },
                {
                  id: "R-2023-1004",
                  user: "최공연",
                  performance: "국악 한마당",
                  date: "2023-05-13",
                  amount: 40000,
                  status: "결제 실패",
                },
                {
                  id: "R-2023-1005",
                  user: "정예매",
                  performance: "팝 콘서트",
                  date: "2023-05-14",
                  amount: 60000,
                  status: "결제 완료",
                },
              ].map((reservation) => (
                <TableRow key={reservation.id}>
                  <TableCell className="font-medium">{reservation.id}</TableCell>
                  <TableCell>{reservation.user}</TableCell>
                  <TableCell>{reservation.performance}</TableCell>
                  <TableCell>{reservation.date}</TableCell>
                  <TableCell>{reservation.amount.toLocaleString()}원</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        reservation.status === "결제 완료"
                          ? "success"
                          : reservation.status === "결제 대기중"
                            ? "outline"
                            : reservation.status === "결제 실패"
                              ? "destructive"
                              : "secondary"
                      }
                    >
                      {reservation.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button size="sm" variant="outline" className="h-8 w-8 p-0" asChild>
                      <Link href={`/admin/reservations/${reservation.id}`}>
                        <Eye className="h-4 w-4" />
                        <span className="sr-only">상세보기</span>
                      </Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">총 120개 중 1-5 표시</div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled>
              이전
            </Button>
            <Button variant="outline" size="sm">
              다음
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
