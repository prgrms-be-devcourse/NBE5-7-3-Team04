import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { CheckCircle, XCircle, Search } from "lucide-react"

export default function RoleApprovalPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-2xl font-bold tracking-tight">공연관리자 권한 승인</h1>
        <p className="text-muted-foreground">
          공연관리자 권한을 신청한 사용자 목록입니다. 승인 또는 거절할 수 있습니다.
        </p>
      </div>

      <div className="flex items-center gap-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="사용자 검색..." className="pl-8" />
        </div>
        <Button variant="outline">검색</Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>권한 신청 목록</CardTitle>
          <CardDescription>총 8개의 신청이 있습니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>신청자</TableHead>
                <TableHead>이메일</TableHead>
                <TableHead>신청일</TableHead>
                <TableHead>상태</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[
                { id: 1, name: "김공연", email: "kim@example.com", date: "2023-05-12", status: "대기중" },
                { id: 2, name: "이벤트", email: "lee@example.com", date: "2023-05-13", status: "대기중" },
                { id: 3, name: "박페스티벌", email: "park@example.com", date: "2023-05-14", status: "대기중" },
                { id: 4, name: "최콘서트", email: "choi@example.com", date: "2023-05-15", status: "대기중" },
                { id: 5, name: "정뮤지컬", email: "jung@example.com", date: "2023-05-16", status: "대기중" },
              ].map((user) => (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">{user.name}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.date}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{user.status}</Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
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
          <div className="text-sm text-muted-foreground">총 5개 중 1-5 표시</div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled>
              이전
            </Button>
            <Button variant="outline" size="sm" disabled>
              다음
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  )
}
