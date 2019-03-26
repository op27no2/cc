package op27no2.comsta;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.kyleduo.switchbutton.SwitchButton;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

import static op27no2.comsta.R.id.drawer;
import static op27no2.comsta.R.id.edit1;

//TODO generic posts populating instead of Example COmments??

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private MenuAdapter mMenuAdapter;
    private String LOGTAG = "accessibility error: ";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private ArrayList<String> mDataset= new ArrayList<String>();
    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    private SwitchButton mSwitch;
    private ImageView excelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        goToFragment(new FragmentMain(), false, "main");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mContext = this;
        prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        if(prefs.getBoolean("firstinstall", true) == true) {
            try {
                System.out.println("first install db");
                DB snappydb = DBFactory.open(this);
                snappydb.destroy();
                edt.putBoolean("firstinstall", false);
                edt.commit();
            } catch (SnappydbException e) {
                e.printStackTrace();
            }
        }

        System.out.println("service active? " + prefs.getBoolean("service_active",false));

        Util mUtil = new Util(this);
        System.out.println("mstart titles: "+mUtil.getArray("titles"));


        final DuoDrawerLayout drawerLayout = (DuoDrawerLayout) findViewById(drawer);
        DuoDrawerToggle drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        mSwitch = findViewById(R.id.switch_button);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    if(isAccessibilityEnabled() == true || prefs.getBoolean("generate_mode", false) == true) {
                        //do nothing
                        System.out.println("switching service active");
                        edt.putBoolean("service_active", true);
                        edt.commit();
                        if(prefs.getBoolean("generate_mode",false) == true){
                            Toast.makeText(mContext, "Copy/Paste mode active, be sure to launch popup",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        //start accessibility service
                        accessibilityDialog();
                        mSwitch.setChecked(false);
                    }

                }else{
                    //deactivate service and clear clipboard
                    ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", "");
                    clipboard.setPrimaryClip(clip);

                    //stopService(new Intent(mContext, SampleAccessibilityService.class));
                    edt.putBoolean("service_active", false);
                    edt.commit();
                }

            }
        });

        excelButton = (ImageView) findViewById(R.id.excel_button);

        excelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentConstruct fragment = (FragmentConstruct) getSupportFragmentManager().findFragmentByTag("construct");
                fragment.dialogExcel();
            }
        });

        TextView mUninstallText = findViewById(R.id.uninstall_text);
        mUninstallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstallDialog();
            }
        });

        RelativeLayout mLayout = findViewById(R.id.uninstall_feedback);
        if(prefs.getBoolean("hide_uninstall", false) == true) {
            mLayout.setVisibility(View.GONE);
        }

        ImageView mHide = findViewById(R.id.close_btn);
        mHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout mLayout = findViewById(R.id.uninstall_feedback);
                mLayout.setVisibility(View.GONE);
                edt.putBoolean("hide_uninstall",true);
                edt.commit();
            }
        });


        final ArrayList<String> options = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.options)));

        DuoMenuView duoMenuView = (DuoMenuView) findViewById(R.id.menu);
        final MenuAdapter menuAdapter = new MenuAdapter(options);
        duoMenuView.setAdapter(menuAdapter);
        duoMenuView.setOnMenuClickListener(new DuoMenuView.OnMenuClickListener() {
            @Override
            public void onFooterClicked() {
                // If the footer view contains a button
                // it will launch this method on the button click.
                // If the view does not contain a button it will listen
                // to the root view click.
            }

            @Override
            public void onHeaderClicked() {

            }

            @Override
            public void onOptionClicked(int position, Object objectClicked) {
                // Set the toolbar title
                setTitle(options.get(position));

                // Set the right options selected
                menuAdapter.setViewSelected(position, true);

                // Navigate to the right fragment
                switch (position) {
                    case 0:
                    goToFragment(new FragmentMain(), false, "main");
                    break;

                    case 1:
                        goToFragment(new FragmentConstruct(), false, "construct");
                        break;
                    case 2:
                        goToFragment(new FragmentPremium(), false, "premium");
                        break;
                    case 3:
                        goToFragment(new FragmentSettings(), false, "settings");
                        break;
                    case 4:
                        goToFragment(new FragmentHelp(), false, "help");
                        break;

                    default:
                           goToFragment(new FragmentMain(), false, "main");
                        break;
                }

                drawerLayout.closeDrawer();

            }

        });


        if(prefs.getBoolean("firsst_main", true) == true){
            System.out.println("accessibility enable: " + isAccessibilityEnabled());
            if (isAccessibilityEnabled() == true) {
                //do nothing
            } else {
                //start accessibility service
            //    accessibilityDialog();
                //dont need to do this since first onResume gives setChecked(false)
            }
            if (isPopupEnabled() == true) {
                //do nothing
            } else {
                //start accessibility service
                popupDialog();
            }


            tutorialDialog();

            edt.putBoolean("firsst_main",false);
            edt.commit();
        }



        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);



