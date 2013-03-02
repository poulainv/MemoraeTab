package com.utc.event;

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

public class PostItDragToLocalEvent implements OnDragListener {

	private ApplicationModel appModel ;
	
	private View targetLayout;
	
	
	public void setTargetView(final View targerView){
		this.targetLayout=targerView;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();
		Log.i("PostDragToLocalEvent","onDrag started");
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			// All involved view accept ACTION_DRAG_STARTED for
			// MIMETYPE_TEXT_PLAIN
			if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_INTENT)) {
				Log.i("PostItdrag", "ACTION_DRAG_STARTED accepted");
				return true; // Accept
			} else {
				Log.i("PostItdrag", "ACTION_DRAG_STARTED refused");
				return false; // reject
			}
		case DragEvent.ACTION_DRAG_ENTERED:
			Log.i("PostItdrag", "ACTION_DRAG_ENTERED");
			return true;
		case DragEvent.ACTION_DRAG_LOCATION:
				Log.v("PostItdrag", "ACTION_DRAG_LOCATION" + event.getX() + " : " + event.getY());
			return true;
		case DragEvent.ACTION_DRAG_EXITED:
			Log.i("PostItdrag", "ACTION_DRAG_Sexited");
			return true;
		case DragEvent.ACTION_DROP:
			// Gets the item containing the dragged data

			// If apply only if drop on buttonTarget
			if (v == targetLayout) {
	
				ClipData.Item item = event.getClipData().getItemAt(0);
				Intent intent = item.getIntent();
				Bundle bundle = intent.getExtras();
				bundle.setClassLoader(PostIt.class.getClassLoader());
				PostIt p = bundle.getParcelable("postit");
			

				if(p.getOrigin()==1 ){
					Log.i("PostItdrag", "ACTION_DRAG_DROP");
					Log.i("postit dropé  (to Local): "," "+p.getText());
					Log.i("postit dropé (toLocal) : origine :"," "+p.getOrigin());
					appModel.transfertPostItServerToLocal(p);
				}	else if(p.getOrigin()==2 ){
					Log.i("PostItdrag", "ACTION_DRAG_DROP");
					Log.i("postit dropé  (to Local): "," "+p.getText());
					Log.i("postit dropé (toLocal) : origine :"," "+p.getOrigin());
					appModel.transfertPostItTableToLocal(p);
				}
				//				droppedList.add(droppedItem);
//				droppedAdapter.notifyDataSetChanged();

				return true;
			} else {
				return false;
			}

		case DragEvent.ACTION_DRAG_ENDED:
			if (event.getResult()) {
				  Log.i("PostItdrag","SUCCESS");
			} else {
				  Log.i("PostItdrag","FAIL");
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


}
