package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_Erase extends Spell
	implements AlterationDevotion
{
	public Spell_Erase()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Erase Scroll";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Mage().ID(),1);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Erase();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Erase what?.");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		
		
		Item item=null;
		if(target instanceof Item)
			item=(Item)target;
		
		if((item==null)||((item!=null)&&(!item.isReadable())))
		{
			mob.tell("You can't erase that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> rub(s) on <T-NAME>, making the words fade.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				item.setReadableText("");
			}
		}
		else
			beneficialFizzle(mob,target,"<S-NAME> rub(s) on <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
