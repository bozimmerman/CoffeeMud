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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2008 Bo Zimmerman

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
			commands.removeElementAt(0);
			if((((String)commands.firstElement()).equalsIgnoreCase("ROOM"))
			||(((String)commands.firstElement()).equalsIgnoreCase("AREA"))
			||(((String)commands.firstElement()).equalsIgnoreCase("WORLD")))
			{
				String which=((String)commands.firstElement()).toLowerCase();
				Enumeration rooms=null;
				if(which.equalsIgnoreCase("ROOM"))
					rooms=CMParms.makeVector(mob.location()).elements();
				else
				if(which.equalsIgnoreCase("AREA"))
					rooms=mob.location().getArea().getCompleteMap();
				else
				if(which.equalsIgnoreCase("WORLD"))
					rooms=CMLib.map().rooms();
				commands.removeElementAt(0);
				int whatKind=0;
				String type="objects";
				if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=1; type="mobs";}
				if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=2;type="items";}
				
				if((mob.session()!=null)
				&&(mob.session().confirm("You are about to auto-catalog (and save) all "+which+" "+type+", are you sure (y/N)?","N")))
				{
					Area A=null;
					//boolean dirtyMobs=false;
					//boolean dirtyItems=false;
					Item I=null;
					//Item cI=null;
					//MOB M=null;
					//MOB cM=null;
					//int ndx=0;
					//CMFlagLibrary flagLib=CMLib.flags();
					//WorldMap mapLib=CMLib.map();
					for(;rooms.hasMoreElements();)
					{
						R=(Room)rooms.nextElement();
						//dirtyMobs=false;
						//dirtyItems=false;
						if(R.roomID().length()>0)
						{
							A=R.getArea();
				            int oldFlag=A.getAreaFlags();
							R=CMLib.coffeeMaker().makeNewRoomContent(R);
							if(R==null) continue;
							A.setAreaFlags(Area.FLAG_FROZEN);
							if((whatKind==0)||(whatKind==2))
								for(int i=0;i<R.numItems();i++)
								{
									I=R.fetchItem(i);
									if((I==null)||(I instanceof Coins)) continue;
									//ndx=mapLib.getCatalogItemIndex(I.Name());
								}
							A.setAreaFlags(oldFlag);
						}
					}
				}
				
			}
			else
			if(((String)commands.firstElement()).equalsIgnoreCase("LIST"))
			{
				commands.removeElementAt(0);
				int whatKind=0; // 0=both, 1=mob, 2=item
				if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=1;}
				if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=2;}
				String ID=CMParms.combine(commands,0);
				StringBuffer list=new StringBuffer("");
				int col=0;
				MOB M=null;
				Item I=null;
				int[] usage=null;
				if((whatKind==0)||(whatKind==1)) 
				{
					list.append("^HMobs\n\r^N");
					list.append(CMStrings.padRight("Name",33)+" "+CMStrings.padRight("#",4)+" ");
					list.append(CMStrings.padRight("Name",33)+" "+CMStrings.padRight("#",4)+" ");
					list.append(CMStrings.repeat("-",78)+"\n\r");
					for(int i=0;i<CMLib.map().getCatalogMobs().size();i++)
					{
						M=CMLib.map().getCatalogMob(i);
						usage=CMLib.map().getCatalogMobUsage(i);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(M.Name(),ID)))
						{
							list.append(CMStrings.padRight(M.Name(),33)+" "+CMStrings.padRight(""+usage[0],4));
							if(col==0)
							{
								col++;
								list.append(" ");
							}
							else
							{
								col++;
								list.append("\n\r");
							}
						}
					}
					list.append("\n\r\n\r");
				}
				if((whatKind==0)||(whatKind==2)) 
				{
					list.append("^HItems\n\r^N");
					list.append(CMStrings.padRight("Name",33)+" "+CMStrings.padRight("#",4)+" ");
					list.append(CMStrings.padRight("Name",33)+" "+CMStrings.padRight("#",4)+" ");
					list.append(CMStrings.repeat("-",78)+"\n\r");
					for(int i=0;i<CMLib.map().getCatalogItems().size();i++)
					{
						I=CMLib.map().getCatalogItem(i);
						usage=CMLib.map().getCatalogItemUsage(i);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(I.Name(),ID)))
						{
							list.append(CMStrings.padRight(I.Name(),33)+" "+CMStrings.padRight(""+usage[0],4));
							if(col==0)
							{
								col++;
								list.append(" ");
							}
							else
							{
								col++;
								list.append("\n\r");
							}
						}
					}
					list.append("\n\r");
				}
				if(mob.session()!=null)
					mob.session().wraplessPrintln(list.toString());
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
				String ID=CMParms.combine(commands,0);
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
				String ID=CMParms.combine(commands,0);
				if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
					thisThang=mob;
				if(thisThang==null)
					thisThang=R.fetchFromRoomFavorMOBs(null,ID,Item.WORNREQ_ANY);
				if(thisThang!=null)
				{
					int exists=-1;
					if(thisThang instanceof MOB)
						exists=CMLib.map().getCatalogMobIndex(thisThang.Name());
					else
						exists=CMLib.map().getCatalogItemIndex(thisThang.Name());
					String msg="<S-NAME> catalog(s) <T-NAMESELF>.";
					
					CMLib.flags().setCataloged(thisThang,true);
					thisThang=(Environmental)thisThang.copyOf();
					CMLib.flags().setCataloged(thisThang,false);
					if(exists>=0)
					{
						StringBuffer diffs=new StringBuffer("");
						Environmental cat=(thisThang instanceof MOB)?
										 (Environmental)CMLib.map().getCatalogMob(exists):
										 (Environmental)CMLib.map().getCatalogItem(exists);
						if(thisThang.sameAs(cat))
						{
							CMLib.flags().setCataloged(thisThang,true);
							mob.tell("The object '"+cat.Name()+"' already exists in the catalog, exactly as it is.");
							return false;
						}
						for(int i=0;i<cat.getStatCodes().length;i++)
							if((!cat.getStat(cat.getStatCodes()[i]).equals(thisThang.getStat(cat.getStatCodes()[i]))))
								diffs.append(cat.getStatCodes()[i]);
						if((mob.session()==null)
						||(!mob.session().confirm("Cataloging that object will change the existing cataloged '"+thisThang.Name()+"' by altering the following properties: "+diffs.toString()+".  Please confirm (y/N)?","Y")))
							return false;
						msg="<S-NAME> modif(ys) the cataloged version of <T-NAMESELF>.";
						if(thisThang instanceof MOB)
						{
							if(((MOB)thisThang).databaseID().length()==0)
							{
								mob.tell("A null-databaseID was encountered.  Please consult CoffeeMud support.");
								return false;
							}
							CMLib.map().addCatalogReplace((MOB)thisThang);
							CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)thisThang);
						}
						else
						if(thisThang instanceof Item)
						{
							if(((Item)thisThang).databaseID().length()==0)
							{
								mob.tell("A null-databaseID was encountered.  Please consult CoffeeMud support.");
								return false;
							}
							CMLib.map().addCatalogReplace((Item)thisThang);
							CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)thisThang);
						}
						if(mob.session()!=null) mob.session().print("Updating world map...");
						CMLib.map().propogateCatalogChange(thisThang);
						if(mob.session()!=null) mob.session().println(".");
					}
					else
					{
						if(thisThang instanceof MOB)
						{
							CMLib.map().addCatalog((MOB)thisThang);
							CMLib.database().DBCreateThisMOB("CATALOG_MOBS",(MOB)thisThang);
						}
						else
						if(thisThang instanceof Item)
						{
							CMLib.map().addCatalog((Item)thisThang);
							CMLib.database().DBCreateThisItem("CATALOG_ITEMS",(Item)thisThang);
						}
					}
					R.show(mob,thisThang,CMMsg.MSG_OK_VISUAL,msg);
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
