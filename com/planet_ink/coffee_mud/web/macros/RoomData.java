package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
				return new Long(new String(CMClass.className(I)+"/"+I.name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
		return "";
	}
	
	public static String getItemCode(Vector allitems, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<allitems.size();i++)
			if(allitems.elementAt(i)==I)
				return new Long(new String(CMClass.className(I)+"/"+I.name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
		return "";
	}
	
	public static String getItemCode(MOB M, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<M.inventorySize();i++)
			if(M.fetchInventory(i)==I)
				return new Long(new String(CMClass.className(I)+"/"+I.name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
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
				return new Long(new String(CMClass.className(M)+"/"+M.name()+"/"+M.displayText()).hashCode()<<5).toString()+code;
			else
			if((M2!=null)&&(M2.isEligibleMonster()))
				code++;
		}
		return "";
	}
	
	public static String getMOBCode(Vector mobs, MOB M)
	{
		if(M==null) return "";
		for(int i=0;i<mobs.size();i++)
			if(mobs.elementAt(i)==M)
				return new Long(new String(CMClass.className(M)+"/"+M.name()+"/"+M.displayText()).hashCode()<<5).toString()+i;
		return "";
	}
	
	public static Item getItemFromCode(MOB M, String code)
	{
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
		for(int i=0;i<allmobs.size();i++)
			if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).equals(code))
				return ((MOB)allmobs.elementAt(i));
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<allmobs.size();i++)
			if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).startsWith(code))
				return ((MOB)allmobs.elementAt(i));
		return null;
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
			for(int m=0;m<CMClass.items.size();m++)
			{
				Item I=(Item)CMClass.items.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.armor.size();m++)
			{
				Item I=(Item)CMClass.armor.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.weapons.size();m++)
			{
				Item I=(Item)CMClass.weapons.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.miscMagic.size();m++)
			{
				Item I=(Item)CMClass.miscMagic.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
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
				boolean found=false;
				for(int m=0;m<mobs.size();m++)
				{
					MOB M2=(MOB)mobs.elementAt(m);
					if(M.sameAs(M2))
					{	found=true;	break;	}
				}
				if((!found)&&(M.isEligibleMonster()))
					mobs.addElement((MOB)M.copyOf());
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
					I.setContainer(null);
					I.wearAt(Item.INVENTORY);
					items.addElement(I.copyOf());
				}
			}
		}
		return items;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		if(last.length()==0) return "";
		
		if(!httpReq.getMUD().gameStatusStr().equalsIgnoreCase("OK"))
			return httpReq.getMUD().gameStatusStr();
		
		Room R=null;
		for(int i=0;i<httpReq.cache().size();i++)
		{
			Object O=httpReq.cache().elementAt(i);
			if((O instanceof Room)&&(((Room)O).ID().equals(last)))
				R=(Room)O;
		}
		if(R==null)
		{
			R=CMMap.getRoom(last);
			if(R==null)
				return "No Room?!";
			ExternalPlay.resetRoom(R);
			httpReq.cache().addElement(R);
		}
		
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("NAME"))
		{
			String name=(String)httpReq.getRequestParameters().get("NAME");
			if((name==null)||(name.length()==0))
				name=R.displayText();
			str.append(name);
		}
		if(parms.containsKey("CLASSES"))
		{
			String className=(String)httpReq.getRequestParameters().get("CLASSES");
			if((className==null)||(className.length()==0))
				className=CMClass.className(R);
			Vector sortMe=new Vector();
			for(int r=0;r<CMClass.locales.size();r++)
				sortMe.addElement(CMClass.className(CMClass.locales.elementAt(r)));
			Object[] sorted=(Object[])(new TreeSet(sortMe)).toArray();
			for(int r=0;r<sorted.length;r++)
			{
				String cnam=(String)sorted[r];
				str.append("<OPTION VALUE=\""+cnam+"\"");
				if(className.equals(cnam))
					str.append(" SELECTED");
				str.append(">"+cnam);
			}
		}
				
		str.append(AreaData.affectsNBehaves(R,httpReq,parms));
				
		if(parms.containsKey("DESCRIPTION"))
		{
			String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
			if((desc==null)||(desc.length()==0))
				desc=R.description();
			str.append(desc);
		}
		
		if((parms.containsKey("XGRID"))&&(R instanceof GridLocale))
		{
			String size=(String)httpReq.getRequestParameters().get("XGRID");
			if((size==null)||(size.length()==0))
				size=((GridLocale)R).xSize()+"";
			str.append(size);
		}
		if((parms.containsKey("YGRID"))&&(R instanceof GridLocale))
		{
			String size=(String)httpReq.getRequestParameters().get("YGRID");
			if((size==null)||(size.length()==0))
				size=((GridLocale)R).ySize()+"";
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
			if(httpReq.getRequestParameters().containsKey("MOB1"))
			{
				moblist=mobs;
				for(int i=1;;i++)
				{
					String MATCHING=(String)httpReq.getRequestParameters().get("MOB"+i);
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
					for(int m=0;m<CMClass.MOBs.size();m++)
					{
						MOB M2=(MOB)CMClass.MOBs.elementAt(m);
						if(CMClass.className(M2).equals(MATCHING)
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
					if(M.isEligibleMonster())
						classes.addElement(M);
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
				if(R.isInhabitant(M))
					str.append("<OPTION SELECTED VALUE=\""+getMOBCode(classes,M)+"\">"+M.name()+" ("+CMClass.className(M)+")");
				else
				if(moblist.contains(M))
					str.append("<OPTION SELECTED VALUE=\""+M+"\">"+M.name()+" ("+CMClass.className(M)+")");
				else
					str.append("<OPTION SELECTED VALUE=\""+CMClass.className(M)+"\">"+M.name()+" ("+CMClass.className(M)+")");
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+(i+1)+" VALUE=EDIT ONCLICK=\"EditMOB('"+getMOBCode(classes,M)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddMOB(this);\" NAME=MOB"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new MOB");
			for(int i=0;i<moblist.size();i++)
			{
				MOB M=(MOB)moblist.elementAt(i);
				str.append("<OPTION VALUE=\""+M+"\">"+M.name()+" ("+CMClass.className(M)+")");
			}
			for(int i=0;i<CMClass.MOBs.size();i++)
			{
				MOB M=(MOB)CMClass.MOBs.elementAt(i);
				if(!M.isGeneric())
				str.append("<OPTION VALUE=\""+CMClass.className(M)+"\">"+M.name()+" ("+CMClass.className(M)+")");
			}
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=\"NEW\" ONCLICK=\"AddNewMOB();\">");
			str.append("</TD></TR></TABLE>");
		}

		if(parms.containsKey("ITEMLIST"))
		{
			Vector classes=new Vector();
			Vector itemlist=null;
			if(httpReq.getRequestParameters().containsKey("ITEM1"))
			{
				itemlist=items;
				for(int i=1;;i++)
				{
					String MATCHING=(String)httpReq.getRequestParameters().get("ITEM"+i);
					if(MATCHING==null)
						break;
					else
					{
						Item I2=getItemFromAnywhere(R,MATCHING);
						if(I2!=null)
							classes.addElement(I2);
					}
				}
			}
			else
			{
				for(int m=0;m<R.numItems();m++)
				{
					Item I2=R.fetchItem(m);
					classes.addElement(I2);
				}
				itemlist=contributeItems(classes);
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<classes.size();i++)
			{
				Item I=(Item)classes.elementAt(i);
				str.append("<TR>");
				str.append("<TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				if(R.isContent(I))
					str.append("<OPTION SELECTED VALUE=\""+getItemCode(classes,I)+"\">"+I.name()+" ("+CMClass.className(I)+")"+((I.container()==null)?"":(" in "+I.container().name())));
				else
				if(itemlist.contains(I))
					str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.name()+" ("+CMClass.className(I)+")"+((I.container()==null)?"":(" in "+I.container().name())));
				else
					str.append("<OPTION SELECTED VALUE=\""+CMClass.className(I)+"\">"+I.name()+" ("+CMClass.className(I)+")");
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+getItemCode(classes,I)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
			for(int i=0;i<itemlist.size();i++)
			{
				Item I=(Item)itemlist.elementAt(i);
				str.append("<OPTION VALUE=\""+I+"\">"+I.name()+" ("+CMClass.className(I)+")");
			}
			Vector sortMe=new Vector();
			for(int r=0;r<CMClass.items.size();r++)
				if(!((Item)CMClass.items.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.items.elementAt(r)));
			for(int r=0;r<CMClass.weapons.size();r++)
				if(!((Item)CMClass.weapons.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.weapons.elementAt(r)));
			for(int r=0;r<CMClass.armor.size();r++)
				if(!((Item)CMClass.armor.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.armor.elementAt(r)));
			for(int r=0;r<CMClass.miscMagic.size();r++)
				if(!((Item)CMClass.miscMagic.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.miscMagic.elementAt(r)));
			Object[] sorted=(Object[])(new TreeSet(sortMe)).toArray();
			for(int i=0;i<sorted.length;i++)
				str.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
			str.append("</TD></TR></TABLE>");
		}
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return strstr;
	}
}
