package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RandomMonsters extends ActiveTicker
{
	public String ID(){return "RandomMonsters";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	protected Vector maintained=new Vector();
	protected int minMonsters=1;
	protected int maxMonsters=1;
	protected String filename="";
	
	public void setParms(String newParms)
	{
		int x=newParms.indexOf(";");
		String oldParms=newParms;
		if(x>=0)
		{
			filename=newParms.substring(x+1).trim();
			oldParms=newParms.substring(0,x).trim();
		}
		super.setParms(oldParms);
		minMonsters=getParmVal(oldParms,"minmonsters",1);
		maxMonsters=getParmVal(oldParms,"maxmonsters",1);
		parms=newParms;
	}
	
	public RandomMonsters()
	{
		tickReset();
	}
	public Behavior newInstance()
	{
		return new RandomMonsters();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		for(int i=maintained.size()-1;i>=0;i--)
		{
			MOB M=(MOB)maintained.elementAt(i);
			if((M.amDead())||(M.location()==null)||(!M.location().isInhabitant(M)))
				maintained.removeElement(M);
		}
		if(maintained.size()>=maxMonsters)
			return true;
		if((canAct(ticking,tickID))||(maintained.size()<minMonsters))
		{
			if(filename.trim().length()==0)
			{
				Log.errOut("RandomMonsters","Blank XML filename: '"+filename+"'.");
				return true;
			}
			Vector monsters=(Vector)Resources.getResource("RANDOMMONSTERS-"+filename);
			if(monsters==null)
			{
				StringBuffer buf=Resources.getFileResource(filename);
				if((buf==null)||((buf!=null)&&(buf.length()==0)))
				{
					Log.errOut("RandomMonsters","Unknown XML file: '"+filename+"' for '"+ticking.name()+"'.");
					return true;
				}
				if(buf.substring(0,20).indexOf("<MOBS>")<0)
				{
					Log.errOut("RandomMonsters","Invalid XML file: '"+filename+"' for '"+ticking.name()+"'.");
					return true;
				}
				monsters=new Vector();
				String error=com.planet_ink.coffee_mud.common.Generic.addMOBsFromXML(buf.toString(),monsters,null);
				if(error.length()>0)
				{
					Log.errOut("RandomMonsters","Error on import of: '"+filename+"' for '"+ticking.name()+"': "+error+".");
					return true;
				}
				if(monsters.size()<=0)
				{
					Log.errOut("RandomMonsters","No mobs loaded: '"+filename+"' for '"+ticking.name()+"'.");
					return true;
				}
				Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
			}
			int num=minMonsters;
			if(num>=minMonsters) num=maintained.size()+1;
			while(maintained.size()<minMonsters)
			{
				MOB M=(MOB)monsters.elementAt(Dice.roll(1,monsters.size(),-1));
				if(M!=null)
				{
					M=(MOB)M.copyOf();
					M.setStartRoom(null);
					M.baseEnvStats().setRejuv(0);
					M.recoverEnvStats();
					M.text();
					maintained.addElement(M);
					if(ticking instanceof Room)
					{
						if(ticking instanceof GridLocale)
						{
							Vector map=((GridLocale)ticking).getAllRooms();
							if(map.size()==0)	
								M.bringToLife(((Room)ticking),true);
							else
							{
								Room room=(Room)map.elementAt(Dice.roll(1,map.size(),-1));
								M.bringToLife(room,true);
							}
						}
						else
							M.bringToLife(((Room)ticking),true);
					}
					else
					if((ticking instanceof Area)&&(((Area)ticking).mapSize()>0))
					{
						Room room=((Area)ticking).getRandomRoom();
						if(room!=null)
							M.bringToLife(room,true);
						else
							maintained.removeElement(M);
					}
					else
						break;
				}
			}
		}
		return true;
	}
}
