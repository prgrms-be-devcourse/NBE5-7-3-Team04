"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { userOnboarding, getMe } from "@/src/api/api";
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
  const [initialEmail, setInitialEmail] = useState("");

  useEffect(() => {
    const checkToken = async () => {
      const accessToken = searchParams.get("accessToken");
      const refreshToken = searchParams.get("refreshToken");
      const storedToken = localStorage.getItem("token");

      if (accessToken && refreshToken) {
        saveToken(accessToken);
        localStorage.setItem("refreshToken", refreshToken);
        await parseAndSetUserInfo(accessToken);
      } else if (storedToken) {
        try {
          const userData = await getMe();
          setFormData((prev) => ({
            ...prev,
            email: userData.email || "",
            name: userData.name || "",
          }));
          setInitialEmail(userData.email || "");
          setTokenReady(true);
        } catch (error) {
          console.error("Error fetching user data:", error);
          router.replace("/login?error=invalid_token");
        }
      } else {
        router.replace("/login?error=missing_token");
      }
    };

    const parseAndSetUserInfo = async (token: string) => {
      try {
        const base64Url = token.split(".")[1];
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
        setInitialEmail(email);
        setTokenReady(true);
      } catch (e) {
        console.error("Error parsing token:", e);
        router.replace("/login?error=invalid_token");
      }
    };

    checkToken();
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

      // 토큰이 이미 저장되어 있으므로 추가 저장 불필요
      window.location.href = "/";
    } catch (error) {
      toast.error("회원가입 중 오류가 발생했습니다.");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    if (name === "phoneNumber") {
      // 숫자만 추출
      const numbers = value.replace(/[^0-9]/g, "");

      // 최대 11자리까지만 허용
      const limitedNumbers = numbers.slice(0, 11);

      // 전화번호 포맷팅 (010-0000-0000)
      let formattedNumber = "";
      if (limitedNumbers.length > 0) {
        formattedNumber = limitedNumbers.replace(
          /(\d{3})(\d{0,4})(\d{0,4})/,
          (_, p1, p2, p3) => {
            let result = p1;
            if (p2) result += `-${p2}`;
            if (p3) result += `-${p3}`;
            return result;
          }
        );
      }

      setFormData((prev) => ({
        ...prev,
        [name]: formattedNumber,
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
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
                  placeholder="01000000000"
                  type="tel"
                  required
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  maxLength={13}
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
                  readOnly={!!initialEmail}
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
