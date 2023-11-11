package ott.primeplay.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import ott.primeplay.R;
import ott.primeplay.network.model.Package;
import ott.primeplay.utils.Constants;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private MyListData[] listdata;
    //  private OnItemClickListener itemClickListener;
    Package aPackage;
    private MyListAdapter.OnItemClickListener myitemClickListener;
    Context mcontext;
    Dialog dialog;

    // RecyclerView recyclerView;
    public MyListAdapter(MyListData[] listdata, Context context, Dialog dialog) {
        this.listdata = listdata;
        this.mcontext = context;
        this.dialog = dialog;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item_age, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MyListData myListData = listdata[position];
        holder.textView.setText(listdata[position].getDescription());


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


              //  Toast.makeText(view.getContext(), "click on item: " + myListData.getDescription(), Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = mcontext.getSharedPreferences(Constants.USER_AGE, MODE_PRIVATE).edit();
                editor.putString("user_age", myListData.getDescription());
                editor.apply();
                dialog.dismiss();

            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);

        }


    }


    public interface OnItemClickListener {
        void onItemClick(MyListData myage);
    }

    public void setItemClickListener(MyListAdapter.OnItemClickListener itemClickListener) {
        this.myitemClickListener = itemClickListener;
    }





/*
    public interface OnItemClickListener {
        void onItemClick(Package pac);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }*/
}