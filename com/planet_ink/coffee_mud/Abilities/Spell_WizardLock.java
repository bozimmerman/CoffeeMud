package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_WizardLock extends Spell
	implements AlterationDevotion
{

	private static final String addOnString=" of ENORMOUS SIZE!!!";

	public Spell_WizardLock()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wizard Lock";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Mage().ID(),3);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WizardLock();
	}

	public boolean okAffect(Affect affect)
	{
		if(affected==null)
			return true;

		if(!super.okAffect(affect))
			return false;

		MOB mob=affect.source();
		if((!affect.amITarget(affected))&&(affect.tool()!=affected))
			return true;
		else
		if(((affect.targetType()&Affect.SOUND)>0)||((affect.targetType()&Affect.VISUAL)>0))
			return true;
		else
		switch(affect.targetCode())
		{
		case Affect.HANDS_OPEN:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case Affect.HANDS_UNLOCK:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case Affect.HANDS_DELICATE:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		default:
			break;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		if(commands.size()<1)
		{
			mob.tell("Wizard Lock what?.");
			return false;
		}
		String targetName=CommandProcessor.combine(commands,0);
		Integer cmd=(Integer)CommandProcessor.commandSet.get(targetName.toUpperCase());
		Environmental target=null;
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
				int dirCode=Directions.getDirectionCode(targetName);
				target=mob.location().getExit(dirCode);
			}
		}
		if(target==null)
			target=mob.location().fetchFromMOBRoom(mob,null,targetName);

		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}

		if((!(target instanceof Container))&&(!(target instanceof Exit)))
		{
			mob.tell("You can't lock that.");
			return false;
		}

		if(target instanceof Container)
		{
			Container container=(Container)target;
			if((!container.hasALid)||(!container.hasALock))
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}
		else
		if(target instanceof Exit)
		{
			Exit exit=(Exit)target;
			if(!exit.hasADoor())
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(name()+" is already magically locked!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> point(s) <S-HIS-HER> finger at <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> look(s) shut tight!");
				beneficialAffect(mob,target,0);
				if(target instanceof Exit)
				{
					Exit exit=(Exit)target;
					if(exit.hasALock())
						exit.setHasLock(true);
					exit.setOpen(false);
				}
				else
				if(target instanceof Container)
				{
					Container container=(Container)target;
					container.isLocked=true;
					container.isOpen=false;
				}
				else
				if(target instanceof Exit)
					((Exit)target).tick(ServiceEngine.EXIT_REOPEN);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> point(s) <S-HIS-HER> at <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}