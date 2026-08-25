package com.dlbyy.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量操作通用请求体
 */
@Data
public class BatchIds {
    private List<Long> ids;
}
