import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getBalance, chargeBalance } from '../../api/auth'
import styles from './ChargePage.module.scss'

const QUICK_AMOUNTS = [10_000, 30_000, 50_000, 100_000]

export default function ChargePage() {
    const queryClient = useQueryClient()
    const [amount, setAmount] = useState('')
    const [successMessage, setSuccessMessage] = useState('')

    const { data: balanceData, isLoading: isBalanceLoading } = useQuery({
        queryKey: ['balance'],
        queryFn: getBalance,
    })

    const { mutate: charge, isPending } = useMutation({
        mutationFn: (chargeAmount: number) => chargeBalance(chargeAmount),
        onSuccess: (_data, chargeAmount) => {
            queryClient.invalidateQueries({ queryKey: ['balance'] })
            setSuccessMessage(`${chargeAmount.toLocaleString('ko-KR')}원이 충전되었습니다.`)
            setAmount('')
            setTimeout(() => setSuccessMessage(''), 4000)
        },
    })

    function handleQuickCharge(quickAmount: number) {
        charge(quickAmount)
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        const parsed = parseInt(amount.replace(/,/g, ''), 10)
        if (!parsed || parsed <= 0) return
        charge(parsed)
    }

    return (
        <div className={styles.page}>
            <div className={styles.container}>
                <h1 className={styles.title}>예치금 충전</h1>

                <div className={styles.balanceCard}>
                    <p className={styles.balanceLabel}>현재 잔액</p>
                    <p className={styles.balanceAmount}>
                        {isBalanceLoading ? '...' : `${(balanceData?.balance ?? 0).toLocaleString('ko-KR')}원`}
                    </p>
                </div>

                <section className={styles.section}>
                    <p className={styles.sectionTitle}>빠른 충전</p>
                    <div className={styles.quickBtns}>
                        {QUICK_AMOUNTS.map((amt) => (
                            <button
                                key={amt}
                                className={styles.quickBtn}
                                onClick={() => handleQuickCharge(amt)}
                                disabled={isPending}
                            >
                                +{amt.toLocaleString('ko-KR')}원
                            </button>
                        ))}
                    </div>
                </section>

                <section className={styles.section}>
                    <p className={styles.sectionTitle}>직접 입력</p>
                    <form onSubmit={handleSubmit} className={styles.form}>
                        <div className={styles.inputWrap}>
                            <input
                                className={styles.input}
                                type="number"
                                min={1}
                                placeholder="충전할 금액 입력"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                            />
                            <span className={styles.inputUnit}>원</span>
                        </div>
                        <button
                            type="submit"
                            className={styles.submitBtn}
                            disabled={isPending || !amount}
                        >
                            {isPending ? '충전 중...' : '충전하기'}
                        </button>
                    </form>
                </section>

                {successMessage && (
                    <p className={styles.successMsg}>✓ {successMessage}</p>
                )}
            </div>
        </div>
    )
}
