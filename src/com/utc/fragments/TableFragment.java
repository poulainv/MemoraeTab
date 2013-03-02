package com.utc.fragments;

import jade.android.MicroRuntimeService;
import jade.android.RuntimeCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.DragShadowBuilder;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.utc.R;
import com.utc.activities.MainActivity;
import com.utc.activities.MainActivity.fragments;
import com.utc.agent.PostItAgent;
import com.utc.agent.PostItAgent.PostItReceiver;
import com.utc.agent.PostItAgent.PostItSet;
import com.utc.event.PostItDragToLocalEvent;
import com.utc.model.ApplicationModel;
import com.utc.resource.PostIt;
import com.utc.resource.PostItAdapter;

/**
 * Si on est authentifié, ce fragment permet de récupérer la liste des postits
 * après avoir choisi la memoire, le concept et l'organisation dans laquelle on
 * souahite travailler
 * 
 */
public class TableFragment extends Fragment implements Observer, PostItReceiver, PostItSet {

	public static final String POSTITLIST = "POSTITLIST";
	public static final String STRING_ANSWER = "getlistpostit";

	static private volatile TableFragment inst;

	private PullToRefreshListView postItList = null;

	private View mainViewFragment;

	private ApplicationModel appModel;

	private Button tableConnectionButton;

	private PostItDragToLocalEvent postItDragEventToLocalListener = new PostItDragToLocalEvent();
	
	private TextView ipAddress;

	private MicroRuntimeService mr;

	private PostItAgent agent;

	private View targetLayout;

	/**
	 * Create a new instance of DetailsFragment, initialized to show the text at
	 * 'index'.
	 */
	public static TableFragment getInstance() {
		if (inst == null) {
			inst = new TableFragment();
		}
		return inst;
	}

	private TableFragment() {
		super();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i("TableFragment", "Creation");
		postItList.setOnItemLongClickListener(listSourceItemLongClickListener);
		postItList.setOnDragListener(postItDragEventToLocalListener);
		ipAddress.setText("192.168.1.16");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainViewFragment = inflater.inflate(R.layout.fragment_table, container, false);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(STRING_ANSWER);
		this.getActivity().registerReceiver(new CustomReceiver(), intentFilter);

		appModel.addObserver(this);
		postItList = (PullToRefreshListView) mainViewFragment.findViewById(R.id.listpostittable);
		postItList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				agent.requestPostIts();
			}
		});
		((MainActivity) getActivity()).setCURRENT_FRAGMENT(fragments.TABLEFRAGMENT);

		ipAddress = (TextView) mainViewFragment.findViewById(R.id.iptable);
		tableConnectionButton = (Button) mainViewFragment.findViewById(R.id.buttonConnexionTable);
		tableConnectionButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mr = new MicroRuntimeService();
				 String s = ipAddress.getText().toString();
//				String s = "192.168.1.16";
				mr.startAgentContainer(s, 1099, new RuntimeCallback<Void>() {

					@Override
					public void onFailure(Throwable arg0) {
						Log.e("tablefragment thread 1", "Connexion agent failure");
					}

					@Override
					public void onSuccess(Void arg0) {
						Log.i("tablefragment thread 1", "Connection agent success");
						startAgent();
					}
				});

			}
		});
		
		return mainViewFragment;
	}

	private void startAgent() {

		mr.startAgent(UUID.randomUUID().toString(), PostItAgent.class.getName(), new Object[] {
				PostItAgent.SD_TABLET_NAME, PostItAgent.SD_TABLE_NAME, this, this }, new RuntimeCallback<Void>() {

			@Override
			public void onFailure(Throwable arg0) {
				Log.e("tablefragment thread 2", "Creation agent failure");
				// broadcastButtonColor(false);
			}

			@Override
			public void onSuccess(Void arg0) {
				Log.i("tablefragment thread 2", "Creation agent success");
				
			}

		});
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
		this.postItDragEventToLocalListener.setAppModel(appModel);
	}

	/**
	 * @return the mainViewFragment
	 */
	public View getMainViewFragment() {
		return mainViewFragment;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof List<?>) {
			List<Object> tp = (List<Object>) data;
			if (tp.size() != 0 && tp.get(0) instanceof PostIt && ((PostIt) tp.get(0)).getOrigin() == 2) {
				Log.i("update TableFragemnt", "Mise à jour des postit");
				postItList.onRefreshComplete();
				List<PostIt> tp2 = (List<PostIt>) data;
				PostItAdapter adapter = new PostItAdapter(this.getActivity(), tp2); // crÈation
				postItList.setAdapter(adapter); // affichage
			}

		}
	}

	@Override
	public void postItReceived(List<PostIt> postits) {
		Log.i("postItReceived", postits.size() + " postit received");
		appModel.setPostItListTableCache(postits);
		
		Intent broadcast = new Intent();
		broadcast.setAction(STRING_ANSWER);
		broadcast.putParcelableArrayListExtra(POSTITLIST,(ArrayList<PostIt>) postits);
		this.getActivity().getApplicationContext().sendBroadcast(broadcast);
	
	}

	@Override
	public List<PostIt> getPostIts() {
		Log.i("postItSent", "TEST");

		// TODO Auto-generated method stub
		return appModel.getPostItListCache();
	}

	public void setAgent(PostItAgent agent) {
		Log.i("TableFragment", "setAgent");
		this.agent = agent;
	}

	private class CustomReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v("tablefragment custom recevier", "begin");
			String action = intent.getAction();
			if (action.equals(STRING_ANSWER)) {
				List<PostIt> postitListTemp = intent.getParcelableArrayListExtra(POSTITLIST);

				try {

					Log.i("update TableFragemnt", "Mise à jour des postit");
					inst.postItList.onRefreshComplete();
					PostItAdapter adapter = new PostItAdapter(inst.getActivity(), postitListTemp); // crÈation
					inst.postItList.setAdapter(adapter); // affichage

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}
	
	// Pour le DRAG DROP ///

	OnItemLongClickListener listSourceItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
			Log.i("TableFragment", "LONF CLICK MEMORAE");
			MainActivity ma = (MainActivity) inst.getActivity();
			ListPostItFragment mf = ma.getListPostItFragment();
			View vi = mf.getMainViewFragment();
			inst.setTargetLayout(vi);
			Bundle objetbunble = new Bundle();

			objetbunble.putParcelable("postit", appModel.getPostItListTableCache().get(position - 1));
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
		}
	};

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
	public View getTargetLayout() {
		return targetLayout;
	}

	/**
	 * @param targetLayout
	 *            the targetLayout to set
	 */
	public void setTargetLayout(View targetLayout) {
		this.targetLayout = targetLayout;
		targetLayout.setOnDragListener(postItDragEventToLocalListener);
		this.postItDragEventToLocalListener.setTargetView(targetLayout);
	}

	public PostItAgent getAgent() {
		return agent;
	}

	/**
	 * @param mainViewFragment the mainViewFragment to set
	 */
	public void setMainViewFragment(View mainViewFragment) {
		this.mainViewFragment = mainViewFragment;
	}


}