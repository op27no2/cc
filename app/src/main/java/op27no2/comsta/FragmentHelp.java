package op27no2.comsta;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;

import static op27no2.comsta.R.id.edit1;

/**
 * Created by CristMac on 11/4/17.
 */

public class FragmentHelp extends android.support.v4.app.Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();

    String[] genericArray1;
    String[] genericArray2;
    String[] genericArray3;
    private ArrayList<ArrayList<String>> mGenericData = new ArrayList<ArrayList<String>>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        ImageView mExcel = getActivity().findViewById(R.id.excel_button);
        mExcel.setVisibility(View.GONE);

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Fragment Help Viewed")
                .putContentType("Help Views")
                .putContentId("help"));

        MaterialFancyButton loadButton = view.findViewById(R.id.feedback_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackDialog();
            }
        });

        MaterialFancyButton tutorialButton = view.findViewById(R.id.tutorial_button);
        tutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTutorial();
            }
        });


        return view;
    }





    public void storeArray(ArrayList<String> mList, String key){

        try {
            DB snappydb = DBFactory.open(getActivity());
            String[] slist = mList.toArray(new String[mList.size()]);
            snappydb.put(key, slist);
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }

    private void feedbackDialog(){
        final Dialog dialog = new Dialog(getActivity());

        Toast.makeText(getActivity(), "Switch App \"Off\" to test", Toast.LENGTH_LONG).show();


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_feedback);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);


        final EditText mEdit = dialog.findViewById(edit1);


        Button myButton1 = dialog.findViewById(R.id.cancel);
        myButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button myButton = dialog.findViewById(R.id.confirm);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","jcristapps@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Comsta Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, mEdit.getText().toString());
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                mEdit.setText("");
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showTutorial(){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + "WYQrnlm7wFA"));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + "WYQrnlm7wFA"));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

}