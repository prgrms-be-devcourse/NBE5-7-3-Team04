"use client";

import type React from "react";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";
import { useRouter } from "next/navigation";
import { Loader2, AlertTriangle } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cancelReservation, updateRefundBankInfo } from "@/src/api/api";

interface CancelReservationDialogProps {
  reservationId: string;
  disabled?: boolean;
  open?: boolean;
  setOpen?: (open: boolean) => void;
  refundId?: number;
}

export function CancelReservationDialog({
  reservationId,
  disabled = false,
  open: controlledOpen,
  setOpen: setControlledOpen,
  refundId,
}: CancelReservationDialogProps) {
  const [uncontrolledOpen, setUncontrolledOpen] = useState(false);
  const open = controlledOpen !== undefined ? controlledOpen : uncontrolledOpen;
  const setOpen = setControlledOpen !== undefined ? setControlledOpen : setUncontrolledOpen;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [bankName, setBankName] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [accountHolder, setAccountHolder] = useState("");
  const { toast } = useToast();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      // 예매 취소 API 호출 및 응답 저장
      const cancelResponse = await cancelReservation(reservationId); // <-- 응답 저장

      // 응답에서 refundId 추출 (응답 데이터 구조에 따라 수정 필요)
      // 예를 들어 응답 데이터가 바로 { refundId: ..., ... } 형태라면:
      const obtainedRefundId = cancelResponse?.refundId; // <-- refundId 추출

      // 환불 정보 업데이트 API 호출 (refundId가 있고, 입력 필드가 비어있지 않을 때만)
      if (obtainedRefundId && (bankName || accountNumber || accountHolder)) {

        await updateRefundBankInfo({
          refundId: obtainedRefundId, // <-- 여기서 사용
          bank: bankName,
          account: accountNumber,
          depositorName: accountHolder,
        });
      } else {
      }

      toast({
        title: "예약이 취소되었습니다.",
        description: "환불은 영업일 기준 3-5일 내에 처리됩니다.",
      });

      setOpen(false);
      window.location.reload();
    } catch (error) {
      toast({
        title: "취소 중 오류가 발생했습니다.",
        description: "잠시 후 다시 시도해주세요.",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="destructive" disabled={disabled}>
          예매 취소
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>예매 취소</DialogTitle>
          <DialogDescription>
            예매를 취소하시겠습니까? 환불 정책에 따라 일부 금액만 환불될 수
            있습니다.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="grid gap-4 py-4">
            <div className="flex items-center gap-2 rounded-md bg-amber-50 p-3 text-amber-600">
              <AlertTriangle className="h-4 w-4" />
              <span className="text-xs">
                환불받을 계좌 정보를 입력해주세요.
              </span>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="bank">은행명</Label>
              <Select value={bankName} onValueChange={setBankName} required>
                <SelectTrigger id="bank">
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
            <div className="grid gap-2">
              <Label htmlFor="account">계좌번호</Label>
              <Input
                id="account"
                value={accountNumber}
                onChange={(e) => setAccountNumber(e.target.value)}
                placeholder="'-' 없이 입력해주세요"
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="holder">예금주</Label>
              <Input
                id="holder"
                value={accountHolder}
                onChange={(e) => setAccountHolder(e.target.value)}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
            >
              취소
            </Button>
            <Button type="submit" variant="destructive" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  처리 중...
                </>
              ) : (
                "예매 취소하기"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
