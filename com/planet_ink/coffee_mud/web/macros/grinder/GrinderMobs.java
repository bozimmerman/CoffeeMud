package com.planet_ink.coffee_mud.web.macros.grinder;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.web.macros.RoomData;

public class GrinderMobs
{
	public static String senses(Environmental E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		E.baseEnvStats().setSensesMask(0);
		for(int d=0;d<EnvStats.sensesNames.length;d++)
		{
			String parm=httpReq.getRequestParameter(EnvStats.sensesNames[d]);
			if((parm!=null)&&(parm.equals("on")))
			   E.baseEnvStats().setSensesMask(E.baseEnvStats().sensesMask()|(1<<d));
		}
		return "";
	}

	public static void happilyAddItem(Item I, MOB M)
	{
		if(I.subjectToWearAndTear())
			I.setUsesRemaining(100);
		I.recoverEnvStats();
		M.addInventory(I);
		M.recoverEnvStats();
		M.recoverCharStats();
		M.recoverMaxState();
	}

	public static String abilities(MOB E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		while(E.numAbilities()>0)
		{
			Ability A=E.fetchAbility(0);
			if(E.fetchAffect(A.ID())!=null)
				E.delAffect(E.fetchAffect(A.ID()));
			E.delAbility(A);
		}
		if(httpReq.isRequestParameter("ABLES1"))
		{
			int num=1;
			String aff=httpReq.getRequestParameter("ABLES"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					Ability B=CMClass.getAbility(aff);
					if(B==null) return "Unknown Ability '"+aff+"'.";
					E.addAbility(B);
					B.autoInvocation(E);
				}
				num++;
				aff=httpReq.getRequestParameter("ABLES"+num);
			}
		}
		return "";
	}

	public static String blessings(Deity E, ExternalHTTPRequests httpReq, Hashtable parms)
	{
		while(E.numBlessings()>0)
		{
			Ability A=E.fetchBlessing(0);
			if(A!=null)
				E.delBlessing(A);
		}
		if(httpReq.isRequestParameter("BLESS1"))
		{
			int num=1;
			String aff=httpReq.getRequestParameter("BLESS"+num);
			while(aff!=null)
			{
				if(aff.length()>0)
				{
					Ability B=CMClass.getAbility(aff);
					if(B==null) return "Unknown Blessing '"+aff+"'.";
					E.addBlessing(B);
				}
				num++;
				aff=httpReq.getRequestParameter("BLESS"+num);
			}
		}
		return "";
	}

	public static String editMob(ExternalHTTPRequests httpReq, Hashtable parms, Room R)
	{
		String mobCode=httpReq.getRequestParameter("MOB");
		if(mobCode==null) return "@break@";

		String newClassID=httpReq.getRequestParameter("CLASSES");

		ExternalPlay.resetRoom(R);

		MOB M=null;
		if(mobCode.equals("NEW"))
			M=CMClass.getMOB(newClassID);
		else
			M=RoomData.getMOBFromCode(R,mobCode);

		if(M==null)
		{
			StringBuffer str=new StringBuffer("No MOB?!");
			str.append(" Got: "+mobCode);
			str.append(", Includes: ");
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M2=R.fetchInhabitant(m);
				if((M2!=null)&&(M2.isEligibleMonster()))
				   str.append(M2.Name()+"="+RoomData.getMOBCode(R,M2));
			}
			return str.toString();
		}
		MOB oldM=M;
		if((newClassID!=null)&&(!newClassID.equals(CMClass.className(M))))
			M=CMClass.getMOB(newClassID);
		M.setStartRoom(R);

		Vector allitems=new Vector();
		while(oldM.inventorySize()>0)
		{
			Item I=oldM.fetchInventory(0);
			allitems.addElement(I);
			oldM.delInventory(I);
		}

		String[] okparms={"NAME","CLASSES","DISPLAYTEXT","DESCRIPTION",
						  " LEVEL"," ABILITY"," REJUV"," MISCTEXT",
						  "RACE","GENDER","HEIGHT","WEIGHT",
						  "SPEED","ATTACK","DAMAGE","ARMOR",
						  "ALIGNMENT","MONEY","ISRIDEABLE","RIDEABLETYPE",
						  "MOBSHELD","ISSHOPKEEPER","SHOPKEEPERTYPE","ISGENERIC",
						  "ISBANKER","COININT","ITEMINT","BANKNAME","SHOPPREJ",
						  "ISDEITY","CLEREQ","CLERIT","WORREQ","WORRIT","BLESSINGS"};
		for(int o=0;o<okparms.length;o++)
		{
			String parm=okparms[o];
			boolean generic=true;
			if(parm.startsWith(" "))
			{
				generic=false;
				parm=parm.substring(1);
			}
			String old=httpReq.getRequestParameter(parm);
			if(old==null) old="";

			if((M.isGeneric()||(!generic)))
			switch(o)
			{
			case 0: // name
				M.setName(old);
				break;
			case 1: // classes
				break;
			case 2: // displaytext
				M.setDisplayText(old);
				break;
			case 3: // description
				M.setDescription(old);
				break;
			case 4: // level
				M.baseEnvStats().setLevel(Util.s_int(old));
				break;
			case 5: // ability;
				M.baseEnvStats().setAbility(Util.s_int(old));
				break;
			case 6: // rejuv;
				M.baseEnvStats().setRejuv(Util.s_int(old));
				break;
			case 7: // misctext
				if(!M.isGeneric())
					M.setMiscText(old);
				break;
			case 8: // race
				M.baseCharStats().setMyRace(CMClass.getRace(old));
				break;
			case 9: // gender
				M.baseCharStats().setStat(CharStats.GENDER,(int)old.charAt(0));
				break;
			case 10: // height
				M.baseEnvStats().setHeight(Util.s_int(old));
				break;
			case 11: // weight;
				M.baseEnvStats().setWeight(Util.s_int(old));
				break;
			case 12: // speed
				double d=Util.s_double(old);
				if(d==0.0) d=1.0;
				M.baseEnvStats().setSpeed(d);
				break;
			case 13: // attack
				M.baseEnvStats().setAttackAdjustment(Util.s_int(old));
				break;
			case 14: // damage
				M.baseEnvStats().setDamage(Util.s_int(old));
				break;
			case 15: // armor
				M.baseEnvStats().setArmor(Util.s_int(old));
				break;
			case 16: // alignment
				M.setAlignment(Util.s_int(old));
				break;
			case 17: // money
				M.setMoney(Util.s_int(old));
				break;
			case 18: // is rideable
				break;
			case 19: // rideable type
				if(M instanceof Rideable)
					((Rideable)M).setRideBasis(Util.s_int(old));
				break;
			case 20: // mobs held
				if(M instanceof Rideable)
					((Rideable)M).setRiderCapacity(Util.s_int(old));
				break;
			case 21: // is shopkeeper
				break;
			case 22: // shopkeeper type
				if(M instanceof ShopKeeper)
					((ShopKeeper)M).setWhatIsSold(Util.s_int(old));
				break;
			case 23: // is generic
				break;
			case 24: // is banker
				break;
			case 25: // coin interest
				if(M instanceof Banker)
					((Banker)M).setCoinInterest(Util.s_double(old));
				break;
			case 26: // item interest
				if(M instanceof Banker)
					((Banker)M).setItemInterest(Util.s_double(old));
				break;
			case 27: // bank name
				if(M instanceof Banker)
					((Banker)M).setBankChain(old);
				break;
			case 28: // shopkeeper prejudices
				if(M instanceof ShopKeeper)
					((ShopKeeper)M).setPrejudiceFactors(old);
				break;
			case 29: // is deity
				break;
			case 30: // cleric requirements
				if(M instanceof Deity)
					((Deity)M).setClericRequirements(old);
				break;
			case 31: // cleric ritual
				if(M instanceof Deity)
					((Deity)M).setClericRitual(old);
				break;
			case 32: // worshipper requirements
				if(M instanceof Deity)
					((Deity)M).setWorshipRequirements(old);
				break;
			case 33: // worshipper ritual
				if(M instanceof Deity)
					((Deity)M).setWorshipRitual(old);
				break;
			}
		}

		if(M.isGeneric())
		{
			String error=GrinderExits.dispositions(M,httpReq,parms);
			if(error.length()>0) return error;
			error=GrinderMobs.senses(M,httpReq,parms);
			if(error.length()>0) return error;
			error=GrinderAreas.doAffectsNBehavs(M,httpReq,parms);
			if(error.length()>0) return error;
			error=GrinderMobs.abilities(M,httpReq,parms);
			if(error.length()>0) return error;
			if(M instanceof Deity)
			{
				error=GrinderMobs.blessings((Deity)M,httpReq,parms);
				if(error.length()>0) return error;
			}

			if(httpReq.isRequestParameter("ITEM1"))
			{
				for(int i=1;;i++)
				{
					String MATCHING=httpReq.getRequestParameter("ITEM"+i);
					if(MATCHING==null)
						break;
					else
					{
						Item I2=RoomData.getItemFromAnywhere(allitems,MATCHING);
						if(I2!=null)
						{
							if(Util.s_int(MATCHING)>0)
								happilyAddItem(I2,M);
							else
								happilyAddItem((Item)I2.copyOf(),M);
						}
					}
				}
			}
			else
				return "No Item Data!";

			if((M instanceof ShopKeeper)
			&&(httpReq.isRequestParameter("SHP1")))
			{
				ShopKeeper K=(ShopKeeper)M;
				Vector inventory=K.getUniqueStoreInventory();
				K.clearStoreInventory();

				int num=1;
				String MATCHING=httpReq.getRequestParameter("SHP"+num);
				String theparm=httpReq.getRequestParameter("SDATA"+num);
				while((MATCHING!=null)&&(theparm!=null))
				{
					if(MATCHING==null)
						break;
					else
					if(Util.s_int(MATCHING)>0)
					{
						Environmental O=(Environmental)inventory.elementAt(Util.s_int(MATCHING)-1);
						if(O!=null)
							K.addStoreInventory(O,Util.s_int(theparm));
					}
					else
					if(MATCHING.indexOf("@")>0)
					{
						Environmental O=null;
						for(int m=0;m<RoomData.mobs.size();m++)
						{
							MOB M2=(MOB)RoomData.mobs.elementAt(m);
							if(MATCHING.equals(""+M2))
							{	O=M2;	break;	}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							K.addStoreInventory(O.copyOf(),Util.s_int(theparm));
					}
					else
					{
						Environmental O=null;
						for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
						{
							MOB M2=(MOB)m.nextElement();
							if(CMClass.className(M2).equals(MATCHING)&&(!M2.isGeneric()))
							{	O=M2.copyOf(); break;	}
						}
						if(O==null)
						for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
						{
							Ability A2=(Ability)a.nextElement();
							if(CMClass.className(A2).equals(MATCHING))
							{	O=A2.copyOf(); break;	}
						}
						if(O==null)
							O=RoomData.getItemFromAnywhere(null,MATCHING);
						if(O!=null)
							K.addStoreInventory(O.copyOf(),Util.s_int(theparm));
					}
					num++;
					MATCHING=httpReq.getRequestParameter("SHP"+num);
					theparm=httpReq.getRequestParameter("SDATA"+num);
				}
			}

			for(int i=0;i<allitems.size();i++)
			{
				Item I=(Item)allitems.elementAt(i);
				if(!M.isMine(I))
				{
					I.setOwner(M);
					I.destroy();
				}
			}
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I.container()!=null)&&(!M.isMine(I.container())))
					I.setContainer(null);
			}
		}

		M.recoverEnvStats();
		M.recoverCharStats();
		M.recoverMaxState();
		M.resetToMaxState();
		M.text();
		if(mobCode.equals("NEW"))
			M.bringToLife(R,true);
		else
		if(M!=oldM)
		{
			oldM.destroy();
			R.delInhabitant(oldM);
			M.bringToLife(R,true);
		}
		R.recoverRoomStats();
		ExternalPlay.DBUpdateMOBs(R);
		httpReq.addRequestParameters("MOB",RoomData.getMOBCode(R,M));
		return "";
	}
}
