import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, CheckCircle, XCircle, MapPin, Calendar, Clock, Users, Ticket } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import { Separator } from "@/components/ui/separator"

export default function PerformanceDetailPage({ params }: { params: { performanceId: string } }) {
  // 실제 구현에서는 params.performanceId를 사용하여 API에서 데이터를 가져옵니다
  const performance = {
    id: params.performanceId,
    title: "2023 여름 재즈 페스티벌",
    manager: "김재즈",
    managerEmail: "kim@example.com",
    date: "2023-07-15",
    time: "18:00 - 22:00",
    location: "서울 올림픽 공원",
    address: "서울특별시 송파구 올림픽로 424",
    capacity: 500,
    price: 50000,
    description:
      "여름을 맞이하여 국내외 유명 재즈 아티스트들이 모여 특별한 공연을 선보입니다. 다양한 재즈 음악과 함께 특별한 여름 밤을 경험해보세요.",
    type: "신규 등록",
    status: "대기중",
    createdAt: "2023-05-10",
    categories: ["재즈", "페스티벌", "음악"],
    performers: ["김재즈 트리오", "서울 재즈 오케스트라", "재즈 보컬리스트 이소울", "외국인 게스트 아티스트"],
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-4">
        <Button variant="outline" size="icon" asChild>
          <Link href="/admin/approve/performances">
            <ArrowLeft className="h-4 w-4" />
            <span className="sr-only">뒤로 가기</span>
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{performance.title}</h1>
          <p className="text-muted-foreground">
            공연 ID: {performance.id} | 신청자: {performance.manager}
          </p>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card className="md:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>공연 정보</CardTitle>
                <CardDescription>공연 상세 정보 및 승인 상태</CardDescription>
              </div>
              <div className="flex items-center gap-2">
                <Badge variant={performance.type === "신규 등록" ? "default" : "secondary"}>{performance.type}</Badge>
                <Badge variant="outline">{performance.status}</Badge>
              </div>
            </div>
          </CardHeader>
          <CardContent className="grid gap-6 md:grid-cols-2">
            <div>
              <div className="relative aspect-video overflow-hidden rounded-lg">
                <Image src="/placeholder.svg" alt={performance.title} fill className="object-cover" />
              </div>
              <div className="mt-4 space-y-2">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span>{performance.date}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <span>{performance.time}</span>
                </div>
                <div className="flex items-center gap-2">
                  <MapPin className="h-4 w-4 text-muted-foreground" />
                  <span>{performance.location}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4 text-muted-foreground" />
                  <span>수용 인원: {performance.capacity}명</span>
                </div>
                <div className="flex items-center gap-2">
                  <Ticket className="h-4 w-4 text-muted-foreground" />
                  <span>티켓 가격: {performance.price.toLocaleString()}원</span>
                </div>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <h3 className="font-medium mb-2">공연 설명</h3>
                <p className="text-sm text-muted-foreground">{performance.description}</p>
              </div>
              <Separator />
              <div>
                <h3 className="font-medium mb-2">카테고리</h3>
                <div className="flex flex-wrap gap-2">
                  {performance.categories.map((category) => (
                    <Badge key={category} variant="secondary">
                      {category}
                    </Badge>
                  ))}
                </div>
              </div>
              <Separator />
              <div>
                <h3 className="font-medium mb-2">출연진</h3>
                <ul className="text-sm text-muted-foreground space-y-1">
                  {performance.performers.map((performer) => (
                    <li key={performer}>{performer}</li>
                  ))}
                </ul>
              </div>
              <Separator />
              <div>
                <h3 className="font-medium mb-2">신청자 정보</h3>
                <div className="text-sm space-y-1">
                  <p>
                    <span className="text-muted-foreground">이름:</span> {performance.manager}
                  </p>
                  <p>
                    <span className="text-muted-foreground">이메일:</span> {performance.managerEmail}
                  </p>
                  <p>
                    <span className="text-muted-foreground">신청일:</span> {performance.createdAt}
                  </p>
                </div>
              </div>
            </div>
          </CardContent>
          <CardFooter className="flex justify-between">
            <Button variant="outline" asChild>
              <Link href="/admin/approve/performances">취소</Link>
            </Button>
            <div className="flex gap-2">
              <Button variant="destructive">
                <XCircle className="mr-2 h-4 w-4" />
                거절
              </Button>
              <Button>
                <CheckCircle className="mr-2 h-4 w-4" />
                승인
              </Button>
            </div>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
