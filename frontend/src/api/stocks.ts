import client from './client'

export interface Stock {
    id: number
    productId: number
    productName: string
    quantity: number
}

export async function getStocks(): Promise<Stock[]> {
    const response = await client.get<Stock[]>('/stocks')
    return response.data
}

/**
 * 재고 추가 API — stock-service에 PUT /stocks/{productId}/quantity 구현 예정
 * 현재 백엔드 미구현 상태이므로 호출 시 에러가 발생할 수 있음
 */
export async function addStock(productId: number, quantity: number): Promise<Stock> {
    const response = await client.put<Stock>(`/stocks/${productId}/quantity`, { quantity })
    return response.data
}
