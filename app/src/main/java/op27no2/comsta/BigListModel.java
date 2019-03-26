package op27no2.comsta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by CristMac on 11/14/17.
 */

public class BigListModel {
    private String name;
    private ArrayList<ArrayList<String>> mData = new ArrayList<ArrayList<String>>();

    public BigListModel(String name,ArrayList<ArrayList<String>> mData) {
        this.name = name;
        this.mData = mData;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

    public ArrayList<ArrayList<String>> getData() {
        return mData;
    }

    public void setData(ArrayList<ArrayList<String>> data) {
        mData = data;
    }

    public String getCombos() {
        int combos = 1;
        for(int i=0; i<mData.size(); i++){
            combos = combos*mData.get(i).size();
        }
        return NumberFormat.getNumberInstance(Locale.US).format(combos);
    }


}