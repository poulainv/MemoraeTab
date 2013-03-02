package com.utc.fragments;

import java.util.Observable;
import java.util.Observer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.utc.R;
import com.utc.activities.CreatePostItActivity;
import com.utc.activities.MainActivity;
import com.utc.activities.MainActivity.fragments;
import com.utc.model.ApplicationModel;
import com.utc.resource.PostIt;

public class DetailPostItFragment extends Fragment implements Observer{
	
	static private volatile DetailPostItFragment inst ;
	
	private static String TAG = "DetailPostItFragment";
	
	private ApplicationModel appModel ;
	
	private TextView detailPostIt ;
	
	private TextView tagsPostIt ;
	
	private View mainViewFragment ;
	
	private PostIt p = new PostIt();
	
	 /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static DetailPostItFragment getInstance() {
    	if(inst == null){
    		inst = new DetailPostItFragment();
    	}
    	return inst;
		}
	
	
	private DetailPostItFragment(){
		super();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		
		mainViewFragment = inflater.inflate(R.layout.fragment_detail, container, false);	
		detailPostIt = (TextView) mainViewFragment.findViewById(R.id.postitdetailtext);
		detailPostIt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
			    	Bundle objetbunble = new Bundle();
					
					objetbunble.putParcelable("postit", p);
					Intent intent = new Intent(inst.getActivity(), CreatePostItActivity.class);
					intent.putExtras(objetbunble);
					startActivityForResult(intent, 0);
				
			}
		}); 
		
		tagsPostIt = (TextView) mainViewFragment.findViewById(R.id.postittagtext);
		
		((MainActivity)getActivity()).setCURRENT_FRAGMENT(fragments.DETAILPOSTIT);
		return mainViewFragment;
	}
	
	  @Override
	    public void onResume(){
	    	super.onResume();
	    	appModel.onResume();
	    	
	    }
	  
	  @Override
	    public void onPause(){
	    	super.onPause();
	    	appModel.setActivityChanged(true);
	    	
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


	@Override
	public void update(Observable arg0, Object pi) {
		
		if(pi instanceof PostIt){
			p = (PostIt) pi ;
			Log.i(TAG,"text is :"+p.getText());
			this.detailPostIt.setText(p.getText());
			if(p.getTagList().toString().replace("[","").replace("]", "")!="")
				this.tagsPostIt.setText("Tags : "+ p.getTagList().toString().replace("[","").replace("]", ""));
			else
				this.tagsPostIt.setText("Tags : Aucun tag défini");
		}
	}
	
}