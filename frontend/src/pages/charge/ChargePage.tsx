import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getBalance, chargeBalance } from '../../api/auth'
import styles from './ChargePage.module.scss'

// 직접 입력 input에 누적 가산할 금액 목록
const ADD_AMOUNTS = [100_000, 1_000_000, 10_000_000]

// 숫자 문자열을 천 단위 콤마로 포매팅
function formatWithComma(value: string): string {
    if (!value) return ''
    return Number(value).toLocaleString('ko-KR')
}

// 콤마/숫자 외 문자를 제거해 순수 숫자 문자열 반환
function stripNonDigits(value: string): string {
    return value.replace(/\D/g, '')
}

export default function ChargePage() {
    const queryClient = useQueryClient()
    // amount는 콤마 없는 순수 숫자 문자열로 관리
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

    function handleAmountChange(e: React.ChangeEvent<HTMLInputElement>) {
        // 숫자 외 입력은 제거하고 순수 숫자만 상태에 보관
        setAmount(stripNonDigits(e.target.value))
    }

    function handleAddAmount(addAmount: number) {
        // 현재 입력 금액에 누적 가산
        const current = parseInt(amount, 10) || 0
        setAmount(String(current + addAmount))
    }

    function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        const parsed = parseInt(amount, 10)
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
                    <p className={styles.sectionTitle}>직접 입력</p>
                    <form onSubmit={handleSubmit} className={styles.form}>
                        <div className={styles.inputWrap}>
                            <input
                                className={styles.input}
                                type="text"
                                inputMode="numeric"
                                placeholder="충전할 금액 입력"
                                value={formatWithComma(amount)}
                                onChange={handleAmountChange}
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

                    <div className={styles.quickBtns}>
                        {ADD_AMOUNTS.map((amt) => (
                            <button
                                key={amt}
                                type="button"
                                className={styles.quickBtn}
                                onClick={() => handleAddAmount(amt)}
                                disabled={isPending}
                            >
                                +{amt.toLocaleString('ko-KR')}
                            </button>
                        ))}
                    </div>
                </section>

                {successMessage && (
                    <p className={styles.successMsg}>✓ {successMessage}</p>
                )}
            </div>
        </div>
    )
}
