package op27no2.comsta;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.PurchaseEvent;
import com.rilixtech.materialfancybutton.MaterialFancyButton;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by CristMac on 11/4/17.
 */

public class FragmentPremium extends android.support.v4.app.Fragment implements PurchasesUpdatedListener {

    private Util mUtil;
    private ArrayList<String> mTitles = new ArrayList<String>();
    private SharedPreferences prefs;
    private SharedPreferences.Editor edt;

    private TextView mTextTitle1;
    private TextView mTextTitle2;
    private MaterialFancyButton purchaseButton;

    private ArrayList<ArrayList<String>> mGenericData = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> mFitnessData = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<String>> mMusicData = new ArrayList<ArrayList<String>>();

    private BillingClient mBillingClient;
    private List<String> skuList = new ArrayList<>();
    private List<String> priceList = new ArrayList<>();
    private Activity mActivity;

    private RecyclerView mPurchaseRecyclerView;
    private LinearLayoutManager mPurchaseLayoutManager;
    private MyPurchaseAdapter mPurchaseAdapter;
    private ArrayList<String> mData = new ArrayList<String>();
    private ArrayList<String> mData2 = new ArrayList<String>();
    private ArrayList<String> mDataSku = new ArrayList<String>();
    private HashMap<String, String> mPriceMap = new HashMap<String, String>();

    private Boolean billingReady = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_premium, container, false);
        ImageView mExcel = getActivity().findViewById(R.id.excel_button);
        mExcel.setVisibility(View.GONE);

        prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        edt = prefs.edit();

        //premium_generic(1-4)
        //fitness_followback_(1-4)
        //music_(1-7)
        mDataSku.add("excelfeatures");
        mDataSku.add("genericpost");
        mDataSku.add("fitnesscomments");
        mDataSku.add("musiccomments");
        mData.add("Excel Import/Export");
        mData.add("Generic Comments");
        mData.add("Fitness Comments");
        mData.add("Music Comments");

        mUtil = new Util(getActivity());

        //empty placeholder since excel doesn't have combos...
        mData2.add("");

        for(int i=0;i<4;i++) {
            int id = getResources().getIdentifier("premium_generic"+(i+1), "array","op27no2.comsta");
            mGenericData.add(new ArrayList(Arrays.asList(getResources().getStringArray(id))));
        }
        BigListModel mList = new BigListModel("Generic Comments", mGenericData);
        mUtil.saveObjectToSharedPreference(getActivity(),"Generic Comments", mList);
        mData2.add(mList.getCombos()+" Combinations");

        for(int i=0;i<4;i++) {
            int resID = getResources().getIdentifier("fitness_followback_" + (i + 1), "array", "op27no2.comsta");
            mFitnessData.add(new ArrayList(Arrays.asList(getResources().getStringArray(resID))));
        }
        BigListModel mList2 = new BigListModel("Fitness Comments", mFitnessData);
        mUtil.saveObjectToSharedPreference(getActivity(),"Fitness Comments", mList2);
        mData2.add(mList2.getCombos()+" Combinations");

        for(int i=0;i<7;i++) {
            int resID = getResources().getIdentifier("music_" + (i + 1), "array", "op27no2.comsta");
            mMusicData.add(new ArrayList(Arrays.asList(getResources().getStringArray(resID))));
        }
        BigListModel mList3 = new BigListModel("Music Comments", mMusicData);
        mUtil.saveObjectToSharedPreference(getActivity(),"Music Comments", mList3);
        mData2.add(mList3.getCombos()+" Combinations");

     /*   genericArray1 = getResources().getStringArray(R.array.premium_generic1);
        genericArray2 = getResources().getStringArray(R.array.premium_generic2);
        genericArray3 = getResources().getStringArray(R.array.premium_generic3);
        genericArray4 = getResources().getStringArray(R.array.premium_generic4);
        mGenericData.add(new ArrayList(Arrays.asList(genericArray1)));
        mGenericData.add(new ArrayList(Arrays.asList(genericArray2)));
        mGenericData.add(new ArrayList(Arrays.asList(genericArray3)));
        mGenericData.add(new ArrayList(Arrays.asList(genericArray4)));*/

        mPurchaseRecyclerView =  view.findViewById(R.id.purchase_recycler);
        mPurchaseRecyclerView.setHasFixedSize(true);
        mPurchaseLayoutManager = new LinearLayoutManager(getActivity());
        mPurchaseLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPurchaseRecyclerView.setLayoutManager(mPurchaseLayoutManager);

        //ADD DATA
        mPurchaseAdapter = new MyPurchaseAdapter(mData, mData2);

        mPurchaseRecyclerView.setAdapter(mPurchaseAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mPurchaseRecyclerView.getContext(),
                mPurchaseLayoutManager.getOrientation());
        mPurchaseRecyclerView.addItemDecoration(dividerItemDecoration);
        mPurchaseRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPurchaseRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mPurchaseRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        System.out.println("list item clicked: "+position);

                        if(billingReady) {
                            if(position == 0){
                                purchaseExcel(position);
                            }
                            else {
                                purchaseDialog(position);
                            }
                        }
                        else{
                            Toast.makeText(getActivity(), "Billing Connection Still Loading or Not Able to Connect", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        System.out.println("list item long clicked: "+position);

                    }
                })
        );


        skuList.add("genericpost");
        skuList.add("fitnesscomments");
        skuList.add("musiccomments");
        skuList.add("excelfeatures");

        mBillingClient = BillingClient.newBuilder(getActivity()).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    System.out.println("billing client ready");
                    billingReady = true;

                    priceList = getPrices(skuList);


                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                System.out.println("billing services disconnected ");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Fragment Premium Viewed")
                .putContentType("Premium Views")
                .putContentId("premium"));




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

    private ArrayList<String> getPrices(List<String> productList) {
        final ArrayList<String> prices = new ArrayList<String>();

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK
                                && skuDetailsList != null) {

                            System.out.println("Price results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String price = skuDetails.getPrice();
                                String name = skuDetails.getSku();
                                mPriceMap.put(name,price);
                            }
                            System.out.println("pricemap: "+mPriceMap);



                        }else{
                            System.out.println("billing response code not ok: "+responseCode);
                            Toast.makeText(getActivity(), "Billing unavaialble, please check your internet connection", Toast.LENGTH_LONG).show();
                        }
            }

        });

        return prices;
    }


        private void queryForProducts(final String productSku){
        System.out.println("query product method called");

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK
                                && skuDetailsList != null) {

                            System.out.println("IAP results: " + skuDetailsList);
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String name = skuDetails.getTitle();
                                String price = skuDetails.getPrice();
                                if (productSku.equals(sku)) {
                                    String mPremiumUpgradePrice = price;
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSku(sku)
                                            .setType(BillingClient.SkuType.INAPP)
                                            .build();
                                    int mresponseCode = mBillingClient.launchBillingFlow(mActivity, flowParams);
                                    System.out.println("billing response code: " + mresponseCode);

                                }
                            }
                        }

                    }

        });

