package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_Disintegrate extends Spell
	implements EvocationDevotion
{
	public Spell_Disintegrate()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disintegrate";

		malicious=true;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(25);

		addQualifyingClass(new Mage().ID(),25);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Disintegrate();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Environmental target=null;
		if(commands.size()<1)
		{
			if(mob.isInCombat())
				target=mob.getVictim();
			else
			{
				mob.tell("Disintegrate what?");
				return false;
			}
		}
		if(target==null)
			target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if((!(target instanceof Item))&&(!(target instanceof MOB)))
		{
			mob.tell("You can't disintegrate that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;


		boolean success=false;
		int targetType=Affect.SOUND_MAGIC;
		if(target instanceof Item)
			success=profficiencyCheck(mob.envStats().level()-target.envStats().level());
		else
		{
			targetType=Affect.STRIKE_MAGIC;
			success=profficiencyCheck(-(target.envStats().level()*5));
		}

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,targetType,Affect.SOUND_MAGIC,"<S-NAME> point(s) at <T-NAME> and utter(s) a treacherous spell!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					Hashtable V=new Hashtable();
					for(int i=0;i<mob.location().numItems();i++)
					{
						Item item=mob.location().fetchItem(i);
						if(item instanceof DeadBody)
							V.put(item,item);
					}

					if(target instanceof MOB)
						TheFight.doDamage((MOB)target,((MOB)target).curState().getHitPoints()+10);

					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> disintegrate(s)!");
					if(target instanceof Item)
					{
						((Item)target).destroyThis();
					}
					int i=0;
					while(i<mob.location().numItems())
					{
						int s=mob.location().numItems();
						Item item=mob.location().fetchItem(i);
						if((item instanceof DeadBody)&&(V.get(item)==null))
							item.destroyThis();
						if(s==mob.location().numItems())
							s++;
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAME> and utter(s) a treacherous but fizzled spell!");


		// return whether it worked
		return success;
	}
}