package com.utc.model;

import com.utc.resource.Concept;
import com.utc.resource.Memoire;
import com.utc.resource.Organisation;
import com.utc.resource.PostIt;
import com.utc.resource.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Contient toutes les mŽthodes qui permettent l'Žchange d'informations avec le
 * serveur. Attention la plupart de ces mŽthodes utilisent des requetes http
 * elles sont donc a executer sous un thread pour faire cela correctement
 * (utiliser asynctask) ServerAccess est implŽmentŽ en Singleton
 */
public class ServerAccess {

	private static volatile ServerAccess instance = null;

	/**
	 * Adresse IP du serveur memorae
	 */
	static private String IPSERV = "192.168.1.16";
	/**
	 * chemin dacces au service sur le serveur
	 */
	static private String URLSERV = "http://"+IPSERV+"/tx/";
	final static private String URLCHECKUSER = "json_gateway.php/UtilisateurService/checkUserPassword";
	final static private String URLGETUSERBYID = "json_gateway.php/UtilisateurService/getUserById";
	final static private String URLGETALLCONCEPTBYORGAID = "json_gateway.php/ConceptService/getAllConceptByOrganisationID";
	final static private String URLGETRESSOURCEBYBYMEMOIREANDCONCEPT = "json_gateway.php/RessourcesService/getAllRessourceByMemoireForConceptID";
	final static private String URLADDRESSOURCE = "json_gateway.php/RessourcesService/addRessource";
	final static private String URLADDRESSOURCEIN = "json_gateway.php/RessourcesService/addRessourceIn";
	final static private String URLGETALLORGA = "json_gateway.php/OrganisationService/getAllOrganisation";
	final static private String URLGETMEMOIREFORUSERBYORGAID = "json_gateway.php/MemoireService/getMemoireForUserByOrgaID";

	private ServerAccess() {
		super();
	}

	/**
	 * MŽthode permettant de renvoyer une instance de la classe ServerAccess
	 * 
	 * @return Retourne l'instance du ServerAccess.
	 */
	public final static ServerAccess getInstance() {
		if (ServerAccess.instance == null) {
			synchronized (ServerAccess.class) {
				if (ServerAccess.instance == null) {
					ServerAccess.instance = new ServerAccess();
				}
			}
		}
		return ServerAccess.instance;
	}

	/**
	 * Renvoie une liste de Memoire null si erreur
	 * 
	 * @param id
	 *            de user l'utilisateur recupŽrŽ si le mdp etait ok
	 * @return User ou null si erreur
	 * @throws ConnectException
	 */
	public List<Memoire> getAllMemoireByIdUtilisateurForOrgaId(final int userID, final int orgaID) throws ConnectException {
		List<Memoire> memoireList = new ArrayList<Memoire>();
		try {
			JSONArray jsonArray = new JSONArray(sendRequest(URLSERV + URLGETMEMOIREFORUSERBYORGAID + "?userID="
					+ userID+"&orgaID="+orgaID));

			for (int i = 0; i < jsonArray.length(); i++) {
				Memoire memoire = new Memoire(jsonArray.getJSONObject(i));
				Log.i("Log User : ", memoire.getName());
				memoireList.add(memoire);
			}

			return memoireList;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("getUserById",
					"je n'ai pas p parser le json renvoyee par le server pour getAllMemoireByIdUtilisateur byid");
			return null;
		}
	}
	
	/**
	 * Renvoie une liste d'organisation null si erreur
	 * 
	 * @throws ConnectException
	 */
	public List<Organisation> getAllOrganisation() throws ConnectException {
		List<Organisation> orgaList = new ArrayList<Organisation>();
		try {
			JSONArray jsonArray = new JSONArray(sendRequest(URLSERV + URLGETALLORGA ));

			for (int i = 0; i < jsonArray.length(); i++) {
				Organisation orga = new Organisation(jsonArray.getJSONObject(i));
				Log.i("Log User : ", orga.getNom());
				orgaList.add(orga);
			}

			return orgaList;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("getUserById",
					"je n'ai pas p parser le json renvoyee par le server pour getAllOrganisation byid");
			return null;
		}
	}

	/**
	 * Renvoie une liste de Concept null si erreur
	 * 
	 * @param id
	 *            de la memoire choisie
	 * @return User ou null si erreur
	 * @throws ConnectException
	 */
	public List<Concept> getAllConceptByOrgaID(final int orgaID,final int userID) throws ConnectException {
		List<Concept> conceptList = new ArrayList<Concept>();
		try {
			JSONArray jsonArray = new JSONArray(sendRequest(URLSERV + URLGETALLCONCEPTBYORGAID + "?orgaID="
					+ orgaID+"&userID="+userID));

			for (int i = 0; i < jsonArray.length(); i++) {
				Concept concept = new Concept(jsonArray.getJSONObject(i));
				Log.v("getAllConceptByMemoireID : ", concept.getName());
				// Si le type ==0 , c'est pas vraiment un concept
				if (concept.getTypeId() != 0)
					conceptList.add(concept);
			}
			return conceptList;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("getAllConceptByMemoireID",
					"je n'ai pas p parser le json renvoyee par le server pour getAllMemoireByIdUtilisateur byid");
			return null;
		}
	}

