import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { InfoIcon } from "lucide-react"

export default function ManagerRegisterPage() {
  return (
    <div className="container max-w-2xl">
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-bold tracking-tight">공연 관리자 권한 신청</h1>
          <p className="text-muted-foreground">공연을 등록하고 관리하기 위한 공연 관리자 권한을 신청합니다.</p>
        </div>

        <Alert>
          <InfoIcon className="h-4 w-4" />
          <AlertTitle>신청 전 안내사항</AlertTitle>
          <AlertDescription>
            공연 관리자 권한은 실제 공연을 주최하거나 관리하는 개인 또는 단체에게 부여됩니다. 신청 후 관리자 검토를 거쳐
            승인됩니다. 승인까지 1-2일이 소요될 수 있습니다.
          </AlertDescription>
        </Alert>

        <Card>
          <CardHeader>
            <CardTitle>신청자 정보</CardTitle>
            <CardDescription>공연 관리자 권한 신청을 위한 정보를 입력해주세요.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-4">
              <div className="grid gap-2">
                <Label htmlFor="name">이름 (개인 또는 단체명)</Label>
                <Input id="name" placeholder="이름을 입력하세요" />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="phone">연락처</Label>
                <Input id="phone" placeholder="연락 가능한 전화번호를 입력하세요" />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="company">소속 (선택사항)</Label>
                <Input id="company" placeholder="소속 회사나 단체를 입력하세요" />
              </div>
            </div>

            <Separator />

            <div className="space-y-4">
              <div className="grid gap-2">
                <Label htmlFor="experience">공연 기획/운영 경험</Label>
                <Textarea id="experience" placeholder="이전 공연 기획 또는 운영 경험을 간략히 설명해주세요" rows={3} />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="reason">신청 사유</Label>
                <Textarea id="reason" placeholder="공연 관리자 권한이 필요한 이유를 설명해주세요" rows={3} />
              </div>
            </div>

            <Separator />

            <div className="space-y-4">
              <div className="grid gap-2">
                <Label htmlFor="bank">은행명</Label>
                <Input id="bank" placeholder="정산받을 은행명을 입력하세요" />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="account">계좌번호</Label>
                <Input id="account" placeholder="정산받을 계좌번호를 입력하세요" />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="holder">예금주</Label>
                <Input id="holder" placeholder="예금주명을 입력하세요" />
              </div>
            </div>
          </CardContent>
          <CardFooter>
            <Button className="w-full">신청하기</Button>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
