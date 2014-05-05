package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Burning extends StdAbility
{
	@Override public String ID() { return "Burning"; }
	private final static String localizedName = CMLib.lang()._("Burning");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Burning)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override protected int canAffectCode(){return Ability.CAN_ITEMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public long flags(){return Ability.FLAG_HEATING|Ability.FLAG_FIREBASED;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		Physical affected=this.affected;
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
				final Room R=((Room)(((Item)affected).owner()));
				if(R.numInhabitants()>0)
					R.showHappens(CMMsg.MSG_OK_ACTION,_("The @x1 puts out @x2.",what,affected.name()));
				unInvoke();
				return false;
			}
		}
		if((tickDown<2)&&(affected!=null))
		{
			if(affected instanceof Item)
			{
				final Environmental E=((Item)affected).owner();
				if(E==null)
					((Item)affected).destroy();
				else
				if(E instanceof Room)
				{
					final Room room=(Room)E;
					if((affected instanceof RawMaterial)
					&&(room.isContent((Item)affected)))
					{
						for(int i=0;i<room.numItems();i++)
						{
							final Item I=room.getItem(i);
							if(I.name().equals(affected.name())
							&&(I!=affected)
							&&(I instanceof RawMaterial)
							&&(I.material()==((Item)affected).material()))
							{
								int durationOfBurn=CMLib.flags().burnStatus(I);
								if(durationOfBurn<=0) durationOfBurn=5;
								final Burning B=new Burning();
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
					case RawMaterial.MATERIAL_GAS:
					case RawMaterial.MATERIAL_PRECIOUS:
					case RawMaterial.MATERIAL_ROCK:
					case RawMaterial.MATERIAL_UNKNOWN:
						break;
					default:
					{
						if(CMLib.flags().isABonusItems(affected))
						{
							if(invoker==null)
							{
								invoker=CMClass.getMOB("StdMOB");
								invoker.setLocation(CMClass.getLocale("StdRoom"));
								invoker.basePhyStats().setLevel(affected.phyStats().level());
								invoker.phyStats().setLevel(affected.phyStats().level());
							}
							room.showHappens(CMMsg.MSG_OK_ACTION,_("@x1 EXPLODES!!!",affected.name()));
							for(int i=0;i<room.numInhabitants();i++)
							{
								final MOB target=room.fetchInhabitant(i);
								CMLib.combat().postDamage(invoker(),target,null,CMLib.dice().roll(affected.phyStats().level(),5,1),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The blast <DAMAGE> <T-NAME>!");
							}
							((Item)affected).destroy();
						}
						else
						{
							final Item ash=CMClass.getItem("GenResource");
							ash.setName("some ash");
							ash.setDisplayText("a small pile of ash is here");
							ash.setMaterial(RawMaterial.RESOURCE_ASH);
							ash.basePhyStats().setWeight(1);
							ash.recoverPhyStats();
							room.addItem(ash,ItemPossessor.Expire.Monster_EQ);
							((RawMaterial)ash).rebundle();
							if((affected instanceof RawMaterial)
							&&(affected.basePhyStats().weight()>1)
							&&(CMLib.flags().burnStatus(affected)>0))
							{
								affected.basePhyStats().setWeight(affected.basePhyStats().weight()-1);
								affected.recoverPhyStats();
								this.tickDown = CMLib.flags().burnStatus(affected);
								CMLib.materials().adjustResourceName((Item)affected);
								room.recoverRoomStats();
								return super.tick(ticking,tickID);
							}
							room.showHappens(CMMsg.MSG_OK_VISUAL, _("@x1 is no longer burning.",affected.name()));
							((Item)affected).destroy();
						}
						break;
					}
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

		affected=this.affected;
		if(affected==null)
			return false;

		if(affected instanceof Item)
		{
			final Item I=(Item)affected;
			final Environmental owner=I.owner();
			if((owner instanceof MOB)&&(((MOB)owner).location()!=null))
			{
				if(!ouch((MOB)owner))
					CMLib.commands().postDrop((MOB)owner,I,false,false,false);
			}
			if((I.subjectToWearAndTear())
			&&(I.usesRemaining()>1))
			{
				if(owner instanceof MOB)
					CMLib.combat().postItemDamage((MOB)owner, I, null, 1, CMMsg.TYP_FIRE, null);
				else
				if(owner instanceof Room)
				{
					final MOB M=CMLib.map().getFactoryMOB((Room)owner);
					CMLib.combat().postItemDamage(M, I, null, 1, CMMsg.TYP_FIRE, null);
					M.destroy();
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
			case RawMaterial.MATERIAL_GAS:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_UNKNOWN:
				mob.tell(_("Ouch!! @x1 is HOT!",CMStrings.capitalizeAndLower(affected.name())));
				break;
			default:
				mob.tell(_("Ouch!! @x1 is on fire!",CMStrings.capitalizeAndLower(affected.name())));
				break;
			}
			CMLib.combat().postDamage(invoker,mob,this,CMLib.dice().roll(1,5,5),CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,null);
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected!=null)
		&&(affected instanceof Item)
		&&(msg.amITarget(affected))
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL)))
		{
			if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
				return ouch(msg.source());
			// the "oven" exception
			final Item container=(Item)affected;
			final Item target=(Item)msg.tool();
			if((target.owner()==container.owner())
			&&(target.container()==container))
				switch(container.material()&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_PRECIOUS:
				case RawMaterial.MATERIAL_ENERGY:
				case RawMaterial.MATERIAL_GAS:
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

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(msg.tool()!=null)
		&&(msg.tool()==affected)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Container)
		&&(msg.targetMinor()==CMMsg.TYP_PUT))
		{
			final Item I=(Item)affected;
			final Item C=(Container)msg.target();
			if((C instanceof Drink)
			   &&(((Drink)C).containsDrink()))
			{
				msg.addTrailerMsg(CMClass.getMsg(invoker,null,CMMsg.MSG_OK_VISUAL,_("@x1 is extinguished.",I.name())));
				I.delEffect(this);
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
	}
	@Override
	public boolean invoke(MOB mob, Vector commands, Physical target, boolean auto, int asLevel)
	{
		if(!auto) return false;
		if(target==null) return false;
		if(target.fetchEffect("Burning")==null)
		{
			if(((target instanceof Item)&&(((Item)target).material()==RawMaterial.RESOURCE_NOTHING))
			||(target instanceof ClanItem))
				return false;
			if(mob!=null)
			{
				final Room room=mob.location();
				if(room!=null)
				{
					final CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,null);
					if(room.okMessage(mob,msg))
						room.send(mob,msg);
				}
			}
			if(asLevel == 0)
				asLevel = CMLib.flags().burnStatus(target);
			if(asLevel < 0) asLevel = 0;
			beneficialAffect(mob,target,0,asLevel);
			target.recoverPhyStats();
			if(target instanceof Item)
			{
				final ItemPossessor owner=((Item)target).owner();
				if(owner!=null)
				{
					owner.recoverPhyStats();
					if(owner instanceof Room)
						((Room)owner).recoverRoomStats();
					else
					if(owner instanceof MOB)
					{
						final MOB ownerM=(MOB)owner;
						final Room ownerRoom=ownerM.location();
						if(ownerRoom != null)
							ownerRoom.recoverRoomStats();
					}
				}
			}
		}
		return true;
	}
}
