// API 기본 URL
 const API_BASE_URL = "http://43.201.79.165:8080/api/v1"
//const API_BASE_URL = "http://localhost:8080/api/v1"

// 개발 환경에서 API 요청 실패 시 사용할 모의 데이터
const MOCK_DATA_ENABLED = true

// 토큰 가져오기 함수
export const getToken = () => {
  if (typeof window !== "undefined") {
    return localStorage.getItem("token")
  }
  return null
}

// 기본 fetch 함수
export async function fetchAPI(endpoint: string, options: RequestInit = {}) {
  const token = getToken()

  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    })

    if (!response.ok) {
      const error = await response.json().catch(() => ({}))
      throw new Error(error.message || `API 요청 실패: ${response.status}`)
    }

    // 204 No Content 응답인 경우 빈 객체 반환
    if (response.status === 204) {
      return {}
    }

    return await response.json()
  } catch (error) {
    console.error(`API 요청 오류 (${endpoint}):`, error)

    // 개발 환경에서 모의 데이터 사용
    if (MOCK_DATA_ENABLED) {
      console.warn("API 요청 실패, 모의 데이터를 사용합니다.")
      return getMockData(endpoint, options)
    }

    throw error
  }
}

// 모의 데이터 제공 함수
function getMockData(endpoint: string, options: RequestInit = {}) {
  // 공연 상세 정보 모의 데이터
  if (endpoint.match(/\/users\/performances\/\d+/)) {
    const performanceId = endpoint.split("/").pop()
    return {
      id: Number(performanceId),
      title: "2023 여름 재즈 페스티벌",
      price: 50000,
      totalSeats: 500,
      venue: "서울 올림픽 공원",
      description:
        "여름을 맞이하여 국내외 유명 재즈 아티스트들이 모여 특별한 공연을 선보입니다. 다양한 재즈 음악과 함께 특별한 여름 밤을 경험해보세요.",
      status: "CONFIRMED",
      fileUrl: "/placeholder.svg?height=400&width=800",
      startDate: new Date(Date.now() + 86400000).toISOString(), // 내일
      endDate: new Date(Date.now() + 86400000 * 30).toISOString(), // 30일 후
      bookmarked: false,
      category: "SINGING",
      schedules: [
        {
          id: 1,
          startTime: new Date(Date.now() + 86400000).toISOString(), // 내일
          endTime: new Date(Date.now() + 86400000 + 7200000).toISOString(), // 내일 + 2시간
          remainingSeats: 275,
          isCanceled: false,
        },
        {
          id: 2,
          startTime: new Date(Date.now() + 86400000 + 21600000).toISOString(), // 내일 저녁
          endTime: new Date(Date.now() + 86400000 + 28800000).toISOString(), // 내일 저녁 + 2시간
          remainingSeats: 120,
          isCanceled: false,
        },
        {
          id: 3,
          startTime: new Date(Date.now() + 172800000).toISOString(), // 모레
          endTime: new Date(Date.now() + 172800000 + 7200000).toISOString(), // 모레 + 2시간
          remainingSeats: 350,
          isCanceled: false,
        },
        {
          id: 4,
          startTime: new Date(Date.now() + 172800000 + 21600000).toISOString(), // 모레 저녁
          endTime: new Date(Date.now() + 172800000 + 28800000).toISOString(), // 모레 저녁 + 2시간
          remainingSeats: 0,
          isCanceled: false,
        },
      ],
    }
  }

  // 공연 목록 모의 데이터
  if (endpoint.match(/\/users\/performances/)) {
    return {
      totalElements: 8,
      totalPages: 1,
      size: 10,
      content: Array(8)
        .fill(null)
        .map((_, i) => ({
          id: i + 1,
          fileUrl: "/placeholder.svg?height=300&width=400",
          title: `공연 제목 ${i + 1}`,
          price: 50000 + i * 10000,
          startDate: new Date(Date.now() + 86400000 * i).toISOString(),
          endDate: new Date(Date.now() + 86400000 * (i + 10)).toISOString(),
          venue: `공연장 ${i + 1}`,
          category: ["OPERA", "DANCING", "SINGING"][i % 3],
        })),
      number: 0,
      sort: {
        empty: false,
        sorted: true,
        unsorted: false,
      },
      numberOfElements: 8,
      pageable: {
        offset: 0,
        pageNumber: 0,
        pageSize: 10,
      },
      first: true,
      last: true,
      empty: false,
    }
  }

  // 예약 생성 모의 응답
  if (endpoint === "/reservations" && options?.method === "POST") {
    return {
      reservationId: Math.floor(Math.random() * 10000),
      title: "2023 여름 재즈 페스티벌",
      venue: "서울 올림픽 공원",
      quantity: JSON.parse(options.body as string).quantity,
      status: "PAYMENTS_PENDING",
      createdAt: new Date().toISOString(),
      expirationAt: new Date(Date.now() + 86400000).toISOString(),
      ticketPrice: 50000,
      totalPrice: 50000 * JSON.parse(options.body as string).quantity,
    }
  }

  // 검색 결과 모의 데이터
  if (endpoint.match(/\/users\/search/)) {
    const query = new URLSearchParams(endpoint.split("?")[1])
    const searchTerm = query.get("title") || ""

    return {
      totalElements: 3,
      totalPages: 1,
      size: 10,
      content: Array(3)
        .fill(null)
        .map((_, i) => ({
          id: i + 1,
          fileUrl: "/placeholder.svg?height=300&width=400",
          title: `${searchTerm} 관련 공연 ${i + 1}`,
          price: 50000 + i * 10000,
          startDate: new Date(Date.now() + 86400000 * i).toISOString(),
          endDate: new Date(Date.now() + 86400000 * (i + 10)).toISOString(),
          venue: `공연장 ${i + 1}`,
          category: ["OPERA", "DANCING", "SINGING"][i % 3],
        })),
      number: 0,
      pageable: {
        offset: 0,
        pageNumber: 0,
        pageSize: 10,
      },
      first: true,
      last: true,
      empty: false,
    }
  }

  // 소셜 로그인 모의 응답
  if (endpoint.match(/\/oauth2\/authorization\/(google|naver|kakao)/)) {
    return {
      token: "mock_token_" + Math.random().toString(36).substring(2, 15),
      user: {
        id: Math.floor(Math.random() * 1000),
        name: "사용자" + Math.floor(Math.random() * 100),
        email: "user" + Math.floor(Math.random() * 100) + "@example.com",
      },
    }
  }

  // 리뷰 목록 모의 데이터
  if (endpoint.match(/\/reviews\/\d+/)) {
    const page = new URLSearchParams(endpoint.split("?")[1]).get("page") || "0"
    const pageNum = Number.parseInt(page)

    return {
      totalElements: 15,
      totalPages: 3,
      size: 5,
      content: Array(5)
        .fill(null)
        .map((_, i) => ({
          id: pageNum * 5 + i + 1,
          userId: Math.floor(Math.random() * 1000),
          userName: `관람객${pageNum * 5 + i + 1}`,
          rating: Math.floor(Math.random() * 3) + 3, // 3-5점 사이
          comment: [
            "정말 멋진 공연이었습니다. 배우들의 연기가 인상적이었어요.",
            "음향과 조명이 완벽했습니다. 다음에도 꼭 보러 오고 싶네요.",
            "기대했던 것보다 더 좋았습니다. 특히 마지막 장면이 감동적이었어요.",
            "친구들과 함께 봤는데 모두 만족했습니다. 추천해요!",
            "티켓 값이 아깝지 않은 공연이었습니다. 배우들의 열정이 느껴졌어요.",
          ][i % 5],
          createdAt: new Date(Date.now() - Math.random() * 30 * 86400000).toISOString(), // 최근 30일 내
        })),
      number: pageNum,
      pageable: {
        offset: pageNum * 5,
        pageNumber: pageNum,
        pageSize: 5,
      },
      first: pageNum === 0,
      last: pageNum === 2,
      empty: false,
    }
  }

  // 리뷰 작성 모의 응답
  if (endpoint === "/reviews" && options?.method === "POST") {
    return {
      id: Math.floor(Math.random() * 1000),
      userId: Math.floor(Math.random() * 1000),
      userName: "현재 사용자",
      rating: 5,
      comment: JSON.parse(options.body as string).comments,
      createdAt: new Date().toISOString(),
    }
  }

  // 기본 빈 응답
  return {}
}

