// 라우트 파라미터를 Promise로 래핑하는 타입
export type RouteParams<T> = {
  params: Promise<T>
}

// 검색 파라미터를 Promise로 래핑하는 타입
export type SearchParams<T = { [key: string]: string | string[] | undefined }> = {
  searchParams: Promise<T>
}

// 페이지 props 타입
export type PageProps<P, S = { [key: string]: string | string[] | undefined }> = RouteParams<P> & SearchParams<S>

// 레이아웃 props 타입
export type LayoutProps<P> = RouteParams<P> & {
  children: React.ReactNode
} 