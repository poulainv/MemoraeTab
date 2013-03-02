package com.utc.event;


import com.utc.agent.PostItAgent;
import com.utc.fragments.TableFragment;
import com.utc.model.ApplicationModel;
import com.utc.resource.PostIt;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/**
 * Gère tout les postit venant du local
 * @author vincentpoulain
 *
 */
public class PostItDragFromLocalEvent implements OnDragListener {


	private TableFragment tableFragment ; 
	
	private ApplicationModel appModel ;
	
	private View targetLayoutServer;
	private View targetLayoutTable;
	
	
	
	public void setTargetView(final View targetViewServer, final View targetViewTable){
		this.targetLayoutServer=targetViewServer;
		this.targetLayoutTable=targetViewTable;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();
		Log.i("PostItDragFromLocalEvent","onDrag started");
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			// All involved view accept ACTION_DRAG_STARTED for
			// MIMETYPE_TEXT_PLAIN
			if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
				Log.i("PostItDragFromLocalEvent", "ACTION_DRAG_STARTED accepted");
				return true; // Accept
			} else {
				Log.i("PostItDragFromLocalEvent", "ACTION_DRAG_STARTED refused");
				return false; // reject
			}
		case DragEvent.ACTION_DRAG_ENTERED:
			Log.i("PostItDragFromLocalEvent", "ACTION_DRAG_ENTERED");
			return true;
		case DragEvent.ACTION_DRAG_LOCATION:
				Log.v("PostItDragFromLocalEvent", "ACTION_DRAG_LOCATION" + event.getX() + " : " + event.getY());
			return true;
		case DragEvent.ACTION_DRAG_EXITED:
			Log.i("PostItDragFromLocalEvent", "ACTION_DRAG_Sexited");
			return true;
		case DragEvent.ACTION_DROP:
			// Gets the item containing the dragged data
			Log.i("PostItDragFromLocalEvent", "ACTION_DROP");
			// If apply only if drop on buttonTarget
			if (v == targetLayoutServer) {
	
				ClipData.Item item = event.getClipData().getItemAt(0);
				Intent intent = item.getIntent();
				Bundle bundle = intent.getExtras();
				bundle.setClassLoader(PostIt.class.getClassLoader());
				PostIt p = bundle.getParcelable("postit");
				Log.i("PostItdrag", "ACTION_DRAG_DROP");
				Log.i("postit dropé (toServer) : "," "+p.getText());
				Log.i("postit dropé (toServer) : origine :"," "+p.getOrigin());

				if(p.getOrigin()==0){	
					appModel.getAsyncTaskAddPostit().execute(new PostIt[] {p});
				}

				return true;
			} if (v == targetLayoutTable) {
	
				ClipData.Item item = event.getClipData().getItemAt(0);
				Intent intent = item.getIntent();
				Bundle bundle = intent.getExtras();
				bundle.setClassLoader(PostIt.class.getClassLoader());
				PostIt p = bundle.getParcelable("postit");
				Log.i("PostItDragFromLocalEvent", "ACTION_DRAG_DROP");
				Log.i("postit dropé (toTable) : "," "+p.getText());
				Log.i("postit dropé (toTable) : origine :"," "+p.getOrigin());

				if(p.getOrigin()==0){	
					PostItAgent pAgent = tableFragment.getAgent();
					pAgent.sendPostIt(p); 
				}

				return true;
			} else {
				return false;
			}

		case DragEvent.ACTION_DRAG_ENDED:
			if (event.getResult()) {
				  Log.i("PostItDragFromLocalEvent","SUCCESS");
			} else {
				  Log.i("PostItDragFromLocalEvent","FAIL");
			}
			;
			return true;
		default: // unknown case
			  Log.i("PostItdrag","what happen ??");
			return false;

		}
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

	/**
	 * @return the tableFragment
	 */
	public TableFragment getTableFragment() {
		return tableFragment;
	}

	/**
	 * @param tableFragment the tableFragment to set
	 */
	public void setTableFragment(TableFragment tableFragment) {
		this.tableFragment = tableFragment;
	}


}
