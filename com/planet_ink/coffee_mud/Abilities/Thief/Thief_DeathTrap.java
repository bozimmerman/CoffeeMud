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
public class Thief_DeathTrap extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_DeathTrap"; }
	public String name(){ return "Death Trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"DEATHTRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_TRAPPING; }
	protected boolean sprung=false;

	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public boolean sprung(){return false;}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
    public Vector getTrapComponents() {
        Vector V=new Vector();
        for(int i=0;i<100;i++)
        V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON));
        return V;
    }
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Trap T=(Trap)copyOf();
		T.setInvoker(mob);
		E.addEffect(T);
		CMLib.threads().startTickDown(T,Tickable.TICKID_TRAP_DESTRUCTION,(int)(CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)+(2*getXLEVELLevel(mob))));
		return T;
	}

	public void spring(MOB M)
	{
		if((!sprung)&&(CMLib.dice().rollPercentage()+(2*getXLEVELLevel(invoker()))>M.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			CMLib.combat().postDeath(invoker(),M,null);
	}

	public MOB invoker()
	{
		if(super.miscText.length()==0)
			return super.invoker();
		MOB M=super.invoker();
		if((M!=null)&&(M.Name().equals(miscText))) return M;
		M=CMLib.players().getLoadPlayer(miscText);
		if(M==null)
			miscText="";
		else
			invoker=M;
		return super.invoker();
	}


	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==affected)
		&&(msg.source()!=invoker())
		&&(!msg.source().Name().equals(text()))
		&&(!sprung)
		&&(invoker()!=null)
		&&(invoker().mayIFight(msg.source()))
		&&((canBeUninvoked())||(!CMLib.law().doesHavePriviledgesHere(msg.source(),(Room)affected)))
		&&(CMLib.dice().rollPercentage()>msg.source().charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			CMLib.combat().postDeath(invoker(),msg.source(),msg);
		super.executeMsg(myHost,msg);
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

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room trapThis=mob.location();

		Item resource=CMLib.materials().findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
		if(resource == null)
			resource=CMLib.materials().findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_MITHRIL);
		int amount=0;
		if(resource!=null)
			amount=CMLib.materials().findNumberOfResource(mob.location(),resource.material());
		if(amount<100)
		{
			mob.tell("You need 100 pounds of raw metal to build this trap.");
			return false;
		}
        if(mob.isInCombat())
        {
            mob.tell("You are too busy to get that done right now.");
            return false;
        }

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(resource!=null)
			CMLib.materials().destroyResources(mob.location(),100, resource.material(), -1, null);

		CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,(auto?trapThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap here."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
			{
				mob.tell("You have set the trap.");
				setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1,false);
				Thief_DeathTrap T=(Thief_DeathTrap)trapThis.fetchEffect(ID());
				if(T!=null) T.setMiscText(mob.Name());
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50)
				{
					Trap T=setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1,false);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
					T.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt to set the death trap.");
				}
			}
		}
		return success;
	}
}
