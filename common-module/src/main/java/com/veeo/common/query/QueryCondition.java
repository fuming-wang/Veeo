package com.veeo.common.query;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Data
public class QueryCondition implements Serializable {

    /**
     * 操作的字段名, 例如 "id" 在构建LambdaQueryWrapper时映射为Class::getId
     */
    private String field;

    /**
     * 操作类型：EQ, NE, LIKE, IN, BETWEEN, GT, LT等
     */
    private Operator operator;

    /**
     * 值（单个值、数组或范围）
     */
    private Object value;

    /**
     * 第二个值（用于BETWEEN等需要两个值的操作）
     */
    private Object secondValue;
    /**
     * 逻辑类型（AND/OR）
     */
    private Operator logicType;
    /**
     * 子条件组（用于逻辑组合）
     */
    private List<QueryCondition> children = new ArrayList<>();
    /**
     * 用来选择select字段
     */
    private List<String> selectFields = new ArrayList<>();

    /**
     * MyBatisPlus的select
     *
     * @param fields 字段列表
     * @return condition
     */
    public static QueryCondition select(String... fields) {
        QueryCondition condition = new QueryCondition();
        condition.setOperator(Operator.SELECT);
        condition.getSelectFields().addAll(Arrays.asList(fields));
        return condition;
    }

    /**
     * MyBatisPlus的and逻辑
     *
     * @return condition
     */
    public static QueryCondition and() {
        QueryCondition condition = new QueryCondition();
        condition.setLogicType(Operator.AND);
        return condition;
    }

    public static QueryCondition or() {
        QueryCondition condition = new QueryCondition();
        condition.setLogicType(Operator.OR);
        return condition;
    }

    /**
     * 判断是否是逻辑组合条件
     *
     * @return boolean
     */
    public boolean isLogicGroup() {
        return this.logicType != null;
    }

    /**
     * 判断是否是选择字段条件
     *
     * @return boolean
     */
    public boolean isSelect() {
        return operator != null && operator == Operator.SELECT;
    }

    public QueryCondition eq(String field, Object value) {
        QueryCondition condition = new QueryCondition();
        condition.setField(field);
        condition.setOperator(Operator.EQ);
        condition.setValue(value);
        this.children.add(condition);
        return this;
    }

    public QueryCondition in(String field, Collection<?> values) {
        QueryCondition condition = new QueryCondition();
        condition.setField(field);
        condition.setOperator(Operator.IN);
        condition.setValue(values);
        this.children.add(condition);
        return this;
    }

    public QueryCondition between(String field, Comparable<?> start, Comparable<?> end) {
        QueryCondition condition = new QueryCondition();
        condition.setField(field);
        condition.setOperator(Operator.BETWEEN);
        condition.setValue(start);
        condition.setSecondValue(end);
        this.children.add(condition);
        return this;
    }

    // 添加子条件
    public QueryCondition addChild(QueryCondition child) {
        this.children.add(child);
        return this;
    }

    /**
     * 操作符号枚举, 和 MyBatisPlus 的符号向对应
     */
    public enum Operator {
        EQ, NE, LIKE, NOT_LIKE, IN, NOT_IN, BETWEEN, NOT_BETWEEN, GT, GE, LT, LE, IS_NULL, IS_NOT_NULL,
        SELECT, AND, OR
    }


}