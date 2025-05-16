import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Search, Eye } from "lucide-react"
import Link from "next/link"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DatePicker } from "@/components/ui/date-picker"

export default function SettlementsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">정산 처리/확인</h1>
        <p className="text-muted-foreground">공연 관리자에게 지급할 정산 내역을 확인하고 처리할 수 있습니다.</p>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 flex-wrap">
        <div className="relative flex-1 w-full sm:max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="공연명 또는 관리자 검색..." className="pl-8 w-full" />
        </div>
        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="정산 상태" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 상태</SelectItem>
              <SelectItem value="pending">정산 대기중</SelectItem>
              <SelectItem value="processing">처리중</SelectItem>
              <SelectItem value="completed">정산 완료</SelectItem>
            </SelectContent>
          </Select>
          <DatePicker />
          <Button variant="outline">검색</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>정산 목록</CardTitle>
          <CardDescription>총 45개의 정산 내역이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>정산 ID</TableHead>
                <TableHead>공연명</TableHead>
                <TableHead>공연일</TableHead>
                <TableHead>관리자</TableHead>
                <TableHead>정산 금액</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                {
                  id: "ST-2023-1001",
                  performance: "2023 여름 재즈 페스티벌",
                  date: "2023-07-15",
                  manager: "김재즈",
                  amount: 4250000,
                  status: "정산 대기중",
                },
                {
                  id: "ST-2023-1002",
                  performance: "클래식 오케스트라 공연",
                  date: "2023-08-20",
                  manager: "이클래식",
                  amount: 3800000,
                  status: "처리중",
                },
                {
                  id: "ST-2023-1003",
                  performance: "인디 밴드 콘서트",
                  date: "2023-06-30",
                  manager: "박인디",
                  amount: 2100000,
                  status: "정산 완료",
                },
                {
                  id: "ST-2023-1004",
                  performance: "국악 한마당",
                  date: "2023-09-10",
                  manager: "최국악",
                  amount: 1850000,
                  status: "정산 대기중",
                },
                {
                  id: "ST-2023-1005",
                  performance: "팝 콘서트",
                  date: "2023-07-25",
                  manager: "정팝",
                  amount: 5200000,
                  status: "정산 완료",
                },
              ].map((settlement) => (
                <TableRow key={settlement.id}>
                  <TableCell className="font-medium">{settlement.id}</TableCell>
                  <TableCell>{settlement.performance}</TableCell>
                  <TableCell>{settlement.date}</TableCell>
                  <TableCell>{settlement.manager}</TableCell>
                  <TableCell>{settlement.amount.toLocaleString()}원</TableCell>
                  <TableCell>
                    <Badge
                      variant={
                        settlement.status === "정산 완료"
                          ? "success"
                          : settlement.status === "정산 대기중"
                            ? "outline"
                            : settlement.status === "처리중"
                              ? "secondary"
                              : "default"
                      }
                    >
                      {settlement.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button size="sm" variant="outline" className="h-8 w-8 p-0" asChild>
                      <Link href={`/admin/settlements/${settlement.id}`}>
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
          <div className="text-sm text-muted-foreground">총 45개 중 1-5 표시</div>
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
