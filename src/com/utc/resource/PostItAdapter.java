package com.utc.resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.utc.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PostItAdapter extends BaseAdapter implements ListAdapter{
	
	private static final int MAX_AFFICHAGE = 28;
	LayoutInflater inflater;
	List<PostIt> liste;
	List<View> listView = new ArrayList<View>(); 



	public PostItAdapter(Context context, List<PostIt> liste) 
	{

		inflater = LayoutInflater.from(context);
		this.liste=liste;


	}
	
	public int getCount() {

		return liste.size();

	}

	public Object getItem(int position) {

		return liste.get(position);
	}

	public long getItemId(int position) {

		return position;

	}

	private class ViewHolder 
	{

		TextView texte;
		TextView date;
		ImageView imgSync;

	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{

		ViewHolder holder;
		String texte;
		String date;
		SimpleDateFormat dateStandard = new SimpleDateFormat("dd/MM/yyyy");
		date = dateStandard.format(new Date(liste.get(position).getTimeGconf()));

		if(convertView == null) {

		holder = new ViewHolder();

		convertView = inflater.inflate(R.layout.item, null);

		holder.texte = (TextView)convertView.findViewById(R.id.texte);
		holder.date = (TextView)convertView.findViewById(R.id.date);
		holder.imgSync = (ImageView)convertView.findViewById(R.id.imageSync);

		convertView.setTag(holder);

		} else {

		holder = (ViewHolder) convertView.getTag();

		}
		
		
		if (liste.get(position).getText().length() > MAX_AFFICHAGE )
		{
			texte = liste.get(position).getText().substring(0,MAX_AFFICHAGE) + "...";
		}
		else if (liste.get(position).getText().indexOf("\n")>0)
		{
			texte = liste.get(position).getText().substring(0,liste.get(position).getText().indexOf("\n"))+ "...";
		}
		else
		{
			texte = liste.get(position).getText();
		}
		holder.texte.setText(texte);
		holder.date.setText(date);
		if(liste.get(position).getOrigin()==0){
			if(liste.get(position).isSynchro()){
				holder.imgSync.setImageResource(R.drawable.sync);
			}
			else{
				holder.imgSync.setImageResource(R.drawable.sync_n);
			}
		}
		listView.add(convertView);
		return convertView;

	}

	public View getView(final int position)
	{
		if(position>listView.size()-1)
			return null;
		else
			return listView.get(position);
	}
	
	public List<View> getListView() {
		return listView;
	}

	public void setListView(List<View> listView) {
		this.listView = listView;
	}

}

