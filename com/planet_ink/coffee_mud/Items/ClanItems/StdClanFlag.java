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
   Copyright 2000-2013 Bo Zimmerman

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
public class StdClanFlag extends StdItem implements ClanItem
{
	public String ID(){    return "StdClanFlag";}
	private Environmental riteOwner=null;
	public Environmental rightfulOwner(){return riteOwner;}
	public void setRightfulOwner(Environmental E){riteOwner=E;}
	protected String myClan="";
	protected int ciType=0;
	private long lastClanCheck=0;
	public int ciType(){return ciType;}
	public void setCIType(int type){ ciType=type;}
	public StdClanFlag()
	{
		super();

		setName("a clan flag");
		basePhyStats.setWeight(1);
		setDisplayText("an flag belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_FLAG);
		material=RawMaterial.RESOURCE_COTTON;
		recoverPhyStats();
	}

	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}
	
	public void setOwner(ItemPossessor E)
	{
		if((E==null)&&(super.owner!=null)&&(!amDestroyed())&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.FLAGWATCHING)))
		{ Log.debugOut("FLAGWATCH",name()); Log.debugOut("FLAGWATCH",new Exception(name()+" is being null-ownered."));}
		super.setOwner(E);
	}

	public void destroy()
	{
		if((super.owner!=null)&&(!amDestroyed())&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.FLAGWATCHING)))
		{ Log.debugOut("FLAGWATCH",name()); Log.debugOut("FLAGWATCH",new Exception(name()+" is being destroyed."));}
		super.destroy();
	}

	public long expirationDate(){return 0;}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((System.currentTimeMillis()-lastClanCheck)>TimeManager.MILI_HOUR)
		{
			lastClanCheck=System.currentTimeMillis();
			if((clanID().length()>0)&&(CMLib.clans().getClan(clanID())==null))
			{
				destroy();
				return;
			}
		}
		if(StdClanItem.stdExecuteMsg(this,msg))
		{
			super.executeMsg(myHost,msg);
			if((msg.amITarget(this))
			&&(clanID().length()>0)
			&&(msg.source().getClanRole(clanID())!=null))
			{
				Room R=msg.source().location();
				if(R==null)
					return;
				if((msg.targetMinor()==CMMsg.TYP_DROP)&&(msg.trailerMsgs()==null))
				{
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),this,CMMsg.MSG_LOOK,null));
					setRightfulOwner(R);
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
				{
					LegalBehavior B=CMLib.law().getLegalBehavior(R);
					String s="";
					if(B!=null) s=B.conquestInfo(CMLib.law().getLegalObject(R));
					if(s.length()>0)
						msg.source().tell(s);
					else
						msg.source().tell("This area is under the control of the Archons.");
					return;
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_SPEAK)
				&&(CMSecurity.isAllowed(msg.source(),R,CMSecurity.SecFlag.CMDROOMS))
				&&(msg.targetMessage()!=null))
				{
					String msgStr=CMStrings.getSayFromMessage(msg.targetMessage().toUpperCase());
					final String alert="I HEREBY DECLARE THIS AREA";
					int msgIndex=msgStr.indexOf(alert);
					if(msgIndex>=0)
					{
						LegalBehavior B=CMLib.law().getLegalBehavior(R);
						if(B!=null) B.setControlPoints(clanID(),B.controlPoints()+1);
					}
				}
			}
		}
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((clanID().length()>0)&&(msg.amITarget(this)))
		{
			if(msg.source().getClanRole(clanID())==null)
			{
				if((msg.targetMinor()==CMMsg.TYP_GET)
				||(msg.targetMinor()==CMMsg.TYP_PUSH)
				||(msg.targetMinor()==CMMsg.TYP_PULL)
				||(msg.targetMinor()==CMMsg.TYP_CAST_SPELL))
				{
					Room R=CMLib.map().roomLocation(this);
					if(CMLib.clans().findRivalrousClan(msg.source())==null)
					{
						msg.source().tell("You must belong to an elligible clan to take a clan item.");
						return false;
					}
					else
					if(R!=null)
					{
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(M.getClanRole(clanID())!=null)
							&&(CMLib.flags().aliveAwakeMobileUnbound(M,true))
							&&(CMLib.flags().canBeSeenBy(this,M))
							&&(!CMLib.flags().isAnimalIntelligence(M)))
							{
								R.show(M,null,CMMsg.MSG_QUIETMOVEMENT,"<S-NAME> guard(s) "+name()+" closely.");
								return false;
							}
						}
						String rulingClan="";
						LegalBehavior B=CMLib.law().getLegalBehavior(R);
						if(B!=null) rulingClan=B.rulingOrganization();
						if(msg.source().getClanRole(rulingClan)==null)
						{
							msg.source().tell("You must conquer and fully control this area to take the clan flag.");
							return false;
						}
						if((B!=null)&&(!B.isFullyControlled()))
						{
							msg.source().tell("Your clan does not yet fully control the area.");
							return false;
						}
					}
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_INTERMSG)))
				{
					Room R=msg.source().location();
					LandTitle T=null;
					Area A=null;
					LegalBehavior B=null;
					if(R!=null)
					{
						A=R.getArea();
						T=CMLib.law().getLandTitle(R);
					}
					if((T==null)
					||((!T.getOwnerName().equals(clanID()))
					   &&((!T.getOwnerName().equals(msg.source().getLiegeID()))||(!msg.source().isMarriedToLiege()))
					   &&(!T.getOwnerName().equals(msg.source().Name()))))
					{
						boolean ok=false;
						if(A!=null) 
						{
							B=CMLib.law().getLegalBehavior(R);
							if(B!=null) ok=B.controlPoints()>0;
						}
						if(!ok)
						{
							msg.source().tell("You can not place a flag here, this place is controlled by the Archons.");
							return false;
						}
					}
				}
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_GET)
			&&(msg.source().location()!=null)
			&&(msg.source().isMonster()))
			{
				boolean foundOne=false;
				for(int i=0;i<msg.source().location().numInhabitants();i++)
				{
					MOB M=msg.source().location().fetchInhabitant(i);
					if((M!=null)
					&&(!M.isMonster())
					&&(M.getClanRole(clanID())!=null))
					{ foundOne=true; break;}
				}
				if(!foundOne)
				{
					msg.source().tell("You are guarding "+name()+" too closely.");
					return false;
				}
			}
		}

		if(StdClanItem.stdOkMessage(this,msg))
		{
			if((clanID().length()>0)
			&&(msg.amITarget(this))
			&&(msg.targetMinor()==CMMsg.TYP_DROP))
			{
				LegalBehavior B=CMLib.law().getLegalBehavior(msg.source().location());
				String rulingClan=(B!=null)?B.rulingOrganization():"";
				if(rulingClan.length()==0)
					msg.source().tell("Area '"+msg.source().location().getArea().name()+"' is presently neutral.");
				else
				{
					msg.source().tell("Area '"+msg.source().location().getArea().name()+"' is presently controlled by "+rulingClan+".");
					if(!rulingClan.equals(clanID()))
					{
						int relation=Clan.REL_WAR;
						Clan C=CMLib.clans().getClan(clanID());
						if(C==null)
						{
							msg.source().tell("This ancient relic from a lost clan fades out of existence.");
							this.destroy();
							return false;
						}
						relation=C.getClanRelations(rulingClan);
						if(relation!=Clan.REL_WAR)
						{
							msg.source().tell("You must be at war with this clan to put down your flag on their area.");
							return false;
						}
					}
				}
			}
			return super.okMessage(myHost,msg);
		}
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!StdClanItem.standardTick(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}
}
