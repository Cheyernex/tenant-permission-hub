package com.cmtdevsolutions.iam.common.filter;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        Long tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID no disponible en el contexto actual");
        }
        return tenantId;
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}