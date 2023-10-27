package ott.primeplay.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ott.primeplay.network.model.Package;

import ott.primeplay.R;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {


    private Context context;
    private List<Package> packageList;
    private int c;
    private OnItemClickListener itemClickListener;
    private String currency;


    MyListData[] myListData;

    public PackageAdapter(Context context, List<Package> packageList, String currency) {
        this.context = context;
        this.packageList = packageList;
        this.currency = currency;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_package_item_2, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //holder.buyNowTv.setBackgroundColor(context.getResources().getColor(getColor()));
        Package pac = packageList.get(position);
        if (pac != null) {
            // holder.packageTv.setText(currency + " " + pac.getPrice() +" - " + pac.getName());
            holder.packageTv.setText(pac.getName());
            holder.packagevalidity.setText(pac.getDay() + " Days");

            holder.packageprice.setText(currency + " " + pac.getPrice());
        }

    }


    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        /*TextView buyNowTv, priceTv, packageNameTv, packageDesTv;
        RelativeLayout packageLayout;*/

        TextView packageTv, packageprice, packagevalidity;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linear_layout);
            packageTv = itemView.findViewById(R.id.package_name);
            packagevalidity = itemView.findViewById(R.id.package_validity);
            packageprice = itemView.findViewById(R.id.price);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


//original

                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(packageList.get(getAdapterPosition()));
                    }

//agepopup
                   /* final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
                    dialog.setContentView(R.layout.confirm_spinner_user_age_dialog);
                    dialog.setCancelable(false);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                    //  Button btConfirm = dialog.findViewById(R.id.btConfirm);
                    TextView txtCancel = dialog.findViewById(R.id.txtCancel);
                    RecyclerView rec_age = dialog.findViewById(R.id.recyclerView);

                    myListData = new MyListData[]{

                            new MyListData("18"),
                            new MyListData("19"),
                            new MyListData("20"),
                            new MyListData("21"),
                            new MyListData("22"),
                            new MyListData("23"),
                            new MyListData("24"),
                            new MyListData("25"),
                            new MyListData("26"),
                            new MyListData("27"),
                            new MyListData("28"),


                            new MyListData("29"),
                            new MyListData("30"),
                            new MyListData("31"),
                            new MyListData("32"),
                            new MyListData("33"),
                            new MyListData("34"),
                            new MyListData("35"),
                            new MyListData("36"),
                            new MyListData("37"),
                            new MyListData("38"),
                            new MyListData("39"),

                            new MyListData("40"),
                            new MyListData("41"),
                            new MyListData("42"),
                            new MyListData("42"),
                            new MyListData("43"),
                            new MyListData("44"),
                            new MyListData("45"),
                            new MyListData("46"),
                            new MyListData("47"),
                            new MyListData("48"),
                            new MyListData("49"),


                            new MyListData("50"),
                            new MyListData("51"),
                            new MyListData("52"),
                            new MyListData("52"),
                            new MyListData("53"),
                            new MyListData("54"),
                            new MyListData("55"),
                            new MyListData("56"),
                            new MyListData("57"),
                            new MyListData("58"),
                            new MyListData("59"),


                            new MyListData("60"),
                            new MyListData("61"),
                            new MyListData("62"),
                            new MyListData("62"),
                            new MyListData("63"),
                            new MyListData("64"),
                            new MyListData("65"),
                            new MyListData("66"),
                            new MyListData("67"),
                            new MyListData("68"),
                            new MyListData("69"),

                            new MyListData("70"),
                            new MyListData("71"),
                            new MyListData("72"),
                            new MyListData("72"),
                            new MyListData("73"),
                            new MyListData("74"),
                            new MyListData("75"),
                            new MyListData("76"),
                            new MyListData("77"),
                            new MyListData("78"),
                            new MyListData("79"),


                            new MyListData("80"),
                            new MyListData("81"),
                            new MyListData("82"),
                            new MyListData("82"),
                            new MyListData("83"),
                            new MyListData("84"),
                            new MyListData("85"),
                            new MyListData("86"),
                            new MyListData("87"),
                            new MyListData("88"),
                            new MyListData("89"),


                            new MyListData("90"),
                            new MyListData("91"),
                            new MyListData("92"),
                            new MyListData("92"),
                            new MyListData("93"),
                            new MyListData("94"),
                            new MyListData("95"),
                            new MyListData("96"),
                            new MyListData("97"),
                            new MyListData("98"),
                            new MyListData("99"),
                            new MyListData("100"),

                    };


                     MyListAdapter MyListAdapter = new MyListAdapter(myListData,packageList.get(getAdapterPosition()));
                  //  MyListAdapter MyListAdapter = new MyListAdapter(myListData);
                    rec_age.setHasFixedSize(true);
                    rec_age.setLayoutManager(new LinearLayoutManager(context));
                    rec_age.setAdapter(MyListAdapter);




                    rec_age.addOnItemTouchListener(
                            new RecyclerItemClickListener(context, rec_age, new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    // do whatever
                                    if (itemClickListener != null) {
                                        itemClickListener.onItemClick(packageList.get(getAdapterPosition()));
                                        itemClickListener.onItemClick(packageList.get(getAdapterPosition()));

                                    }


                                    ott.primeplay.adapters.MyListAdapter.ViewHolder viewHolder = (ott.primeplay.adapters.MyListAdapter.ViewHolder) rec_age.getChildViewHolder(view);

                                    View age = viewHolder.itemView.findViewById(position);

                                    String agee = String.valueOf(age);

                                }


                            })
                    );


                    txtCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
//                finish();
                        }
                    });

                    dialog.show();
                    dialog.getWindow().setAttributes(lp);
*/
                }
            });


        }
    }

    private int getColor() {

        int colorList[] = {R.color.red_400, R.color.blue_400, R.color.indigo_400, R.color.orange_400, R.color.light_green_400, R.color.blue_grey_400};
        //int colorList2[] = {R.drawable.gradient_1 ,R.drawable.gradient_2,R.drawable.gradient_3,R.drawable.gradient_4,R.drawable.gradient_5,R.drawable.gradient_6};

        if (c >= 6) {
            c = 0;
        }

        int color = colorList[c];
        c++;

        return color;

    }


    public interface OnItemClickListener {
        void onItemClick(Package pac);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);

        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }


            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));


                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }
}
