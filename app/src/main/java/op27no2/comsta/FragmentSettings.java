package op27no2.comsta;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;

/**
 * Created by CristMac on 11/4/17.
 */

public class FragmentSettings extends android.support.v4.app.Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();
    private SwitchButton mSwitch;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ImageView mExcel = getActivity().findViewById(R.id.excel_button);
        mExcel.setVisibility(View.GONE);

        /*view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });*/
        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();
        mUtil = new Util(getActivity());
        mTitles = (mUtil.getArray("titles"));

        mSwitch = view.findViewById(R.id.switch_button);
        mSwitch.setChecked(prefs.getBoolean("generate_mode", false));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    // activate service, well can't stop accessibility service?
                    edt.putBoolean("generate_mode", true);
                    edt.commit();
                }else{
                    edt.putBoolean("generate_mode", false);
                    edt.commit();
                }

            }
        });

        TextView generateText = (TextView) view.findViewById(R.id.text_one);
        generateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                explainDialog(getString(R.string.explain_generate));
            }
        });

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Fragment Settings Viewed")
                .putContentType("Settings Views")
                .putContentId("settings"));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwitch.setChecked(prefs.getBoolean("generate_mode",false));
    }


    public void explainDialog(String text)
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_delete);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((9 * width) / 10, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.confirm_title);
        mText.setText(text);

        Button mButton = dialog.findViewById(R.id.confirm);
        mButton.setText("Back");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancel).setVisibility(View.GONE);


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

}