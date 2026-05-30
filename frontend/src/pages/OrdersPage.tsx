import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getOrders, type Order } from '../api/orders'
import StatusBadge from '../components/StatusBadge'
import styles from './OrdersPage.module.scss'

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
    const { data: orders, isLoading, isError } = useQuery<Order[]>({
        queryKey: ['orders'],
        queryFn: getOrders,
    })

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

    const sorted = [...(orders ?? [])].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )

    return (
        <div className={styles.page}>
            <div className={styles.pageHeader}>
                <h1 className={styles.pageTitle}>주문 내역</h1>
                <p className={styles.pageDescription}>총 {sorted.length}건의 주문이 있습니다.</p>
            </div>

            {sorted.length === 0 ? (
                <div className={styles.emptyBox}>
                    <p>주문 내역이 없습니다.</p>
                    <p>상품 목록에서 주문을 생성하세요.</p>
                </div>
            ) : (
                <div className={styles.list}>
                    {sorted.map((order) => (
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
    )
}
