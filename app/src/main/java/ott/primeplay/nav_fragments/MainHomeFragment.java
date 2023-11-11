package ott.primeplay.nav_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.volcaniccoder.bottomify.BottomifyNavigationView;
import com.volcaniccoder.bottomify.OnNavigationItemChangeListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import ott.primeplay.CpGoldFragment;
import ott.primeplay.HomeFragment;
import ott.primeplay.MainActivity;
import ott.primeplay.MoreActivity;
import ott.primeplay.R;
import ott.primeplay.SearchActivity;
import ott.primeplay.TvSeriesFragment;
import ott.primeplay.reels.ReelsListActivity;

public class MainHomeFragment extends Fragment {
    private MainActivity activity;
    //    private MoreActivity activity1;
    private BottomifyNavigationView bottomifyNavigationViewDark, bottomifyNavigationViewLight;
    LinearLayout searchRootLayout;
    String from = "";
    SharedPreferences sharedPreferences;
    LinearLayoutCompat lnr_home, lnr_gold, lnr_watchlist, lnr_download, lnr_account, lnr_search;
    AppCompatImageView img_home, img_gold, img_watchlist, img_download, img_account, img_search;
    TextView txt_home, txt_gold, txt_watchlist, txt_download, txt_account, txt_search;
    FloatingActionButton fab_goals;
    CleverTapAPI clevertapscreenviewd;
    private AdView mAdView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            from = bundle.getString("from");
        }

//        if (from.equals("main")) {
        activity = (MainActivity) getActivity();
        /*} else {
            activity1 = (MoreActivity) getActivity();
        }*/
        return inflater.inflate(R.layout.fragment_main_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomifyNavigationViewDark = view.findViewById(R.id.bottomify_nav);
        bottomifyNavigationViewLight = view.findViewById(R.id.bottomify_nav_light);
        searchRootLayout = view.findViewById(R.id.search_root_layout);
        lnr_home = view.findViewById(R.id.lnr_home);
        lnr_gold = view.findViewById(R.id.lnr_gold);
        fab_goals = view.findViewById(R.id.fab_goals);
        lnr_watchlist = view.findViewById(R.id.lnr_watchlist);
        lnr_download = view.findViewById(R.id.lnr_download);
        lnr_account = view.findViewById(R.id.lnr_account);
        lnr_search = view.findViewById(R.id.lnr_search);
        img_home = view.findViewById(R.id.img_home);
        img_gold = view.findViewById(R.id.img_gold);
        img_watchlist = view.findViewById(R.id.img_watchlist);
        img_download = view.findViewById(R.id.img_download);
        img_account = view.findViewById(R.id.img_account);
        img_search = view.findViewById(R.id.img_search);
        txt_home = view.findViewById(R.id.txt_home);
        txt_gold = view.findViewById(R.id.txt_gold);
        txt_watchlist = view.findViewById(R.id.txt_watchlist);
        txt_download = view.findViewById(R.id.txt_download);
        txt_account = view.findViewById(R.id.txt_account);
        txt_search = view.findViewById(R.id.txt_search);

        clevertapscreenviewd = CleverTapAPI.getDefaultInstance(getActivity());


        //admob banner ads
        MobileAds.initialize(getActivity());
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView adView = new AdView(getContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getResources().getString(R.string.admob_banner_unit_id));
        // adView.setAdUnitId("ca-app-pub-1307905966777808/6708516251");
        //  adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");//test unit id
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.

                mAdView.setVisibility(View.GONE);

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });



//        if (activity != null)
        sharedPreferences = activity.getSharedPreferences("push", MODE_PRIVATE);
