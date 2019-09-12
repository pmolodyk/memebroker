package com.jattilainen.memebroker.RandomMemes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by User on 2/28/2017.
 */

public class RandomMemesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    View view;
    RandomMemesAdapter adapter;
    boolean created = false;
    private List<RandomMemesItem> memes = new ArrayList<>();
    RecyclerView recyclerView;
    String finalLanguage = "English";
    SwipeRefreshLayout swipeRefreshLayout;
    boolean needLoadMore = false;
    Random rand;
    MainActivity context;
    private Set<Integer> inList = new HashSet<>();
    public void setContext(MainActivity context) {
        this.context = context;
    }
    boolean searchViewOpened = false;
    public RandomMemesFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onRefresh() {
        if (needLoadMore && !searchViewOpened) {
            firstLoadRandom();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public interface OnButtonListener {
        void onRandomLanguageSelectButtonSelected();
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!created) {
            rand = new Random();
            Log.e("tor", "hi");
            view = inflater.inflate(R.layout.fragment_random_memes, container, false);
            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.languages_array, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            adapter = new RandomMemesAdapter(memes, (MainActivity)getActivity());
            created = true;
            recyclerView = view.findViewById(R.id.randomMemesRecyclerView);
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
                                loadMore();
                            }
                        }
                    }
                }
            });
            swipeRefreshLayout = view.findViewById(R.id.randomMemesRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    firstLoadRandom();
                }
            });
        }
        return view;
    }

    private void firstLoadRandom() {
        inList.clear();
        needLoadMore = false;
        final int size = memes.size();
        memes.clear();
        adapter.onDetachedFromRecyclerView(recyclerView);
        adapter = new RandomMemesAdapter(memes, context);
        recyclerView.setAdapter(adapter);
        loadDataToList();
    }
    private void loadMore() {
        needLoadMore = false;
        loadDataToList();
    }
    private void loadDataToList() {
        final String worklang = finalLanguage;
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference amountRef = dbref.child("images").child(worklang).child("amount");
        amountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                needLoadMore = true;
                if (!dataSnapshot.exists()) {
                    Toast.makeText(context, R.string.noMemesOnServer, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                Integer amountOfMemes = dataSnapshot.getValue(Integer.class);
                if (amountOfMemes == null || amountOfMemes == 0) {
                    Toast.makeText(context, R.string.noMemesOnServer, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                final int oldSize = memes.size();
                int i = oldSize;
                if (memes.size() == 0) {
                    adapter.notifyDataSetChanged();
                }
                for(;i < oldSize + Constants.RANDOM_ONE_BLOCK_SIZE; i++) {
                    int newId = rand.nextInt(amountOfMemes) + 1;
                    Log.e("contains", String.valueOf(newId));
                    if (!inList.contains(newId)) {
                        Log.e("contains2", inList.toString() + " " + String.valueOf(newId));
                        addImage(newId);
                        Log.e("conteins2", inList.toString());
                        inList.add(newId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void addImage(Integer newId)
    {
        final String worklang = finalLanguage;
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgref = dbref.child("images").child(worklang).child("images");
        Log.e("got id:", "onDataChange: " +  newId);
        imgref.orderByChild("id").equalTo(newId).addListenerForSingleValueEvent(new ValueEventListener() {
            double ratio = 0;
            String hash = "";
            String url = "";
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer reports = dataSnapshot.child("reports").getValue(Integer.class);
                needLoadMore = true;
                if (!dataSnapshot.exists() || (reports != null &&  reports > Constants.MAX_REPORTS))
                {
                    return;
                }
                Log.e("ratio", "onDataChange: ");
                Log.e("ratio", String.valueOf(ratio));
                for (DataSnapshot memeSnapshot : dataSnapshot.getChildren())
                {
                    hash = (memeSnapshot).getKey();
                    int height = Integer.parseInt(memeSnapshot.child("height").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    int width = Integer.parseInt(memeSnapshot.child("width").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    ratio = (double)height/width;
                }
                if (!("").equals(hash)) {
                    StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + hash);
                    chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri url) {
                            swipeRefreshLayout.setRefreshing(false);
                            memes.add(new RandomMemesItem(hash, worklang, ratio, url.toString()));
                            adapter.notifyItemInserted(memes.size());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void setImage(String memeId) {
        final String worklang = finalLanguage;
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgref = dbref.child("images").child(worklang).child("images");
        Integer id = Integer.parseInt(memeId);
        swipeRefreshLayout.setRefreshing(true);
        Log.e("setimage", memeId);
        imgref.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            double ratio = 0;
            String hash = "";
            String url = "";
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer reports = dataSnapshot.child("reports").getValue(Integer.class);
                if (!dataSnapshot.exists() || (reports != null &&  reports > Constants.MAX_REPORTS)) {
                    Toast.makeText(context, R.string.memeNotFound, Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                Log.e("ratio", "onDataChange: ");
                Log.e("ratio", String.valueOf(ratio));
                for (DataSnapshot memeSnapshot : dataSnapshot.getChildren())
                {
                    hash = (memeSnapshot).getKey();
                    int height = Integer.parseInt(memeSnapshot.child("height").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    int width = Integer.parseInt(memeSnapshot.child("width").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    ratio = (double)height/width;
                }
                if (!("").equals(hash)) {
                    memes.clear();
                    adapter.notifyDataSetChanged();

                    StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + hash);
                    chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri url) {
                            memes.add(new RandomMemesItem(hash, worklang, ratio, url.toString()));
                            adapter.notifyItemInserted(memes.size());
                        }
                    });
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.meme_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.memeFragment_search_button);
        final MenuItem languageItem = menu.findItem(R.id.memeFragment_language_button);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setQueryHint(context.getString(R.string.meme_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                setImage(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchViewOpened = false;
                languageItem.setVisible(true);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageItem.setVisible(false);
                searchViewOpened = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.memeFragment_language_button:
                Log.e("search", "onOptionsItemSelected: ");
                RandomMemesFragment.OnButtonListener listener = (RandomMemesFragment.OnButtonListener) getActivity();
                if (listener != null) {
                    listener.onRandomLanguageSelectButtonSelected();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void setFinalLanguage(String finalLanguage) {
        Log.e("langchoose", "finish");
        searchViewOpened = false;//because it stays true if user changes fragment
        if (!this.finalLanguage.equals(finalLanguage)) {
            this.finalLanguage = finalLanguage;
            if (created) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        firstLoadRandom();
                    }
                });
            }
        }
    }
}