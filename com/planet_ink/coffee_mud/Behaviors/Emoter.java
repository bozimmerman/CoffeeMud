package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Emoter extends ActiveTicker
{
	protected Vector emotes=new Vector();

	public Emoter()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10;maxTicks=30;chance=50;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new Emoter();
	}

	public void setParms(String newParms)
	{
		String myParms=newParms;
		emotes=new Vector();
		int x=myParms.indexOf("/");
		if(x>0)
		{
			String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
		while(myParms.length()>0)
		{
			String thisEmote=myParms;
			x=myParms.indexOf("/");
			if(x>0)
			{
				thisEmote=myParms.substring(0,x);
				myParms=myParms.substring(x+1);
			}
			else
				myParms="";
			emotes.addElement(thisEmote);
		}
		parms=newParms;
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			MOB mob=this.getBehaversMOB(ticking);
			Room room=this.getBehaversRoom(ticking);
			if(room==null) return;

			boolean nonMOBfound=false;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)&&(!inhab.isMonster()))
				{
					nonMOBfound=true;
					break;
				}
			}
			if(!nonMOBfound) return;

			String emote=(String)emotes.elementAt(Dice.roll(1,emotes.size(),0)-1);
			if((mob!=null)&&(ticking instanceof MOB))
			{
				Vector V=new Vector();
				V.addElement("emote");
				V.addElement(emote);
				try{
				ExternalPlay.doCommand(mob,V);}
				catch(Exception e){ Log.errOut("Emoter",e);}
			}
			else
			if(ticking instanceof Room)
			{
				MOB dummyMOB=CMClass.getMOB("StdMOB");
				room.show(dummyMOB,null,Affect.MSG_OK_ACTION,emote);
			}
			else
			if(ticking instanceof Item)
			{
				Item item=(Item)ticking;
				if(mob!=null)
					room.show(mob,null,Affect.MSG_OK_ACTION,item.name()+" belonging to <S-NAME> "+emote);
				else
				{
					MOB dummyMOB=CMClass.getMOB("StdMOB");
					room.show(dummyMOB,null,Affect.MSG_OK_ACTION,item.name()+" "+emote);
				}
			}
		}
	}
}
