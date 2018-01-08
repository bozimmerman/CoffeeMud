package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class StdClanItem extends StdItem implements ClanItem
{
	@Override
	public String ID()
	{
		return "StdClanItem";
	}

	protected String		myClan			= "";
	protected ClanItemType	ciType			= ClanItemType.SPECIALOTHER;
	private long			lastClanCheck	= System.currentTimeMillis();
	private Environmental	riteOwner		= null;

	public StdClanItem()
	{
		super();

		setName("a clan item");
		basePhyStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity = "";
		baseGoldValue = 1;
		material = RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	@Override
	public ClanItemType getClanItemType()
	{
		return ciType;
	}

	@Override
	public void setClanItemType(ClanItemType type)
	{
		ciType = type;
	}

	@Override
	public Environmental rightfulOwner()
	{
		return riteOwner;
	}

	@Override
	public void setRightfulOwner(Environmental E)
	{
		riteOwner = E;
	}

	@Override
	public String clanID()
	{
		return myClan;
	}

	@Override
	public void setClanID(String ID)
	{
		myClan = ID;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if ((System.currentTimeMillis() - lastClanCheck) > TimeManager.MILI_HOUR / 2)
		{
			if ((clanID().length() > 0) 
			&& (owner() instanceof MOB) 
			&& (!amDestroyed()))
			{
				if ((CMLib.clans().getClan(clanID()) == null) 
				|| ((getClanItemType() != ClanItem.ClanItemType.PROPAGANDA) 
					&& (((MOB) owner()).getClanRole(clanID()) == null)))
				{
					final Room R = CMLib.map().roomLocation(this);
					setRightfulOwner(null);
					unWear();
					removeFromOwnerContainer();
					if (owner() != R)
						R.moveItemTo(this, ItemPossessor.Expire.Player_Drop);
					if (R != null)
						R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 is dropped!", name()));
				}
			}
			lastClanCheck = System.currentTimeMillis();
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (StdClanItem.stdOkMessage(this, msg))
			return super.okMessage(myHost, msg);
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if (!StdClanItem.standardTick(this, tickID))
			return false;
		return super.tick(ticking, tickID);
	}

	public static boolean wearingAClanItem(MOB mob)
	{
		if (mob == null)
			return false;
		Item I = null;
		for (int i = 0; i < mob.numItems(); i++)
		{
			I = mob.getItem(i);
			if ((I instanceof ClanItem) 
			&& (!I.amWearingAt(Wearable.IN_INVENTORY)))
				return true;
		}
		return false;
	}

	public static boolean standardTick(Tickable ticking, int tickID)
	{
		if (tickID != Tickable.TICKID_CLANITEM)
			return true;
		if ((!(ticking instanceof ClanItem)) 
		|| (((ClanItem) ticking).clanID().length() == 0) 
		|| (((Item) ticking).amDestroyed()))
			return true;
		final ClanItem CI = (ClanItem) ticking;
		if (CI.owner() instanceof MOB)
		{
			final MOB M = ((MOB) ((Item) ticking).owner());
			if ((CI.getClanItemType() != ClanItem.ClanItemType.PROPAGANDA) 
			&& (M.getClanRole(CI.clanID()) == null))
			{
				if (M.location() != null)
				{
					CI.unWear();
					CI.removeFromOwnerContainer();
					CI.setRightfulOwner(null);
					if (CI.owner() != M.location())
						M.location().moveItemTo(CI, ItemPossessor.Expire.Player_Drop);
					M.location().show(M, CI, CMMsg.MSG_OK_VISUAL, CMLib.lang().L("<S-NAME> drop(s) <T-NAME>."));
					return false;
				}
			}
			else 
			if ((CI.amWearingAt(Wearable.IN_INVENTORY)) 
			&& (M.isMonster()) 
			&& (!wearingAClanItem(M)) 
			&& (CMLib.flags().isInTheGame(M, true)))
			{
				CI.setContainer(null);
				CI.wearAt(CI.rawProperLocationBitmap());
			}
		}
		else 
		if ((CI.owner() instanceof Room) && (CI.rightfulOwner() instanceof MOB))
		{
			if (CI.container() instanceof DeadBody)
				CI.setContainer(null);
			final MOB M = (MOB) CI.rightfulOwner();
			if (M.amDestroyed())
				CI.setRightfulOwner(null);
			else 
			if (!M.amDead())
				M.moveItemTo(CI);
		}
		return true;
	}

	protected static List<List<String>> loadList(StringBuffer str)
	{
		final List<List<String>> V = new Vector<List<String>>();
		if (str == null)
			return V;
		List<String> V2 = new Vector<String>();
		boolean oneComma = false;
		int start = 0;
		int longestList = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '\t')
			{
				V2.add(str.substring(start, i));
				start = i + 1;
				oneComma = true;
			}
			else 
			if ((str.charAt(i) == '\n') || (str.charAt(i) == '\r'))
			{
				if (oneComma)
				{
					V2.add(str.substring(start, i));
					if (V2.size() > longestList)
						longestList = V2.size();
					V.add(V2);
					V2 = new Vector<String>();
				}
				start = i + 1;
				oneComma = false;
			}
		}
		if (V2.size() > 1)
		{
			if (oneComma)
				V2.add(str.substring(start, str.length()));
			if (V2.size() > longestList)
				longestList = V2.size();
			V.add(V2);
		}
		for (int v = 0; v < V.size(); v++)
		{
			V2 = V.get(v);
			while (V2.size() < longestList)
				V2.add("");
		}
		return V;
	}

	public static synchronized List<List<String>> loadRecipes()
	{
		@SuppressWarnings("unchecked")
		List<List<String>> V = (List<List<String>>) Resources.getResource("PARSED: clancraft.txt");
		if (V == null)
		{
			final StringBuffer str = new CMFile(Resources.buildResourcePath("skills") + "clancraft.txt", null, CMFile.FLAG_LOGERRORS).text();
			V = loadList(str);
			if (V.size() == 0)
				Log.errOut("StdClanItem", "Recipes not found!");
			Resources.submitResource("PARSED: clancrtaft.txt", V);
		}
		return V;
	}

	public static boolean stdOkMessageMOBS(MOB giver, MOB targetMOB, Item myHost)
	{
		if ((targetMOB != null) && (targetMOB.isMonster()))
		{
			Item alreadyHasOne = null;
			for (int i = 0; i < targetMOB.numItems(); i++)
			{
				final Item I = targetMOB.getItem(i);
				if ((I instanceof ClanItem) 
				&& ((((ClanItem) myHost).getClanItemType() != ClanItem.ClanItemType.PROPAGANDA) 
					|| (((ClanItem) I).getClanItemType() == ClanItem.ClanItemType.PROPAGANDA)))
				{
					alreadyHasOne = I;
					break;
				}
			}
			if (alreadyHasOne != null)
			{
				if (giver != null)
					giver.tell(CMLib.lang().L("@x1 already has @x2, and cannot have another Clan Item.", targetMOB.name(), alreadyHasOne.name()));
				else
					targetMOB.location().show(targetMOB, null, myHost, CMMsg.MSG_OK_VISUAL, CMLib.lang().L("<S-NAME> can't seem to find the room for <O-NAME>."));
				return false;
			}
			if ((((ClanItem) myHost).getClanItemType() == ClanItem.ClanItemType.BANNER) 
			&& (!CMLib.flags().isMobile(targetMOB)))
			{
				if (giver != null)
					giver.tell(CMLib.lang().L("This item should only be given to those who roam the area."));
				else
					targetMOB.location().show(targetMOB, null, myHost, CMMsg.MSG_OK_VISUAL, CMLib.lang().L("<S-NAME> do(es)n't seem mobile enough to take <O-NAME>."));
				return false;
			}
			final Room startRoom = targetMOB.getStartRoom();
			if ((startRoom != null) 
			&& (startRoom.getArea() != null) 
			&& (targetMOB.location() != null) 
			&& (startRoom.getArea() != targetMOB.location().getArea()))
			{
				final LegalBehavior theLaw = CMLib.law().getLegalBehavior(startRoom.getArea());
				if ((theLaw != null) 
				&& (theLaw.rulingOrganization() != null) 
				&& (targetMOB.getClanRole(theLaw.rulingOrganization()) != null))
				{
					if (giver != null)
						giver.tell(CMLib.lang().L("You can only give a clan item to a conquered mob within the conquered area."));
					else
						targetMOB.location().show(targetMOB, null, myHost, CMMsg.MSG_OK_VISUAL, CMLib.lang().L("<S-NAME> can't seem to take <O-NAME> here."));
					return false;
				}
			}
		}
		return true;
	}

	public static boolean stdOkMessage(Environmental myHost, CMMsg msg)
	{
		if (((msg.tool() == myHost) 
			|| (msg.tool() == ((ClanItem) myHost).ultimateContainer(null))) 
		&& (msg.targetMinor() == CMMsg.TYP_GIVE) 
		&& (msg.target() instanceof MOB) 
		&& (myHost instanceof ClanItem)
		&& (((ClanItem) myHost).clanID().length() > 0))
		{
			final MOB targetMOB = (MOB) msg.target();
			if ((targetMOB.getClanRole(((ClanItem) myHost).clanID()) == null) 
			&& (((ClanItem) myHost).getClanItemType() != ClanItem.ClanItemType.PROPAGANDA))
			{
				msg.source().tell(CMLib.lang().L("You cannot give this item to @x1.", targetMOB.name()));
				return false;
			}
			else 
			if (!stdOkMessageMOBS(msg.source(), targetMOB, (Item) myHost))
				return false;
		}
		else 
		if ((msg.amITarget(myHost) 
			|| (msg.target() == ((ClanItem) myHost).ultimateContainer(null))) 
		&& (((ClanItem) myHost).clanID().length() > 0))
		{
			if ((msg.targetMinor() == CMMsg.TYP_GET) 
			|| (msg.targetMinor() == CMMsg.TYP_PUSH) 
			|| (msg.targetMinor() == CMMsg.TYP_PULL) 
			|| (msg.targetMinor() == CMMsg.TYP_CAST_SPELL))
			{
				if (CMLib.clans().findRivalrousClan(msg.source()) == null)
				{
					msg.source().tell(CMLib.lang().L("You must belong to an elligible clan to do that to a clan item."));
					return false;
				}
				else
				{
					final Clan itemC = CMLib.clans().getClan(((ClanItem) myHost).clanID());
					if (itemC == null)
					{
						msg.source().tell(CMLib.lang().L("This ancient relic from a lost clan fades out of existence."));
						((ClanItem) myHost).destroy();
						return false;
					}
					if ((msg.targetMinor() != CMMsg.TYP_CAST_SPELL) && (!stdOkMessageMOBS(null, msg.source(), (Item) myHost)))
						return false;
					else 
					if ((msg.source().getClanRole(itemC.clanID()) == null) 
					&& (((ClanItem) myHost).getClanItemType() != ClanItem.ClanItemType.PROPAGANDA))
					{
						int relation = -1;
						for (final Pair<Clan, Integer> p : CMLib.clans().findRivalrousClans(msg.source()))
						{
							relation = itemC.getClanRelations(p.first.clanID());
							if (relation == Clan.REL_WAR)
								break;
						}
						final Room room = msg.source().location();
						if ((room != null) && (room.getArea() != null))
						{
							final LegalBehavior theLaw = CMLib.law().getLegalBehavior(room.getArea());
							if ((theLaw != null) && (theLaw.rulingOrganization() != null))
							{
								if(theLaw.rulingOrganization().equals(itemC.clanID()))
								{
									msg.source().tell(CMLib.lang().L("You'll need to conquer this area to do that."));
									return false;
								}
								if(msg.source().getClanRole(theLaw.rulingOrganization())!=null)
								{
									if(!theLaw.isFullyControlled())
									{
										msg.source().tell(CMLib.lang().L("Your clan does not yet fully control the area."));
										return false;
									}
								}
							}
							else
							if (relation != Clan.REL_WAR)
							{
								msg.source().tell(CMLib.lang().L("You must be at war with this clan to take one of their items."));
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public static boolean stdExecuteMsg(Environmental myHost, CMMsg msg)
	{
		if ((msg.amITarget(myHost)) 
		&& ((msg.targetMinor() == CMMsg.TYP_GET) 
			|| (msg.targetMinor() == CMMsg.TYP_PUSH) 
			|| (msg.targetMinor() == CMMsg.TYP_PULL)) 
		&& (((ClanItem) myHost).clanID().length() > 0)
		&& (((ClanItem) myHost).getClanItemType() != ClanItem.ClanItemType.PROPAGANDA))
		{
			final MOB M = msg.source();
			if (M.getClanRole(((ClanItem) myHost).clanID()) != null)
			{
				if (M.isMonster())
					((ClanItem) myHost).setRightfulOwner(M);
				else
					((ClanItem) myHost).setRightfulOwner(null);
			}
			else
			{
				if (M.location() != null)
					M.location().show(M, myHost, CMMsg.MSG_OK_ACTION, CMLib.lang().L("<T-NAME> is destroyed by <S-YOUPOSS> touch!"));
				for (final Pair<Clan, Integer> clanP : CMLib.clans().findRivalrousClans(M))
				{
					final Clan C = clanP.first;
					final List<List<String>> recipes = loadRecipes();
					for (int v = 0; v < recipes.size(); v++)
					{
						final List<String> V = recipes.get(v);
						if ((V.size() > 3) && (CMath.s_int(V.get(3)) == ((ClanItem) myHost).getClanItemType().ordinal()))
						{
							final int exp = CMath.s_int(V.get(6)) / 2;
							if (exp > 0)
							{
								C.setExp(C.getExp() + exp);
								M.tell(CMLib.lang().L("@x1 gains @x2 experience points for this capture.", CMStrings.capitalizeFirstLetter(C.getName()), "" + exp));
							}
							break;
						}
					}
				}
				((Item) myHost).destroy();
				return false;
			}
		}
		return true;
	}
}
