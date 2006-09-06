package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
   Copyright 2000-2006 Bo Zimmerman

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
	public String ID(){	return "StdClanItem";}
	protected String myClan="";
	protected int ciType=0;
	public int ciType(){return ciType;}
	public void setCIType(int type){ ciType=type;}
	private long lastClanCheck=System.currentTimeMillis();
	public StdClanItem()
	{
		super();

		setName("a clan item");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		material=RawMaterial.RESOURCE_OAK;
		recoverEnvStats();
	}

	public String clanID(){return myClan;}
	public void setClanID(String ID){myClan=ID;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        super.executeMsg(myHost,msg);
	    if((System.currentTimeMillis()-lastClanCheck)>TimeManager.MILI_HOUR/2)
	    {
		    if((clanID().length()>0)&&(owner() instanceof MOB)&&(!amDestroyed()))
		    {
		    	if((CMLib.clans().getClan(clanID())==null)
		    	||((!((MOB)owner()).getClanID().equals(clanID()))&&(ciType()!=ClanItem.CI_PROPAGANDA)))
		    	{
		    		Room R=CMLib.map().roomLocation(this);
					unWear();
					removeFromOwnerContainer();
					if(owner()!=R) R.bringItemHere(this,Item.REFUSE_PLAYER_DROP,false);
					if(R!=null)
						R.showHappens(CMMsg.MSG_OK_VISUAL,name()+" is dropped!");
		    	}
		    }
		    lastClanCheck=System.currentTimeMillis();
	    }
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

	public static boolean wearingAClanItem(MOB mob)
	{
		if(mob==null) return false;
		Item I=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			I=mob.fetchInventory(i);
			if((I!=null)
			&&(I instanceof ClanItem)
			&&(!I.amWearingAt(Item.IN_INVENTORY)))
				return true;
		}
		return false;
	}
	
	public static boolean standardTick(Tickable ticking, int tickID)
	{
		if(tickID!=Tickable.TICKID_CLANITEM)
			return true;
		if((!(ticking instanceof Item))
		||(!(ticking instanceof ClanItem))
		||(((ClanItem)ticking).clanID().length()==0)
		||(!(((Item)ticking).owner() instanceof MOB))
		||(((Item)ticking).amDestroyed()))
			return true;

		MOB M=((MOB)((Item)ticking).owner());
		Item I=(Item)ticking;
		if((!M.getClanID().equals(((ClanItem)I).clanID()))
		&&(((ClanItem)I).ciType()!=ClanItem.CI_PROPAGANDA))
		{
			if(M.location()!=null)
			{
				I.unWear();
				I.removeFromOwnerContainer();
				if(I.owner()!=M.location())
					M.location().bringItemHere(I,Item.REFUSE_PLAYER_DROP,false);
				M.location().show(M,I,CMMsg.MSG_OK_VISUAL,"<S-NAME> drop(s) <T-NAME>.");
				return false;
			}
		}
		else
		if((I.amWearingAt(Item.IN_INVENTORY))
		&&(M.isMonster())
		&&(!wearingAClanItem(M))
		&&(CMLib.flags().isInTheGame(M,true)))
		{
			I.setContainer(null);
			I.wearAt(I.rawProperLocationBitmap());
		}
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
		Vector V=(Vector)Resources.getResource("PARSED: clancraft.txt");
		if(V==null)
		{
			StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+"clancraft.txt",null,true).text();
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("StdClanItem","Recipes not found!");
			Resources.submitResource("PARSED: clancrtaft.txt",V);
		}
		return V;
	}

	public static boolean stdOkMessageMOBS(MOB giver, MOB targetMOB, Item myHost)
	{
		if(targetMOB.isMonster())
		{
			Item alreadyHasOne=null;
			for(int i=0;i<targetMOB.inventorySize();i++)
			{
				Item I=targetMOB.fetchInventory(i);
				if((I!=null)
                &&(I instanceof ClanItem)
                &&((((ClanItem)myHost).ciType()!=ClanItem.CI_PROPAGANDA)||(((ClanItem)I).ciType()==ClanItem.CI_PROPAGANDA)))
				{ alreadyHasOne=I; break;}
			}
			if(alreadyHasOne!=null)
			{
				if(giver!=null)
					giver.tell(targetMOB.name()+" already has "+alreadyHasOne.name()+", and cannot have another Clan Item.");
				else
					targetMOB.location().show(targetMOB,null,myHost,CMMsg.MSG_OK_VISUAL,"<S-NAME> can't seem to find the room for <O-NAME>.");
				return false;
			}
			if((((ClanItem)myHost).ciType()==ClanItem.CI_BANNER)
			&&(!CMLib.flags().isMobile(targetMOB)))
			{
				if(giver!=null)
					giver.tell("This item should only be given to those who roam the area.");
				else
					targetMOB.location().show(targetMOB,null,myHost,CMMsg.MSG_OK_VISUAL,"<S-NAME> doesn't seem mobile enough to take <O-NAME>.");
				return false;
			}
			Room startRoom=targetMOB.getStartRoom();
			if((startRoom!=null)
			&&(startRoom.getArea()!=null)
			&&(targetMOB.location()!=null)
			&&(startRoom.getArea()!=targetMOB.location().getArea()))
			{
				LegalBehavior theLaw=CMLib.utensils().getLegalBehavior(startRoom.getArea());
				if((theLaw!=null)&&(theLaw.rulingClan()!=null)&&(theLaw.rulingClan().equals(targetMOB.getClanID())))
				{
					if(giver!=null)
						giver.tell("You can only give a clan item to a conquered mob within the conquered area.");
					else
						targetMOB.location().show(targetMOB,null,myHost,CMMsg.MSG_OK_VISUAL,"<S-NAME> can't seem to take <O-NAME> here.");
					return false;
				}
			}
		}
		return true;
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
			if(!stdOkMessageMOBS(msg.source(),targetMOB,(Item)myHost))
				return false;
		}
		else
		if((msg.amITarget(myHost))
        &&(((ClanItem)myHost).clanID().length()>0)
        &&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)))
		{
			if(msg.source().getClanID().length()==0)
			{
				msg.source().tell("You must belong to a clan to take a clan item.");
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_GET)
			&&(!stdOkMessageMOBS(null,msg.source(),(Item)myHost)))
				return false;
			else
			if((!msg.source().getClanID().equals(((ClanItem)myHost).clanID()))
			&&(((ClanItem)myHost).ciType()!=ClanItem.CI_PROPAGANDA))
			{
				Clan C=CMLib.clans().getClan(msg.source().getClanID());
				int relation=-1;
				if(C!=null)
					relation=C.getClanRelations(((ClanItem)myHost).clanID());
				else
				{
					C=CMLib.clans().getClan(((ClanItem)myHost).clanID());
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
		&&(((ClanItem)myHost).clanID().length()>0)
		&&(((ClanItem)myHost).ciType()!=ClanItem.CI_PROPAGANDA))
		{
			MOB M=msg.source();
			if((M.getClanID().length()>0)
			&&(!M.getClanID().equals(((ClanItem)myHost).clanID())))
			{
				Clan C=CMLib.clans().getClan(M.getClanID());

				if(M.location()!=null)
					M.location().show(M,myHost,CMMsg.MSG_OK_ACTION,"<T-NAME> is destroyed by <S-YOUPOSS> touch!");
				if(C!=null)
				{
					Vector recipes=loadRecipes();
					for(int v=0;v<recipes.size();v++)
					{
						Vector V=(Vector)recipes.elementAt(v);
						if((V.size()>3)&&(CMath.s_int((String)V.elementAt(3))==((ClanItem)myHost).ciType()))
						{
							int exp=CMath.s_int((String)V.elementAt(6))/2;
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
