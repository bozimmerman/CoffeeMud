package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class ProtectedCitizens extends ActiveTicker
{
	public String ID(){return "ProtectedCitizens";}
	protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_AREAS|Behavior.CAN_ROOMS;}
	private static String citizenZapper="";
	private static String helperZapper="";
	private static String[] defclaims={"Help! I'm being attacked!","Help me!!"};
	private String[] claims=null;
	private int radius=7;
	private int maxAssistance=1;
	private Hashtable assisters=new Hashtable();



	public ProtectedCitizens()
	{
		minTicks=1;
		maxTicks=3;
		chance=99;
		radius=7;
		maxAssistance=1;
		tickReset();
	}

	public void setParms(String parms)
	{
		super.setParms(parms);
		citizenZapper=null;
		helperZapper=null;
		radius=Util.getParmInt(parms,"radius",radius);
		maxAssistance=Util.getParmInt(parms,"maxassists",maxAssistance);
		claims=null;
	}

	public String getProtectedZapper()
	{
		if(citizenZapper!=null) return citizenZapper;
		String s=getParmsNoTicks();
		if(s.length()==0){ citizenZapper=""; return "";}
		char c=';';
		int x=s.indexOf(c);
		if(x<0){ citizenZapper=""; return "";}
		citizenZapper=s.substring(0,x);
		return citizenZapper;
	}

	public String getCityguardZapper()
	{
		if(helperZapper!=null) return helperZapper;
		String s=getParmsNoTicks();
		if(s.length()==0){ helperZapper=""; return "";}
		char c=';';
		int x=s.indexOf(c);
		if(x<0){ helperZapper=""; return "";}
		s=s.substring(x+1).trim();
		x=s.indexOf(c);
		if(x<0){ helperZapper=""; return "";}
		helperZapper=s.substring(0,x);
		return helperZapper;
	}

	public String[] getClaims()
	{
		if(claims!=null) return claims;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ claims=defclaims; return claims;}

		char c=';';
		int x=s.indexOf(c);
		if(x<0)	{ claims=defclaims; return claims;}
		s=s.substring(x+1).trim();
		x=s.indexOf(c);
		if(x<0)	{ claims=defclaims; return claims;}
		s=s.substring(x+1).trim();
		if(s.length()==0)
		{ claims=defclaims; return claims;}
		Vector V=new Vector();
		x=s.indexOf(c);
		while(x>=0)
		{
			String str=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if(str.length()>0)V.addElement(str);
			x=s.indexOf(c);
		}
		if(s.length()>0)V.addElement(s);
		claims=new String[V.size()];
		for(int i=0;i<V.size();i++)
			claims[i]=(String)V.elementAt(i);
		return claims;
	}

	public boolean assistMOB(MOB mob)
	{
		if(mob==null)
			return false;

		if((!mob.isMonster())
		||(!mob.isInCombat())
		||(!Sense.aliveAwakeMobile(mob,true))
		||(mob.location()==null))
		{
			if(assisters.containsKey(mob))
				assisters.remove(mob);
			return false;
		}

		if(!MUDZapper.zapperCheck(getProtectedZapper(),mob))
			return false;

		int assistance=0;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB M=mob.location().fetchInhabitant(i);
			if((M!=null)
			&&(M!=mob)
			&&(M.getVictim()==mob.getVictim()))
			   assistance++;
		}
		if(assistance>=maxAssistance)
			return true;

		String claim=getClaims()[Dice.roll(1,getClaims().length,-1)].trim();
		if(claim.startsWith(","))
			mob.doCommand(Util.parse("EMOTE \""+claim.substring(1).trim()+"\""));
		else
			mob.doCommand(Util.parse("YELL \""+claim+"\""));

		Room thisRoom=mob.location();
		Vector rooms=new Vector();
		Vector assMOBS=(Vector)assisters.get(mob);
		if(assMOBS==null)
		{
			assMOBS=new Vector();
			assisters.put(mob,assMOBS);
		}
		MUDTracker.getRadiantRooms(thisRoom,rooms,true,true,false,null,radius);
		for(int a=0;a<assMOBS.size();a++)
		{
			MOB M=(MOB)assMOBS.elementAt(a);
			if((M!=null)
			&&(M.mayIFight(mob.getVictim()))
			&&(M!=mob.getVictim())
			&&(M.location()!=null)
			&&(Sense.aliveAwakeMobile(M,true)
			&&(!M.isInCombat())
			&&(!BrotherHelper.isBrother(mob.getVictim(),M))
			&&(BrotherHelper.canFreelyBehaveNormal(M))
			&&(M.fetchEffect("Skill_Track")==null)
			&&(Sense.canHear(M))))
			{
				if(M.location()==thisRoom)
					MUDFight.postAttack(M,mob.getVictim(),M.fetchWieldedItem());
				else
				{
					int dir=MUDTracker.radiatesFromDir(M.location(),rooms);
					if(dir>=0)
						MUDTracker.move(M,dir,false,false);
				}
				assistance++;
			}
		}

		if(assistance>=maxAssistance)
			return true;

		for(int r=0;r<rooms.size();r++)
		{
			Room R=(Room)rooms.elementAt(r);
			if(R.getArea().Name().equals(thisRoom.getArea().Name()))
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M=R.fetchInhabitant(i);
					if((M!=null)
					&&(M.mayIFight(mob.getVictim()))
					&&(M!=mob.getVictim())
					&&(Sense.aliveAwakeMobile(M,true)
					&&(!M.isInCombat())
					&&((Sense.isMobile(M))||(M.location()==thisRoom))
					&&(!assMOBS.contains(M))
					&&(BrotherHelper.canFreelyBehaveNormal(M))
					&&(!BrotherHelper.isBrother(mob.getVictim(),M))
					&&(MUDZapper.zapperCheck(getCityguardZapper(),M))
					&&(M.fetchEffect("Skill_Track")==null)
					&&(Sense.canHear(M))))
					{
						boolean notAllowed=false;
						for(Enumeration a=assisters.elements();a.hasMoreElements();)
						{
							Vector assers=(Vector)a.nextElement();
							if(assers.contains(M))
							{ notAllowed=true; break;}
						}
						if(!notAllowed)
						{
							assMOBS.addElement(M);
							if(M.location()==thisRoom)
								MUDFight.postAttack(M,mob.getVictim(),M.fetchWieldedItem());
							else
							{
								int dir=MUDTracker.radiatesFromDir(M.location(),rooms);
								if(dir>=0)
									MUDTracker.move(M,dir,false,false);
							}
							assistance++;
						}
					}
					if(assistance>=maxAssistance)
						return true;
				}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			if(ticking instanceof MOB)
				assistMOB((MOB)ticking);
			else
			if(ticking instanceof Room)
				for(int i=0;i<((Room)ticking).numInhabitants();i++)
					assistMOB(((Room)ticking).fetchInhabitant(i));
			else
			if(ticking instanceof Area)
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(int i=0;i<R.numInhabitants();i++)
						assistMOB(R.fetchInhabitant(i));
				}
		}
		return true;
	}
}
