package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheService {

    private static final String KEY_PREFIX = "eff_perms:";
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final TypeReference<List<PermisoResponse>> LIST_TYPE = new TypeReference<>() {};

    private final Optional<StringRedisTemplate> redisTemplateOpt;
    private final ObjectMapper redisObjectMapper;
    private final PermissionResolutionService resolutionService;

    private String key(Long tenantId, Long userId) {
        return KEY_PREFIX + tenantId + ":" + userId;
    }

    private boolean isRedisAvailable() {
        return redisTemplateOpt.isPresent();
    }

    public List<PermisoResponse> getEffectivePermisosCached(Long tenantId, Long userId) {
        if (!isRedisAvailable()) {
            return resolutionService.getEffectivePermisos(userId);
        }
        StringRedisTemplate redis = redisTemplateOpt.get();
        String k = key(tenantId, userId);
        try {
            String cached = redis.opsForValue().get(k);
            if (cached != null) {
                List<PermisoResponse> list = redisObjectMapper.readValue(cached, LIST_TYPE);
                log.debug("Cache HIT eff_perms {} -> {} permisos", k, list.size());
                return list;
            }
        } catch (Exception e) {
            log.warn("Error leyendo cache {}: {}", k, e.getMessage());
        }
        List<PermisoResponse> fresh = resolutionService.getEffectivePermisos(userId);
        try {
            String json = redisObjectMapper.writeValueAsString(fresh);
            redis.opsForValue().set(k, json, TTL);
            log.debug("Cache MISS eff_perms {} -> poblado con {} permisos", k, fresh.size());
        } catch (Exception e) {
            log.warn("Error escribiendo cache {}: {}", k, e.getMessage());
        }
        return fresh;
    }

    public Set<Long> getEffectivePermisoIdsCached(Long tenantId, Long userId) {
        return getEffectivePermisosCached(tenantId, userId).stream()
                .map(PermisoResponse::getId).collect(Collectors.toSet());
    }

    public Set<String> getEffectiveKeysCached(Long tenantId, Long userId) {
        return getEffectivePermisosCached(tenantId, userId).stream()
                .map(PermisoResponse::getKey).collect(Collectors.toSet());
    }

    public void evict(Long tenantId, Long userId) {
        if (!isRedisAvailable()) return;
        String k = key(tenantId, userId);
        try {
            redisTemplateOpt.get().delete(k);
            log.debug("Cache EVICT {}", k);
        } catch (Exception e) {
            log.warn("Error evict cache {}: {}", k, e.getMessage());
        }
    }

    public void evictByUserIds(Long tenantId, Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty() || !isRedisAvailable()) return;
        for (Long uid : userIds) evict(tenantId, uid);
    }
}
