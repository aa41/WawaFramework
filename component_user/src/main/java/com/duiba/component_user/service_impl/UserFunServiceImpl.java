package com.duiba.component_user.service_impl;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.duiba.component_base.component.user.interfaces.IUserFunObj;
import com.duiba.component_base.component.user.path.UserRouterPath;
import com.duiba.component_base.component.user.rpc.IUserFunService;
import com.duiba.component_user.bean.UserFunObj;

/**
 * @author: jintai
 * @time: 2017/11/6-19:02
 * @Email: jintai@duiba.com.cn
 * @desc:用户功能服务实现
 */
@Route(path = UserRouterPath.USER_SERVER_FUN)
public class UserFunServiceImpl implements IUserFunService {
    @Override
    public void init(Context context) {

    }

    @Override
    public IUserFunObj provideObj() {
        return new UserFunObj();
    }
}
