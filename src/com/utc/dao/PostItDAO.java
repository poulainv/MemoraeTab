package com.utc.dao;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.utc.DBSQLite.DBSQLite;
import com.utc.resource.PostIt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PostItDAO {
	 
	 
		private static final int VERSION_BDD = 22;
		private static final String NOM_BDD = "memorae.local";
	 
		private static final String TABLE_POSTIT = "POSTIT";
		private static final String COL_ID = "ID";
		private static final int NUM_COL_ID = 0;
		private static final String COL_TEXT = "TEXT";
		private static final int NUM_COL_TEXT = 1;
		private static final String COL_TIME = "TIME";
		private static final int NUM_COL_TIME = 2;
		private static final String COL_TAGS = "TAGS";
		private static final int NUM_COL_TAGS = 3;
		private static final String COL_SYNCHRO = "SYNCHRO";
		private static final int NUM_COL_SYNCHRO = 4;
	 
		private SQLiteDatabase bdd;
	 
		private DBSQLite maBaseSQLite;
	 
		public PostItDAO(Context context){
			//On cree la BDD et sa table
			maBaseSQLite = DBSQLite.getInstance(context, NOM_BDD, null, VERSION_BDD);
		}
	 
		/**
		 * Methode pour ouvrir en ecriture la base de donnees
		 */
		public void open(){
			bdd = maBaseSQLite.getWritableDatabase();
		}
	 
		/**
		 * Methode pour ferme en lecture et en ecrite l'acces a la bdd
		 */
		public void close(){
			bdd.close();
		}
		/**
		 * Getter
		 * @return SQLiteBatabase
		 */
		public SQLiteDatabase getBDD(){
			return bdd;
		}
		
		/** 
		 * Insertion d'un post-it dans la base de donnees
		 * Attention la base de donn�e doit etre ouverte en ecriture avant ( open() )
		 * @param postit
		 * @return id ins�r�
		 */
		public long insertPostIt(PostIt postit){
			//Creation d'un ContentValues (fonctionne comme une HashMap)
			ContentValues values = new ContentValues();
			Log.i("insertPostIt TAG :",postit.getTagList().toString().replace("[","").replace("]", ""));
			//on lui ajoute une valeur associee a une cle (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
			values.put(COL_TEXT, postit.getText());
			values.put(COL_TIME, postit.getTimeGconf());
			values.put(COL_TAGS, postit.getTagList().toString().replace("[","").replace("]", ""));
			values.put(COL_SYNCHRO,""+postit.isSynchro());
			//on insere l'objet dans la BDD via le ContentValues
			return bdd.insert(TABLE_POSTIT, null, values);
			
		}
		/** 
		 * Modification d'un post-it dans la base de donnees. L'identifiant du post it a modifier doit 
		 * etre passe en parametre
		 * Attention la base de donn�e doit etre ouverte en ecriture avant ( open() )
		 * @param nouveau post it
		 * @param id identififiant (guid) du post it a remplacer
		 * @return ??
		 */
		public int updatePostIt(int id, PostIt postit){
			
			ContentValues values = new ContentValues();
			values.put(COL_TEXT, postit.getText());
			values.put(COL_TIME, postit.getTimeGconf());
			values.put(COL_TAGS, postit.getTagList().toString().replace("[","").replace("]", ""));
			values.put(COL_SYNCHRO,""+postit.isSynchro());	
			return bdd.update(TABLE_POSTIT, values, COL_ID + " = " +id, null);
	
		}
	 
		/**
		 * Supprime un post it de la bdd
		 * @param id identifiant (guid) du post it a supprimer
		 * @return ??
		 */
		public int removePostItWithID(int id){
			
			int result = bdd.delete(TABLE_POSTIT, COL_ID + " = " +id, null);
			
			//Suppression d'un postit de la BDD grace a l'ID
			return result;
			
	
		} 
	 /**
	  * Renvoit un post it dont l'identifiant est passe en parametre
	  * @param id identifiant (guid) du post it en renvoyer
	  * @return Post it
	  */
		public PostIt getPostItById(final int id){
			//Recupere dans un Cursor les valeur correspondant � un livre contenu dans la BDD (ici on s�lectionne le livre gr�ce � son titre)
			Cursor c = bdd.query(TABLE_POSTIT, new String[] {COL_ID, COL_TEXT, COL_TIME, COL_TAGS,COL_SYNCHRO}, COL_ID + " LIKE \"" + id +"\"", null, null, null, null);
			return cursorToPostIt(c);
		}
		
		/**
		 * Retourne la liste de post it contenu dans la base de donnee 
		 * @return la liste de post it contenu dans la base de donnee sous la fome d'une List<PostIt>
		 */
		public List<PostIt> getListPostIt(){
			//R�cup�re dans un Cursor les valeur correspondant � un livre contenu dans la BDD (ici on s�lectionne le livre gr�ce � son titre)
			List<PostIt> res;
			Cursor c = bdd.query(TABLE_POSTIT, new String[] {COL_ID, COL_TEXT, COL_TIME, COL_TAGS, COL_SYNCHRO}, COL_ID + " IS NOT NULL ", null, null, null, null);
			
			//On inverse l'ordre de la liste pour avoir le post-it le plus récent en haut
			res=cursorToListPostIt(c);
			Collections.reverse(res); 
			return res;
//			return cursorToListPostIt(c);
		}
	 
		//Cette methode permet de convertir un cursor en un postit
		private PostIt cursorToPostIt(Cursor c){
			//si aucun element n'a ete retourne dans la requete, on renvoie null
			if (c.getCount() == 0)
				return new PostIt();
	 
			//Sinon on se place sur le premier element
			c.moveToFirst();
			//On cree un postit
			PostIt postit = new PostIt();
			//on lui affecte toutes les infos grace aux infos contenues dans le Cursor
			postit.setGuid(c.getInt(NUM_COL_ID));
			postit.setText(c.getString(NUM_COL_TEXT));
			postit.setTimeGconf(c.getLong(NUM_COL_TIME));
			
		
			postit.setSynchro(Boolean.getBoolean(c.getString(NUM_COL_SYNCHRO)));
			postit.setTagList(this.convertStringIntoList(c.getString(NUM_COL_TAGS)));
			//On ferme le cursor
			c.close();
	  
			//On retourne le postit
			return postit;
		}
		
		//Cette methode permet de convertir un cursor en une liste de postit
		private List<PostIt> cursorToListPostIt(Cursor c){
			//si aucun element n'ax ete retourne dans la requete, on renvoie null
			if (c.getCount() == 0)
				return new ArrayList<PostIt>();
			List<PostIt> listPostIt = new ArrayList<PostIt>();
			
			
			//Sinon on se place sur le premier element
			c.moveToFirst();
			//On cree un postit
			PostIt postit = new PostIt();
			//on lui affecte toutes les infos grace aux infos contenues dans le Cursor
			postit.setGuid(c.getInt(NUM_COL_ID));
			postit.setText(c.getString(NUM_COL_TEXT));
			postit.setTimeGconf(c.getLong(NUM_COL_TIME));
			postit.setOrigin(0);
			postit.setTagList(this.convertStringIntoList(c.getString(NUM_COL_TAGS)));
			postit.setSynchro(Boolean.valueOf(c.getString(NUM_COL_SYNCHRO)));
			// Ajout la liste
			listPostIt.add(postit);
			
			while(c.moveToNext()){
				PostIt np = new PostIt();
				np.setGuid(c.getInt(NUM_COL_ID));
				np.setText(c.getString(NUM_COL_TEXT));
				np.setTimeGconf(c.getLong(NUM_COL_TIME));
				np.setOrigin(0);
				np.setTagList(this.convertStringIntoList(c.getString(NUM_COL_TAGS)));
				np.setSynchro(Boolean.valueOf(c.getString(NUM_COL_SYNCHRO)));
				listPostIt.add(np);
			}
			
			
			//On ferme le cursor
			c.close();
	 
			//On retourne le postit
			return listPostIt;
		}
		
		private List<String> convertStringIntoList(final String tags){
			List<String> tagList = new ArrayList<String>();
			String[] tagsTemp = tags.split(", ");
			for(int i=0;i<tagsTemp.length;i++){
				tagList.add(tagsTemp[i]);
			}
			return tagList;
		}
}
