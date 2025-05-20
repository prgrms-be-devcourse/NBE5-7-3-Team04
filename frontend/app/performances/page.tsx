"use client";

import type React from "react";
import { useState, useEffect, useCallback } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { searchPerformances, getPerformances } from "@/src/api/api";
import {
    Loader2,
    Search,
    ChevronLeft,
    ChevronRight,
    MapPin,
    Calendar,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { getPerformanceImageUrl } from "@/lib/utils";
import type { PerformancePageResponse } from "../../src/types/performance";
import { format, parseISO, addHours } from "date-fns";

export default function PerformancesPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const initialSearchQuery = searchParams.get("search") || "";
    const initialPage = Number(searchParams.get("page")) || 0;

    const [searchQuery, setSearchQuery] = useState(initialSearchQuery);
    const [category, setCategory] = useState("all");
    const [performances, setPerformances] = useState<PerformancePageResponse[]>(
        []
    );
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(initialPage);
    const [totalPages, setTotalPages] = useState(1);
    const [debouncedSearchQuery, setDebouncedSearchQuery] =
        useState(initialSearchQuery);

    // 디바운스 처리
  useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearchQuery(searchQuery);
        }, 500); // 500ms 딜레이

        return () => clearTimeout(timer);
    }, [searchQuery]);

    // 검색 실행
    const executeSearch = useCallback(
        async (query: string, cat: string, pageNum: number) => {
    try {
                setLoading(true);
                setError(null);

      const params: any = {
                    page: pageNum,
        size: 12,
                };

                if (query) params.title = query;
                if (cat !== "all") params.category = cat;

                const data = await searchPerformances(params);

                setPerformances(data.content || []);
                setTotalPages(data.totalPages || 1);
                setPage(pageNum);

                // URL 업데이트
                const searchParams = new URLSearchParams();
                if (query) searchParams.set("search", query);
                if (cat !== "all") searchParams.set("category", cat);
                searchParams.set("page", pageNum.toString());
                router.replace(`/performances?${searchParams.toString()}`, { scroll: false });
    } catch (err) {
                console.error("검색 오류:", err);
                setError("검색 중 오류가 발생했습니다.");
    } finally {
                setLoading(false);
    }
        },
        [router]
    );

    // 초기 로딩 시 URL 파라미터 처리
    useEffect(() => {
        const search = searchParams.get("search");
        const categoryParam = searchParams.get("category");
        const pageParam = searchParams.get("page");

        if (search !== null && search !== searchQuery) setSearchQuery(search);
        if (categoryParam !== null && categoryParam !== category)
            setCategory(categoryParam);
        if (pageParam !== null) setPage(Number(pageParam));

        if (search || categoryParam) {
            executeSearch(
                search || "",
                categoryParam || "all",
                Number(pageParam) || 0
            );
        } else {
            fetchPerformances();
        }
    }, []); // 초기 로딩 시에만 실행

    // 디바운스된 검색어나 카테고리가 변경될 때 검색 실행
    useEffect(() => {
        const isInitialLoad = !debouncedSearchQuery && category === "all" && page === 0;
        if (isInitialLoad) return; // 초기 로딩 시에는 실행하지 않음

        if (debouncedSearchQuery || category !== "all") {
            executeSearch(debouncedSearchQuery, category, 0);
        } else {
            fetchPerformances();
        }
    }, [debouncedSearchQuery, category, executeSearch]);

    const handleSearch = async (e?: React.FormEvent) => {
        if (e) e.preventDefault();
        executeSearch(searchQuery, category, 0);
    };

  const handlePageChange = (newPage: number) => {
        if (debouncedSearchQuery || category !== "all") {
            executeSearch(debouncedSearchQuery, category, newPage);
        } else {
            fetchPerformances(newPage);
        }
        window.scrollTo(0, 0);
    };

    const fetchPerformances = async (pageNum: number = page) => {
        try {
            setLoading(true);
            setError(null);

            const data = await getPerformances(pageNum, 12);

            setPerformances(data.content || []);
            setTotalPages(data.totalPages || 1);
            setPage(pageNum);
        } catch (err) {
            console.error("공연 목록 가져오기 오류:", err);
            setError("공연 목록을 불러오는 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (category: PerformancePageResponse["category"]) => {
        switch (category) {
            case "SINGING":
                return (
                    <Badge
                        variant="outline"
                        className="bg-blue-50 text-blue-700 border-blue-200"
                    >
                        콘서트
                    </Badge>
                );
            case "DANCING":
                return (
                    <Badge
                        variant="outline"
                        className="bg-purple-50 text-purple-700 border-purple-200"
                    >
                        무용
                    </Badge>
                );
            case "OPERA":
                return (
                    <Badge
                        variant="outline"
                        className="bg-red-50 text-red-700 border-red-200"
                    >
                        오페라
                    </Badge>
                );
            default:
                return null;
        }
    };

  return (
        <div className="min-h-screen bg-gradient-to-b from-purple-50/50 to-white">
            <div className="container py-12 px-4 md:px-6">
                <div className="flex flex-col gap-8">
                    <div className="text-center space-y-2">
                        <h1 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-purple-600 to-indigo-600 bg-clip-text text-transparent">
                            공연 목록
                        </h1>
                        <p className="text-lg text-muted-foreground">
                            다양한 공연을 검색하고 예매하세요
                        </p>
        </div>

                    <Card className="border-none shadow-lg bg-white/80 backdrop-blur-sm">
          <CardHeader className="pb-3">
                            <CardTitle className="text-xl">검색</CardTitle>
                            <CardDescription>
                                공연명, 장소, 카테고리로 검색할 수 있습니다.
                            </CardDescription>
          </CardHeader>
          <CardContent>
                            <form
                                onSubmit={handleSearch}
                                className="flex flex-col sm:flex-row gap-4"
                            >
              <div className="relative flex-1">
                                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="공연명 검색..."
                                        className="pl-10 h-12 text-lg"
                  value={searchQuery}
                                        onChange={(e) =>
                                            setSearchQuery(e.target.value)
                                        }
                />
              </div>
                                <Select
                                    value={category}
                                    onValueChange={setCategory}
                                >
                                    <SelectTrigger className="w-[180px] h-12 text-lg">
                  <SelectValue placeholder="카테고리" />
                </SelectTrigger>
                <SelectContent>
                                        <SelectItem value="all">
                                            모든 카테고리
                                        </SelectItem>
                                        <SelectItem value="SINGING">
                                            콘서트
                                        </SelectItem>
                                        <SelectItem value="DANCING">
                                            무용
                                        </SelectItem>
                                        <SelectItem value="OPERA">
                                            오페라
                                        </SelectItem>
                </SelectContent>
              </Select>
                                <Button
                                    type="submit"
                                    className="h-12 px-8 text-lg bg-purple-600 hover:bg-purple-700"
                                >
                                    검색
                                </Button>
            </form>
          </CardContent>
        </Card>

        {loading ? (
          <div className="flex items-center justify-center py-12">
                            <Loader2 className="h-8 w-8 animate-spin text-purple-600" />
                            <span className="ml-2 text-lg">
                                공연 정보를 불러오는 중...
                            </span>
          </div>
        ) : error ? (
                        <div className="text-center py-12 text-red-500 text-lg">
                            {error}
                        </div>
        ) : performances.length === 0 ? (
                        <div className="text-center py-12 text-muted-foreground text-lg">
            검색 결과가 없습니다. 다른 검색어로 시도해보세요.
          </div>
        ) : (
          <>
                            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                                {performances.map((performance) => (
                                    <Card
                                        key={performance.id}
                                        className="group cursor-pointer hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border-none bg-white/80 backdrop-blur-sm"
                                        onClick={() =>
                                            router.push(
                                                `/performances/${performance.id}`
                                            )
                                        }
                                    >
                                        <div className="aspect-[3/4] relative overflow-hidden">
                                            <img
                                                src={getPerformanceImageUrl(
                                                    performance.fileUrl
                                                )}
                                                alt={performance.title}
                                                className="object-cover w-full h-full transition-transform duration-500 group-hover:scale-105"
                                            />
                                            <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                                            <div className="absolute top-4 left-4">
                                                {getStatusBadge(
                                                    performance.category
                                                )}
                                            </div>
                                            <div className="absolute bottom-0 left-0 right-0 p-4 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                                <h3 className="text-xl font-bold mb-2 line-clamp-2">
                                                    {performance.title}
                                                </h3>
                                                <div className="space-y-2 text-sm text-muted-foreground">
                                                    <div className="flex items-center">
                                                        <MapPin className="mr-1 h-3.5 w-3.5" />
                                                        <span className="text-white">
                                                            {performance.venue}
                                                        </span>
                                                    </div>
                                                    <div className="flex items-center">
                                                        <Calendar className="mr-1 h-3.5 w-3.5" />
                                                        <span className="text-white">
                                                            {format(addHours(parseISO(performance.startDate), 9), 'yyyy.MM.dd')}{" "}
                                                            ~{" "}
                                                            {format(addHours(parseISO(performance.endDate), 9), 'yyyy.MM.dd')}
                                                        </span>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <CardContent className="p-4">
                                            <div className="space-y-2">
                                                <h3 className="font-semibold line-clamp-1">
                                                    {performance.title}
                                                </h3>
                                                <div className="flex flex-col gap-1 text-sm">
                                                    <span>
                                                        {performance.venue}
                                                    </span>
                                                    <span className="text-muted-foreground">
                                                        {format(addHours(parseISO(performance.startDate), 9), 'yyyy.MM.dd')}{" "}
                                                        ~{" "}
                                                        {format(addHours(parseISO(performance.endDate), 9), 'yyyy.MM.dd')}
                                                    </span>
                                                </div>
                                            </div>
                                        </CardContent>
                                    </Card>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center mt-8 gap-2">
                                    <Button
                                        variant="outline"
                                        onClick={() =>
                                            handlePageChange(page - 1)
                                        }
                                        disabled={page === 0}
                                        className="h-10 w-10 p-0"
                                    >
                                        <ChevronLeft className="h-4 w-4" />
                </Button>
                                    {Array.from(
                                        { length: totalPages },
                                        (_, i) => (
                                            <Button
                                                key={i}
                                                variant={
                                                    i === page
                                                        ? "default"
                                                        : "outline"
                                                }
                                                onClick={() =>
                                                    handlePageChange(i)
                                                }
                                                className={`h-10 w-10 p-0 ${
                                                    i === page
                                                        ? "bg-purple-600 hover:bg-purple-700"
                                                        : ""
                                                }`}
                                            >
                    {i + 1}
                  </Button>
                                        )
                                    )}
                                    <Button
                                        variant="outline"
                                        onClick={() =>
                                            handlePageChange(page + 1)
                                        }
                                        disabled={page === totalPages - 1}
                                        className="h-10 w-10 p-0"
                                    >
                                        <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
        </div>
    );
}
