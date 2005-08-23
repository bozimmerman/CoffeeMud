package com.planet_ink.coffee_mud.Abilities.Misc;
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

public class Burning extends StdAbility
{
	public String ID() { return "Burning"; }
	public String name(){ return "Burning";}
	public String displayText(){ return "(Burning)";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public long flags(){return Ability.FLAG_HEATING|Ability.FLAG_BURNING;}

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
            if(Dice.rollPercentage()<unInvokeChance)
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
					if((affected instanceof EnvResource)
					&&(room.isContent((Item)affected)))
					{
						for(int i=0;i<room.numItems();i++)
						{
							Item I=room.fetchItem(i);
							if(I.name().equals(affected.name())
							&&(I!=affected)
							&&(I instanceof EnvResource)
							&&(I.material()==((Item)affected).material()))
							{
								int durationOfBurn=5;
								switch(I.material()&EnvResource.MATERIAL_MASK)
								{
								case EnvResource.MATERIAL_LEATHER:
									durationOfBurn=20+I.envStats().weight();
									break;
								case EnvResource.MATERIAL_CLOTH:
								case EnvResource.MATERIAL_PAPER:
								case EnvResource.MATERIAL_PLASTIC:
									durationOfBurn=5+I.envStats().weight();
									break;
								case EnvResource.MATERIAL_WOODEN:
									durationOfBurn=40+(I.envStats().weight()*2);
									break;
								case EnvResource.MATERIAL_ENERGY:
									durationOfBurn=1;
									break;
								}
								Burning B=new Burning();
								B.setProfficiency(durationOfBurn);
								B.invoke(invoker,I,true,0);
								break;
							}
						}
					}
					switch(((Item)affected).material()&EnvResource.MATERIAL_MASK)
					{
					case EnvResource.MATERIAL_LIQUID:
					case EnvResource.MATERIAL_METAL:
					case EnvResource.MATERIAL_MITHRIL:
					case EnvResource.MATERIAL_ENERGY:
					case EnvResource.MATERIAL_PRECIOUS:
					case EnvResource.MATERIAL_ROCK:
					case EnvResource.MATERIAL_UNKNOWN:
						break;
					default:
					    if(Sense.isABonusItems(affected))
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
								MUDFight.postDamage(invoker(),target,null,Dice.roll(affected.envStats().level(),5,1),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The blast <DAMAGE> <T-NAME>!");
					        }
							((Item)affected).destroy();
					    }
					    else
					    {
							Item ash=CMClass.getItem("GenResource");
							ash.setName("some ash");
							ash.setDisplayText("a small pile of ash is here");
							ash.setMaterial(EnvResource.RESOURCE_ASH);
							room.addItemRefuse(ash,Item.REFUSE_MONSTER_EQ);
							((Item)affected).destroy();
					    }
						break;
					}
					((Room)E).recoverRoomStats();
				}
				else
				if(E instanceof MOB)
				{
					switch(((Item)affected).material()&EnvResource.MATERIAL_MASK)
					{
					case EnvResource.MATERIAL_LIQUID:
					case EnvResource.MATERIAL_METAL:
					case EnvResource.MATERIAL_ENERGY:
					case EnvResource.MATERIAL_MITHRIL:
					case EnvResource.MATERIAL_PRECIOUS:
					case EnvResource.MATERIAL_ROCK:
					case EnvResource.MATERIAL_UNKNOWN:
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

		if(tickID!=MudHost.TICK_MOB)
			return true;

		if(affected==null)
			return false;

		if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
		{
			Item I=(Item)affected;
			if(!ouch((MOB)I.owner()))
				CommonMsgs.drop((MOB)I.owner(),I,false,false);
			if(I.subjectToWearAndTear())
			{
				if((I.usesRemaining()<1000)
				&&(I.usesRemaining()>1))
					I.setUsesRemaining(I.usesRemaining()-1);
			}
		}

		// might want to add the ability for it to spread
		return true;
	}

	public boolean ouch(MOB mob)
	{
		if(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_FIRE)-50))
		{
			if(affected instanceof Item)
			switch(((Item)affected).material()&EnvResource.MATERIAL_MASK)
			{
			case EnvResource.MATERIAL_LIQUID:
			case EnvResource.MATERIAL_METAL:
			case EnvResource.MATERIAL_MITHRIL:
			case EnvResource.MATERIAL_PRECIOUS:
			case EnvResource.MATERIAL_ENERGY:
			case EnvResource.MATERIAL_ROCK:
			case EnvResource.MATERIAL_UNKNOWN:
				mob.tell("Ouch!! "+Util.capitalizeAndLower(affected.name())+" is HOT!");
				break;
			default:
				mob.tell("Ouch!! "+Util.capitalizeAndLower(affected.name())+" is on fire!");
				break;
			}
			MUDFight.postDamage(invoker,mob,this,Dice.roll(1,5,5),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,null);
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
				switch(container.material()&EnvResource.MATERIAL_MASK)
				{
				case EnvResource.MATERIAL_METAL:
				case EnvResource.MATERIAL_MITHRIL:
				case EnvResource.MATERIAL_PRECIOUS:
				case EnvResource.MATERIAL_ENERGY:
				case EnvResource.MATERIAL_ROCK:
				case EnvResource.MATERIAL_UNKNOWN:
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
				msg.addTrailerMsg(new FullMsg(invoker,null,CMMsg.MSG_OK_VISUAL,I.name()+" is extinguished."));
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
			if((target instanceof Item)&&(((Item)target).material()==EnvResource.RESOURCE_NOTHING))
				return false;
			if((mob!=null)&&(mob.location()!=null))
			{
				FullMsg msg=new FullMsg(mob,target,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,null);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			beneficialAffect(mob,target,asLevel,profficiency());
			target.recoverEnvStats();
			if(target instanceof Item)
			{
				((Item)target).owner().recoverEnvStats();
				if(((Item)target).owner() instanceof Room)
					((Room)((Item)target).owner()).recoverRoomStats();
				else
				if(((Item)target).owner() instanceof MOB)
					((MOB)((Item)target).owner()).location().recoverRoomStats();
			}
		}
		return true;
	}
}
