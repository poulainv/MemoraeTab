package com.utc.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.utc.R;
import com.utc.activities.MainActivity;
import com.utc.activities.MainActivity.fragments;
import com.utc.model.ApplicationModel;
/**
 * Fragment d'identification
 * Useless depuis la mise en place des boites de dialogues
 */
public class AuthentificationFragment extends Fragment{
	
	Button buttonConnexion = null;
	
	EditText loginText;
	
	EditText passText;
	
	private View mainViewFragment ;
	
	private ApplicationModel appModel ;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		mainViewFragment = inflater.inflate(R.layout.fragment_auth, container, false);	

		loginText = (EditText) mainViewFragment.findViewById(R.id.login);
		passText = (EditText) mainViewFragment.findViewById(R.id.password);
		buttonConnexion = (Button) mainViewFragment.findViewById(R.id.buttonConnexion);
		buttonConnexion.setOnClickListener(new OnClickListener() {
			        
			public void onClick(View v) {
				 appModel.getAsyncTaskCheckUser().execute(new String[]
				    		{loginText.getText().toString(),passText.getText().toString()});
				 FragmentTransaction ft = getFragmentManager().beginTransaction();
				   ft.replace(R.id.layoutRight, DetailPostItFragment.getInstance(),"detilfragment").commit();
			}
		});
		((MainActivity)getActivity()).setCURRENT_FRAGMENT(fragments.AUTHENTIFICATION);
		return mainViewFragment;
	}
	
	/**
	 * @return the appModel
	 */
	public ApplicationModel getAppModel() {
		return appModel;
	}

	/**
	 * @param appModel the appModel to set
	 */
	public void setAppModel(ApplicationModel appModel) {
		this.appModel = appModel;
	}

	
}