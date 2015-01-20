package com.reachlocal.mobile.liger.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.model.ToolbarItemSpec;

import java.util.List;

public class ToolbarLayout extends LinearLayout {

    private static final String PREVIEW_TOOLBAR = "[{\"callback\":\"EDGE.openMailDialog(\\\"\\\", \\\"\\\", \\\"\\\", \\\"\\\", \\\"\\\", \\\"\\\")\",\"icon\":\"f\"},{\"callback\":\"ACTIONDETAIL.gotoContactDetails()\",\"icon\":\"p\"}]";

    private List<ToolbarItemSpec> toolbarSpecs;
    private OnToolbarItemClickListener onToolbarItemClickListener;
    private OnClickListener itemClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (onToolbarItemClickListener != null) {
                ToolbarItemSpec spec = (ToolbarItemSpec) view.getTag();
                onToolbarItemClickListener.onToolbarItemClicked(spec);
            }
        }
    };

    public ToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            toolbarSpecs = ToolbarItemSpec.parseSpecArray(PREVIEW_TOOLBAR);
        }
        if (toolbarSpecs != null) {
            addToolbarItems();
        }


    }

    private void addToolbarItems() {
        if (toolbarSpecs != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            boolean isFirst = true;
            for (ToolbarItemSpec spec : toolbarSpecs) {
                if (!isFirst) {
                    inflater.inflate(R.layout.toolbar_divider, this, true);
                }
                TextView item = (TextView) inflater.inflate(R.layout.toolbar_item_icon, this, false);
                item.setText(spec.getIconGlyph());
                item.setTag(spec);

                item.setOnClickListener(itemClickListener);
                this.addView(item);

                isFirst = false;
            }
        }
    }

    public OnToolbarItemClickListener getOnToolbarItemClickListener() {
        return onToolbarItemClickListener;
    }

    public void setOnToolbarItemClickListener(OnToolbarItemClickListener onToolbarItemClickListener) {
        this.onToolbarItemClickListener = onToolbarItemClickListener;
    }

    public void setToolbarSpecs(List<ToolbarItemSpec> toolbarSpecs) {
        this.toolbarSpecs = toolbarSpecs;
        removeAllViews();
        addToolbarItems();
    }

    public interface OnToolbarItemClickListener {
        public void onToolbarItemClicked(ToolbarItemSpec item);
    }
}
