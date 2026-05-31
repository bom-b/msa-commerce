import { useState, useCallback } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getStocks, addStock, type Stock } from '../../api/stocks'
import Toast, { type ToastData } from '../../components/ui/Toast'
import styles from './InventoryPage.module.scss'

/** 상품별 재고 추가 입력 상태 */
interface AddState {
    quantity: number
    loading: boolean
}

export default function InventoryPage() {
    const queryClient = useQueryClient()
    const { data: stocks, isLoading, isError } = useQuery<Stock[]>({
        queryKey: ['stocks'],
        queryFn: getStocks,
    })

    const [addStates, setAddStates] = useState<Record<number, AddState>>({})
    const [toast, setToast] = useState<ToastData | null>(null)

    function getAddState(productId: number): AddState {
        return addStates[productId] ?? { quantity: 10, loading: false }
    }

    function setAddField(productId: number, partial: Partial<AddState>) {
        setAddStates((prev) => ({
            ...prev,
            [productId]: { ...getAddState(productId), ...partial },
        }))
    }

    /** "재고 추가" 버튼 클릭 — 재고 추가 API를 호출하고 결과를 토스트로 표시한다 */
    async function handleAddStock(stock: Stock) {
        const state = getAddState(stock.productId)
        if (state.quantity < 1) return

        const addedQuantity = state.quantity
        setAddField(stock.productId, { loading: true })
        try {
            await addStock(stock.productId, addedQuantity)
            setAddField(stock.productId, { loading: false, quantity: 10 })
            setToast({ message: `${stock.productName} 재고 ${addedQuantity}개를 추가했습니다.` })
            queryClient.invalidateQueries({ queryKey: ['stocks'] })
        } catch {
            // 전역 Axios 인터셉터가 window.alert()로 처리
            setAddField(stock.productId, { loading: false })
        }
    }

    const handleCloseToast = useCallback(() => setToast(null), [])

    if (isLoading) {
        return (
            <div className={styles.page}>
                <p className={styles.stateBox}>재고 목록을 불러오는 중...</p>
            </div>
        )
    }

    if (isError) {
        return (
            <div className={styles.page}>
                <p className={styles.errorBox}>재고 목록을 불러오지 못했습니다. 서버 상태를 확인하세요.</p>
            </div>
        )
    }

    return (
        <div className={styles.page}>
            <Toast toast={toast} onClose={handleCloseToast} />

            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>재고 관리</h1>
                <p className={styles.pageDescription}>상품별 재고를 확인하고 추가할 수 있습니다.</p>
            </div>

            <div className={styles.tableWrapper}>
                <table className={styles.table}>
                    <thead className={styles.thead}>
                        <tr>
                            <th className={styles.th}>상품 ID</th>
                            <th className={styles.th}>상품명</th>
                            <th className={styles.th}>현재 재고</th>
                            <th className={styles.th}>추가 수량</th>
                        </tr>
                    </thead>
                    <tbody>
                        {(stocks ?? []).map((stock) => {
                            const state = getAddState(stock.productId)
                            const stockClass =
                                stock.quantity === 0
                                    ? styles.out
                                    : stock.quantity <= 10
                                    ? styles.low
                                    : ''

                            return (
                                <tr key={stock.productId} className={styles.tr}>
                                    <td className={styles.td}>{stock.productId}</td>
                                    <td className={`${styles.td} ${styles.productName}`}>{stock.productName}</td>
                                    <td className={styles.td}>
                                        <span className={`${styles.stockValue} ${stockClass}`}>
                                            {stock.quantity.toLocaleString()}개
                                        </span>
                                    </td>
                                    <td className={styles.td}>
                                        <div className={styles.addForm}>
                                            <input
                                                className={styles.addInput}
                                                type="number"
                                                min={1}
                                                value={state.quantity}
                                                disabled={state.loading}
                                                aria-label={`${stock.productName} 추가 수량`}
                                                onChange={(e) =>
                                                    setAddField(stock.productId, {
                                                        quantity: Math.max(1, Number(e.target.value)),
                                                    })
                                                }
                                            />
                                            <button
                                                className={styles.addBtn}
                                                disabled={state.loading}
                                                onClick={() => handleAddStock(stock)}
                                            >
                                                {state.loading ? '처리 중...' : '재고 추가'}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
