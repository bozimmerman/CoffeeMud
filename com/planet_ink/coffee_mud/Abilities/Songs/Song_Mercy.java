package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Mercy extends Song
{
	private Room lastRoom=null;
	private int count=3;
	
	public Song_Mercy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mercy";
		displayText="(Song of Mercy)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		baseEnvStats().setLevel(5);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;
		if(mob.location()!=lastRoom)
		{
			count=3;
			lastRoom=mob.location();
		}
		else
			count--;
		return true;
	}
	public boolean okAffect(Affect affect)
	{
		MOB mob=(MOB)affected;
		if(((affect.targetCode()&Affect.MASK_MALICIOUS)>0)
		&&(mob.location()!=null)
		&&((affect.amITarget(mob)))
		&&((count>0)||(lastRoom==null)||(lastRoom!=mob.location())))
		{
			MOB target=(MOB)affect.target();
			if((!target.isInCombat())&&(affect.source().getVictim()!=target))
			{
				affect.source().tell("You feel like showing "+target.name()+" mercy right now.");
				if(target.getVictim()==affect.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
			
		}
		return super.okAffect(affect);
	}
	
	public Environmental newInstance()
	{
		return new Song_Mercy();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		count=3;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		count=3;
		return true;
	}
}
