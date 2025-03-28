package io.github.zuneho.domain.common.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class QueryDslUtil {

    public static final OrderSpecifier<?>[] EMPTY_ORDER_SPECIFIER_ARRAY = new OrderSpecifier<?>[0];

    /**
     * pageable 의 sort property 를 expressionMap 에서 매칭된 속성 으로 OrderSpecifier 배열 반환
     *
     * @param pageable      Spring pageable
     * @param expressionMap queryDsl 로 생성한 qClass 기반의 Expression
     * @param defaultOrder  sortMapping 을 못할 경우  사용 하게 될 기본 정렬
     * @return sort 내의 sort property 순서 대로 OrderSpecifier 배열
     */
    public static OrderSpecifier<?>[] getSort(Pageable pageable,
                                              Map<String, Expression<? extends Comparable<?>>> expressionMap,
                                              OrderSpecifier<?> defaultOrder
    ) {
        OrderSpecifier<?>[] parsedOrderBy = getSort(pageable, expressionMap);
        return ArrayUtils.isNotEmpty(parsedOrderBy) ? parsedOrderBy : getDefaultSort(defaultOrder);
    }

    /**
     * pageable 의 sort property 를 qClassList 에서 매칭된 속성 으로 OrderSpecifier 배열 반환
     *
     * @param pageable     Spring pageable
     * @param qClassList   QueryDSL 로 생성된 EntityPathBase 객체 목록
     * @param defaultOrder 기본 정렬
     * @return sort 내의 sort property 순서 대로 OrderSpecifier 배열
     */
    public static OrderSpecifier<?>[] getSort(Pageable pageable,
                                              List<EntityPathBase<?>> qClassList,
                                              OrderSpecifier<?> defaultOrder
    ) {
        if (pageable == null || qClassList == null || qClassList.isEmpty()) {
            return getDefaultSort(defaultOrder);
        }

        Map<String, OrderSpecifier<?>> orderByMap = new LinkedHashMap<>();
        qClassList.forEach(qClass -> Arrays.stream(getSort(pageable, qClass))
                .filter(Objects::nonNull)
                .forEach(orderSpecifier ->
                        orderByMap.putIfAbsent(extractKey(orderSpecifier.getTarget().toString()), orderSpecifier)));

        if (orderByMap.isEmpty()) {
            return getDefaultSort(defaultOrder);
        }

        OrderSpecifier<?>[] orderBy = pageable.getSort().stream()
                .map(pageableSort -> orderByMap.get(pageableSort.getProperty()))
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);

        return ArrayUtils.isNotEmpty(orderBy) ? orderBy : getDefaultSort(defaultOrder);
    }

    /**
     * pageable 의 sort property 를 expressionMap 에서 매칭된 속성 으로 OrderSpecifier 배열 반환
     *
     * @param pageable      Spring 의 pageable
     * @param expressionMap queryDsl 로 생성한 qClass 기반의 Expression
     * @return sort 내의 sort property 순서 대로 OrderSpecifier 배열
     */
    public static OrderSpecifier<?>[] getSort(Pageable pageable, Map<String, Expression<? extends Comparable<?>>> expressionMap) {
        if (pageable == null || expressionMap == null) {
            return EMPTY_ORDER_SPECIFIER_ARRAY;
        }
        return pageable.getSort().stream()
                .map(sort -> {
                    Expression<? extends Comparable<?>> expression = expressionMap.get(sort.getProperty());
                    return expression != null
                            ? new OrderSpecifier<>(sort.isAscending() ? Order.ASC : Order.DESC, expression)
                            : null;
                })
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * pageable 의 sort property 를 qClass List 에서 매칭된 속성 으로 OrderSpecifier 배열 반환
     * qClass 내에 연관된 entity field 까지는 접근 하지 않는다.
     *
     * @param pageable Spring pageable
     * @param qClass   QueryDSL 로 생성된 EntityPathBase 객체
     * @return sort 내의 sort property 순서 대로 OrderSpecifier 배열
     */
    public static <T> OrderSpecifier<?>[] getSort(Pageable pageable, EntityPathBase<T> qClass) {
        if (pageable == null || qClass == null) {
            return EMPTY_ORDER_SPECIFIER_ARRAY;
        }
        Set<String> directFields = Arrays.stream(qClass.getClass().getDeclaredFields())
                .filter(field ->
                        field.getType().equals(ComparableExpressionBase.class) ||
                                field.getType().equals(NumberPath.class) ||
                                field.getType().equals(StringPath.class) ||
                                field.getType().equals(DateTimePath.class))
                .map(Field::getName)
                .collect(Collectors.toSet());

        PathBuilder<T> pathBuilder = new PathBuilder<>(qClass.getType(), qClass.getMetadata().getName());

        return pageable.getSort().stream()
                .filter(sort -> directFields.contains(sort.getProperty()))
                .map(sort -> {
                    ComparableExpressionBase<?> expression = pathBuilder.getComparable(sort.getProperty(), Comparable.class);
                    return expression != null
                            ? new OrderSpecifier<>(sort.isAscending() ? Order.ASC : Order.DESC, expression)
                            : null;
                })
                .filter(Objects::nonNull)
                .toArray(OrderSpecifier[]::new);
    }

    private static String extractKey(String fullProperty) {
        if (fullProperty == null || !fullProperty.contains(".")) {
            return fullProperty;
        }
        return fullProperty.substring(fullProperty.lastIndexOf('.') + 1);
    }

    private static OrderSpecifier<?>[] getDefaultSort(OrderSpecifier<?> defaultOrderSpecifier) {
        if (defaultOrderSpecifier == null) {
            log.error("QueryDslUtil getSort defaultOrderSpecifier is null. stack={}", getStackTraceMethodNames(3));
            return EMPTY_ORDER_SPECIFIER_ARRAY;
            //throw new IllegalArgumentException("defaultOrderSpecifier is null");
        }
        return new OrderSpecifier<?>[]{defaultOrderSpecifier};
    }

    private static String getStackTraceMethodNames(int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace, 2, Math.min(stackTrace.length, 2 + depth))// 스택 에서 현재 메서드 까지 불 필요한 정보 제거함 getStackTrace()와 getStackTraceMethodNames() 를 제외한 호출 스택
                .map(element -> element.getClassName() + "." + element.getMethodName() + "()")
                .collect(Collectors.joining(" -> "));
    }
}
