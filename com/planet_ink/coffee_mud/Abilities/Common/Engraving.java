package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Engraving extends CommonSkill
{
	private Item found=null;
	private String foundShortName="";
	public Engraving()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Engraving";

		displayText="You are engraving...";
		verb="engraving";
		miscText="";
		triggerStrings.addElement("ENGRAVE");
		triggerStrings.addElement("ENGRAVING");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		//CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Engraving();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
		}
		return super.tick(tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify what you want to engrave onto, and what words to engrave on it.");
			return false;
		}
		verb="engraving";
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int duration=60-mob.envStats().level();
		if(duration<25) duration=25;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) engraving.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}