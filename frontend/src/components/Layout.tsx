import { useState, useRef, useEffect } from 'react'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import { getBalance } from '../api/auth'
import UserIcon from '../assets/icon/user.svg?react'
import PackageIcon from '../assets/icon/package.svg?react'
import InfoIcon from '../assets/icon/info.svg?react'
import InventoryIcon from '../assets/icon/inventory.svg?react'
import OrdersIcon from '../assets/icon/orders.svg?react'
import LogoutIcon from '../assets/icon/logout.svg?react'
import styles from './Layout.module.scss'

const NAV_ITEMS = [
    { path: '/about', label: '프로젝트 소개', Icon: InfoIcon },
    { path: '/', label: '상품 목록', Icon: PackageIcon },
    { path: '/inventory', label: '재고 관리', Icon: InventoryIcon },
]

export default function Layout() {
    const navigate = useNavigate()
    const location = useLocation()
    const { userId, isAuthenticated, clearAuth } = useAuthStore()
    const [dropdownOpen, setDropdownOpen] = useState(false)
    const dropdownRef = useRef<HTMLDivElement>(null)

    const { data: balanceData } = useQuery({
        queryKey: ['balance'],
        queryFn: getBalance,
        enabled: isAuthenticated && dropdownOpen,
    })

    // 드롭다운 외부 클릭 시 닫기
    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setDropdownOpen(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [])

    function handleLogout() {
        clearAuth()
        navigate('/login')
    }

    function isActive(path: string) {
        if (path === '/') return location.pathname === '/'
        return location.pathname.startsWith(path)
    }

    return (
        <div className={styles.wrapper}>
            <header className={styles.header}>
                <div className={styles.headerLeft}>
                    <button className={styles.logo} onClick={() => navigate('/')}>
                        MSA Commerce
                    </button>
                </div>

                <div className={styles.headerRight} ref={dropdownRef}>
                    {isAuthenticated ? (
                        <>
                            <p className={styles.userName}>{userId} 님</p>

                            <button
                                className={styles.iconBtn}
                                onClick={() => setDropdownOpen((prev) => !prev)}
                                aria-label="사용자 메뉴"
                            >
                                <UserIcon width={20} height={20} />
                            </button>

                            {dropdownOpen && (
                                <div className={styles.userDropdown}>
                                    <div className={styles.balanceInfo}>
                                        <div className={styles.balanceRow}>
                                            <span className={styles.balanceLabel}>예치금</span>
                                            <span className={styles.balanceAmount}>
                                                {balanceData
                                                    ? `${balanceData.balance.toLocaleString('ko-KR')}원`
                                                    : '로딩 중...'}
                                        </span>
                                        </div>
                                        <button
                                            className={styles.chargeBtn}
                                            onClick={() => { navigate('/charge'); setDropdownOpen(false) }}
                                        >
                                            예치금 충전
                                        </button>
                                    </div>
                                    <button
                                        className={styles.dropdownNavBtn}
                                        onClick={() => { navigate('/orders'); setDropdownOpen(false) }}
                                    >
                                        <OrdersIcon width={16} height={16} />
                                        주문 내역
                                    </button>
                                    <button className={styles.logoutBtn} onClick={handleLogout}>
                                        <LogoutIcon width={16} height={16} />
                                        로그아웃
                                    </button>
                                </div>
                            )}
                        </>
                    ) : (
                        <button className={styles.loginBtn} onClick={() => navigate('/login')}>
                            로그인
                        </button>
                    )}
                </div>
            </header>

            <div className={styles.body}>
                <aside className={styles.sidebar}>
                    <p className={styles.navSection}>메뉴</p>
                    {NAV_ITEMS.map(({ path, label, Icon }) => (
                        <button
                            key={path}
                            className={`${styles.navItem} ${isActive(path) ? styles.active : ''}`}
                            onClick={() => navigate(path)}
                        >
                            <Icon width={18} height={18} />
                            {label}
                        </button>
                    ))}
                </aside>

                <main className={styles.main}>
                    <Outlet />
                </main>
            </div>
        </div>
    )
}
