package com.planet_ink.coffee_mud.utils;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class CoffeeUtensils
{
	public static ShopKeeper getShopKeeper(MOB mob)
	{
		if(mob==null) return null;
		if(mob instanceof ShopKeeper) return (ShopKeeper)mob;
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A instanceof ShopKeeper))
				return (ShopKeeper)A;
		}
		return null;
	}

	public static Trap getATrap(Environmental unlockThis)
	{
		Trap theTrap=null;
		int roll=(int)Math.round(Math.random()*100.0);
		if(unlockThis instanceof Exit)
		{
			if(((Exit)unlockThis).hasADoor())
			{
				if(((Exit)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Enter");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Enter");
		}
		else
		if(unlockThis instanceof Container)
		{
			if(((Container)unlockThis).hasALid())
			{
				if(((Container)unlockThis).hasALock())
				{
					if(roll<20)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
					if(roll<80)
						theTrap=(Trap)CMClass.getAbility("Trap_Unlock");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
				else
				{
					if(roll<50)
						theTrap=(Trap)CMClass.getAbility("Trap_Open");
					else
						theTrap=(Trap)CMClass.getAbility("Trap_Get");
				}
			}
			else
				theTrap=(Trap)CMClass.getAbility("Trap_Get");
		}
		else
		if(unlockThis instanceof Item)
			theTrap=(Trap)CMClass.getAbility("Trap_Get");
		return theTrap;
	}

	public static Trap fetchMyTrap(Environmental myThang)
	{
		if(myThang==null) return null;
		for(int a=0;a<myThang.numAffects();a++)
		{
			Ability A=myThang.fetchAffect(a);
			if((A!=null)&&(A instanceof  Trap))
				return (Trap)A;
		}
		return null;
	}

	public static void setTrapped(Environmental myThang, boolean isTrapped)
	{
		Trap t=getATrap(myThang);
		t.setReset(50);
		setTrapped(myThang,t,isTrapped);
	}
	public static void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numAffects();a++)
		{
			Ability A=myThang.fetchAffect(a);
			if((A!=null)&&(A instanceof Trap))
				A.unInvoke();
		}

		if((isTrapped)&&(myThang.fetchAffect(theTrap.ID())==null))
			myThang.addAffect(theTrap);
	}

	public static boolean containsString(String toSrchStr, String srchStr)
	{
		if(srchStr.equalsIgnoreCase("all")) return true;
		if(srchStr.endsWith("$")
		&&(Util.removeColors(toSrchStr.toUpperCase()).endsWith(srchStr.toUpperCase().substring(0,srchStr.length()-1))))
		   return true;
		int x=Util.removeColors(toSrchStr.toUpperCase()).indexOf(srchStr.toUpperCase());
		if(x<0) return false;

		if(x==0)
			return true;
		else
		{
			if(Character.isLetter(Util.removeColors(toSrchStr.toUpperCase()).charAt(x-1)))
			   return false;
			return true;
		}
	}
	
	public static boolean reachableItem(MOB mob, Environmental I)
	{
		if((I==null)||(!(I instanceof Item))) 
			return true;
		if((mob.isMine(I))
		||((mob.riding()!=null)&&((I==mob.riding())
								  ||(((Item)I).owner()==mob.riding())
								  ||(((Item)I).ultimateContainer()==mob.riding()))))
		   return true;
		return false;
	}
	
	public static Environmental fetchEnvironmental(Vector list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;

		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}

		int myOccurrance=occurrance;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||thisThang.name().equalsIgnoreCase(srchStr)
					   ||thisThang.Name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Environmental thisThang=(Environmental)list.elementAt(i);
					if((!(thisThang instanceof Ability))
					&&(thisThang.displayText().length()>0)
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Hashtable list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}

		if(list.get(srchStr)!=null)
			return (Environmental)list.get(srchStr);
		int myOccurrance=occurrance;
		if(exactOnly)
		{
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if(thisThang.ID().equalsIgnoreCase(srchStr)
				||thisThang.Name().equalsIgnoreCase(srchStr)
				||thisThang.name().equalsIgnoreCase(srchStr))
					if((!allFlag)||(thisThang.displayText().length()>0))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		else
		{
			myOccurrance=occurrance;
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
				&&((!allFlag)||(thisThang.displayText().length()>0)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=occurrance;
			for(Enumeration e=list.elements();e.hasMoreElements();)
			{
				Environmental thisThang=(Environmental)e.nextElement();
				if((thisThang.displayText().length()>0)&&(containsString(thisThang.displayText(),srchStr)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
		}
		return null;
	}

	public static Environmental fetchEnvironmental(Environmental[] list, String srchStr, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("the")))
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}

		int myOccurrance=occurrance;
		if(exactOnly)
		{
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if(thisThang!=null)
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					||thisThang.Name().equalsIgnoreCase(srchStr)
					||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
			}
		}
		else
		{
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if(thisThang!=null)
					if((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
			myOccurrance=occurrance;
			for(int i=0;i<list.length;i++)
			{
				Environmental thisThang=(Environmental)list[i];
				if((thisThang!=null)&&(thisThang.displayText().length()>0))
					if(containsString(thisThang.displayText(),srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		return null;
	}

	public static Item fetchAvailableItem(Vector list, String srchStr, Item goodLocation, int wornReqCode, boolean exactOnly)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equalsIgnoreCase("THE")))
		   return null;
		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equalsIgnoreCase("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=Util.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=Util.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		int myOccurrance=occurrance;
		if(exactOnly)
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||(thisThang.Name().equalsIgnoreCase(srchStr))
					   ||(thisThang.name().equalsIgnoreCase(srchStr))))
						if((!allFlag)||(thisThang.displayText().length()>0))
							if((--myOccurrance)<=0)
								return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);

					if((thisThang.container()==goodLocation)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&((containsString(thisThang.name(),srchStr)||containsString(thisThang.Name(),srchStr))
					   &&((!allFlag)||(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=occurrance;
			try
			{
				for(int i=0;i<list.size();i++)
				{
					Item thisThang=(Item)list.elementAt(i);
					boolean beingWorn=!thisThang.amWearingAt(Item.INVENTORY);
					if((thisThang.container()==goodLocation)
					&&(thisThang.displayText().length()>0)
					&&((wornReqCode==Item.WORN_REQ_ANY)||(beingWorn&(wornReqCode==Item.WORN_REQ_WORNONLY))||((!beingWorn)&&(wornReqCode==Item.WORN_REQ_UNWORNONLY)))
					&&(containsString(thisThang.displayText(),srchStr)))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public static void wanderAway(MOB M, boolean mindPCs, boolean andGoHome)
	{
		Room R=M.location();
		if(R==null) return;
		int tries=0;
		while((M.location()==R)&&((++tries)<100)&&((!mindPCs)||(M.location().numPCInhabitants()>0)))
			SaucerSupport.beMobile(M,true,true,false,false,null);
		if((M.getStartRoom()!=null)&&(andGoHome))
			M.getStartRoom().bringMobHere(M,true);
	}

	public static Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
		return null;
	}

	//int attChance=(int)(adjArmor+adjAttack);
	public static boolean normalizeAndRollLess(int score)
	{
		return (Dice.rollPercentage()<normalizeBy5(score));
	}

	public static int normalizeBy5(int score)
	{
		if(score>95)
			return 95;
		else
		if(score<5)
			return 5;
		return score;
	}
	
	public static MOB makeMOBfromCorpse(DeadBody corpse, String type)
	{
		if((type==null)||(type.length()==0))
			type="StdMOB";
		MOB mob=CMClass.getMOB(type);
		if(corpse!=null)
		{
			mob.setName(corpse.name());
			mob.setDisplayText(corpse.displayText());
			mob.setDescription(corpse.description());
			mob.setBaseCharStats(corpse.charStats().cloneCharStats());
			mob.setBaseEnvStats(corpse.baseEnvStats().cloneStats());
			mob.recoverCharStats();
			mob.recoverEnvStats();
			int level=mob.baseEnvStats().level();
			mob.baseState().setHitPoints(Dice.rollHP(level,mob.baseEnvStats().ability()));
			mob.baseState().setMana(mob.baseCharStats().getCurrentClass().getLevelMana(mob));
			mob.baseState().setMovement(mob.baseCharStats().getCurrentClass().getLevelMove(mob));
			mob.recoverMaxState();
			mob.resetToMaxState();
			mob.baseCharStats().getMyRace().startRacing(mob,false);
		}
		return mob;
	}

    public static double memoryUse ( Environmental E, int number )
    {
		double s=-1.0;
		try
		{
            int n = number;
            Object[] objs = new Object[n] ;
            Environmental cl = E;
            Runtime rt = Runtime.getRuntime() ;
			long m0 =rt.totalMemory() - rt.freeMemory() ;
			System.gc() ;
			Thread.sleep( 500 ) ;
            for (int i = 0 ; i < n ; ++i) objs[i] =
                    E=cl.copyOf();
			System.gc() ;
			Thread.sleep( 1000 ) ;
			long m1 =rt.totalMemory() - rt.freeMemory() ;
            long dm = m1 - m0 ;
            s = (double)dm / (double)n ;
			if(s<0.0) return memoryUse(E,number);
		}
		catch(Exception e){return -1;}
		return s;
    }
}

