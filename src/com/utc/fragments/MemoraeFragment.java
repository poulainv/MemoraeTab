package com.utc.fragments;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
import com.utc.R;
import com.utc.activities.MainActivity;
import com.utc.activities.MainActivity.fragments;
import com.utc.event.PostItDragToLocalEvent;
import com.utc.model.ApplicationModel;
import com.utc.resource.Concept;
import com.utc.resource.Memoire;
import com.utc.resource.Organisation;
import com.utc.resource.PostIt;
import com.utc.resource.PostItAdapter;

/**
 * Si on est authentifié, ce fragment permet de récupérer la liste des postits
 * après avoir choisi la memoire, le concept et l'organisation dans laquelle on
 * souahite travailler
 * 
 */
public class MemoraeFragment extends Fragment implements Observer {

	private static String TAG = "MemoraeFragment";
	
	static private volatile MemoraeFragment inst;

	private Spinner spinnerMemoire;

	private int memoireSelectedPos = 0;

	private Spinner spinnerConcept;

	private int conceptSelectedPos = 0;

	private Spinner spinnerOrga;

	private int orgaSelectedPos = 0;

	private View targetLayout;

	private PostItDragToLocalEvent postItDragEventToLocalListener = new PostItDragToLocalEvent();

	private ArrayAdapter<CharSequence> adapterMemoire;

	private ArrayAdapter<CharSequence> adapterConcept;

	private ArrayAdapter<CharSequence> adapterOrga;

	private PullToRefreshListView postItList = null;

	private View mainViewFragment;

	private ApplicationModel appModel;

	/**
	 * Create a new instance of DetailsFragment, initialized to show the text at
	 * 'index'.
	 */
	public static MemoraeFragment getInstance() {
		if (inst == null) {
			inst = new MemoraeFragment();
		}
		return inst;
	}

	private MemoraeFragment() {
		super();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "Creation");

