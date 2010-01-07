package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Regeneration extends StdAbility
{
	private static final int maxTickDown=3;
    protected int regenTick=maxTickDown;

	public String ID() { return "Regeneration"; }
	public String name(){ return "Stat Regeneration";}
	public String displayText(){ return "(Stat Regeneration)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"REGENERATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
    protected int permanentDamage=0;


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((--regenTick)>0)
			return true;
		regenTick=maxTickDown;
		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob.location()==null) return true;
		if(mob.amDead()) return true;

		boolean doneAnything=false;
		doneAnything=doneAnything||mob.curState().adjHitPoints((int)Math.round(CMath.div(mob.envStats().level(),2.0)),mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMana(mob.envStats().level()*2,mob.maxState());
		doneAnything=doneAnything||mob.curState().adjMovement(mob.envStats().level()*3,mob.maxState());
		if(doneAnything)
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> regenerate(s).");
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB M=(MOB)affected;
			if(msg.amISource(M)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
			{
				permanentDamage=0;
				M.recoverMaxState();
			}
			else
			if((msg.amITarget(M))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(text().length()>0))
			{
				String text=text().toUpperCase();
				boolean hurts=false;
				if(msg.tool() instanceof Weapon)
				{
					int x=text.indexOf(Weapon.TYPE_DESCS[((Weapon)msg.tool()).weaponType()]);
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
						hurts=true;
					if(CMLib.flags().isABonusItems(msg.tool()))
					{
						x=text.indexOf("MAGIC");
						if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
							hurts=true;
					}
					x=text.indexOf("LEVEL");
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
					{
						String lvl=text.substring(x+5);
						if(lvl.indexOf(" ")>=0)
							lvl=lvl.substring(lvl.indexOf(" "));
						if(msg.tool().envStats().level()>=CMath.s_int(lvl))
							hurts=true;
					}
					x=text.indexOf(RawMaterial.CODES.NAME(((Weapon)msg.tool()).material()));
					if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
						hurts=true;
				}
				else
				if(msg.tool() instanceof Ability)
				{
					int classType=((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES;
					switch(classType)
					{
					case Ability.ACODE_SPELL:
					case Ability.ACODE_PRAYER:
					case Ability.ACODE_CHANT:
					case Ability.ACODE_SONG:
						{
							int x=text.indexOf("MAGIC");
							if((x>=0)&&((x==0)||(text.charAt(x-1)=='+')))
								hurts=true;
						}
						break;
					default:
						break;
					}
				}
				if(hurts)
				{
					permanentDamage+=msg.value();
					M.recoverMaxState();
				}
			}

		}
		return true;
	}

	public void affectCharState(MOB mob, CharState state)
	{
		super.affectCharState(mob,state);
		state.setHitPoints(state.getHitPoints()-permanentDamage);
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You feel less regenerative.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String str=auto?"":"<S-NAME> lay(s) regenerative magic upon <T-NAMESELF>.";
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,str);
			if(target.location().okMessage(target,msg))
			{
			    target.location().send(target,msg);
				success=beneficialAffect(mob,target,asLevel,0);
			}
		}

        return success;

	}
}
