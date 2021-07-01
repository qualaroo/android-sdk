package com.qualaroo.ui.render.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class ListeningCheckableGroup extends LinearLayout {

    public static final int NOTHING_SELECTED = -1;

    private int checkedItemId = NOTHING_SELECTED;
    private CompoundButton.OnCheckedChangeListener childOnCheckedChangeListener;
    private boolean protectFromCheckedChange = false;
    private OnCheckedChangeListener onCheckedChangeListener;
    private PassThroughHierarchyChangeListener passThroughListener;

    public ListeningCheckableGroup(Context context) {
        super(context);
        setOrientation(VERTICAL);
        init();
    }

    public ListeningCheckableGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        init();
    }

    private void init() {
        childOnCheckedChangeListener = new CheckedStateTracker();
        passThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(passThroughListener);
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        passThroughListener.onHierarchyChangeListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (checkedItemId != -1) {
            protectFromCheckedChange = true;
            setCheckedStateForView(checkedItemId, true);
            protectFromCheckedChange = false;
            setCheckedId(checkedItemId);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof ListeningCheckable) {
            final ListeningCheckable checkable = (ListeningCheckable) child;
            if (checkable.isChecked()) {
                protectFromCheckedChange = true;
                if (checkedItemId != -1) {
                    setCheckedStateForView(checkedItemId, false);
                }
                protectFromCheckedChange = false;
                setCheckedId(child.getId());
            }
        }

        super.addView(child, index, params);
    }

    public void check(@IdRes int id) {
        // don't even bother
        if (id != -1 && (id == checkedItemId)) {
            return;
        }

        if (checkedItemId != -1) {
            setCheckedStateForView(checkedItemId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    private void setCheckedId(@IdRes int id) {
        checkedItemId = id;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, checkedItemId);
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof RadioButton) {
            ((RadioButton) checkedView).setChecked(checked);
        }
    }

    @IdRes
    public int getCheckedId() {
        return checkedItemId;
    }

    public void clearCheck() {
        check(-1);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ListeningCheckableGroup.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof ListeningCheckableGroup.LayoutParams;
    }

    @Override
    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return ListeningCheckableGroup.class.getName();
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, float initWeight) {
            super(w, h, initWeight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @Override
        protected void setBaseAttributes(TypedArray a,
                int widthAttr, int heightAttr) {

            if (a.hasValue(widthAttr)) {
                width = a.getLayoutDimension(widthAttr, "layout_width");
            } else {
                width = WRAP_CONTENT;
            }
            
            if (a.hasValue(heightAttr)) {
                height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                height = WRAP_CONTENT;
            }
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ListeningCheckableGroup group, @IdRes int checkedId);
    }

    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // prevents from infinite recursion
            if (protectFromCheckedChange) {
                return;
            }

            protectFromCheckedChange = true;
            if (checkedItemId != -1) {
                setCheckedStateForView(checkedItemId, false);
            }
            protectFromCheckedChange = false;

            int id = buttonView.getId();
            setCheckedId(id);
        }
    }

    private class PassThroughHierarchyChangeListener implements
            ViewGroup.OnHierarchyChangeListener {
        private ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener;

        public void onChildViewAdded(View parent, View child) {
            if (parent == ListeningCheckableGroup.this && child instanceof ListeningCheckable) {
                ((ListeningCheckable) child).setOnCheckedChangeListener(childOnCheckedChangeListener);
            }
            if (onHierarchyChangeListener != null) {
                onHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            if (parent == ListeningCheckableGroup.this && child instanceof ListeningCheckable) {
                ((ListeningCheckable) child).setOnCheckedChangeListener(null);
            }

            if (onHierarchyChangeListener != null) {
                onHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
