package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Thief_Kamikaze extends ThiefSkill
{
	public String ID() { return "Thief_Kamikaze"; }
	public String name(){ return "Kamikaze";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"KAMIKAZE"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I.container()==null))
				{
					Trap T=CMLib.utensils().fetchMyTrap(I);
					if((T!=null)&&(T.isABomb()))
					{
						if(!I.amWearingAt(Wearable.IN_INVENTORY))
							CMLib.commands().postRemove(mob,I,true);
						CMLib.commands().postDrop(mob,I,false,false);
						if(I.owner() instanceof Room)
						{
							Room R=(Room)I.owner();
							for(int i2=0;i2<R.numInhabitants();i2++)
							{
								MOB M=R.fetchInhabitant(i2);
								if(M!=null)
									T.spring(M);
							}
							T.disable();
							T.unInvoke();
							I.destroy();
						}
					}
				}
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(!mob.amDead())&&(mob.location()!=null))
		{
			if(mob.amFollowing()!=null)
				CMLib.commands().postFollow(mob,null,false);
			CMLib.commands().postStand(mob,true);
			if((mob.isMonster())&&(!CMLib.flags().isMobile(mob)))
				CMLib.tracking().wanderAway(mob,true,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify who your kamikaze bomber is, and which direction they should go.");
			return false;
		}
		String s=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
		{
			mob.tell("You can't talk "+target.name()+" into a kamikaze mission.");
			return false;
		}

		if((s.length()==0)||(CMParms.parse(s).size()==0))
		{
			mob.tell("Send "+target.charStats().himher()+" which direction?");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		double goldRequired=(double)(( Math.round( ( 100.0 - ( ( mob.charStats().getStat( CharStats.STAT_CHARISMA ) + ( 2.0 * getXLEVELLevel( mob ) ) ) * 2.0 ) ) ) * target.envStats().level() ) );
		String localCurrency=CMLib.beanCounter().getCurrency(target);
	    String costWords=CMLib.beanCounter().nameCurrencyShort(localCurrency,goldRequired);
		if(CMLib.beanCounter().getTotalAbsoluteValue(mob,localCurrency)<goldRequired)
		{
			mob.tell(target.charStats().HeShe()+" requires "+costWords+" to do this.");
			return false;
		}

		Trap bombFound=null;
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I=target.fetchInventory(i);
			if((I!=null)&&(I.container()==null))
			{
				Trap T=CMLib.utensils().fetchMyTrap(I);
				if((T!=null)&&(T.isABomb()))
				{
					bombFound=T;
					break;
				}
			}
		}
		if(bombFound==null)
		{
			mob.tell(target.name()+" must have some bombs for this to work.");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to convince <T-NAMESELF> to kamikaze "+s+", but no deal is reached.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> pay(s) <T-NAMESELF> to Kamikaze "+s+" for "+costWords+".^?");

			CMLib.beanCounter().subtractMoney(mob,localCurrency,goldRequired);
			mob.recoverEnvStats();
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.beanCounter().addMoney(target,localCurrency,goldRequired);
				target.recoverEnvStats();
				beneficialAffect(mob,target,asLevel,2);
				bombFound.activateBomb();
				commands=new Vector();
				commands.addElement("GO");
				commands.addElement(s);
				target.enqueCommand(commands,Command.METAFLAG_FORCED,0);
			}
		}
		return success;
	}

}