	/**
	 * Renvoie une liste de postit null si erreur
	 * 
	 * @param
	 * @return
	 * @throws ConnectException
	 */
	public List<PostIt> getPostit(final int memoireID, final int conceptID) throws ConnectException {
		List<PostIt> postitList = new ArrayList<PostIt>();
		try {
			JSONArray jsonArray = new JSONArray(sendRequest(URLSERV + URLGETRESSOURCEBYBYMEMOIREANDCONCEPT + "?memID="
					+ memoireID + "&conceptID=" + conceptID));

			for (int i = 0; i < jsonArray.length(); i++) {
				PostIt postit = new PostIt(jsonArray.getJSONObject(i));
				postit.setOrigin(1);
				Log.v("getPostit : ", postit.getText());
				postitList.add(postit);
			}
			return postitList;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("getPostit",
					"je n'ai pas p parser le json renvoyee par le server pour getAllMemoireByIdUtilisateur byid");
			return null;
		}
	}

	/**
	 * Renvoie le num de l'utilisateur -1 si le mot de passe est faux
	 * 
	 * @param user
	 * @param password
	 * @return
	 * @throws ConnectException
	 */
	public String checkUserPassword(final String user, final String password) throws ConnectException {
		return sendRequest(URLSERV + URLCHECKUSER + "?user=" + user + "&password=" + password);

	}

	/**
	 * Renvoie un objet de type User null si erreur
	 * 
	 * @param id
	 *            de user l'utilisateur recupŽrŽ si le mdp etait ok
	 * @return User ou null si erreur
	 * @throws ConnectException
	 */
	public User getUserById(final int userID) throws ConnectException {
		try {
			JSONObject json = new JSONObject(sendRequest(URLSERV + URLGETUSERBYID + "?userID=" + userID));
			User user = new User(json);
			Log.i("Log User : ", user.toString());
			return user;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("getUserById", "je n'ai pas p parser le json renvoyee par le server pour getusier byid");
			return null;
		}

	}

	/**
	 * Renvoie un objet de type User null si erreur
	 * 
	 * @param id
	 *            de user l'utilisateur recupŽrŽ si le mdp etait ok
	 * @return User ou null si erreur
	 * @throws ConnectException
	 */
	public int addPostIt(final PostIt p, User user, int conceptId, int memoireId) throws ConnectException {

		try {
			JSONObject json = new JSONObject();
			JSONObject json2 = new JSONObject();
			json.put("type", 4);
			json.put("ajouteur", user.getGuid());
			json.put("nom", "Post It Tablette");
			json.put("description", p.getText());
			json.put("url", p.getTagList().toString().replace("[","").replace("]", ""));
			json.put("auteur", user.getGuid());
			json.put("id", -1);
			json.put("date", "");
			json.put("conceptID", conceptId);
			json.put("nomPrenom", user.getLogin());
			json2.put("ress", json);
			String jsonText = sendRequestPost(URLSERV + URLADDRESSOURCE, json2);
			final JSONObject jsonRes = new JSONObject(jsonText);
			int id = jsonRes.getInt("resultat");
			Log.i("addPostIt", "post it added : guid = " + id);
			if (sendRequest(URLSERV + URLADDRESSOURCEIN + "?memID=" + memoireId + "&cptID=" + conceptId + "&ressID="
					+ id) == null) {
				Log.e("addPostIt", " le lien entre postt concept et memoire a echouŽ");
				return -1;
			}
			return id;
		} catch (JSONException e) {
			Log.e("addPostIt", "resultat de la requete post post it, impossible a parser en json");
			e.printStackTrace();
		}

		return -1;
	}

	private String sendRequest(String param) throws ConnectException {
		BufferedReader in = null;
		try {
			Log.i("Thread NetWork", "Get" + param);
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();

			request.setURI(new URI(param));
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String page = sb.toString();
			System.out.println(page);
			return page;

			// TODO gerer les echecs de connexion pour que �a ne plante pas
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SEND EXCEPTION");
			throw new ConnectException("erreur de connexion au serveur");

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public String sendRequestPost(String url, JSONObject json) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("Content-type", "application/json");
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			httppost.setEntity(new StringEntity(json.toString()));
			HttpResponse response = client.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null
					&& (response.getStatusLine().getStatusCode() == 201 || response.getStatusLine().getStatusCode() == 200)) {
				String res = EntityUtils.toString(entity);
				System.out.println(res);
				return res;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return the iPSERV
	 */
	public static String getIPSERV() {
		return IPSERV;
	}

	/**
	 * @param iPSERV the iPSERV to set
	 */
	public static void setIPSERV(String iPSERV) {
		IPSERV = iPSERV;
		URLSERV = "http://"+IPSERV+"/tx/"; 
	}

}
