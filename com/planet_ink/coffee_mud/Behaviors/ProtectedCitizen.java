package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ProtectedCitizen extends ActiveTicker
{
	public String ID(){return "ProtectedCitizen";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private static String defcityguard="cityguard";
	private static String[] defclaims={"Help! I'm being attacked!","Help me!!"};
	private String cityguard=null;
	private String[] claims=null;
	private int radius=7;

	public Behavior newInstance()
	{
		return new ProtectedCitizen();
	}

	public ProtectedCitizen()
	{
		minTicks=1; maxTicks=3; chance=99; radius=7;
		tickReset();
	}

	public void setParms(String parms)
	{
		super.setParms(parms);
		cityguard=null;
		radius=getParmVal(parms,"radius",radius);
		claims=null;
	}

	public String getCityguardName()
	{
		if(cityguard!=null) return cityguard;
		String s=getParmsNoTicks();
		if(s.length()==0)
		{ cityguard=defcityguard; return cityguard;}
		char c=';';
		int x=s.indexOf(c);
		if(x<0){ c='/'; x=s.indexOf(c);}
		if(x<0)
		{ cityguard=defcityguard; return cityguard;}
		cityguard=s.substring(0,x).trim();
		if(cityguard.length()==0)
		{ cityguard=defcityguard; return cityguard;}
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

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))
		&&(ticking instanceof MOB)
		&&(((MOB)ticking).isInCombat()))
		{
			MOB mob=(MOB)ticking;
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB M=mob.location().fetchInhabitant(i);
				if((M!=null)
				&&(M!=mob)
				&&(M.getVictim()==mob.getVictim()))
				   return true;
			}

			try{
				String claim=getClaims()[Dice.roll(1,getClaims().length,-1)];
			ExternalPlay.doCommand(mob,Util.parse("YELL \""+claim+"\""));
			}catch(Exception e){}


			Room thisRoom=mob.location();
			Vector V=new Vector();
			SaucerSupport.getRadiantRooms(thisRoom,V,true,radius);
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				MOB M=R.fetchInhabitant(getCityguardName());
				if((M!=null)
				&&(R.getArea().Name().equals(mob.location().getArea().Name()))
				&&(M!=mob.getVictim())
				&&(Sense.aliveAwakeMobile(M,true))
				&&(!M.isInCombat())
				&&(Sense.isMobile(M))
				&&(!BrotherHelper.isBrother(mob.getVictim(),M))
				&&(BrotherHelper.canFreelyBehaveNormal(M))
				&&(M.fetchAffect("Skill_Track")==null)
				&&(Sense.canHear(M)))
				{
					if(R==mob.location())
						ExternalPlay.postAttack(M,mob.getVictim(),M.fetchWieldedItem());
					else
					{
						int dir=SaucerSupport.radiatesFromDir(R,V);
						if(dir>=0)
							ExternalPlay.move(M,dir,false,false);
					}
				}
			}
		}
		return true;
	}
}