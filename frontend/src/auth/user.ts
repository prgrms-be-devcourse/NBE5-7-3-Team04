"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

interface User {
  id: number;
  email: string;
  name: string;
  role: string;
  profileImage?: string;
}

// 전역 이벤트를 위한 커스텀 이벤트
const AUTH_EVENT = "auth-state-changed";

export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const parseToken = (token: string) => {
    try {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join("")
      );
      const { sub: id, email, role } = JSON.parse(jsonPayload);
      return {
        id: Number(id),
        email,
        name: email ? email.split("@")[0] : `user${id}`,
        role: role.replace("ROLE_", ""),
      };
    } catch (error) {
      console.error("Error parsing token:", error);
      return null;
    }
  };

  useEffect(() => {
    const token = getToken();
    if (token) {
      const userData = parseToken(token);
      console.log('parsed token data:', userData);
      if (userData) {
        setIsAuthenticated(true);
        setUser(userData);
      } else {
        removeToken();
      }
    }
    setIsLoading(false);

    // 인증 상태 변경 이벤트 리스너
    const handleAuthChange = () => {
      const token = getToken();
      if (token) {
        const userData = parseToken(token);
        if (userData) {
          setIsAuthenticated(true);
          setUser(userData);
        }
      } else {
        setIsAuthenticated(false);
        setUser(null);
      }
    };

    window.addEventListener(AUTH_EVENT, handleAuthChange);
    return () => window.removeEventListener(AUTH_EVENT, handleAuthChange);
  }, []);

  const requireRole = (role: string) => {
    console.log('requireRole 호출:', { role, isAuthenticated, user });
    if (!isAuthenticated || !user || user.role !== role) {
      console.log('권한 없음, 로그인 페이지로 이동');
      router.push("/login");
      return false;
    }
    return true;
  };

  const userRole = user?.role || null;

  return {
    isAuthenticated,
    user,
    requireRole,
    userRole,
    isLoading,
  };
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

export function saveToken(token: string) {
  localStorage.setItem("token", token);
  const userData = parseToken(token);
  if (userData) {
    localStorage.setItem("userInfo", JSON.stringify(userData));
    // 인증 상태 변경 이벤트 발생
    window.dispatchEvent(new Event(AUTH_EVENT));
  }
}

function parseToken(token: string) {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const { sub: id, email, role } = JSON.parse(jsonPayload);
    return {
      id: Number(id),
      email,
      name: email ? email.split("@")[0] : `user${id}`,
      role: role.replace("ROLE_", ""),
    };
  } catch (error) {
    console.error("Error parsing token:", error);
    return null;
  }
}

export function removeToken() {
  localStorage.removeItem("token");
  localStorage.removeItem("userInfo");
  // 인증 상태 변경 이벤트 발생
  window.dispatchEvent(new Event(AUTH_EVENT));
}

export function logout() {
  removeToken();
  window.location.href = "/login";
}
