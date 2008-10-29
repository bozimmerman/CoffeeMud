package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.io.IOException;
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
public class CoffeeUtensils extends StdLibrary implements CMMiscUtils
{
    public String ID(){return "CoffeeUtensils";}
	
	public String niceCommaList(Vector V, boolean andTOrF)
	{
		String id="";
		for(int v=0;v<V.size();v++)
		{
			String s=null;
			if(V.elementAt(v) instanceof Environmental)
				s=((Environmental)V.elementAt(v)).name();
			else
			if(V.elementAt(v) instanceof String)
				s=(String)V.elementAt(v);
			else
				continue;
			if(V.size()==1)
				id+=s;
			else
			if(v==(V.size()-1))
				id+=((andTOrF)?"and ":"or ")+s;
			else
				id+=s+", ";
		}
		return id;
	}
	
	public String getFormattedDate(Environmental E)
	{
	    String date=CMStrings.padRight("Unknown",11);
	    if(E!=null)
	    {
		    TimeClock C=(E instanceof Area)?((Area)E).getTimeObj():((CMLib.map().roomLocation(E)!=null)?CMLib.map().roomLocation(E).getArea().getTimeObj():null);
		    if(C!=null)
		        date=CMStrings.padRight(C.getDayOfMonth()+"-"+C.getMonth()+"-"+C.getYear(),11);
	    }
	    return date;
	}

	public void outfit(MOB mob, Vector items)
	{
		if((mob==null)||(items==null)||(items.size()==0))
			return;
		for(int i=0;i<items.size();i++)
		{
			Item I=(Item)items.elementAt(i);
			if(mob.fetchInventory("$"+I.name()+"$")==null)
			{
				I=(Item)I.copyOf();
				I.text();
				I.recoverEnvStats();
				mob.addInventory(I);
				if(I.whereCantWear(mob)<=0)
					I.wearIfPossible(mob);
				if(((I instanceof Armor)||(I instanceof Weapon))
				&&(I.amWearingAt(Item.IN_INVENTORY)))
					I.destroy();
			}
		}
	}

	public Trap makeADeprecatedTrap(Environmental unlockThis)
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


