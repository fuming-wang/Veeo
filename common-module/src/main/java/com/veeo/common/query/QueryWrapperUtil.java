package com.veeo.common.query;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.veeo.common.constant.CrossDataBaseConstant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class QueryWrapperUtil {

    /**
     * 将QueryCondition转换为LambdaQueryWrapper
     * QueryCondition有三重职责 1. 指明查询数据库字段 2. 指明逻辑and 或者 or 3. 在逻辑确定后执行.eq .in等选择方法
     */
    public static <T> LambdaQueryWrapper<T> convert(List<QueryCondition> conditions, DataBase dataBase) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        for (QueryCondition condition : conditions) {
            buildWrapper(wrapper, condition, dataBase);
        }
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private static <T> void buildWrapper(LambdaQueryWrapper<T> wrapper, QueryCondition condition, DataBase dataBase) {
        // 貌似可以将所有操作符放进一个枚举类中？
        if (condition.isLogicGroup()) {
            // 处理逻辑组合条件
            if (condition.getLogicType() == QueryCondition.Operator.AND) {
                wrapper.and(qw -> {
                    condition.getChildren().forEach(child -> {
                        buildWrapper(qw, child, dataBase);
                    });
                });
            } else {
                wrapper.or(qw -> {
                    condition.getChildren().forEach(child -> {
                        buildWrapper(qw, child, dataBase);
                    });
                });

            }
        } else if (condition.isSelect()) {
            // 处理字段选择
            List<SFunction<T, ?>> clowns = new ArrayList<>();
            condition.getSelectFields().forEach(filed -> clowns.add((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(filed)));
            wrapper.select(clowns);

        } else {
            // 处理普通条件
            String fieldName = condition.getField();
            switch (condition.getOperator()) {
                case EQ:
                    wrapper.eq((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), condition.getValue());
                    break;
                case NE:
                    wrapper.ne((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), condition.getValue());
                    break;
                case LIKE:
                    wrapper.like((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), condition.getValue());
                    break;
                case NOT_LIKE:
                    wrapper.notLike((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), condition.getValue());
                    break;
                case IN:
                    wrapper.in((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Collection<?>) condition.getValue());
                    break;
                case NOT_IN:
                    wrapper.notIn((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Collection<?>) condition.getValue());
                    break;
                case BETWEEN:
                    wrapper.between((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue(),
                            (Comparable<?>) condition.getSecondValue());
                    break;
                case NOT_BETWEEN:
                    wrapper.notBetween((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue(),
                            (Comparable<?>) condition.getSecondValue());
                    break;
                case GT:
                    wrapper.gt((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue());
                    break;
                case GE:
                    wrapper.ge((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue());
                    break;
                case LT:
                    wrapper.lt((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue());
                    break;
                case LE:
                    wrapper.le((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName), (Comparable<?>) condition.getValue());
                    break;
                case IS_NULL:
                    wrapper.isNull((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName));
                    break;
                case IS_NOT_NULL:
                    wrapper.isNotNull((SFunction<T, ?>) CrossDataBaseConstant.getFieldsForEntity(dataBase).get(fieldName));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + condition.getOperator());
            }
        }
    }
}