// 공연 목록 가져오기
export async function getPerformances(page = 0, size = 10) {
  return fetchAPI(`/users/performances?page=${page}&size=${size}`)
}

// 공연 검색
export async function searchPerformances(params: {
  title?: string
  venue?: string
  start?: string
  end?: string
  category?: string
  page?: number
  size?: number
}) {
  const queryParams = new URLSearchParams()

  if (params.title) queryParams.append("title", params.title)
  if (params.venue) queryParams.append("venue", params.venue)
  if (params.start) queryParams.append("start", params.start)
  if (params.end) queryParams.append("end", params.end)
  if (params.category) queryParams.append("category", params.category)
  queryParams.append("page", String(params.page || 0))
  queryParams.append("size", String(params.size || 10))

  return fetchAPI(`/users/search?${queryParams.toString()}`)
}

// 공연 상세 정보 가져오기
export async function getPerformanceDetail(performanceId: number | string) {
  return fetchAPI(`/users/performances/${performanceId}`)
}

// 예약하기
export async function createReservation(data: { scheduleId: number; quantity: number }) {
  return fetchAPI("/reservations", {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// 예약 취소하기
export async function cancelReservation(reservationId: number | string) {
  return fetchAPI(`/reservations/${reservationId}/cancel`, {
    method: "POST",
  })
}

// 사용자 예약 목록 가져오기
export async function getUserReservations(page = 0, size = 10) {
  return fetchAPI(`/reservations/me?page=${page}&size=${size}`)
}

// 예약 상세 정보 가져오기
export async function getReservationDetail(reservationId: number | string) {
  return fetchAPI(`/reservations/me/${reservationId}`)
}

// 환불 정보 업데이트
export async function updateRefundBankInfo(data: {
  refundId: number
  account: string
  bank: string
  depositorName: string
}) {
  return fetchAPI("/refunds", {
    method: "PATCH",
    body: JSON.stringify(data),
  })
}

// 북마크 추가
export async function addBookmark(performanceId: number | string) {
  return fetchAPI(`/bookmark/${performanceId}`, {
    method: "POST",
  })
}

// 북마크 취소
export async function removeBookmark(performanceId: number | string) {
  return fetchAPI(`/bookmark/${performanceId}`, {
    method: "PATCH",
  })
}

// 리뷰 작성
export async function createReview(data: {
  performanceId: number
  scheduledId: number
  comments: string
  rating?: number
}) {
  return fetchAPI("/reviews", {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// 리뷰 목록 가져오기
export async function getReviews(performanceId: number | string, page = 0, size = 5) {
  return fetchAPI(`/reviews/${performanceId}?page=${page}&size=${size}`)
}

// 사용자 정보 가져오기
export async function getUserInfo() {
  return fetchAPI("/users/me")
}

// 공연 관리자 신청
export async function submitManagerRequest() {
  return fetchAPI("/users/manager-request", {
    method: "POST",
  })
}

// 사용자 온보딩
export async function userOnboarding(data: { phoneNumber: string; email: string }) {
  return fetchAPI("/users/onboarding", {
    method: "POST",
    body: JSON.stringify(data),
  })
}

// 소셜 로그인 리다이렉트 URL 가져오기
export function getSocialLoginUrl(provider: string) {
  return `${API_BASE_URL}/oauth2/authorization/${provider}`
}
