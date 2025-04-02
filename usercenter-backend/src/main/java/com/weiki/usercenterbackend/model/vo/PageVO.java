package com.weiki.usercenterbackend.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页视图对象
 */
@Data
@ApiModel(description = "分页结果")
public class PageVO<T> {

    /**
     * 总记录数
     */
    @ApiModelProperty("总记录数")
    private long total;

    /**
     * 页码
     */
    @ApiModelProperty("当前页码")
    private long current;

    /**
     * 每页条数
     */
    @ApiModelProperty("每页条数")
    private long pageSize;

    /**
     * 数据列表
     */
    @ApiModelProperty("数据列表")
    private List<T> records;

    /**
     * 构造方法
     *
     * @param records  数据列表
     * @param total    总记录数
     * @param current  当前页码
     * @param pageSize 每页条数
     */
    public PageVO(List<T> records, long total, long current, long pageSize) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.pageSize = pageSize;
    }
} 