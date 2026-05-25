import client from './client'

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'

export interface Payment {
    id: number
    orderId: number
    amount: number
    status: PaymentStatus
    createdAt: string
}

export async function getPaymentByOrderId(orderId: number): Promise<Payment> {
    const response = await client.get<Payment>(`/payments/${orderId}`)
    return response.data
}
