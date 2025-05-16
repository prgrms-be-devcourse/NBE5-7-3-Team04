import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Search, Eye } from "lucide-react"
import Link from "next/link"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

export default function PerformanceApprovalPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">공연 등록/수정 승인</h1>
        <p className="text-muted-foreground">공연 등록 또는 수정 승인을 기다리는 공연 목록입니다.</p>
      </div>

      <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
        <div className="relative flex-1 w-full sm:max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="공연명 검색..." className="pl-8 w-full" />
        </div>
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="상태 필터" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 상태</SelectItem>
              <SelectItem value="pending">승인 대기중</SelectItem>
              <SelectItem value="approved">승인됨</SelectItem>
              <SelectItem value="rejected">거절됨</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline">검색</Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>공연 승인 목록</CardTitle>
          <CardDescription>총 12개의 승인 요청이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>공연명</TableHead>
                <TableHead>신청자</TableHead>
                <TableHead>공연 날짜</TableHead>
                <TableHead>신청 유형</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                {
                  id: 1,
                  title: "2023 여름 재즈 페스티벌",
                  manager: "김재즈",
                  date: "2023-07-15",
                  type: "신규 등록",
                  status: "대기중",
                },
                {
                  id: 2,
                  title: "클래식 오케스트라 공연",
                  manager: "이클래식",
                  date: "2023-08-20",
                  type: "신규 등록",
                  status: "대기중",
                },
                {
                  id: 3,
                  title: "인디 밴드 콘서트",
                  manager: "박인디",
                  date: "2023-06-30",
                  type: "수정",
                  status: "대기중",
                },
                {
                  id: 4,
                  title: "국악 한마당",
                  manager: "최국악",
                  date: "2023-09-10",
                  type: "신규 등록",
                  status: "대기중",
                },
                { id: 5, title: "팝 콘서트", manager: "정팝", date: "2023-07-25", type: "수정", status: "대기중" },
              ].map((performance) => (
                <TableRow key={performance.id}>
                  <TableCell className="font-medium">{performance.title}</TableCell>
                  <TableCell>{performance.manager}</TableCell>
                  <TableCell>{performance.date}</TableCell>
                  <TableCell>
                    <Badge variant={performance.type === "신규 등록" ? "default" : "secondary"}>
                      {performance.type}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">{performance.status}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button size="sm" variant="outline" className="h-8 w-8 p-0" asChild>
                        <Link href={`/admin/approve/performances/${performance.id}`}>
                          <Eye className="h-4 w-4" />
                          <span className="sr-only">상세보기</span>
                        </Link>
                      </Button>
                      <Button size="sm" variant="outline" className="h-8 w-8 p-0">
                        <CheckCircle className="h-4 w-4 text-green-500" />
                        <span className="sr-only">승인</span>
                      </Button>
                      <Button size="sm" variant="outline" className="h-8 w-8 p-0">
                        <XCircle className="h-4 w-4 text-red-500" />
                        <span className="sr-only">거절</span>
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">총 12개 중 1-5 표시</div>
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
