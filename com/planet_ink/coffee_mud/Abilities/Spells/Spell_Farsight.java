package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
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

		addQualifyingClass("Mage",9);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Farsight();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Which direction should I 'look'?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(!success)
			this.beneficialVisualFizzle(mob,null,"<S-NAME> get(s) a far off look, but the spell fizzles.");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,affectType,"<S-NAME> get(s) a far off look in <S-HIS-HER> eyes.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Room thatRoom=mob.location();
				while(commands.size()>0)
				{
					String whatToOpen=(String)commands.elementAt(0);
					int dirCode=Directions.getGoodDirectionCode(whatToOpen);
					if(dirCode<0)
					{
						mob.tell("\n\r'"+whatToOpen+"' is not a valid direction.");
						commands.removeAllElements();
						success=false;
					}
					else
					{
						Exit exit=thatRoom.exits()[dirCode];
						Room room=thatRoom.doors()[dirCode];

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