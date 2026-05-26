import client from './client'

export interface LoginRequest {
    username: string
    password: string
}

export interface LoginResponse {
    token: string
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
    const response = await client.post<LoginResponse>('/auth/login', data)
    return response.data
}
