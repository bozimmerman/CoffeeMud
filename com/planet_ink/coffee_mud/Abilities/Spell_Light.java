package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Spell_Light extends Spell
	implements InvocationDevotion
{

	public Spell_Light()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Light";
		displayText="(Light)";
		miscText="";

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
		return new Spell_Light();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		if(!(affected instanceof Room))
			affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_LIGHT);
		if(Sense.isInDark(affected))
			affectableStats.setDisposition(affectableStats.disposition()-Sense.IS_DARK);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		Room room=((MOB)affected).location();
		room.show(mob,null,Affect.VISUAL_ONLY,"The light above <S-NAME> dims.");
		super.unInvoke();
		room.recoverRoomStats();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You already have light.");
			return false;
		}

		boolean success=profficiencyCheck(0);

		FullMsg msg=new FullMsg(mob,mob.location(),this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.VISUAL_WNOISE,"<S-NAME> invoke(s) a white light above <S-HIS-HER> head!");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,0);
		}
		else
			beneficialFizzle(mob,mob.location(),"<S-NAME> attempt(s) to invoke light, but fail(s).");

		return success;
	}
}
