package com.utc.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.utc.R;
import com.utc.model.ApplicationModel;
import com.utc.resource.PostIt;
import com.utc.resource.PostItAdapter;
import com.utc.activities.CreatePostItActivity;
import com.utc.activities.MainActivity;
import com.utc.activities.MainActivity.fragments;
import com.utc.event.PostItDragFromLocalEvent;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;

public class ListPostItFragment extends ListFragment implements Observer {

	private static final int MENU_CONTEXT_DELETE = 0;
	private static final int MENU_CONTEXT_MODIFY = 1;
	private static String TAG = "ListPostItFragment";
	private View mainViewFragment ;
	
	private ListPostItFragment inst = this;

	private ApplicationModel appModel;

	public PostItDragFromLocalEvent postItDragEventListener = new PostItDragFromLocalEvent();

	private List<PostIt> listPostIt = new ArrayList<PostIt>();

	private View targetLayoutServer;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "Creation");

		this.getListView().setOnItemLongClickListener(
				listSourceItemLongClickListener);
		this.getListView().setOnDragListener(postItDragEventListener);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "Item clicked: " + id);
		int i = 0;
		while(((PostItAdapter)l.getAdapter()).getView(i)!=null){
			((PostItAdapter)l.getAdapter()).getView(i).setBackgroundColor(Color.WHITE);
			i++;
		}
		v.setBackgroundColor(0xFFB9B9B9);
		if (appModel == null) {
			Log.i(TAG,
					"appModel is null have you setAppModel ?");
			throw new NullPointerException();
		} else {
			appModel.postItSelected((int) id);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		appModel.onResume();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		this.registerForContextMenu(this.getListView());
	}

	@Override
	public void onPause() {
		super.onPause();
		appModel.setActivityChanged(true);

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainViewFragment = inflater.inflate(R.layout.fragment_list, container, false);
		return mainViewFragment;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable arg0, Object listP) {
		if (listP instanceof List<?> && listP.getClass().equals(listPostIt.getClass())) {
			try {
				// Ces deux ligne servent juste a verifier que j'ai bien des
				// postit
				// si ce sont autre chose cela declenche une exception qui est
				// gŽrŽ
				listPostIt = (List<PostIt>) listP;
				if (listPostIt.size() == 0)
					return;
				PostIt pp = listPostIt.get(0);
				if (pp.getOrigin() == 0) {
					PostItAdapter adapter = new PostItAdapter(getActivity().getApplicationContext(),
							(List<PostIt>) listPostIt); // création de l'adapter
														// ? toujours pas
														// compris ce que
														// c'était..
					setListAdapter(adapter);
				}

			}

			catch (ClassCastException e) {
			} catch (NullPointerException e) {
			}
		}

	}

	@Override
	public boolean onContextItemSelected (MenuItem item){
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Log.i("onContextItemSelected","postit selected : "+info.position);
		switch(item.getItemId()){
		case MENU_CONTEXT_DELETE :
				Log.i("onContextItemSelected","postit DELETED");
				appModel.removePostItLocal(listPostIt.get(info.position).getGuid());
				listPostIt.remove(info.position);
				displayPostItList();
			return true;
		case MENU_CONTEXT_MODIFY :
			Log.i("onContextItemSelected","postit MODIFIED");
			Bundle objetbunble = new Bundle();
			objetbunble.putParcelable("postit", appModel.getPostItListCache().get(info.position));
			Intent intent = new Intent(inst.getActivity(), CreatePostItActivity.class);
			intent.putExtras(objetbunble);
			startActivityForResult(intent, 0);
			return true;
			default :
				return false ;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	  if (v==this.getListView()) {
	    menu.setHeaderTitle("Menu Post-It");
	    menu.add(0, MENU_CONTEXT_DELETE, 0, R.string.supprimer);
	    menu.add(0, MENU_CONTEXT_MODIFY, 0, R.string.modifier);
	  }
	}
	
	/**
	 * @return the appModel
	 */
	public ApplicationModel getAppModel() {
		return appModel;
	}

	/**
	 * @param appModel
	 *            the appModel to set
	 */
	public void setAppModel(ApplicationModel appModel) {
		this.appModel = appModel;
		this.postItDragEventListener.setAppModel(appModel);
	}

	/**
	 * Affiche la liste des posts it
	 */
	public void displayPostItList() {
		if (appModel == null) {
			Log.i(TAG, "appModel is null have you setAppModel ?");
			throw new NullPointerException();
		} else {
			listPostIt = appModel.getListPostItLocalDb();
			appModel.postItSelected(0);
			PostItAdapter adapter = new PostItAdapter(getActivity().getApplicationContext(), listPostIt); // création
			setListAdapter(adapter);
		}
	}

	OnItemLongClickListener listSourceItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
			Log.i(TAG, "LongclickPostIt");
			// Pas tres tres propre
			MainActivity ma = (MainActivity) inst.getActivity();
			if (((ma.getCURRENT_FRAGMENT().equals(fragments.MEMORAESERVER))) || ((ma.getCURRENT_FRAGMENT().equals(fragments.TABLEFRAGMENT)))) {
				MemoraeFragment mf = ma.getMemoraeFragment();
				TableFragment tf = ma.getTableFragment();
				inst.setTargetLayout(mf.getMainViewFragment(),tf.getMainViewFragment());
				Bundle objetbunble = new Bundle();

				objetbunble.putParcelable("postit", appModel.getListPostItLocalDb().get(position));
				Intent intent = new Intent();
				intent.putExtras(objetbunble);

				// Selected item is passed as item in dragData
				ClipData.Item item = new ClipData.Item(intent);

				String[] clipDescription = { ClipDescription.MIMETYPE_TEXT_INTENT };
				Log.i("onitemlongitempositiclick", "LongClick OK");

				ClipData dragData = new ClipData("postit", clipDescription, item);
				DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

				v.startDrag(dragData, // ClipData
						myShadow, // View.DragShadowBuilder
						item, // Object myLocalState
						0); // flags
				return true;
			} else {
				return false;
			}
		}
	};
	private View targetLayoutTable;

	private static class MyDragShadowBuilder extends View.DragShadowBuilder {
		private static Drawable shadow;

		public MyDragShadowBuilder(View v) {
			super(v);
			shadow = new ColorDrawable(Color.LTGRAY);
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			int width = getView().getWidth();
			int height = getView().getHeight();

			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width / 2, height / 2);
		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}

	}

	/**
	 * @return the targetLayout
	 */
	public View getTargetLayoutServer() {
		return targetLayoutServer;
	}

	/**
	 * @param targetLayout
	 *            the targetLayout to set
	 */
	private void setTargetLayout(final View targetLayoutServer, final View targetLayoutTable) {
		this.targetLayoutServer = targetLayoutServer;
		this.targetLayoutTable = targetLayoutTable;

		if(targetLayoutServer!=null)
			this.targetLayoutServer.setOnDragListener(postItDragEventListener);
		if(targetLayoutTable!=null)
			this.targetLayoutTable.setOnDragListener(postItDragEventListener);
		
		this.postItDragEventListener.setTargetView(targetLayoutServer,targetLayoutTable);
	}

	/**
	 * @return the mainViewFragment
	 */
	public View getMainViewFragment() {
		return mainViewFragment;
	}

	/**
	 * @param mainViewFragment the mainViewFragment to set
	 */
	public void setMainViewFragment(View mainViewFragment) {
		this.mainViewFragment = mainViewFragment;
	}

}