		postItList.setOnItemLongClickListener(listSourceItemLongClickListener);
		postItList.setOnDragListener(postItDragEventToLocalListener);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainViewFragment = inflater.inflate(R.layout.fragment_memorae, container, false);
		appModel.addObserver(this);
		postItList = (PullToRefreshListView) mainViewFragment.findViewById(R.id.listpostitserver);
		postItList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// Do work to refresh the list here.
				appModel.getAsyncTaskGetPostit().execute(
						new Integer[] { appModel.getMemoireIdSelected(), appModel.getConceptIdSelected() });
			}
		});

		// Def des spinner et de leur adapter
		spinnerMemoire = (Spinner) mainViewFragment.findViewById(R.id.spinner);
		adapterMemoire = new ArrayAdapter<CharSequence>(this.getActivity(), android.R.layout.simple_spinner_item);
		adapterMemoire.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerMemoire.setAdapter(adapterMemoire);
		/**/
		spinnerConcept = (Spinner) mainViewFragment.findViewById(R.id.spinnerConcept);
		adapterConcept = new ArrayAdapter<CharSequence>(this.getActivity(), android.R.layout.simple_spinner_item);
		adapterConcept.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerConcept.setAdapter(adapterConcept);

		spinnerOrga = (Spinner) mainViewFragment.findViewById(R.id.spinnerOrga);
		adapterOrga = new ArrayAdapter<CharSequence>(this.getActivity(), android.R.layout.simple_spinner_item);
		adapterOrga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerOrga.setAdapter(adapterOrga);

		// Si l'utilsiateur est loggé
		if (ApplicationModel.user.getGuid() != -1) {
			adapterOrga.clear();
			adapterOrga.add("Choisir...");

			if (appModel.getOrgaList() == null)
				appModel.getAsyncTaskGetOrganisation().execute();
			else {
				update(null, appModel.getOrgaList());
				spinnerOrga.setSelection(orgaSelectedPos);
			}

			// Deifinition du listener lorsqu'on clique sur une memoire
			spinnerOrga.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					// on recupere tous les concepts de la memoire
					if (pos != 0 && (memoireSelectedPos == 0 || pos!=orgaSelectedPos)) {
						Log.i("selection orga", "get memoire de l'orga " + pos);
						orgaSelectedPos = pos;
						appModel.setOrganisationIdSelected(appModel.getOrgaList().get(pos - 1).getGuid());
						appModel.getAsyncTaskGetMemoireByUserIdForOrgaId()
								.execute(
										new Integer[] { ApplicationModel.user.getGuid(),
												appModel.getOrganisationIdSelected() });
						adapterConcept.clear();
						adapterMemoire.clear();
						conceptSelectedPos=0;
						memoireSelectedPos=0;
						adapterMemoire.add("Choisir...");
					}
					
					// Si une memoire est deja selectionnée...
					if (memoireSelectedPos != 0) {
						adapterMemoire.add("Choisir...");
						update(null, appModel.getMemoireList());
						spinnerMemoire.setSelection(memoireSelectedPos);
					}

				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			// Deifinition du listener lorsqu'on clique sur une memoire
			spinnerMemoire.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					// on recupere tous les concepts de la memoire
					if (pos != 0 && (conceptSelectedPos == 0 || pos!=memoireSelectedPos)) {
						Log.i("selection memoire", "get concept de la memoire " + pos);
						memoireSelectedPos = pos;
						appModel.setMemoireIdSelected(appModel.getMemoireList().get(pos - 1).getGuid());
						appModel.getAsyncTaskGetConceptByMemoireId().execute(
								new Integer[] { appModel.getOrganisationIdSelected(),appModel.getUser().getGuid() });
						adapterConcept.clear();
						conceptSelectedPos=0;
						adapterConcept.add("Choisir...");

						
					}
					if (conceptSelectedPos != 0) {
						adapterConcept.add("Choisir...");
						update(null, appModel.getConceptList());
						spinnerConcept.setSelection(conceptSelectedPos);

					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			// Lorsque qu'on clique sur un concept
			spinnerConcept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					// on recupere tous les postit
					if (pos != 0) {
						conceptSelectedPos = pos;
						Log.i("selection concept", "get ressource du concept " + pos);
						appModel.setConceptIdSelected(appModel.getConceptList().get(pos - 1).getGuid());
						appModel.getAsyncTaskGetPostit().execute(
								new Integer[] { appModel.getMemoireIdSelected(), appModel.getConceptIdSelected() });

					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});


		} else {
			adapterOrga.add("Aucunes données...");
			Toast.makeText(this.getActivity(), "Vous n'etes pas loggé, authentifiez-vous", Toast.LENGTH_LONG).show();
		}
		((MainActivity) getActivity()).setCURRENT_FRAGMENT(fragments.MEMORAESERVER);
		return mainViewFragment;
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
			if (tp.size() == 0)
				return;
			// Si l'objet est une liste de Memoire
			if (tp.get(0) instanceof Memoire) {
				Log.i("update MemoraeFragemnt", "Mise à jour des memoires");
				List<Memoire> tp2 = (List<Memoire>) data;
				for (int i = 0; i < tp2.size(); i++) {
					adapterMemoire.add(tp2.get(i).getName());
				}
			}
			// Si l'objet est une liste de concept
			else if (tp.get(0) instanceof Concept) {
				Log.i("update MemoraeFragemnt", "Mise à jour des concepts");
				List<Concept> tp2 = (List<Concept>) data;
				for (int i = 0; i < tp2.size(); i++) {
					adapterConcept.add(tp2.get(i).getName());
				}

			} else if (tp.get(0) instanceof Organisation) {
				Log.i("update MemoraeFragemnt", "Mise à jour des organisation");
				List<Organisation> tp2 = (List<Organisation>) data;
				for (int i = 0; i < tp2.size(); i++) {
					adapterOrga.add(tp2.get(i).getCode());
				}
			}
			// Pour vérifier que les postit qui nous arrivent viennent bien du
			// serveur
			// on vérifie que leur guid ( id de sqlite =-1 )
			else if (tp.get(0) instanceof PostIt && ((PostIt) tp.get(0)).getOrigin() == 1) {
				Log.i("update MemoraeFragemnt", "Mise à jour des postit");
				postItList.onRefreshComplete();
				List<PostIt> tp2 = (List<PostIt>) data;
				PostItAdapter adapter = new PostItAdapter(this.getActivity(), tp2); // crÈation
				postItList.setAdapter(adapter); // affichage
			}
		}

	}

	// Pour le DRAG DROP ///

	OnItemLongClickListener listSourceItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
			Log.i(TAG, "Long click");
			MainActivity ma = (MainActivity) inst.getActivity();
			ListPostItFragment mf = ma.getListPostItFragment();
			View vi = mf.getMainViewFragment();
			inst.setTargetLayout(vi);
			Bundle objetbunble = new Bundle();

			objetbunble.putParcelable("postit", appModel.getPostItListServerCache().get(position - 1));
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

}