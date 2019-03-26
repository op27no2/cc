package op27no2.comsta;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by CristMac on 11/5/17.
 */

public class Util {
private Context mContext;

    public Util(Context context) {
        mContext = context;
    }



/*    public ArrayList<String> getArray(String key){
        ArrayList<String> mList = new ArrayList<String>();

        try {
            DB snappydb = DBFactory.open(mContext);
            String[] slist  =  snappydb.getArray(key, String.class);
            mList = new ArrayList<String>(Arrays.asList(slist));
            return mList;

        } catch (SnappydbException e) {
            System.out.println("snappy error: "+e.getMessage());
            e.printStackTrace();
            return mList;
        }

    }*/

    // mUtil.saveObjectToSharedPreference(getActivity(), "titles", mTitles);
    //  ArrayList mTest = mUtil.getSavedObjectFromPreference(getActivity(), "titles", ArrayList.class);
    //  System.out.println("store array test" + mTest);

    public ArrayList<String> getArray(String key){
        ArrayList<String> mList = new ArrayList<String>();

        try {
            DB snappydb = DBFactory.open(mContext);
            String[] slist  =  snappydb.getArray(key, String.class);
            mList = new ArrayList<String>(Arrays.asList(slist));
            return mList;

        } catch (SnappydbException e) {
            System.out.println("snappy error: "+e.getMessage());
            e.printStackTrace();
            return mList;
        }

    }

    public void storeArray(ArrayList<String> mList, String key){

        try {
            DB snappydb = DBFactory.open(mContext);
            String[] slist = mList.toArray(new String[mList.size()]);
            snappydb.put(key, slist);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void deleteArray(String key){

        try {
            DB snappydb = DBFactory.open(mContext);
            snappydb.del(key);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    public void storeInt(String key, Integer number){

        try {
            DB snappydb = DBFactory.open(mContext);
            snappydb.putInt(key, number);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public int getInt(String key){

        try {
            DB snappydb = DBFactory.open(mContext);
            Integer myInt = snappydb.getInt(key);
            snappydb.close();
            return myInt;

        } catch (SnappydbException e) {
            e.printStackTrace();
            return 0;
        }
    }



    public void storeBoolean(String key, Boolean active){

        try {
            DB snappydb = DBFactory.open(mContext);
            snappydb.putBoolean(key, active);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public boolean getBoolean(String key){

        try {
            DB snappydb = DBFactory.open(mContext);
            Boolean active = snappydb.getBoolean(key);
            snappydb.close();
            return active;

        } catch (SnappydbException e) {
            e.printStackTrace();
            return false;
        }
    }





    public String getListMessage(String mylist){

        Random r1 = new Random();
        String mString = "";


        BigListModel mObject = getSavedObjectFromPreference(mContext, mylist, BigListModel.class);
        ArrayList<ArrayList<String>> mData = mObject.getData();

        for (int j = 0; j < mData.size(); j++) {
            //ArrayList<String> mSegment = mUtil.getArray(mActiveTitles.get(list) + j);
            ArrayList<String> mSegment = mData.get(j);

            if (mSegment.size() != 0 && mSegment != null) {
                mString = mString + mSegment.get(r1.nextInt(mSegment.size()))+" ";
                System.out.println("string: "+mString);

            }
        }


        return mString;
    }

    public static void saveObjectToSharedPreference(Context context, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context, String preferenceKey, Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }



}
