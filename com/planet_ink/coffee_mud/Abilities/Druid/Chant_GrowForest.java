package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_GrowForest extends Chant
{
	public String ID() { return "Chant_GrowForest"; }
	public String name(){ return "Grow Forest";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int type=mob.location().domainType();
		if(((type&Room.INDOORS)>0)
			||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
		    ||(type==Room.DOMAIN_OUTDOORS_SPACEPORT)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic won't work here.");
			return false;
		}

		int material=-1;
		Vector choices=new Vector();
		String s=Util.combine(commands,0);

		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			int code=EnvResource.RESOURCE_DATA[i][0];
			if(((code&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			&&(code!=EnvResource.RESOURCE_WOOD))
			{
				choices.addElement(new Integer(code));
				if((s.length()>0)&&(EnglishParser.containsString(EnvResource.RESOURCE_DESCS[i],s)))
					material=code;
			}
		}
		if((material<0)&&(s.length()>0))
		{
			mob.tell("'"+s+"' is not a recognized form of tree!");
			return false;
		}

		if((material<0)&&(choices.size()>0))
			material=((Integer)choices.elementAt(Dice.roll(1,choices.size(),-1))).intValue();

		if(material<0) return false;

		String shortName=EnvResource.RESOURCE_DESCS[material&EnvResource.RESOURCE_MASK];

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"A grove of "+shortName.toLowerCase()+" trees sprout up.");
				mob.location().setResource(material);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}