package com.utc.resource;

import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

public class User extends Observable {
	
	private int guid;
	
	private String login ;
	
	private String password;

	private String nom ;
	
	private String prenom ;
	
	private String email ;
	
	private boolean logged ;
	
	
	public User(){
		this.guid=-1;
		this.login = "";
		this.password = "";
		this.logged  = false; 
	}
	
	public User(final String login, final String password){
		this.login = login;
		this.password = password;
	}
	
	public User(final JSONObject json) throws JSONException{
		super();
		initWithJSON(json);
	}
	
	public boolean initWithJSON(final JSONObject json) throws JSONException{
			try {
				this.setGuid(json.getInt("id"));
				this.setEmail(json.getString( "email" ));
			    this.setNom(json.getString( "nom" ));
			    this.setPrenom(json.getString( "prenom" ));
			    this.setLogin(json.getString( "nom" ));
			    return true;
			} catch (JSONException e) {
				e.printStackTrace();
				throw new JSONException("erreur JSON parse USER");
		}
			
	}
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
		setChanged();
		notifyObservers();
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
	 * @return the prenom
	 */
	public String getPrenom() {
		return prenom;
	}

	/**
	 * @param prenom the prenom to set
	 */
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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
	
	@Override
	public String toString(){
		return "Je suis "+prenom+" "+nom;
	}
	
	
}
