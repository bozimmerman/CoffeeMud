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

public class Spell_Farsight extends Spell
	implements DivinationDevotion
{
	public Spell_Farsight()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Farsight";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Mage().ID(),9);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Farsight();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Which direction should I 'look'?");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(0);

		if(!success)
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> get(s) a far off look, but the spell fizzles.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> get(s) a far off look in <S-HIS-HER> eyes.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				while(commands.size()>0)
				{
					String whatToOpen=(String)commands.elementAt(0);
					Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
					int dirCode=-1;
					if(cmd!=null)
					{
						int dir=cmd.intValue();
						if(
						  (dir==CommandSet.NORTH)
						||(dir==CommandSet.SOUTH)
						||(dir==CommandSet.EAST)
						||(dir==CommandSet.WEST)
						||(dir==CommandSet.UP)
						||(dir==CommandSet.DOWN))
							dirCode=Directions.getDirectionCode(whatToOpen);
					}

					if(dirCode<0)
					{
						mob.tell("\n\r'"+whatToOpen+"' is not a valid direction.");
						commands.removeAllElements();
						success=false;
					}
					else
					{
						Exit exit=thatRoom.getExit(dirCode);
						Room room=thatRoom.getRoom(dirCode);

						if((exit==null)||(room==null)||((exit!=null)&&(!Sense.canBeSeenBy(exit,mob)))||((exit!=null)&&(!exit.isOpen())))
						{
							mob.tell("\n\rSomething has obstructed your vision.");
							success=false;
							commands.removeAllElements();
						}
						else
						{
							commands.removeElementAt(0);
							thatRoom=room;
						}
					}
				}
				if(success)
				{
					mob.tell("\n\r");
					thatRoom.look(mob);
				}
			}
		}

		return success;
	}
}