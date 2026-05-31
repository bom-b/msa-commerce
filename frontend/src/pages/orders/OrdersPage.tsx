import { useState, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getOrders, type Order, type PageResponse } from '../../api/orders'
import StatusBadge from '../../components/ui/StatusBadge'
import styles from './OrdersPage.module.scss'

const PAGE_SIZE = 10

function formatDate(iso: string) {
    return new Date(iso).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    })
}

function formatAmount(amount: number) {
    return amount.toLocaleString('ko-KR') + '원'
}

export default function OrdersPage() {
    const navigate = useNavigate()
    const [page, setPage] = useState(0)
    const listRef = useRef<HTMLDivElement>(null)

    const { data, isLoading, isError } = useQuery<PageResponse<Order>>({
        queryKey: ['orders', page],
        queryFn: () => getOrders(page, PAGE_SIZE),
    })

    function handlePageChange(nextPage: number) {
        setPage(nextPage)
        listRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }

    if (isLoading) {
        return (
            <div className={styles.page}>
                <p className={styles.stateBox}>주문 목록을 불러오는 중...</p>
            </div>
        )
    }

    if (isError) {
        return (
            <div className={styles.page}>
                <p className={styles.errorBox}>주문 목록을 불러오지 못했습니다. 서버 상태를 확인하세요.</p>
            </div>
        )
    }

    const orders = data?.content ?? []
    const totalElements = data?.totalElements ?? 0
    const totalPages = data?.totalPages ?? 0

    return (
        <div className={styles.page}>
            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>주문 내역</h1>
                <p className={styles.pageDescription}>총 {totalElements.toLocaleString()}건의 주문이 있습니다.</p>
            </div>

            <div ref={listRef}>
                {orders.length === 0 ? (
                    <div className={styles.emptyBox}>
                        <p>주문 내역이 없습니다.</p>
                        <p>상품 목록에서 주문을 생성하세요.</p>
                    </div>
                ) : (
                    <div className={styles.list}>
                        {orders.map((order) => (
                            <div
                                key={order.id}
                                className={styles.card}
                                role="button"
                                tabIndex={0}
                                onClick={() => navigate(`/orders/${order.id}`)}
                                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') navigate(`/orders/${order.id}`) }}
                            >
                                <div className={styles.cardTop}>
                                    <span className={styles.orderId}>주문 #{order.id}</span>
                                    <StatusBadge status={order.status} type="order" />
                                </div>

                                <div className={styles.cardMeta}>
                                    <div className={styles.metaItem}>
                                        <span className={styles.metaLabel}>상품명</span>
                                        <span className={styles.metaValue}>{order.productName}</span>
                                    </div>
                                    <div className={styles.metaItem}>
                                        <span className={styles.metaLabel}>수량</span>
                                        <span className={styles.metaValue}>{order.quantity.toLocaleString()}개</span>
                                    </div>
                                    <div className={styles.metaItem}>
                                        <span className={styles.metaLabel}>총 금액</span>
                                        <span className={styles.metaValue}>{formatAmount(order.totalAmount)}</span>
                                    </div>
                                </div>

                                <div className={styles.cardFooter}>
                                    <span className={styles.createdAt}>{formatDate(order.createdAt)}</span>
                                    <span className={styles.viewDetail}>상세 보기 &rsaquo;</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {totalPages > 1 && (
                <div className={styles.pagination}>
                    <button
                        className={styles.pageBtn}
                        onClick={() => handlePageChange(page - 1)}
                        disabled={data?.first ?? true}
                        aria-label="이전 페이지"
                    >
                        &lsaquo; 이전
                    </button>

                    <span className={styles.pageInfo}>
                        <span className={styles.pageCurrentNum}>{page + 1}</span>
                        <span className={styles.pageSeparator}>/</span>
                        <span className={styles.pageTotalNum}>{totalPages}</span>
                    </span>

                    <button
                        className={styles.pageBtn}
                        onClick={() => handlePageChange(page + 1)}
                        disabled={data?.last ?? true}
                        aria-label="다음 페이지"
                    >
                        다음 &rsaquo;
                    </button>
                </div>
            )}
        </div>
    )
}
