"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getMe } from "@/src/api/api";

export default function OAuthCallback() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleOAuthCallback = async () => {
      try {
        const token = searchParams.get("token");
        if (!token) {
          router.push("/login");
          return;
        }

        // 토큰 저장
        localStorage.setItem("token", token);

        // 사용자 정보 확인
        const userData = await getMe();
        
        // 온보딩 상태 확인
        if (!userData.phoneNumber || !userData.email) {
          window.location.href = "/oauth2/sign-up";
          return;
        }
        
        router.push("/");
      } catch (error) {
        console.error("OAuth 콜백 처리 오류:", error);
        router.push("/login");
      }
    };

    handleOAuthCallback();
  }, [router, searchParams]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <h1 className="text-2xl font-bold mb-4">로그인 처리 중...</h1>
        <p className="text-muted-foreground">잠시만 기다려주세요.</p>
      </div>
    </div>
  );
} 