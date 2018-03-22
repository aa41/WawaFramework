package com.duiba.component_user;

import android.test.AndroidTestCase;

import org.junit.Before;
import org.robolectric.shadows.ShadowLog;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: jintai
 * @time: 2018/3/22-14:00
 * @Email: jintai@qccr.com
 * @desc:基础的网络单元测试
 */
public class BaseNetUnitTest extends AndroidTestCase{
    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        initRxJava2();
    }

    private void initRxJava2() {
        RxJavaPlugins.reset();
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.reset();
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

}
