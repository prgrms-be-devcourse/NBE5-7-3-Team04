import { type NextRequest, NextResponse } from "next/server"

// 소셜 로그인 처리 라우트
export async function GET(request: NextRequest, { params }: { params: { provider: string } }) {
  const provider = params.provider

  // 실제 구현에서는 백엔드 API로 리다이렉트
  const redirectUrl = `http://43.201.79.165:8080/oauth2/authorization/${provider}`

  // 리다이렉트
  return NextResponse.redirect(redirectUrl)
}
