package ch.marclandolt.spa.ui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import ch.marclandolt.spa.R;
import ch.marclandolt.spa.backend.XmppService;
import ch.marclandolt.spa.config.Config;

public class HelpFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_help, container, false);

		Button needHelp = (Button) view.findViewById(R.id.needHelp);
		needHelp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogboxRallyOrderHelp();
			}
		});

		return view;
	}

	public void dialogboxRallyOrderHelp() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					orderHelp();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					return;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.sure_to_contact_supporter)
				.setPositiveButton(R.string.positive_button, dialogClickListener)
				.setNegativeButton(R.string.negative_button, dialogClickListener).show();
	}


public void orderHelp()
{
	Intent intent = new Intent(XmppService.SEND_TO_SERVICE);
	intent.putExtra(XmppService.CONNECT, "connect");
	intent.putExtra(XmppService.NEED_HELP, "help");
	getActivity().sendBroadcast(intent);

	Config.inSession=true;
	
	FragmentTransaction ft = getFragmentManager()
			.beginTransaction();
	ft.replace(R.id.fragment_place, new SearchFragment());
	ft.commit();
}
}