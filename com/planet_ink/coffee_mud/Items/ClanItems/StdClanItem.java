package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;
import java.util.*;
import java.io.*;


public class StdClanItem extends StdItem implements ClanItem
{
	public String ID(){	return "StdClanItem";}
	public Environmental newInstance(){ return new StdClanItem();}
	protected String myClan="";
	protected int ciType=0;
	public int ciType(){return ciType;}
	public void setCIType(int type){ ciType=type;}	
	public StdClanItem()
	{
		super();

		setName("a clan item");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}
	
	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(StdClanItem.stdExecuteMsg(this,msg))
			super.executeMsg(myHost,msg);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(StdClanItem.stdOkMessage(this,msg))
			return super.okMessage(myHost,msg);
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!StdClanItem.standardTick(this,tickID))
			return false;
		return super.tick(ticking,tickID);
	}
	
	public static boolean standardTick(Tickable ticking, int tickID)
	{
		if(tickID!=Host.TICK_CLANITEM)
			return true;
		if((!(ticking instanceof Item))
		||(!(ticking instanceof ClanItem))
		||(((ClanItem)ticking).clanID().length()==0)
		||(!(((Item)ticking).owner() instanceof MOB)))
			return true;
		
		MOB M=((MOB)((Item)ticking).owner());
		Item I=(Item)ticking;
		if((!M.getClanID().equals(((ClanItem)I).clanID()))
		&&(((ClanItem)I).ciType()!=ClanItem.CI_PROPAGANDA))
		{
			if(M.location()!=null)
			{
				I.unWear();
				M.location().show(M,I,CMMsg.MSG_OK_VISUAL,"<T-NAME> carried by <S-NAME> disintegrates!");
				I.destroy();
				return false;
			}
		}
		else
		if((I.amWearingAt(Item.INVENTORY))
		&&(M.isMonster()))
			I.wearAt(I.rawProperLocationBitmap());
			
		return true;
	}
	
	protected static Vector loadList(StringBuffer str)
	{
		Vector V=new Vector();
		if(str==null) return V;
		Vector V2=new Vector();
		boolean oneComma=false;
		int start=0;
		int longestList=0;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\t')
			{
				V2.addElement(str.substring(start,i));
				start=i+1;
				oneComma=true;
			}
			else
			if((str.charAt(i)=='\n')||(str.charAt(i)=='\r'))
			{
				if(oneComma)
				{
					V2.addElement(str.substring(start,i));
					if(V2.size()>longestList) longestList=V2.size();
					V.addElement(V2);
					V2=new Vector();
				}
				start=i+1;
				oneComma=false;
			}
		}
		if(V2.size()>1)
		{
			if(oneComma)
				V2.addElement(str.substring(start,str.length()));
			if(V2.size()>longestList) longestList=V2.size();
			V.addElement(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=(Vector)V.elementAt(v);
			while(V2.size()<longestList)
				V2.addElement("");
		}
		return V;
	}

	public static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("CLANCRAFTING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"clancraft.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("StdClanItem","Recipes not found!");
			Resources.submitResource("CLANCRAFTING RECIPES",V);
		}
		return V;
	}
	
	public static boolean stdOkMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.tool()==myHost)
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(myHost instanceof ClanItem)
		&&(((ClanItem)myHost).clanID().length()>0))
		{
			MOB targetMOB=(MOB)msg.target();
			if((!targetMOB.getClanID().equals(((ClanItem)myHost).clanID()))
			&&(((ClanItem)myHost).ciType()!=ClanItem.CI_PROPAGANDA))
			{
				msg.source().tell("You cannot give this item to "+targetMOB.name()+".");
				return false;
			}
			else
			if(targetMOB.isMonster())
			{
				Item alreadyHasOne=null;
				for(int i=0;i<targetMOB.inventorySize();i++)
				{
					Item I=(Item)targetMOB.fetchInventory(i);
					if((I!=null)&&(I instanceof ClanItem))
					{ alreadyHasOne=I; break;}
				}
				if(alreadyHasOne!=null)
				{
					msg.source().tell(targetMOB.name()+" already has "+alreadyHasOne.name()+", and cannot have another Clan Item.");
					return false;
				}
			}
		}
		else
		if((msg.amITarget(myHost))
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(((ClanItem)myHost).clanID().length()>0))
		{
			MOB M=(MOB)msg.source();
			if(msg.source().getClanID().length()==0)
			{
				msg.source().tell("You must belong to a clan to take a clan item.");
				return false;
			}
			else
			if((!msg.source().getClanID().equals(((ClanItem)myHost).clanID()))
			&&(((ClanItem)myHost).ciType()!=ClanItem.CI_PROPAGANDA))
			{
				Clan C=Clans.getClan(msg.source().getClanID());
				int relation=-1;
				if(C!=null) 
					relation=C.getClanRelations(((ClanItem)myHost).clanID());
				else
				{
					C=Clans.getClan(((ClanItem)myHost).clanID());
					if(C!=null)
						relation=C.getClanRelations(msg.source().getClanID());
				}
				if(relation!=Clan.REL_WAR)
				{
					msg.source().tell("You must be at war with this clan to take one of their items.");
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean stdExecuteMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(myHost))
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(((ClanItem)myHost).clanID().length()>0))
		{
			MOB M=(MOB)msg.source();
			if((M.getClanID().length()>0)
			&&(!M.getClanID().equals(((ClanItem)myHost).clanID())))
			{
				Clan C=Clans.getClan(M.getClanID());
				
				if(M.location()!=null)
					M.location().show(M,(Item)myHost,CMMsg.MSG_OK_ACTION,"<T-NAME> is destroyed by <S-YOUPOSS> touch!");
				if(C!=null)
				{
					Vector recipes=loadRecipes();
					for(int v=0;v<recipes.size();v++)
					{
						Vector V=(Vector)recipes.elementAt(v);
						if((V.size()>3)&&(Util.s_int((String)V.elementAt(3))==((ClanItem)myHost).ciType()))
						{
							int exp=Util.s_int((String)V.elementAt(6))/2;
							if(exp>0)
							{
								C.setExp(C.getExp()+exp);
								M.tell("Your clan gains "+exp+" experience points for this capture.");
							}
							break;
						}
					}
				}
				((Item)myHost).destroy();
				return false;
			}
		}
		return true;
	}
}