/*        //purchase code
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(SkuType.INAPP)
                .build();
        int responseCode = mBillingClient.launchBillingFlow(flowParams);

        //purchase history
        mBillingClient.queryPurchaseHistoryAsync(SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(@BillingResponse int responseCode,
                                                          List<Purchase> purchasesList) {
                        if (responseCode == BillingResponse.OK
                                && purchasesList != null) {
                            for (Purchase purchase : purchasesList) {
                                // Process the result.
                            }
                        }
                    }
        });*/


    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        System.out.println("purchase updated");
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase mPurchase){
        if(mPurchase.getSku().equals("genericpost")){
            //code to add generic comments
            mTitles = (mUtil.getArray("titles"));
            mTitles.add("Generic Comments");
            storeArray(mTitles, "titles");
        }
        if(mPurchase.getSku().equals("fitnesscomments")){
            //code to add generic comments
            mTitles = (mUtil.getArray("titles"));
            mTitles.add("Fitness Comments");
            storeArray(mTitles, "titles");
        }
        if(mPurchase.getSku().equals("musiccomments")){
            //code to add generic comments
            mTitles = (mUtil.getArray("titles"));
            mTitles.add("Music Comments");
            storeArray(mTitles, "titles");
        }
        if(mPurchase.getSku().equals("excelfeatures")){
            //code to add generic comments
            edt.putBoolean("excel_premium",true);
            edt.commit();
            Toast.makeText(getActivity(), "Import/Export Icon Added to Construct Page", Toast.LENGTH_LONG).show();

        }


        Answers.getInstance().logPurchase(new PurchaseEvent()
                .putItemName("Dialog purchase "+mPurchase.getSku())
                .putItemType("Apparel")
                .putSuccess(true));
    }

    private void purchaseDialog(final int position){
        final Dialog dialog = new Dialog(getActivity());

        Toast.makeText(getActivity(), "Switch App \"Off\" to test", Toast.LENGTH_LONG).show();

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(mDataSku.get(position))
                .putContentType("Purchase Views")
                .putContentId(mDataSku.get(position)));


        System.out.println("price map: "+mPriceMap);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_purchase);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.title_text);
        mText.setText(mData.get(position)+" - "+mPriceMap.get(mDataSku.get(position)));

        final EditText mEdit = dialog.findViewById(R.id.edit1);
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit.setText(getTestMessage(mData.get(position)));
            }
        });

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
                queryForProducts(mDataSku.get(position));
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void purchaseExcel(final int position){
        final Dialog dialog = new Dialog(getActivity());

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(mDataSku.get(position))
                .putContentType("Purchase Views")
                .putContentId(mDataSku.get(position)));


        System.out.println("price map: "+mPriceMap);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_purchaseexcel);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        dialog.getWindow().setLayout((8 * width) / 9, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView mText = dialog.findViewById(R.id.title_text);
        mText.setText(mData.get(position)+" - "+mPriceMap.get(mDataSku.get(position)));


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
                queryForProducts(mDataSku.get(position));
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private String getTestMessage(String product){
        String mString = "";

        Util mUtil = new Util(getActivity());
        BigListModel mObject = mUtil.getSavedObjectFromPreference(getActivity(), product, BigListModel.class);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mActivity = null;
    }



}