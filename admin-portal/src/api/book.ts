import request from '@/utils/request'

export interface Book {
    id: string
    title: string
    author: string
    edition: string
    status: string
    category?: string
    tags?: string
    createTime?: string
    updateTime?: string
}

export interface BookListParams {
    page?: number
    pageSize?: number
    keyword?: string
    status?: string
}

export interface PageResponse<T> {
    total: number
    items: T[]
}

export function listBooks(params: BookListParams) {
    return request<any, PageResponse<Book>>({
        url: '/api/admin/books',
        method: 'get',
        params
    })
}

export function getBook(id: string) {
    return request<any, Book>({
        url: `/api/admin/books/${id}`,
        method: 'get'
    })
}

export function createBook(data: Partial<Book>) {
    return request<any, Book>({
        url: '/api/admin/books',
        method: 'post',
        data
    })
}

export function updateBook(id: string, data: Partial<Book>) {
    return request<any, Book>({
        url: `/api/admin/books/${id}`,
        method: 'put',
        data
    })
}

export function deleteBook(id: string) {
    return request<any, boolean>({
        url: `/api/admin/books/${id}`,
        method: 'delete'
    })
}

export function importChapters(id: string, content: string) {
    return request<any, void>({
        url: `/api/admin/books/${id}/chapters/import`,
        method: 'post',
        headers: {
            'Content-Type': 'text/plain'
        },
        data: content
    })
}
