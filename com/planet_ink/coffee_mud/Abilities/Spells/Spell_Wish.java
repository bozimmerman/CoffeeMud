package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.LegalLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_Wish extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Wish";
	}

	private final static String localizedName = CMLib.lang().L("Wish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	protected Physical maybeAdd(MOB mob, Physical E, Vector<Physical> foundAll, Physical foundThang)
	{
		final Room R=CMLib.map().roomLocation(E);
		if((E!=null)
		&&(!(E instanceof ArchonOnly))
		&&(!(E instanceof ClanItem))
		&&((R==null)||(CMLib.law().getLandTitle(R)==null)||(CMLib.law().doesHavePriviledgesHere(mob, R)))
		&&((foundThang==null)
		   ||((foundThang.ID().equals(E.ID()))&&(foundThang.name().equals(E.name())))))
		{
			if(foundThang==null)
				foundThang=E;
			foundAll.addElement(E);
		}
		return foundThang;
	}

	private void bringThangHere(MOB mob, Room here, Physical target)
	{
		if(target instanceof MOB)
		{
			mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> teleport(s) to @x1.",here.displayText()));
			here.bringMobHere((MOB)target,false);
			if(here.isInhabitant((MOB)target))
				here.show((MOB)target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> appear(s) out of nowhere."));
		}
		else
		if(target instanceof Item)
		{
			final Item item=(Item)target;
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> is teleported to @x1!",here.displayText()));
			item.unWear();
			item.setContainer(null);
			item.removeFromOwnerContainer();
			here.addItem(item,ItemPossessor.Expire.Player_Drop);
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> appears out of the Java Plane!"));
		}
	}

	public void wishDrain(MOB mob, int expLoss, boolean conLoss)
	{
		if(mob==null)
			return;
		expLoss=getXPCOSTAdjustment(mob,expLoss);
		if(expLoss > mob.getExperience())
			expLoss=mob.getExperience();
		CMLib.leveler().postExperience(mob,null,null,-expLoss,false);
		if(conLoss)
		{
			mob.tell(L("Your wish drains you of @x1 experience points and a point of constitution.",""+(expLoss)));
			mob.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)-1);
			mob.baseCharStats().setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,mob.baseCharStats().getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)-1);
			mob.recoverCharStats();
			if(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<1)
				CMLib.combat().postDeath(mob,mob,null);
		}
		else
			mob.tell(L("Your wish drains @x1 experience points.",""+(expLoss)));
	}

	public void age(MOB mob)
	{
		final Ability A=CMClass.getAbility("Chant_SpeedAging");
		if (A != null)
		{
			A.setAbilityCode(65536);
			A.invoke(mob, mob, true, 0);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> sigh(s)."));
			CMLib.commands().postSay(mob,null,L("My wishes never seem to come true."),false,false);
			return false;
		}

		String myWish=CMParms.combine(commands,0);
		if(((!auto)&&(mob.phyStats().level()<20))||(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<2))
		{
			mob.tell(L("You are too weak to wish."));
			return false;
		}
		if(myWish.toUpperCase().trim().startsWith("FOR "))
			myWish=myWish.trim().substring(3);
		if(myWish.length()==0)
		{
			mob.tell(L("What would you like to wish for?"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int baseLoss=25;
		final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L("^S<S-NAME> wish(es) for '@x1'!!^?",myWish));
		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			baseLoss=getXPCOSTAdjustment(mob,baseLoss);
			CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
			beneficialWordsFizzle(mob,null,L("<S-NAME> wish(es) for '@x1', but the spell fizzles.",myWish));
			return false;
		}
		else
		if(mob.location().okMessage(mob,msg))
		{
			// cast wish bless were cast on me
			// cast wish to have restoration cast on me
			// cast wish to cast bless on me
			// cast wish to cast disintegrate on orc
			// cast wish to cast geas on orc to kill bob
			Log.sysOut("Wish",mob.Name()+" wished for "+myWish+".");

			mob.location().send(mob,msg);
			final StringBuffer wish=new StringBuffer(myWish);
			for(int i=0;i<wish.length();i++)
			{
				if(!Character.isLetterOrDigit(wish.charAt(i)))
					wish.setCharAt(i,' ');
			}
			myWish=wish.toString().trim().toUpperCase();
			final Vector<String> wishV=CMParms.parse(myWish);
			myWish=" "+myWish+" ";
			if(wishV.size()==0)
			{
				baseLoss=getXPCOSTAdjustment(mob,baseLoss);
				CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
				beneficialWordsFizzle(mob,null,L("<S-NAME> make(s) a wish comes true! Nothing happens!"));
				return false;
			}

			// do locate object first.. its the most likely
			String objectWish=myWish;
			final String[] redundantStarts={
				"CREATE",
				"TO CREATE",
				"ANOTHER",
				"THERE WAS",
				"I HAD",
				"I COULD HAVE",
				"MAY I HAVE",
				"CAN I HAVE",
				"CAN YOU",
				"CAN I",
				"MAKE",
				"TO MAKE",
				"GIVE",
				"ME",
				"TO HAVE",
				"TO GET",
				"A NEW",
				"SOME MORE",
				"MY OWN",
				"A",
				"PLEASE",
				"THE",
				"I OWNED"
			};
			final String[] redundantEnds=
			{
				"TO APPEAR",
				"OF MY OWN",
				"FOR ME",
				"BE",
				"CREATED",
				"PLEASE",
				"HERE"
			};
			int i=0;
			while(i<redundantStarts.length)
			{
				if(objectWish.startsWith(" "+redundantStarts[i]+" "))
				{
					objectWish = objectWish.substring(1 + redundantStarts[i].length());
					i = -1;
				}
				i++;
			}
			i=0;
			while(i<redundantEnds.length)
			{
				if(objectWish.endsWith(" "+redundantEnds[i]+" "))
				{
					objectWish = objectWish.substring(0, objectWish.length() - (1 + redundantEnds[i].length()));
					i = -1;
				}
				i++;
			}
			String goldWish=objectWish.toUpperCase();
			objectWish=objectWish.toLowerCase().trim();

			final String[] redundantGoldStarts=
			{
				"A PILE OF",
				"A STACK OF",
				"PILE OF",
				"STACK OF"
			};
			i=0;
			while(i<redundantGoldStarts.length)
			{
				if(goldWish.startsWith(" "+redundantGoldStarts[i]+" "))
				{
					goldWish = goldWish.substring(1 + redundantGoldStarts[i].length());
					i = -1;
				}
				i++;
			}
			final Vector<String> goldCheck=CMParms.parse(goldWish.trim().toLowerCase());
			if((goldCheck.size()>1)
			&&(CMath.isNumber(goldCheck.firstElement()))
			&&(CMath.s_int(goldCheck.firstElement())>0)
			&&(CMLib.english().matchAnyCurrencySet(CMParms.combine(goldCheck,1))!=null))
			{
				final Coins newItem=(Coins)CMClass.getItem("StdCoins");
				newItem.setCurrency(CMLib.english().matchAnyCurrencySet(CMParms.combine(goldCheck,1)));
				newItem.setDenomination(CMLib.english().matchAnyDenomination(newItem.getCurrency(),CMParms.combine(goldCheck,1)));
				final long goldCoins=CMath.s_long(goldCheck.firstElement());
				newItem.setNumberOfCoins(goldCoins);
				int experienceRequired=Math.max((int)Math.round(CMath.div(newItem.getTotalValue(),10.0)),0);
				while((experienceRequired > mob.getExperience())
				&& (experienceRequired > 0)
				&& (newItem.getNumberOfCoins() > 1))
				{
					final int difference=experienceRequired-mob.getExperience();
					final double diffPct=CMath.div(difference, experienceRequired);
					long numCoinsToLose=Math.round(CMath.mul(diffPct, newItem.getNumberOfCoins()));
					if(numCoinsToLose<1)
						numCoinsToLose=1;
					newItem.setNumberOfCoins(newItem.getNumberOfCoins()-numCoinsToLose);
					experienceRequired=Math.max((int)Math.round(CMath.div(newItem.getTotalValue(),10.0)),0);
				}
				newItem.setContainer(null);
				newItem.wearAt(0);
				newItem.recoverPhyStats();
				mob.location().addItem(newItem,ItemPossessor.Expire.Player_Drop);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 drops from the sky.",newItem.name()));
				mob.location().recoverRoomStats();
				wishDrain(mob,(baseLoss+experienceRequired),false);
				return true;
			}

			final Vector<Physical> thangsFound=new Vector<Physical>();
			Physical foundThang=null;
			final Physical P=mob.location().fetchFromRoomFavorItems(null,objectWish);
			foundThang=maybeAdd(mob,P,thangsFound,foundThang);
			try
			{
				final List<Environmental> items=new LinkedList<Environmental>();
				items.addAll(CMLib.map().findRoomItems(CMLib.map().rooms(), mob,objectWish,true,10));
				items.addAll(CMLib.map().findInhabitants(CMLib.map().rooms(), mob,objectWish,10));
				items.addAll(CMLib.map().findInventory(CMLib.map().rooms(), mob,objectWish,10));
				items.addAll(CMLib.map().findShopStock(CMLib.map().rooms(), mob,objectWish,10));
				for(final Environmental O : items)
				{
					if(O instanceof Physical)
						foundThang=maybeAdd(mob,((Physical)O),thangsFound,foundThang);
				}
				if(objectWish.equals("LAND")
				||objectWish.equals("A HOUSE")
				||objectWish.equals("HOUSE")
				||objectWish.equals("A HOME")
				||objectWish.equals("HOME")
				||objectWish.equals("REAL ESTATE")
				||objectWish.equals("SOME REAL ESTATE")
				||objectWish.equals("PROPERTY")
				||objectWish.equals("SOME PROPERTY")
				||objectWish.equals("SOME LAND"))
				{
					final LegalLibrary law=CMLib.law();
					for(Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if((R!=null)&&(!(R.getArea() instanceof BoardableShip)))
						{
							final LandTitle title = law.getLandTitle(R);
							if((title!=null)
							&&(title.getOwnerName().length()==0))
							{
								LandTitle T=(LandTitle)CMClass.getItem("GenTitle");
								T.setLandPropertyID(title.landPropertyID());
								T.text();
								items.add(T);
							}
						}
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}

			if(foundThang instanceof PackagedItems)
				foundThang = ((PackagedItems)foundThang).peekFirstItem();

			if((thangsFound.size()>0)&&(foundThang!=null))
			{
				// yea, we get to DO something!
				int experienceRequired=100*(foundThang.phyStats().level()-1);
				if(foundThang instanceof MOB)
				{
					final MOB foundMOB=(MOB)foundThang;
					MOB newMOB;
					final boolean isPlayer=foundMOB.playerStats()!=null;
					if(isPlayer && (!foundMOB.isMonster()) && CMLib.flags().isInTheGame(foundMOB, true))
					{
						newMOB=foundMOB;
						mob.location().bringMobHere(newMOB, false);
					}
					else
					{
						if(isPlayer)
						{
							newMOB=CMClass.getMOB("GenMOB");
							newMOB.setName(L("CopyOf@x1",foundThang.Name()));
							newMOB.setDisplayText(((MOB) foundThang).displayText(mob));
							newMOB.setDescription(foundThang.description());
						}
						else
							newMOB=(MOB)foundMOB.copyOf();
						newMOB.setStartRoom(null);
						newMOB.setLocation(mob.location());
						newMOB.recoverCharStats();
						newMOB.recoverPhyStats();
						newMOB.recoverMaxState();
						newMOB.resetToMaxState();
						newMOB.bringToLife(mob.location(),true);
					}
					newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 instantiates from the Java Plane.",newMOB.name()));
					newMOB.setFollowing(mob);
					if(experienceRequired<=0)
						experienceRequired=0;
					wishDrain(mob,(baseLoss+experienceRequired),false);
					return true;
				}
				else
				if((foundThang instanceof Item)
				&&(!(foundThang instanceof ArchonOnly))
				&&(!(foundThang instanceof ClanItem))
				&&(!CMath.bset(foundThang.phyStats().sensesMask(), PhyStats.SENSE_ITEMNOWISH)))
				{
					final Item newItem=(Item)foundThang.copyOf();
					experienceRequired+=newItem.value();
					if(newItem instanceof LandTitle)
						((LandTitle)newItem).setOwnerName(mob.Name());
					if(experienceRequired>mob.getExpPrevLevel())
						experienceRequired=mob.getExpPrevLevel();
					newItem.setContainer(null);
					newItem.wearAt(0);
					mob.location().addItem(newItem,ItemPossessor.Expire.Player_Drop);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 drops from the sky.",newItem.name()));
					mob.location().recoverRoomStats();
					if(experienceRequired<=0)
						experienceRequired=0;
					wishDrain(mob,(baseLoss+experienceRequired),false);
					return true;
				}
			}

			boolean onFlag=false;
			// anything else may refer to another person or item
			Physical target=null;
			String possName=wishV.elementAt(0).trim();
			if(wishV.size()>2)
			{
				possName=CMParms.combine(wishV,0,2);
				target=mob.location().fetchFromRoomFavorMOBs(null,possName);
				if(target==null)
					target=mob.findItem(possName);
				if(target==null)
					possName=wishV.elementAt(0).trim();
			}
			if(target==null)
				target=mob.location().fetchFromRoomFavorMOBs(null,possName);
			if(target==null)
				target=mob.findItem(possName);
			if((target==null)
			||(possName.equalsIgnoreCase("FOR"))
			||(possName.equalsIgnoreCase("TO"))
			||(possName.equalsIgnoreCase("BE"))
			||(possName.equalsIgnoreCase("WOULD"))
			||(possName.equalsIgnoreCase("A"))
			||(possName.equalsIgnoreCase("THE"))
			||(possName.equalsIgnoreCase("AN"))
			||(possName.equalsIgnoreCase("I")))
			{
				if(possName.equalsIgnoreCase("I"))
				{
					wishV.removeElementAt(0);
					myWish=" "+CMParms.combine(wishV,0).toUpperCase()+" ";
				}
				if((wishV.size()>3)&&(wishV.get(wishV.size()-2).equals("ON")))
				{
					possName=wishV.get(wishV.size()-1);
					if(possName.equals("SELF")
					||possName.equals("ME")
					||possName.equals("MYSELF"))
						target=mob;
					if(target==null)
						target=mob.location().fetchFromRoomFavorMOBs(null,possName);
					if(target==null)
						target=mob.findItem(possName);
					if(target!=null)
					{
						wishV.remove(wishV.size()-1);
						wishV.remove(wishV.size()-1);
						onFlag=true;
						myWish=" "+CMParms.combine(wishV,0).toUpperCase().trim()+" ";
					}
				}
				if(target==null)
					target=mob;
			}
			else
			{
				wishV.removeElementAt(0);
				myWish=" "+CMParms.combine(wishV,0).toUpperCase().trim()+" ";
			}
			
			if(target instanceof PackagedItems)
				target = ((PackagedItems)target).peekFirstItem();

			if((target instanceof ArchonOnly)
			||(target instanceof ClanItem))
				target=null;

			if((target!=null)
			&&(target!=mob)
			&&(target instanceof MOB)
			&&(!((MOB)target).isMonster())
			&&(!mob.mayIFight((MOB)target)))
			{
				mob.tell(L("You cannot cast wish on @x1 until @x2 permits you. You must both toggle your playerkill flags on.",target.name(mob),mob.charStats().heshe()));
				return false;
			}

			// a wish for recall
			if((myWish.startsWith(" TO BE RECALLED "))
			||(myWish.startsWith(" TO RECALL "))
			||(myWish.startsWith(" RECALL "))
			||(myWish.startsWith(" BE RECALLED "))
			||(myWish.startsWith(" WAS RECALLED "))
			||(myWish.startsWith(" WOULD RECALL "))
			||(myWish.endsWith(" WAS RECALLED "))
			||(myWish.endsWith(" WOULD RECALL "))
			||(myWish.endsWith(" TO RECALL "))
			||(myWish.endsWith(" BE RECALLED "))
			||(myWish.endsWith(" RECALL ")&&(!myWish.endsWith(" OF RECALL "))))
			{
				Room recallRoom=mob.getStartRoom();
				if((recallRoom==null)&&(target instanceof MOB)&&(((MOB)target).getStartRoom()!=null))
					recallRoom=((MOB)target).getStartRoom();
				if(recallRoom!=null)
				{
					wishDrain(mob,baseLoss,false);
					bringThangHere(mob,recallRoom,target);
					return true;
				}
			}

			// a wish for death or destruction
			if((myWish.startsWith(" TO DIE "))
			||(myWish.startsWith(" TO BE DESTROYED "))
			||(myWish.startsWith(" TO CROAK "))
			||(myWish.startsWith(" WAS DEAD "))
			||(myWish.startsWith(" WAS GONE "))
			||(myWish.startsWith(" WOULD GO AWAY "))
			||(myWish.startsWith(" WAS BANISHED "))
			||(myWish.startsWith(" WOULD DIE "))
			||(myWish.startsWith(" WOULD BE DEAD "))
			||(myWish.startsWith(" WAS DESTROYED "))
			||(myWish.startsWith(" DEATH "))
			||(myWish.startsWith(" FOR DEATH "))
			||(myWish.startsWith(" DESTRUCTION "))
			||(myWish.startsWith(" TO BE BANISHED "))
			||(myWish.startsWith(" TO BE DEAD "))
			||(myWish.startsWith(" TO BE GONE "))
			||(myWish.startsWith(" TO DISAPPEAR "))
			||(myWish.startsWith(" TO VANISH "))
			||(myWish.startsWith(" TO BE INVISIBLE "))
			||(myWish.startsWith(" TO GO AWAY "))
			||(myWish.startsWith(" TO GO TO HELL ")))
			{
				if(target instanceof Item)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 quietly vanishes.",target.name()));
					((Item)target).destroy();
				}
				else
				if(target instanceof MOB)
				{
					final int exp=mob.getExperience();
					CMLib.combat().postDeath(mob,(MOB)target,null);
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
					&&!mob.charStats().getCurrentClass().expless()
					&&!mob.charStats().getMyRace().expless()
					&&(mob.getExperience()>exp))
						baseLoss=mob.getExperience()-exp;
				}
				wishDrain(mob,baseLoss*2,false);
				return true;
			}

			// a wish for movement
			String locationWish=myWish;
			final String[] redundantStarts2={
					"TO GO TO",
				  "TO TELEPORT TO",
				  "TO TRANSPORT TO",
				  "TO TRANSFER TO",
				  "TO PORTAL TO",
				  "WOULD TELEPORT TO",
				  "WOULD TRANSPORT TO",
				  "WOULD TRANSFER TO",
				  "WOULD PORTAL TO",
				  "WOULD GO TO",
				  "TO PORTAL TO",
				  "TO BE TELEPORTED TO",
				  "TO BE TRANSPORTED TO",
				  "TO BE TRANSFERRED TO",
				  "TO BE PORTALLED TO",
				  "TO BE PORTALED TO",
				  "TO BE TELEPORTED",
				  "TO BE TRANSPORTED",
				  "TO BE TRANSFERRED",
				  "TO BE PORTALLED",
				  "TO BE PORTALED",
				  "TO APPEAR IN ",
				  "TO BE IN",
				  "TO APPEAR AT",
				  "TO BE AT",
				  "TO GO",
				  "TO MOVE TO",
				  "TO MOVE",
				  "TO BE AT",
				  "TO BE IN",
				  "TO BE",
				  "TO TRAVEL",
				  "TO WALK TO",
				  "TO WALK",
				  "TO TRAVEL TO",
				  "TO GOTO",
				  "TELEPORTATION TO",
				  "TRANSPORTED TO",
				  "TELEPORTED TO",
				  "TRANSFERRED TO",
				  "WAS TRANSPORTED TO",
				  "WAS TELEPORTED TO",
				  "WAS TRANSFERRED TO",
				  "TELEPORT",
				  "GO",
				  "GO TO",
				  "GOTO",
				  "TRANSFER",
				  "PORTAL",
				  "TELEPORTATION"};
			final String[] redundantEnds2={"IMMEDIATELY","PLEASE","NOW","AT ONCE"};
			boolean validStart=false;
			i=0;
			while(i<redundantStarts2.length)
			{
				if(locationWish.startsWith(" "+redundantStarts2[i]+" "))
				{
					validStart=true;
					locationWish=locationWish.substring(1+redundantStarts2[i].length());
					i=-1;
				}
				i++;
			}
			i=0;
			while(i<redundantEnds2.length)
			{
				if(locationWish.endsWith(" "+redundantEnds2[i]+" "))
				{
					locationWish=locationWish.substring(0,locationWish.length()-(1+redundantEnds2[i].length()));
					i=-1;
				}
				i++;
			}

			// a wish for teleportation
			if(validStart)
			{
				Room newRoom=null;
				final int dir=CMLib.directions().getGoodDirectionCode(wishV.lastElement());
				if(dir>=0)
					newRoom=mob.location().getRoomInDir(dir);
				if(newRoom==null)
				{
					try
					{
						final List<Room> rooms=CMLib.map().findRooms(CMLib.map().rooms(), mob, locationWish.trim(), true, 10);
						if(rooms.size()>0)
							newRoom=rooms.get(CMLib.dice().roll(1,rooms.size(),-1));
					}
					catch (final NoSuchElementException nse)
					{
					}
				}
				if(newRoom!=null)
				{
					bringThangHere(mob,newRoom,target);
					newRoom.show(mob, null, CMMsg.MSG_OK_VISUAL, L("<S-NAME> appears!"));
					wishDrain(mob,baseLoss,false);
					return true;
				}
			}

			if((target instanceof MOB)
			&&((myWish.indexOf(" END PREGNANCY ")>=0)
				||(myWish.indexOf(" FINISH PREGNANCY ")>=0)
				||(myWish.indexOf(" ABORT PREGNANCY ")>=0)
				||(myWish.indexOf(" ABORTION ")>=0)))
			{
				Ability A=target.fetchEffect("Pregnancy");
				if(A != null)
				{
					A.unInvoke();
					target.delEffect(A);
					A.setAffectedOne(null);
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 loses @x2 baby!",target.name(),((MOB)target).charStats().hisher()));
					wishDrain(mob,baseLoss,false);
					return true;
				}
			}

			if((target instanceof MOB)
			&&((myWish.indexOf(" GIVE BIRTH ")>=0)
				||(myWish.indexOf(" COME TO TERM ")>=0)
				||(myWish.indexOf(" HAVE BABY ")>=0)
				||(myWish.indexOf(" HAVE HER BABY ")>=0)
				||(myWish.indexOf(" COME TO FULL TERM ")>=0)))
			{
				Ability A=target.fetchEffect("Pregnancy");
				if(A != null)
				{
					long pregStart = CMath.s_long(A.getStat("PREGSTART"));
					long pregEnd = CMath.s_long(A.getStat("PREGEND"));
					if((pregStart>=0)&&(pregEnd>=0))
					{
						long diff = pregEnd - System.currentTimeMillis() + 10000;
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 start(s) going into labor!",target.name()));
						A.setStat("PREGSTART", ""+(pregStart - diff));
						A.setStat("PREGEND", ""+(pregEnd - diff));
						wishDrain(mob,baseLoss,false);
						return true;
					}
				}
				
			}

			if(((target instanceof MOB)
				||(target instanceof CagedAnimal))
			&&(target != null)
			&&((myWish.indexOf(" GROW UP ")>=0)
				||(myWish.indexOf(" BE OLDER ")>=0)
				||(myWish.indexOf(" GROW OLDER ")>=0)))
			{
				final Ability A=target.fetchEffect("Age");
				if((target instanceof MOB)
				&&((A==null)||(A.displayText().length()==0)))
				{
					final MOB M=(MOB)target;
					mob.location().show(M,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> age(s) a bit."));
					int ageCat = M.baseCharStats().ageCategory();
					if(ageCat < Race.AGE_ANCIENT)
					{
						while((M.baseCharStats().ageCategory() == ageCat)
						||(M.baseCharStats().ageCategory() < Race.AGE_YOUNGADULT))
						{
							M.baseCharStats().setStat(CharStats.STAT_AGE, M.baseCharStats().getStat(CharStats.STAT_AGE)+1);
							if(M.playerStats()!=null)
								M.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR]--;
							CMLib.commands().tickAging(M,60000);
							M.recoverCharStats();
						}
						wishDrain(mob,baseLoss,false);
						return true;
					}
				}
				else
				{
					long start=CMath.s_long(A.text());
					long age=System.currentTimeMillis()-start;
					final long millisPerMudday=CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)*CMProps.getTickMillis();
					if(age<millisPerMudday)
						age=millisPerMudday;
					final long millisPerMonth=CMLib.time().globalClock().getDaysInMonth() * millisPerMudday;
					final long millisPerYear=CMLib.time().globalClock().getMonthsInYear() * millisPerMonth;
					int ageYears = (int)(age / millisPerYear);
					final CharStats stats = (CharStats)CMClass.getCommon("DefaultCharStats");
					stats.setStat(CharStats.STAT_AGE,ageYears);
					int ageCat = stats.ageCategory();
					if(ageCat < Race.AGE_ANCIENT)
					{
						while((stats.ageCategory() == ageCat)
						||(stats.ageCategory() < Race.AGE_YOUNGADULT))
						{
							stats.setStat(CharStats.STAT_AGE, stats.getStat(CharStats.STAT_AGE)+1);
							start -= millisPerYear;
							A.setMiscText(""+start);
							target.recoverPhyStats();
							if(target instanceof MOB)
								((MOB)target).recoverCharStats();
							if(target instanceof MOB)
								((MOB)target).recoverPhyStats();
						}
						if(target instanceof MOB)
							mob.location().show((MOB)target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> age(s) a bit."));
						else
							mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 ages a bit.",target.name()));
						target.recoverPhyStats();
						wishDrain(mob,baseLoss,false);
						return true;
					}
				}
				
			}

			// temporary stat changes
			if((target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)))
			{
				final MOB tm=(MOB)target;
				if((myWish.indexOf("HIT POINT")>=0)&&(tm.curState().getHitPoints()<tm.maxState().getHitPoints()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is healthier!",target.name()));
					tm.curState().setHitPoints(tm.maxState().getHitPoints());
					wishDrain(mob,baseLoss,false);
					return true;
				}
				else
				if(myWish.indexOf("HIT POINT")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is healthier!",target.name()));
					tm.baseState().setHitPoints(tm.baseState().getHitPoints()+2);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}
				if((myWish.indexOf("MANA")>=0)&&(tm.curState().getMana()<tm.maxState().getMana()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 has more mana!",target.name()));
					tm.curState().setMana(tm.maxState().getMana());
					wishDrain(mob,baseLoss,false);
					return true;
				}
				else
				if(myWish.indexOf("MANA")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 has more mana!",target.name()));
					tm.baseState().setMana(tm.baseState().getMana()+2);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}
				if((myWish.indexOf("MOVE")>=0)&&(tm.curState().getMovement()<tm.maxState().getMovement()))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 has more move points!",target.name()));
					tm.curState().setMovement(tm.maxState().getMovement());
					wishDrain(mob,baseLoss,false);
					return true;
				}
				else
				if(myWish.indexOf("MOVE")>=0)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 has more move points!",target.name()));
					tm.baseState().setMovement(tm.baseState().getMovement()+5);
					tm.recoverMaxState();
					wishDrain(mob,baseLoss,true);
					return true;
				}

			}
			if((target instanceof MOB)
			&&(((MOB)target).charStats().getStat(CharStats.STAT_GENDER)!='M')
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0))
			&&((myWish.indexOf(" MALE ")>=0)
			||(myWish.indexOf(" MAN ")>=0)
			||(myWish.indexOf(" BOY ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				((MOB)target).baseCharStats().setStat(CharStats.STAT_GENDER,'M');
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now male!",target.name()));
				return true;
			}

			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" LIGHTER ")>=0)
			||(myWish.indexOf(" LOSE WEIGHT ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).basePhyStats().weight();
				weight-=50;
				if(weight<=0)
					weight=1;
				((MOB)target).basePhyStats().setWeight(weight);
				((MOB)target).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now lighter!",target.name()));
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" HEAVIER ")>=0)
			||(myWish.indexOf(" GAIN WEIGHT ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).basePhyStats().weight();
				weight+=50;
				((MOB)target).basePhyStats().setWeight(weight);
				((MOB)target).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now heavier!",target.name()));
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" EXP ")>=0)
			||(myWish.indexOf(" EXPERIENCE ")>=0)))
			{
				int x=myWish.indexOf(" EXP");
				final String wsh=myWish.substring(0,x).trim();
				x=wsh.lastIndexOf(' ');
				int amount=25;
				if((x>=0)&&(CMath.isNumber(wsh.substring(x).trim())))
					amount=CMath.s_int(wsh.substring(x).trim());
				if((amount*4)>mob.getExperience())
					amount=mob.getExperience()/4;

				CMLib.leveler().postExperience(mob,null,null,-(amount*4),false);
				mob.tell(L("Your wish has drained you of @x1 experience points.",""+(amount*4)));
				CMLib.leveler().postExperience((MOB)target,null,null,amount,false);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 gains experience!",target.name()));
				return true;
			}

			if((target!=null)
			&&((myWish.indexOf(" LOWER ")>=0)
				||(myWish.indexOf(" LOSE ")>=0)
				||(myWish.indexOf(" GAIN ")>=0)
				||(myWish.indexOf(" HIGHER ")>=0)
				||(myWish.indexOf(" WAS ")>=0)
				||(myWish.indexOf(" WOULD BE ")>=0)
				||(myWish.indexOf(" WOULD BECOME ")>=0)
				||(myWish.indexOf(" BECAME ")>=0))
			&&((myWish.indexOf(" LEVEL ")>=0)
				||(myWish.indexOf(" LEVELS ")>=0))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
			{
				int level=0;
				if(myWish.indexOf(" LOWER ")>=0)
					level=-1;
				else
				if(myWish.indexOf(" HIGHER" )>=0)
					level=1;
				else
				if(myWish.indexOf(" GAIN ")>=0)
				{
					level=1;
					final Vector<String> V=CMParms.parse(myWish);
					for(int i2=1;i2<V.size();i2++)
					{
						if(V.elementAt(i2).equalsIgnoreCase("LEVELS"))
						{
							final String s=V.elementAt(i2-1);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=CMath.s_int(s);
								break;
							}
						}
					}
				}
				else
				if(myWish.indexOf(" LOSE" )>=0)
				{
					level=-1;
					final Vector<String> V=CMParms.parse(myWish);
					for(int i2=1;i2<V.size();i2++)
					{
						if(V.elementAt(i2).equalsIgnoreCase("LEVELS"))
						{
							final String s=V.elementAt(i2);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=-CMath.s_int(s);
								break;
							}
						}
					}
				}
				else
				{
					final Vector<String> V=CMParms.parse(myWish);
					for(int i2=0;i2<V.size()-1;i2++)
					{
						if(V.elementAt(i2).equalsIgnoreCase("LEVEL"))
						{
							final String s=V.elementAt(i2+1);
							if(CMath.isNumber(s)
							&&((CMath.s_int(s)!=0)||(s.equalsIgnoreCase("0"))))
							{
								level=CMath.s_int(s)-target.basePhyStats().level();
								break;
							}
						}
					}
				}
				if(level!=0)
				{
					int levelsLost=level;
					if(levelsLost<0)
						levelsLost=levelsLost*-1;
					int levelsGained=levelsLost;
					levelsLost*=4;
					if(levelsLost>=mob.basePhyStats().level())
					{
						levelsLost=mob.basePhyStats().level()-1;
						levelsGained=levelsLost/4;
						if(level>0)
							level=levelsGained;
						else level=-levelsGained;
					}
					final int newLevel=target.basePhyStats().level()+level;
					if(target instanceof MOB)
					{
						if(((newLevel>CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL))
							||(((MOB)target).charStats().getCurrentClass().leveless())
							||(((MOB)target).charStats().isLevelCapped(((MOB)target).charStats().getCurrentClass()))
							||(((MOB)target).charStats().getMyRace().leveless()))
						&&(newLevel>target.basePhyStats().level()))
						{
							wishDrain(mob,baseLoss,false);
							mob.tell(L("That's beyond your power, but you lost exp even for trying."));
							return false;
						}
					}

					if(target instanceof MOB)
					{
						final MOB MT=(MOB)target;
						if(level>0)
						{
							for(int i2=0;i2<levelsGained;i2++)
							{
								CMLib.leveler().level(MT);
								MT.recoverPhyStats();
								MT.setExperience(CMLib.leveler().getLevelExperience(MT.basePhyStats().level()-1));
							}
						}
						else
						{
							while(MT.basePhyStats().level()>newLevel)
							{
								CMLib.leveler().unLevel(MT);
								MT.setExperience(CMLib.leveler().getLevelExperience(MT.basePhyStats().level()-1));
								MT.recoverPhyStats();
							}
						}
					}
					else
					{
						target.basePhyStats().setLevel(newLevel);
						target.recoverPhyStats();
					}
					wishDrain(mob,baseLoss*levelsLost,true);
					if((mob!=target)||(level>0))
					{
						msg.source().tell(L("Your wish also causes you to lose @x1 levels.",""+levelsLost));
						for(int i2=0;i2<levelsLost;i2++)
						{
							CMLib.leveler().unLevel(mob);
							mob.setExperience(CMLib.leveler().getLevelExperience(mob.basePhyStats().level()-1));
						}
					}
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now level @x2!",target.name(),""+target.phyStats().level()));
				}
				return true;
			}

			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" SMALL ")>=0)
			||(myWish.indexOf(" SHORT ")>=0)
			||(myWish.indexOf(" LITTLE ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).basePhyStats().height();
				weight-=12;
				if(weight<=0)
					weight=5;
				((MOB)target).basePhyStats().setHeight(weight);
				((MOB)target).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now shorter!",target.name()));
				return true;
			}
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" WAS ")>=0))
			&&((myWish.indexOf(" LARGE ")>=0)
			||(myWish.indexOf(" BIG ")>=0)
			||(myWish.indexOf(" TALL ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				int weight=((MOB)target).basePhyStats().height();
				weight+=12;
				((MOB)target).basePhyStats().setHeight(weight);
				((MOB)target).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now taller!",target.name()));
				return true;
			}

			if((target instanceof MOB)
			&&(((MOB)target).charStats().getStat(CharStats.STAT_GENDER)!='F')
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0))
			&&((myWish.indexOf(" FEMALE ")>=0)
			||(myWish.indexOf(" WOMAN ")>=0)
			||(myWish.indexOf(" GIRL ")>=0)))
			{
				wishDrain(mob,baseLoss,true);
				((MOB)target).baseCharStats().setStat(CharStats.STAT_GENDER,'F');
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now female!",target.name()));
				return true;
			}

			// change race
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0)))
			{
				final Race R=CMClass.findRace(wishV.lastElement());
				if((R!=null)
				&& (CMath.bset(R.availabilityCode(),Area.THEME_FANTASY))
				&&(!R.ID().equalsIgnoreCase("StdRace"))
				&&(!R.ID().equalsIgnoreCase("Unique")))
				{
					if(!((MOB)target).isMonster())
					{
						baseLoss+=500;
						CMLib.leveler().unLevel(mob);
						mob.setExperience(CMLib.leveler().getLevelExperience(mob.basePhyStats().level()-1));
					}
					wishDrain(mob,baseLoss,true);
					if(!((MOB)target).isMonster())
						msg.source().tell(L("Your wish also causes you lose a level."));
					final int oldCat=((MOB)target).baseCharStats().ageCategory();
					((MOB)target).baseCharStats().setMyRace(R);
					((MOB)target).baseCharStats().getMyRace().startRacing(((MOB)target),true);
					((MOB)target).baseCharStats().getMyRace().setHeightWeight(((MOB)target).basePhyStats(),(char)((MOB)target).baseCharStats().getStat(CharStats.STAT_GENDER));
					((MOB)target).recoverCharStats();
					((MOB)target).recoverPhyStats();
					CMLib.utensils().confirmWearability((MOB)target);
					if(!((MOB)target).isMonster())
						((MOB)target).baseCharStats().setStat(CharStats.STAT_AGE,R.getAgingChart()[oldCat]);
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now a @x2!",target.name(),R.name()));
					return true;
				}
			}

			// change class
			if((target instanceof MOB)
			&&((myWish.indexOf(" BECOME ")>=0)
			||(myWish.indexOf(" TURN INTO ")>=0)
			||(myWish.indexOf(" CHANGE")>=0)
			||(myWish.indexOf(" LEARN TO BE")>=0)
			||(myWish.indexOf(" BE A")>=0)
			||(myWish.indexOf(" WAS A")>=0)
			||(myWish.indexOf(" TRANSFORM")>=0)))
			{
				final CharClass C=CMClass.findCharClass(wishV.lastElement());
				if((C!=null)&&(CMath.bset(C.availabilityCode(),Area.THEME_FANTASY)))
				{
					final CharClass oldC=mob.baseCharStats().getCurrentClass();
					baseLoss+=1000;
					wishDrain(mob,baseLoss,true);
					msg.source().tell(L("Your wish also causes you lose 3 levels."));
					CMLib.leveler().unLevel(mob);
					CMLib.leveler().unLevel(mob);
					CMLib.leveler().unLevel(mob);
					mob.setExperience(CMLib.leveler().getLevelExperience(mob.basePhyStats().level()-1));
					final StringBuffer str=new StringBuffer("");
					for(final int trait: CharStats.CODES.BASECODES())
					{
						final int newVal=C.maxStatAdjustments()[trait];
						final int amountToLose=oldC.maxStatAdjustments()[trait]-newVal;
						if((amountToLose>0)&&(mob.baseCharStats().getStat(trait)>amountToLose))
						{
							mob.baseCharStats().setStat(trait,mob.baseCharStats().getStat(trait)-amountToLose);
							str.append(L("\n\rYou lost @x1 points of @x2.",""+amountToLose,CharStats.CODES.DESC(trait).toLowerCase()));
						}
					}
					mob.tell(str.toString()+"\n\r");
					((MOB)target).baseCharStats().setCurrentClass(C);
					if((!((MOB)target).isMonster())&&(((MOB)target).soulMate()==null))
						CMLib.coffeeTables().bump(target,CoffeeTableRow.STAT_CLASSCHANGE);
					((MOB)target).baseCharStats().getCurrentClass().startCharacter((MOB)target,false,true);
					((MOB)target).recoverCharStats();
					((MOB)target).recoverPhyStats();
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is now a @x2!",target.name(),C.name(((MOB)target).baseCharStats().getCurrentClassLevel())));
					return true;
				}
			}

			// gaining new abilities!
			if(target instanceof MOB)
			{
				int code=-1;
				int x = myWish.indexOf(" KNOW ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" KNEW ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" LEARN ");
				if ((x >= 0) && (x + 6 > code))
					code = x + 6;
				x = myWish.indexOf(" COULD ");
				if ((x >= 0) && (x + 6 > code))
					code = x + 6;
				x = myWish.indexOf(" GAIN ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" BE TAUGHT ");
				if ((x >= 0) && (x + 10 > code))
					code = x + 10;
				x = myWish.indexOf(" HOW TO ");
				if ((x >= 0) && (x + 7 > code))
					code = x + 7;
				x = myWish.indexOf(" ABLE TO ");
				if ((x >= 0) && (x + 8 > code))
					code = x + 8;
				x = myWish.indexOf(" CAST ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" SING ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" PRAY FOR ");
				if ((x >= 0) && (x + 9 > code))
					code = x + 9;
				if((code>=0)&&(code<myWish.length()))
				{
					final MOB tm=(MOB)target;
					Ability A=CMClass.findAbility(myWish.substring(code).trim());
					if((A!=null)
					&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>0)
					&&(!CMath.bset(A.classificationCode(),Ability.DOMAIN_ARCHON)))
					{
						if(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>=25)
						{
							baseLoss=getXPCOSTAdjustment(mob,baseLoss);
							CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
							mob.tell(L("Your wish has drained you of @x1 experience points, but that is beyond your wishing ability.",""+baseLoss));
							return false;
						}
						if(tm.fetchAbility(A.ID())!=null)
						{
							baseLoss=getXPCOSTAdjustment(mob,baseLoss);
							A=tm.fetchAbility(A.ID());
							CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
							mob.tell(L("Your wish has drained you of @x1 experience points.",""+baseLoss));
						}
						else
						{
							if(((myWish.indexOf(" CAST ")>=0)
								||(myWish.indexOf(" PRAY ")>=0)
								||(myWish.indexOf(" SING ")>=0))
							&&(onFlag)
							&&(target!=mob)&&(!A.isAutoInvoked()))
							{
								A.setProficiency(100);
								A.invoke(mob, target, true, asLevel);
								A=null;
							}
							else
							{
								tm.addAbility(A);
								baseLoss+=500;
								wishDrain(mob,baseLoss,true);
								msg.source().tell(L("Your wish also causes you lose 2 levels."));
								CMLib.leveler().unLevel(mob);
								CMLib.leveler().unLevel(mob);
							}
							mob.setExperience(CMLib.leveler().getLevelExperience(mob.basePhyStats().level()-1));
						}
						if(A!=null)
						{
							A=tm.fetchAbility(A.ID());
							A.setProficiency(100);
							A.autoInvocation(tm, false);
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 now knows @x2!",target.name(),A.name()));
							final Ability A2=tm.fetchEffect(A.ID());
							if(A2!=null)
								A2.setProficiency(100);
						}
						return true;
					}
				}
				code=-1;
				x = myWish.indexOf(" FORGOT ");
				if ((x >= 0) && (x + 7 > code))
					code = x + 7;
				x = myWish.indexOf(" LOST ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" LOSE ");
				if ((x >= 0) && (x + 5 > code))
					code = x + 5;
				x = myWish.indexOf(" REVOKE ");
				if ((x >= 0) && (x + 7 > code))
					code = x + 7;
				x = myWish.indexOf(" REVOKED ");
				if ((x >= 0) && (x + 8 > code))
					code = x + 8;
				x = myWish.indexOf(" DIDN`T KNOW ");
				if ((x >= 0) && (x + 12 > code))
					code = x + 12;
				x = myWish.indexOf(" DID NOT KNOW ");
				if ((x >= 0) && (x + 13 > code))
					code = x + 13;
				if(code>=0)
				{
					x = myWish.indexOf(" ABILITY TO ");
					if ((x >= 0) && (x + 12 > code))
						code = x + 12;
					x = myWish.indexOf(" KNOWLEDGE OF ");
					if ((x >= 0) && (x + 13 > code))
						code = x + 13;
					x = myWish.indexOf(" CAST ");
					if ((x >= 0) && (x + 5 > code))
						code = x + 5;
					x = myWish.indexOf(" SING ");
					if ((x >= 0) && (x + 5 > code))
						code = x + 5;
					x = myWish.indexOf(" PRAY FOR ");
					if ((x >= 0) && (x + 9 > code))
						code = x + 9;
				}
				if((code>=0)&&(code<myWish.length()))
				{
					final MOB tm=(MOB)target;
					Ability A=CMClass.findAbility(myWish.substring(code).trim());
					if((A!=null)
					&&(CMLib.ableMapper().lowestQualifyingLevel(A.ID())>0))
					{
						if(tm.fetchAbility(A.ID())!=null)
						{
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 no longer knows @x2!",target.name(),A.name()));
							tm.delAbility(A);
						}
						else
						if(tm.fetchEffect(A.ID())!=null)
						{
							mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 is no longer under @x2!",target.name(),A.name()));
						}
						else
						{
							A=null;
						}
						if(A!=null)
						{
							baseLoss=getXPCOSTAdjustment(mob,baseLoss);
							CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
							mob.tell(L("Your wish has drained you of @x1 experience points.",""+baseLoss));
							A=tm.fetchEffect(A.ID());
							if(A!=null)
							{
								A.unInvoke();
								tm.delEffect(A);
							}
							return true;
						}
					}
				}
			}

			// attributes will be hairy
			int foundAttribute=-1;
			for(final int attributes : CharStats.CODES.ALLCODES())
			{
				if(CMLib.english().containsString(myWish,CharStats.CODES.DESC(attributes)))
				{
					foundAttribute = attributes;
					break;
				}
			}
			if(myWish.indexOf("STRONG")>=0)
				foundAttribute=CharStats.STAT_STRENGTH;
			if(myWish.indexOf(" INTELLIGEN")>=0)
				foundAttribute=CharStats.STAT_INTELLIGENCE;
			if(myWish.indexOf(" SMART")>=0)
				foundAttribute=CharStats.STAT_INTELLIGENCE;
			if(myWish.indexOf(" WISE")>=0)
				foundAttribute=CharStats.STAT_WISDOM;
			if(myWish.indexOf(" FAST")>=0)
				foundAttribute=CharStats.STAT_DEXTERITY;
			if(myWish.indexOf(" DEXTROUS")>=0)
				foundAttribute=CharStats.STAT_DEXTERITY;
			if(myWish.indexOf(" HEALTH")>=0)
				foundAttribute=CharStats.STAT_CONSTITUTION;
			if(myWish.indexOf(" PRETTY")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if(myWish.indexOf(" NICE")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if(myWish.indexOf(" PRETTIER")>=0)
				foundAttribute=CharStats.STAT_CHARISMA;
			if((myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0))
			{
				for(final int saveStat : CharStats.CODES.SAVING_THROWS())
				{
					if(myWish.indexOf(" "+CharStats.CODES.DESC(saveStat))>=0)
						foundAttribute=saveStat;
				}
				if(foundAttribute<0)
				{
					for(final int saveStat : CharStats.CODES.SAVING_THROWS())
					{
						if(myWish.indexOf(" "+CharStats.CODES.NAME(saveStat))>=0)
							foundAttribute=saveStat;
					}
				}
				if(myWish.indexOf(" PARALY")>=0)
					foundAttribute=CharStats.STAT_SAVE_PARALYSIS;
				if(myWish.indexOf(" FIRE")>=0)
					foundAttribute=CharStats.STAT_SAVE_FIRE;
				if(myWish.indexOf(" FLAMES")>=0)
					foundAttribute=CharStats.STAT_SAVE_FIRE;
				if(myWish.indexOf(" COLD")>=0)
					foundAttribute=CharStats.STAT_SAVE_COLD;
				if(myWish.indexOf(" FROST")>=0)
					foundAttribute=CharStats.STAT_SAVE_COLD;
				if(myWish.indexOf(" GAS")>=0)
					foundAttribute=CharStats.STAT_SAVE_GAS;
				if(myWish.indexOf(" ACID")>=0)
					foundAttribute=CharStats.STAT_SAVE_ACID;
				if(myWish.indexOf(" SPELL ")>=0)
					foundAttribute=CharStats.STAT_SAVE_MAGIC;
				if(myWish.indexOf(" TRAPS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_TRAPS;
				if(myWish.indexOf(" SPELLS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_MAGIC;
				if(myWish.indexOf(" SONGS")>=0)
					foundAttribute=CharStats.STAT_SAVE_MIND;
				if(myWish.indexOf(" CHARMS")>=0)
					foundAttribute=CharStats.STAT_SAVE_MIND;
				if(myWish.indexOf(" ELECTRI")>=0)
					foundAttribute=CharStats.STAT_SAVE_ELECTRIC;
				if(myWish.indexOf(" POISON")>=0)
					foundAttribute=CharStats.STAT_SAVE_POISON;
				if(myWish.indexOf(" DEATH")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
				if(myWish.indexOf(" DISEASE")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" PLAGUE")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" COLDS ")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" SICK")>=0)
					foundAttribute=CharStats.STAT_SAVE_DISEASE;
				if(myWish.indexOf(" UNDEAD")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
				if(myWish.indexOf(" EVIL")>=0)
					foundAttribute=CharStats.STAT_SAVE_UNDEAD;
			}
			if((foundAttribute>=0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" LESS ")>=0)
			||(myWish.indexOf(" LOWER ")>=0)
			||(myWish.indexOf(" LESS RESIST")>=0)
			||(myWish.indexOf(" LESS IMMUN")>=0)
			||(myWish.indexOf(" NO RESIST")>=0)
			||(myWish.indexOf(" NO IMMUN")>=0)
			||(myWish.indexOf(" LOSE ")>=0)))
			{
				if(CharStats.CODES.isBASE(foundAttribute))
					baseLoss-=1000;
				else
					baseLoss-=10;
				wishDrain(mob,baseLoss,true);
				if(foundAttribute<=6)
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)-1);
				else
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)-33);
				((MOB)target).recoverCharStats();
				mob.recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 has lost @x2.",target.name(),CharStats.CODES.DESC(foundAttribute).toLowerCase()));
				return true;
			}

			if((foundAttribute>=0)
			&&(target instanceof MOB)
			&&((myWish.indexOf(" MORE ")>=0)
			||(myWish.indexOf(" HIGHER ")>=0)
			||(myWish.indexOf("RESIST")>=0)
			||(myWish.indexOf("IMMUN")>=0)
			||(myWish.indexOf(" BIGGER ")>=0)
			||(myWish.indexOf(" TO HAVE ")>=0)
			||(myWish.indexOf(" GAIN ")>=0)
			||(myWish.indexOf(" WAS ")>=0)
			||(myWish.indexOf(" TO BE ")>=0)))
			{
				switch(foundAttribute)
				{
				case CharStats.STAT_CHARISMA:
				case CharStats.STAT_CONSTITUTION:
				case CharStats.STAT_DEXTERITY:
				case CharStats.STAT_INTELLIGENCE:
				case CharStats.STAT_STRENGTH:
				case CharStats.STAT_WISDOM:
				{
					int trainsRequired=CMLib.login().getTrainingCost(mob, foundAttribute, true);
					if(trainsRequired<0)
						trainsRequired=-trainsRequired;
					if(trainsRequired>100)
						trainsRequired=100;
					baseLoss+=((CMLib.leveler().getLevelExperienceJustThisLevel(mob.basePhyStats().level())/5)*(1+trainsRequired));
					break;
				}
				default:
					baseLoss+=10;
					break;
				}
				wishDrain(mob,baseLoss,true);
				if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
				{
					msg.source().tell(L("Your wish causes you lose a level."));
					CMLib.leveler().unLevel(mob);
					mob.setExperience(CMLib.leveler().getLevelExperience(mob.basePhyStats().level()-1));
				}
				if(foundAttribute<=6)
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)+1);
				else
					((MOB)target).baseCharStats().setStat(foundAttribute,((MOB)target).baseCharStats().getStat(foundAttribute)+10);
				mob.recoverCharStats();
				((MOB)target).recoverCharStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 has gained @x2.",target.name(),CharStats.CODES.DESC(foundAttribute).toLowerCase()));
				return true;
			}

			baseLoss=getXPCOSTAdjustment(mob,baseLoss);
			CMLib.leveler().postExperience(mob,null,null,-baseLoss,false);
			Log.sysOut("Wish",mob.Name()+" unsuccessfully wished for '"+CMParms.combine(commands,0)+"'");
			mob.tell(L("Your attempted wish has cost you @x1 experience points, but it did not come true.  You might try rewording your wish next time.",""+baseLoss));
			return false;
		}
		return success;
	}
}
