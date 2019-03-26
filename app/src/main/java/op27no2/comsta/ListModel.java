package op27no2.comsta;

import android.content.Context;

/**
 * Created by CristMac on 11/14/17.
 */

public class ListModel {
    private boolean isSelected;
    private String name;
    private Context mContext;

    public ListModel(Context context) {
        mContext = context;
    }


    public String getTitle() {
        return name;
    }

    public void setTitle(String title) {
        this.name = title;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        Util mUtil = new Util(mContext);
        mUtil.storeBoolean(name+"isActive", selected);
        isSelected = selected;
    }
}