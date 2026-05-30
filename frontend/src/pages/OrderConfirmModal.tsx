import { useEffect, useRef } from 'react'
import styles from './OrderConfirmModal.module.scss'

interface Props {
    /** 상품명 */
    productName: string
    /** 주문 수량 */
    quantity: number
    /** 상품 단가 */
    price: number
    /** 처리 중 여부 */
    loading: boolean
    /** 확인 버튼 클릭 콜백 */
    onConfirm: () => void
    /** 취소 버튼 클릭 콜백 */
    onCancel: () => void
}

/**
 * 주문하기 클릭 시 표시되는 주문 확인 모달.
 * ProductsPage 전용 컴포넌트.
 * - ESC 키로 닫기 지원
 * - 열릴 때 취소 버튼으로 포커스 이동
 */
export default function OrderConfirmModal({ productName, quantity, price, loading, onConfirm, onCancel }: Props) {
    const totalPrice = price * quantity
    const cancelBtnRef = useRef<HTMLButtonElement>(null)

    useEffect(() => {
        cancelBtnRef.current?.focus()

        function handleKeyDown(e: KeyboardEvent) {
            if (e.key === 'Escape') onCancel()
        }
        document.addEventListener('keydown', handleKeyDown)
        return () => document.removeEventListener('keydown', handleKeyDown)
    }, [onCancel])

    return (
        <div className={styles.backdrop} onClick={onCancel}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="modal-title">
                <h2 className={styles.title} id="modal-title">주문 확인</h2>

                <div className={styles.infoList}>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>상품명</span>
                        <span className={styles.infoValue}>{productName}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>주문 수량</span>
                        <span className={styles.infoValue}>{quantity.toLocaleString()}개</span>
                    </div>
                    <div className={styles.divider} />
                    <div className={styles.infoRow}>
                        <span className={styles.totalLabel}>총 주문 금액</span>
                        <span className={styles.totalValue}>{totalPrice.toLocaleString()}원</span>
                    </div>
                </div>

                <div className={styles.actions}>
                    <button ref={cancelBtnRef} className={styles.cancelBtn} onClick={onCancel} disabled={loading}>
                        취소
                    </button>
                    <button className={styles.confirmBtn} onClick={onConfirm} disabled={loading}>
                        {loading ? '처리 중...' : '확인'}
                    </button>
                </div>
            </div>
        </div>
    )
}
