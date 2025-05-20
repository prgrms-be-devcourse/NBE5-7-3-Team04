import { type NextRequest } from "next/server"
import { type RouteParams } from "@/types/route"

interface ProviderParams {
  provider: string
}

// 소셜 로그인 처리 라우트
export async function GET(
  request: NextRequest,
  context: RouteParams<ProviderParams>
) {
  const { provider } = await context.params

  // 실제 구현에서는 백엔드 API로 리다이렉트
  // const redirectUrl = `https://ticket4u.xyz/oauth2/authorization/${provider}`
  const redirectUrl = `http://43.201.79.165:8080/oauth2/authorization/${provider}`
  // const redirectUrl = `http://localhost:8080/oauth2/authorization/${provider}`

  // 리다이렉트
  return Response.redirect(redirectUrl)
}
