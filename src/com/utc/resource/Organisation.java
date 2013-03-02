package com.utc.resource;

import org.json.JSONException;
import org.json.JSONObject;

public class Organisation {

	private int guid ;
	
	private String nom ;
	
	private String code ;
	
	private String description ;
	
	private int conceptId;
	
	private int memoireId;
	
	private int groupeId;

	
	
	public Organisation(final JSONObject json) throws JSONException{
		super();
		initWithJSON(json);
	}

	public boolean initWithJSON(final JSONObject json) throws JSONException{
		try{
			guid = json.getInt( "id" );
			nom = json.getString( "nom" );
			code = json.getString("code");
			description = json.getString("description");
//			this.setConceptId(json.getInt("conceptID"));
//			this.setTypeId(json.getInt("type"));
			return true;
		}
		catch (JSONException e){
			throw new JSONException("erreur json parse memoire");
		}
	}

	
	
	/**
	 * @return the guid
	 */
	public int getGuid() {
		return guid;
	}

	/**
	 * @param guid the guid to set
	 */
	public void setGuid(int guid) {
		this.guid = guid;
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the conceptId
	 */
	public int getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return the memoireId
	 */
	public int getMemoireId() {
		return memoireId;
	}

	/**
	 * @param memoireId the memoireId to set
	 */
	public void setMemoireId(int memoireId) {
		this.memoireId = memoireId;
	}

	/**
	 * @return the groupeId
	 */
	public int getGroupeId() {
		return groupeId;
	}

	/**
	 * @param groupeId the groupeId to set
	 */
	public void setGroupeId(int groupeId) {
		this.groupeId = groupeId;
	}
	
	
	
	
}
