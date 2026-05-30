import { useState, useCallback } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { getStocks, type Stock } from '../api/stocks'
import { createOrder } from '../api/orders'
import Toast, { type ToastData } from '../components/Toast'
import OrderConfirmModal from './OrderConfirmModal'
import styles from './ProductsPage.module.scss'

/** 상품별 주문 입력 상태 */
interface OrderState {
    quantity: number
    loading: boolean
}

/** 주문 확인 모달에 전달할 컨텍스트 */
interface ConfirmContext {
    stock: Stock
    quantity: number
}

function getStockBadgeClass(quantity: number, styles: Record<string, string>) {
    if (quantity === 0) return styles.stockBadgeOut
    if (quantity <= 10) return styles.stockBadgeLow
    return styles.stockBadge
}

function getStockBadgeLabel(quantity: number) {
    if (quantity === 0) return '품절'
    if (quantity <= 10) return '재고 부족'
    return '재고 있음'
}

export default function ProductsPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { isAuthenticated } = useAuthStore()
    const { data: stocks, isLoading, isError } = useQuery<Stock[]>({
        queryKey: ['stocks'],
        queryFn: getStocks,
    })

    const [orderStates, setOrderStates] = useState<Record<number, OrderState>>({})
    const [imgErrors, setImgErrors] = useState<Set<number>>(new Set())
    const [confirmCtx, setConfirmCtx] = useState<ConfirmContext | null>(null)
    const [toast, setToast] = useState<ToastData | null>(null)

    function getOrderState(productId: number): OrderState {
        return orderStates[productId] ?? { quantity: 1, loading: false }
    }

    function setOrderField(productId: number, partial: Partial<OrderState>) {
        setOrderStates((prev) => ({
            ...prev,
            [productId]: { ...getOrderState(productId), ...partial },
        }))
    }

    /** "주문하기" 버튼 클릭 — 확인 모달을 띄운다 */
    function handleOrderClick(stock: Stock) {
        if (!isAuthenticated) {
            navigate('/login')
            return
        }
        const state = getOrderState(stock.productId)
        if (state.quantity < 1) return
        setConfirmCtx({ stock, quantity: state.quantity })
    }

    /** 모달 "확인" 클릭 — 실제 주문 API 호출 */
    async function handleConfirm() {
        if (!confirmCtx) return
        const { stock, quantity } = confirmCtx
        setOrderField(stock.productId, { loading: true })

        try {
            const order = await createOrder({ productId: stock.productId, quantity })
            setConfirmCtx(null)
            setOrderField(stock.productId, { loading: false, quantity: 1 })
            setToast({
                message: `주문이 생성되었습니다. (주문 #${order.id})`,
                actionLabel: '주문 내역 보기',
                onAction: () => navigate('/orders'),
            })
            queryClient.invalidateQueries({ queryKey: ['stocks'] })
            queryClient.invalidateQueries({ queryKey: ['orders'] })
        } catch {
            // 전역 Axios 인터셉터가 window.alert()로 처리
            setConfirmCtx(null)
            setOrderField(stock.productId, { loading: false })
        }
    }

    const handleCloseToast = useCallback(() => setToast(null), [])

    if (isLoading) {
        return (
            <div className={styles.page}>
                <p className={styles.stateBox}>상품 목록을 불러오는 중...</p>
            </div>
        )
    }

    if (isError) {
        return (
            <div className={styles.page}>
                <p className={styles.errorBox}>상품 목록을 불러오지 못했습니다. 서버 상태를 확인하세요.</p>
            </div>
        )
    }

    return (
        <div className={styles.page}>
            <Toast toast={toast} onClose={handleCloseToast} />

            {confirmCtx && (
                <OrderConfirmModal
                    productName={confirmCtx.stock.productName}
                    quantity={confirmCtx.quantity}
                    price={confirmCtx.stock.price}
                    loading={getOrderState(confirmCtx.stock.productId).loading}
                    onConfirm={handleConfirm}
                    onCancel={() => setConfirmCtx(null)}
                />
            )}

            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>상품 목록</h1>
                <p className={styles.pageDescription}>상품을 선택하여 주문하세요.</p>
            </div>

            <div className={styles.grid}>
                {(stocks ?? []).map((stock) => {
                    const state = getOrderState(stock.productId)
                    const outOfStock = stock.quantity === 0

                    return (
                        <div key={stock.productId} className={styles.card}>
                            <div className={styles.productImageWrapper}>
                                {stock.imageName && !imgErrors.has(stock.productId) ? (
                                    <img
                                        className={styles.productImage}
                                        src={`/images/stock/${stock.imageName}`}
                                        alt={stock.productName}
                                        onError={() =>
                                            setImgErrors((prev) => new Set(prev).add(stock.productId))
                                        }
                                    />
                                ) : (
                                    <div className={styles.productImagePlaceholder}>
                                        <span>{stock.productName.charAt(0)}</span>
                                    </div>
                                )}
                            </div>

                            <div className={styles.cardBody}>
                                <div className={styles.cardHeader}>
                                    <span className={styles.productName}>{stock.productName}</span>
                                    <span className={getStockBadgeClass(stock.quantity, styles)}>
                                        {getStockBadgeLabel(stock.quantity)}
                                    </span>
                                </div>

                                <div className={styles.stockInfo}>
                                    <span className={styles.stockLabel}>가격</span>
                                    <span className={styles.stockValue}>{stock.price.toLocaleString()}원</span>
                                </div>

                                <div className={styles.stockInfo}>
                                    <span className={styles.stockLabel}>재고</span>
                                    <span className={styles.stockValue}>{stock.quantity.toLocaleString()}개</span>
                                </div>

                                <div className={styles.divider} />

                                <div className={styles.orderForm}>
                                    <span className={styles.orderLabel}>주문 수량</span>
                                    <div className={styles.orderRow}>
                                        <input
                                            className={styles.quantityInput}
                                            type="number"
                                            min={1}
                                            max={stock.quantity}
                                            value={state.quantity}
                                            disabled={outOfStock}
                                            aria-label={`${stock.productName} 주문 수량`}
                                            onChange={(e) =>
                                                setOrderField(stock.productId, {
                                                    quantity: Math.max(1, Number(e.target.value)),
                                                })
                                            }
                                        />
                                        <button
                                            className={styles.orderBtn}
                                            disabled={outOfStock || state.loading}
                                            onClick={() => handleOrderClick(stock)}
                                        >
                                            {state.loading ? '처리 중...' : '주문하기'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )
                })}
            </div>
        </div>
    )
}
