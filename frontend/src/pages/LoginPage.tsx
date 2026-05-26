import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import styles from './LoginPage.module.scss'

export default function LoginPage() {
    const navigate = useNavigate()
    const { setAuth } = useAuthStore()
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const [loading, setLoading] = useState(false)

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setLoading(true)

        try {
            const { token } = await login({ username, password })
            setAuth(token, username)
            navigate('/')
        } catch {
            // 전역 Axios 인터셉터가 window.alert()로 처리
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className={styles.page}>
            <div className={styles.card}>
                <h1 className={styles.title}>MSA Commerce</h1>
                <p className={styles.subtitle}>계속하려면 로그인하세요.</p>

                <form className={styles.form} onSubmit={handleSubmit}>
                    <div className={styles.fieldGroup}>
                        <label className={styles.label} htmlFor="username">사용자명</label>
                        <input
                            id="username"
                            className={styles.input}
                            type="text"
                            placeholder="사용자명 입력"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            autoComplete="username"
                            required
                        />
                    </div>

                    <div className={styles.fieldGroup}>
                        <label className={styles.label} htmlFor="password">패스워드</label>
                        <input
                            id="password"
                            className={styles.input}
                            type="password"
                            placeholder="패스워드 입력"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            autoComplete="current-password"
                            required
                        />
                    </div>

                    <button className={styles.submitBtn} type="submit" disabled={loading}>
                        {loading ? '로그인 중...' : '로그인'}
                    </button>
                </form>

                <p className={styles.hint}>
                    테스트 계정: <code>test</code> / <code>test</code>
                </p>
            </div>
        </div>
    )
}
