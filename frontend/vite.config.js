import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        proxy: {
            '/auth': 'http://localhost:8080',
            '/orders': 'http://localhost:8080',
            '/payments': 'http://localhost:8080',
            '/stocks': 'http://localhost:8080',
        },
    },
})
