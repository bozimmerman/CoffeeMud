package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Prayer extends StdAbility
{
	public String ID() { return "Prayer"; }
	public String name(){ return "a Prayer";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	private static final String[] triggerStrings = {"PRAY","PR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.PRAYER;}

	protected int affectType(boolean auto){
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}

	protected String prayWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) to "+mob.getMyDeity().name();
		else
			return "pray(s)";
	}

	protected String prayForWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "pray(s) for "+mob.getMyDeity().name();
		else
			return "pray(s)";
	}

	protected String inTheNameOf(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " in the name of "+mob.getMyDeity().name();
		return "";
	}
	protected String againstTheGods(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " against "+mob.getMyDeity().name();
		else
			return " against the gods";
	}
	protected String hisHerDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return mob.getMyDeity().name();
		return "<S-HIS-HER> god";
	}
	protected String ofDiety(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return " of "+mob.getMyDeity().name();
		return "";
	}
	protected String prayingWord(MOB mob)
	{
		if(mob.getMyDeity()!=null)
			return "praying to "+mob.getMyDeity().name();
		else
			return "praying";
	}

	public boolean appropriateToMyAlignment(int alignment)
	{
		int qual=0;
		if(Util.bset(flags(),Ability.FLAG_HOLY))
		{
			if(!Util.bset(flags(),Ability.FLAG_UNHOLY))
				qual=1;
		}
		else
		if(Util.bset(flags(),Ability.FLAG_UNHOLY))
			qual=2;
		switch(qual)
		{
		case 0:
			if((alignment>350)&&(alignment<650))
				return true;
			break;
		case 1:
			if(alignment>650) return true;
			break;
		case 2:
			if(alignment<350) return true;
			break;
		}
		return false;
	}
	public void helpProfficiency(MOB mob)
	{

		Ability A=mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.appropriateToMyAlignment(mob.getAlignment()))
		{
			super.helpProfficiency(mob);
			return;
		}
		return;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,target,auto,asLevel))
			return false;
		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(mob.isMine(this))
		&&(!appropriateToMyAlignment(mob.getAlignment())))
		{
			int hq=500;
			if(Util.bset(flags(),Ability.FLAG_HOLY))
			{
				if(!Util.bset(flags(),Ability.FLAG_UNHOLY))
					hq=1000;
			}
			else
			if(Util.bset(flags(),Ability.FLAG_UNHOLY))
				hq=0;

			int basis=0;
			if(hq==0)
				basis=mob.getAlignment()/10;
			else
			if(hq==1000)
				basis=(1000-mob.getAlignment())/10;
			else
			{
				basis=(500-mob.getAlignment())/10;
				if(basis<0) basis=basis*-1;
				basis-=10;
			}

			if(Dice.rollPercentage()>basis)
				return true;

			if(hq==0)
				mob.tell("The evil nature of "+name()+" disrupts your prayer.");
			else
			if(hq==1000)
				mob.tell("The goodness of "+name()+" disrupts your prayer.");
			else
			if(mob.getAlignment()>650)
				mob.tell("The anti-good nature of "+name()+" disrupts your thought.");
			else
			if(mob.getAlignment()<350)
				mob.tell("The anti-evil nature of "+name()+" disrupts your thought.");
			return false;
		}
		return true;
	}

}
