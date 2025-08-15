package com.veeo.common.query;


import com.baomidou.mybatisplus.core.metadata.OrderItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class QueryDTO {

    /** 根条件组（默认为AND逻辑）*/
    private List<QueryCondition> conditions = new ArrayList<>();

    /** 排序条件 */
    private List<OrderItem> orders = new ArrayList<>();

    /** 分页信息 */
//    private PageParam page;

    public void addConditions(QueryCondition... conditions) {
        this.conditions.addAll(Arrays.asList(conditions));
    }

    public void addOrders(OrderItem... orders) {
        this.orders.addAll(Arrays.asList(orders));
    }

}
