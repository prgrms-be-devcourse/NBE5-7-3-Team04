import axios, {
    InternalAxiosRequestConfig,
    AxiosResponse,
    AxiosError,
} from "axios";

// API 기본 URL
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;
export const CLOUDFRONT_URL = process.env.NEXT_PUBLIC_CLOUDFRONT_URL;

// 로그인 관련 API URL
const AUTH_API_URL = process.env.NEXT_PUBLIC_AUTH_API_URL;

// 개발 환경에서 API 요청 실패 시 사용할 모의 데이터
const MOCK_DATA_ENABLED = false;

// Axios 인스턴스 생성
export const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
    timeout: 10000, // 10초 타임아웃 설정
});

// API 요청을 위한 유틸리티 함수
async function fetchAPI(
    endpoint: string,
    options?: { method?: string; body?: string }
) {
    if (options?.method === "POST") {
        const response = await api.post(endpoint, options.body);
        return response.data;
    } else if (options?.method === "PATCH") {
        const response = await api.patch(endpoint, options.body);
        return response.data;
    } else {
        const response = await api.get(endpoint);
        return response.data;
    }
}

// 요청 인터셉터
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem("token");
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error: AxiosError) => {
        console.error("Request error:", error);
        return Promise.reject(error);
    }
);

// 응답 인터셉터
api.interceptors.response.use(
    (response: AxiosResponse) => {
        console.log("[API Interceptor] 응답 성공:", {
            url: response.config.url,
            method: response.config.method,
            status: response.status
        });
        return response;
    },
    (error: AxiosError) => {
        if (error.response) {
            console.error("[API Interceptor] 응답 에러:", {
                url: error.config?.url,
                method: error.config?.method,
                status: error.response.status,
                data: error.response.data,
                headers: error.response.headers,
            });

            if (error.response.status === 401) {
                console.log("[API Interceptor] 401 에러 발생 - 토큰 제거");
                localStorage.removeItem("token");
            }
        } else if (error.request) {
            console.error("[API Interceptor] 요청은 보냈지만 응답 없음:", error.request);
        } else {
            console.error("[API Interceptor] 요청 설정 중 에러:", error.message);
        }
        return Promise.reject(error);
    }
);

// API 함수들
export async function getPerformances(page = 0, size = 10) {
    const response = await api.get(
        `/users/performances?page=${page}&size=${size}`
    );
    return response.data;
}

export async function searchPerformances(params: {
    title?: string;
    venue?: string;
    start?: string;
    end?: string;
    category?: string;
    page?: number;
    size?: number;
    sort?: string[];
}) {
    const queryParams = new URLSearchParams();
    if (params.title) queryParams.append("title", params.title);
    if (params.venue) queryParams.append("venue", params.venue);
    if (params.start) queryParams.append("start", params.start);
    if (params.end) queryParams.append("end", params.end);
    if (params.category) queryParams.append("category", params.category);
    if (params.page !== undefined)
        queryParams.append("page", params.page.toString());
    if (params.size !== undefined)
        queryParams.append("size", params.size.toString());
    if (params.sort)
        params.sort.forEach((sort) => queryParams.append("sort", sort));

    const response = await api.get(`/users/search?${queryParams.toString()}`);
    return response.data;
}

export async function getPerformanceDetail(performanceId: number | string) {
    const response = await api.get(`/users/performances/${performanceId}`);
    return response.data;
}

export async function createReservation(data: {
    performanceId: number;
    scheduleId: number;
    quantity: number;
}) {
    const response = await api.post("/reservations", data);
    return response.data;
}

export async function cancelReservation(reservationId: number | string) {
    const response = await api.post(`/reservations/${reservationId}/cancel`);
    return response.data;
}

export async function getUserReservations(page = 0, size = 10) {
    const response = await api.get(
        `/users/reservations?page=${page}&size=${size}`
    );
    return response.data;
}

export async function getReservationDetail(reservationId: number | string) {
    const response = await api.get(`/reservations/me/${reservationId}`);
    return response.data;
}

export async function updateRefundBankInfo(data: {
    refundId: number;
    account: string;
    bank: string;
    depositorName: string;
}) {
    const response = await api.put(
        `/users/refunds/${data.refundId}/bank-info`,
        {
            account: data.account,
            bank: data.bank,
            depositorName: data.depositorName,
        }
    );
    return response.data;
}

export async function addBookmark(performanceId: number | string) {
    const response = await api.post(`/bookmark/${performanceId}`);
    return response.data;
}

export async function removeBookmark(performanceId: number | string) {
    const response = await api.patch(`/bookmark/${performanceId}`);
    return response.data;
}

