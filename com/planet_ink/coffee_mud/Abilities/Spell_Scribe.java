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

		addQualifyingClass(new Mage().ID(),14);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Scribe();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<2)
		{
			mob.tell("Scribe which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,(String)commands.lastElement());
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		if(!(target instanceof ScrollSpell))
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
		ScrollSpell scroll=(ScrollSpell)target;

		String spellName=CommandProcessor.combine(commands,0).trim();
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

		if(mob.getExperience()<(mob.envStats().level()-1)*1000)
		{
			mob.tell("You need to gain more experience before you can cast this.");
			return false;
		}

		// lose all the mana!
		if(!super.invoke(mob,commands))
			return false;

		mob.curState().setMana(0);
		mob.setExperience(mob.getExperience()-100);

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(scroll.text().trim().length()==0)
					scroll.setMiscText(scrollThis.ID());
				else
					scroll.setMiscText(scroll.text()+";"+scrollThis.ID());
				scroll.setUsesRemaining(scroll.usesRemaining()+1);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAME>, looking very frustrated.");


		// return whether it worked
		return success;
	}
}
