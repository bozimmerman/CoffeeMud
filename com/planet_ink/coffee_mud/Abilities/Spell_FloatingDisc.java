package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_FloatingDisc extends Spell
	implements IllusionistDevotion
{
	public Spell_FloatingDisc()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Floating Disc";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		addQualifyingClass(new Mage().ID(),5);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FloatingDisc();
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		if((invoker==null)||(!(invoker instanceof MOB)))
			return;

		MOB mob=(MOB)invoker;
		Item item=(Item)affected;
		super.unInvoke();


		if(item.amWearingAt(Item.FLOATING_NEARBY))
		{
			mob.location().show(mob,item,Affect.VISUAL_WNOISE,"<T-NAME> floating near <S-NAME> now floats back into <S-HIS-HER> hands.");
			item.remove();
		}
		item.recoverEnvStats();
		mob.recoverCharStats();
		mob.recoverEnvStats();
	}


	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("You must specify what to cast this upon.");
			return false;
		}
		String whatToFloat=CommandProcessor.combine(commands,0);
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,whatToFloat);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+whatToFloat+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell("You can't place that on a floating disc.");
			return false;
		}
		if(!mob.isMine(target))
		{
			mob.tell("You don't have that.");
			return false;
		}
		if(mob.amWearingSomethingHere(Item.FLOATING_NEARBY))
		{
			mob.tell("You are already carrying something on a floating disc.");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a floating disc underneath <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				long properWornCode=((Item)target).rawProperLocationBitmap();
				boolean properWornLogical=((Item)target).rawLogicalAnd();
				((Item)target).setRawLogicalAnd(false);
				((Item)target).setRawProperLocationBitmap(Item.FLOATING_NEARBY);
				((Item)target).wear(Item.FLOATING_NEARBY);
				((Item)target).setRawLogicalAnd(properWornLogical);
				((Item)target).setRawProperLocationBitmap(properWornCode);
				((Item)target).recoverEnvStats();
				beneficialAffect(mob,target,mob.envStats().level()*20);
				mob.recoverEnvStats();
				mob.recoverCharStats();
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke a floating disc, but fail(s).");



		// return whether it worked
		return success;
	}
}