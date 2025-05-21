"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  getReservationDetail,
  cancelReservation,
  getRefundByReservationId,
  updateRefundBankInfo,
} from "@/src/api/api";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Calendar,
  Clock,
  MapPin,
  ArrowLeft,
  AlertTriangle,
  Ticket,
} from "lucide-react";
import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import { CancelReservationDialog } from "@/components/cancel-reservation-dialog";
import { getPerformanceImageUrl } from "@/lib/utils";
import { formatKSTDateTime } from "@/src/api/utils/date";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";

export default function ReservationDetailPage() {
  const params = useParams();
  const router = useRouter();
  const reservationId = params?.reservationId as string;
  const [reservation, setReservation] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refundInfo, setRefundInfo] = useState<any>(null);
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundError, setRefundError] = useState<string | null>(null);
  const [bankName, setBankName] = useState("");
  const [accountNumber, setAccountNumber] = useState("");
  const [accountHolder, setAccountHolder] = useState("");
  const [isEditingRefund, setIsEditingRefund] = useState(false);
  const [isRegisteringRefund, setIsRegisteringRefund] = useState(false);
  const { toast } = useToast();

  useEffect(() => {
    if (!reservationId) return;
    setLoading(true);
    getReservationDetail(reservationId)
      .then((data) => {
        console.log("reservation detail:", JSON.stringify(data, null, 2));
        setReservation(data);
      })
      .catch(() => setError("예매 정보를 불러오지 못했습니다."))
      .finally(() => setLoading(false));

    // 예약 상태가 취소 관련일 경우 환불 정보 조회
    if (
      reservation?.status === "CANCEL_PENDING"
    ) {
      setRefundLoading(true);
      getRefundByReservationId(reservationId)
        .then((data) => {
          setRefundInfo(data);
          if (data) {
            // 한글 은행명을 약어로 변환하는 매핑
            const bankNameMap: { [key: string]: string } = {
              "신한은행": "shinhan",
              "국민은행": "kb",
              "우리은행": "woori",
              "하나은행": "hana",
              "기업은행": "ibk",
              "토스뱅크": "toss",
            };
            
            // API에서 받은 한글 은행명을 약어로 변환하여 상태 설정
            const englishBankName = bankNameMap[data.bank] || "";
            setBankName(englishBankName);
            if (!data.bank && !data.account && !data.depositorName) {
              setIsRegisteringRefund(true);
            } else {
              setIsRegisteringRefund(false);
            }
            setAccountNumber(data.account || "");
            setAccountHolder(data.depositorName || "");
            // 필드가 모두 채워져 있으면 수정 모드 아님 (입력 불가)
            if (data.bank && data.account && data.depositorName) {
              setIsEditingRefund(false);
            } else {
              setIsEditingRefund(true); // 필드가 비어있으면 등록 모드 (입력 가능)
            }
          } else {
            setIsEditingRefund(true); // 데이터 없으면 등록 모드
          }
        })
        .catch((err) => {
          console.error("Error fetching refund info:", err);
          setRefundError("환불 정보를 불러오지 못했습니다.");
          setIsEditingRefund(true); // 오류 발생 시 등록 모드 (입력 가능)
        })
        .finally(() => setRefundLoading(false));
    }
  }, [reservationId, reservation?.status]); // reservation.status를 의존성 배열에 추가

  if (loading || refundLoading)
    return <div className="p-8 text-center">로딩 중...</div>;
  if (error || !reservation)
    return (
      <div className="p-8 text-center text-red-500">
        {error || "예매 정보가 없습니다."}
      </div>
    );

  const statusVariant =
    reservation.status === "PAYMENTS_CONFIRMED"
      ? "success"
      : reservation.status === "PAYMENTS_PENDING"
      ? "secondary"
      : "destructive";

  // 상태 한글 변환 함수
  const getStatusLabel = (status: string) => {
    switch (status) {
      case "PAYMENTS_PENDING":
        return "예약 확정 대기";
      case "PAYMENTS_CONFIRMED":
        return "예약 확정";
      case "CANCEL_PENDING":
        return "취소 대기";
      case "CANCEL_CONFIRMED":
        return "취소 확정";
      default:
        return status;
    }
  };

  // 취소 버튼 비활성화 조건
  const isCancelDisabled =
    reservation.status === "CANCEL_PENDING" ||
    reservation.status === "CANCEL_CONFIRMED";

  // 환불 정보 필드 값 모두 채워져 있는지 확인
  const areRefundFieldsFilled = bankName && accountNumber && accountHolder;

  return (
    <div className="container">
      <div className="flex flex-col gap-6">
        <div className="flex items-center gap-4">
          <Button variant="outline" size="icon" asChild>
            <Link href="/users/mypage/reservations">
              <ArrowLeft className="h-4 w-4" />
              <span className="sr-only">뒤로 가기</span>
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">
              예매 상세 정보
            </h1>
            <p className="text-muted-foreground">
              예약번호: {reservation.reservationId} |{" "}
              {formatKSTDateTime(reservation.createdAt)} 예매
            </p>
          </div>
        </div>

        <div className="grid gap-6 md:grid-cols-2 items-stretch">
          {/* 공연 정보(설명) Card - 빨간 테두리 */}
          <Card className="h-full">
            <CardHeader>
              {/* 이미지와 정보 컨테이너 */}
              <div className="flex flex-col md:flex-row gap-4 h-full">
                {/* 공연 이미지 (왼쪽) */}
                <div className="w-full md:w-1/2 flex-shrink-0">
                  <img
                    src={getPerformanceImageUrl(reservation.fileUrl)}
                    alt={reservation.title}
                    className="w-full h-full object-cover rounded-md"
                  />
                </div>
                {/* 공연 정보 텍스트 (오른쪽) */}
                <div className="flex-1 flex flex-col gap-2">
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle>
                        <a
                          href={`/performances/${reservation.performanceId}`}
                          className="hover:underline text-lg text-blue-700 font-bold"
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          {reservation.title}
                        </a>
                      </CardTitle>
                      <CardDescription>예매한 공연 정보</CardDescription>
                    </div>
                    <Badge variant={statusVariant}>
                      {getStatusLabel(reservation.status)}
                    </Badge>
                  </div>
                  {reservation.description && (
                    <div
                      className="text-gray-700 text-sm truncate"
                      style={{ maxWidth: "100%" }}
                      title={reservation.description}
                    >
                      {reservation.description}
                    </div>
                  )}
                  <div className="grid gap-2 text-sm">
                    {/* 회차 시작/ 종료 시간 */}
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <span>
                        {reservation.startTime
                          ? `시작: ${formatKSTDateTime(reservation.startTime)}`
                          : ""}
                        {reservation.endTime
                          ? ` ~ 종료: ${formatKSTDateTime(reservation.endTime)}`
                          : ""}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <div>{reservation.venue}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {/* Description, Times, Venue moved to CardHeader flex container */}
            </CardContent>
          </Card>

          <div className="flex flex-col gap-6 h-full">
            <Card>
              <CardHeader>
                <CardTitle>결제 정보</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-2 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">티켓 가격</span>
                    <span>
                      {reservation.ticketPrice?.toLocaleString()}원 x{" "}
                      {reservation.quantity}매
                    </span>
                  </div>
                  <div className="flex items-center justify-between font-bold">
                    <span>총 결제 금액</span>
                    <span>{reservation.totalPrice?.toLocaleString()}원</span>
                  </div>
                </div>
                {/* 결제 방법 등 추가 */}
              </CardContent>
            </Card>

            <Card className="h-full">
              <CardHeader>
                <CardTitle>환불 정책</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex items-center gap-2 text-amber-500">
                    <AlertTriangle className="h-4 w-4" />
                    <span className="text-sm font-medium">
                      취소 시 아래 정책에 따라 환불됩니다.
                    </span>
                  </div>
                  <ul className="list-inside list-disc text-sm text-muted-foreground">
                    <li>공연 7일 전까지: 전액 환불</li>
                    <li>공연 3일 전까지: 70% 환불</li>
                    <li>공연 1일 전까지: 50% 환불</li>
                    <li>공연 당일: 환불 불가</li>
                  </ul>
                </div>
              </CardContent>
              <CardFooter className="flex justify-end">
                {reservation.status === "PAYMENTS_PENDING" ? (
                  <Button
                    variant="destructive"
                    disabled={isCancelDisabled}
                    onClick={async () => {
                      if (window.confirm("정말 예매를 취소하시겠습니까?")) {
                        try {
                          await cancelReservation(reservation.reservationId);
                          window.location.reload();
                        } catch (e) {
                          alert(
                            "예매 취소 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                          );
                        }
                      }
                    }}
                  >
                    예매 취소
                  </Button>
                ) : (
                  <CancelReservationDialog
                    reservationId={reservation.reservationId}
                    disabled={isCancelDisabled}
                    refundId={refundInfo?.refundId}
                  />
                )}
              </CardFooter>
            </Card>
          </div>
        </div>
        {/* 환불 정보 Card (취소 상태일 때만 표시) */}
        {(reservation.status === "CANCEL_PENDING") && (
          <div className="mt-6">
            {refundError && (
              <div className="p-4 text-red-500">{refundError}</div>
            )}
            {!refundLoading && !refundError && (
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle className="text-base">환불 정보</CardTitle>
                  {/* 수정 버튼 (정보가 있고, 수정 모드가 아닐 때) */}
                  {areRefundFieldsFilled && !isEditingRefund && !isRegisteringRefund &&(
                    <Button
                      variant="outline"
                      onClick={() => setIsEditingRefund(true)}
                    >
                      정보 수정
                    </Button>
                  )}
                  {/* 수정 취소 버튼 (수정 모드일 때) */}
                  {areRefundFieldsFilled && isEditingRefund && !isRegisteringRefund &&(
                    <Button
                      variant="outline"
                      onClick={() => setIsEditingRefund(false)}
                    >
                      수정 취소
                    </Button>
                  )}
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                      <Label htmlFor="refund-bank">은행명</Label>
                      <Select
                        value={bankName}
                        onValueChange={setBankName}
                        disabled={!isEditingRefund}
                        required={isEditingRefund}
                      >
                        <SelectTrigger id="refund-bank">
                          <SelectValue placeholder="은행 선택" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="shinhan">신한은행</SelectItem>
                          <SelectItem value="kb">국민은행</SelectItem>
                          <SelectItem value="woori">우리은행</SelectItem>
                          <SelectItem value="hana">하나은행</SelectItem>
                          <SelectItem value="ibk">기업은행</SelectItem>
                          <SelectItem value="toss">토스뱅크</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="refund-account">계좌번호</Label>
                      <Input
                        id="refund-account"
                        value={accountNumber}
                        onChange={(e) => setAccountNumber(e.target.value)}
                        placeholder="'-' 없이 입력해주세요"
                        disabled={!isEditingRefund}
                        required={isEditingRefund}
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="refund-holder">예금주</Label>
                      <Input
                        id="refund-holder"
                        value={accountHolder}
                        onChange={(e) => setAccountHolder(e.target.value)}
                        disabled={!isEditingRefund}
                        required={isEditingRefund}
                      />
                    </div>
                  </div>
                </CardContent>
                <CardFooter className="flex justify-end">
                  {/* 등록 또는 수정 완료 버튼 (수정 모드일 때) */}
                  {isEditingRefund && (
                    <Button
                      onClick={async () => {
                        // 등록 또는 수정 완료 API 호출
                        if (!bankName || !accountNumber || !accountHolder) {
                          toast({
                            title: "입력 오류",
                            description: "은행명, 계좌번호, 예금주를 모두 입력해주세요.",
                            variant: "destructive",
                          });
                          return;
                        }
                        try {
                          const bankNameReverseMap: { [key: string]: string } = {
                            shinhan: "신한은행",
                            kb: "국민은행",
                            woori: "우리은행",
                            hana: "하나은행",
                            ibk: "기업은행",
                            toss: "토스뱅크",
                          };

                          await updateRefundBankInfo({
                            refundId: refundInfo?.refundId,
                            bank: bankNameReverseMap[bankName],
                            account: accountNumber,
                            depositorName: accountHolder,
                          });
                          toast({
                            title: "성공",
                            description: "환불 정보가 저장되었습니다.",
                          });
                          setIsEditingRefund(false); // 수정 완료 후 보기 모드로 전환
                          if(isRegisteringRefund) {
                            window.location.reload();
                          } else {
                            const updatedData = await getRefundByReservationId(reservationId);
                            // 상태 세팅
                            setRefundInfo(updatedData);
                          }
                        } catch (e) {
                          toast({
                            title: "저장 오류",
                            description: "환불 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            variant: "destructive",
                          });
                        }
                      }}
                    >
                      {isRegisteringRefund ? "등록하기" : "수정 완료"}
                    </Button>
                  )}
                </CardFooter>
              </Card>
            )}
          </div>
        )}
        {/* 티켓 정보 Card */}
        <div className="mt-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">티켓 정보</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-row gap-3 overflow-x-auto">
                {reservation.ticketNumbers?.map(
                  (ticketNumber: string, idx: number) => (
                    <div
                      key={ticketNumber}
                      className="flex items-center gap-2 rounded-md border px-4 py-3 min-w-[160px] bg-gray-50 shadow-sm"
                    >
                      <Ticket className="h-5 w-5 text-muted-foreground" />
                      <span className="font-medium">
                        티켓 번호: {ticketNumber}
                      </span>
                    </div>
                  )
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
