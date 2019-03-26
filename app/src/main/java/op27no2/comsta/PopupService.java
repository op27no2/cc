package op27no2.comsta;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.rilixtech.materialfancybutton.MaterialFancyButton;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by CristMac on 11/1/17.
 */

public class PopupService extends Service {
    private WindowManager mWindowManager;
    private View mChatHeadView;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PopupAdapter mAdapter;
    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();
    private ArrayList<ListModel> mModelTitles = new ArrayList<ListModel>();

    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private SwitchButton mSwitch;
    private ImageView expandButton;
    private MaterialFancyButton generateButton;
    private ArrayList<String> mActiveTitles = new ArrayList<String>();
    private String LOGTAG = "accessibility error: ";

    public PopupService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

/*        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);*/

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(600,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the chat head position
        //Initially view will be added to top-left corner
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);

        mUtil = new Util(this);
        mTitles = (mUtil.getArray("titles"));

        for(int i=0;i<mTitles.size();i++){
            ListModel modelTitle = new ListModel(this);
            modelTitle.setTitle(mTitles.get(i));
            modelTitle.setSelected(mUtil.getBoolean(mTitles.get(i)+"isActive"));
            mModelTitles.add(modelTitle);
        }

        mRecyclerView = (RecyclerView) mChatHeadView.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PopupAdapter(mModelTitles);
        mRecyclerView.setAdapter(mAdapter);

        //Set the close button.
        ImageView closeButton = (ImageView) mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close the service and remove the chat head from the window
                stopSelf();
            }
        });

        mSwitch = mChatHeadView.findViewById(R.id.switch_button);
        mSwitch.setChecked(prefs.getBoolean("service_active",false));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    if(isAccessibilityEnabled() == true || prefs.getBoolean("generate_mode", false) == true) {

                        // activate service - well, accessibility service always active?
                        edt.putBoolean("service_active", true);
                        edt.commit();

                        if (prefs.getBoolean("generate_mode", false) == true) {
                            Toast.makeText(mContext, "Copy/Paste mode active - this switch does nothing in this mode - be sure to launch popup and press generate", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    //deactivate service and clear clipboard
                    ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", "");
                    clipboard.setPrimaryClip(clip);

                    stopService(new Intent(PopupService.this, SampleAccessibilityService.class));
                    edt.putBoolean("service_active", false);
                    edt.commit();
                }
            }
        });

        expandButton = (ImageView) mChatHeadView.findViewById(R.id.expand);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRecyclerView.getVisibility()==View.VISIBLE) {
                    mRecyclerView.setVisibility(View.GONE);
                    expandButton.setRotation(270);
                }else{
                    mRecyclerView.setVisibility(View.VISIBLE);
                    expandButton.setRotation(0);
                }
            }
        });

        generateButton = mChatHeadView.findViewById(R.id.generate_button);
        if(prefs.getBoolean("generate_mode", false) == true){
            generateButton.setVisibility(View.VISIBLE);
        }else{
            generateButton.setVisibility(View.GONE);
        }
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", getMessage());
                clipboard.setPrimaryClip(clip);

            }
        });


//Drag and move chat head using user's touch action.
        final RelativeLayout chatHeadImage = (RelativeLayout) mChatHeadView.findViewById(R.id.chat_head_root);
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.
                            Intent intent = new Intent(PopupService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            //close the service and remove the chat heads
                            stopSelf();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }


    private String getMessage(){
        Util mUtil = new Util(this);
        mActiveTitles.clear();
        mTitles = (mUtil.getArray("titles"));
        for(int i=0; i<mTitles.size(); i++){
            if(mUtil.getBoolean(mTitles.get(i)+"isActive")){
                mActiveTitles.add(mTitles.get(i));
            }
        }

        Random r1 = new Random();
        int size = mActiveTitles.size();
        String mString = "";

        if(size !=0) {

            int list = r1.nextInt(size);

            System.out.println("data list: "+mActiveTitles.get(list));


            BigListModel mObject = mUtil.getSavedObjectFromPreference(this, mActiveTitles.get(list), BigListModel.class);
            ArrayList<ArrayList<String>> mData = mObject.getData();
            System.out.println("data click: "+mData);


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


    public boolean isAccessibilityEnabled(){
        int accessibilityEnabled = 0;
        final String MY_ACCESSIBILITY_SERVICE = "op27no2.comsta.SampleAccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(LOGTAG, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOGTAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled==1){
            Log.d(LOGTAG, "***ACCESSIBILIY IS ENABLED***: ");
            accessibilityFound = true;

            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d(LOGTAG, "Setting: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    Log.d(LOGTAG, "Setting: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(MY_ACCESSIBILITY_SERVICE)){
                        Log.d(LOGTAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }

            Log.d(LOGTAG, "***END***");
        }
        else{
            Log.d(LOGTAG, "***ACCESSIBILIY IS DISABLED***");

        }
        return accessibilityFound;
    }



}