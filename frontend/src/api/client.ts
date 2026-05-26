import axios from 'axios'

const client = axios.create({
    baseURL: '/',
    headers: {
        'Content-Type': 'application/json',
    },
})

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

// 응답 인터셉터: 401 시 로그아웃 처리
client.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default client
