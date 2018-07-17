package com.duiba.component_base.component;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.ViewModel;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.support.annotation.CheckResult;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.duiba.component_base.BuildConfig;
import com.duiba.component_base.application.BaseApplication;
import com.duiba.component_base.lifecycle.LifecycleTransformer;
import com.duiba.component_base.lifecycle.RxLifecycle;
import com.duiba.component_base.util.EventBusUtil;
import com.duiba.library_common.bean.Event;
import com.duiba.library_common.bean.EventCode;
import com.hannesdorfmann.mosby3.mvp.MvpActivity;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

import com.duiba.wsmanager.WsManager;
import com.duiba.wsmanager.WsManagerFactory;
import com.duiba.wsmanager.listener.AbstractWsStatusListener;

import io.reactivex.subjects.BehaviorSubject;

/**
 * @author jintai
 * @date 2018/03/23
 * @desc 基础的BaseActivity
 */
public abstract class BaseActivity<Model extends ViewModel, V extends DuibaMvpView, P extends DuibaMvpPresenter<Model, V>>
        extends MvpActivity<V, P> {
    protected final String TAG = this.getClass().getSimpleName();
    protected final String TAG_CURRENT = "CurrentActivity";


    public Model getViewModel() {
        return mViewModel;
    }

    private LifecycleRegistry mLifecycleRegistry;
    /**
     * 基础的viewModel
     */
    private Model mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //竖向
        if (isLockedPortrait()) {
            int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            if (getRequestedOrientation() != orientation) {
                setRequestedOrientation(orientation);
            }
        }

        //NO_TITLE
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //输出当前activity
        if (BuildConfig.DEBUG) {
            Logger.t(TAG_CURRENT).v("===CurrentActivity====>" + this.getClass().getCanonicalName());
        }

        //onCreat时候注册EventBus事件
        if (isRegisterEventBus()) {
            EventBusUtil.register(this);
        }
        if (isMVP()) {
            //初始化并订阅ViewModel
            subscribeViewModel();
            if (mViewModel == null) {
                throw new NullPointerException("请重写subscribeViewModel方法为mViewModel赋值并建立绑定");
            }
        }


        //初始化webSocket
        if (isOpenWebSocket()) {
            Logger.v("===========OpenWebSocket==============");
            createWSStatusListener();
            initWebSocket();
        }

        //发送生命周期
        mLifecycleRegistry = (LifecycleRegistry) getLifecycle();
        //订阅presenter
        getLifecycle().addObserver(getPresenter());

        mLifecycleRegistry.markState(Lifecycle.State.CREATED);
        lifecycleSubject.onNext(Lifecycle.State.CREATED);
    }

    public ViewModel getGlobalViewModel(String key) {
        return BaseApplication.getApplication().getGlobalViewModel(key);
    }

    /**
     * 初始化WebSocket
     */
    //public static final String WEBSOCKET_URL = "ws://echo.websocket.org";
    public static final String WEBSOCKET_URL = "ws://116.62.21.231:7878";
    protected AbstractWsStatusListener mWsAbstractStatusListener;
    private WsManager mWsManager;

    private void initWebSocket() {
        if (mWsAbstractStatusListener == null) {
            throw new NullPointerException("请在子Activity中重写createWSStatusListener方法并为mWsAbstractStatusListener赋值");
        }
        mWsManager = WsManagerFactory.createWsManager(getApplicationContext(), WEBSOCKET_URL);
        mWsManager.addWsStatusListener(mWsAbstractStatusListener);
        mWsManager.startConnect();
    }

    /**
     * 是否竖屏锁定
     *
     * @return
     */
    public boolean isLockedPortrait() {
        return false;
    }


    /**
     * 是否启用webSocket并自动打开webSocket 默认返回false
     * 如果要启用 请重写该方法并返回true
     */
    public boolean isOpenWebSocket() {
        return false;
    }

    /**
     * 如果isOpenWebSocket  一定要重写该方法 并为mWsAbstractStatusListener赋值
     */
    public void createWSStatusListener() {

    }

    /**
     * 订阅viewModel
     */
    private void subscribeViewModel() {
        mViewModel = createViewModel();
        performViewModelSubscribe(mViewModel);
    }


    /**
     * 抽象方法 创建ViewModel
     *
     * @return Model
     */
    protected abstract Model createViewModel();

    /**
     * 执行ViewModel订阅
     *
     * @param viewModel
     */
    protected abstract void performViewModelSubscribe(Model viewModel);

    /**
     * 抽象方法 是否采用mvp模式
     *
     * @return true or false
     */
    protected abstract boolean isMVP();


    /**
     * 是否注册EventBus
     */
    protected boolean isRegisterEventBus() {
        return false;
    }

    /**
     * 不要重写该方法 不设置权限为private的原因是因为 eventBus规定订阅方法必须为public
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusCome(Event<Object> event) {
        if (registerEventCode() == null) {
            throw new NullPointerException("请实现相应的registerEventCode()方法");
        }

        if (event != null && Arrays.asList(registerEventCode()).contains(event.getEventCode())) {
            receiveEvent(event);
        }
    }


    /**
     * 不要重写该方法 不设置权限为private的原因是因为 eventBus规定订阅方法必须为public
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(
            Event<Object> event) {
        if (registerEventCode() == null) {
            throw new NullPointerException("请实现相应的registerEventCode()方法");
        }

        if (event != null && Arrays.asList(registerEventCode()).contains(event.getEventCode())) {
            receiveStickyEvent(event);
        }
    }

    /**
     * isRegisterEventBus 为true时需要重写
     * 注册指定的EventCode 并接收相应的事件
     * isRegisterEventBus() 返回true时重写
     */
    public EventCode[] registerEventCode() {
        return null;
    }

    /**
     * 接收到分发到事件 当重写isRegisterEventBus并返回true时重写
     *
     * @param event 事件
     */
    public void receiveEvent(Event<Object> event) {
        if (!isRegisterEventBus()) {
            throw new RuntimeException(
                    this.getClass().getSimpleName() + "==>请重写isRegisterEventBus()方法并返回true");
        }
    }

    /**
     * 接受到分发的粘性事件 当重写isRegisterEventBus并返回true时重写
     *
     * @param event 粘性事件
     */
    public void receiveStickyEvent(Event<Object> event) {
        if (!isRegisterEventBus()) {
            throw new RuntimeException(
                    this.getClass().getSimpleName() + "==>请重写isRegisterEventBus()方法并返回true");
        }
    }

    /**
     * 显示顶部snack bar
     *
     * @param message
     * @param textColor
     * @param bgColor
     */
    protected void showSnackBar(String message, @ColorRes int textColor, @ColorRes int bgColor) {
        showSnackBar(message, textColor, bgColor, 0, 24, 0, 24);
    }

    /**
     * 显示顶部snack bar 详细
     *
     * @param message
     * @param textColor
     * @param bgColor
     * @param leftIcon
     * @param leftSizeDp
     * @param rightIcon
     * @param rightSizeDp
     */
    protected void showSnackBar(String message, @ColorRes int textColor, @ColorRes int bgColor, @ColorRes int leftIcon, int leftSizeDp, @ColorRes int rightIcon, int rightSizeDp) {
        if (mRootView == null) {
            return;
        }
        TSnackbar tSnackbar = TSnackbar.make(mRootView, message, TSnackbar.LENGTH_SHORT);
        //Size in dp - 24 is great!
        if (leftIcon != 0) {
            tSnackbar.setIconLeft(leftIcon, leftSizeDp == 0 ? 24 : leftSizeDp);
        }

        //Resize to bigger dp
        if (rightIcon != 0) {
            tSnackbar.setIconRight(rightIcon, rightSizeDp == 0 ? 24 : rightSizeDp);
        }
        tSnackbar.setIconPadding(8);
        tSnackbar.setMaxWidth(3000);
        View snackbarView = tSnackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(bgColor));
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(textColor));
        tSnackbar.show();
    }


    /**
     * 页面的根布局
     **/
    View mRootView;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        mRootView = findViewById(android.R.id.content);
        //发送生命周期时间
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
        lifecycleSubject.onNext(Lifecycle.State.RESUMED);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWsManager != null) {
            mWsManager.removeWsStatusListener(mWsAbstractStatusListener);
        }
        if (isRegisterEventBus()) {
            EventBusUtil.unRegister(this);
        }
        //发送生命周期时间
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        lifecycleSubject.onNext(Lifecycle.State.DESTROYED);
    }


    /**
     * 不建议重写此方法  只需要重写onCreatePresenter即刻
     *
     * @return P
     */
    @Deprecated
    @NonNull
    @Override
    public P createPresenter() {
        P presenter = onCreatePresenter();
        if (presenter == null) {
            return null;
        }
        getLifecycle().addObserver(presenter);
        return presenter;

    }

    /**
     * 返回自定义的Presenter
     *
     * @return P
     */
    public abstract P onCreatePresenter();

    private final BehaviorSubject<Lifecycle.State> lifecycleSubject = BehaviorSubject.create();

    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull Lifecycle.State event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }
}
