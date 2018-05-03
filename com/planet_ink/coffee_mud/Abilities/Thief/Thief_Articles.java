package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.StdBehavior;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_Articles extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Articles";
	}

	private final static String	localizedName	= CMLib.lang().L("Articles");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ARTICLES"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	protected int abilityCode = 0;
	
	@Override
	public int abilityCode()
	{
		return abilityCode;
	}
	
	@Override
	public void setAbilityCode(int newCode)
	{
		this.abilityCode = newCode;
	}
	
	@Override
	public CMObject copyOf()
	{
		Thief_Articles A=(Thief_Articles)super.copyOf();
		A.sailor=null;
		return A;
	}
	
	private enum CrewType
	{
		GUNNER,
		DEFENDER,
		BOARDER
	}
	
	protected String	shipName	= "";
	protected CrewType	type		= CrewType.GUNNER;
	protected Behavior	sailor		= null;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		int x=newMiscText.indexOf(';');
		if(x>0)
		{
			shipName=newMiscText.substring(0,x);
			type=(CrewType)CMath.s_valueOf(CrewType.class, newMiscText.substring(x+1));
		}
	}
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		switch(type)
		{
		case GUNNER:
			affectableStats.addAmbiance(L("Siege Op"));
			break;
		case DEFENDER:
			affectableStats.addAmbiance(L("Defender"));
			break;
		case BOARDER:
			affectableStats.addAmbiance(L("Boarder"));
			break;
		}
	}
	
	public Behavior getSailor()
	{
		if(affected instanceof PhysicalAgent)
		{
			PhysicalAgent agent=(PhysicalAgent)affected;
			if((sailor == null)||(agent.fetchBehavior("Sailor")!=sailor))
			{
				final Behavior B=agent.fetchBehavior("Sailor");
				if(B!=null)
					agent.delBehavior(B);
				sailor = CMClass.getBehavior("Sailor");
				switch(type)
				{
				case GUNNER:
					sailor.setParms("FIGHTTECH=true FIGHTMOVER=false TICKBONUS="+abilityCode());
					break;
				case DEFENDER:
					sailor.setParms("DEFENDER=true FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				case BOARDER:
					sailor.setParms("BOARDER=true FIGHTMOVER=false FIGHTTECH=false TICKBONUS="+abilityCode());
					break;
				}
				sailor.setSavable(false);
				((PhysicalAgent)affected).addBehavior(sailor);
			}
		}
		return sailor;
	}
	
	@Override
	public void unInvoke()
	{
		Physical affected=this.affected;
		if(affected instanceof PhysicalAgent)
		{
			PhysicalAgent agent=(PhysicalAgent)affected;
			final Behavior B=agent.fetchBehavior("Sailor");
			if(B!=null)
				agent.delBehavior(B);
		}
		super.unInvoke();
	}
	
	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting, msg))
			return false;
		return true;
	}
	
	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting, msg);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		getSailor();
		return true;
	}
	
	protected boolean isCrew(final MOB M, final String shipName)
	{
		Thief_Articles articlesA=(Thief_Articles)M.fetchEffect(ID());
		return ((articlesA!=null)&&(articlesA.shipName.equals(shipName)));
	}
	
	protected String getCrewShip(final MOB M)
	{
		Thief_Articles articlesA=(Thief_Articles)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.shipName : "";
	}
	
	protected CrewType getCrewType(final MOB M)
	{
		Thief_Articles articlesA=(Thief_Articles)M.fetchEffect(ID());
		return (articlesA!=null) ? articlesA.type : null;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if(!(R.getArea() instanceof BoardableShip))
		{
			mob.tell(L("You must be on a sailing ship."));
			return false;
		}
		final BoardableShip myShip=(BoardableShip)R.getArea();
		final Item myShipItem=myShip.getShipItem();
		final Area myShipArea=myShip.getShipArea();
		if((myShipItem==null)
		||(myShipArea==null)
		||(!(myShipItem.owner() instanceof Room)))
		{
			mob.tell(L("You must be on your sailing ship."));
			return false;
		}
		
		if((!CMLib.law().doesHavePriviledgesHere(mob, R))
		&&(!CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDMOBS)))
		{
			mob.tell(L("You must be on the deck of a ship that you have privileges on."));
			return false;
		}
		
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		
		if((!target.isMonster())
		||(CMLib.flags().isAnimalIntelligence(target))
		||((target.getStartRoom()==null)&&(target.fetchEffect(ID())==null)))
		{
			mob.tell(L("You can't offer the articles to @x1.",target.name(mob)));
			return false;
		}
		
		if(isCrew(target,myShipItem.Name()))
		{
			mob.tell(L("@x1 has already signed the articles.",target.name(mob)));
			return false;
		}
		
		boolean allowedToOfferToThem=false;
		if(CMLib.flags().isBoundOrHeld(target))
		{
			allowedToOfferToThem=true;
		}

		if(mob.getGroupMembers(new HashSet<MOB>()).contains(target))
		{
			allowedToOfferToThem=true;
		}

		if(!allowedToOfferToThem)
		{
			mob.tell(L("You can't offer @x1 the articles.",target.name()));
			return false;
		}
		
		int numRooms=0;
		int numCrew=0;
		int numDecks=0;
		int[] numTypes=new int[CrewType.values().length];
		for(Enumeration<Room> r=myShipArea.getProperMap();r.hasMoreElements();)
		{
			final Room R2=r.nextElement();
			switch(R2.domainType())
			{
			case Room.DOMAIN_INDOORS_AIR:
			case Room.DOMAIN_OUTDOORS_AIR:
				break;
			default:
				if(((R2.domainType()&Room.INDOORS)!=0))
					numDecks++;
				numRooms++;
				break;
			}
			for(Enumeration<MOB> m=R2.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if((M!=null)
				&&(M.isMonster())
				&&(isCrew(M,myShipItem.Name())))
				{
					numCrew++;
					numTypes[getCrewType(M).ordinal()]++;
				}
			}
		}
		
		int bonus= ( adjustedLevel(mob,asLevel) / 10);
		if(bonus > 0)
		{
			int bonusDecks = bonus / 2;
			numRooms += bonus;
			numDecks += bonusDecks;
		}
		
		CrewType nextType = null;
		final int maxGunners = numDecks;
		int maxBoarders = (numRooms-numDecks)/2;
		if(maxBoarders<1)
			maxBoarders=1;
		int maxDefenders = (numRooms-numDecks - maxBoarders);
		if(maxDefenders<1)
			maxDefenders=1;
		
		if(numCrew >= (maxGunners + maxBoarders + maxDefenders))
		{
			mob.tell(L("This ship already has the maximum crew."));
			return false;
		}
		
		int attempts=10000;
		while((nextType == null)&&(--attempts>0))
		{
			nextType = CrewType.values()[CMLib.dice().roll(1, CrewType.values().length, -1)];
			if(numTypes[nextType.ordinal()]>0)
			{
				switch(nextType)
				{
				case GUNNER:
					if(numTypes[nextType.ordinal()]>=numDecks)
						nextType=null;
					break;
				case DEFENDER:
					if(numTypes[nextType.ordinal()]>=maxDefenders)
						nextType=null;
					break;
				case BOARDER:
					if(numTypes[nextType.ordinal()]>=maxBoarders)
						nextType=null;
					break;
				}
			}
		}
		
		if(nextType == null)
		{
			mob.tell(L("This ship already has enough crew."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=target.phyStats().level()-((mob.phyStats().level()+super.getXLEVELLevel(mob))/2);
		boolean success=proficiencyCheck(mob,-adjustment,auto);
		if(success)
		{
			String str=auto?"":L("^S<S-NAME> offer(s) <T-NAME> the articles of piracy..^?");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,CMMsg.MSG_THIEF_ACT|(auto?CMMsg.MASK_ALWAYS:0),str,CMMsg.MSG_NOISYMOVEMENT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				if(msg.value()<=0)
				{
					R.show(target, null, CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> sign(s) the articles of piracy and is now a member of the crew of @x1.",myShipItem.name()));
					Ability A=target.fetchEffect(ID());
					if(A!=null)
						target.delEffect(A);
					A=(Ability)copyOf();
					A.setMiscText(myShipItem.Name()+";"+nextType.name());
					A.setAbilityCode(super.getXLEVELLevel(mob));
					target.addNonUninvokableEffect(A);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> offer(s) <T-NAME> the pirate articles, but <T-HIM-HER> isn't convinced."));

		// return whether it worked
		return success;
	}
}
