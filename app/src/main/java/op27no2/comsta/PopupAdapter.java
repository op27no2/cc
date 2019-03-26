package op27no2.comsta;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanks.library.AnimateCheckBox;

import java.util.ArrayList;

/**
 * Created by CristMac on 11/3/17.
 */

public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {
    private ArrayList<ListModel> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PopupAdapter(ArrayList<ListModel> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PopupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popup_rowview, parent, false);
        // set the view's size, margins, paddings and layout parameters
        final ViewHolder holder = new ViewHolder(v);


        AnimateCheckBox mBox = v.findViewById(R.id.check_box);
        mBox.setOnCheckedChangeListener(new AnimateCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    System.out.println("check change called");
                    mDataset.get(holder.getAdapterPosition()).setSelected(isChecked);
                }
            }
        });

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ListModel mModel = mDataset.get(position);

        System.out.println("position test"+position);
        TextView mText = holder.mView.findViewById(R.id.text_view);
        mText.setText(mDataset.get(position).getTitle());
       // final Util mUtil = new Util(holder.mView.getContext());

        final AnimateCheckBox mBox = holder.mView.findViewById(R.id.check_box);
        mBox.setChecked(mModel.getSelected());
        mBox.setOnCheckedChangeListener(new AnimateCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View buttonView, boolean isChecked) {
                if(holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    System.out.println("check change called");
                    mDataset.get(holder.getAdapterPosition()).setSelected(isChecked);
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBox.setChecked(mModel.getSelected());
            }
        }, 300);


    }




    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }





}



