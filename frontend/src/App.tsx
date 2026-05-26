import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Layout from './components/Layout'
import PrivateRoute from './components/PrivateRoute'
import PublicOnlyRoute from './components/PublicOnlyRoute'
import ScrollToTop from './components/ScrollToTop'
import LoginPage from './pages/LoginPage'
import AboutPage from './pages/AboutPage'
import ProductsPage from './pages/ProductsPage'
import InventoryPage from './pages/InventoryPage'
import OrdersPage from './pages/OrdersPage'
import OrderDetailPage from './pages/OrderDetailPage'
import ChargePage from './pages/ChargePage'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30_000,
            retry: 1,
        },
    },
})

export default function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <ScrollToTop />
                <Routes>
                    {/* 로그인 (비로그인 전용) */}
                    <Route element={<PublicOnlyRoute />}>
                        <Route path="/login" element={<LoginPage />} />
                    </Route>

                    {/* 비로그인 접근 가능 */}
                    <Route element={<Layout />}>
                        <Route path="/" element={<ProductsPage />} />
                        <Route path="/about" element={<AboutPage />} />
                    </Route>

                    {/* 인증 필요 페이지 */}
                    <Route element={<PrivateRoute />}>
                        <Route element={<Layout />}>
                            <Route path="/inventory" element={<InventoryPage />} />
                            <Route path="/orders" element={<OrdersPage />} />
                            <Route path="/orders/:id" element={<OrderDetailPage />} />
                            <Route path="/charge" element={<ChargePage />} />
                        </Route>
                    </Route>

                    {/* 매칭 없는 경로 → 홈으로 */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </BrowserRouter>
        </QueryClientProvider>
    )
}
