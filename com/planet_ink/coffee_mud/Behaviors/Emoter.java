package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Emoter extends ActiveTicker
{
	public String ID(){return "Emoter";}
	public Emoter()
	{
		minTicks=10;maxTicks=30;chance=50;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Emoter();
	}

	protected Vector emotes=new Vector();
	protected boolean broadcast=false;
	
	protected final static int EMOTE_VISUAL=0;
	protected final static int EMOTE_SOUND=1;
	protected final static int EMOTE_SMELL=2;
	protected int emoteType=0;
	

	public void setParms(String newParms)
	{
		parms=newParms;
		broadcast=false;
		emoteType=EMOTE_VISUAL;
		
		emotes=new Vector();
		char c=';';
		int x=newParms.indexOf(c);
		if(x<0){ c='/'; x=newParms.indexOf(c);}
		if(x>0)
		{
			String parmText=newParms.substring(0,x);
			newParms=newParms.substring(x+1);
			Vector V=Util.parse(parmText);
			for(int v=V.size()-1;v>=0;v--)
			{
				String str=((String)V.elementAt(v)).toUpperCase();
				if(str.equals("BROADCAST"))
				{
					V.removeElementAt(v);
					broadcast=true;
				}
				else
				if(str.equals("AROMA")||(str.equals("SMELL")))
				{
					V.removeElementAt(v);
					emoteType=EMOTE_SMELL;
				}
				else
				if(str.equals("SOUND")||(str.equals("NOISE")))
				{
					V.removeElementAt(v);
					emoteType=EMOTE_SOUND;
				}
			}
			super.setParms(parmText);
		}
		while(newParms.length()>0)
		{
			String thisEmote=newParms;
			x=newParms.indexOf(";");
			if(x>0)
			{
				thisEmote=newParms.substring(0,x);
				newParms=newParms.substring(x+1);
			}
			else
				newParms="";
			if(thisEmote.length()>0)
				emotes.addElement(thisEmote);
		}
	}

	private void emoteHere(Room room, MOB emoter, String emote)
	{
		if(room==null) return;
		Room oldLoc=emoter.location();
		if(emoter.location()!=room) emoter.setLocation(room);
		FullMsg msg=new FullMsg(emoter,null,Affect.MSG_EMOTE,emote);
		
		if(room.okAffect(msg))
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster()))
			switch(emoteType)
			{
			case EMOTE_VISUAL:
				if(Sense.canBeSeenBy(emoter,M))	M.affect(msg);
				break;
			case EMOTE_SOUND:
				if(Sense.canBeHeardBy(emoter,M)) M.affect(msg);
				break;
			case EMOTE_SMELL:
				if(Sense.canSmell(M)) M.affect(msg);
				break;
			}
		}
		if(oldLoc!=null) emoter.setLocation(oldLoc);
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(emotes.size()>0))
		{
			String emote=(String)emotes.elementAt(Dice.roll(1,emotes.size(),-1));
			MOB emoter=null;
			if(ticking instanceof Area)
			{
				emoter=CMClass.getMOB("StdMOB");
				Vector V=((Area)ticking).getMyMap();
				for(int r=0;r<V.size();r++)
				{
					Room room=(Room)V.elementAt(r);
					emoteHere(room,emoter,emote);
				}
				return;
			}
			
			Room room=getBehaversRoom(ticking);
			if(room==null) return;
			if(ticking instanceof MOB)
				emoter=(MOB)ticking;
			else
			if(ticking instanceof Room)
			{
				emoter=CMClass.getMOB("StdMOB");
				emoter.setName(ticking.name());
			}
			else
			{
				emoter=CMClass.getMOB("StdMOB");
				MOB mob=getBehaversMOB(ticking);
				if(mob!=null)
					emoter.setName(ticking.name()+" belonging to "+mob.name());
				else
					emoter.setName(ticking.name());
			}
			emoteHere(room,emoter,"^E<S-NAME> "+emote+"^?");
			
			if(broadcast)
			{
				if(ticking instanceof MOB)
					emoter=CMClass.getMOB("StdMOB");
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=room.getRoomInDir(d);
					Exit E=room.getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						emoter.setName("something "+Directions.getInDirectionName(Directions.getOpDirectionCode(d)));
						emoteHere(R,emoter,"^E<S-NAME> "+emote+"^?");
					}
				}
			}
		}
	}
}
