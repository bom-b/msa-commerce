import type { OrderStatus } from '../../api/orders'
import type { PaymentStatus } from '../../api/payments'
import styles from './StatusBadge.module.scss'

const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
    PENDING: '처리중',
    COMPLETED: '완료',
    CANCELLED: '취소',
}

const PAYMENT_STATUS_LABEL: Record<PaymentStatus, string> = {
    PENDING: '처리중',
    COMPLETED: '완료',
    FAILED: '실패',
    REFUNDED: '환불',
}

interface Props {
    status: OrderStatus | PaymentStatus
    type?: 'order' | 'payment'
}

const statusClassMap: Record<string, string> = {
    pending: styles.pending,
    completed: styles.completed,
    cancelled: styles.cancelled,
    failed: styles.failed,
    refunded: styles.refunded,
}

export default function StatusBadge({ status, type = 'order' }: Props) {
    const label =
        type === 'order'
            ? ORDER_STATUS_LABEL[status as OrderStatus] ?? status
            : PAYMENT_STATUS_LABEL[status as PaymentStatus] ?? status

    const className = statusClassMap[status.toLowerCase()] ?? ''

    return <span className={`${styles.badge} ${className}`}>{label}</span>
}
