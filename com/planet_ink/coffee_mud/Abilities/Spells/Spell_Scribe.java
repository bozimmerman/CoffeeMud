package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Scribe extends Spell
{
	public String ID() { return "Spell_Scribe"; }
	public String name(){return "Scribe";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_Scribe();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<2)
		{
			mob.tell("Scribe which spell onto what?");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.lastElement(),Item.WORN_REQ_UNWORNONLY);
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
		if((mob.curState().getMana()<mob.maxState().getMana())&&(!auto))
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
			if((A!=null)
			&&(A instanceof Spell)
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				scrollThis=(Spell)A;
		}
		if(scrollThis==null)
		{
			mob.tell("You don't know how to scribe '"+spellName+"'.");
			return false;
		}
		int numSpells=(CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this));
		if(numSpells<0) numSpells=0;
		if(scroll.numSpells()>numSpells)
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

		if(!auto)mob.curState().setMana(0);
		
		int experienceToLose=10*CMAble.lowestQualifyingLevel(scrollThis.ID());
		mob.charStats().getCurrentClass().loseExperience(mob,experienceToLose);
		mob.tell("You lose "+experienceToLose+" experience points for the effort.");

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			setMiscText(scrollThis.ID());
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting softly.^?");
			if(mob.location().okAffect(mob,msg))
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
			beneficialWordsFizzle(mob,target,"<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, encanting softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
