package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2007 Bo Zimmerman

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
public class Catalog extends StdCommand
{
	public Catalog(){}

	private String[] access={"CATALOG"};
	public String[] getAccessWords(){return access;}
	
	public int[] findCatalogIndex(int whatKind, String ID, boolean exactOnly)
	{
		int[] data=new int[]{-1,-1};
		if((data[0]<0)&&((whatKind==0)||(whatKind==1)))
		{ data[0]=CMLib.map().getCatalogMobIndex(ID); if(data[0]>=0) data[1]=1;}
		if((data[0]<0)&&((whatKind==0)||(whatKind==2)))
		{ data[0]=CMLib.map().getCatalogItemIndex(ID); if(data[0]>=0) data[1]=2;}
		if(exactOnly) return data;
		if((data[0]<0)&&((whatKind==0)||(whatKind==1)))
			for(int x=0;x<CMLib.map().getCatalogMobs().size();x++)
				if(CMLib.english().containsString(((MOB)CMLib.map().getCatalogMobs().elementAt(x,1)).Name(), ID))
				{	data[0]=x; data[1]=1; break;}
		if((data[0]<0)&&((whatKind==0)||(whatKind==2)))
			for(int x=0;x<CMLib.map().getCatalogItems().size();x++)
				if(CMLib.english().containsString(((Item)CMLib.map().getCatalogItems().elementAt(x,1)).Name(), ID))
				{	data[0]=x; data[1]=2; break;}
		return data;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Room R=mob.location();
		if(R==null) return false;
		if((commands!=null)&&(commands.size()>1))
		{
			if(((String)commands.firstElement()).equalsIgnoreCase("LIST"))
			{
				commands.removeElementAt(0);
				int whatKind=0; // 0=both, 1=mob, 2=item
				if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=1;}
				if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=2;}
				
				
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("DELETE"))
			{
				commands.removeElementAt(0);
				int whatKind=0; // 0=both, 1=mob, 2=item
				if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=1;}
				if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=2;}
				String ID=CMParms.combine(commands,1);
				int[] foundData=findCatalogIndex(whatKind,ID,false);
				if(foundData[0]<0)
				{
					mob.tell("'"+ID+"' not found in catalog! Try LIST CATALOG");
					return false;
				}
				Environmental E=(foundData[1]==1)?
								(Environmental)CMLib.map().getCatalogMob(foundData[0]):
								(Environmental)CMLib.map().getCatalogItem(foundData[0]);
				int[] usage=(foundData[1]==1)?
							CMLib.map().getCatalogMobUsage(foundData[0]):
							CMLib.map().getCatalogItemUsage(foundData[0]);
				if(E instanceof MOB)
				{
					String prefix="";
					if(usage[0]>0)
						prefix="Catalog MOB '"+((MOB)E).Name()+" is currently listed as being in use '"+usage[0]+" times.  ";
					if((mob.session()!=null)
					&&(mob.session().confirm(prefix+"This will permanently delete mob '"+((MOB)E).Name()+"' from the catalog.  Are you sure (y/N)?","N")))
					{
						CMLib.map().delCatalog((MOB)E);
						CMLib.database().DBDeleteMOB("CATALOG_MOBS",(MOB)E);
						mob.tell("MOB '"+((MOB)E).Name()+" has been permanently removed from the catalog.");
					}
				}
				else
				if(E instanceof Item)
				{
					String prefix="";
					if(usage[0]>0)
						prefix="Catalog Item '"+((Item)E).Name()+" is currently listed as being in use '"+usage[0]+" times.  ";
					if((mob.session()!=null)
					&&(mob.session().confirm(prefix+"This will permanently delete item '"+((Item)E).Name()+"' from the catalog.  Are you sure (y/N)?","N")))
					{
						CMLib.map().delCatalog((Item)E);
						CMLib.database().DBDeleteItem("CATALOG_ITEMS",(Item)E);
						mob.tell("Item '"+((MOB)E).Name()+" has been permanently removed from the catalog.");
					}
				}
					
			}
			else
			{
				Environmental thisThang=null;
				String ID=CMParms.combine(commands,1);
				if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
					thisThang=mob;
				if(thisThang==null)
					thisThang=R.fetchFromRoomFavorMOBs(null,ID,Item.WORNREQ_ANY);
				if(thisThang!=null)
				{
					String textMsg="<S-NAME> catalog(s) <T-NAMESELF>.";
					CMMsg msg=CMClass.getMsg(mob,thisThang,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_LOOK,textMsg);
					if(R.okMessage(mob,msg))
						R.send(mob,msg);
				}
				else
					mob.tell("You don't see '"+ID+"' here!");
			}
		}
		else
			mob.tell("Catalog huh? Try CATALOG LIST (MOBS/ITEMS) (MASK), CATALOG <mob/item name>, CATALOG DELETE <mob/item name>.");
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CATALOG");}
}
