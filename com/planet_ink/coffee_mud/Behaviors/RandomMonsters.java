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
	protected Vector restrictedLocales=null;
	private boolean alreadyTriedLoad=false;

	public void setParms(String newParms)
	{
		int x=newParms.indexOf(";");
		String oldParms=newParms;
		restrictedLocales=null;
		if(x>=0)
		{
			oldParms=newParms.substring(0,x).trim();
			String extraParms=oldParms;
			int extraX=newParms.indexOf("<MOBS>");
			if(extraX<0)
			{
				String xtra=newParms.substring(x+1);
				extraX=xtra.indexOf(";");
				if(extraX>=0) extraParms=xtra.substring(extraX+1);
			}
			Vector V=Util.parse(extraParms);
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				if((s.startsWith("+")||(s.startsWith("-")))&&(s.length()>1))
				{
					if(restrictedLocales==null)
						restrictedLocales=new Vector();
					if(s.equalsIgnoreCase("+ALL"))
						restrictedLocales.clear();
					else
					if(s.equalsIgnoreCase("-ALL"))
					{
						restrictedLocales.clear();
						for(int i=0;i<Room.indoorDomainDescs.length;i++)
							restrictedLocales.addElement(new Integer(Room.INDOORS+i));
						for(int i=0;i<Room.outdoorDomainDescs.length;i++)
							restrictedLocales.addElement(new Integer(i));
					}
					else
					{
						char c=s.charAt(0);
						s=s.substring(1).toUpperCase().trim();
						int code=-1;
						for(int i=0;i<Room.indoorDomainDescs.length;i++)
							if(Room.indoorDomainDescs[i].startsWith(s))
								code=Room.INDOORS+i;
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
								restrictedLocales.removeElement(new Integer(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
								restrictedLocales.addElement(new Integer(code));
						}
						code=-1;
						for(int i=0;i<Room.outdoorDomainDescs.length;i++)
							if(Room.outdoorDomainDescs[i].startsWith(s))
								code=i;
						if(code>=0)
						{
							if((c=='+')&&(restrictedLocales.contains(new Integer(code))))
								restrictedLocales.removeElement(new Integer(code));
							else
							if((c=='-')&&(!restrictedLocales.contains(new Integer(code))))
								restrictedLocales.addElement(new Integer(code));
						}

					}
				}
			}
		}
		super.setParms(oldParms);
		minMonsters=Util.getParmInt(oldParms,"minmonsters",1);
		maxMonsters=Util.getParmInt(oldParms,"maxmonsters",1);
		parms=newParms;
		alreadyTriedLoad=false;
		if((restrictedLocales!=null)&&(restrictedLocales.size()==0))
			restrictedLocales=null;
	}

	public RandomMonsters()
	{
		tickReset();
	}


	public boolean okRoomForMe(MOB M, Room newRoom)
	{
		if(newRoom==null) return false;
		if(M==null) return false;
		if(restrictedLocales==null) return true;
		return !restrictedLocales.contains(new Integer(newRoom.domainType()));
	}

	public Vector getMonsters(Tickable thang, String theseparms)
	{
		Vector monsters=null;
		int x=theseparms.indexOf(";");
		String filename=(x>=0)?theseparms.substring(x+1):theseparms;
		if(filename.trim().length()==0)
		{
			Log.errOut("RandomMonsters","Blank XML/filename: '"+filename+"'.");
			return null;
		}
		int start=filename.indexOf("<MOBS>");
		if((start>=0)&&(start<=20))
		{
			int end=start+20;
			if(end>filename.length()) end=filename.length();
			monsters=(Vector)Resources.getResource("RANDOMMONSTERS-XML/"+filename.length()+"/"+filename.hashCode());
			if(monsters!=null) return monsters;
			monsters=new Vector();
			String error=CoffeeMaker.addMOBsFromXML(filename.toString(),monsters,null);
			if(error.length()>0)
			{
				Log.errOut("RandomMonsters","Error on import of xml for '"+((thang!=null)?thang.name():"null")+"': "+error+".");
				return null;
			}
			if(monsters.size()<=0)
			{
				Log.errOut("RandomMonsters","No mobs loaded for '"+((thang!=null)?thang.name():"null")+"'.");
				return null;
			}
			Resources.submitResource("RANDOMMONSTERS-XML/"+filename.length()+"/"+filename.hashCode(),monsters);
		}
		else
		if(!alreadyTriedLoad)
		{
			alreadyTriedLoad=true;
			int extraSemicolon=filename.indexOf(";");
			if(extraSemicolon>=0) filename=filename.substring(0,extraSemicolon);
			filename=filename.trim();
			monsters=(Vector)Resources.getResource("RANDOMMONSTERS-"+filename);
			if(monsters!=null) return monsters;
			StringBuffer buf=Resources.getFileResource(filename);
			if((buf==null)||((buf!=null)&&(buf.length()<20)))
			{
				Log.errOut("RandomMonsters","Unknown XML file: '"+filename+"' for '"+((thang!=null)?thang.name():"null")+"'.");
				return null;
			}
			if(buf.substring(0,20).indexOf("<MOBS>")<0)
			{
				Log.errOut("RandomMonsters","Invalid XML file: '"+filename+"' for '"+((thang!=null)?thang.name():"null")+"'.");
				return null;
			}
			monsters=new Vector();
			String error=CoffeeMaker.addMOBsFromXML(buf.toString(),monsters,null);
			if(error.length()>0)
			{
				Log.errOut("RandomMonsters","Error on import of: '"+filename+"' for '"+((thang!=null)?thang.name():"null")+"': "+error+".");
				return null;
			}
			if(monsters.size()<=0)
			{
				Log.errOut("RandomMonsters","No mobs loaded: '"+filename+"' for '"+((thang!=null)?thang.name():"null")+"'.");
				return null;
			}
			Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
		}
		return monsters;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
		||(CMSecurity.isDisabled("RANDOMMONSTERS")))
			return true;
		for(int i=maintained.size()-1;i>=0;i--)
		{
			try
			{
				MOB M=(MOB)maintained.elementAt(i);
				if((M.amDead())||(M.location()==null)||(!M.location().isInhabitant(M)))
					maintained.removeElement(M);
			} catch(Exception e){	}
		}
		if(maintained.size()>=maxMonsters)
			return true;
		if((canAct(ticking,tickID))||(maintained.size()<minMonsters))
		{
			Vector monsters=getMonsters(ticking,getParms());
			if(monsters==null) return true;

			int num=minMonsters;
			if(maintained.size()>=minMonsters)
				num=maintained.size()+1;
			if(num>maxMonsters) num=maxMonsters;
			while(maintained.size()<num)
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
						Resources.removeResource("HELP_"+((Room)ticking).getArea().name().toUpperCase());
					}
					else
					if((ticking instanceof Area)&&(((Area)ticking).mapSize()>0))
					{
						Resources.removeResource("HELP_"+ticking.name().toUpperCase());
						Room room=null;
						if(restrictedLocales==null)
						{
							int tries=0;
							while(((room==null)||(room.roomID().length()==0))&&((++tries)<100))
								room=((Area)ticking).getRandomRoom();
						}
						else
						{
							Vector map=new Vector();
							for(Enumeration e=((Area)ticking).getMap();e.hasMoreElements();)
							{
								Room R=(Room)e.nextElement();
								if((okRoomForMe(M,R))
								&&((Sense.isInFlight(M))
								  ||(((R.domainType()&Room.DOMAIN_INDOORS_AIR)==0)
								    &&((R.domainType()&Room.DOMAIN_OUTDOORS_AIR)==0)))
								&&(R.roomID().trim().length()>0))
									map.addElement(R);
							}
							if(map.size()>0)
								room=(Room)map.elementAt(Dice.roll(1,map.size(),-1));
						}
						if((room!=null)&&(room instanceof GridLocale))
						{
							Vector map=((GridLocale)room).getAllRooms();
							if(map.size()>0)
								room=(Room)map.elementAt(Dice.roll(1,map.size(),-1));
						}
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
