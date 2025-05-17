"use client";

import { useState, useEffect } from "react";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {
    getManagerPerformances,
    getManagerPerformanceDetails,
    registerPerformanceSchedule,
    cancelPerformanceSchedule,
} from "@/src/api/api";
import { format, parseISO } from "date-fns";
import { useAuth } from "@/src/auth/user";
import { useRouter } from "next/navigation";

export default function SchedulesPage() {
    const [performances, setPerformances] = useState<any[]>([]);
    const [selectedPerformanceId, setSelectedPerformanceId] =
        useState<string>("");
    const [selectedPerformance, setSelectedPerformance] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [detailLoading, setDetailLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const { isLoading: authLoading, userRole } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (authLoading) return;
        if (userRole !== "MANAGER") {
            router.push("/login");
            return;
        }
        const fetchPerformances = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await getManagerPerformances();
                setPerformances(data.content || []);
            } catch (err) {
                console.error("공연 목록 가져오기 오류:", err);
                setError("공연 목록을 불러오는 중 오류가 발생했습니다.");
            } finally {
                setLoading(false);
            }
        };
        fetchPerformances();
    }, [authLoading, userRole]);

    useEffect(() => {
        if (selectedPerformanceId) {
            const fetchPerformanceDetails = async () => {
                try {
                    setDetailLoading(true);
                    setError(null);
                    const data = await getManagerPerformanceDetails(
                        selectedPerformanceId
                    );
                    setSelectedPerformance(data);
                } catch (err) {
                    console.error("공연 상세 정보 가져오기 오류:", err);
                    setError(
                        "공연 상세 정보를 불러오는 중 오류가 발생했습니다."
                    );
                    setSelectedPerformance(null);
                } finally {
                    setDetailLoading(false);
                }
            };

            fetchPerformanceDetails();
        } else {
            setSelectedPerformance(null);
        }
    }, [selectedPerformanceId]);

    // 날짜 포맷팅 함수
    const formatDate = (dateString: string) => {
        try {
            return format(parseISO(dateString), "yyyy년 MM월 dd일 HH:mm");
        } catch (error) {
            return "날짜 정보 없음";
        }
    };

    return (
        <div className="container py-8">
            <div className="flex flex-col gap-6">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">
                        공연 일정
                    </h1>
                    <p className="text-muted-foreground mt-1">
                        등록된 공연의 일정을 확인합니다.
                    </p>
                </div>

                <Card>
                    <CardHeader>
                        <CardTitle>공연 선택</CardTitle>
                        <CardDescription>
                            일정을 확인할 공연을 선택하세요.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        {loading ? (
                            <div className="flex items-center gap-2">
                                로딩 중...
                            </div>
                        ) : (
                            <div>공연 리스트 표시 영역</div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
