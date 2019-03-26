package op27no2.comsta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by CristMac on 11/4/17.
 */

public class FragmentConstruct extends android.support.v4.app.Fragment {

    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MySegmentAdapter mSegmentAdapter;
    private ArrayList<String> mSegmentDataset = new ArrayList<String>();

    private RecyclerView mListRecyclerView;
    private LinearLayoutManager mListLayoutManager;
    private MyListAdapter mListAdapter;
    private ArrayList<String> mListDataset = new ArrayList<String>();
    private ArrayList<String> mTitles = new ArrayList<String>();
    private String mTitle;
    private int mSegment=0;
    private MaterialSpinner spinner;
    private Util mUtil;
    private TextView myText;
    private int SEGMENT_LIST = 1;
    private int LIST_LIST = 2;
    private ArrayList<ArrayList<String>> mData = new ArrayList<ArrayList<String>>();

    private int REQUEST_CODE_DOC;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_construct, container, false);

        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        ImageView mExcel = getActivity().findViewById(R.id.excel_button);
        if(prefs.getBoolean("excel_premium", false) == true){
            mExcel.setVisibility(View.VISIBLE);
        }else{
            mExcel.setVisibility(View.GONE);
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mUtil = new Util(getActivity());

        myText = view.findViewById(R.id.text);

        //if Excel feature enabled
        checkPermission();


        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSegmentAdapter = new MySegmentAdapter(mSegmentDataset);
        mRecyclerView.setAdapter(mSegmentAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        System.out.println("segment item clicked: "+position);
                        if(position == mData.size()){
                            mData.add(new ArrayList<String>());
                            matchSegments();
                            saveObject();

                        }else {
                            selectSegment(position);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        System.out.println("segment item long clicked: "+position);
                        if(position != mSegmentDataset.size()-1) {
                            //TODO deleting segments still not quite right
                            confirmDelete(position, SEGMENT_LIST);
                        }
                    }
                })
        );


        mTitles = (getArray("titles"));
        System.out.println("titles: "+mTitles);

        spinner = view.findViewById(R.id.spinner);
        spinner.setItems(mTitles);
        int position = prefs.getInt("spinnerPosition", 0);
        if(mTitles.size() >= (position+1)) {
            spinner.setSelectedIndex(prefs.getInt("spinnerPosition", 0));
            mTitle = mTitles.get(prefs.getInt("spinnerPosition", 0));

        }else if(mTitles.size() >0){
            spinner.setSelectedIndex(0);
            mTitle = mTitles.get(0);
            edt.putInt("spinnerPosition",0);
            edt.commit();
        }
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                mTitle = mTitles.get(position);
                setupList(mTitle);
            }
        });

        System.out.println("starting title: "+mTitle);
        BigListModel mObject = mUtil.getSavedObjectFromPreference(getActivity(), mTitle, BigListModel.class);
        if(mObject != null) {
            mData = mObject.getData();
            System.out.println("starting title: " + mData);
        }
        if(mObject == null || mData.size() == 0){
            ArrayList dummyList = new ArrayList<String>();
            mData.add(dummyList);
            saveObject();
        }

        mListRecyclerView =  view.findViewById(R.id.list_recycler);
        mListRecyclerView.setHasFixedSize(true);
        mListLayoutManager = new LinearLayoutManager(getActivity());
        mListLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListRecyclerView.setLayoutManager(mListLayoutManager);
        mListAdapter = new MyListAdapter(mData.get(mSegment));
        mListRecyclerView.setAdapter(mListAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mListRecyclerView.getContext(),
                mListLayoutManager.getOrientation());
        mListRecyclerView.addItemDecoration(dividerItemDecoration);
        mListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mListRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mListRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        System.out.println("list item clicked: "+position);


                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        System.out.println("list item long clicked: "+position);
                        confirmDelete(position, LIST_LIST);
                    }
                })
        );


        final EditText mEdit2 = view.findViewById(R.id.edit_text2);
        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s){

            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){



            }
            public void  onTextChanged (CharSequence s, int start, int before,int count) {
                if (s.length() < 1 || start >= s.length() || start < 0)
                    return;

                if (s.subSequence(start, start + 1).toString().equalsIgnoreCase("\n")) {
                    // It was Enter
                    System.out.println("pressed enter");

                    // Change text to show without '\n'
                    String s_text = start > 0 ? s.subSequence(0, start).toString() : "";
                    s_text += start < s.length() ? s.subSequence(start + 1, s.length()).toString() : "";
                    mEdit2.setText(s_text);

                    // Move cursor to the end of the line
                    mEdit2.setSelection(s_text.length());

                    //Add to list
                    String newText = mEdit2.getText().toString();
                    if(!newText.equals("")) {
                       // mListDataset.add(mEdit2.getText().toString());
                        mData.get(mSegment).add(mEdit2.getText().toString());
                        mEdit2.setText("");
                        mListRecyclerView.scrollToPosition(mListAdapter.getItemCount());
                       // storeArray(mListDataset, mTitle + mSegment);
                        saveObject();
                        updateCombos(mTitle);
                        mListLayoutManager.scrollToPosition( mData.get(mSegment).size() - 1);
                    }
                }


            }
        };
        mEdit2.addTextChangedListener(tw);


        MaterialFancyButton loadButton = view.findViewById(R.id.add_phrase_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("add phrase clicked");
                String newText = mEdit2.getText().toString();
                if(!newText.equals("")) {
                   /* mListDataset.add(mEdit2.getText().toString());
                    mEdit2.setText("");
                    mListRecyclerView.scrollToPosition(mListAdapter.getItemCount());
                    storeArray(mListDataset, mTitle + mSegment);
                    updateCombos(mTitle);
                    mListLayoutManager.scrollToPosition(mListDataset.size() - 1);
*/
                    mData.get(mSegment).add(mEdit2.getText().toString());
                    mEdit2.setText("");
                    mListRecyclerView.scrollToPosition(mListAdapter.getItemCount());
                    saveObject();
                    updateCombos(mTitle);
                    mListLayoutManager.scrollToPosition( mData.get(mSegment).size() - 1);
                }
            }
        });

        RelativeLayout mTextBox = view.findViewById(R.id.text_box);
        final TextView mText = view.findViewById(R.id.test_text);
        mTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mText.setText(mUtil.getListMessage(mTitles.get(spinner.getSelectedIndex())));
            }
        });

        FloatingActionButton mButton = view.findViewById(R.id.fab);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNew();
            }
        });

        if(spinner.length() != 0) {
            mTitle = mTitles.get(spinner.getSelectedIndex());
            setupList(mTitle);
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Fragment Construct Viewed")
                .putContentType("Fragment Views")
                .putContentId("construct"));

        return view;
    }

    private void setupList(String title){

        BigListModel mObject = mUtil.getSavedObjectFromPreference(getActivity(), title, BigListModel.class);

        //System.out.println("object data:"+mObject.getData().get(0)+mObject.getData().get(1));

        if(mObject != null) {
            mData = mObject.getData();
            matchSegments();

            updateCombos(title);
            selectSegment(0);
        }
    }



    private void selectSegment(int position){

        if( mData.size() == 0){
            ArrayList dummyList = new ArrayList<String>();
            mData.add(dummyList);
            saveObject();
            matchSegments();
        }

        mSegment = position;
        System.out.println("select segment "+position);
        mListAdapter.updateData(mData.get(mSegment));

        mSegmentAdapter.setSelected(position);

        System.out.println("segment dataset"+ mListDataset);

        mSegmentAdapter.notifyDataSetChanged();

       // if(mListAdapter != null) {
       /* }else{
            mListAdapter = new MyListAdapter(mData.get(mSegment));
        }*/
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

    public ArrayList<String> getArray(String key){
        ArrayList<String> mList = new ArrayList<String>();

        try {
            DB snappydb = DBFactory.open(getActivity());
            String[] slist  =  snappydb.getArray(key, String.class);
            mList = new ArrayList<String>(Arrays.asList(slist));
            return mList;

        } catch (SnappydbException e) {
            System.out.println("snappy error: "+e.getMessage());
            e.printStackTrace();
            return mList;
        }
    }



    public void confirmDelete(final int position, final int list)
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_delete);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((4 * width) / 7, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list == LIST_LIST) {
                    mData.get(mSegment).remove(position);
                   // storeArray(mListDataset, mTitle + mSegment);
                    saveObject();
                    mListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                if(list == SEGMENT_LIST) {

                    mData.remove(position);
                    saveObject();
                    matchSegments();
                    //move all segment arrays down one
                /*    for(int i=position;i<mSegmentDataset.size()-3;i++) {
                            //for all but last, move the next one down
                            ArrayList<String> mSegment = mUtil.getArray(mTitle + (i + 1));
                            mUtil.storeArray(mSegment, mTitle + i);
                    }*/
                    //for the last (second to last really, ignore + slot, delete data
                    /*ArrayList<String> clear = new ArrayList<String>();
                    mUtil.storeArray(clear, mTitle + (mSegmentDataset.size()-2));
*/

                    // mSegmentDataset.remove(position);
                   //  mUtil.storeInt(mTitle+"segments", (mSegmentDataset.size()));
                   // mSegmentAdapter.notifyDataSetChanged();

                    if(position == mData.size()){
                        if((position-1)<0){
                            selectSegment(0);
                        }else {
                            selectSegment(position - 1);
                        }
                    }
                    else {
                        selectSegment(position);
                    }

                    dialog.dismiss();
                }
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

    public void dialogNew()
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText mEdit = dialog.findViewById(R.id.edit_text);


        dialog.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEdit.getText().toString().equals("")){
                    mTitle = mEdit.getText().toString();
                    mTitles.add(mTitle);
                    spinner.setItems(mTitles);
                    spinner.setSelectedIndex(mTitles.size()-1);
                    storeArray(mTitles, "titles");

                    mData = new ArrayList<ArrayList<String>>();
                    ArrayList<String> list1 = new ArrayList<String>();
                    ArrayList<String> list2 = new ArrayList<String>();
                    mData.add(list1);
                    mData.add(list2);
                   /* BigListModel mList = new BigListModel(mTitle, mData);
                    mUtil.saveObjectToSharedPreference(getActivity(),mTitle, mList);
*/
                    saveObject();
                    matchSegments();
                    selectSegment(0);

                    dialog.dismiss();
                }else{
                  //must select title
                }


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


    private String getFormatedAmount(int amount){
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    private void updateCombos(String title){

        int combos = 1;
        for(int i=0; i<mData.size(); i++) {

            if(mData.get(i).size() != 0) {
                combos = combos * mData.get(i).size();
            }
            myText.setText("Click to Test "+getFormatedAmount(combos)+" Combinations");
        }


    }

    public void saveObject(){
        BigListModel mList = new BigListModel(mTitle, mData);
        mUtil.saveObjectToSharedPreference(getActivity(),mTitle, mList);
    }

    public void matchSegments(){
        mSegmentDataset.clear();
        for(int i=0; i<mData.size()+1; i++){
            mSegmentDataset.add("fill");
        }
        mSegmentAdapter.notifyDataSetChanged();
    }

    public void excelPressed(){
        System.out.println("excel test");
        WriteExcel test = new WriteExcel();
        test.setContext(getActivity());

        test.makeExcelFile(mTitle);
        //Toast.makeText(getActivity(), "File saved to /Comsta Sheets/"+mTitle, Toast.LENGTH_LONG).show();

    }


    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission2())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        }
    }

    private boolean checkPermission2() {
        int result = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getActivity(), "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 12:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }


    private void selectFile(){

        String[] mimeTypes =
                {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), REQUEST_CODE_DOC);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_DOC) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                File mFile = new File(data.getData().getPath());
                WriteExcel test = new WriteExcel();
                test.setContext(getActivity());
                test.addToList(mFile.getName(), mFile, mTitle);
                setupList(mTitle);
            }
        }
    }


    public void dialogExcel()
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_excel);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((6 * width) / 7, LinearLayout.LayoutParams.WRAP_CONTENT);

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.excel_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
                dialog.dismiss();
            }
        });


        dialog.findViewById(R.id.excel_export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                excelPressed();
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }



}