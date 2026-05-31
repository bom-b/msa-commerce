import client from './client'

export interface Stock {
    id: number
    productId: number
    productName: string
    quantity: number
    price: number
    imageName?: string
}

export async function getStocks(): Promise<Stock[]> {
    const response = await client.get<Stock[]>('/stocks')
    return response.data
}

/**
 * 재고 추가 API — stock-service의 POST /stocks/{productId}/quantity 호출
 * 바디로 추가 수량을 전달하면 갱신된 Stock을 반환한다
 */
export async function addStock(productId: number, quantity: number): Promise<Stock> {
    const response = await client.post<Stock>(`/stocks/${productId}/quantity`, { quantity })
    return response.data
}
