package com.utc.activities;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.utc.R;
import com.utc.dao.PostItDAO;
import com.utc.resource.PostIt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Utilisée pour créer ou modifier un postit
 *
 */
public class CreatePostItActivity extends Activity{
	
	@SuppressWarnings("unused")
	private static final SimpleDateFormat monthDayYearformatter = new SimpleDateFormat(
		      "MMMMM dd, yyyy");

	
	PostIt p = null ;
	CreatePostItActivity instance = null;
	EditText textPostIt = null;
	EditText textTag = null;
	
	Button buttonSavePostIt = null;
	TextView etatPostIt = null ; // Indique soit Nouveau Postit, soit Postit du JJMMAA
	
	// Listener (controller) du bouton de creation de nouveau post-it
	private OnClickListener clickListenerButtonSavePostIt = new OnClickListener() {
		
		public void onClick(View v) {
 
			String text = textPostIt.getText().toString();
			String tag = textTag.getText().toString();
			
			if(text.equals("")){
				Toast.makeText(instance, R.string.postitempty, Toast.LENGTH_LONG)
				.show();
			} 
			else{
				PostItDAO pDAO = new PostItDAO(instance);
				pDAO.open();
				
				// le postit est nouveau
				if(p==null){
					PostIt np = new PostIt(text);
					np.setTagListString(tag);
					np.setSynchro(false);
					np.setOrigin(0);
					pDAO.insertPostIt(np);
					
					Toast.makeText(instance, R.string.postitsaved, Toast.LENGTH_LONG)
					.show();
					pDAO.close();
				}
				// on edite un post it
				else{
					p.setText(text);
					p.setTagListString(tag);
					p.setSynchro(false);
					pDAO.updatePostIt(p.getGuid(), p);
					pDAO.close();
					instance.finishActivity(4); // On termine l'activity showActivity
				}
				
				instance.finish();
				
			}
			//On démarre l'autre Activity
//			startActivityForResult(intent, 2);
		}
	};
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createpostit);
		this.instance = this;
		
		// Recuperation du postit
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		p = bundle.getParcelable("postit");
		
		// Liaisons XML - variable et listener - bouton(variable)
		textPostIt = (EditText) findViewById(R.id.newPostItText);
		textTag = (EditText) findViewById(R.id.newTagText);
		buttonSavePostIt = (Button) findViewById(R.id.buttonSavePostIt);
		buttonSavePostIt.setOnClickListener(clickListenerButtonSavePostIt);
		etatPostIt = (TextView) findViewById(R.id.etatPostIt);
		
		// Si p est null alors c'est un nouveau postit : creation d'un postit
		if(p==null){
			etatPostIt.setText("Nouveau Post-it");
			
		}
		// Sinon on edit ce postit
		else{
			Date d = new Date(p.getTimeGconf());
			etatPostIt.setText("PostIt du "+d.toLocaleString()); //TODO saloperie dans la date
			textPostIt.setText(p.getText());
			textTag.setText(p.getTagList().toString().replaceAll("\\[","").replaceAll("\\]",""));
		}
		
		
		
	}

}
