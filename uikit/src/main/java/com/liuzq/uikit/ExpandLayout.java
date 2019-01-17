package com.liuzq.uikit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * 可展开的Layout
 */
public class ExpandLayout extends LinearLayout implements View.OnClickListener {

    private static final String TAG = ExpandLayout.class.getSimpleName();

    private TextView tvTip;
    private ImageView ivArrow;

    private boolean isExpand = false;//是否是展开状态，默认是隐藏

    private int defaultItemCount;//一开始展示的条目数
    private final int DEFAULT_ITEM_COUNT = 2;
    private String expandText;//待展开显示的文字
    private String hideText;//待隐藏显示的文字
    private boolean useDefaultBottom;//是否使用默认的底部，默认为true使用默认的底部
    private float fontSize;
    private final float FONT_SIZE = 14;
    private int textColor;
    private final int DEFAULT_COLOR = Color.parseColor("#666666");
    private int arrowResId;
    private View bottomView;
    private boolean addedBottom;    //添加了底部视图

    public ExpandLayout(Context context) {
        this(context, null);
    }

    public ExpandLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
    }

    private void initAttr(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandLayout);
        defaultItemCount = ta.getInt(R.styleable.ExpandLayout_defaultItemCount, DEFAULT_ITEM_COUNT);
        expandText = ta.getString(R.styleable.ExpandLayout_expandText);
        hideText = ta.getString(R.styleable.ExpandLayout_hideText);
        fontSize = ta.getDimension(R.styleable.ExpandLayout_tipTextSize, UIUtils.sp2px(context, FONT_SIZE));
        textColor = ta.getColor(R.styleable.ExpandLayout_tipTextColor, DEFAULT_COLOR);
        arrowResId = ta.getResourceId(R.styleable.ExpandLayout_arrowDownImg, R.mipmap.arrow_down);
        useDefaultBottom = ta.getBoolean(R.styleable.ExpandLayout_useDefaultBottom, true);
        ta.recycle();

        setOrientation(VERTICAL);
    }

    public ExpandLayout setExpandText(String expandText) {
        this.expandText = expandText;
        return this;
    }

    public ExpandLayout setHintText(String hideText) {
        this.hideText = hideText;
        return this;
    }

    public ExpandLayout setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public ExpandLayout setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public ExpandLayout setArrowResId(int arrowResId) {
        this.arrowResId = arrowResId;
        return this;
    }

    public void setUseDefaultBottom(boolean useDefaultBottom) {
        this.useDefaultBottom = useDefaultBottom;
    }

    //使用默认底部布局
    private void setDefaultLayout() {
        bottomView = View.inflate(getContext(), R.layout.item_default_bottom, null);
    }

    //使用自定义布局
    private void setCustomLayout(int layout) {
        bottomView = View.inflate(getContext(), layout, null);
    }

    //使用自定义布局
    private void setCustomLayout(View view) {
        bottomView = view;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (useDefaultBottom) {
            build(null);
        }
    }

    /**
     * 初始化构建底部view
     * 强烈提醒：自定义布局中必须定义id为iv_arrow和tv_tip两个控件，否则报错
     */
    public void build(Object obj) {
        if (obj instanceof View) {
            setCustomLayout((View) obj);
        } else if (obj instanceof Integer) {
            setCustomLayout((Integer) obj);
        } else {
            setDefaultLayout();
        }

        if (bottomView != null) {
            ivArrow = (ImageView) bottomView.findViewById(R.id.iv_arrow);

            tvTip = (TextView) bottomView.findViewById(R.id.tv_tip);
            tvTip.getPaint().setTextSize(fontSize);
            if (!isExpand) {
                tvTip.setText(expandText);
            } else {
                tvTip.setText(hideText);
            }
            tvTip.setTextColor(textColor);
            ivArrow.setImageResource(arrowResId);

            bottomView.setOnClickListener(this);
        }


    }

    /**
     * 添加Item
     *
     * @param view
     */
    public void addItem(View view) {
        addView(view);
        int childCount = getChildCount();

        Log.e("ExpandLayout", childCount + " === " + defaultItemCount);
        if (childCount > defaultItemCount) {
            refreshUI(view);
        }
    }

    /**
     * 刷新UI
     *
     * @param view
     */
    private void refreshUI(View view) {
        view.setVisibility(GONE);//大于默认数目的先隐藏
    }

    /**
     * 判断是否添加底部布局
     * 注意：此方法必须放在添加Item之后调用，否则出错
     */
    public void justToAddBottom() {
        int childCount = getChildCount();
        //假如子控件数量小于等于默认数量，则不需要添加底部布局
        if (childCount <= defaultItemCount) {
            addedBottom = false;
        } else {
            addedBottom = true;
            addView(bottomView);
        }
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
        }
        super.setOrientation(orientation);
    }

    /**
     * 展开
     */
    private void expand() {
        int endIndex = addedBottom ? getChildCount() - 1 : getChildCount();
        for (int i = defaultItemCount; i < endIndex; i++) {
            //从默认显示条目位置以下的都显示出来
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);
        }
    }

    /**
     * 收起
     */
    private void hide() {
        int endIndex = addedBottom ? getChildCount() - 1 : getChildCount();
        for (int i = defaultItemCount; i < endIndex; i++) {
            //从默认显示条目位置以下的都隐藏
            View view = getChildAt(i);
            view.setVisibility(GONE);
        }
    }

    // 箭头的动画
    private void doArrowAnim() {
        if (isExpand) {
            // 当前是展开，将执行收起，箭头由上变为下
            ObjectAnimator.ofFloat(ivArrow, "rotation", -180, 0).start();
        } else {
            // 当前是收起，将执行展开，箭头由下变为上
            ObjectAnimator.ofFloat(ivArrow, "rotation", 0, 180).start();
        }
    }

    @Override
    public void onClick(View v) {
        toggle();
    }

    private void toggle() {
        if (isExpand) {
            //隐藏
            hide();
            tvTip.setText(expandText);
        } else {
            //展开
            expand();
            tvTip.setText(hideText);
        }
        doArrowAnim();
        isExpand = !isExpand;

        //回调
        if (mStateListener != null) {
            mStateListener.onStateChanged(isExpand);
        }
    }

    private OnStateChangeListener mStateListener;

    /**
     * 定义状态改变接口
     */
    public interface OnStateChangeListener {
        void onStateChanged(boolean isExpanded);
    }

    public void setOnStateChangeListener(OnStateChangeListener mListener) {
        this.mStateListener = mListener;
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        int endIndex = addedBottom ? getChildCount() - 1 : getChildCount();//如果是使用底部，则结束的下标是到底部之前
        for (int i = 0; i < endIndex; i++) {
            View view = getChildAt(i);
            final int position = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
