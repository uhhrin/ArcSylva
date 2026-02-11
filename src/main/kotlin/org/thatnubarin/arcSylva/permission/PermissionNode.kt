package org.thatnubarin.arcSylva.permission

enum class PermissionNode(val node: String) {
    USE("arcsylva.use"),
    FORCE("arcsylva.force"),
    CONFIRM("arcsylva.confirm"),
    BYPASS_LIMIT("arcsylva.bypass.limit"),
    ADMIN_RELOAD("arcsylva.admin.reload"),
    ADMIN_INFO("arcsylva.admin.info");
}
