import client from './client'

export type OrderStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED'

export interface Order {
    id: number
    userId: string
    productId: number
    productName: string
    quantity: number
    totalAmount: number
    status: OrderStatus
    failureReason?: string
    createdAt: string
}

export interface CreateOrderRequest {
    productId: number
    quantity: number
}

export async function createOrder(data: CreateOrderRequest): Promise<Order> {
    const response = await client.post<Order>('/orders', data)
    return response.data
}

export async function getOrders(): Promise<Order[]> {
    const response = await client.get<Order[]>('/orders')
    return response.data
}

export async function getOrder(id: number): Promise<Order> {
    const response = await client.get<Order>(`/orders/${id}`)
    return response.data
}
