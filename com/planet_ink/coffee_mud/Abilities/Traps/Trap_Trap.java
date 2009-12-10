package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Trap_Trap extends StdAbility implements Trap
{
	public String ID() { return "Trap_Trap"; }
	public String name(){ return "a Trap!";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}

	protected static MOB benefactor=CMClass.getMOB("StdMOB");
	protected boolean sprung=false;
	protected Room myPit=null;
	protected Room myPitUp=null;
	protected int reset=60; // 5 minute reset is standard
	protected int trapType(){return CMLib.dice().roll(1,3,-1);}

	public Trap_Trap()
	{
		super();
		if(benefactor==null)
			benefactor=CMClass.getMOB("StdMOB");
	}

	public void activateBomb(){}
	public boolean isABomb(){return false;}
	public boolean sprung(){return sprung;}
	public boolean disabled(){return sprung;}
	public void disable(){ sprung=true;}
	public void setReset(int Reset){reset=Reset;}
	public int getReset(){return reset;}

	// as these are not standard traps, we return this!
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
    public Vector getTrapComponents() { return new Vector(); }
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		int level=mob.envStats().level();
		if(level<qualifyingClassLevel) level=qualifyingClassLevel;
		level+=trapBonus;
		if(level>=100) level=99;
		int rejuv=((100-level)*30);
		Trap T=(Trap)copyOf();
		T.setReset(rejuv);
		T.setInvoker(mob);
		E.addEffect(T);
        T.setAbilityCode(trapBonus);
        if(perm)
        {
            T.setSavable(true);
            T.makeNonUninvokable();
        }
        else
        {
            T.setSavable(false);
    		CMLib.threads().startTickDown(T,Tickable.TICKID_TRAP_DESTRUCTION,level*30);
        }
		return T;
	}
	public void gas(MOB mob)
	{
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a gas trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap set in <T-NAME>!"))
			if(mob.envStats().level()>15)
			{
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The room fills with gas!");
				for(int i=0;i<mob.location().numInhabitants();i++)
				{
					MOB target=mob.location().fetchInhabitant(i);
					if(target==null) break;

					int dmg=CMLib.dice().roll(target.envStats().level(),10,1);
					CMMsg msg=CMClass.getMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_GAS,CMMsg.MSG_NOISYMOVEMENT,null);
					if(target.location().okMessage(target,msg))
					{
						target.location().send(target,msg);
						if(msg.value()>0)
							dmg=(int)Math.round(CMath.div(dmg,2.0));
						CMLib.combat().postDamage(invoker(),target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"The gas <DAMAGE> <T-NAME>!");
					}
				}
			}
			else
			{
				MOB target=mob;
				int dmg=CMLib.dice().roll(target.envStats().level(),10,1);
				CMMsg msg=CMClass.getMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_GAS,CMMsg.MSG_NOISYMOVEMENT,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					if(msg.value()>0)
						dmg=(int)Math.round(CMath.div(dmg,2.0));
					CMLib.combat().postDamage(invoker(),target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,Weapon.TYPE_GASSING,"A sudden blast of gas <DAMAGE> <T-NAME>!");
				}
			}
	}

	public void needle(MOB mob)
	{
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a needle trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a needle trap set in <T-NAME>!"))
		{
			MOB target=mob;
			int dmg=CMLib.dice().roll(target.envStats().level(),5,1);
			CMMsg msg=CMClass.getMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()>0)
					dmg=(int)Math.round(CMath.div(dmg,2.0));
				CMLib.combat().postDamage(invoker(),target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"The needle <DAMAGE> <T-NAME>!");

				Ability P=CMClass.getAbility("Poison");
				if(P!=null) P.invoke(invoker(),target,true,0);
			}
		}
	}

	public void blade(MOB mob)
	{
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a blade trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a blade trap set in <T-NAME>!"))
		{
			MOB target=mob;
			int dmg=CMLib.dice().roll(target.envStats().level(),2,0);
			CMMsg msg=CMClass.getMsg(invoker(),target,this,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()>0)
					dmg=(int)Math.round(CMath.div(dmg,2.0));
				Ability P=CMClass.getAbility("Poison");
				if(P!=null) P.invoke(invoker(),target,true,0);
				CMLib.combat().postDamage(invoker(),target,this,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,"The blade <DAMAGE> <T-NAME>!");
			}
		}
	}

	public void victimOfSpell(MOB mob)
	{
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a magic trap set in <T-NAME>.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap set in <T-NAME>!"))
		{
			String spell=text();
			int x=spell.indexOf(";");
			Vector V=new Vector();
			V.addElement(mob.name());
			if(x>0)
			{
				V=CMParms.parse(spell.substring(x+1));
				V.insertElementAt(mob.name(),0);
				spell=spell.substring(0,x);
			}
			Ability A=CMClass.findAbility(spell);
			if(A==null)
			{
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"But nothing happened...");
				return;
			}
			A.invoke(invoker(),V,mob,true,0);
		}
	}

	public void fallInPit(MOB mob)
	{
		if(CMLib.flags().isInFlight(mob))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> pause(s) over it in flight.");
			return;
		}
		else
		if(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> avoid(s) a trap door beneath <S-HIS-HER> feet.");
		else
		if(mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap door beneath <S-HIS-HER> feet! <S-NAME> fall(s) in!"))
		{
			if((myPit==null)||(myPitUp==null))
			{
				myPitUp=CMClass.getLocale("ClimbableSurface");
				myPitUp.setRoomID("");
				myPitUp.setArea(mob.location().getArea());
				myPitUp.baseEnvStats().setDisposition(myPitUp.baseEnvStats().disposition()|EnvStats.IS_DARK);
				myPitUp.setDisplayText("Inside a dark pit");
				myPitUp.setDescription("The walls here are slick and tall.  The trap door has already closed.");
				myPitUp.recoverEnvStats();

				myPit=CMClass.getLocale("StdRoom");
				myPit.setRoomID("");
				myPit.setArea(mob.location().getArea());
				myPit.baseEnvStats().setDisposition(myPit.baseEnvStats().disposition()|EnvStats.IS_DARK);
				myPit.setDisplayText("Inside a dark pit");
				myPit.setDescription("The walls here are slick and tall.  You can barely see the closed trap door well above you.");
				myPit.setRawExit(Directions.UP,CMClass.getExit("StdOpenDoorway"));
				myPit.rawDoors()[Directions.UP]=myPitUp;
				myPitUp.recoverEnvStats();

			}
			myPitUp.setRawExit(Directions.UP,CMClass.getExit("StdClosedDoorway"));
			myPitUp.rawDoors()[Directions.UP]=mob.location();
			if((mob.location().getRoomInDir(Directions.DOWN)==null)
			&&(mob.location().getExitInDir(Directions.DOWN)==null))
			{
				mob.location().setRawExit(Directions.DOWN,CMClass.getExit("StdClosedDoorway"));
				mob.location().rawDoors()[Directions.DOWN]=myPitUp;
			}
			myPit.bringMobHere(mob,false);
			if(mob.envStats().weight()<5)
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
			else
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
				int damage=CMLib.dice().roll(mob.envStats().level(),3,1);
				CMLib.combat().postDamage(invoker(),mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
			}
			CMLib.commands().postLook(mob,true);
		}
	}

	public MOB invoker()
	{
		if(invoker==null) return benefactor;
		return super.invoker();
	}

	public int classificationCode()
	{
		return Ability.ACODE_TRAP;
	}

	public void spring(MOB target)
	{
		sprung=true;
		benefactor.setLocation(target.location());
		benefactor.baseEnvStats().setLevel(target.envStats().level());
		benefactor.recoverCharStats();
		benefactor.recoverEnvStats();
		switch(trapType())
		{
		case TRAP_GAS:
			gas(target);
			break;
		case TRAP_NEEDLE:
			needle(target);
			break;
		case TRAP_PIT_BLADE:
			if(affected instanceof Exit)
				fallInPit(target);
			else
				blade(target);
			break;
		case TRAP_SPELL:
			victimOfSpell(target);
			break;
		default:
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) a trap, but it appears to have misfired.");
			break;
		}

		if((getReset()>0)&&(getReset()<Integer.MAX_VALUE))
			CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,getReset());
		else
			unInvoke();
	}

	public void unInvoke()
	{
		if((trapType()==Trap.TRAP_PIT_BLADE)
		&&(affected instanceof Exit)
		&&(myPit!=null)
		&&(canBeUninvoked())
		&&(myPitUp!=null))
		{
			Room R=myPitUp.getRoomInDir(Directions.UP);
			if((R!=null)&&(R.getRoomInDir(Directions.DOWN)==myPitUp))
			{
				R.rawDoors()[Directions.DOWN]=null;
				R.setRawExit(Directions.DOWN,null);
			}
			/**
			don't do this, cuz someone might still be down there.
			myPitUp.rawDoors()[Directions.UP]=null;
			myPitUp.getRawExit(Directions.UP]=null;
			myPitUp.rawDoors()[Directions.DOWN]=null;
			myPitUp.getRawExit(Directions.DOWN]=null;
			myPit.rawDoors()[Directions.UP]=null;
			myPit.getRawExit(Directions.UP]=null;
			*/
			if(myPit!=null) myPit.destroy();
			if(myPitUp!=null) myPitUp.destroy();
		}
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}

}
