package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Skill_SongWrite extends StdAbility
{
	public String ID() { return "Skill_SongWrite"; }
	public String name(){ return "Song Write";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SONGWRITE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_SongWrite();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Write which song onto what?");
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
			mob.tell("You can't write music on that.");
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
		Song scrollThis=null;
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)
			&&(A instanceof Song)
			&&(A.name().toUpperCase().startsWith(spellName.toUpperCase()))
			&&(!A.ID().equals(this.ID())))
				scrollThis=(Song)A;
		}
		if(scrollThis==null)
		{
			mob.tell("You don't know how to write '"+spellName+"'.");
			return false;
		}
		int numSpells=(CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this));
		if(numSpells<0) numSpells=0;
		if(scroll.numSpells()>numSpells)
		{
			mob.tell("You aren't powerful enough to write any more magic onto "+scroll.name()+".");
			return false;
		}

		for(int i=0;i<scroll.getSpells().size();i++)
			if(((Ability)scroll.getSpells().elementAt(i)).ID().equals(scrollThis.ID()))
			{
				mob.tell("That spell is already written on "+scroll.name()+".");
				return false;
			}

		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(!auto)mob.curState().setMana(0);

		int experienceToLose=20*CMAble.lowestQualifyingLevel(scrollThis.ID());
		mob.charStats().getCurrentClass().loseExperience(mob,experienceToLose);
		mob.tell("You lose "+experienceToLose+" experience points for the effort.");

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			setMiscText(scrollThis.ID());
			FullMsg msg=new FullMsg(mob,target,this,(auto?Affect.MASK_GENERAL:0)|Affect.MSG_DELICATE_SMALL_HANDS_ACT,"^S<S-NAME> write(s) music onto <T-NAMESELF>, singing softly.^?");
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
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to write music on <T-NAMESELF>, singing softly, and looking very frustrated.");


		// return whether it worked
		return success;
	}
}
