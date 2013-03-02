package com.utc.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.utc.fragments.TableFragment;
import com.utc.resource.PostIt;
/**
 * 
 *Post t de communication avec la table tactile
 */

@SuppressWarnings("serial")
public class PostItAgent extends Agent
{
	private static final String SD_TYPE = "PostItAgent";
	public  static final String SD_TABLE_NAME = "TablePostItAgent";
	public  static final String SD_TABLET_NAME = "TabletPostItAgent";
	
	private static final String CID_REQUEST_POSTITS = "REQUEST_POSTITS";
	private static final String CID_RECEIVE_POSTITS = "RECEIVE_POSTITS";
	
	public interface PostItReceiver
	{
		public void postItReceived (List<PostIt> postits);
	}
	
	public interface PostItSet
	{
		public List<PostIt> getPostIts ();
	}
	
	private String destType;
	private PostItReceiver postItReceiver;
	private PostItSet postItSet;
	private TableFragment tableFragment;

	protected void setup()
	{
		Object[] arguments = getArguments();
		assert arguments.length == 4;
		assert arguments[0] instanceof String;
		assert arguments[1] instanceof String;
		assert arguments[2] instanceof PostItReceiver;
		assert arguments[3] instanceof PostItSet;

		tableFragment = (TableFragment) arguments[2];
		tableFragment.setAgent(this);

		destType = (String) arguments[1];
		postItReceiver = (PostItReceiver) arguments[2];
		postItSet = (PostItSet) arguments[3];

		ServiceDescription sd = new ServiceDescription();
		sd.setType(SD_TYPE);
		sd.setName((String) arguments[0]);

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addServices(sd);
		
		try
		{
			DFService.register(this, dfd);
		}
		catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
		
		addBehaviour(new ReceiveBehaviour());
		this.requestPostIts();
	}
	
	public void sendPostIt (PostIt postits)
	{
		Log.i("Send postit : "," "+postits.getText());

		ACLMessage message = new ACLMessage(ACLMessage.INFORM);

//		ServiceDescription sd = new ServiceDescription();
//		sd.setType(SD_TYPE);
//		sd.setName(destType);
//
//		DFAgentDescription dfd = new DFAgentDescription();
//		dfd.addServices(sd);
//
//		DFAgentDescription[] result = null;
		try
		{
//			result = DFService.search(this, dfd);
//
//			if (result.length > 0)
//			{
//				int i = 0;
//				for (; i < result.length; ++i)
//					if (!result[0].getName().equals(getName()))
//						break;

//				if (i != result.length)
//				{
//			message.addReceiver(result[0].getName());
			message.addReceiver(new AID(destType, AID.ISLOCALNAME));
			JSONObject jobj = new JSONObject( );
			JSONArray array = new JSONArray();
			array.put(postits.toJSON() );
			
			jobj.put("Array", array);

			message.setContent(jobj.toString());
			message.setConversationId(CID_RECEIVE_POSTITS);
//				}
//			}
		}
//		catch (FIPAException e)
//		{
//			e.printStackTrace();
//		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		addBehaviour(new SendBehaviour(message));
	}
	
	public void requestPostIts ()
	{
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

//		ServiceDescription sd = new ServiceDescription();
//		sd.setType(SD_TYPE);
//		sd.setName(destType);
//
//		DFAgentDescription dfd = new DFAgentDescription();
//		dfd.addServices(sd);
//
//		DFAgentDescription[] result = null;
//		try
//		{
//			result = DFService.search(this, dfd);
//
//			if (result.length > 0)
//			{
//				int i = 0;
//				for (; i < result.length; ++i)
//					if (!result[0].getName().equals(getName()))
//						break;
//
//				if (i != result.length)
//				{
//		message.addReceiver(result[0].getName());
		message.addReceiver(new AID(destType, AID.ISLOCALNAME));
					message.setConversationId(CID_REQUEST_POSTITS);
//				}
//			}
//		}
//		catch (FIPAException e)
//		{
//			e.printStackTrace();
//		}
		addBehaviour(new SendBehaviour(message));
	}
	
	private void onRequestPostits(ACLMessage message)
	{
		ACLMessage reply = message.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		reply.setConversationId(CID_RECEIVE_POSTITS);
		JSONObject jobj = new JSONObject();
		try
		{
			jobj.put("Array", postItSet.getPostIts());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		reply.setContent(jobj.toString());
		addBehaviour(new SendBehaviour(reply));
	}
	
	private void onReceivePostit (ACLMessage message)
	{
		if (message.getConversationId().equals(CID_RECEIVE_POSTITS))
		{
			String content = message.getContent();
			try 
			{
				JSONObject jobj = new JSONObject(content);
				JSONArray array = jobj.getJSONArray("Array");
				ArrayList<PostIt> postits = new ArrayList<PostIt>();
				Log.i("PostItAgent","onReceivePostit, "+postits.size()+" postits recu");
				for (int i = 0; i < array.length(); ++i)
					postits.add(new PostIt(array.getJSONObject(i)));
					postItReceiver.postItReceived(postits);			
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private class ReceiveBehaviour extends CyclicBehaviour
	{
		@Override
		public void action()
		{
			ACLMessage message = myAgent.receive(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					                                                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));

			if (message != null)
			{
				switch (message.getPerformative())
				{
				case ACLMessage.REQUEST:
					if (message.getConversationId().equals(CID_REQUEST_POSTITS))
						onRequestPostits(message);
					break;
				case ACLMessage.INFORM:
					if (message.getConversationId().equals(CID_RECEIVE_POSTITS))
						onReceivePostit(message);
					break;
				}
			}
			else
				block();
		}
	}
	
	private class SendBehaviour extends OneShotBehaviour
	{
		private ACLMessage message;
		
		private SendBehaviour (ACLMessage message)
		{
			this.message = message;
		}
		
		@Override
		public void action ()
		{
			send(message);
		}
	}
}

