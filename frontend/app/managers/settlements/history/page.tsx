"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getManagerSettlements } from "@/src/api/api"
import { Loader2, AlertCircle, CreditCard, Calendar } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { format, parseISO } from "date-fns"
import { useAuth } from "@/src/auth/user"
import { useRouter } from "next/navigation"
import { formatKSTDateTime } from "@/src/api/utils/date"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { editManagerSettlement } from "@/src/api/api-manager"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

export default function SettlementHistoryPage() {
  const [settlements, setSettlements] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const { isLoading: authLoading, userRole } = useAuth()
  const router = useRouter()
  const [editOpen, setEditOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<any>(null)
  const [editBank, setEditBank] = useState("")
  const [editAccount, setEditAccount] = useState("")
  const [editLoading, setEditLoading] = useState(false)

  useEffect(() => {
    if (authLoading) return;
    if (userRole !== "MANAGER") {
      router.push("/login")
      return;
    }
    const fetchSettlements = async () => {
      try {
        setLoading(true)
        setError(null)
        const data = await getManagerSettlements(page)
        setSettlements(data.content || [])
        setTotalPages(data.totalPages || 1)
      } catch (err) {
        console.error("정산 내역 가져오기 오류:", err)
        setError("정산 내역을 불러오는 중 오류가 발생했습니다.")
      } finally {
        setLoading(false)
      }
    }
    fetchSettlements()
  }, [authLoading, userRole, page])

  const handlePageChange = (newPage: number) => {
    setPage(newPage)
    window.scrollTo(0, 0)
  }

  const openEditDialog = (settlement: any) => {
    setEditTarget(settlement)
    setEditBank(settlement.bank)
    setEditAccount(settlement.account)
    setEditOpen(true)
  }

  const handleEditSave = async () => {
    if (!editTarget) return
    setEditLoading(true)
    try {
      await editManagerSettlement({
        settlementId: editTarget.settlementId,
        bank: editBank,
        account: editAccount
      })
      setEditOpen(false)
      const data = await getManagerSettlements(page)
      setSettlements(data.content || [])
    } catch (e: any) {
      alert(e.message || "수정에 실패했습니다.")
    } finally {
      setEditLoading(false)
    }
  }

  return (
    <div className="container py-8">
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">정산 내역</h1>
          <p className="text-muted-foreground" style={{marginTop: '10px'}}>신청한 정산 내역을 확인합니다.</p>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <span className="ml-2">정산 내역을 불러오는 중...</span>
          </div>
        ) : error ? (
          <div className="flex items-center gap-2 rounded-lg border p-4 text-sm text-destructive">
            <AlertCircle className="h-4 w-4" />
            <p>{error}</p>
          </div>
        ) : settlements.length === 0 ? (
          <Card>
            <CardHeader>
              <CardTitle>정산 내역이 없습니다</CardTitle>
              <CardDescription>아직 정산 신청 내역이 없습니다.</CardDescription>
            </CardHeader>
            <CardContent>
              <Button asChild>
                <a href="/managers/settlements/request">정산 신청하기</a>
              </Button>
            </CardContent>
          </Card>
        ) : (
          <>
            <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-4">
              {settlements.map((settlement) => (
                <Card key={settlement.settlementId} className="shadow-sm rounded-xl border border-gray-200">
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between">
                      <Badge variant={settlement.status === "CONFIRMED" ? "success" : "secondary"} className="px-3 py-1 text-xs">
                        {settlement.status === "CONFIRMED" ? "승인됨" : "대기중"}
                      </Badge>
                      <span className="text-xs text-muted-foreground">
                        {settlement.settledAt ? formatKSTDateTime(settlement.settledAt) : "처리 대기중"}
                      </span>
                    </div>
                    <div className="h-1" />
                    <CardTitle className="text-lg font-bold text-gray-900">{settlement.title}</CardTitle>
                    <CardDescription className="mt-1 text-xs text-gray-500">정산 ID: {settlement.settlementId}</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-5 pt-2 pb-4">
                    <div className="flex flex-col gap-3">
                      <div className="flex flex-col gap-1">
                        <div className="flex items-center gap-2">
                          <CreditCard className="h-4 w-4 text-black" />
                          <span className="text-sm font-medium text-black">정산 금액</span>
                        </div>
                        <span className="font-bold text-lg text-gray-900 ml-4">{settlement.totalAmount.toLocaleString()}원</span>
                      </div>
                      <div className="flex flex-col gap-1">
                        <div className="flex items-center gap-2">
                          <Calendar className="h-4 w-4 text-black" />
                          <span className="text-sm font-medium text-black">계좌 정보</span>
                        </div>
                        <span className="text-sm text-gray-800 break-all ml-4">{settlement.bank} {settlement.account}</span>
                      </div>
                    </div>
                    {settlement.status === "CONFIRMED" && (
                      <div className="mt-5 rounded-md bg-green-50 p-3 text-sm text-green-800 text-center">
                        정산이 완료되었습니다.
                      </div>
                    )}
                    {settlement.status === "PENDING" && (
                      <>
                        <div className="mt-5 rounded-md bg-yellow-50 p-3 text-sm text-yellow-800 text-center">
                          관리자 승인 대기 중입니다.
                        </div>
                        <Button className="mt-2 w-full" variant="outline" onClick={() => openEditDialog(settlement)}>
                          정산 정보 수정
                        </Button>
                      </>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center mt-8 gap-2">
                <Button variant="outline" onClick={() => handlePageChange(page - 1)} disabled={page === 0}>
                  이전
                </Button>
                {Array.from({ length: totalPages }, (_, i) => (
                  <Button key={i} variant={i === page ? "default" : "outline"} onClick={() => handlePageChange(i)}>
                    {i + 1}
                  </Button>
                ))}
                <Button variant="outline" onClick={() => handlePageChange(page + 1)} disabled={page === totalPages - 1}>
                  다음
                </Button>
              </div>
            )}
          </>
        )}
        <Dialog open={editOpen} onOpenChange={setEditOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>정산 정보 수정</DialogTitle>
              <DialogDescription>은행명과 계좌번호를 수정할 수 있습니다.</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">은행명</label>
                <Select value={editBank} onValueChange={setEditBank} required>
                  <SelectTrigger id="edit-bank">
                    <SelectValue placeholder="은행 선택" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="신한은행">신한은행</SelectItem>
                    <SelectItem value="국민은행">국민은행</SelectItem>
                    <SelectItem value="우리은행">우리은행</SelectItem>
                    <SelectItem value="하나은행">하나은행</SelectItem>
                    <SelectItem value="기업은행">기업은행</SelectItem>
                    <SelectItem value="토스뱅크">토스뱅크</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">계좌번호</label>
                <Input value={editAccount} onChange={e => setEditAccount(e.target.value)} placeholder="계좌번호" />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setEditOpen(false)} disabled={editLoading}>취소</Button>
              <Button onClick={handleEditSave} disabled={editLoading}>
                {editLoading ? "저장 중..." : "저장"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  )
}
