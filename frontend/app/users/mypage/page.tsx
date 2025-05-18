import { Suspense } from "react"
import { Loader2, Mail, Phone, Calendar, User2 } from "lucide-react"
import { getUserProfile } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { format } from "date-fns"
import { ko } from "date-fns/locale"

async function UserProfileContent() {
  const user = await getUserProfile()

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">{user.nickname || user.name}님의 프로필</h2>
          <p className="text-muted-foreground">회원 정보를 확인하고 관리하세요.</p>
        </div>
        <Button variant="outline">정보 수정</Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>기본 정보</CardTitle>
            <CardDescription>회원 기본 정보</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <User2 className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">이름</p>
                <p className="font-medium">{user.name}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Mail className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">이메일</p>
                <p className="font-medium">{user.email}</p>
              </div>
            </div>

            {user.phoneNumber && (
              <div className="flex items-center gap-3">
                <Phone className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">전화번호</p>
                  <p className="font-medium">{user.phoneNumber}</p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>추가 정보</CardTitle>
            <CardDescription>회원 추가 정보</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {user.nickname && (
              <div className="flex items-center gap-3">
                <User2 className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">닉네임</p>
                  <p className="font-medium">{user.nickname}</p>
                </div>
              </div>
            )}

            {user.birthDate && (
              <div className="flex items-center gap-3">
                <Calendar className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">생년월일</p>
                  <p className="font-medium">{format(new Date(user.birthDate), "PPP", { locale: ko })}</p>
                </div>
              </div>
            )}

            {user.gender && (
              <div className="flex items-center gap-3">
                <User2 className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">성별</p>
                  <p className="font-medium">{user.gender === "MALE" ? "남성" : "여성"}</p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default function MyPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-40 items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      }
    >
      <UserProfileContent />
    </Suspense>
  )
}
