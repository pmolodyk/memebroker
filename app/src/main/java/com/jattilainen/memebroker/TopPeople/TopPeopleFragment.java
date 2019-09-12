package com.jattilainen.memebroker.TopPeople;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.TopMemes.TopMemesItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.min;

public class TopPeopleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    boolean needLoadMore = false;
    TopPeopleAdapter adapter;
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    private List<TopPeopleItem> people = new ArrayList<>();
    private List<TopPeopleItem> allPeople = new ArrayList<>();
    private HashMap<String, Integer> peopleOrders = new HashMap<>();
    SwipeRefreshLayout swipeRefreshLayout;
    boolean searchViewOpened = false;
    RecyclerView recyclerView;
    Context context;
    public void setContext(Context context) {
        this.context = context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_people, container, false);
        swipeRefreshLayout = view.findViewById(R.id.topPeopleRefreshLayout);
        recyclerView = view.findViewById(R.id.topPeopleRecyclerView);
        adapter = new TopPeopleAdapter(people, getActivity());
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                firstLoadTop();
            }
        });
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.top_people_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.people_searchview);
        final MenuItem updateItem = menu.findItem(R.id.people_update);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setQueryHint(getContext().getString(R.string.people_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                loadSearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                loadSearch(s);
                Log.e("textChanged", "gg");
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchViewOpened = false;
                updateItem.setVisible(true);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateItem.setVisible(false);
                searchViewOpened = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.people_update:
                if (needLoadMore) {
                    firstLoadTop();
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void firstLoadTop() {
        Log.e("firstload", "firstLoadTop: 2");
        swipeRefreshLayout.setRefreshing(true);
        peopleOrders.clear();
        needLoadMore = false;
        final DatabaseReference peopleRef = FirebaseDatabase.getInstance().getReference().child("users");
        peopleRef.orderByChild("money").limitToFirst(Constants.TOP_PEOPLE_TOTAL_LOAD_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("firstload", "firstLoadTop: 3" + dataSnapshot.toString());
                people.clear();
                allPeople.clear();
                for (DataSnapshot personSnapshot : dataSnapshot.getChildren()) {
                    if (!(personSnapshot.child("banned").exists() && personSnapshot.child("banned").getValue(Integer.class) == 1)) {
                        allPeople.add(0, new TopPeopleItem(personSnapshot.getKey(), personSnapshot.child("money").getValue(Integer.class), 0));
                    }
                }
                for (int i = 0; i < allPeople.size(); i++) {
                    TopPeopleItem topPeopleItem = allPeople.get(i);
                    peopleOrders.put(topPeopleItem.getName(), i + 1);
                    topPeopleItem.setOrder(i + 1);
                }
                //progressBar.setVisibility(View.GONE);
                adapter = new TopPeopleAdapter(people, context);
                recyclerView.setAdapter(adapter);
                loadDataToList();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void loadDataToList() {
        needLoadMore = false;
        final int oldSize = people.size();
        int i = oldSize;
        Log.e("loaddata", String.valueOf(allPeople.size()));
        for(;i < min(oldSize + Constants.TOP_PEOPLE_ONE_BLOCK_SIZE, allPeople.size()); i++) {
            people.add(allPeople.get(i));
            adapter.notifyItemInserted(people.size());
        }
        needLoadMore = true;
    }

    @Override
    public void onRefresh() {
        Log.e("firstload", "firstLoadTop: 1");
        if (needLoadMore && !searchViewOpened) {
            firstLoadTop();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    private void loadSearch(final String query) {
        Log.e("searchload", "loadSearch: " + query);
        if ("".equals(query)) {
            firstLoadTop();
        } else {
            swipeRefreshLayout.setRefreshing(true);
            final DatabaseReference peopleRef = FirebaseDatabase.getInstance().getReference().child("users");
            peopleRef.orderByKey().startAt(query).limitToFirst(Constants.TOP_PEOPLE_TOTAL_LOAD_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    people.clear();
                    allPeople.clear();
                    adapter.notifyDataSetChanged();
                    Log.e("pepsearch", String.valueOf(dataSnapshot.getChildrenCount()));
                    for (DataSnapshot personSnapshot : dataSnapshot.getChildren()) {
                        if (personSnapshot.getKey() != null && personSnapshot.getKey().startsWith(query)) {
                            allPeople.add(new TopPeopleItem(personSnapshot.getKey(), personSnapshot.child("money").getValue(Integer.class), 0));
                        } else if (personSnapshot.getKey() == null) {
                            swipeRefreshLayout.setRefreshing(false);
                            return;
                        }
                    }
                    for (int i = 0; i < allPeople.size(); i++) {
                        TopPeopleItem topPeopleItem = allPeople.get(i);
                        if (peopleOrders.containsKey(topPeopleItem.getName())) {
                            topPeopleItem.setOrder(peopleOrders.get(topPeopleItem.getName()));
                        } else {
                            topPeopleItem.setOrder(Constants.TOP_PEOPLE_TOTAL_LOAD_SIZE);
                        }
                    }
                    Collections.sort(allPeople, new Comparator<TopPeopleItem>() {
                        @Override
                        public int compare(TopPeopleItem topPeopleItem, TopPeopleItem t1) {
                            int cmp;
                            if (topPeopleItem.getMoney() < t1.getMoney()) {
                                cmp = 1;
                            } else if (topPeopleItem.getMoney() > t1.getMoney()) {
                                cmp = -1;
                            } else {
                                cmp = 0;
                            }
                            return cmp;
                        }
                    });
                    //progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    loadDataToList();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
