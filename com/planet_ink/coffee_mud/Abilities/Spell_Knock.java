package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Knock extends Spell
	implements AlterationDevotion
{

	public Spell_Knock()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Knock";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Knock Spell)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Mage().ID(),3);
		addQualifyingClass(new Thief().ID(),18);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Knock();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		String whatToOpen=CommandProcessor.combine(commands,0);
		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
		Environmental openThis=null;
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
			{
				int dirCode=Directions.getDirectionCode(whatToOpen);
				openThis=mob.location().getExit(dirCode);
			}
		}
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoom(mob,null,whatToOpen);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see that here.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;


		int levelDiff=openThis.envStats().level()-mob.envStats().level();

		boolean success=profficiencyCheck(-(levelDiff*5));

		if(!success)
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> point(s) at "+openThis.name()+" and shouts incoherantly, but nothing happens.");
		else
		{
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> point(s) at "+openThis.name());
			for(int a=0;a<openThis.numAffects();a++)
			{
				Ability A=openThis.fetchAffect(a);
				if((A instanceof Spell_WizardLock)&&(A.invoker()!=null)&&(A.invoker().envStats().level()<mob.envStats().level()+3))
				{
					A.unInvoke();
					break;
				}
				mob.location().show(mob,null,Affect.SOUND_MAGIC,"A spell aroud "+openThis.name()+" seems to fade.");
			}

			FullMsg msg=new FullMsg(mob,openThis,null,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> point(s) at <T-NAME>");
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,openThis,null,Affect.HANDS_UNLOCK,Affect.HANDS_UNLOCK,Affect.VISUAL_WNOISE,null);
				mob.location().send(mob,msg);
				msg=new FullMsg(mob,openThis,null,Affect.HANDS_OPEN,Affect.HANDS_OPEN,Affect.VISUAL_WNOISE,"<T-NAME> opens.");
				mob.location().send(mob,msg);
			}
		}

		return success;
	}
}