package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public static Vector mobs=new Vector();
	public static Vector items=new Vector();

	public static String getItemCode(Room R, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<R.numItems();i++)
			if(R.fetchItem(i)==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+i;
		return "";
	}

	public static String getItemCode(Vector allitems, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<allitems.size();i++)
			if(allitems.elementAt(i)==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+i;
        for(int i=0;i<allitems.size();i++)
            if(((Environmental)allitems.elementAt(i)).sameAs(I))
                return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+i;
		return "";
	}

	public static String getItemCode(MOB M, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<M.inventorySize();i++)
			if(M.fetchInventory(i)==I)
				return Long.toString( ( I.ID() + "/" + I.Name() + "/" + I.displayText() ).hashCode() << 5 ) + i;
		return "";
	}

	public static String getMOBCode(Room R, MOB M)
	{
		if(M==null) return "";
		int code=0;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M2=R.fetchInhabitant(i);
			if(M==M2)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + code;
			else
			if((M2!=null)&&(M2.savable()))
				code++;
		}
		return "";
	}

	public static String getMOBCode(Vector mobs, MOB M)
	{
		if(M==null) return "";
		for(int i=0;i<mobs.size();i++)
			if(mobs.elementAt(i)==M)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + i;
		return "";
	}

	public static Item getItemFromCode(MOB M, String code)
	{
        if(M==null) return getItemFromCode(items,code);
		for(int i=0;i<M.inventorySize();i++)
			if(getItemCode(M,M.fetchInventory(i)).equals(code))
				return M.fetchInventory(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<M.inventorySize();i++)
			if(getItemCode(M,M.fetchInventory(i)).startsWith(code))
				return M.fetchInventory(i);
		return null;
	}

	public static Item getItemFromCode(Room R, String code)
	{
        if(R==null) return getItemFromCode(items,code);
		for(int i=0;i<R.numItems();i++)
			if(getItemCode(R,R.fetchItem(i)).equals(code))
				return R.fetchItem(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numItems();i++)
			if(getItemCode(R,R.fetchItem(i)).startsWith(code))
				return R.fetchItem(i);
		return null;
	}

	public static Item getItemFromCode(Vector allitems, String code)
	{
        if(code.startsWith("CATALOG-"))
            return getItemFromCatalog(code);
		for(int i=0;i<allitems.size();i++)
			if(getItemCode(allitems,(Item)allitems.elementAt(i)).equals(code))
				return (Item)allitems.elementAt(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<allitems.size();i++)
			if(getItemCode(allitems,(Item)allitems.elementAt(i)).startsWith(code))
				return (Item)allitems.elementAt(i);
		return null;
	}

	public static MOB getMOBFromCode(Room R, String code)
	{
        if(R==null) return getMOBFromCode(mobs,code);
		for(int i=0;i<R.numInhabitants();i++)
			if(getMOBCode(R,R.fetchInhabitant(i)).equals(code))
				return R.fetchInhabitant(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numInhabitants();i++)
			if(getMOBCode(R,R.fetchInhabitant(i)).startsWith(code))
				return R.fetchInhabitant(i);
		return null;
	}

	public static MOB getMOBFromCode(Vector allmobs, String code)
	{
        if(code.startsWith("CATALOG-"))
            return getMOBFromCatalog(code);
		for(int i=0;i<allmobs.size();i++)
			if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).equals(code))
				return ((MOB)allmobs.elementAt(i));
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<allmobs.size();i++)
			if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).startsWith(code))
				return ((MOB)allmobs.elementAt(i));
		return null;
	}


	public static MOB getMOBFromCatalog(String MATCHING)
	{
        if(!MATCHING.startsWith("CATALOG-"))
            return null;
        MOB M2=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
        if(M2!=null)
        {
            M2=(MOB)M2.copyOf();
            CMLib.catalog().changeCatalogUsage(M2,true);
        }
        return M2;
	}


    public static Item getItemFromCatalog(String MATCHING)
    {
        if(!MATCHING.startsWith("CATALOG-"))
            return null;
        Item I=CMLib.catalog().getCatalogItem(MATCHING.substring(8));
        if(I!=null)
        {
            I=(Item)I.copyOf();
            CMLib.catalog().changeCatalogUsage(I,true);
        }
        return I;
    }

    public static String getAppropriateCode(Environmental E, Environmental RorM, Vector classes, Vector list)
    {
        if(((RorM instanceof Room)&&(((Room)RorM).isHere(E)))
        ||((RorM instanceof MOB)&&(((MOB)RorM).isMine(E))))
            return (E instanceof Item)?getItemCode(classes,(Item)E):getMOBCode(classes,(MOB)E);
        else
        if(list.contains(E))
            return ""+E;
        else
        if(CMLib.flags().isCataloged(E))
            return "CATALOG-"+E.Name();
        return E.ID();
    }


	public static Item getItemFromAnywhere(Object allitems, String MATCHING)
	{
		if(isAllNum(MATCHING))
		{
			if(allitems instanceof Room)
				return getItemFromCode((Room)allitems,MATCHING);
			else
			if(allitems instanceof MOB)
				return getItemFromCode((MOB)allitems,MATCHING);
			else
			if(allitems instanceof Vector)
				return getItemFromCode((Vector)allitems,MATCHING);
		}
		else
        if(MATCHING.startsWith("CATALOG-"))
            return getItemFromCatalog(MATCHING);
        else
		if(MATCHING.indexOf("@")>0)
		{
			for(int m=0;m<items.size();m++)
			{
				Item I2=(Item)items.elementAt(m);
				if(MATCHING.equals(""+I2))
					return I2;
			}
		}
		else
		{
			Item I=CMClass.getItem(MATCHING);
			if((I!=null)&&(!(I instanceof ArchonOnly))) return I;
		}
		return null;
	}

    public static MOB getReferenceMOB(MOB M)
    {
        if(M==null) return null;
        for(int m=0;m<mobs.size();m++)
        {
            MOB M2=(MOB)mobs.elementAt(m);
            if(M.sameAs(M2)) return M2;
        }
        return null;
    }

    public static Item getReferenceItem(Item I)
    {
        if(I==null) return null;
        for(int i=0;i<items.size();i++)
        {
            Item I2=(Item)mobs.elementAt(i);
            if(I.sameAs(I2)) return I2;
        }
        return null;
    }

	public static Vector contributeMOBs(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			MOB M=(MOB)inhabs.elementAt(i);
			if(M.isGeneric())
			{
				if((getReferenceMOB(M)==null)&&(M.savable()))
				{
					MOB M3=(MOB)M.copyOf();
					mobs.addElement(M3);
					for(int i3=0;i3<M3.inventorySize();i3++)
					{
						Item I3=M3.fetchInventory(i3);
						if(I3!=null) I3.stopTicking();
					}
				}
			}
		}
		return mobs;
	}

	public static boolean isAllNum(String str)
	{
		if(str.length()==0) return false;
		for(int c=0;c<str.length();c++)
			if((!Character.isDigit(str.charAt(c)))
			&&(str.charAt(c)!='-'))
				return false;
		return true;
	}

	public static Vector contributeItems(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			Item I=(Item)inhabs.elementAt(i);
			if(I.isGeneric())
			{
				boolean found=false;
				for(int i2=0;i2<items.size();i2++)
				{
					Item I2=(Item)items.elementAt(i2);
					if(I.sameAs(I2))
					{	found=true;	break;	}
				}
				if(!found)
				{
					Item I2=(Item)I.copyOf();
                    I2.setContainer(null);
                    I2.wearAt(Wearable.IN_INVENTORY);
					items.addElement(I2);
					I2.stopTicking();
				}
			}
		}
		return items;
	}


	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		if(last.length()==0) return "";

		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=CMLib.map().getRoom(last);
			if(R==null)
				return "No Room?!";
			CMLib.map().resetRoom(R);
			httpReq.getRequestObjects().put(last,R);
		}
    	synchronized(("SYNC"+R.roomID()).intern())
    	{
    		R=CMLib.map().getRoom(R);

			StringBuffer str=new StringBuffer("");
			if(parms.containsKey("NAME"))
			{
				String name=httpReq.getRequestParameter("NAME");
				if((name==null)||(name.length()==0))
					name=R.displayText();
				str.append(name);
			}
			if(parms.containsKey("CLASSES"))
			{
				String className=httpReq.getRequestParameter("CLASSES");
				if((className==null)||(className.length()==0))
					className=CMClass.classID(R);
				Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-LOCALES");
				if(sorted==null)
				{
					Vector sortMe=new Vector();
					for(Enumeration l=CMClass.locales();l.hasMoreElements();)
						sortMe.addElement(CMClass.classID(l.nextElement()));
					sorted=(new TreeSet(sortMe)).toArray();
					Resources.submitResource("MUDGRINDER-LOCALES",sorted);
				}
				for(int r=0;r<sorted.length;r++)
				{
					String cnam=(String)sorted[r];
					str.append("<OPTION VALUE=\""+cnam+"\"");
					if(className.equals(cnam))
						str.append(" SELECTED");
					str.append(">"+cnam);
				}
			}

			str.append(AreaData.affectsNBehaves(R,httpReq,parms,1));
			if(parms.containsKey("IMAGE"))
			{
				String name=httpReq.getRequestParameter("IMAGE");
				if((name==null)||(name.length()==0))
					name=R.rawImage();
				str.append(name);
			}
			if(parms.containsKey("DESCRIPTION"))
			{
				String desc=httpReq.getRequestParameter("DESCRIPTION");
				if((desc==null)||(desc.length()==0))
					desc=R.description();
				str.append(desc);
			}
			if((parms.containsKey("XGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getRequestParameter("XGRID");
				if((size==null)||(size.length()==0))
					size=((GridLocale)R).xGridSize()+"";
				str.append(size);
			}
			if((parms.containsKey("YGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getRequestParameter("YGRID");
				if((size==null)||(size.length()==0))
					size=((GridLocale)R).yGridSize()+"";
				str.append(size);
			}
			if(parms.containsKey("ISGRID"))
			{
				if(R instanceof GridLocale)
					return "true";
				return "false";
			}
			if(parms.containsKey("MOBLIST"))
			{
				Vector classes=new Vector();
				Vector moblist=null;
				if(httpReq.isRequestParameter("MOB1"))
				{
					moblist=mobs;
					for(int i=1;;i++)
					{
						String MATCHING=httpReq.getRequestParameter("MOB"+i);
						if(MATCHING==null)
							break;
						else
						if(isAllNum(MATCHING))
						{
							MOB M2=getMOBFromCode(R,MATCHING);
							if(M2!=null)
								classes.addElement(M2);
						}
						else
						if(MATCHING.startsWith("CATALOG-"))
						{
					        MOB M=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
					        if(M!=null)
					        {
					            M=(MOB)M.copyOf();
					            CMLib.catalog().changeCatalogUsage(M,true);
                                classes.addElement(M);
					        }
						}
						else
						if(MATCHING.indexOf("@")>0)
						{
							for(int m=0;m<moblist.size();m++)
							{
								MOB M2=(MOB)moblist.elementAt(m);
								if(MATCHING.equals(""+M2))
								{	classes.addElement(M2);	break;	}
							}
						}
						else
						for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
						{
							MOB M2=(MOB)m.nextElement();
							if(CMClass.classID(M2).equals(MATCHING)
							   &&(!M2.isGeneric()))
							{	classes.addElement(M2.copyOf()); break;	}
						}
					}
				}
				else
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
	                    if(M!=null)
	                    {
	                    	CMLib.catalog().updateCatalogIntegrity(M);
							if(M.savable())
								classes.addElement(M);
	                    }
					}
					moblist=contributeMOBs(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				for(int i=0;i<classes.size();i++)
				{
					MOB M=(MOB)classes.elementAt(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelMOB(this);\" NAME=MOB"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					String code=getAppropriateCode(M,R,classes,moblist);
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+M.Name()+" ("+M.ID()+")");
					str.append("</SELECT>");
					str.append("</TD>");
					str.append("<TD WIDTH=10%>");
                    if(!CMLib.flags().isCataloged(M))
    					str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+(i+1)+" VALUE=EDIT ONCLICK=\"EditMOB('"+getMOBCode(classes,M)+"');\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddMOB(this);\" NAME=MOB"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new MOB");
				for(int i=0;i<moblist.size();i++)
				{
					MOB M=(MOB)moblist.elementAt(i);
					str.append("<OPTION VALUE=\""+M+"\">"+M.Name()+" ("+M.ID()+")");
				}
				StringBuffer mlist=(StringBuffer)Resources.getResource("MUDGRINDER-MOBLIST");
				if(mlist==null)
				{
					mlist=new StringBuffer("");
					for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
					{
						MOB M=(MOB)m.nextElement();
						if(!M.isGeneric())
							mlist.append("<OPTION VALUE=\""+M.ID()+"\">"+M.Name()+" ("+M.ID()+")");
					}
					Resources.submitResource("MUDGRINDER-MOBLIST",mlist);
				}
                str.append(mlist);
				str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
				String[] names=CMLib.catalog().getCatalogMobNames();
				for(int m=0;m<names.length;m++)
				    str.append("<OPTION VALUE=\"CATALOG-"+names[m]+"\">"+names[m]);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=\"NEW\" ONCLICK=\"AddNewMOB();\">");
				str.append("</TD></TR></TABLE>");
			}

			if(parms.containsKey("ITEMLIST"))
			{
				Vector classes=new Vector();
				Vector containers=new Vector();
				Vector beingWorn=new Vector();
				Vector itemlist=null;
				if(httpReq.isRequestParameter("ITEM1"))
				{
					itemlist=items;
					Vector cstrings=new Vector();
					for(int i=1;;i++)
					{
						String MATCHING=httpReq.getRequestParameter("ITEM"+i);
                        String WORN=httpReq.getRequestParameter("ITEMWORN"+i);
						if(MATCHING==null) break;
						Item I2=getItemFromAnywhere(R,MATCHING);
						if(I2!=null)
						{
							classes.addElement(I2);
                            beingWorn.addElement(Boolean.valueOf((WORN!=null)&&(WORN.equalsIgnoreCase("on"))));
                            String CONTAINER=httpReq.getRequestParameter("ITEMCONT"+i);
                            cstrings.addElement((CONTAINER==null)?"":CONTAINER);
                        }
                    }
                    for(int i=0;i<cstrings.size();i++)
                    {
                        String CONTAINER=(String)cstrings.elementAt(i);
                        Item C2=null;
                        if(CONTAINER.length()>0)
                            C2=(Item)CMLib.english().fetchEnvironmental(classes,CONTAINER,true);
                        containers.addElement((C2!=null)?(Object)C2:"");
                    }
				}
				else
				{
					for(int m=0;m<R.numItems();m++)
					{
						Item I2=R.fetchItem(m);
                        if(I2!=null)
                        {
                        	CMLib.catalog().updateCatalogIntegrity(I2);
    						classes.addElement(I2);
    						containers.addElement((I2.container()==null)?"":(Object)I2.container());
    						beingWorn.addElement(Boolean.valueOf(!I2.amWearingAt(Wearable.IN_INVENTORY)));
                        }
					}
					itemlist=contributeItems(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				for(int i=0;i<classes.size();i++)
				{
					Item I=(Item)classes.elementAt(i);
					Item C=(classes.contains(containers.elementAt(i))?(Item)containers.elementAt(i):null);
					//Boolean W=(Boolean)beingWorn.elementAt(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					String code=getAppropriateCode(I,R,classes,itemlist);
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+I.Name()+" ("+I.ID()+")");
					str.append("</SELECT><BR>");
					str.append("<FONT COLOR=WHITE SIZE=-1>");
					str.append("Container: ");
					str.append("<SELECT NAME=ITEMCONT"+(i+1)+">");
                    str.append("<OPTION VALUE=\"\" "+((C==null)?"SELECTED":"")+">On the ground");
	                for(int i2=0;i2<classes.size();i2++)
	                    if((classes.elementAt(i2) instanceof Container)&&(i2!=i))
    	                {
    	                    Container C2=(Container)classes.elementAt(i2);
    	                    String name=CMLib.english().getContextName(classes,C2);
    	                    str.append("<OPTION "+((C2==C)?"SELECTED":"")+" VALUE=\""+name+"\">"+name+" ("+C2.ID()+")");
    	                }
                    str.append("</SELECT>&nbsp;&nbsp;");
					//str.append("<INPUT TYPE=CHECKBOX NAME=ITEMWORN"+(i+1)+" "+(W.booleanValue()?"CHECKED":"")+">Worn/Wielded");
					str.append("</FONT></TD>");
					str.append("<TD WIDTH=10%>");
					if(!CMLib.flags().isCataloged(I))
					    str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+getItemCode(classes,I)+"');\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
				for(int i=0;i<itemlist.size();i++)
				{
					Item I=(Item)itemlist.elementAt(i);
					str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+" ("+I.ID()+")");
				}
				StringBuffer ilist=(StringBuffer)Resources.getResource("MUDGRINDER-ITEMLIST");
				if(ilist==null)
				{
					ilist=new StringBuffer("");
					Vector sortMe=new Vector();
					CMClass.addAllItemClassNames(sortMe,true,true,false);
					Object[] sorted=(new TreeSet(sortMe)).toArray();
					for(int i=0;i<sorted.length;i++)
						ilist.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
					Resources.submitResource("MUDGRINDER-ITEMLIST",ilist);
				}
                str.append(ilist);
                str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
                String[] names=CMLib.catalog().getCatalogItemNames();
                for(int i=0;i<names.length;i++)
                    str.append("<OPTION VALUE=\"CATALOG-"+names[i]+"\">"+names[i]);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
				str.append("</TD></TR></TABLE>");
			}

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
	        return clearWebMacros(strstr);
    	}
	}
}