export const getReviews = async (
    performanceId: string,
    page: number = 0,
    size: number = 20
) => {
    const response = await fetch(
        `${API_BASE_URL}/reviews/${performanceId}?page=${page}&size=${size}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        }
    );

    if (!response.ok) {
        throw new Error("리뷰를 불러오는데 실패했습니다.");
    }

    return response.json();
};

export const createReview = async (data: {
    performanceId: number;
    comment: string;
}) => {
    const response = await fetch(`${API_BASE_URL}/reviews`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: "리뷰 작성에 실패했습니다." }));
        throw new Error(error.message || "리뷰 작성에 실패했습니다.");
    }

    // 204 응답이거나 응답이 비어있는 경우 빈 객체 반환
    if (response.status === 204 || response.headers.get("content-length") === "0") {
        return {};
    }

    try {
        return await response.json();
    } catch (error) {
        return {};
    }
};

export async function getUserInfo() {
    const response = await api.get("/users/me");
    return response.data;
}

export async function submitManagerRequest() {
    const response = await api.post("/users/manager-request");
    return response.data;
}

export async function userOnboarding(data: {
    phoneNumber: string;
    email: string;
}) {
    const response = await api.post("/users/onboarding", data);
    return response.data;
}

export const getSocialLoginUrl = (provider: string) => {
    return `${AUTH_API_URL}/oauth2/authorization/${provider}`;
};

// Manager API functions
export async function getManagerPerformances(page = 0, size = 10) {
    return fetchAPI(`/managers/performances?page=${page}&size=${size}`);
}

export async function searchManagerPerformances(params: {
    title?: string;
    venue?: string;
    start?: string;
    end?: string;
    status?: string;
    page?: number;
    size?: number;
}) {
    const queryParams = new URLSearchParams();

    if (params.title) queryParams.append("title", params.title);
    if (params.venue) queryParams.append("venue", params.venue);
    if (params.start) queryParams.append("start", params.start);
    if (params.end) queryParams.append("end", params.end);
    if (params.status) queryParams.append("status", params.status);
    queryParams.append("page", String(params.page || 0));
    queryParams.append("size", String(params.size || 10));

    return fetchAPI(`/managers/performances/search?${queryParams.toString()}`);
}

export async function getManagerPerformanceDetails(
    performanceId: number | string
) {
    return fetchAPI(`/managers/performances/${performanceId}`);
}

export async function registerPerformance(data: {
    title: string;
    venue: string;
    price: number;
    totalSeats: number;
    category: string;
    startDate: string;
    endDate: string;
    description: string;
    fileId?: number;
}) {
    return fetchAPI("/managers/register", {
        method: "POST",
        body: JSON.stringify(data),
    });
}

export async function registerPerformanceSchedule(
    performanceId: number | string,
    data: {
        startTime: Date;
        endTime: Date;
    }
) {
    return fetchAPI(`/managers/performances/${performanceId}/register`, {
        method: "POST",
        body: JSON.stringify(data),
    });
}

export async function updatePerformance(
    performanceId: number | string,
    data: {
        fileId: number;
        description: string;
    }
) {
    return fetchAPI(`/managers/performance/${performanceId}`, {
        method: "PATCH",
        body: JSON.stringify(data),
    });
}

export async function cancelPerformance(performanceId: number | string) {
    return fetchAPI(`/managers/performances/${performanceId}/cancel`, {
        method: "PATCH",
    });
}

export async function cancelPerformanceSchedule(
    performanceId: number | string,
    scheduleId: number | string
) {
    return fetchAPI(
        `/managers/performances/${performanceId}/schedules/${scheduleId}`,
        {
            method: "PATCH",
        }
    );
}

export async function getManagerSettlements(page = 0, size = 10) {
    return fetchAPI(`/managers/settlements/me?page=${page}&size=${size}`);
}

export async function createSettlement(data: {
    performanceId: number;
    account: string;
    bank: string;
}) {
    return fetchAPI("/managers/settlements/register", {
        method: "POST",
        body: JSON.stringify(data),
    });
}

// 토큰 가져오기
function getToken() {
    if (typeof window !== "undefined") {
        return localStorage.getItem("token");
    }
    return null;
}

// File upload function
export async function uploadFile(file: File) {
    const formData = new FormData();
    formData.append("file", file);

    const token = getToken();
    const headers = {
        Authorization: token ? `Bearer ${token}` : "",
    };

    try {
        const response = await fetch(`${API_BASE_URL}/files`, {
            method: "POST",
            headers,
            body: formData,
        });

        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(
                error.message || `API 요청 실패: ${response.status}`
            );
        }

        return await response.json();
    } catch (error) {
        console.error("파일 업로드 오류:", error);
        throw error;
    }
}

export async function updateReview(reviewId: number, comment: string) {
    const response = await api.put(`/reviews/${reviewId}`, { comment });
    // 204 응답이거나 응답이 비어있는 경우 빈 객체 반환
    if (response.status === 204 || !response.data) {
        return {};
    }
    return response.data;
}

export async function deleteReview(reviewId: number) {
    const response = await api.delete(`/reviews/${reviewId}`);
    return response.data;
}

export async function getMe() {
    console.log("[getMe] 함수 호출");
    try {
        const response = await api.get("/users/me", {
            headers: {
                'Cache-Control': 'no-cache',
                'Pragma': 'no-cache'
            }
        });
        console.log("[getMe] 응답 성공:", response.data);
        return response.data;
    } catch (error) {
        console.error("[getMe] 에러 발생:", error);
        throw error;
    }
}
