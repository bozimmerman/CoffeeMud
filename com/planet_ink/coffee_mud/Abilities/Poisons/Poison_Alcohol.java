package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Poison_Alcohol extends Poison
{
	public String ID() { return "Poison_Alcohol"; }
	public String name(){ return "Alcohol";}
	private static final String[] triggerStrings = {"POISONALCOHOL"};
	public String displayText(){ return "(Drunk)";}
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 65;}
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "You feel sober again.";}
	protected String POISON_START(){return "^G<S-NAME> burp(s)!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F<S-NAME> inebriate(s) <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to inebriate <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	protected boolean disableHappiness=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((Dice.rollPercentage()==1)&&(!((MOB)affected).isMonster()))
			{
				Ability A=CMClass.getAbility("Disease_Migraines");
				if((A!=null)&&(mob.fetchEffect(A.ID())==null))
					A.invoke(mob,mob,true);
			}
			CommonMsgs.stand(mob,true);
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if(disableHappiness){disableHappiness=false; return true;}

		MOB mob=(MOB)affected;
		if(mob==null) return true;

		Room room=mob.location();
		if((Dice.rollPercentage()<20)&&(Sense.aliveAwakeMobile(mob,true))&&(room!=null))
		{
			if(mob.getAlignment()<350)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around making ugly faces.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_NOISE,"<S-NAME> belch(es) grotesquely.");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> spin(s) <S-HIS-HER> head around.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_NOISE,"<S-NAME> can't stop snarling.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> just fell over!");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely sh** faced!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at the ground.");
				break;
			}
			else
			if(mob.getAlignment()<650)
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around aimlessly.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> burp(s) noncommitally.");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> almost fell over.");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and almost smile(s).");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> belch(es)!");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely drunk!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly ahead.");
				break;
			}
			else
			switch(Dice.roll(1,9,-1))
			{
			case 0:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stagger(s) around trying to hug everyone.");
				break;
			case 1:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> hiccup(s) and smile(s).");
				break;
			case 2:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> bob(s) <S-HIS-HER> head back and forth.");
				break;
			case 3:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't stop smiling.");
				break;
			case 4:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> lean(s) slightly to one side.");
				break;
			case 5:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> look(s) around with glazed over eyes.");
				break;
			case 6:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> can't seem to focus.");
				break;
			case 7:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> <S-IS-ARE> definitely a bit tipsy!");
				break;
			case 8:
				room.show(mob,null,this,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> stare(s) blankly at <S-HIS-HER> eyelids.");
				break;
			}

		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof MOB)
		{
			if(msg.source()!=affected)
				return true;
			if(msg.source().location()==null)
				return true;

			if((msg.amISource((MOB)affected))
			&&(msg.sourceMessage()!=null)
			&&(msg.tool()==null)
			&&((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			   ||(msg.sourceMinor()==CMMsg.TYP_TELL)
			   ||(Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))))
			{
				Ability A=CMClass.getAbility("Drunken");
				if(A!=null)
				{
					A.setProfficiency(100);
					A.invoke(msg.source(),null,true);
					A.setAffectedOne(msg.source());
					if(!A.okMessage(myHost,msg))
						return false;
				}
			}
			else
			if((!Util.bset(msg.targetMajor(),CMMsg.MASK_GENERAL))
			&&(msg.targetMajor()>0))
			{
				if((msg.target() !=null)
					&&(msg.target() instanceof MOB))
						msg.modify(msg.source(),msg.source().location().fetchInhabitant(Dice.roll(1,msg.source().location().numInhabitants(),0)-1),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
			}
		}
		else
		{

		}
		return true;
	}
}
