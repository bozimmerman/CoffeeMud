package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ProtectedCitizen extends ActiveTicker
{
	public String ID(){return "ProtectedCitizen";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private static String zapper=null;
	private static String defcityguard="cityguard";
	private static String[] defclaims={"Help! I'm being attacked!","Help me!!"};
	private String cityguard=null;
	private String[] claims=null;
	private int radius=7;
	private int maxAssistance=1;
	private boolean wander=false;



	public ProtectedCitizen()
	{
		minTicks=1; maxTicks=3; chance=99; radius=7; maxAssistance=1;
		tickReset();
	}

	public void setParms(String parms)
	{
		super.setParms(parms);
		cityguard=null;
		zapper=null;
		wander=parms.toUpperCase().indexOf("WANDER")>=0;
		radius=Util.getParmInt(parms,"radius",radius);
		maxAssistance=Util.getParmInt(parms,"maxassists",maxAssistance);
		claims=null;
	}

	public String getCityguardName()
	{
		if(cityguard!=null) return "-NAME \"+"+cityguard+"\"";
		if(zapper!=null) return zapper;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		char c=';';
		int x=s.indexOf(c);
		if(x<0){ c='/'; x=s.indexOf(c);}
		if(x<0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		cityguard=s.substring(0,x).trim();
		if(cityguard.length()==0)
		{ cityguard=defcityguard; return "-NAME \"+"+cityguard+"\"";}
		if((cityguard.indexOf("+")>0)
		||(cityguard.indexOf("-")>0)
		||(cityguard.indexOf(">")>0)
		||(cityguard.indexOf("<")>0)
		||(cityguard.indexOf("=")>0))
		{
			zapper=cityguard;
			cityguard=null;
			return zapper;
		}
		return cityguard;
	}

	public String[] getClaims()
	{
		if(claims!=null) return claims;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ claims=defclaims; return claims;}

		char c=';';
		int x=s.indexOf(c);
		if(x<0){ c='/'; x=s.indexOf(c);}
		if(x<0)
		{ claims=defclaims; return claims;}
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
			return false;

		String claim=getClaims()[Dice.roll(1,getClaims().length,-1)].trim();
		if(claim.startsWith(","))
			mob.doCommand(Util.parse("EMOTE \""+claim.substring(1).trim()+"\""));
		else
			mob.doCommand(Util.parse("YELL \""+claim+"\""));

		Room thisRoom=mob.location();
		Vector V=new Vector();
		MUDTracker.getRadiantRooms(thisRoom,V,true,!wander,false,null,radius);
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			MOB M=null;
			if(R.getArea().Name().equals(mob.location().getArea().Name()))
				for(int i=0;i<R.numInhabitants();i++)
				{
					MOB M2=R.fetchInhabitant(i);
					if((M2!=null)
					&&(M2.mayIFight(mob.getVictim()))
					&&(M2!=mob.getVictim())
					&&(Sense.aliveAwakeMobile(M2,true)
					&&(!M2.isInCombat())
					&&(Sense.isMobile(M2))
					&&(MUDZapper.zapperCheck(getCityguardName(),M2))
					&&(!BrotherHelper.isBrother(mob.getVictim(),M2))
					&&(BrotherHelper.canFreelyBehaveNormal(M2))
					&&(M2.fetchEffect("Skill_Track")==null)
					&&(Sense.canHear(M2))))
					{
						M=M2; break;
					}
				}
			if(M!=null)
			{
				if(R==mob.location())
					MUDFight.postAttack(M,mob.getVictim(),M.fetchWieldedItem());
				else
				{
					int dir=MUDTracker.radiatesFromDir(R,V);
					if(dir>=0)
						MUDTracker.move(M,dir,false,false);
				}
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			if((ticking instanceof MOB)&&(((MOB)ticking).isInCombat()))
				assistMOB((MOB)ticking);
		}
		return true;
	}
}