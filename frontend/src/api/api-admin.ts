// import { api } from "./api"
// import { AxiosError } from "axios"

// // TODO: 테스트 완료 후 삭제
// export async function approveMe() {
//   try {
//     console.log('매니저 승인 요청 중...')
//     await api.patch("/users/approve/me")
    
//     console.log('업데이트된 유저 정보 가져오는 중...')
//     const response = await api.get("/users/me")
//     const user = response.data
    
//     localStorage.setItem("userInfo", JSON.stringify(user))
//     return user
//   } catch (error) {
//     console.error('approveMe 함수 실행 중 오류 발생:', error)
//     if (error instanceof AxiosError && error.response) {
//       console.error('서버 응답:', {
//         상태: error.response.status,
//         데이터: error.response.data
//       })
//     }
//     throw error
//   }
// } 