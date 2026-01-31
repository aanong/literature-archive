package com.literature.common.core.model;

import java.util.List;

public record PageResponse<T>(long total, List<T> items) {
}