//        else
//            sharedPreferences = activity1.getSharedPreferences("push", MODE_PRIVATE);

        boolean isDark = sharedPreferences.getBoolean("dark", false);

       /* if (isDark) {
            //bottomifyNavigationView
            bottomifyNavigationViewDark.setVisibility(View.VISIBLE);
            bottomifyNavigationViewDark.setBackgroundColor(getResources().getColor(R.color.black_window_light));
        } else {
            //bottomifyNavigationView light
            bottomifyNavigationViewLight.setVisibility(View.VISIBLE);
            bottomifyNavigationViewLight.setBackgroundColor(getResources().getColor(R.color.white));
        }*/

        //bottomifyNavigationView
        bottomifyNavigationViewDark.setActiveNavigationIndex(0);
        bottomifyNavigationViewDark.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
            @Override
            public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                switch (navigationItem.getPosition()) {
                    case 0:
                        loadFragment(new HomeFragment());
                        break;
                   /* case 1:
                        loadFragment(new MoviesFragment());
                        break;
                    case 2:
                        loadFragment(new LiveTvFragment());
                        break;*/
                    case 1:
                        loadFragment(new TvSeriesFragment());
                        break;
                    case 2:
                        loadFragment(new FavoriteFragment());
                        break;

                    case 3:
                        loadFragment(new FavoriteFragment());
                        break;
                }
            }
        });

        //bottomify light
        bottomifyNavigationViewLight.setActiveNavigationIndex(0);
        bottomifyNavigationViewLight.setOnNavigationItemChangedListener(new OnNavigationItemChangeListener() {
            @Override
            public void onNavigationItemChanged(@NotNull BottomifyNavigationView.NavigationItem navigationItem) {
                switch (navigationItem.getPosition()) {
                    case 0:
                        loadFragment(new HomeFragment());
                        break;
                 /*   case 1:
                        loadFragment(new MoviesFragment());
                        break;
                    case 2:
                        loadFragment(new LiveTvFragment());
                        break;*/
                    case 1:
                        loadFragment(new TvSeriesFragment());
                        break;
                    case 2:
                        loadFragment(new FavoriteFragment());
                        break;
                    case 3:
                        loadFragment(new FavoriteFragment());
                        break;

                }
            }
        });

        lnr_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadHomeFragment();
            }
        });


        fab_goals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ReelsListActivity.class);
                intent.putExtra("reelId", "");
                startActivity(intent);

            }
        });


       /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadHomeFragment();
            }
        });*/


        lnr_gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadGold();
            }
        });
        lnr_watchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadWatchlist();
            }
        });


        lnr_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDownloadFragment();


                HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
                screenViewedAction.put("Screen Name", "DownloadActivity");
                clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);


            }
        });


        lnr_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadAccountFragment();


                HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
                screenViewedAction.put("Screen Name", "AccountActivity");
                clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

            }
        });



        lnr_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadsearchFragment();


                HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
                screenViewedAction.put("Screen Name", "SearchActivity");
                clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

            }
        });


//        loadFragment(new HomeFragment());

    }


    @Override
    public void onResume() {
        super.onResume();
        loadHomeFragment();
    }

    public void loadHomeFragment() {
        txt_home.setTextColor(getResources().getColor(R.color.red));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.white));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_watchlist.setTextColor(getResources().getColor(R.color.white));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.white));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_account.setTextColor(getResources().getColor(R.color.white));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_search.setTextColor(getResources().getColor(R.color.white));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        loadFragment(new HomeFragment());
    }

    public void loadGold() {
        txt_home.setTextColor(getResources().getColor(R.color.white));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.red));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_watchlist.setTextColor(getResources().getColor(R.color.white));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.white));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_account.setTextColor(getResources().getColor(R.color.white));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_search.setTextColor(getResources().getColor(R.color.white));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        loadFragment(new CpGoldFragment());
    }

    public void loadWatchlist() {
        txt_home.setTextColor(getResources().getColor(R.color.white));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.white));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_watchlist.setTextColor(getResources().getColor(R.color.red));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.white));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_account.setTextColor(getResources().getColor(R.color.white));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_search.setTextColor(getResources().getColor(R.color.white));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


//        loadFragment(new FavoriteFragment());

        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

    public void loadDownloadFragment() {
        txt_home.setTextColor(getResources().getColor(R.color.white));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.white));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_watchlist.setTextColor(getResources().getColor(R.color.white));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.red));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_account.setTextColor(getResources().getColor(R.color.white));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_search.setTextColor(getResources().getColor(R.color.white));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        loadFragment(new DownloadNewFragment());
    }


    public void loadAccountFragment() {

        txt_home.setTextColor(getResources().getColor(R.color.white));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.white));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_watchlist.setTextColor(getResources().getColor(R.color.white));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.white));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_account.setTextColor(getResources().getColor(R.color.red));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_search.setTextColor(getResources().getColor(R.color.white));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        Intent intent = new Intent(getActivity(), MoreActivity.class);
        startActivity(intent);

        //loadFragment(new AccountFragment());
    }


    public void loadsearchFragment() {

        txt_home.setTextColor(getResources().getColor(R.color.white));
        img_home.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_gold.setTextColor(getResources().getColor(R.color.white));
        img_watchlist.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_watchlist.setTextColor(getResources().getColor(R.color.white));
        img_gold.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        txt_download.setTextColor(getResources().getColor(R.color.white));
        img_download.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_account.setTextColor(getResources().getColor(R.color.white));
        img_account.setColorFilter(ContextCompat.getColor(activity, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);


        txt_search.setTextColor(getResources().getColor(R.color.red));
        img_search.setColorFilter(ContextCompat.getColor(activity, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);


        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);


        //loadFragment(new AccountFragment());
    }


    //----load fragment----------------------
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        }
        return false;

    }


}