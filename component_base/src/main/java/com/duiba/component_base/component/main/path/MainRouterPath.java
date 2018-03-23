package com.duiba.component_base.component.main.path;

/**
 * @author: jintai
 * @time: 2018/3/20-18:54
 * @Email: jintai@duiba.com.cn
 * @desc:
 */
public class MainRouterPath {
    private static final String MAIN_ROOT = "/main/";

    /**
     * =============================================主模块组件服务router============================================
     */
    /**
     * 用户组件page
     */
    public static final String MAIN_ACTIVITY_MAIN = MAIN_ROOT + "main_activity";

    public static final String MAIN_ACTIVITY_OTHER = MAIN_ROOT + "other_activity";

    /**
     * 用户组件资源服务
     */
    public static final String MAIN_SERVER_RES = MAIN_ROOT + "res_service";
    /**
     * 用户组件功能服务
     */
    public static final String MAIN_SERVER_FUN = MAIN_ROOT + "fun_service";
    /**
     * 用户组件控件服务
     */
    public static final String MAIN_SERVER_VIEW = MAIN_ROOT + "view_service";
    /**
     * 用户组件其他服务
     */
    public static final String MAIN_SERVER_OTHER = MAIN_ROOT + "other_service";

}
