import { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getMe } from "@/src/api/api";

interface User {
  id: number;
  email: string;
  name: string;
  role: "ADMIN" | "MANAGER" | "USER";
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isAdmin: boolean;
  isManager: boolean;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  isAdmin: false,
  isManager: false,
  logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        console.log("Checking auth..."); // 디버깅용 로그
        const userData = await getMe();
        console.log("User data received:", userData); // 디버깅용 로그
        setUser(userData);
      } catch (error) {
        console.error("Auth check failed:", error); // 디버깅용 로그
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    // 토큰이 있는 경우에만 getMe 호출
    const token = localStorage.getItem("token");
    if (token) {
      checkAuth();
    } else {
      setIsLoading(false);
    }
  }, []); // 의존성 배열이 비어있으므로 컴포넌트 마운트 시 한 번만 실행

  const logout = () => {
    localStorage.removeItem("token");
    setUser(null);
    router.push("/");
  };

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    isAdmin: user?.role === "ADMIN",
    isManager: user?.role === "MANAGER",
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
} 