package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Dyeing extends CommonSkill
{
	private Item found=null;
	private String writing="";
	public Dyeing()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dying";

		displayText="You are dyeing...";
		verb="dyeing";
		miscText="";
		triggerStrings.addElement("DYE");
		triggerStrings.addElement("DYEING");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		//CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Dyeing();
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(writing.length()==0)
				mob.tell("You mess up the dyeing.");
			else
			{
				String desc=found.description();
				int x=desc.indexOf(" Embroidered on it are the words `");
				int y=desc.lastIndexOf("`");
				if((x>=0)&&(y>x))
					desc=desc.substring(0,x);
				found.setDescription(desc+" Embroidered on it are the words `"+writing+"`.");
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify what you want to dye, and color to dye it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		else
			commands.remove(commands.firstElement());
		
		if(((target.material()&EnvResource.MATERIAL_CLOTH)==0)
		||(!target.isGeneric()))
		{
			mob.tell("You can't dye that material.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		writing=Util.combine(commands,0);
		verb="dyeing "+target.name();
		displayText="You are "+verb;
		found=target;
		if(!profficiencyCheck(0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) dyeing "+target.name());
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}