"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getMe } from "@/src/api/api";

interface User {
  id: number;
  email: string;
  name: string;
  role: "ADMIN" | "MANAGER" | "USER";
  profileImage?: string;
}

let globalUser: User | null = null;
let globalIsLoading = true;

export function logout() {
  localStorage.removeItem("token");
  globalUser = null;
  window.location.href = "/";
}

export function useAuth() {
  const [user, setUser] = useState<User | null>(globalUser);
  const [isLoading, setIsLoading] = useState(globalIsLoading);
  const router = useRouter();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const userData = await getMe();
        setUser(userData);
        globalUser = userData;
      } catch (error) {
        setUser(null);
        globalUser = null;
        localStorage.removeItem("token");
      } finally {
        setIsLoading(false);
        globalIsLoading = false;
      }
    };

    const token = localStorage.getItem("token");
    if (token) {
      checkAuth();
    } else {
      setIsLoading(false);
      globalIsLoading = false;
    }
  }, []);

  const requireRole = (role: string) => {
    if (!user || user.role !== role) {
      router.push("/login");
      return false;
    }
    return true;
  };

  return {
    user,
    isAuthenticated: !!user,
    isLoading,
    isAdmin: user?.role === "ADMIN",
    isManager: user?.role === "MANAGER",
    logout,
    requireRole,
    userRole: user?.role || null,
  };
}

export function saveToken(token: string) {
  localStorage.setItem("token", token);
  window.location.href = "/";
}

export function getToken() {
  if (typeof window !== "undefined") {
    return localStorage.getItem("token");
  }
  return null;
}
