package io.github.zuneho.domain.common.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class QueryDslUtilTest {
//
//    @Test
//    void testGetSortWithExpressionMap() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("purchaseMoney"), Sort.Order.desc("regDate")));
//        Map<String, Expression<? extends Comparable<?>>> expressionMap = Map.of(
//                "purchaseMoney", moneyCouponPurchase.purchaseMoney,
//                "regDate", moneyCouponPurchase.regDate,
//                "userCode", member.userCode
//        );
//
//        OrderSpecifier<?> defaultOrder = new OrderSpecifier<>(Order.ASC, moneyCouponPurchase.idx);
//
//        // Act
//        OrderSpecifier<?>[] result = QueryDslUtil.getSort(pageable, expressionMap, defaultOrder);
//
//        // Assert
//        assertThat(result).hasSize(2); // 두 개의 정렬 조건
//        assertThat(result[0].getTarget().toString()).isEqualTo("moneyCouponPurchase.purchaseMoney");
//        assertThat(result[1].getTarget().toString()).isEqualTo("moneyCouponPurchase.regDate");
//    }
//
//    @Test
//    void testGetSortWithQClassList() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("userCode"), Sort.Order.desc("idx")));
//        List<EntityPathBase<?>> qClassList = List.of(moneyCouponPurchase, member);
//        OrderSpecifier<?> defaultOrder = new OrderSpecifier<>(Order.ASC, moneyCouponPurchase.idx);
//
//        // Act
//        OrderSpecifier<?>[] result = QueryDslUtil.getSort(pageable, qClassList, defaultOrder);
//
//        // Assert
//        assertThat(result).hasSize(2); // 두 개의 정렬 조건
//        assertThat(result[0].getTarget().toString()).isEqualTo("member1.userCode");
//        assertThat(result[1].getTarget().toString()).isEqualTo("moneyCouponPurchase.idx"); // 첫 번째 발견된 idx
//    }
//
//    @Test
//    void testGetSortWithDefaultOrderOnly() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());
//        Map<String, Expression<? extends Comparable<?>>> expressionMap = Map.of(
//                "purchaseMoney", moneyCouponPurchase.purchaseMoney,
//                "regDate", moneyCouponPurchase.regDate
//        );
//        OrderSpecifier<?> defaultOrder = new OrderSpecifier<>(Order.ASC, moneyCouponPurchase.idx);
//
//        // Act
//        OrderSpecifier<?>[] result = QueryDslUtil.getSort(pageable, expressionMap, defaultOrder);
//
//        // Assert
//        assertThat(result).hasSize(1); // 기본 정렬 조건만
//        assertThat(result[0].getTarget().toString()).isEqualTo("moneyCouponPurchase.idx");
//    }
//
//    @Test
//    void testGetSortWithNoMatchingProperty() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nonExistentProperty")));
//        Map<String, Expression<? extends Comparable<?>>> expressionMap = Map.of(
//                "purchaseMoney", moneyCouponPurchase.purchaseMoney,
//                "regDate", moneyCouponPurchase.regDate
//        );
//        OrderSpecifier<?> defaultOrder = new OrderSpecifier<>(Order.ASC, moneyCouponPurchase.idx);
//
//        // Act
//        OrderSpecifier<?>[] result = QueryDslUtil.getSort(pageable, expressionMap, defaultOrder);
//
//        // Assert
//        assertThat(result).hasSize(1); // 기본 정렬 조건만
//        assertThat(result[0].getTarget().toString()).isEqualTo("moneyCouponPurchase.idx");
//    }
//
//    @Test
//    void testGetSortWithEmptyQClassList() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("idx")));
//        List<EntityPathBase<?>> qClassList = List.of(); // 빈 리스트
//        OrderSpecifier<?> defaultOrder = new OrderSpecifier<>(Order.ASC, moneyCouponPurchase.idx);
//
//        // Act
//        OrderSpecifier<?>[] result = QueryDslUtil.getSort(pageable, qClassList, defaultOrder);
//
//        // Assert
//        assertThat(result).hasSize(1); // 기본 정렬 조건만
//        assertThat(result[0].getTarget().toString()).isEqualTo("moneyCouponPurchase.idx");
//    }
//
//
//    @Test
//    void testGetSortNullPointerException() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // Assert
//        assertTrue(ArrayUtils.isEmpty(QueryDslUtil.getSort(pageable, Map.of(), null)));
//
//        Map<String, Expression<? extends Comparable<?>>> expressionMap = Map.of(
//                "purchaseMoney", moneyCouponPurchase.purchaseMoney,
//                "regDate", moneyCouponPurchase.regDate
//        );
//        assertTrue(ArrayUtils.isEmpty(QueryDslUtil.getSort(null, expressionMap, null)));
//
//        assertTrue(ArrayUtils.isEmpty(QueryDslUtil.getSort(pageable, List.of(), null)));
//    }
}