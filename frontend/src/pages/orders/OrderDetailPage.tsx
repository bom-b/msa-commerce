import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getOrder, type Order } from '../../api/orders'
import { getPaymentByOrderId, type Payment } from '../../api/payments'
import StatusBadge from '../../components/ui/StatusBadge'
import ChevronLeftIcon from '../../assets/icon/chevron-left.svg?react'
import InfoIcon from '../../assets/icon/info.svg?react'
import styles from './OrderDetailPage.module.scss'

function formatDate(iso: string) {
    return new Date(iso).toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    })
}

function formatAmount(amount: number) {
    return amount.toLocaleString('ko-KR') + '원'
}

export default function OrderDetailPage() {
    const { id } = useParams<{ id: string }>()
    const navigate = useNavigate()
    const orderId = Number(id)

    const {
        data: order,
        isLoading: orderLoading,
        isError: orderError,
    } = useQuery<Order>({
        queryKey: ['order', orderId],
        queryFn: () => getOrder(orderId),
        enabled: !isNaN(orderId),
    })

    const {
        data: payment,
        isLoading: paymentLoading,
    } = useQuery<Payment>({
        queryKey: ['payment', orderId],
        queryFn: () => getPaymentByOrderId(orderId),
        enabled: !isNaN(orderId),
        // 결제 조회 실패 시 에러로 처리하지 않음 (PENDING 상태에선 없을 수 있음)
        retry: false,
    })

    if (orderLoading) {
        return (
            <div className={styles.page}>
                <p className={styles.stateBox}>주문 정보를 불러오는 중...</p>
            </div>
        )
    }

    if (orderError || !order) {
        return (
            <div className={styles.page}>
                <p className={styles.errorBox}>주문 정보를 불러오지 못했습니다.</p>
            </div>
        )
    }

    const isCancelled = order.status === 'CANCELLED'

    return (
        <div className={styles.page}>
            <button className={styles.backBtn} onClick={() => navigate('/orders')}>
                <ChevronLeftIcon width={16} height={16} />
                주문 내역으로 돌아가기
            </button>

            <h1 className={styles.pageTitle}>주문 #{order.id}</h1>

            {/* 취소 사유 강조 배너 */}
            {isCancelled && order.failureReason && (
                <div className={styles.cancelBanner} role="alert">
                    <InfoIcon className={styles.cancelBannerIcon} width={20} height={20} aria-hidden="true" />
                    <div className={styles.cancelBannerContent}>
                        <span className={styles.cancelBannerLabel}>취소 사유</span>
                        <span className={styles.cancelBannerValue}>{order.failureReason}</span>
                    </div>
                </div>
            )}

            {/* 주문 정보 */}
            <div className={styles.section}>
                <div className={styles.sectionHeader}>
                    <span className={styles.sectionTitle}>주문 정보</span>
                    <StatusBadge status={order.status} type="order" />
                </div>

                <div className={styles.sectionBody}>
                    <div className={styles.fieldGrid}>
                        <div className={styles.field}>
                            <span className={styles.fieldLabel}>상품명</span>
                            <span className={styles.fieldValue}>{order.productName}</span>
                        </div>
                        <div className={styles.field}>
                            <span className={styles.fieldLabel}>주문 수량</span>
                            <span className={styles.fieldValue}>{order.quantity.toLocaleString()}개</span>
                        </div>
                        <div className={styles.field}>
                            <span className={styles.fieldLabel}>총 금액</span>
                            <span className={styles.fieldValueLarge}>{formatAmount(order.totalAmount)}</span>
                        </div>
                        <div className={styles.field}>
                            <span className={styles.fieldLabel}>주문 일시</span>
                            <span className={styles.fieldValue}>{formatDate(order.createdAt)}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* 결제 정보 */}
            <div className={styles.section}>
                <div className={styles.sectionHeader}>
                    <span className={styles.sectionTitle}>결제 정보</span>
                    {payment && <StatusBadge status={payment.status} type="payment" />}
                </div>

                <div className={styles.sectionBody}>
                    {paymentLoading ? (
                        <p className={styles.paymentPending}>결제 정보를 불러오는 중...</p>
                    ) : payment ? (
                        <div className={styles.fieldGrid}>
                            <div className={styles.field}>
                                <span className={styles.fieldLabel}>결제 금액</span>
                                <span className={styles.fieldValueLarge}>{formatAmount(payment.amount)}</span>
                            </div>
                            <div className={styles.field}>
                                <span className={styles.fieldLabel}>결제 일시</span>
                                <span className={styles.fieldValue}>{formatDate(payment.createdAt)}</span>
                            </div>
                        </div>
                    ) : (
                        <p className={styles.paymentPending}>
                            {order.status === 'PENDING'
                                ? '결제 처리 중입니다. 잠시 후 새로고침하세요.'
                                : '결제 정보를 찾을 수 없습니다.'}
                        </p>
                    )}
                </div>
            </div>
        </div>
    )
}
