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
	
	public boolean catalog(Room R, MOB mob, Environmental E)
	    throws java.io.IOException
	{
	    Environmental origE=E;
        int exists=-1;
        if(E instanceof MOB)
            exists=CMLib.catalog().getCatalogMobIndex(E.Name());
        else
            exists=CMLib.catalog().getCatalogItemIndex(E.Name());
        String msg="<S-NAME> catalog(s) <T-NAMESELF>.";
        if(CMLib.flags().isCataloged(E))
        {
            mob.tell("The object '"+E.Name()+"' is already cataloged.");
            return false;
        }
        CMLib.flags().setCataloged(E,true);
        E=(Environmental)E.copyOf();
        CMLib.flags().setCataloged(E,false);
        if(exists>=0)
        {
            StringBuffer diffs=new StringBuffer("");
            Environmental cat=(E instanceof MOB)?
                             (Environmental)CMLib.catalog().getCatalogMob(exists):
                             (Environmental)CMLib.catalog().getCatalogItem(exists);
            if(E.sameAs(cat))
            {
                CMLib.flags().setCataloged(E,true);
                mob.tell("The object '"+cat.Name()+"' already exists in the catalog, exactly as it is.");
                return true;
            }
            for(int i=0;i<cat.getStatCodes().length;i++)
                if((!cat.getStat(cat.getStatCodes()[i]).equals(E.getStat(cat.getStatCodes()[i]))))
                    diffs.append(cat.getStatCodes()[i]+",");
            if((mob.session()==null)
            ||(!mob.session().confirm("Cataloging that object will change the existing cataloged '"+E.Name()+"' by altering the following properties: "+diffs.toString()+".  Please confirm (y/N)?","Y")))
            {
                CMLib.flags().setCataloged(origE,false);
                return false;
            }
            msg="<S-NAME> modif(ys) the cataloged version of <T-NAMESELF>.";
            if(E instanceof MOB)
            {
                if(((MOB)E).databaseID().length()==0)
                {
                    CMLib.flags().setCataloged(origE,false);
                    mob.tell("A null-databaseID was encountered.  Please consult CoffeeMud support.");
                    return false;
                }
                CMLib.catalog().addCatalogReplace((MOB)E);
                CMLib.database().DBUpdateMOB("CATALOG_MOBS",(MOB)E);
            }
            else
            if(E instanceof Item)
            {
                if(((Item)E).databaseID().length()==0)
                {
                    CMLib.flags().setCataloged(origE,false);
                    mob.tell("A null-databaseID was encountered.  Please consult CoffeeMud support.");
                    return false;
                }
                CMLib.catalog().addCatalogReplace((Item)E);
                CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)E);
            }
            if(mob.session()!=null) mob.session().print("Updating world map...");
            CMLib.catalog().propogateCatalogChange(E);
            if(mob.session()!=null) mob.session().println(".");
        }
        else
        {
            if(E instanceof MOB)
            {
                CMLib.catalog().addCatalog((MOB)E);
                CMLib.database().DBCreateThisMOB("CATALOG_MOBS",(MOB)E);
            }
            else
            if(E instanceof Item)
            {
                CMLib.catalog().addCatalog((Item)E);
                CMLib.database().DBCreateThisItem("CATALOG_ITEMS",(Item)E);
            }
        }
        R.show(mob,E,CMMsg.MSG_OK_VISUAL,msg);
        return true;
	}
	
	
	public int[] findCatalogIndex(int whatKind, String ID, boolean exactOnly)
	{
		int[] data=new int[]{-1,-1};
		if((data[0]<0)&&((whatKind==0)||(whatKind==1)))
		{ data[0]=CMLib.catalog().getCatalogMobIndex(ID); if(data[0]>=0) data[1]=1;}
		if((data[0]<0)&&((whatKind==0)||(whatKind==2)))
		{ data[0]=CMLib.catalog().getCatalogItemIndex(ID); if(data[0]>=0) data[1]=2;}
		if(exactOnly) return data;
		if((data[0]<0)&&((whatKind==0)||(whatKind==1)))
			for(int x=0;x<CMLib.catalog().getCatalogMobs().size();x++)
				if(CMLib.english().containsString(((MOB)CMLib.catalog().getCatalogMobs().elementAt(x,1)).Name(), ID))
				{	data[0]=x; data[1]=1; break;}
		if((data[0]<0)&&((whatKind==0)||(whatKind==2)))
			for(int x=0;x<CMLib.catalog().getCatalogItems().size();x++)
				if(CMLib.english().containsString(((Item)CMLib.catalog().getCatalogItems().elementAt(x,1)).Name(), ID))
				{	data[0]=x; data[1]=2; break;}
		return data;
	}
	
	public boolean execute(MOB mob, Vector<Object> commands, int metaFlags)
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
					rooms=CMParms.makeVector(CMLib.map().getExtendedRoomID(mob.location())).elements();
				else
				if(which.equalsIgnoreCase("AREA"))
					rooms=mob.location().getArea().getProperRoomnumbers().getRoomIDs();
				else
				if(which.equalsIgnoreCase("WORLD"))
					rooms=CMLib.map().roomIDs();
				commands.removeElementAt(0);
				int whatKind=0;
				String type="objects";
				if((commands.size()>0)&&("MOBS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=1; type="mobs";}
				if((commands.size()>0)&&("ITEMS".startsWith(((String)commands.firstElement()).toUpperCase().trim())))
				{ commands.removeElementAt(0); whatKind=2;type="items";}
				
				if((mob.session()!=null)
				&&(mob.session().confirm("You are about to auto-catalog (and auto-save) all "+which+" "+type+".\n\r"
			        +"This command, if used improperly, may alter "+type+" in this "+which+".\n\rAre you absolutely sure (y/N)?","N"))
                &&(mob.session().confirm("I'm serious now.  You can't abort this, and it WILL modify stuff.\n\r"
                    +"Have you tested this command on small areas and know what you're doing?.\n\rAre you absolutely POSITIVELY sure (y/N)?","N")))
				{
					Item I=null;
					MOB M=null;
					Environmental E=null;
					ShopKeeper SK=null;
					String roomID=null;
                    boolean dirtyItems=false;
                    boolean dirtyMobs=false;
                    Vector shops=null;
                    Vector shopItems=null;
                    Environmental shopItem=null;
					for(;rooms.hasMoreElements();)
					{
						roomID=(String)rooms.nextElement();
						if(roomID.length()>0)
						{
							R=CMLib.coffeeMaker().makeNewRoomContent(CMLib.map().getRoom(roomID));
							if(R==null) continue;
		                    dirtyItems=false;
		                    dirtyMobs=false;
                            shops=CMLib.coffeeShops().getAllShopkeepers(R,null);
                            for(int s=0;s<shops.size();s++)
                            {
                                boolean dirtyShop=false;
                                E=(Environmental)shops.elementAt(s);
                                if(E==null) continue;
                                SK=CMLib.coffeeShops().getShopKeeper(E);
                                if(SK==null) continue;
                                shopItems=SK.getShop().getStoreInventory();
                                DVector addBacks=new DVector(3);
                                for(int b=0;b<shopItems.size();b++)
                                {
                                    shopItem=(Environmental)shopItems.elementAt(b);
                                    int num=SK.getShop().numberInStock(shopItem);
                                    int price=SK.getShop().stockPrice(shopItem);
                                    if((shopItem instanceof MOB)&&(whatKind!=2))
                                        dirtyShop=catalog(R,mob,shopItem)||dirtyShop;
                                    if((shopItem instanceof Item)&&(whatKind!=1))
                                        dirtyShop=catalog(R,mob,shopItem)||dirtyShop;
                                    addBacks.addElement(shopItem,new Integer(num),new Integer(price));
                                }
                                if(dirtyShop)
                                {
                                    SK.getShop().emptyAllShelves();
                                    for(int a=0;a<addBacks.size();a++)
                                        SK.getShop().addStoreInventory(
                                                (Environmental)addBacks.elementAt(a,1),
                                                ((Integer)addBacks.elementAt(a,2)).intValue(),
                                                ((Integer)addBacks.elementAt(a,3)).intValue(),
                                                SK);
                                    dirtyMobs=(E instanceof MOB)||dirtyMobs;
                                    dirtyItems=(E instanceof Item)||dirtyItems;
                                }
                            }
							if((whatKind==0)||(whatKind==2))
								for(int i=0;i<R.numItems();i++)
								{
									I=R.fetchItem(i);
									if((I==null)||(I instanceof Coins)) continue;
									dirtyItems=catalog(R,mob,I)||dirtyItems;
								}
							for(int m=0;m<R.numInhabitants();m++)
							{
							    M=R.fetchInhabitant(m);
							    if(M==null) continue;
	                            if((whatKind==0)||(whatKind==2))
	                            {
	                                for(int i=0;i<M.inventorySize();i++)
	                                {
	                                    I=M.fetchInventory(i);
	                                    if((I==null)||(I instanceof Coins)) continue;
	                                    dirtyMobs=catalog(R,mob,I)||dirtyMobs;
	                                }
	                            }
                                if((whatKind==0)||(whatKind==1))
                                    dirtyMobs=catalog(R,mob,M)||dirtyMobs;
							}
							if(dirtyMobs)
							    CMLib.database().DBUpdateMOBs(R);
                            if(dirtyItems)
                                CMLib.database().DBUpdateItems(R);
                            R.destroy();
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
				CatalogLibrary.CataData data;
				if((whatKind==0)||(whatKind==1)) 
				{
					list.append("^HMobs\n\r^N");
					list.append(CMStrings.padRight("Name",38)+" ");
					list.append(CMStrings.padRight("Name",38)+" ");
					list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
					for(int i=0;i<CMLib.catalog().getCatalogMobs().size();i++)
					{
						M=CMLib.catalog().getCatalogMob(i);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(M.Name(),ID)))
						{
							list.append(CMStrings.padRight(M.Name(),38));
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
					list.append(CMStrings.padRight("Name",38)+" ");
                    list.append(CMStrings.padRight("Rate",6)+" ");
					list.append(CMStrings.padRight("Mask",31)+" ");
                    list.append("\n\r"+CMStrings.repeat("-",78)+"\n\r");
					for(int i=0;i<CMLib.catalog().getCatalogItems().size();i++)
					{
						I=CMLib.catalog().getCatalogItem(i);
                        data=CMLib.catalog().getCatalogItemData(i);
						if((ID==null)||(ID.length()==0)||(CMLib.english().containsString(I.Name(),ID)))
						{
							list.append(CMStrings.padRight(I.Name(),38)+" ");
							if(data.rate<=0.0)
							{
							    list.append("N/A   ");
			                    list.append(CMStrings.padRight(" ",31));
							}
							else
							{
							    list.append(CMStrings.padRight(CMath.toPct(data.rate),6)+" ");
                                list.append(data.live?"L ":"D ");
                                list.append(CMStrings.padRight(data.lmaskStr,29));
							}
							list.append("\n\r");
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
					mob.tell("'"+ID+"' not found in catalog! Try CATALOG LIST");
					return false;
				}
				Environmental E=(foundData[1]==1)?
								(Environmental)CMLib.catalog().getCatalogMob(foundData[0]):
								(Environmental)CMLib.catalog().getCatalogItem(foundData[0]);
				//int[] usage=(foundData[1]==1)?
				//			CMLib.catalog().getCatalogMobUsage(foundData[0]):
				//			CMLib.catalog().getCatalogItemUsage(foundData[0]);
				if(E instanceof MOB)
				{
					String prefix="";
					//if(usage[0]>0)
					//	prefix="Catalog MOB '"+((MOB)E).Name()+"' is currently listed as being in use '"+usage[0]+" times.  ";
					if((mob.session()!=null)
					&&(mob.session().confirm(prefix+"This will permanently delete mob '"+((MOB)E).Name()+"' from the catalog.  Are you sure (y/N)?","N")))
					{
						CMLib.catalog().delCatalog((MOB)E);
						CMLib.database().DBDeleteMOB("CATALOG_MOBS",(MOB)E);
						mob.tell("MOB '"+((MOB)E).Name()+" has been permanently removed from the catalog.");
					}
				}
				else
				if(E instanceof Item)
				{
					String prefix="";
					//if(usage[0]>0)
					//	prefix="Catalog Item '"+((Item)E).Name()+"' is currently listed as being in use '"+usage[0]+" times.  ";
					if((mob.session()!=null)
					&&(mob.session().confirm(prefix+"This will permanently delete item '"+((Item)E).Name()+"' from the catalog.  Are you sure (y/N)?","N")))
					{
						CMLib.catalog().delCatalog((Item)E);
						CMLib.database().DBDeleteItem("CATALOG_ITEMS",(Item)E);
						mob.tell("Item '"+E.Name()+" has been permanently removed from the catalog.");
					}
				}
					
			}
            else
            if(((String)commands.firstElement()).equalsIgnoreCase("EDIT"))
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
                    mob.tell("'"+ID+"' not found in catalog! Try CATALOG LIST");
                    return false;
                }
                Environmental E=(foundData[1]==1)?
                                (Environmental)CMLib.catalog().getCatalogMob(foundData[0]):
                                (Environmental)CMLib.catalog().getCatalogItem(foundData[0]);
                CatalogLibrary.CataData data=(foundData[1]==1)?
                                            CMLib.catalog().getCatalogMobData(foundData[0]):
                                            CMLib.catalog().getCatalogItemData(foundData[0]);
                if(E instanceof MOB)
                {
                    mob.tell("There is no extra mob data to edit. See help on CATALOG.");
                }
                else
                if(E instanceof Item)
                {
                    if(mob.session()!=null)
                    {
                        String newRate=mob.session().prompt("Enter a new Drop Rate or 0% to disable ("+CMath.toPct(data.rate)+"): ", CMath.toPct(data.rate));
                        if(!CMath.isPct(newRate))
                            return false;
                        data.rate=CMath.s_pct(newRate);
                        if(data.rate<=0.0)
                        {
                            data.lmaskStr="";
                            data.lmaskV=null;
                            data.rate=0.0;
                            CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)E);
                            mob.tell("No drop item.");
                            return false;
                        }
                        String choice=mob.session().choose("Is this for L)ive mobs or D)ead ones ("+(data.live?"L":"D")+"): ","LD", (data.live?"L":"D"));
                        data.live=choice.equalsIgnoreCase("L");
                        String newMask="?";
                        while(newMask.equalsIgnoreCase("?"))
                        {
                            newMask=mob.session().prompt("Enter new MOB selection mask, or NULL ("+data.lmaskStr+")\n\r: ",data.lmaskStr);
                            if(newMask.equalsIgnoreCase("?"))
                                mob.tell(CMLib.masking().maskHelp("\n","disallow"));
                        }
                        if((newMask.length()==0)||(newMask.equalsIgnoreCase("null")))
                        {
                            data.lmaskStr="";
                            data.lmaskV=null;
                            data.rate=0.0;
                            mob.tell("Mask removed.");
                        }
                        else
                        {
                            data.lmaskStr=newMask;
                            data.lmaskV=CMLib.masking().maskCompile(newMask);
                        }
                        CMLib.database().DBUpdateItem("CATALOG_ITEMS",(Item)E);
                        mob.tell("Item '"+E.Name()+" has been updated.");
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
				    if(!catalog(R,mob,thisThang))
				        return false;
				}
				else
					mob.tell("You don't see '"+ID+"' here!");
			}
		}
		else
			mob.tell("Catalog huh? Try CATALOG LIST (MOBS/ITEMS) (MASK), CATALOG [mob/item name], CATALOG DELETE [mob/item name], CATALOG EDIT [item name].");
		return false;
	}
	
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CATALOG");}
}
