package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_HealingAura extends Prayer
{
	public String ID() { return "Prayer_HealingAura"; }
	public String name(){ return "Healing Aura";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Healing Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean  canBeUninvoked(){return false;}
	public boolean  isAutoInvoked(){return true;}
	public Environmental newInstance(){	return new Prayer_HealingAura();}
	private int fiveDown=5;
	private int tenDown=10;
	private int twentyDown=20;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return false;
		if(tickID!=Host.TICK_MOB) return true;
		MOB myChar=(MOB)affected;

		if((fiveDown>1)&&(tenDown>1)&&(twentyDown>1)) return true;

		Hashtable followers=myChar.getGroupMembers(new Hashtable());
		if(myChar.location()!=null)
			for(int i=0;i<myChar.location().numInhabitants();i++)
			{
				MOB M=myChar.location().fetchInhabitant(i);
				if((M!=null)
				&&((M.getVictim()==null)||(followers.get(M.getVictim())==null)))
					followers.put(M,M);
			}
		if((--fiveDown)<=0)
		{
			fiveDown=5;
			Ability A=CMClass.getAbility("Prayer_CureLight");
			if(A!=null)
			for(Enumeration e=followers.elements();e.hasMoreElements();)
				A.invoke(myChar,((MOB)e.nextElement()),true);
		}
		if((--tenDown)<=0)
		{
			tenDown=10;
			Ability A=CMClass.getAbility("Prayer_RemovePoison");
			if(A!=null)
			for(Enumeration e=followers.elements();e.hasMoreElements();)
				A.invoke(myChar,((MOB)e.nextElement()),true);
		}
		if((--twentyDown)<=0)
		{
			twentyDown=10;
			Ability A=CMClass.getAbility("Prayer_CureDisease");
			if(A!=null)
			for(Enumeration e=followers.elements();e.hasMoreElements();)
				A.invoke(myChar,((MOB)e.nextElement()),true);
		}
		return true;
	}
}
