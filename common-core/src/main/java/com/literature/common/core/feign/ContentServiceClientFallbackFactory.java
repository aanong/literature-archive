package com.literature.common.core.feign;

import com.literature.common.core.dto.BookDTO;
import com.literature.common.core.model.ApiResponse;
import com.literature.common.core.model.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * ContentServiceClient 熔断降级工厂
 */
@Component
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(ContentServiceClientFallbackFactory.class);

    @Override
    public ContentServiceClient create(Throwable cause) {
        log.error("ContentServiceClient 调用失败，触发熔断降级", cause);
        
        return new ContentServiceClient() {
            @Override
            public ApiResponse<BookDTO> getBookById(Long bookId) {
                log.warn("getBookById 降级处理, bookId: {}", bookId);
                return ApiResponse.error(
                    ErrorCode.DOWNSTREAM_ERROR,
                    "内容服务暂时不可用",
                    null
                );
            }

            @Override
            public ApiResponse<List<BookDTO>> getBooksByIds(String bookIds) {
                log.warn("getBooksByIds 降级处理, bookIds: {}", bookIds);
                return ApiResponse.error(
                    ErrorCode.DOWNSTREAM_ERROR,
                    "内容服务暂时不可用",
                    null
                );
            }

            @Override
            public ApiResponse<Void> updateBookMetadata(Long bookId, BookDTO bookDTO) {
                log.warn("updateBookMetadata 降级处理, bookId: {}", bookId);
                return ApiResponse.error(
                    ErrorCode.DOWNSTREAM_ERROR,
                    "内容服务暂时不可用",
                    null
                );
            }
        };
    }
}
