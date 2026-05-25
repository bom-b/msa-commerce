import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

/** 로그인 상태에서 /login 접근 시 홈으로 리다이렉트 */
export default function PublicOnlyRoute() {
    const { isAuthenticated } = useAuthStore()
    return isAuthenticated ? <Navigate to="/" replace /> : <Outlet />
}
