package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_DistantVision extends Spell
	implements DivinationDevotion
{
	public Spell_DistantVision()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Distant Vision";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);

		addQualifyingClass(new Mage().ID(),13);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_DistantVision();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Divine a vision of where?");
			return false;
		}
		String areaName=CommandProcessor.combine(commands,0).trim().toUpperCase();
		Room thisRoom=null;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room room=(Room)MUD.map.elementAt(m);
			if(Util.containsString(room.displayText().toUpperCase(),areaName))
			{
				thisRoom=room;
				break;
			}
		}

		if(thisRoom==null)
		{
			mob.tell("You can't seem to fixate on a place called '"+CommandProcessor.combine(commands,0)+"'.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> close(s) <S-HIS-HER> eyes and invoke(s) a vision.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.tell("\n\r\n\r");
				thisRoom.look(mob);
			}

		}
		else
			beneficialFizzle(mob,null,"<S-NAME> close(s) <S-HIS-HER> eyes, but then open(s) them in frustration.");


		// return whether it worked
		return success;
	}
}
