package com.utc.activities;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import com.utc.R;
import com.utc.fragments.AuthentificationFragment;
import com.utc.fragments.DetailPostItFragment;
import com.utc.fragments.ListPostItFragment;
import com.utc.fragments.MemoraeFragment;
import com.utc.fragments.TableFragment;
import com.utc.model.ApplicationModel;
import com.utc.model.ServerAccess;
import com.utc.resource.User;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

/**
 * Activité principale
 * 
 */
public class MainActivity extends FragmentActivity implements Observer, TabHost.OnTabChangeListener {

	private TabHost mTabHost;

	private MainActivity inst = this;

	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, MainActivity.TabInfo>();

	public enum fragments {
		MEMORAESERVER, DETAILPOSTIT, AUTHENTIFICATION, TABLEFRAGMENT
	}

	private fragments CURRENT_FRAGMENT;

	private Logger LOG = Logger.getAnonymousLogger();

	private static final int ACTIVITYCODE = 1;

	private ApplicationModel appModel = null;

	private ListPostItFragment listPostItFragment;

	private DetailPostItFragment detailPostItFragment;

	private MemoraeFragment memoraeFragment;

	private TableFragment tableFragment;

	private SearchView searchByTagView;

	private Menu topMenu;

	private FragmentTransaction fragmentTransaction;

	private final String tabDetails = "local";
	private final String tabServer = "server";
	private final String tabTable = "table";

	/**
	 * Listener (controller) du bouton de creation de nouveau post-it Charge une
	 * nouvelle activité permettant la création ou l'edition d'un nouveau
	 * post-it
	 */
	private OnClickListener clickListenerButtonNewPostIt = new OnClickListener() {

		public void onClick(View v) {
			Bundle objetbunble = new Bundle();
			Intent intent = new Intent(MainActivity.this, CreatePostItActivity.class);

			// On affecte à l'Intent le Bundle que l'on a créé
			intent.putExtras(objetbunble);

			startActivityForResult(intent, ACTIVITYCODE);
		}
	};

