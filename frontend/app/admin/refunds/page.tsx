import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Search, Eye } from "lucide-react"
import Link from "next/link"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DatePicker } from "@/components/ui/date-picker"

export default function RefundsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">환불 처리</h1>
        <p className="text-muted-foreground">환불 요청을 확인하고 처리할 수 있습니다.</p>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 flex-wrap">
        <div className="relative flex-1 w-full sm:max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="예약번호 또는 사용자 검색..." className="pl-8 w-full" />
        </div>
        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="환불 상태" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 상태</SelectItem>
              <SelectItem value="pending">환불 대기중</SelectItem>
              <SelectItem value="processing">처리중</SelectItem>
              <SelectItem value="completed">환불 완료</SelectItem>
              <SelectItem value="rejected">환불 거절</SelectItem>
            </SelectContent>
          </Select>
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="공연 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 공연</SelectItem>
              <SelectItem value="1">2023 여름 재즈 페스티벌</SelectItem>
              <SelectItem value="2">클래식 오케스트라 공연</SelectItem>
              <SelectItem value="3">인디 밴드 콘서트</SelectItem>
              <SelectItem value="4">국악 한마당</SelectItem>
              <SelectItem value="5">팝 콘서트</SelectItem>
            </SelectContent>
          </Select>
          <DatePicker />
          <Button variant="outline">검색</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>환불 요청 목록</CardTitle>
          <CardDescription>총 32개의 환불 요청이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>환불 ID</TableHead>
                <TableHead>예약번호</TableHead>
                <TableHead>사용자</TableHead>
                <TableHead>공연명</TableHead>
                <TableHead>요청일</TableHead>
                <TableHead>금액</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                {
                  id: "RF-2023-1001",
                  reservationId: "R-2023-2001",
                  user: "김환불",
                  performance: "2023 여름 재즈 페스티벌",
                  date: "2023-05-15",
                  amount: 50000,
                  status: "환불 대기중",
                },
                {
                  id: "RF-2023-1002",
                  reservationId: "R-2023-2002",
                  user: "이취소",
                  performance: "클래식 오케스트라 공연",
                  date: "2023-05-16",
                  amount: 70000,
                  status: "처리중",
                },
                {
                  id: "RF-2023-1003",
                  reservationId: "R-2023-2003",
                  user: "박환불",
                  performance: "인디 밴드 콘서트",
                  date: "2023-05-17",
                  amount: 35000,
                  status: "환불 완료",
                },
                {
                  id: "RF-2023-1004",
                  reservationId: "R-2023-2004",
                  user: "최취소",
                  performance: "국악 한마당",
                  date: "2023-05-18",
                  amount: 40000,
                  status: "환불 대기중",
                },
                {
                  id: "RF-2023-1005",
                  reservationId: "R-2023-2005",
                  user: "정환불",
                  performance: "팝 콘서트",
                  date: "2023-05-19",
                  amount: 60000,
                  status: "환불 거절",
                },
              ].map((refund) => (
                <TableRow key={refund.id}>
                  <TableCell className="font-medium">{refund.id}</TableCell>
                  <TableCell>{refund.reservationId}</TableCell>
                  <TableCell>{refund.user}</TableCell>
                  <TableCell>{refund.performance}</TableCell>
                  <TableCell>{refund.date}</TableCell>
                  <TableCell>{refund.amount.toLocaleString()}원</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        refund.status === "환불 완료"
                          ? "success"
                          : refund.status === "환불 대기중"
                            ? "outline"
                            : refund.status === "처리중"
                              ? "secondary"
                              : refund.status === "환불 거절"
                                ? "destructive"
                                : "default"
                      }
                    >
                      {refund.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button size="sm" variant="outline" className="h-8 w-8 p-0" asChild>
                      <Link href={`/admin/refunds/${refund.id}`}>
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
          <div className="text-sm text-muted-foreground">총 32개 중 1-5 표시</div>
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
