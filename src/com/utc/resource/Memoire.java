package com.utc.resource;

import org.json.JSONException;
import org.json.JSONObject;

public class Memoire {

	private int guid;
	
	private String name;
	
	private int conceptId;
	
	private int typeId ;
	
	public Memoire(){
		
	}
	
	public Memoire(final int guid, final String name){
		this.guid = guid;
		this.name = name;
	}
	
	public Memoire(final JSONObject json) throws JSONException{
		super();
		initWithJSON(json);
	}

	public boolean initWithJSON(final JSONObject json) throws JSONException{
		try{
			guid = json.getInt( "id" );
			name = json.getString( "nom" );
//			this.setConceptId(json.getInt("conceptID"));
//			this.setTypeId(json.getInt("type"));
			return true;
		}
		catch (JSONException e){
			throw new JSONException("erreur json parse memoire");
		}
	}

	
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name){
		this.name = name;
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
	 * @return the typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
	
	
}
