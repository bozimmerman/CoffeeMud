package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_IdentifyObject extends Spell
	implements DivinationDevotion
{
	public Spell_IdentifyObject()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Identify Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(8);

		addQualifyingClass(new Mage().ID(),8);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_IdentifyObject();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Identify what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell("You can't identify that.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> study(s) <T-NAME> very closely.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				String identity=((Item)target).secretIdentity();
				mob.tell(identity);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> study(s) <T-NAME>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
