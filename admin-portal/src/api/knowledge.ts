import request from '@/utils/request'

export interface BookIngestionRequest {
    bookId?: number
    bookTitle?: string
    category?: string
    tags?: string[]
    content: string
    splitStrategy: 'PARAGRAPH' | 'FIXED_SIZE' | 'CHAPTER_MARKER'
    chunkSize?: number
    chunkOverlap?: number
    autoVectorize?: boolean
}

export interface TextChunk {
    content: string
    metadata: Record<string, any>
    tokens: number
}

export interface BookIngestionResult {
    bookId: number
    totalChunks: number
    chunks: TextChunk[]
    successCount: number
    failedCount: number
    vectorizedCount: number
    previewChunks?: TextChunk[]
}

export function ingestBook(data: BookIngestionRequest) {
    return request<any, BookIngestionResult>({
        url: '/knowledge/ingest',
        method: 'post',
        data
    })
}

export function previewIngest(data: BookIngestionRequest) {
    return request<any, BookIngestionResult>({
        url: '/knowledge/ingest/preview',
        method: 'post',
        data
    })
}