	public void setTrapped(Environmental myThang, boolean isTrapped)
	{
		Trap t=makeADeprecatedTrap(myThang);
		t.setReset(50);
		setTrapped(myThang,t,isTrapped);
	}
	public void setTrapped(Environmental myThang, Trap theTrap, boolean isTrapped)
	{
		for(int a=0;a<myThang.numEffects();a++)
		{
			Ability A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof Trap))
				A.unInvoke();
		}

		if((isTrapped)&&(myThang.fetchEffect(theTrap.ID())==null))
			myThang.addEffect(theTrap);
	}

	public Trap fetchMyTrap(Environmental myThang)
	{
		if(myThang==null) return null;
        Ability A=null;
		for(int a=0;a<myThang.numEffects();a++)
		{
			A=myThang.fetchEffect(a);
			if((A!=null)&&(A instanceof  Trap))
				return (Trap)A;
		}
		return null;
	}
	public boolean reachableItem(MOB mob, Environmental E)
	{
		if((E==null)||(!(E instanceof Item)))
			return true;
        Item I=(Item)E;
		if((mob.isMine(I))
		||((mob.riding()!=null)&&((I==mob.riding())
								  ||(I.owner()==mob.riding())
								  ||(I.ultimateContainer()==mob.riding())))
        ||(I.owner()==null)
        ||((I.owner() instanceof Room)&&(!((Room)I.owner()).isContent(I))))
		   return true;
		return false;
	}

    public double memoryUse ( Environmental E, int number )
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
                    E=(Environmental)cl.copyOf();
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

	public void extinguish(MOB source, Environmental target, boolean mundane)
	{
		if(target instanceof Room)
		{
			Room R=(Room)target;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null) extinguish(source,M,mundane);
			}
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I!=null) extinguish(source,I,mundane);
			}
			return;
		}
		for(int a=target.numEffects()-1;a>=0;a--)
		{
			Ability A=target.fetchEffect(a);
			if((A!=null)&&((!mundane)||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)))
			{
				if((CMath.bset(A.flags(),Ability.FLAG_HEATING)&&(!mundane))
				||(CMath.bset(A.flags(),Ability.FLAG_FIREBASED))
				||((A.ID().equalsIgnoreCase("Spell_SummonElemental")&&A.text().toUpperCase().indexOf("FIRE")>=0)))
					A.unInvoke();
			}
		}
		if((target instanceof MOB)&&(!mundane))
		{
			MOB tmob=(MOB)target;
			if(tmob.charStats().getMyRace().ID().equals("FireElemental"))
				CMLib.combat().postDeath(source,(MOB)target,null);
			for(int i=0;i<tmob.inventorySize();i++)
			{
				Item I=tmob.fetchInventory(i);
				if(I!=null) extinguish(tmob,I,mundane);
			}
		}
		if((target instanceof Light)&&(((Light)target).isLit()))
		{
			((Light)target).tick(target,Tickable.TICKID_LIGHT_FLICKERS);
			((Light)target).light(false);
		}
	}

	public void roomAffectFully(CMMsg msg, Room room, int dirCode)
	{
		room.send(msg.source(),msg);
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return;
		if(dirCode<0)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(room.getExitInDir(d)==msg.target()){ dirCode=d; break;}
		}
		if(dirCode<0) return;
		Exit pair=room.getPairedExit(dirCode);
		if(pair!=null)
		{
			CMMsg altMsg=null;
			if((msg.targetCode()==CMMsg.MSG_OPEN)&&(pair.isLocked()))
			{
				altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null,CMMsg.MSG_UNLOCK,null);
				pair.executeMsg(msg.source(),altMsg);
			}
			altMsg=CMClass.getMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
			pair.executeMsg(msg.source(),altMsg);
		}
	}

    
    public void recursiveDropMOB(MOB mob,
                                 Room room,
                                 Item thisContainer,
                                 boolean bodyFlag)
    {
        // caller is responsible for recovering any env
        // stat changes!
        
        if(CMLib.flags().isHidden(thisContainer))
            thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
        mob.delInventory(thisContainer);
        thisContainer.unWear();
        if(!bodyFlag) bodyFlag=(thisContainer instanceof DeadBody);
        if(bodyFlag)
        {
            room.addItem(thisContainer);
            thisContainer.setExpirationDate(0);
        }
        else
            room.addItemRefuse(thisContainer,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
        thisContainer.recoverEnvStats();
        boolean nothingDone=true;
        do
        {
            nothingDone=true;
            for(int i=0;i<mob.inventorySize();i++)
            {
                Item thisItem=mob.fetchInventory(i);
                if((thisItem!=null)&&(thisItem.container()==thisContainer))
                {
                    recursiveDropMOB(mob,room,thisItem,bodyFlag);
                    nothingDone=false;
                    break;
                }
            }
        }while(!nothingDone);
    }
    
    
    public MOB getMobPossessingAnother(MOB mob)
    {
        if(mob==null) return null;
        Session S=null;
        MOB M=null;
        for(int s=0;s<CMLib.sessions().size();s++)
        {
            S=CMLib.sessions().elementAt(s);
            if(S!=null)
            {
                M=S.mob();
                if((M!=null)&&(M.soulMate()==mob))
                    return M;
            }
        }
        return null;
    }
    
	
	public boolean armorCheck(MOB mob, int allowedArmorLevel)
	{
		if(allowedArmorLevel==CharClass.ARMOR_ANY) return true;

		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if(I==null) break;
			if((!I.amWearingAt(Item.IN_INVENTORY))
			&&((I instanceof Armor)||(I instanceof Shield)))
			{
				boolean ok=true;
				switch(I.material()&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_LEATHER:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
					||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
						ok=false;
					break;
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
					||(allowedArmorLevel==CharClass.ARMOR_NONMETAL))
						ok=false;
					break;
				case RawMaterial.MATERIAL_ENERGY:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN))
					   return false;
					break;
				case RawMaterial.MATERIAL_CLOTH:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
					||((allowedArmorLevel==CharClass.ARMOR_VEGAN)
					   &&((I.material()==RawMaterial.RESOURCE_HIDE)
						  ||(I.material()==RawMaterial.RESOURCE_FUR)
						  ||(I.material()==RawMaterial.RESOURCE_FEATHERS)
						  ||(I.material()==RawMaterial.RESOURCE_WOOL))))
						ok=false;
					break;
				case RawMaterial.MATERIAL_PLASTIC:
				case RawMaterial.MATERIAL_WOODEN:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY)
					||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
					||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
						ok=false;
					break;
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_GLASS:
					if((allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_LEATHER)
					||(allowedArmorLevel==CharClass.ARMOR_METALONLY))
						ok=false;
					break;
				case RawMaterial.MATERIAL_FLESH:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||(allowedArmorLevel==CharClass.ARMOR_VEGAN)
					||(allowedArmorLevel==CharClass.ARMOR_CLOTH)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
						ok=false;
					break;
				default:
					if((allowedArmorLevel==CharClass.ARMOR_METALONLY)
					||(allowedArmorLevel==CharClass.ARMOR_OREONLY))
						ok=false;
					break;
				}
				if((!ok)&&((I.rawWornCode()&CharClass.ARMOR_WEARMASK)>0))
					return false;
			}
		}
		return true;
	}
    
    
    public String wornList(long wornCode)
    {
        StringBuffer buf=new StringBuffer("");
        for(int wornNum=0;wornNum<Item.WORN_DESCS.length-1;wornNum++)
        {
            if(CMath.isSet(wornCode,wornNum))
                buf.append(Item.WORN_DESCS[wornNum+1]+", ");
        }
        String buff=buf.toString();
        if(buff.endsWith(", ")) buff=buff.substring(0,buff.length()-2).trim();
        return buff;
    }
    
    public int getWornCode(String name)
    {
        int wornNum=0;
        name=name.toLowerCase().trim();
        for(;wornNum<Item.WORN_DESCS.length-1;wornNum++)
            if(Item.WORN_DESCS[wornNum].endsWith(name))
                return wornNum;
        return -1;
    }
    
    public Vector getDeadBodies(Environmental E)
    {
        if(E instanceof DeadBody)
            return CMParms.makeVector(E);
        if(E instanceof Container)
        {
            Vector Bs=new Vector();
            Vector V=((Container)E).getContents();
            for(int v=0;v<V.size();v++)
                Bs.addAll(getDeadBodies((Environmental)V.elementAt(v)));
            return Bs;
        }
        return new Vector();
    }
    
    
    public DVector parseLootPolicyFor(MOB mob)
    {
        if(mob==null) return new DVector(3);
        Vector lootPolicy=(!mob.isMonster())?new Vector():CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_ITEMLOOTPOLICY),true);
        DVector policies=new DVector(3);
        for(int p=0;p<lootPolicy.size();p++)
        {
            String s=((String)lootPolicy.elementAt(p)).toUpperCase().trim();
            if(s.length()==0) continue;
            Vector compiledMask=null;
            int maskDex=s.indexOf("MASK=");
            if(maskDex>=0)
            {
                s=s.substring(0,maskDex).trim();
                compiledMask=CMLib.masking().maskCompile(((String)lootPolicy.elementAt(p)).substring(maskDex+5).trim());
            }
            else
                compiledMask=new Vector();
            Vector<String> parsed=CMParms.parse(s);
            int pct=100;
            for(int x=0;x<parsed.size();x++)
                if(CMath.isInteger((String)parsed.elementAt(x)))
                    pct=CMath.s_int((String)parsed.elementAt(x));
                else
                if(CMath.isPct((String)parsed.elementAt(x)))
                    pct=(int)Math.round(CMath.s_pct((String)parsed.elementAt(x))*100.0);
            int flags=0;
            if(parsed.contains("RUIN")) flags|=CMMiscUtils.LOOTFLAG_RUIN;
            else
            if(parsed.contains("LOSS")) flags|=CMMiscUtils.LOOTFLAG_LOSS;
            if(flags==0) flags|=CMMiscUtils.LOOTFLAG_LOSS;
            if(parsed.contains("WORN")) flags|=CMMiscUtils.LOOTFLAG_WORN;
            else
            if(parsed.contains("UNWORN")) flags|=CMMiscUtils.LOOTFLAG_UNWORN;
            policies.addElement(new Integer(pct),new Integer(flags),compiledMask);
        }
        return policies;
    }
    
    public Item isRuinedLoot(DVector policies, Item I)
    {
        if(I==null) return null;
        if((CMath.bset(I.envStats().disposition(),EnvStats.IS_UNSAVABLE))
        ||(CMath.bset(I.envStats().sensesMask(), EnvStats.SENSE_ITEMNORUIN))
        ||(I instanceof Coins))
            return I;
        if(I.name().toLowerCase().indexOf("ruined ")>=0)
        	return I;
        for(int d=0;d<policies.size();d++)
        {
            if((((Vector)policies.elementAt(d,3)).size()>0)
            &&(!CMLib.masking().maskCheck((Vector)policies.elementAt(d,3),I,true)))
                continue;
            if(CMLib.dice().rollPercentage()>((Integer)policies.elementAt(d,1)).intValue())
                continue;
            int flags=((Integer)policies.elementAt(d,2)).intValue();
            if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_WORN)&&I.amWearingAt(Item.IN_INVENTORY))
                continue;
            else
            if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_UNWORN)&&(!I.amWearingAt(Item.IN_INVENTORY)))
                continue;
            if(CMath.bset(flags,CMMiscUtils.LOOTFLAG_LOSS))
                return null;
            Item I2=CMClass.getItem("GenItem");
            I2.baseEnvStats().setWeight(I.baseEnvStats().weight());
            I2.setName(I.Name());
            I2.setDisplayText(I.displayText());
            I2.setDescription(I2.description());
            I2.recoverEnvStats();
            I2.setMaterial(I.material());
            String ruinDescAdder=null;
            switch(I2.material()&RawMaterial.MATERIAL_MASK)
            {
                case RawMaterial.MATERIAL_LEATHER:
                case RawMaterial.MATERIAL_CLOTH:
                case RawMaterial.MATERIAL_VEGETATION:
                case RawMaterial.MATERIAL_FLESH:
                case RawMaterial.MATERIAL_PAPER:
                    ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is torn and ruined beyond repair."; 
                    break;
                case RawMaterial.MATERIAL_METAL:
                case RawMaterial.MATERIAL_MITHRIL:
                case RawMaterial.MATERIAL_WOODEN:
                    ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is battered and ruined beyond repair."; 
                    break;
                case RawMaterial.MATERIAL_GLASS:
                    ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is shattered and ruined beyond repair."; 
                    break;
                case RawMaterial.MATERIAL_ROCK:
                case RawMaterial.MATERIAL_PRECIOUS:
                case RawMaterial.MATERIAL_PLASTIC:
                    ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is cracked and ruined beyond repair."; 
                    break;
                case RawMaterial.MATERIAL_UNKNOWN:
                case RawMaterial.MATERIAL_ENERGY:
                case RawMaterial.MATERIAL_LIQUID:
                default:
                    ruinDescAdder=CMStrings.capitalizeFirstLetter(I2.name())+" is ruined beyond repair."; 
                    break;
            }
            I2.setDescription(CMStrings.endWithAPeriod(I2.description())+" "+ruinDescAdder);
            String oldName=I2.Name();
            I2.setName(CMLib.english().insertUnColoredAdjective(I2.Name(),"ruined"));
            int x=I2.displayText().toUpperCase().indexOf(oldName.toUpperCase());
            I2.setBaseValue(0);
            if(x>=0)
                I2.setDisplayText(I2.displayText().substring(0,x)+I2.Name()+I2.displayText().substring(x+oldName.length()));
            return I2;
        }
        return I;
    }
    
    public void reloadCharClasses(CharClass oldC)
    {
        for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
        {
            Room room=(Room)e.nextElement();
            for(int i=0;i<room.numInhabitants();i++)
            {
                MOB M=room.fetchInhabitant(i);
                if(M==null) continue;
                for(int c=0;c<M.baseCharStats().numClasses();c++)
                    if(M.baseCharStats().getMyClass(c)==oldC)
                    {
                        M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
                        break;
                    }
                for(int c=0;c<M.charStats().numClasses();c++)
                    if(M.charStats().getMyClass(c)==oldC)
                    {
                        M.charStats().setMyClasses(M.charStats().getMyClassesStr());
                        break;
                    }
            }
            for(e=CMLib.players().players();e.hasMoreElements();)
            {
                MOB M=(MOB)e.nextElement();
                for(int c=0;c<M.baseCharStats().numClasses();c++)
                    if(M.baseCharStats().getMyClass(c)==oldC)
                    {
                        M.baseCharStats().setMyClasses(M.baseCharStats().getMyClassesStr());
                        break;
                    }
                for(int c=0;c<M.charStats().numClasses();c++)
                    if(M.charStats().getMyClass(c)==oldC)
                    {
                        M.charStats().setMyClasses(M.charStats().getMyClassesStr());
                        break;
                    }
            }
        }
    }
    
    public void swapRaces(Race newR, Race oldR)
    {
        for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
        {
            Room room=(Room)e.nextElement();
            for(int i=0;i<room.numInhabitants();i++)
            {
                MOB M=room.fetchInhabitant(i);
                if(M==null) continue;
                if(M.baseCharStats().getMyRace()==oldR)
                    M.baseCharStats().setMyRace(newR);
                if(M.charStats().getMyRace()==oldR)
                    M.charStats().setMyRace(newR);
            }
            for(e=CMLib.players().players();e.hasMoreElements();)
            {
                MOB M=(MOB)e.nextElement();
                if(M.baseCharStats().getMyRace()==oldR)
                    M.baseCharStats().setMyRace(newR);
                if(M.charStats().getMyRace()==oldR)
                    M.charStats().setMyRace(newR);
            }
        }
    }
}

