package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Prayer_BirdsEye extends Prayer
{
	public String ID() { return "Prayer_BirdsEye"; }
	public String name(){ return "Birds Eye";}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public int quality(){return Ability.INDIFFERENT;}
	public Environmental newInstance(){	return new Prayer_BirdsEye();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for a birds eye view.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("BardMap");
				if(I!=null)
				{
					Vector set=new Vector();
					MUDTracker.getRadiantRooms(mob.location(),set,false,false,true,null,2);
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<set.size();i++)
						str.append(CMMap.getExtendedRoomID((Room)set.elementAt(i))+";");
					I.setReadableText(str.toString());
					I.setName("");
					I.baseEnvStats().setDisposition(EnvStats.IS_GLOWING);
					msg=new FullMsg(mob,I,CMMsg.MSG_READSOMETHING,"");
					mob.addInventory(I);
					mob.location().send(mob,msg);
					I.destroy();
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a birds eye view, but fail(s).");

		return success;
	}
}
