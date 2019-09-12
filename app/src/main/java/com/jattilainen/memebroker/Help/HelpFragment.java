package com.jattilainen.memebroker.Help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jattilainen.memebroker.R;


public class HelpFragment extends Fragment {
    public HelpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_help, container, false);
        LinearLayout openBuySellHelp = v.findViewById(R.id.help_bs_layout);
        LinearLayout openUploadHelp = v.findViewById(R.id.help_up_layout);
        LinearLayout openRespHelp = v.findViewById(R.id.help_respect_layout);
        LinearLayout openShortlistHelp = v.findViewById(R.id.help_shortlist_layout);
        openBuySellHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpBuySellActivity.class);
                getActivity().startActivity(intent);
            }
        });
        openUploadHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpUploadActivity.class);
                getActivity().startActivity(intent);
            }
        });
        openRespHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpRespectActivity.class);
                getActivity().startActivity(intent);
            }
        });
        openShortlistHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HelpShortlist.class);
                getActivity().startActivity(intent);
            }
        });
        return v;
    }

}
