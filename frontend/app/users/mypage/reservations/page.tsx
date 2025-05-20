"use client";

import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { getReservations } from "../../../../src/api/reservation";
import type {
  ReservationResponse,
  ReservationPageResponse,
} from "../../../../src/types/reservation";
import { useRouter } from "next/navigation";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { formatKSTDateTime } from "@/src/api/utils/date";

export default function ReservationsPage() {
  const [reservations, setReservations] = useState<ReservationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const router = useRouter();

  const fetchReservations = async (pageNum: number = 0) => {
    try {
      setLoading(true);
      const data = await getReservations({ page: pageNum, size: 10 });
      console.log("API Response:", data); // API 응답 확인
      if (data && data.content) {
        setReservations(data.content);
        setTotalPages(
          Math.max(1, data.totalPages || Math.ceil(data.totalElements / 10))
        );
        setPage(pageNum);
      }
    } catch (err) {
      console.error("Error fetching reservations:", err);
      setError("예약 목록을 불러오는 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations(0);
  }, []);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PAYMENTS_PENDING":
        return (
          <Badge
            variant="outline"
            className="bg-yellow-50 text-yellow-700 border-yellow-200"
          >
            결제 대기
          </Badge>
        );
      case "PAYMENTS_CONFIRMED":
        return (
          <Badge
            variant="outline"
            className="bg-green-50 text-green-700 border-green-200"
          >
            결제 완료
          </Badge>
        );
      case "CANCEL_PENDING":
        return (
          <Badge
            variant="outline"
            className="bg-yellow-50 text-yellow-700 border-yellow-200"
          >
            취소 대기
          </Badge>
        );
      case "CANCEL_CONFIRMED":
        return (
          <Badge
            variant="outline"
            className="bg-red-50 text-red-700 border-red-200"
          >
            취소 확정
          </Badge>
        );
      default:
        return (
          <Badge
            variant="outline"
            className="bg-gray-50 text-gray-700 border-gray-200"
          >
            {status}
          </Badge>
        );
    }
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchReservations(newPage);
      window.scrollTo(0, 0);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">예약 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="container py-8">
      <h1 className="text-3xl font-bold mb-8">내 예약 목록</h1>
      {reservations.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <p className="text-muted-foreground mb-4">
              아직 예약한 공연이 없습니다.
            </p>
            <Button onClick={() => router.push("/performances")}>
              공연 둘러보기
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[100px] text-center">
                      예약 번호
                    </TableHead>
                    <TableHead className="text-center w-[200px]">
                      공연명
                    </TableHead>
                    <TableHead className="text-center w-[150px]">
                      공연장
                    </TableHead>
                    <TableHead className="text-center">예약일시</TableHead>
                    <TableHead className="text-center">수량</TableHead>
                    <TableHead className="text-center">티켓 가격</TableHead>
                    <TableHead className="text-center">총 결제 금액</TableHead>
                    <TableHead className="text-center">상태</TableHead>
                    <TableHead className="w-[120px] text-center"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {reservations.map((reservation) => (
                    <TableRow key={reservation.reservationId}>
                      <TableCell className="font-medium text-center">
                        {reservation.reservationId}
                      </TableCell>
                      <TableCell
                        className="font-medium text-center truncate max-w-[200px]"
                        title={reservation.title}
                      >
                        {reservation.title}
                      </TableCell>
                      <TableCell
                        className="text-center truncate max-w-[150px]"
                        title={reservation.venue}
                      >
                        {reservation.venue}
                      </TableCell>
                      <TableCell className="text-center">
                        {formatKSTDateTime(reservation.createdAt)}
                      </TableCell>
                      <TableCell className="text-center">
                        {reservation.quantity}매
                      </TableCell>
                      <TableCell className="text-center">
                        {typeof reservation.ticketPrice === "number" &&
                        !isNaN(reservation.ticketPrice)
                          ? `${reservation.ticketPrice.toLocaleString()}원`
                          : "-"}
                      </TableCell>
                      <TableCell className="text-center">
                        {typeof reservation.totalPrice === "number" &&
                        !isNaN(reservation.totalPrice)
                          ? `${reservation.totalPrice.toLocaleString()}원`
                          : "-"}
                      </TableCell>
                      <TableCell className="text-center">
                        {getStatusBadge(reservation.status)}
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() =>
                              router.push(
                                `/users/mypage/reservations/${reservation.reservationId}`
                              )
                            }
                          >
                            상세보기
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <div className="flex justify-center mt-8 gap-2">
            <Button
              variant="outline"
              onClick={() => handlePageChange(page - 1)}
              disabled={page === 0}
              className="h-10 w-10 p-0"
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            {Array.from({ length: totalPages }, (_, i) => (
              <Button
                key={i}
                variant={i === page ? "default" : "outline"}
                onClick={() => handlePageChange(i)}
                className={`h-10 w-10 p-0 ${
                  i === page ? "bg-purple-600 hover:bg-purple-700" : ""
                }`}
              >
                {i + 1}
              </Button>
            ))}
            <Button
              variant="outline"
              onClick={() => handlePageChange(page + 1)}
              disabled={page === totalPages - 1}
              className="h-10 w-10 p-0"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
