package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Prayer_Resurrect extends Prayer
{
	public Prayer_Resurrect()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resurrect";
		displayText="(Resurrected)";

		baseEnvStats().setLevel(25);

		addQualifyingClass(new Cleric().ID(),baseEnvStats().level());
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_Resurrect();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Item body=this.getTarget(mob,mob.location(),commands);
		if(body==null) return false;
		if(!(body instanceof DeadBody))
		{
			mob.tell("You can't resurrect that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,body,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> resurrect(s) <T-NAME>!");
			if(mob.location().okAffect(msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				int x=0;
				if((body instanceof DeadBody)&&((x=body.name().toUpperCase().indexOf("BODY OF"))>=0))
				{
					String mobName=body.name().substring(x+7).trim();
					MOB rejuvedMOB=(MOB)MOBloader.MOBs.get(mobName);
					if(rejuvedMOB!=null)
					{
						rejuvedMOB.tell(rejuvedMOB,null,"You are being resurrected.");
						if(rejuvedMOB.location()!=mob.location())
						{
							rejuvedMOB.location().delInhabitant(rejuvedMOB);
							rejuvedMOB.location().showOthers(rejuvedMOB,null,Affect.VISUAL_WNOISE,"<S-NAME> disappears!");
							mob.location().addInhabitant(rejuvedMOB);
							rejuvedMOB.setLocation(mob.location());
						}
						int it=0;
						while(it<mob.location().numItems())
						{
							Item item=mob.location().fetchItem(it);
							if(item.location()==body)
							{
								FullMsg msg2=new FullMsg(mob,body,item,Affect.HANDS_GET,null,Affect.HANDS_GET,null,Affect.NO_EFFECT,null);
								rejuvedMOB.location().send(rejuvedMOB,msg2);
								it=0;
							}
							else
								it++;
						}
						body.destroyThis();
						rejuvedMOB.location().show(rejuvedMOB,null,Affect.VISUAL_WNOISE,"<S-NAME> gets up!");
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,body,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> attempt(s) to resurrect <T-NAME>, but nothing happens.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}


		// return whether it worked
		return success;
	}
}
