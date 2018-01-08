package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Prop_PrivateProperty extends Property implements PrivateProperty
{
	@Override
	public String ID()
	{
		return "Prop_PrivateProperty";
	}

	@Override
	public String name()
	{
		return "Physical Property";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected int		price		= -1;
	protected String	owner		= "";
	protected long		expiresec	= 0;
	protected long		expires		= 0;
	protected MOB		lastCheckM	= null;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText!=null)
		{
			price = CMParms.getParmInt(newMiscText, "PRICE", -1);
			owner = CMParms.getParmStr(newMiscText, "OWNER", "");
			expiresec = CMParms.getParmLong(newMiscText, "EXPIRESEC", 0L);
		}
	}
	
	@Override
	public int getPrice()
	{
		if((affected instanceof Item)&&(price<0))
			return ((Item)affected).value();
		if(price<0)
			return 0;
		return price;
	}

	@Override
	public void setPrice(int price)
	{
		this.price=price;
	}

	@Override
	public String getOwnerName()
	{
		return owner;
	}

	@Override
	public void setOwnerName(String owner)
	{
		if(owner==null)
			this.owner="";
		else
			this.owner=owner;
	}

	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return null;
		final Clan C=CMLib.clans().getClan(owner);
		if(C!=null)
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}

	@Override
	public String getTitleID()
	{
		return affected.toString();
	}

	protected void checkExpiration()
	{
		final Physical affected=this.affected;
		if(affected instanceof Item)
		{
			final Item I=(Item)affected;
			final Physical owner=I.owner();
			if(owner instanceof MOB)
			{
				if(owner != this.lastCheckM)
				{
					if(CMLib.law().doesHaveWeakPrivilegesWith((MOB)owner, this))
					{
						affected.delEffect(this);
						this.affected=null;
						this.expires=0;
					}
					lastCheckM=(MOB)owner;
				}
			}
			else
			if(owner instanceof Room)
			{
				final Room R=(Room)owner;
				if(expires>0)
				{
					if(System.currentTimeMillis()>expires)
					{
						affected.delEffect(this);
						this.affected=null;
						this.expires=0;
					}
				}
				else
				if((this.getOwnerName().length()>0)
				&&(CMLib.law().doesOwnThisProperty(this.getOwnerName(), R)))
				{
					if(expiresec<=0)
					{
						affected.delEffect(this);
						this.affected=null;
						this.expires=0;
					}
					else
					{
						if(R.numInhabitants()==0)
							expires=System.currentTimeMillis()+expiresec;
						else
						{
							boolean expire=true;
							for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
							{
								final MOB M=m.nextElement();
								if((M!=null)&&(CMLib.law().doesHaveWeakPrivilegesWith(M, this)))
								{
									expire=false;
									break;
								}
							}
							if(expire)
								expires=System.currentTimeMillis()+(expiresec * 1000L);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void affectPhyStats(Physical affectedOne, PhyStats affectableStats)
	{
		super.affectPhyStats(affectedOne, affectableStats);
		checkExpiration();
	}
	
	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		return true;
	}
}
