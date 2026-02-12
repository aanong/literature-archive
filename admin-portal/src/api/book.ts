import request from '@/utils/request'

export interface Book {
    id: number
    title: string
    author: string
    category: string
    description: string
    coverUrl: string
    status: string
}

export interface BookListParams {
    page?: number
    pageSize?: number
    keyword?: string
    status?: string
}

export interface PageResponse<T> {
    total: number
    records: T[]
}

export function listBooks(params: BookListParams) {
    return request<any, PageResponse<Book>>({
        url: '/admin/books',
        method: 'get',
        params
    })
}

export function getBook(id: number) {
    return request<any, Book>({
        url: `/admin/books/${id}`,
        method: 'get'
    })
}
