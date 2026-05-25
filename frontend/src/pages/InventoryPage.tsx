import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getStocks, addStock, type Stock } from '../api/stocks'
import styles from './InventoryPage.module.scss'

interface AddState {
    quantity: number
    loading: boolean
    success: string | null
    error: string | null
}

export default function InventoryPage() {
    const queryClient = useQueryClient()
    const { data: stocks, isLoading, isError } = useQuery<Stock[]>({
        queryKey: ['stocks'],
        queryFn: getStocks,
    })

    const [addStates, setAddStates] = useState<Record<number, AddState>>({})

    function getAddState(productId: number): AddState {
        return addStates[productId] ?? { quantity: 10, loading: false, success: null, error: null }
    }

    function setAddField(productId: number, partial: Partial<AddState>) {
        setAddStates((prev) => ({
            ...prev,
            [productId]: { ...getAddState(productId), ...partial },
        }))
    }

    async function handleAddStock(stock: Stock) {
        const state = getAddState(stock.productId)
        if (state.quantity < 1) return

        setAddField(stock.productId, { loading: true, success: null, error: null })
        try {
            // 재고 추가 API — stock-service에 PUT /stocks/{productId}/quantity 구현 예정
            await addStock(stock.productId, state.quantity)
            setAddField(stock.productId, {
                loading: false,
                success: `${state.quantity}개 추가 완료`,
                quantity: 10,
            })
            queryClient.invalidateQueries({ queryKey: ['stocks'] })
        } catch {
            setAddField(stock.productId, {
                loading: false,
                // 백엔드 미구현 안내 메시지
                error: '재고 추가 API가 아직 구현되지 않았습니다.',
            })
        }
    }

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
            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>재고 관리</h1>
                <p className={styles.pageDescription}>상품별 재고를 확인하고 추가할 수 있습니다.</p>
            </div>

            <div className={styles.notice}>
                재고 추가 기능은 stock-service 백엔드 구현 후 정상 동작합니다. (API: PUT /stocks/&#123;productId&#125;/quantity)
            </div>

            <div className={styles.tableWrapper}>
                <table className={styles.table}>
                    <thead className={styles.thead}>
                        <tr>
                            <th className={styles.th}>상품 ID</th>
                            <th className={styles.th}>상품명</th>
                            <th className={styles.th}>현재 재고</th>
                            <th className={styles.th}>추가 수량</th>
                            <th className={styles.th}>결과</th>
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
                                    <td className={`${styles.td} ${styles.resultCell}`}>
                                        {state.success && <span className={styles.successMsg}>{state.success}</span>}
                                        {state.error && <span className={styles.errorMsg}>{state.error}</span>}
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
