package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Engraving extends CommonSkill
{
	public String ID() { return "Engraving"; }
	public String name(){ return "Engraving";}
	private static final String[] triggerStrings = {"ENGRAVE","ENGRAVING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	private static boolean mapped=false;
	public Engraving()
	{
		super();
		displayText="You are engraving...";
		verb="engraving";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,"You mess up your engraving.");
				else
				{
					String desc=found.description();
					int x=desc.indexOf(" Engraved on it are the words `");
					int y=desc.lastIndexOf("`");
					if((x>=0)&&(y>x))
						desc=desc.substring(0,x);
					found.setDescription(desc+" Engraved on it are the words `"+writing+"`.");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to engrave onto, and what words to engrave on it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		else
			commands.remove(commands.firstElement());

		if((((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ROCK)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PLASTIC)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_MITHRIL))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't engrave onto that material.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		writing=Util.combine(commands,0);
		verb="engraving on "+target.name();
		displayText="You are "+verb;
		found=target;
		if(!profficiencyCheck(mob,0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_HANDS,"<S-NAME> start(s) engraving on <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}