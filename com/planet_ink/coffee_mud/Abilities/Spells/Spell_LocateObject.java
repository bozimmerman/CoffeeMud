package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_LocateObject extends Spell
{
	public Spell_LocateObject()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Locate Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=0;
		
		baseEnvStats().setLevel(10);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_LocateObject();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Locate what?");
			return false;
		}
		
		int levelFind=0;
		if((mob.isASysOp(mob.location()))&&(Util.s_int((String)commands.lastElement())>0))
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
				Vector itemsFound=new Vector();
				for(int m=0;m<CMMap.numRooms();m++)
				{
					Room room=CMMap.getRoom(m);
					Environmental item=room.fetchItem(null,what);
					if(item!=null)
					{
						String str=item.name()+" is in a place called '"+room.displayText()+"'.";
						if(mob.isASysOp(null))
							mob.tell(str);
						else
							itemsFound.addElement(str);
					}
					for(int i=0;i<room.numInhabitants();i++)
					{
						MOB inhab=room.fetchInhabitant(i);
						if(inhab==null) break;
						
						item=inhab.fetchInventory(what);
						if((item==null)&&(inhab instanceof ShopKeeper))
							item=((ShopKeeper)inhab).getStock(what);
						if(item!=null)
						{
							if((levelFind==0)
							 ||(item.envStats().level()<=levelFind))
							{
								String str=item.name()+((levelFind==0)?"":("("+item.envStats().level()+")"))+" is being carried by "+inhab.name()+" in a place called '"+room.displayText()+"'.";
								if(mob.isASysOp(null))
									mob.tell(str);
								else
									itemsFound.addElement(str);
							}
						}
					}
				}
				if(!mob.isASysOp(null))
				{
					if(itemsFound.size()==0)
						mob.tell("There doesn't seem to be anything in the wide world called '"+what+"'.");
					else
						mob.tell((String)itemsFound.elementAt(Dice.roll(1,itemsFound.size(),-1)));
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> invoke(s) a divination, shouting '"+what+"', but there is no answer.");


		// return whether it worked
		return success;
	}
}
