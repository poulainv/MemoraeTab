package com.utc.model;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.utc.dao.PostItDAO;
import com.utc.resource.Concept;
import com.utc.resource.Memoire;
import com.utc.resource.Organisation;
import com.utc.resource.PostIt;
import com.utc.resource.User;

/**
 * Modèle de notre application Note : un design pattern Observer / Observable
 * est mis en place. Cependant à cause d'une contrainte d'android il est
 * nécessaire d'appeler la fonction onResume et l'appModel si on est ammené à
 * changer d'activité
 */
public class ApplicationModel extends Observable {

	private ApplicationModel instance = this;

	private PostItDAO pdao = null;

	private List<PostIt> postItListCache;

	private List<PostIt> postItListServerCache;
	
	private List<PostIt> postItListTableCache;

	private List<Memoire> memoireList;

	private List<Concept> conceptList;

	private List<Organisation> orgaList;

	private PostIt postItSelected;

	private int conceptIdSelected = -1;

	private int memoireIdSelected = -1;

	private int organisationIdSelected = -1;

	private boolean activityChanged = false;

	private ServerAccess serverAccess = ServerAccess.getInstance();

	public static User user = new User();

	public ApplicationModel(Context context) {
		pdao = new PostItDAO(context);
		// this.context = context;
	}

	public List<PostIt> getListPostItLocalDb() {
		Log.i("AppModel", "GET POST IT LIST");
		pdao.open();
		postItListCache = pdao.getListPostIt();
		pdao.close();

		return postItListCache;
	}

	/**
	 * Indique au modèle qu'un postit a été selectionné Notifie ces observers en
	 * envoyant ce post-it
	 * 
	 * @param pos
	 *            position du postit dans la liste de postit du modèle
	 */
	public void postItSelected(final int pos) {
		Log.i("ApplicationModel", "postItSelecte :" + pos);
		if (postItListCache.size() != 0) {
			postItSelected = postItListCache.get(pos);
			setChanged();
			notifyObservers(postItSelected);
		}
	}

	/**
	 * Surement useless
	 * 
	 * @param id
	 * @param postit
	 * @return
	 */
	public int updatePostItLocal(int id, PostIt postit) {
		pdao.open();
		int res = pdao.updatePostIt(id, postit);
		pdao.close();
		return res;
	}
	
	
	/**
	 * Supprime un post it dont le guid est passé en param
	 * @param id
	 * @return le nombre de ligne supprimée en bdd
	 */
	public int removePostItLocal(final int id) {
		pdao.open();
		int res = pdao.removePostItWithID(id);
		pdao.close();
		return res;
	}

	/**
	 * Renvoie un postit selon son guid
	 * 
	 * @param id
	 * @return
	 */
	public PostIt getPostById(final int id) {
		for (int i = 0; i < postItListCache.size(); i++) {
			if (postItListCache.get(i).getGuid() == id)
				return postItListCache.get(i);
		}
		return null;
	}
	
	/**
	 * Renvoie un concept selon son guid
	 * 
	 * @param id
	 * @return
	 */
	public Concept getConceptById(final int id) {
		for (int i = 0; i < conceptList.size(); i++) {
			if (conceptList.get(i).getGuid() == id)
				return conceptList.get(i);
		}
		return null;
	}

	// TODO a améliorer
	/**
	 * Filtre le liste de post it dans le modèle avec le(s) tag(s) passés en
	 * parametre Tous les post it qui ne contiennent pas tous ces tag sont
	 * supprimés
	 * 
	 * @param tag
	 * @return vrai si on a enlevé un post it faux si rien a changé
	 */
	public boolean filterListByTag(final String tag) {
		this.getListPostItLocalDb();
		boolean flag = false;
		String[] tags = tag.split(" ");
		List<String> tagsList = new ArrayList<String>();

		for (int i = 0; i < tags.length; i++) {
			tagsList.add(tags[i]);
		}

		for (int i = 0; i < this.postItListCache.size(); i++) {
			// Log.i("Filter : ","++"+postItList.get(i).getTagList().get(1)+à"++");
			if (!postItListCache.get(i).getTagList().containsAll(tagsList)) {
				postItListCache.remove(i);
				i--;
				flag = true;
			}
		}
		if (flag) {
			setChanged();
			notifyObservers(postItListCache);
		}
		return flag;
	}

