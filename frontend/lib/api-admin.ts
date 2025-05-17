import { fetchAPI } from "@/lib/api"


// TODO: 테스트 완료 후 삭제
export async function approveMe() {
  // 매니저 승인 요청
  await fetchAPI("/users/approve/me", {
    method: "PATCH",
    credentials: "include",
  })
  // 승인 후 최신 유저 정보 받아와서 localStorage 갱신
  const user = await fetchAPI("/users/me", { credentials: "include" })
  localStorage.setItem("userInfo", JSON.stringify(user))
  return user
} 