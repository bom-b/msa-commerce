import axios from 'axios'

const client = axios.create({
    baseURL: '/',
    headers: {
        'Content-Type': 'application/json',
    },
})

// 전역 alert을 띄우지 않을 HTTP 상태코드 (호출처가 정상 흐름으로 처리하는 경우)
const SILENT_STATUSES = new Set([404])

// 요청 인터셉터: /api 접두사 자동 추가 + JWT 토큰 자동 첨부
client.interceptors.request.use((config) => {
    if (config.url && !config.url.startsWith('/api')) {
        config.url = `/api${config.url}`
    }

    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
})

// 응답 인터셉터: 401 시 로그아웃 처리, 그 외 HTTP 에러는 백엔드 메시지를 alert으로 표시
client.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            window.location.href = '/login'
            return Promise.reject(error)
        }

        // 일부 상태코드는 호출처가 직접 처리하므로 전역 alert을 생략
        const status = error.response?.status
        if (status && SILENT_STATUSES.has(status)) {
            return Promise.reject(error)
        }

        const message: string =
            error.response?.data?.message ??
            error.response?.data?.error ??
            error.message ??
            '알 수 없는 오류가 발생했습니다.'

        window.alert(message)
        return Promise.reject(error)
    }
)

export default client
