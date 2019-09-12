package com.jattilainen.memebroker.TopMemes;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.RandomMemes.RandomMemesAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;


public class TopMemesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    View view;
    TopMemesAdapter adapter;
    boolean created = false;
    private List<TopMemesItem> memes = new ArrayList<>();
    private List<TopMemesItem> allMemes = new ArrayList<>();
    RecyclerView recyclerView;
    String loadLanguage = "English";
    SwipeRefreshLayout swipeRefreshLayout;
    boolean needLoadMore = false;
    Context context;
    public void setContext(Context context) {
        this.context = context;
    }

    public TopMemesFragment() { setHasOptionsMenu(true); }
    public interface OnButtonListener {
        void onTopLanguageSelectButtonSelected();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!created) {
            Log.e("tor", "hi");
            view = inflater.inflate(R.layout.fragment_top_memes, container, false);
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.languages_array, android.R.layout.simple_spinner_item);
            adapter = new TopMemesAdapter(memes, getActivity());
            created = true;
            recyclerView = view.findViewById(R.id.topMemesRecyclerView);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(dy > 0) {
                        if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                            if (needLoadMore) {
                                loadDataToList();
                            }
                        }
                    }
                }
            });
            swipeRefreshLayout = view.findViewById(R.id.topMemesRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    firstLoadTop();
                }
            });
        }
        return view;
    }
    private void firstLoadTop() {
        Log.e("firstload", "firstLoadTop: 2");
        needLoadMore = false;
        //progressBar.setVisibility(View.VISIBLE);
        DatabaseReference images = FirebaseDatabase.getInstance().getReference().child("images").child(loadLanguage).child("images");
        images.orderByChild("price").limitToFirst(Constants.TOP_MEMES_TOTAL_LOAD_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
            String hash;
            double ratio;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("firstload", "firstLoadTop: 3" + dataSnapshot.toString());
                memes.clear();
                allMemes.clear();
                adapter.onDetachedFromRecyclerView(recyclerView);
                for (DataSnapshot memeSnapshot : dataSnapshot.getChildren()) {
                    hash = memeSnapshot.getKey();
                    Integer reports = memeSnapshot.child("reports").getValue(Integer.class);
                    if (reports != null && reports > Constants.MAX_REPORTS) {
                        continue;
                    }
                    int height = Integer.parseInt(memeSnapshot.child("height").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    int width = Integer.parseInt(memeSnapshot.child("width").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    ratio = (double)height/width;
                    Log.e("firstload", memeSnapshot.getKey());
                    allMemes.add(0, new TopMemesItem(hash, loadLanguage, ratio,  ""));
                }
                //progressBar.setVisibility(View.GONE);
                adapter = new TopMemesAdapter(memes, context);
                recyclerView.setAdapter(adapter);
                loadDataToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void loadDataToList() {
        needLoadMore = false;
        final int oldSize = memes.size();
        int i = oldSize;
        final Map<String, String> urls = new HashMap<>();
        final int needToLoad = min(oldSize + Constants.TOP_MEMES_ONE_BLOCK_SIZE, allMemes.size()) - i;
        for(; i < min(oldSize + Constants.TOP_MEMES_ONE_BLOCK_SIZE, allMemes.size()); i++) {
            final String hash = allMemes.get(i).getHash();
            StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + hash);
            chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri url) {
                    urls.put(hash, url.toString());
                    if (urls.size() == needToLoad) {
                        for (int j = oldSize; j < min(oldSize + Constants.TOP_MEMES_ONE_BLOCK_SIZE, allMemes.size()); j++) {
                            final String hash = allMemes.get(j).getHash();
                            final double ratio = allMemes.get(j).getRatio();
                            final String urlOfHash = urls.get(hash);
                            swipeRefreshLayout.setRefreshing(false);
                            memes.add(new TopMemesItem(hash, loadLanguage, ratio,  urlOfHash));
                            adapter.notifyItemInserted(memes.size());
                        }
                        needLoadMore = true;
                    }
                }
            });
        }
        if (min(oldSize + Constants.TOP_MEMES_ONE_BLOCK_SIZE, allMemes.size()) == oldSize) {
            needLoadMore = true;
        }
    }

    @Override
    public void onRefresh() {
        Log.e("firstload", "firstLoadTop: 1");
        if (needLoadMore) {
            firstLoadTop();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.top_memes_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.topMemes_language_button:
                Log.e("search", "onOptionsItemSelected: ");
                TopMemesFragment.OnButtonListener listener = (TopMemesFragment.OnButtonListener) getActivity();
                if (listener != null) {
                    listener.onTopLanguageSelectButtonSelected();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void setFinalLanguage(String lang) {
        if (!loadLanguage.equals(lang)) {
            loadLanguage = lang;
            if (needLoadMore && created) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        firstLoadTop();
                    }
                });
            }
        }
    }
}