/*        WriteExcel test = new WriteExcel();
        test.setContext(this);

        try {
            test.write("testexcel.xls");
        } catch (IOException e) {
            System.out.println("fail io" +e.getMessage());
            e.printStackTrace();
        } catch (WriteException e) {
            System.out.println("fail write" +e.getMessage());
            e.printStackTrace();
        }*/


    }

    private void initializeView() {
        MaterialFancyButton loadButton = (MaterialFancyButton) findViewById(R.id.start_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("button clicked");
                startService(new Intent(MainActivity.this, PopupService.class));
                startService(new Intent(MainActivity.this, SampleAccessibilityService.class));
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwitch.setChecked(prefs.getBoolean("service_active",false));


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            System.out.println("resultCode: "+resultCode);
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
               // initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

               // finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    private void goToFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        int commit = transaction.replace(R.id.container, fragment, tag).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        

        return super.onOptionsItemSelected(item);
    }


    public void accessibilityDialog()
    {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_delete);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.confirm_title);
        mText.setText("Comsta has one-click paste options that help users with limited motor function, but are also very convenient for all users. Please navigate to settings to turn on Accessibility access for Comsta (or do this later!). If you do not wish to use this feature you must enable copy/paste mode in settings.");

        Button mButton = dialog.findViewById(R.id.confirm);
        mButton.setText("Proceed");
        dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void popupDialog()
    {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_delete);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.confirm_title);
        mText.setText("Comsta has an optional popup overlay feature. To use the feature please enable the permission for Comsta (or do this later!)");

        Button mButton = dialog.findViewById(R.id.confirm);
        mButton.setText("Proceed");
        dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);

                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

    private boolean isPopupEnabled(){
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            return false;
        } else {
            //do nothing
            return true;
        }
    }

    private void tutorialDialog(){
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tutorial);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText mEdit = dialog.findViewById(edit1);
        if(prefs.getBoolean("service_active", false) == false) {
            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEdit.setText(getTutorialMessage());
                }
            });
        }


        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private String getTutorialMessage(){
        String mString = "";

        Util mUtil = new Util(this);
        System.out.println("tutorial titles: "+mUtil.getArray("titles"));

        BigListModel mObject = mUtil.getSavedObjectFromPreference(this, "Cool Post Comments", BigListModel.class);
        ArrayList<ArrayList<String>> mData = mObject.getData();
        System.out.println("data click: "+mData);

        Random r1 = new Random();

        //for (int j = 0; j < numSegments; j++) {
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


    private void uninstallDialog(){
        final Dialog dialog = new Dialog(this);



        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_uninstall);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText mEdit = dialog.findViewById(edit1);


        Button myButton1 = dialog.findViewById(R.id.impression);
        myButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logReason("Wrong Impression");
                dialog.dismiss();
                Toast.makeText(mContext, "Thank You For Your Feedback, Please Considering Waiting For Updates As I Develop Features", Toast.LENGTH_LONG).show();
            }
        });
        Button myButton2 = dialog.findViewById(R.id.understand);
        myButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logReason("Don't Understand");
                dialog.dismiss();
                Toast.makeText(mContext, "Thank You For Your Feedback, Please Considering Waiting For Updates As I Develop Features", Toast.LENGTH_LONG).show();
            }
        });
        Button myButton3 = dialog.findViewById(R.id.content);
        myButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logReason("Needs Content");
                dialog.dismiss();
                Toast.makeText(mContext, "Thank You For Your Feedback, Please Considering Waiting For Updates As I Develop Features", Toast.LENGTH_LONG).show();
            }
        });
        Button myButton4 = dialog.findViewById(R.id.features);
        myButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logReason("Needs Features");
                dialog.dismiss();
                Toast.makeText(mContext, "Thank You For Your Feedback, Please Considering Waiting For Updates As I Develop Features", Toast.LENGTH_LONG).show();
            }
        });
        Button myButton5 = dialog.findViewById(R.id.errors);
        myButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logReason("Too Many Errors");
                dialog.dismiss();
                Toast.makeText(mContext, "Thank You For Your Feedback, Please Considering Waiting For Updates As I Develop Features", Toast.LENGTH_LONG).show();
            }
        });
        Button myButton6 = dialog.findViewById(R.id.cancel);
        myButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void logReason(String reason){
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(reason)
                .putContentType(reason)
                .putContentId(reason));

        RelativeLayout mLayout = findViewById(R.id.uninstall_feedback);
        edt.putBoolean("hide_uninstall",true);
        edt.commit();
        mLayout.setVisibility(View.GONE);
    }

}