	/**
	 * Met à jout le guidserver d'un postit et indique que le post it est
	 * synchro Met aussi à jour la bdd
	 * 
	 * @param guidLocal
	 * @param guidServer
	 * @return vrai si le post a correctement été mis à jour, faux si on la pas
	 *         trouvé
	 */
	public boolean updatePostItLocalWithGuidServer(final int guidLocal, final int guidServer) {
		for (int i = 0; i < postItListCache.size(); i++) {
			if (postItListCache.get(i).getGuid() == guidLocal) {
				postItListCache.get(i).setGuidServer(guidServer);
				postItListCache.get(i).setSynchro(true);
				updatePostItLocal(postItListCache.get(i).getGuid(), postItListCache.get(i));
				return true;
			}
		}
		return false;
	}
	
	public void addPostItToListPostItTableCache(final PostIt p){
		postItListTableCache.add(p);
		setChanged();
		notifyObservers(postItListTableCache);
	}

	/**
	 * Met a jour la liste des post it du modèle et actualise le post focused
	 * avec un notify après un changement d'activity
	 */
	public void onResume() {
		if (activityChanged) {
			Log.i("AppModel", "onResume");
			this.getListPostItLocalDb();
			for (int i = 0; i < postItListCache.size(); i++) {
				if (postItSelected != null && postItListCache.get(i).getGuid() == postItSelected.getGuid()) {
					Log.i("onResume AppModel", "notifiy new post it : " + postItListCache.get(i).getText());
					setChanged();
					this.notifyObservers(postItListCache.get(i));
					setChanged();
					this.notifyObservers(postItListCache);
				}
			}
			activityChanged = false;
		}
	}

	public void transfertPostItServerToLocal(final PostIt pos){
			Log.i("transfertPostItServerToLocal","transfert du postit : pos :"+pos);
			PostIt np = new PostIt(pos.getText());
			np.setGuidServer(pos.getGuidServer());
			np.setTagList(pos.getTagList());
			np.setSynchro(true);
			pdao.open();
			np.setGuid((int)pdao.insertPostIt(np));
			pdao.close();
			postItListCache.add(np);
			setChanged();
			notifyObservers(postItListCache);
	}
	public void transfertPostItTableToLocal(final PostIt pos){
		Log.i("transfertPostItTableToLocal","transfert du postit : pos :"+pos);
		PostIt np = new PostIt(pos.getText());
//		np.setGuidServer(pos.getGuidServer());
//		np.setTagList(pos.getTagList());
//		np.setSynchro(true);
		pdao.open();
		np.setGuid((int)pdao.insertPostIt(np));
		pdao.close();
		postItListCache.add(np);
		setChanged();
		notifyObservers(postItListCache);
	}
	
	
	/**
	 * @return the activityChanged
	 */
	public boolean isActivityChanged() {
		return activityChanged;
	}

	/**
	 * Permet de signifier au modèle qu'on change d'activity
	 * 
	 * @param activityChanged
	 *            the activityChanged to set
	 */
	public void setActivityChanged(boolean activityChanged) {
		this.activityChanged = activityChanged;
	}

