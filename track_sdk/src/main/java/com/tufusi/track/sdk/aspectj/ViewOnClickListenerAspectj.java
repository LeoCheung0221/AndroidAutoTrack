package com.tufusi.track.sdk.aspectj;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;

import com.tufusi.track.sdk.TufusiDataPrivate;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created by LeoCheung on 2020/11/27.
 *
 * @description 切面为各形式点击事件
 * <p>
 * 注：
 * 完整的切入点表达式包含如下
 * execution(<修饰符模式>?<返回类型模式><方法名模式>(<参数模式>)<异常模式>?)
 * <p>
 * 其中：
 * ☆ 带 ? 号的表示这部分是可选的
 * ☆ 修饰符模式指的是 public、protected、private
 * ☆ 异常模式指的是比如 ClassNotFoundException等等
 */
@Aspect
public class ViewOnClickListenerAspectj {

    /**
     * 正常的 View.setOnClickListener 切点
     */
    @After("execution(* android.view.View.OnClickListener.onClick(android.view.View))")
    public void onViewClickAOP(final JoinPoint joinPoint) {
        View view = (View) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClickAOP(view);
    }

    /**
     * 被ButterKnife注解的点击事件 切点
     */
    @After("execution(@butterknife.OnClick * *(android.view.View))")
    public void onButterKnifeClickAOP(final JoinPoint joinPoint) {
        View view = (View) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClickAOP(view);
    }

    /**
     * 支持被自定义注解 TufusiDataTrackViewOnClick 注解的切点
     */
    @After("execution(@com.tufusi.track.sdk.aspectj.TufusiDataTrackViewOnClick * *(android.view.View))")
    public void onTrackViewOnClickAOP(final JoinPoint joinPoint) {
        View view = (View) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClickAOP(view);
    }

    /**
     * 支持 onOptionsItemSelected(android.view.MenuItem)
     */
    @After("execution(* android.app.Activity.onOptionsItemSelected(android.view.MenuItem))")
    public void onOptionsItemSelectedAOP(final JoinPoint joinPoint) {
        MenuItem view = (MenuItem) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClick(joinPoint.getTarget(), view);
    }

    /**
     * 支持 onContextItemSelected(android.view.MenuItem)
     */
    @After("execution(* android.app.Activity.onContextItemSelected(android.view.MenuItem))")
    public void onContextItemSelectedAOP(JoinPoint joinPoint) {
        MenuItem view = (MenuItem) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClick(joinPoint.getTarget(), view);
    }

    /**
     * 支持 onMenuItemSelected(int, android.view.MenuItem)
     */
    @After("execution(* android.app.Activity.onMenuItemSelected(int, android.view.MenuItem))")
    public void onMenuItemSelectedAOP(JoinPoint joinPoint) {
        MenuItem view = (MenuItem) joinPoint.getArgs()[1];
        TufusiDataPrivate.trackViewOnClick(joinPoint.getTarget(), view);
    }

    /**
     * 支持 DialogInterface.OnClickListener.onClick(android.content.DialogInterface, int)
     */
    @After("execution(* android.content.DialogInterface.OnClickListener.onClick(android.content.DialogInterface, int))")
    public void onDialogClickAOP(final JoinPoint joinPoint) {
        DialogInterface dialogInterface = (DialogInterface) joinPoint.getArgs()[0];
        int which = (int) joinPoint.getArgs()[1];
        TufusiDataPrivate.trackViewOnClick(dialogInterface, which);
    }

    /**
     * 支持 DialogInterface.OnMultiChoiceClickListener.onClick(android.content.DialogInterface, int, boolean)
     */
    @After("execution(* android.content.DialogInterface.OnMultiChoiceClickListener.onClick(android.content.DialogInterface, int, boolean))")
    public void onDialogMultiChoiceClickAOP(final JoinPoint joinPoint) {
        DialogInterface dialogInterface = (DialogInterface) joinPoint.getArgs()[0];
        int which = (int) joinPoint.getArgs()[1];
        boolean isChecked = (boolean) joinPoint.getArgs()[2];
        TufusiDataPrivate.trackViewOnClick(dialogInterface, which, isChecked);
    }

