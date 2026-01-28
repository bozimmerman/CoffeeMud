package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class Monger extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Monger";
	}

	private final static String localizedStaticName = CMLib.lang().L("Monger");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Commoner";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 2;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 20;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/6)+(1*(1?5))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/10)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_MONGER;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(final MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	public Monger()
	{
		super();
		maxStatAdj[CharStats.STAT_CHARISMA]=6;
		maxStatAdj[CharStats.STAT_CONSTITUTION]=6;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_ThrownWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Merchant",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SelfInvesting",100,true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SnakeOilSelling",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Barking",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Moneychanging",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Gossip Mongering",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Mongering",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Swindling",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Watching",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Appraise",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"MakeTradeContacts",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Wainwrighting",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Advertising",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"FindCaravan",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"FoodMarketeering",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Mongership2",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Wholesaling",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Orphanaging",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"MakeTradeMaps",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Door2Door",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Triage",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"InnKeeping",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"TradeCharting",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Wholebuying",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Mongership3",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Auctioneering",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Lore",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"CargoLoading",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"CaravanTravel",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"InsiderTrading",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Dealing",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"FencingLoot",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Rentaling",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"WordOfMouth",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Siegecrafting",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Mongership4",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Exporting",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Incorporating",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"FreeMarketing",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Dealership2",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"TradeDisrupting",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"MailCarrying",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"TradeEmbargo",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Mongership5",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"CaravanBuilding",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Bankrupting",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Branding",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Dealership3",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"CaravanTactics",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"LandLording",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Monopolizing",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Mongership6",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"IllicitTrading",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Sellout",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Importing",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Financiering",true);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Dealership4",false);
//		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"SalesClerkHiring",true);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Consitution",Integer.valueOf(5)),
		new Pair<String,Integer>("Charisma",Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Club");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Bonus XP for discovering shops and stores.  "
				+"Investments pay monthly XP per share."); //TODO:BZ:NOT DONE YET!
	}


	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(mob.isPlayer() && mob.charStats().getCurrentClass()==this)
			{
				final Map<String,Object> map = mob.playerStats().getClassVariableMap(this);
				if(!map.containsKey("SAVE"))
					map.put("SAVE", Boolean.TRUE);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host, msg);
	}

	public static void visitationBonusMessage(final Environmental host, final CMMsg msg)
	{
		if((msg.target() instanceof Room)
		&&(msg.source()==host)
		&&(!msg.source().isMonster())
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.source().playerStats()!=null))
		{
			final Room R=(Room)msg.target();
			if(((R.roomID().length()>0)
				||((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0)))
			&&(!CMath.bset(R.getArea().flags(),Area.FLAG_INSTANCE_CHILD))
			&&(!msg.source().playerStats().hasVisited(R))
			)
			{
				MOB M=null;
				boolean shop=false;
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M instanceof ShopKeeper)
					&&(M.getStartRoom()==R))
						shop=true;
				}
				final CharClass C=((MOB)host).charStats().getCurrentClass();
				if(shop)
				{
					int xpGain=25;
					if(((xpGain=CMLib.leveler().postExperience((MOB)host,"CLASS:"+C.ID(),null,null,xpGain, true))>0)
					&&(!CMSecurity.isDisabled(DisFlag.SHOWXPGAINS)))
					{
						msg.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,
								CMLib.lang().L("^HYou have discovered a new shop, you gain @x1 experience.^?",
										CMLib.leveler().getXPAmountTerm(xpGain)),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
				}
			}
		}
	}
}
