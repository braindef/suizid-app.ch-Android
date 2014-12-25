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

public class EvaluateFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_evaluate, container, false);

		Button rescued = (Button) view.findViewById(R.id.rescued);
		rescued.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
				intent.putExtra(XmppService.RESCUED,"true");
				getActivity().sendBroadcast(intent);
				
			}
		});
		
		Button helped = (Button) view.findViewById(R.id.helped);
		helped.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
				intent.putExtra(XmppService.HELPED,"true");
				getActivity().sendBroadcast(intent);
				
			}
		});
		
		
		Button madeworse = (Button) view.findViewById(R.id.madeworse);
		madeworse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
				intent.putExtra(XmppService.MADEWORSE,"true");
				getActivity().sendBroadcast(intent);
				
			}
		});
		
		return view;
	}

}