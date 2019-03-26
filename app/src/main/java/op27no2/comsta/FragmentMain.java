package op27no2.comsta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by CristMac on 11/4/17.
 */

public class FragmentMain extends android.support.v4.app.Fragment {
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();
    private ArrayList<ListModel> mListData = new ArrayList<ListModel>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        ImageView mExcel = getActivity().findViewById(R.id.excel_button);
        mExcel.setVisibility(View.GONE);

        mUtil = new Util(getActivity());
        mTitles = (mUtil.getArray("titles"));
        System.out.println("start titles: "+mTitles);

        //TODO switch array to sharedpref object?

        view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopupEnabled() == true) {
                    getActivity().startService(new Intent(getActivity(), PopupService.class));
                } else {
                    //start accessibility service
                    popupDialog();
                }
              //  getActivity().startService(new Intent(getActivity(), SampleAccessibilityService.class));

            }
        });


/*        if(prefs.getBoolean("firstrun_main", true) == true){
            System.out.println("first run frag main");
            ArrayList<String> startTitle = new ArrayList<String>();
            startTitle.add("Example Comments");

            ArrayList<ArrayList<String>> mData = new ArrayList<ArrayList<String>>();
            ArrayList<String> segment1 = new ArrayList<String>();
            ArrayList<String> segment2 = new ArrayList<String>();
            ArrayList<String> segment3 = new ArrayList<String>();
            segment1.add("Check this out,");
            segment1.add("Look at this,");
            segment1.add("Attention,");
            segment2.add("this is how");
            segment2.add("look how");
            segment2.add("see how");
            segment3.add("comments function");
            segment3.add("this app works");
            segment3.add("you can brainstorm many comments");
            mData.add(segment1);
            mData.add(segment2);
            mData.add(segment3);

            mUtil.storeArray(startTitle, "titles");
            mUtil.storeBoolean("Example Comments"+"isActive",true);

            BigListModel mList = new BigListModel("Example Comments", mData);
            mUtil.saveObjectToSharedPreference(getActivity(),"Example Comments", mList);

            edt.putBoolean("firstrun_main",false);
            edt.commit();
        }*/

        addList("free_thanks_", "Thank You Comments", 2);
        addList("free_general_", "Cool Post Comments", 6);
        addList("free_entertaining_", "General \"That's Entertaining\" Comments", 6);
        addList("free_interesting_", "General \"That's Interesting\" Comments", 5);
        addList("free_fashion_", "General Fashion Comments", 6);



        for(int i=0;i<mTitles.size();i++){
            ListModel modelTitle = new ListModel(getActivity());
            modelTitle.setTitle(mTitles.get(i));
            modelTitle.setSelected(mUtil.getBoolean(mTitles.get(i)+"isActive"));
            mListData.add(modelTitle);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(mListData);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        System.out.println("segment item clicked: "+position);

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        System.out.println("segment item long clicked: "+position);
                            //TODO deleting segments still not quite right
                            confirmDelete(position);

                    }
                })
        );

        return view;
    }


    public void confirmDelete(final int position)
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
                    mListData.remove(position);
                    mTitles.remove(position);
                    mUtil.storeArray(mTitles, "titles");
                    mAdapter.notifyDataSetChanged();
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

    private boolean isPopupEnabled(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
            return false;
        } else {
            //do nothing
            return true;
        }
    }
    public void popupDialog()
    {
        final Dialog dialog = new Dialog(getActivity());

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
                        Uri.parse("package:" + getActivity().getPackageName()));
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

    private void addList(String resource_name, String inAppTitle, int numlists){
        if(prefs.getBoolean(resource_name+"added",false) == false) {
            ArrayList<ArrayList<String>> mGenericData = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < numlists; i++) {
                int id = getResources().getIdentifier(resource_name + (i + 1), "array", "op27no2.comsta");
                mGenericData.add(new ArrayList(Arrays.asList(getResources().getStringArray(id))));
            }
            BigListModel mlist = new BigListModel(inAppTitle, mGenericData);
            mUtil.saveObjectToSharedPreference(getActivity(), inAppTitle, mlist);
            mTitles.add(inAppTitle);
            mUtil.storeArray(mTitles, "titles");
            edt.putBoolean(resource_name+"added",true);
            edt.commit();
        }

    }

}