	/**
	 * Listener du champs de recherche du menu du haut
	 */
	private OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {

		@Override
		public boolean onQueryTextChange(String newText) {
			Log.i("onQueryTextSubmit", "Submit Search : " + newText);
			if (newText.equals("")) {
				Log.i("onQueryTextSubmit", "vide : " + newText);
				appModel.setActivityChanged(true);
				appModel.onResume();
				return true;
			} else
				return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			Log.i("onQueryTextSubmit", "Submit Search");
			appModel.filterListByTag(query);
			return true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment);

		// instanciation des fragments
		appModel = new ApplicationModel(this.getApplicationContext());
		listPostItFragment = (ListPostItFragment) this.getSupportFragmentManager().findFragmentById(R.id.listFragment);
		listPostItFragment.setAppModel(appModel);
		listPostItFragment.displayPostItList();

		detailPostItFragment = DetailPostItFragment.getInstance();
		getFragmentManager().beginTransaction().add(R.id.layoutRight, detailPostItFragment, "detailfragment").commit();
		detailPostItFragment.setAppModel(appModel);

		memoraeFragment = MemoraeFragment.getInstance();
		memoraeFragment.setAppModel(appModel);

		tableFragment = TableFragment.getInstance();
		tableFragment.setAppModel(appModel);

		
		listPostItFragment.postItDragEventListener.setTableFragment(tableFragment);

		
		// ajout des observers
		appModel.addObserver(this);
		appModel.addObserver(detailPostItFragment);
		appModel.addObserver(listPostItFragment);
		appModel.addObserver(tableFragment);

		
		//gestion des onglet
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabInfo tabInfo = null;
		this.addTab(this, this.mTabHost,
				this.mTabHost.newTabSpec(tabDetails).setIndicator(this.getText(R.string.details)),
				(tabInfo = new TabInfo(tabDetails, AuthentificationFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		this.addTab(this, this.mTabHost,
				this.mTabHost.newTabSpec(tabServer).setIndicator(this.getText(R.string.servermemorae)),
				(tabInfo = new TabInfo(tabServer, DetailPostItFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		this.addTab(this, this.mTabHost,
				this.mTabHost.newTabSpec(tabTable).setIndicator(this.getText(R.string.tabletactile)),
				(tabInfo = new TabInfo(tabTable, TableFragment.class, savedInstanceState)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		// Default to first tab
		// this.onTabChanged("Tab1");
		//
		mTabHost.setOnTabChangedListener(this);

	}

	private void addTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
		tabHost.addTab(tabSpec);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * Creation de la barre d'outil du haut de l'ecran
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.topmenu, menu);
		topMenu = menu;
		topMenu.findItem(R.id.itemMode).setTitle("Mode non connecté");
		searchByTagView = (SearchView) menu.findItem(R.id.itemSearchByTag).getActionView();
		searchByTagView.setOnQueryTextListener(onQueryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Definition des action des differents boutons de l'action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemNewPostIt:
			clickListenerButtonNewPostIt.onClick(this.findViewById(R.id.main));
			return true;
		case R.id.itemExit:
			this.finish();
			return true;
		case R.id.itemEditIpServ:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Changement IP serveur");
			alert.setMessage("Entrer la nouvelle adresse IP");
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String newIp = input.getText().toString();
					if (newIp.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}")) {
						ServerAccess.setIPSERV(newIp);
						Toast.makeText(inst, "Sauvergardé", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(inst, "IP incorrecte", Toast.LENGTH_LONG).show();
					}
				}
			});
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Toast.makeText(inst, "Annulé", Toast.LENGTH_LONG).show();
				}
			});
			alert.show();
			return true;
		case R.id.itemConnect:
			goAuthentification();
			// if (appModel.getUser().getGuid() == -1) {
			// AuthentificationFragment f = new AuthentificationFragment();
			// f.setAppModel(appModel);
			// FragmentTransaction ft = getFragmentManager().beginTransaction();
			// ft.replace(R.id.layoutRight, f, "authentificationfragment");
			// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
			// } else {
			// appModel.setUser(new User());
			// topMenu.findItem(R.id.itemConnect).setTitle("Connexion");
			// topMenu.findItem(R.id.itemMode).setTitle("Mode non connecté");
			// }
			return true;
		case R.id.itemServer:
			fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.layoutRight, memoraeFragment, "serverfragment");
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
			return true;
		case R.id.itemLocal:
			fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.layoutRight, detailPostItFragment, "detailfragment");
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
			return true;
		case R.id.itemMode:
			goAuthentification();
//			AuthentificationFragment f2 = new AuthentificationFragment();
//			f2.setAppModel(appModel);
//			fragmentTransaction = getFragmentManager().beginTransaction();
//			fragmentTransaction.replace(R.id.layoutRight, f2, "authentificationfragment");
//			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Si update vient de appModel et si le data est un user, affiche un message
	 * de bienvenu avec le prenom de l'user Si le data est null c'est que l'user
	 * est null donc authentification a echoué donc petit message demande de
	 * reessayer
	 */
	@Override
	public void update(Observable observable, Object data) {
		LOG.info("update");
		// Si le changement vient de appModel
		if (observable instanceof ApplicationModel) {
			if (data instanceof User) {
				User userTemp = (User) data;
				if (userTemp.getGuid() != -1) {
					topMenu.findItem(R.id.itemMode).setTitle("Mode connecté");
					topMenu.findItem(R.id.itemConnect).setTitle("Déconnexion");
					Toast.makeText(this, "Bonjour " + userTemp.getPrenom() + " !!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "Identificant ou mot de passe incorrect", Toast.LENGTH_LONG).show();
				}
			} else if (data instanceof ConnectException) {
				Toast.makeText(this, ((ConnectException) data).getMessage(), Toast.LENGTH_LONG).show();
			} else if (data == null) {
			}

		}

	}

	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		System.out.println("HEY : " + tabId);
		try {
			if (tabId.equals(tabServer)) {
				fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.layoutRight, memoraeFragment, "serverfragment").commit();
			} else if (tabId.equals(tabDetails)) {
				fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.layoutRight, detailPostItFragment, "detailfragment").commit();
			} else if (tabId.equals(tabTable)) {
				fragmentTransaction = getFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.layoutRight, tableFragment, "tablefragment").commit();
			}
		} catch (Exception e) {

		}
	}

	/**
	 * Lance les boites de diaglues successives pour l'authentification
	 */
	public void goAuthentification(){
		if (appModel.getUser().getGuid() == -1) {
			AlertDialog.Builder connectDialogLogin = new AlertDialog.Builder(this);
			connectDialogLogin.setTitle("Authentification");
			connectDialogLogin.setMessage("Login ?");
			final EditText login = new EditText(this);
			connectDialogLogin.setView(login);
			connectDialogLogin.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					final String loginString = login.getText().toString();

					AlertDialog.Builder connectDialogPwd = new AlertDialog.Builder(inst);
					connectDialogPwd.setTitle("Authentification");
					connectDialogPwd.setMessage("Password ? ");
					final EditText pwd = new EditText(inst);
					connectDialogPwd.setView(pwd);
					connectDialogPwd.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String pwdString = pwd.getText().toString();
							appModel.getAsyncTaskCheckUser().execute(new String[] { loginString, pwdString });
						}
					});
					connectDialogPwd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Toast.makeText(inst, "Annulé", Toast.LENGTH_LONG).show();
						}
					});
					connectDialogPwd.show();
					// if (appModel.getUser().getGuid(

				}
			});
			connectDialogLogin.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Toast.makeText(inst, "Annulé", Toast.LENGTH_LONG).show();
				}
			});
			connectDialogLogin.show();
		} else {
			appModel.setUser(new User());
			topMenu.findItem(R.id.itemConnect).setTitle("Connexion");
			topMenu.findItem(R.id.itemMode).setTitle("Mode non connecté");
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
	}

	/**
	 * @return the listPostItFragment
	 */
	public ListPostItFragment getListPostItFragment() {
		return listPostItFragment;
	}

	/**
	 * @return the detailPostItFragment
	 */
	public DetailPostItFragment getDetailPostItFragment() {
		return detailPostItFragment;
	}

	/**
	 * @return the memoraeFragment
	 */
	public MemoraeFragment getMemoraeFragment() {
		return memoraeFragment;
	}

	/**
	 * @return the tableFragment
	 */
	public TableFragment getTableFragment() {
		return tableFragment;
	}

	/**
	 * @param tableFragment
	 *            the tableFragment to set
	 */
	public void setTableFragment(TableFragment tableFragment) {
		this.tableFragment = tableFragment;
	}

	public fragments getCURRENT_FRAGMENT() {
		return CURRENT_FRAGMENT;
	}

	public void setCURRENT_FRAGMENT(fragments cURRENT_FRAGMENT) {
		CURRENT_FRAGMENT = cURRENT_FRAGMENT;
	}

	class TabFactory implements TabContentFactory {

		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}

	}

	private class TabInfo {
		private String tag;

		TabInfo(String tag, Class<?> clazz, Bundle args) {
			this.tag = tag;
		}

	}

}
