package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_LocateObject extends Spell
	implements DivinationDevotion
{
	public Spell_LocateObject()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Locate Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(10);

		addQualifyingClass("Mage",10);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_LocateObject();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Locate what?");
			return false;
		}
		
		int levelFind=0;
		if((mob.isASysOp())&&(Util.s_int((String)commands.lastElement())>0))
		{
			levelFind=Util.s_int((String)commands.lastElement());
			commands.remove(commands.lastElement());
		}
		String what=Util.combine(commands,0);

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"<S-NAME> invoke(s) a divination, shouting '"+what+"'.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				for(int m=0;m<CMMap.map.size();m++)
				{
					Room room=(Room)CMMap.map.elementAt(m);
					Environmental item=room.fetchItem(null,what);
					if(item!=null)
						mob.tell(item.name()+" is in a place called '"+room.displayText()+"'.");
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						item=inhab.fetchInventory(what);
						if((item==null)&&(inhab instanceof ShopKeeper))
							item=((ShopKeeper)inhab).getStock(what);
						if(item!=null)
						{
							if((levelFind==0)
							 ||(item.envStats().level()<=levelFind))
								mob.tell(item.name()+((levelFind==0)?"":("("+item.envStats().level()+")"))+" is being carried by "+inhab.name()+" in a place called '"+room.displayText()+"'.");
						}
					}
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> invoke(s) a divination, shouting '"+what+"', but there is no answer.");


		// return whether it worked
		return success;
	}
}
