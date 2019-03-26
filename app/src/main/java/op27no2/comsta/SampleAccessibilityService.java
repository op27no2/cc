package op27no2.comsta;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by CristMac on 11/1/17.
 */

public class SampleAccessibilityService extends AccessibilityService {
    private ArrayList<String> mTitles = new ArrayList<String>();
    private ArrayList<String> mActiveTitles = new ArrayList<String>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source != null && event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            System.out.println("clicked called");

            if(getApplicationContext() !=null) {
                if(prefs.getBoolean("service_active", false) ==true && prefs.getBoolean("generate_mode",false) == false) {
                    System.out.println("get message service is active??");
                    ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", getMessage());
                    clipboard.setPrimaryClip(clip);
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                    source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    source.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                }
            }
        }
    }


    @Override
    public void onServiceConnected()
    {
        prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        System.out.println("access service connected");
    }


    @Override
    public void onInterrupt() {

    }

    private String getMessage(){
        System.out.println("service get message");

        Util mUtil = new Util(this);
        mActiveTitles.clear();
        mTitles = (mUtil.getArray("titles"));
        for(int i=0; i<mTitles.size(); i++){
            if(mUtil.getBoolean(mTitles.get(i)+"isActive")){
                mActiveTitles.add(mTitles.get(i));
            }
        }
        System.out.println("service active titles: "+mActiveTitles);


        Random r1 = new Random();
        int size = mActiveTitles.size();
        String mString = "";

        if(size !=0) {

            int list = r1.nextInt(size);

            System.out.println("data list: "+mActiveTitles.get(list));


            BigListModel mObject = mUtil.getSavedObjectFromPreference(this, mActiveTitles.get(list), BigListModel.class);
            ArrayList<ArrayList<String>> mData = mObject.getData();
            System.out.println("data click: "+mData);

            //int numSegments = mUtil.getInt(mActiveTitles.get(list) + "segments");
            //System.out.println("number of segments"+numSegments);

            //for (int j = 0; j < numSegments; j++) {
            for (int j = 0; j < mData.size(); j++) {
                //ArrayList<String> mSegment = mUtil.getArray(mActiveTitles.get(list) + j);
                ArrayList<String> mSegment = mData.get(j);

                if (mSegment.size() != 0 && mSegment != null) {
                    mString = mString + mSegment.get(r1.nextInt(mSegment.size()))+" ";
                    System.out.println("string: "+mString);

                }
            }


        }
        System.out.println("titles"+mTitles);
        // System.out.println("active titles"+mActiveTitles);


        return mString;
    }


}