package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Embroidering extends CommonSkill
{
	public String ID() { return "Embroidering"; }
	public String name(){ return "Embroidering";}
	private static final String[] triggerStrings = {"EMBROIDER","EMBROIDERING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	private static boolean mapped=false;
	public Embroidering()
	{
		super();
		displayText="You are embroidering...";
		verb="embroidering";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Embroidering();}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,"You mess up your embroidery.");
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
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to embroider onto, and what words to embroider on it.");
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

		if((((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't embroider onto that material.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		writing=Util.combine(commands,0);
		verb="embroidering on "+target.name();
		displayText="You are "+verb;
		found=target;
		if(!profficiencyCheck(0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) embroidering on "+target.name());
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}