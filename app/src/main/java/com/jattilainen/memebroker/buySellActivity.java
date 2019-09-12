package com.jattilainen.memebroker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.Assets.AssetsItem;

import uk.co.senab.photoview.PhotoViewAttacher;

public class buySellActivity extends AppCompatActivity {
    StorageReference storeref = FirebaseStorage.getInstance().getReference();
    ImageView imageView;
    Button ok;
    TextView price;
    TextView income;
    TextView left;
    SeekBar amount;
    RadioGroup option;
    RadioButton sell;
    RadioButton buy;
    long haveMemes = 0;
    long haveMoney = 0;
    Long haveAllMoney = 0L;//cocite vse
    long memePrice = 0;
    long totalExpenses = 0;
    double ratio;
    boolean memeDataLoaded = false;
    boolean assetsDataLoaded = false;
    boolean moneyDataLoaded = false;
    boolean orderConfirmed = false;
    String nowChosen = "no";
    String memeHash;
    String memeLanguage;
    TextView memeName;
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    DatabaseReference moneyRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("money");
    DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("images");
    MenuItem confirmItem;
    Integer id;
    String author;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);
        Toolbar toolbar = (Toolbar) findViewById(R.id.buySellActivityToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageView = findViewById(R.id.activeMeme);
        price = findViewById(R.id.currPrice);
        income = findViewById(R.id.income);
        left = findViewById(R.id.left);
        amount = findViewById(R.id.amount);
        amount.setEnabled(false);
        option = findViewById(R.id.options);
        sell = findViewById(R.id.sell);
        buy = findViewById(R.id.buy);
        memeName = findViewById(R.id.buySellMemeNameTextView);
        if (!getIntent().hasExtra("memeHash")) {
            Toast.makeText(buySellActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
            finish();
        }
        memeHash = getIntent().getStringExtra("memeHash");
        memeLanguage = getIntent().getStringExtra("memeLanguage");
        url = getIntent().getStringExtra("url");
        Log.e("buyurl", url);
        StorageReference chosen = storeref.child("images/" + memeHash);

        final DatabaseReference curMemeRef = FirebaseDatabase.getInstance().getReference().child("images").child(memeLanguage).child("images").child(memeHash);
        final DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference().child("images").child(memeLanguage).child("images").child(memeHash).child("price");
        curMemeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memeDataLoaded = true;
                if (dataSnapshot.exists()) {
                    memePrice = dataSnapshot.child("price").getValue(Long.class);
                    price.setText(String.valueOf(memePrice));
                    id = dataSnapshot.child("id").getValue(Integer.class);
                    author = dataSnapshot.child("author").getValue(String.class);
                    recountMax();
                    int height = Integer.parseInt(dataSnapshot.child("height").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    int width = Integer.parseInt(dataSnapshot.child("width").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                    ratio = (double)height/width;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        memeRef.child(memeHash).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                buy.setEnabled(true);
                if (orderConfirmed) {
                    return;
                }
                assetsDataLoaded = true;
                if (dataSnapshot.exists()) {
                    sell.setEnabled(true);
                    AssetsItem serverItem = dataSnapshot.getValue(AssetsItem.class);
                    haveMemes = serverItem.getAmount();
                    totalExpenses = serverItem.getTotalExpenses();
                } else {
                    sell.setEnabled(false);
                }
                recountMax();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        moneyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moneyDataLoaded = true;
                if (!dataSnapshot.exists()) {
                    return;
                }
                haveAllMoney = dataSnapshot.getValue(Long.class);
                haveMoney = haveAllMoney * 2 / 3;
                recountMax();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        option.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                amount.setEnabled(true);
                if (checkedId == R.id.sell) {
                    nowChosen = "sell";
                } else {
                    nowChosen = "buy";
                }
                recountMax();
            }
        });
        amount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                recountValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (url != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(buySellActivity.this).load(url).apply(requestOptions).into(imageView);
        }
        PhotoViewAttacher x = new PhotoViewAttacher(imageView);
    }
    long costToBuy(long startPrice, int want) {
        startPrice++;
        return (2 * startPrice + (want - 1)) * want / 2;
    }
    long costToSell(long startPrice, int want) {
        if (want > startPrice) {
            want = (int)startPrice;
        }
        return (2 * startPrice - (want - 1)) * want / 2;
    }
    void recountMax() {
        if (id != null && author != null) {
            SpannableString ss = new SpannableString(getString(R.string.meme_name_with_author, String.valueOf(id), author));
            Log.e("spannable", getString(R.string.meme_name_with_author, String.valueOf(id), author));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent personProfileActivity = new Intent(buySellActivity.this, com.jattilainen.memebroker.PersonProfile.PersonProfileActivity.class);
                    personProfileActivity.putExtra("name", author);
                    startActivity(personProfileActivity);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            ss.setSpan(clickableSpan, ss.length() - author.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            memeName.setText(ss);
            memeName.setMovementMethod(LinkMovementMethod.getInstance());
        }
        if (!memeDataLoaded || !assetsDataLoaded || !moneyDataLoaded) {
            return;
        }
        if ("sell".equals(nowChosen)) {
            amount.setMax((int)haveMemes);
        } else if ("buy".equals(nowChosen)){
            int left = 0;
            int right = 1000000000; //9 zero, 9 нулей))) pitux
            while (right - left > 1) {
                int mid = (left + right) / 2;
                if (costToBuy(memePrice, mid) <= haveMoney) {
                    left = mid;
                } else {
                    right = mid;
                }
            }
            amount.setMax(left);
        }
        recountValues();
    }

    void recountValues() {
        int want = amount.getProgress();
        if ("no".equals(nowChosen)) {
            left.setText(String.valueOf(haveAllMoney));
        } else if ("sell".equals(nowChosen)) {
            income.setText(getString(R.string.incomeWithSign, "+", costToSell(memePrice, want)));
            price.setText(getString(R.string.memePriceWithDelta, memePrice, "-" + String.valueOf(want)));
            income.setTextColor(Color.parseColor("#00ff00"));
            left.setText(String.valueOf(costToSell(memePrice, want) + haveAllMoney));
        } else if ("buy".equals(nowChosen)){
            price.setText(getString(R.string.memePriceWithDelta, memePrice, "+" + String.valueOf(want)));
            income.setText(getString(R.string.incomeWithSign, "-", costToBuy(memePrice, want)));
            income.setTextColor(Color.parseColor("#ff0000"));
            left.setText(String.valueOf(-costToBuy(memePrice, want) + haveAllMoney));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.confirm_menu, menu);
        confirmItem = menu.findItem(R.id.confirm_menu_confirm);
        confirmItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                confirmItem.setEnabled(false);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.confirm_menu_confirm:
                confirm();
        }
        return super.onOptionsItemSelected(item);
    }
    private void confirm() {
        orderConfirmed = true;
        final Long totalMoney = haveAllMoney;
        if (memeHash == null || memeLanguage == null) {
            Toast.makeText(buySellActivity.this, R.string.ConnectionError, Toast.LENGTH_LONG).show();
            return;
        }
        if ("no".equals(nowChosen)) {
            finish();
            return;
        }
        final int want = amount.getProgress();
        if (want == 0) {
            finish();
            return;
        }
        if (!memeDataLoaded || !assetsDataLoaded || !moneyDataLoaded) {
            Toast.makeText(buySellActivity.this, R.string.ConnectionError, Toast.LENGTH_LONG).show();
            return;
        }
        final DatabaseReference priceRef = FirebaseDatabase.getInstance().getReference().child("images").child(memeLanguage).child("images").child(memeHash).child("price");
        priceRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Log.e("buySell", "doTransaction: ");
                Long currPrice = mutableData.getValue(Long.class);
                if (currPrice == null) {
                    return Transaction.success(mutableData);
                }
                if ("buy".equals(nowChosen)) {
                    FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(memeHash).child("assets").child(name).setValue(0);
                    moneyRef.setValue(totalMoney - costToBuy(currPrice, want));
                    memeRef.child(memeHash).setValue(new AssetsItem(memeHash, memeLanguage, totalExpenses + costToBuy(currPrice, want),want + haveMemes, ratio, "zdes kostyl"));
                    mutableData.setValue(currPrice + (long) amount.getProgress());
                } else if ("sell".equals(nowChosen)) {
                    moneyRef.setValue(totalMoney + costToSell(currPrice, want));
                    memeRef.child(memeHash).setValue(new AssetsItem(memeHash, memeLanguage, totalExpenses - costToSell(currPrice, want),haveMemes - want, ratio, "zdes kostyl"));
                    if (haveMemes - want == 0) {
                        FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(memeHash).child("assets").child(name).removeValue();
                        memeRef.child(memeHash).removeValue();
                    }
                    mutableData.setValue(currPrice - (long) amount.getProgress());
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
