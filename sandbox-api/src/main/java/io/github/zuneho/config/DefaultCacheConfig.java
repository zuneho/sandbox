package io.github.zuneho.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@EnableCaching(proxyTargetClass = true)
@Configuration
public class DefaultCacheConfig {
    public static final String DEFAULT_CACHE_MANAGER = "defaultCacheManager";
    public static final String THREE_MINUTES_CACHE_MANAGER = "threeMinutesCacheManager";
    public static final String FIVE_MINUTES_CACHE_MANAGER = "fiveMinutesCacheManager";
    public static final String TEN_MINUTES_CACHE_MANAGER = "tenMinutesCacheManager";

    //************************** CACHE TTL **************************
    private static final long DEFAULT_CACHE_TIMEOUT = 60 * 30; // 30분
    private static final long DEFAULT_EXPIRE_TIME_3_MINUTES = 60 * 3;
    private static final long DEFAULT_EXPIRE_TIME_5_MINUTES = 60 * 5;
    private static final long DEFAULT_EXPIRE_TIME_10_MINUTES = 60 * 10;

    @Primary
    @Bean(name = DEFAULT_CACHE_MANAGER)
    public CacheManager defaultCacheManager() {
        return createCaffeineCacheManager(DEFAULT_CACHE_TIMEOUT);
    }

    @Bean(name = THREE_MINUTES_CACHE_MANAGER)
    public CacheManager threeMinutesCacheManager() {
        return createCaffeineCacheManager(DEFAULT_EXPIRE_TIME_3_MINUTES);
    }

    @Bean(name = FIVE_MINUTES_CACHE_MANAGER)
    public CacheManager fiveMinutesCacheManager() {
        return createCaffeineCacheManager(DEFAULT_EXPIRE_TIME_5_MINUTES);
    }

    @Bean(name = TEN_MINUTES_CACHE_MANAGER)
    public CacheManager tenMinutesCacheManager() {
        return createCaffeineCacheManager(DEFAULT_EXPIRE_TIME_10_MINUTES);
    }

    private CacheManager createCaffeineCacheManager(long expireSeconds) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .recordStats());
        return cacheManager;
    }

    //cacheManager 별 resolver 할당
    @Bean(name = "defaultCacheResolver")
    public CacheResolver defaultCacheResolver() {
        return new ModelHashCacheResolver() {
            @Override
            protected CacheManager getCacheManager() {
                return defaultCacheManager();
            }
        };
    }

    @Bean(name = "threeMinutesCacheResolver")
    public CacheResolver threeMinutesCacheResolver() {
        return new ModelHashCacheResolver() {
            @Override
            protected CacheManager getCacheManager() {
                return threeMinutesCacheManager();
            }
        };
    }

    @Bean(name = "fiveMinutesCacheResolver")
    public CacheResolver fiveMinutesCacheResolver() {
        return new ModelHashCacheResolver() {
            @Override
            protected CacheManager getCacheManager() {
                return fiveMinutesCacheManager();
            }
        };
    }

    @Bean(name = "tenMinutesCacheResolver")
    public CacheResolver tenMinutesCacheResolver() {
        return new ModelHashCacheResolver() {
            @Override
            protected CacheManager getCacheManager() {
                return tenMinutesCacheManager();
            }
        };
    }

    @Slf4j
    public static abstract class ModelHashCacheResolver implements CacheResolver {

        private final ConcurrentHashMap<String, String> cacheNameMap = new ConcurrentHashMap<>();

        protected abstract CacheManager getCacheManager();

        @Override
        public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
            Method method = context.getMethod();
            Class<?> returnType = method.getReturnType();
            Type genericReturnType = method.getGenericReturnType();
            Class<?> modelClass = extractModelClass(returnType, genericReturnType); // 응답 모델 클래스 결정 (컬렉션/배열/옵셔널 등 처리)

            String baseName = method.getName(); // 메소드 이름을 기본 캐시 이름 으로 사용

            String cacheName = getCacheName(baseName, modelClass);
            return Collections.singleton(getCacheManager().getCache(cacheName));
        }

        /**
         * 타입 에서 실제 모델 클래스 를 추출 합니다.
         * 컬렉션, 배열, Optional 등의 타입을 처리 합니다.
         */
        private Class<?> extractModelClass(Class<?> returnType, Type genericReturnType) {
            if (Collection.class.isAssignableFrom(returnType)) { // 컬렉션 타입 처리 (List, Set 등)
                if (genericReturnType instanceof ParameterizedType paramType) {
                    Type[] typeArgs = paramType.getActualTypeArguments();

                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        return (Class<?>) typeArgs[0];
                    }
                }
            }
            else if (returnType.isArray()) { // 배열 타입 처리
                return returnType.getComponentType();
            }
            else if (Map.class.isAssignableFrom(returnType)) { // Map 타입 처리 (값 타입 사용)
                if (genericReturnType instanceof ParameterizedType paramType) {
                    Type[] typeArgs = paramType.getActualTypeArguments();

                    if (typeArgs.length > 1 && typeArgs[1] instanceof Class) {
                        return (Class<?>) typeArgs[1];
                    }
                }
            }
            else if (Optional.class.isAssignableFrom(returnType)) { // Optional 타입 처리
                if (genericReturnType instanceof ParameterizedType paramType) {
                    Type[] typeArgs = paramType.getActualTypeArguments();

                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        return (Class<?>) typeArgs[0];
                    }
                }
            }
            else if (genericReturnType instanceof ParameterizedType paramType) { // 기타 제네릭 타입 처리 (ResponseEntity, Page 등)
                Type[] typeArgs = paramType.getActualTypeArguments();

                if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                    return (Class<?>) typeArgs[0];
                }
            }
            return returnType; // 제네릭 이 아닌 일반 타입
        }

        /**
         * 캐시 기본 이름과 모델 클래스를 기반으로 버전이 포함된 캐시 이름을 생성합니다.
         */
        public String getCacheName(String baseName, Class<?> modelClass) {
            if (baseName == null || baseName.isEmpty() || modelClass == null) {
                throw new IllegalArgumentException("Cache name prefix and model class must not be empty");
            }

            return cacheNameMap.computeIfAbsent(
                    baseName + ":" + modelClass.getName(),
                    key -> {
                        String cacheNameWithVersion = baseName + "_v" + calculateModelHash(modelClass);
                        log.info("Generated cache name: {} for model: {}", cacheNameWithVersion, modelClass.getName());
                        return cacheNameWithVersion;
                    }
            );
        }

        /**
         * 모델 클래스 의 구조를 기반 으로 해시값 을 계산 합니다.
         */
        private String calculateModelHash(Class<?> modelClass) {
            StringBuilder structureBuilder = new StringBuilder();

            // 클래스 이름 포함 (패키지 포함)
            structureBuilder.append(modelClass.getName());

            // 모든 필드를 이름 순으로 정렬 하여 해시 계산에 포함
            Field[] fields = modelClass.getDeclaredFields();
            Arrays.sort(fields, Comparator.comparing(Field::getName));

            for (Field field : fields) {
                // static 필드는 제외, 나머지 필드 정보 추가
                if (!Modifier.isStatic(field.getModifiers())) {
                    structureBuilder.append(field.getName())
                            .append(":")
                            .append(field.getType().getName())
                            .append(":")
                            .append(Modifier.isFinal(field.getModifiers()) ? "final" : "non-final")
                            .append(";");
                }
            }

            // 해시값 계산 (음수가 나오지 않도록 절대값 사용)
            return String.valueOf(Math.abs(structureBuilder.toString().hashCode()));
        }
    }
}