    /**
     * 支持 CompoundButton.OnCheckedChangeListener.onCheckedChanged(android.widget.CompoundButton,boolean)
     */
    @After("execution(* android.widget.CompoundButton.OnCheckedChangeListener.onCheckedChanged(android.widget.CompoundButton,boolean))")
    public void onCheckedChangedAOP(final JoinPoint joinPoint) {
        CompoundButton compoundButton = (CompoundButton) joinPoint.getArgs()[0];
        boolean isChecked = (boolean) joinPoint.getArgs()[1];
        TufusiDataPrivate.trackViewOnClick(compoundButton, isChecked);
    }

    /**
     * 支持 RatingBar.OnRatingBarChangeListener.onRatingChanged(android.widget.RatingBar,float,boolean)
     */
    @After("execution(* android.widget.RatingBar.OnRatingBarChangeListener.onRatingChanged(android.widget.RatingBar,float,boolean))")
    public void onRatingBarChangedAOP(final JoinPoint joinPoint) {
        View view = (View) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClick(view);
    }


    /**
     * 支持 SeekBar.OnSeekBarChangeListener.onStopTrackingTouch(android.widget.SeekBar)
     */
    @After("execution(* android.widget.SeekBar.OnSeekBarChangeListener.onStopTrackingTouch(android.widget.SeekBar))")
    public void onStopTrackingTouchMethod(JoinPoint joinPoint) {
        View view = (View) joinPoint.getArgs()[0];
        TufusiDataPrivate.trackViewOnClick(view);
    }

    /**
     * 支持 AdapterView.OnItemSelectedListener.onItemSelected(android.widget.AdapterView,android.view.View,int,long)
     */
    @After("execution(* android.widget.AdapterView.OnItemSelectedListener.onItemSelected(android.widget.AdapterView,android.view.View,int,long))")
    public void onItemSelectedAOP(final JoinPoint joinPoint) {
        AdapterView<?> adapterView = (AdapterView<?>) joinPoint.getArgs()[0];
        View view = (View) joinPoint.getArgs()[1];
        int position = (int) joinPoint.getArgs()[2];
        TufusiDataPrivate.trackAdapterViewOnClick(adapterView, view, position);
    }

    /**
     * 支持 AdapterView.OnItemClickListener.onItemClick(android.widget.AdapterView,android.view.View,int,long)
     */
    @After("execution(* android.widget.AdapterView.OnItemClickListener.onItemClick(android.widget.AdapterView,android.view.View,int,long))")
    public void onAdapterViewItemClickAOP(final JoinPoint joinPoint) {
        AdapterView<?> adapterView = (AdapterView<?>) joinPoint.getArgs()[0];
        View view = (View) joinPoint.getArgs()[1];
        int position = (int) joinPoint.getArgs()[2];
        TufusiDataPrivate.trackAdapterViewOnClick(adapterView, view, position);
    }

    /**
     * 支持 TabHost.OnTabChangeListener.onTabChanged(String)
     */
    @After("execution(* android.widget.TabHost.OnTabChangeListener.onTabChanged(String))")
    public void onTabChangedAOP(final JoinPoint joinPoint) {
        String tabName = (String) joinPoint.getArgs()[0];
        Activity activity = (Activity) joinPoint.getThis();
        TufusiDataPrivate.trackTabHost(tabName, activity);
    }

    /**
     * public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
     */
    @After("execution(* android.widget.ExpandableListView.OnChildClickListener.onChildClick(android.widget.ExpandableListView, android.view.View, int, int, long))")
    public void onExpandableListViewChildClickAOP(final JoinPoint joinPoint) {
        ExpandableListView expandableListView = (ExpandableListView) joinPoint.getArgs()[0];
        View view = (View) joinPoint.getArgs()[1];
        int groupPosition = (int) joinPoint.getArgs()[2];
        int childPosition = (int) joinPoint.getArgs()[3];
        TufusiDataPrivate.trackAdapterViewOnClick(expandableListView, view, groupPosition, childPosition);
    }

    /**
     * public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l)
     */
    @After("execution(* android.widget.ExpandableListView.OnGroupClickListener.onGroupClick(android.widget.ExpandableListView, android.view.View, int, long))")
    public void onExpandableListViewGroupClickAOP(final JoinPoint joinPoint) {
        ExpandableListView expandableListView = (ExpandableListView) joinPoint.getArgs()[0];
        View view = (View) joinPoint.getArgs()[1];
        int groupPosition = (int) joinPoint.getArgs()[2];
        TufusiDataPrivate.trackAdapterViewOnClick(expandableListView, view, groupPosition, -1);
    }
} 