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

public class Spell_Enlarge extends Spell
	implements AlterationDevotion
{

	private static final String addOnString=" of ENORMOUS SIZE!!!";

	public Spell_Enlarge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Enlarge Object";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(2);

		addQualifyingClass(new Mage().ID(),2);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Enlarge();
	}
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(affectableStats.weight()+9999);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Item))
			return;
		Item item=(Item)affected;
		if(item.name().endsWith(addOnString))
			item.setName(item.name().substring(0,item.name().length()-addOnString.length()).trim());
		int x=item.displayText().indexOf(addOnString);
		if(x>=0)
			item.setDisplayText(item.displayText().substring(0,x)+item.displayText().substring(x+addOnString.length()));
		item.recoverEnvStats();
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<1)
		{
			mob.tell("Enlarge what?.");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!(target instanceof Item))
		{
			mob.tell("You can't enlarge that.");
			return false;
		}
		if(mob.isMine(target))
		{
			mob.tell("You'd better put it down first.");
			return false;
		}
		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> grow(s) to an enormous size!");
				beneficialAffect(mob,target,100);
				String lastWordIn=Util.lastWordIn(target.name());
				int x=target.displayText().toUpperCase().indexOf(lastWordIn.toUpperCase());
				if(x>=0)
					target.setDisplayText(target.displayText().substring(0,x+lastWordIn.length())+addOnString+target.displayText().substring(x+lastWordIn.length()));

				target.setName(target.name()+addOnString);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}