package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_Scribe extends Spell
	implements InvocationDevotion
{
	public Spell_Scribe()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Scribe";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(14);

		addQualifyingClass("Mage",14);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Scribe();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<2)
		{
			mob.tell("Scribe which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement());
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof Scroll))
		{
			mob.tell("You can't scribe onto that.");
			return false;
		}
		if(mob.curState().getMana()<mob.maxState().getMana())
		{
			mob.tell("You need to be at full mana to cast this.");
			return false;
		}

		commands.removeElementAt(commands.size()-1);
		Scroll scroll=(Scroll)target;

		String spellName=Util.combine(commands,0).trim();
		Spell scrollThis=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A instanceof Spell)&&(A.qualifies(mob))&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))&&(!A.ID().equals(this.ID())))
				scrollThis=(Spell)A;
		}
		if(scrollThis==null)
		{
			mob.tell("You don't know how to scribe '"+spellName+"'.");
			return false;
		}

		if(scroll.numSpells()>(mob.envStats().level()-this.qualifyingLevel(mob)))
		{
			mob.tell("You aren't powerful enough to scribe any more spells onto "+scroll.name()+".");
			return false;
		}

		for(int i=0;i<scroll.getSpells().size();i++)
			if(((Ability)scroll.getSpells().elementAt(i)).ID().equals(scrollThis.ID()))
			{
				mob.tell("That spell is already scribed onto "+scroll.name()+".");
				return false;
			}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		mob.curState().setMana(0);
		mob.setExperience(mob.getExperience()-100);

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, chanting softly.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(scroll.getScrollText().trim().length()==0)
					scroll.setScrollText(scrollThis.ID());
				else
					scroll.setScrollText(scroll.getScrollText()+";"+scrollThis.ID());
				if((scroll.usesRemaining()==Integer.MAX_VALUE)||(scroll.usesRemaining()<0))
					scroll.setUsesRemaining(0);
				scroll.setUsesRemaining(scroll.usesRemaining()+1);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, chanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