	//////////////////////////////////	
	///   Connection serveur /////////	
	//////////////////////////////////
	
	
	/**
	 * Vérifie si le user et password sont ok Instancie alors un User exemple :
	 * new aSyncTaskCheckUser().execute(new String[] {"vincent","test"});
	 */
	public class aSyncTaskCheckUser extends AsyncTask<String, Void, User> {
		protected User doInBackground(String... userAndPass) {
			String idString;
			try {
				idString = serverAccess.checkUserPassword(userAndPass[0], userAndPass[1]);
				idString = idString.replaceAll("\"", "").replaceAll("\n", "");
				JSONObject json = new JSONObject(idString);
				int id = json.getInt("resultat");
				if (id != -1) {
					Log.i("aSyncTaskCheckUser  ", "PassWord ok");
					return serverAccess.getUserById(id);
				} else {
					return user;
				}
			} catch (JSONException e) {
				Log.e("checkUser", "aucun id user n'est renvoyé");
				return null;
			} catch (ConnectException e1) {
				Log.e("checkUser", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(User result) {
			Log.i("fin thread", "result : " + result);
			if (result != null) {
				user = result;
				instance.setChanged();
				instance.notifyObservers(user);
			} else {
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
				Log.e("fin thread", "User == null, authentification abandonnée");
			}

		}
	}

	/**
	 * Demande au serveur de renvoyer toutes les memoires d'un utilsateur donné
	 * new aSyncTaskGetMemoireByUserId().execute(new Integer[] {1});
	 */
	public class aSyncTaskGetMemoireByUserIdForOrgaId extends AsyncTask<Integer, Void, List<Memoire>> {
		protected List<Memoire> doInBackground(Integer... id) {
			// if (memoireList == null) {
			// // TODO Prevenir que la connexion a echoue
			// return null;
			// }
			try {
				return serverAccess.getAllMemoireByIdUtilisateurForOrgaId(id[0],id[1]);
			} catch (ConnectException e) {
				Log.e("aSyncTaskGetMemoireByUserId", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(List<Memoire> result) {
			Log.i("fin thread", "result : " + result);
			if (result != null) {
				memoireList = result;
				Log.i("aSyncTaskGetMemoireByUserId", "fin thread ntifyobserver");
				instance.setChanged();
				instance.notifyObservers(memoireList);

			} else {
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
				Log.i("fin thread", "memoireList == null, get failed");
			}

		}

	}
	
	/**
	 * Demande au serveur de renvoyer toutes les memoires d'un utilsateur donné
	 * new aSyncTaskGetMemoireByUserId().execute(new Integer[] {1});
	 */
	public class aSyncTaskGetOrganisation extends AsyncTask<Void, Void, List<Organisation>> {
		protected List<Organisation> doInBackground(Void... id) {
			try {
				return serverAccess.getAllOrganisation();
			} catch (ConnectException e) {
				Log.e("aSyncTaskGetMemoireByUserId", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(List<Organisation> result) {
			Log.i("fin thread", "result : " + result);
			if (result != null) {
				orgaList = result;
				Log.i("aSyncTaskGetOrganisation", "fin thread ntifyobserver");
				instance.setChanged();
				instance.notifyObservers(orgaList);

			} else {
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
				Log.i("fin thread", "memoireList == null, get failed");
			}

		}

	}

	/**
	 * Demande au serveur de renvoyer toutes les memoires d'une memoire donné
	 * new aSyncTaskGetConceptByMemoireId().execute(new Integer[] {1});
	 */
	public class aSyncTaskGetConceptByMemoireId extends AsyncTask<Integer, Void, List<Concept>> {
		protected List<Concept> doInBackground(Integer... id) {
			try {
				return serverAccess.getAllConceptByOrgaID(id[0],id[1]);
			} catch (ConnectException e) {
				Log.e("aSyncTaskGetConceptByMemoireId", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(List<Concept> result) {
			Log.i("fin thread ", "result : " + result);
			if (result != null) {
				conceptList = result;
				Log.i("aSyncTaskGetConceptByMemoireId", "fin thread ntifyobserver");
				instance.setChanged();
				instance.notifyObservers(conceptList);
			} else {
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
				Log.i("fin thread", "conceptList == null, get failed");
			}
		}
	}
 
	/**
	 * Demande au serveur de renvoyer toutes les postit d'une memoire donné ???
	 * new aSyncTaskGetConceptByMemoireId().execute(new Integer[] {1});
	 */
	public class aSyncTaskGetPostit extends AsyncTask<Integer, Void, List<PostIt>> {
		protected List<PostIt> doInBackground(Integer... id) {
			try {
				return serverAccess.getPostit(id[0], id[1]);
			} catch (ConnectException e) {
				Log.e("aSyncTaskGetPostit", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(List<PostIt> result) {
			Log.i("fin thread ", "result : " + result);
			if (result != null) {
				postItListServerCache = result;
				Log.i("aSyncTaskGetPostit", "fin thread ntifyobserver");
				instance.setChanged();
				instance.notifyObservers(postItListServerCache);

			} else {
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
				Log.i("fin thread", "conceptList == null, get failed");
			}

		}

	}

	/**
	 * Demande au serveur d'ajouter un postit à la bddp new
	 * aSyncTaskAddPostit().execute(new PostIt[] {p});
	 */
	public class aSyncTaskAddPostit extends AsyncTask<PostIt, Void, PostIt> {
		protected PostIt doInBackground(PostIt... p) {
			try {
				Log.i("thread addPostit", "add :" + p[0].getText());
				int id = serverAccess.addPostIt(p[0], user, conceptIdSelected, memoireIdSelected);
				p[0].setGuidServer(id);
				return p[0];
			} catch (ConnectException e) {
				Log.e("aSyncTaskAddPostit", "pb de connection");
				return null;
			}
		}

		protected void onPostExecute(PostIt result) {
			Log.i("fin thread ", "result : " + result);
			String concept = getConceptById(conceptIdSelected).getName();
			getPostById(result.getGuid()).addTag(concept);
			updatePostItLocal(result.getGuid(), result);
			if (postItListServerCache != null) {
				Log.i("aSyncTaskaddPostit", "fin thread ntifyobserver");
				updatePostItLocalWithGuidServer(result.getGuid(), result.getGuidServer());
				result.setOrigin(1);
				postItListServerCache.add(result);
				instance.setChanged();
				instance.notifyObservers(postItListServerCache);
				instance.setChanged();
				instance.notifyObservers(postItListCache);
			}else{
				instance.setChanged();
				instance.notifyObservers(new ConnectException("erreur de connexion"));
			}

		}

	}

	/*************************/
	/*** getters aSyncTask ***/
	/*************************/

	public aSyncTaskCheckUser getAsyncTaskCheckUser() {
		return new aSyncTaskCheckUser();
	}

	public aSyncTaskGetMemoireByUserIdForOrgaId getAsyncTaskGetMemoireByUserIdForOrgaId() {
		return new aSyncTaskGetMemoireByUserIdForOrgaId();
	}

	public aSyncTaskGetConceptByMemoireId getAsyncTaskGetConceptByMemoireId() {
		return new aSyncTaskGetConceptByMemoireId();
	}

	public aSyncTaskGetPostit getAsyncTaskGetPostit() {
		return new aSyncTaskGetPostit();
	}

	public aSyncTaskAddPostit getAsyncTaskAddPostit() {
		return new aSyncTaskAddPostit();
	}
	
	public aSyncTaskGetOrganisation getAsyncTaskGetOrganisation() {
		return new aSyncTaskGetOrganisation();
	}

	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		ApplicationModel.user = user;
	}

	/**
	 * @return the memoireList
	 */
	public List<Memoire> getMemoireList() {
		return memoireList;
	}

	/**
	 * @param memoireList
	 *            the memoireList to set
	 */
	public void setMemoireList(List<Memoire> memoireList) {
		this.memoireList = memoireList;
	}

	/**
	 * @return the conceptList
	 */
	public List<Concept> getConceptList() {
		return conceptList;
	}

	/**
	 * @param conceptList
	 *            the conceptList to set
	 */
	public void setConceptList(List<Concept> conceptList) {
		this.conceptList = conceptList;
	}

	/**
	 * @return the conceptIdSelected
	 */
	public int getConceptIdSelected() {
		return conceptIdSelected;
	}

	/**
	 * @param conceptIdSelected
	 *            the conceptIdSelected to set
	 */
	public void setConceptIdSelected(int conceptIdSelected) {
		this.conceptIdSelected = conceptIdSelected;
	}

	/**
	 * @return the orgaList
	 */
	public List<Organisation> getOrgaList() {
		return orgaList;
	}

	/**
	 * @param orgaList the orgaList to set
	 */
	public void setOrgaList(List<Organisation> orgaList) {
		this.orgaList = orgaList;
	}

	/**
	 * @return the organisationIdSelected
	 */
	public int getOrganisationIdSelected() {
		return organisationIdSelected;
	}

	/**
	 * @param organisationIdSelected the organisationIdSelected to set
	 */
	public void setOrganisationIdSelected(int organisationIdSelected) {
		this.organisationIdSelected = organisationIdSelected;
	}

	/**
	 * @return the memoireIdSelected
	 */
	public int getMemoireIdSelected() {
		return memoireIdSelected;
	}

	/**
	 * @param memoireIdSelected
	 *            the memoireIdSelected to set
	 */
	public void setMemoireIdSelected(int memoireIdSelected) {
		this.memoireIdSelected = memoireIdSelected;
	}

	/**
	 * @return the postItListCache
	 */
	public List<PostIt> getPostItListCache() {
		return postItListCache;
	}

	/**
	 * @param postItListCache the postItListCache to set
	 */
	public void setPostItListCache(List<PostIt> postItListCache) {
		this.postItListCache = postItListCache;
	}

	/**
	 * @return the postItListServerCache
	 */
	public List<PostIt> getPostItListServerCache() {
		return postItListServerCache;
	}

	/**
	 * @param postItListServerCache the postItListServerCache to set
	 */
	public void setPostItListServerCache(List<PostIt> postItListServerCache) {
		this.postItListServerCache = postItListServerCache;
	}

	/**
	 * @return the postItListTableCache
	 */
	public List<PostIt> getPostItListTableCache() {
		return postItListTableCache;
	}

	/**
	 * @param postItListTableCache the postItListTableCache to set
	 */
	public void setPostItListTableCache(List<PostIt> postItListTableCache) {
		this.postItListTableCache = postItListTableCache;
	}

	

}
