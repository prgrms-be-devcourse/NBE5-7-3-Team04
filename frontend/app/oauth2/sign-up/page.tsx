"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { userOnboarding } from "@/src/api/api";
import { saveToken } from "@/src/auth/user";

export default function OAuth2SignUpPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    phoneNumber: "",
    email: "",
    name: "",
  });
  const [tokenReady, setTokenReady] = useState(false);

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");

    if (accessToken && refreshToken) {
      saveToken(accessToken);
      localStorage.setItem("refreshToken", refreshToken);

      // JWT에서 정보 파싱
      try {
        const base64Url = accessToken.split(".")[1];
        const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split("")
            .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
            .join("")
        );
        const { email = "", name = "" } = JSON.parse(jsonPayload);

        setFormData((prev) => ({
          ...prev,
          email,
          name,
        }));
      } catch (e) {
        // 파싱 실패 시 무시
      }

      setTokenReady(true);
    } else {
      router.replace("/login");
    }
  }, [router, searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await userOnboarding({
        phoneNumber: formData.phoneNumber,
        email: formData.email,
      });
      toast.success("회원가입이 완료되었습니다.");
      const accessToken = localStorage.getItem("token");
      if (accessToken) {
        saveToken(accessToken);
      }
      router.replace("/");
    } catch (error) {
      toast.error("회원가입 중 오류가 발생했습니다.");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  if (!tokenReady) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div>로그인 처리 중입니다...</div>
      </div>
    );
  }

  return (
    <div className="container flex h-screen w-screen flex-col items-center justify-center">
      <div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px]">
        <div className="flex flex-col space-y-2 text-center">
          <h1 className="text-2xl font-semibold tracking-tight">회원가입</h1>
          <p className="text-sm text-muted-foreground">
            추가 정보를 입력하여 회원가입을 완료해주세요.
          </p>
        </div>

        <div className="grid gap-6">
          <form onSubmit={handleSubmit}>
            <div className="grid gap-4">
              <div className="grid gap-2">
                <Label htmlFor="name">이름</Label>
                <Input
                  id="name"
                  name="name"
                  value={formData.name}
                  readOnly
                  disabled
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="phoneNumber">전화번호</Label>
                <Input
                  id="phoneNumber"
                  name="phoneNumber"
                  placeholder="010-0000-0000"
                  type="tel"
                  required
                  value={formData.phoneNumber}
                  onChange={handleChange}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="email">이메일</Label>
                <Input
                  id="email"
                  name="email"
                  placeholder="example@email.com"
                  type="email"
                  required
                  value={formData.email}
                  readOnly={!!formData.email}
                  onChange={handleChange}
                />
              </div>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? "처리중..." : "회원가입 완료"}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
