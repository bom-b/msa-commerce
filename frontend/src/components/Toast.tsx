import { useEffect } from 'react'
import styles from './Toast.module.scss'

/** Toast 메시지 데이터 */
export interface ToastData {
    /** 표시할 메시지 */
    message: string
    /** 액션 버튼 텍스트. 지정하면 버튼이 표시된다 */
    actionLabel?: string
    /** 액션 버튼 클릭 콜백 */
    onAction?: () => void
}

interface Props {
    toast: ToastData | null
    /** Toast 닫기 콜백 */
    onClose: () => void
    /** 자동 닫힘 시간(ms), 기본 4000 */
    duration?: number
}

/**
 * 화면 상단 중앙에 표시되는 공용 Toast 컴포넌트.
 * duration(ms) 후 자동으로 사라진다.
 * 액션 버튼이 필요한 경우 actionLabel + onAction을 ToastData에 전달한다.
 */
export default function Toast({ toast, onClose, duration = 4000 }: Props) {
    useEffect(() => {
        if (!toast) return
        const timer = setTimeout(onClose, duration)
        return () => clearTimeout(timer)
    }, [toast, duration, onClose])

    if (!toast) return null

    return (
        <div className={styles.toast} role="status" aria-live="polite">
            <span className={styles.message}>{toast.message}</span>
            {toast.actionLabel && toast.onAction && (
                <button
                    className={styles.linkBtn}
                    onClick={() => {
                        onClose()
                        toast.onAction!()
                    }}
                >
                    {toast.actionLabel}
                </button>
            )}
            <button className={styles.closeBtn} onClick={onClose} aria-label="닫기">
                ✕
            </button>
        </div>
    )
}
