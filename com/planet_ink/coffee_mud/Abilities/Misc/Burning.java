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
public class Burning extends StdAbility
{
	public String ID() { return "Burning"; }
	public String name(){ return "Burning";}
	public String displayText(){ return "(Burning)";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HEATING|Ability.FLAG_FIREBASED;}

	public boolean tick(Tickable ticking, int tickID)
	{
        if((affected instanceof Item)&&(((Item)affected).owner() instanceof Room))
        {
            int unInvokeChance=0;
            String what=null;
            switch(((Room)(((Item)affected).owner())).getArea().getClimateObj().weatherType(((Room)(((Item)affected).owner()))))
            {
            case Climate.WEATHER_RAIN:
                what="rain";
                unInvokeChance=10;
                break;
            case Climate.WEATHER_THUNDERSTORM:
                what="pounding rain";
                unInvokeChance=15;
                break;
            case Climate.WEATHER_SLEET:
                what="sleet";
                unInvokeChance=5;
                break;
            case Climate.WEATHER_BLIZZARD:
                what="swirling snow";
                unInvokeChance=10;
                break;
            case Climate.WEATHER_SNOW:
                what="snow";
                unInvokeChance=10;
                break;
            }
            if(CMLib.dice().rollPercentage()<unInvokeChance)
            {
                Room R=((Room)(((Item)affected).owner()));
                if(R.numInhabitants()>0)
                    R.showHappens(CMMsg.MSG_OK_ACTION,"The "+what+" puts out "+affected.name()+".");
                unInvoke();
                return false;
            }
        }
		if((tickDown<2)&&(affected!=null))
		{
			if(affected instanceof Item)
			{
				Environmental E=((Item)affected).owner();
				if(E==null)
					((Item)affected).destroy();
				else
				if(E instanceof Room)
				{
					Room room=(Room)E;
					if((affected instanceof RawMaterial)
					&&(room.isContent((Item)affected)))
					{
						for(int i=0;i<room.numItems();i++)
						{
							Item I=room.fetchItem(i);
							if(I.name().equals(affected.name())
							&&(I!=affected)
							&&(I instanceof RawMaterial)
							&&(I.material()==((Item)affected).material()))
							{
								int durationOfBurn=CMLib.flags().burnStatus(I);
								if(durationOfBurn<=0) durationOfBurn=5;
								Burning B=new Burning();
								B.invoke(invoker,I,true,durationOfBurn);
								break;
							}
						}
					}
                    if(!(affected instanceof ClanItem))
					switch(((Item)affected).material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_LIQUID:
					case RawMaterial.MATERIAL_METAL:
					case RawMaterial.MATERIAL_MITHRIL:
					case RawMaterial.MATERIAL_ENERGY:
					case RawMaterial.MATERIAL_PRECIOUS:
					case RawMaterial.MATERIAL_ROCK:
					case RawMaterial.MATERIAL_UNKNOWN:
						break;
					default:
					    if(CMLib.flags().isABonusItems(affected))
					    {
							if(invoker==null)
							{
								invoker=CMClass.getMOB("StdMOB");
								invoker.setLocation(CMClass.getLocale("StdRoom"));
								invoker.baseEnvStats().setLevel(affected.envStats().level());
								invoker.envStats().setLevel(affected.envStats().level());
							}
					        room.showHappens(CMMsg.MSG_OK_ACTION,affected.name()+" EXPLODES!!!");
					        for(int i=0;i<room.numInhabitants();i++)
					        {
					            MOB target=room.fetchInhabitant(i);
								CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(affected.envStats().level(),5,1),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The blast <DAMAGE> <T-NAME>!");
					        }
							((Item)affected).destroy();
					    }
					    else
					    {
							Item ash=CMClass.getItem("GenResource");
							ash.setName("some ash");
							ash.setDisplayText("a small pile of ash is here");
							ash.setMaterial(RawMaterial.RESOURCE_ASH);
							ash.baseEnvStats().setWeight(1);
							ash.recoverEnvStats();
							room.addItemRefuse(ash,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
							((RawMaterial)ash).rebundle();
							if((affected instanceof RawMaterial)
							&&(affected.baseEnvStats().weight()>1)
							&&(CMLib.flags().burnStatus(affected)>0))
							{
								affected.baseEnvStats().setWeight(affected.baseEnvStats().weight()-1);
								affected.recoverEnvStats();
								this.tickDown = CMLib.flags().burnStatus(affected);
								CMLib.materials().adjustResourceName((Item)affected);
								((Room)E).recoverRoomStats();
								return super.tick(ticking,tickID);
							}
							((Room)E).showHappens(CMMsg.MSG_OK_VISUAL, affected.name()+" is no longer burning.");
							((Item)affected).destroy();
					    }
						break;
					}
					((Room)E).recoverRoomStats();
				}
				else
				if(E instanceof MOB)
				{
					switch(((Item)affected).material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_LIQUID:
					case RawMaterial.MATERIAL_METAL:
					case RawMaterial.MATERIAL_ENERGY:
					case RawMaterial.MATERIAL_MITHRIL:
					case RawMaterial.MATERIAL_PRECIOUS:
					case RawMaterial.MATERIAL_ROCK:
					case RawMaterial.MATERIAL_UNKNOWN:
						break;
					default:
						((Item)affected).destroy();
						break;
					}
					((MOB)E).location().recoverRoomStats();
				}
				return false;
			}
		}
		if(!super.tick(ticking,tickID))
			return false;

		if(tickID!=Tickable.TICKID_MOB)
			return true;

		if(affected==null)
			return false;

		if(affected instanceof Item)
		{
			Item I=(Item)affected;
            Environmental owner=I.owner();
            if(owner instanceof MOB)
            {
    			if(!ouch((MOB)owner))
    				CMLib.commands().postDrop((MOB)owner,I,false,false);
    			if(I.subjectToWearAndTear())
    			{
    				if((I.usesRemaining()<1000)
    				&&(I.usesRemaining()>1))
    					I.setUsesRemaining(I.usesRemaining()-1);
    			}
            }
		}

		// might want to add the ability for it to spread
		return true;
	}

	public boolean ouch(MOB mob)
	{
		if(CMLib.dice().rollPercentage()>(mob.charStats().getSave(CharStats.STAT_SAVE_FIRE)-50))
		{
			if(affected instanceof Item)
			switch(((Item)affected).material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_UNKNOWN:
				mob.tell("Ouch!! "+CMStrings.capitalizeAndLower(affected.name())+" is HOT!");
				break;
			default:
				mob.tell("Ouch!! "+CMStrings.capitalizeAndLower(affected.name())+" is on fire!");
				break;
			}
			CMLib.combat().postDamage(invoker,mob,this,CMLib.dice().roll(1,5,5),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,null);
			return false;
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected!=null)
		&&(affected instanceof Item)
		&&(msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_GET))
		{
			if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
				return ouch(msg.source());
			// the "oven" exception
			Item container=(Item)affected;
			Item target=(Item)msg.tool();
			if((target.owner()==container.owner())
			&&(target.container()==container))
				switch(container.material()&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_PRECIOUS:
				case RawMaterial.MATERIAL_ENERGY:
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_UNKNOWN:
					return true;
				default:
					break;
				}
			return ouch(msg.source());
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(msg.tool()!=null)
		&&(msg.tool()==affected)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Container)
		&&(msg.targetMinor()==CMMsg.TYP_PUT))
		{
			Item I=(Item)affected;
			Item C=(Container)msg.target();
			if((C instanceof Drink)
			   &&(((Drink)C).containsDrink()))
			{
				msg.addTrailerMsg(CMClass.getMsg(invoker,null,CMMsg.MSG_OK_VISUAL,I.name()+" is extinguished."));
				I.delEffect(this);
			}
		}
		super.executeMsg(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental target, boolean auto, int asLevel)
	{
		if(!auto) return false;
		if(target==null) return false;
		if(target.fetchEffect("Burning")==null)
		{
			if(((target instanceof Item)&&(((Item)target).material()==RawMaterial.RESOURCE_NOTHING))
			||(target instanceof ClanItem))
				return false;
			if((mob!=null)&&(mob.location()!=null))
			{
				CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,null);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			if(asLevel == 0)
				asLevel = CMLib.flags().burnStatus(target);
			if(asLevel < 0) asLevel = 0;
			beneficialAffect(mob,target,0,asLevel);
			target.recoverEnvStats();
			if(target instanceof Item)
			{
				Environmental owner=((Item)target).owner();
				if(owner!=null)
				{
					owner.recoverEnvStats();
					if(owner instanceof Room)
						((Room)owner).recoverRoomStats();
					else
					if(owner instanceof MOB)
						if(((MOB)owner).location()!=null)
							((MOB)owner).location().recoverRoomStats();
				}
			}
		}
		return true;
	}
}
