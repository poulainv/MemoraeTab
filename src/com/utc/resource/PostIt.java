package com.utc.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class PostIt extends Observable implements Parcelable {

	private int guid;

	private int guidServer;

	private String text;

	private long timeGconf;

	private List<String> tagList = new ArrayList<String>();

	private boolean synchro;

	private boolean modified;

	/**
	 * 0 local, 1 server, 2 table
	 */
	private int origin = 0;

	public PostIt() {
		timeGconf = System.currentTimeMillis();
	}

	public PostIt(final String text) {

		this.text = text;
		timeGconf = System.currentTimeMillis();

	}

	public PostIt(final Parcel p) {
		this.text = p.readString();
		this.timeGconf = p.readLong();
		this.guid = p.readInt();
		p.readList(tagList, PostIt.class.getClassLoader());
		this.origin = (int) p.readDouble();
	}

	public PostIt(final JSONObject json) throws JSONException {
		super();
		guid = -1;
		initWithJSON(json);
	}

	public boolean initWithJSON(final JSONObject json) throws JSONException {
		try {
			if (!json.isNull("id")) {// provient table
				guidServer = json.getInt("id");
			} else if (!json.isNull("guidServer")) { // provient du serv
				guidServer = json.getInt("guidServer");
				timeGconf = System.currentTimeMillis(); // Tant que il y a pas de date de donnée
				origin = 2;
			}
			if (!json.isNull("description")) {
				text = json.getString("description");
			} else if (!json.isNull("text")) { // Si provient de la table
				text = json.getString("text");
			}

			// this.setConceptId(json.getInt("conceptID"));
			// this.setTypeId(json.getInt("type"));
			return true;
		} catch (JSONException e) {
			throw new JSONException("erreur json parse postit");
		}
	}

	/**
	 * Ajoute un tag au a la liste des tag du post it
	 * 
	 * @param tag
	 *            que l'on veut ajouter
	 * @return vrai si le tag est correctement ajouté, faux si le tag existe
	 *         deja
	 */
	public boolean addTag(final String tag) {
		if (!this.tagList.contains(tag)) {
			this.tagList.add(tag);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Supprime un tag de la liste des tag du post it
	 * 
	 * @param tag
	 *            que l'on veut supprimer
	 * @return vrai si le tag est correctement supprimé, faux si le tag n'existe
	 *         pas
	 */
	public boolean removeTag(final String tag) {
		return this.tagList.remove(tag);
	}

	public void setTagListString(final String tags) {
		tagList.clear();
		String[] temp = tags.split(",");
		for (int i = 0; i < temp.length; i++) {
			tagList.add(temp[i].replaceAll(" ", ""));
		}
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("id", -1);
			json.put("guidServer", guidServer);
			json.put("description", text);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;

	}

	/***********************************/
	/** Getters & setters **/
	/***********************************/

	public int getGuid() {
		return guid;
	}

	public void setGuid(int guid) {
		this.guid = guid;
		this.setChanged();
		this.notifyObservers();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.setChanged();
		this.notifyObservers();
	}

	public long getTimeGconf() {
		return timeGconf;
	}

	public void setTimeGconf(long timeGconf) {
		this.timeGconf = timeGconf;
		this.setChanged();
		this.notifyObservers(this);
	}

	public List<String> getTagList() {
		return tagList;
	}

	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}

	public boolean isSynchro() {
		return synchro;
	}

	public void setSynchro(boolean synchro) {
		this.synchro = synchro;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * @return the guidServer
	 */
	public int getGuidServer() {
		return guidServer;
	}

	/**
	 * @param guidServer
	 *            the guidServer to set
	 */
	public void setGuidServer(int guidServer) {
		this.guidServer = guidServer;
	}

	/**
	 * @return the origin
	 */
	public int getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(int origin) {
		this.origin = origin;
	}

	/***********************************/
	/** Methodes pour la parcelisation **/
	/***********************************/

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public PostIt createFromParcel(Parcel in) {
			return new PostIt(in);
		}

		public PostIt[] newArray(int size) {
			return new PostIt[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(text);
		out.writeLong(timeGconf);
		out.writeInt(guid);
		out.writeList(tagList);
		out.writeDouble(origin);
	}

	/**
	 * It will be required during un-marshaling data stored in a Parcel
	 */
	public class MyCreator implements Parcelable.Creator<PostIt> {
		public PostIt createFromParcel(Parcel source) {
			return new PostIt(source);
		}

		public PostIt[] newArray(int size) {
			return new PostIt[size];
		}
	}

}
