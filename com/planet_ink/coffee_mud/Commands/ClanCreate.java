package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

public class ClanCreate extends StdCommand
{
	public ClanCreate()
	{
	}

	private final String[]	access	= I(new String[] { "CLANCREATE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		int numGovernmentsAvailable=0;
		Pair<Clan,Integer> p=null;
		for(final ClanGovernment gvt : CMLib.clans().getStockGovernments())
		{
			if(CMProps.isPublicClanGvtCategory(gvt.getCategory()))
			{
				if(CMLib.clans().getClansByCategory(mob, gvt.getCategory()).size()<CMProps.getMaxClansThisCategory(gvt.getCategory()))
					numGovernmentsAvailable++;
				else
				if(p==null)
				{
					for(final Pair<Clan,Integer> c : mob.clans())
					{
						if(c.first.getCategory().equalsIgnoreCase(gvt.getCategory()))
							p=c;
					}
				}
			}
		}
		if(numGovernmentsAvailable==0)
		{
			if(p!=null)
				mob.tell(L("You are already a member of @x1. You need to resign before you can create another.",p.first.getName()));
			else
				mob.tell(L("You are not elligible to create a new clan at this time."));
			return false;
		}

		final Session session=mob.session();
		if(session!=null)
		{
			final int cost=CMProps.getIntVar(CMProps.Int.CLANCOST);
			if(cost>0)
			{
				if(CMLib.beanCounter().getTotalAbsoluteNativeValue(mob)<(cost))
				{
					mob.tell(L("It costs @x1 to create a clan.  You don't have it.",CMLib.beanCounter().nameCurrencyShort(mob,cost)));
					return false;
				}
			}
			session.prompt(new InputCallback(InputCallback.Type.CHOOSE,"N","YN\n",0)
			{
				@Override
				public void showPrompt()
				{
					session.promptPrint(L("Are you sure you want to found a new clan (y/N)?"));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					final String check=this.input;
					if(!check.equalsIgnoreCase("Y"))
						return;
					session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\r^HEnter the name of your new clan (30 chars max), exactly how you want it\n\r:^N"));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							final String doubleCheck=this.input;
							if(doubleCheck.length()<1)
								return;
							if(doubleCheck.length()>30) // Robert checking length
							{
								mob.tell(L("That name is too long, please use a shorter one."));
								return;
							}
							final Clan checkC=CMLib.clans().findClan(doubleCheck);
							if(CMLib.players().playerExists(doubleCheck)
							||(doubleCheck.equalsIgnoreCase("All")))
								mob.tell(L("That name can not be used."));
							else
							if(checkC!=null)
								mob.tell(L("Clan @x1  exists already. Type 'CLANLIST' and I'll show you what clans are available.  You may 'CLANAPPLY' to join them.",checkC.clanID()));
							else
							{
								session.prompt(new InputCallback(InputCallback.Type.CHOOSE,"N","YN\n",0)
								{
									@Override
									public void showPrompt()
									{
										session.promptPrint(L("\n\rIs '@x1' correct (y/N)?", doubleCheck));
									}

									@Override
									public void timedOut()
									{
									}

									@Override
									public void callBack()
									{
										final String check=this.input;
										if(!check.equalsIgnoreCase("Y"))
											return;
										final Clan newClan=(Clan)CMClass.getCommon("DefaultClan");
										newClan.setName(doubleCheck);
										final InputCallback[] IC=new InputCallback[1];
										IC[0]=new InputCallback(InputCallback.Type.PROMPT,"",0)
										{
											@Override 
											public void showPrompt()
											{
												final StringBuilder promptmsg=new StringBuilder("\n\r^HNow enter a political style for this clan. Choices are:\n\r^N");
												{
													int longest=0;
													for(final ClanGovernment gvt : CMLib.clans().getStockGovernments())
													{
														if((gvt.getName().length() > longest)&&(CMProps.isPublicClanGvtCategory(gvt.getCategory())))
															longest=gvt.getName().length();
													}
													for(final ClanGovernment gvt : CMLib.clans().getStockGovernments())
													{
														if(CMProps.isPublicClanGvtCategory(gvt.getCategory()))
														{
															promptmsg.append("^H"+CMStrings.padRight(gvt.getName(), longest))
																	 .append("^N:").append(gvt.getShortDesc()).append("\n\r");
														}
													}
												}
												session.promptPrint(promptmsg.toString()+": ");
											}

											@Override
											public void timedOut()
											{
											}

											@Override
											public void callBack()
											{
												final String govt=this.input;
												if(govt.length()==0)
												{
													mob.tell(L("Aborted."));
													return;
												}
												int govtType=-1;
												int newRoleID=-1;
												for(final ClanGovernment gvt : CMLib.clans().getStockGovernments())
												{
													if((govt.equalsIgnoreCase(gvt.getName()))&&(CMProps.isPublicClanGvtCategory(gvt.getCategory())))
													{
														govtType=gvt.getID();
														/*
														if(!CMLib.masking().maskCheck(C.getBasicRequirementMask(), mob, true))
														{
															mob.tell(L("You are not qualified to create a clan of this style.\n\rRequirements: @x1",CMLib.masking().maskDesc(gvt.requiredMaskStr)));
															session.prompt(IC[0].reset());
															return;
														}
														*/
														newClan.setGovernmentID(govtType);
														newRoleID=newClan.getTopQualifiedRoleID(Clan.Function.ASSIGN,mob);
														if((newClan.getAuthority(newRoleID, Clan.Function.ASSIGN) == Clan.Authority.CAN_NOT_DO)
														&&(newClan.getRolesList().length>1))
														{
															mob.tell(L("You are not qualified to lead a clan of this style.\n\r"));
															session.prompt(IC[0].reset());
															return;
														}
														else
														{
															break;
														}
													}
												}
												if((govtType<0)||(newRoleID<0))
												{
													mob.tell(L("That is not a proper type.\n\r"));
													session.prompt(IC[0].reset());
													return;
												}
												if(cost>0)
												{
													CMLib.beanCounter().subtractMoney(mob,cost);
												}

												newClan.setStatus(Clan.CLANSTATUS_PENDING);
												newClan.create();
												CMLib.database().DBUpdateClanMembership(mob.Name(),newClan.getName(),newRoleID);
												newClan.updateClanPrivileges(mob);
												CMLib.clans().clanAnnounce(mob, L("The @x1 @x2 is online and can now accept applicants.",newClan.getGovernmentName(),newClan.clanID()));
											}
										};
										session.prompt(IC[0]);
									}
								});
							}
						}
					});
				}
			});
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}
}
