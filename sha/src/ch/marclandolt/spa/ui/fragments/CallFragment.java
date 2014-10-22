package ch.marclandolt.spa.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import ch.marclandolt.spa.R;
import ch.marclandolt.spa.backend.XmppService;

public class CallFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_call, container, false);

		Button accept = (Button) view.findViewById(R.id.accept);
		accept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
				intent.putExtra(XmppService.ACCEPT,"true");
				getActivity().sendBroadcast(intent);
				
			}
		});
		
		Button decline = (Button) view.findViewById(R.id.decline);
		decline.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
				intent.putExtra(XmppService.DECLINE,"true");
				getActivity().sendBroadcast(intent);
				
			}
		});
		
		return view;
	}

}