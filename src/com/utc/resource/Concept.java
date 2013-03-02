package com.utc.resource;

import org.json.JSONException;
import org.json.JSONObject;

public class Concept {

	private int guid;
	
	private String name;
	
	private String description ;
	
	private String memoire ;
	
	private int typeId;
	
	public Concept(){
		
	}
	
	public Concept(final int guid, final String name){
		this.guid = guid;
		this.name = name;
	}
	public Concept(final JSONObject json) throws JSONException{
		super();
		initWithJSON(json);
	}
	
	public boolean initWithJSON(final JSONObject json) throws JSONException{
		try{
			guid = json.getInt( "id" );
			name = json.getString( "nom" );
			description = json.getString("description");
	//		this.setConceptId(json.getInt("conceptID"));
			this.typeId = json.getInt("type");
			return true;
		}
		catch (JSONException e){
			throw new JSONException("erreur json parse concept");
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
	 * @return the memoire
	 */
	public String getMemoire() {
		return memoire;
	}

	/**
	 * @param memoire the memoire to set
	 */
	public void setMemoire(String memoire) {
		this.memoire = memoire;
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
