import client from './client'

export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    token: string
}

export interface BalanceResponse {
    userId: number
    balance: number
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
    const response = await client.post<LoginResponse>('/auth/login', data)
    return response.data
}

export async function getBalance(): Promise<BalanceResponse> {
    const response = await client.get<BalanceResponse>('/auth/balance')
    return response.data
}

export async function chargeBalance(amount: number): Promise<BalanceResponse> {
    const response = await client.post<BalanceResponse>('/auth/balance/charge', { amount })
    return response.data
}
