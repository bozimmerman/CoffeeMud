package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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


import java.io.IOException;
import java.util.*;
import java.util.regex.*;

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
public class RawCMaterial extends StdLibrary implements MaterialLibrary
{
    public String ID(){return "RawCMaterial";}
 
	public int getRandomResourceOfMaterial(int material)
	{
		material=material&RawMaterial.MATERIAL_MASK;
		if((material<0)||(material>=RawMaterial.MATERIAL_DESCS.length))
			return -1;
		int d=CMLib.dice().roll(1,RawMaterial.RESOURCE_DATA.length,0);
		while(d>0)
		for(int i=0;i<RawMaterial.RESOURCE_DATA.length;i++)
			if((RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK)==material)
				if((--d)==0)
					return RawMaterial.RESOURCE_DATA[i][0];
		return -1;
	}

	public boolean quickDestroy(Item I)
	{
		if(I==null) return false;
		if(I.owner() instanceof MOB)
			((MOB)I.owner()).delInventory(I);
		else
		if(I.owner() instanceof Room)
			((Room)I.owner()).delItem(I);
		I.setOwner(null);
		I.destroy();
		return true;
	}
	
	public boolean rebundle(Item item)
	{
		if((item==null)||(item.amDestroyed())) return false;
		Vector found=new Vector();
		found.addElement(item);
		Item I=null;
		Environmental owner=item.owner();
	    long lowestNonZeroFoodNumber=Long.MAX_VALUE;
		if(owner instanceof Room)
		{
			Room R=(Room)item.owner();
		    for(int i=0;i<R.numItems();i++)
		    {
		        I=R.fetchItem(i);
				if((I instanceof RawMaterial)
				&&(I.material()==item.material())
			    &&(I!=item)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().enchanted(I))
				&&(I.container()==item.container()))
					found.addElement(I);
		    }
		}
		else
		if(owner instanceof MOB)
		{
			MOB M=(MOB)item.owner();
		    for(int i=0;i<M.inventorySize();i++)
		    {
		        I=M.fetchInventory(i);
				if((I instanceof RawMaterial)
				&&(I.material()==item.material())
			    &&(I!=item)
				&&(!CMLib.flags().isOnFire(I))
				&&(!CMLib.flags().enchanted(I))
				&&(I.container()==item.container()))
					found.addElement(I);
		    }
		}
		else
			return false;
		if(found.size()<2) return false;
		Item bundle=null;
		int maxFound=1;
		int totalWeight=0;
		int totalValue=0;
		int totalNourishment=0;
		int totalThirstHeld=0;
		int totalThirstRemain=0;
		for(int i=0;i<found.size();i++)
		{
			I=(Item)found.elementAt(i);
			int weight=I.envStats().weight();
			totalWeight+=weight;
			totalValue+=item.value();
			if(weight>maxFound)
			{
				maxFound=weight;
				bundle=(Item)found.elementAt(i);
			}
		    if((I instanceof Food)
		    &&(((Food)I).decayTime()>0)
		    &&(((Food)I).decayTime()<lowestNonZeroFoodNumber))
		        lowestNonZeroFoodNumber=((Food)I).decayTime();
		    if(I instanceof Food)
		    	totalNourishment+=((Food)I).nourishment();
		    if(I instanceof Drink)
		    {
		    	totalThirstHeld+=((Drink)I).liquidHeld();
		    	totalThirstRemain+=((Drink)I).liquidRemaining();
		    }
		}
		if(bundle==null) bundle=item;
		found.removeElement(bundle);
	    if(lowestNonZeroFoodNumber==Long.MAX_VALUE) lowestNonZeroFoodNumber=0;
	    Hashtable foundAblesH=new Hashtable();
	    Ability A=null;
		for(int i=0;i<found.size();i++)
		{
			I=(Item)found.elementAt(i);
		    for(int a=0;a<I.numEffects();a++)
		    {
		        A=I.fetchEffect(a);
		        if((A!=null)
		        &&(!A.canBeUninvoked())
		        &&(!foundAblesH.containsKey(A.ID())))
		            foundAblesH.put(A.ID(),A);
		    }
		}
		bundle.setName("a "+totalWeight+"# "+RawMaterial.RESOURCE_DESCS[bundle.material()&RawMaterial.RESOURCE_MASK].toLowerCase()+" bundle");
		bundle.setDisplayText(bundle.name()+" is here.");
		bundle.baseEnvStats().setWeight(totalWeight);
		bundle.setBaseValue(totalValue);
		if(bundle instanceof Food)
		    ((Food)bundle).setNourishment(totalNourishment);
		if(bundle instanceof Drink)
		{
		    ((Drink)bundle).setLiquidHeld(totalThirstHeld);
		    ((Drink)bundle).setLiquidRemaining(totalThirstRemain);
		}
		if(bundle instanceof Food)
		    ((Food)bundle).setDecayTime(lowestNonZeroFoodNumber);
		if(bundle.owner()!=owner)
		{
			if(owner instanceof Room)((Room)owner).bringItemHere(bundle,Item.REFUSE_PLAYER_DROP,false);
			if(owner instanceof MOB)((MOB)owner).giveItem(bundle);
		}
		for(Enumeration e=foundAblesH.keys();e.hasMoreElements();)
		{
			A=(Ability)foundAblesH.get(e.nextElement());
			if(bundle.fetchEffect(A.ID())==null)
				bundle.addNonUninvokableEffect((Ability)A.copyOf());
		}
		for(int i=0;i<found.size();i++)
			((RawMaterial)found.elementAt(i)).quickDestroy();
		Room R=CMLib.map().roomLocation(bundle);
		if(R!=null) R.recoverRoomStats();
		return true;
	}
	
    public Environmental unbundle(Item I, int number)
    {
        if((I instanceof PackagedItems)
        &&(I.container()==null)
        &&(!CMLib.flags().isOnFire(I)))
        {
            if(number<=0) number=((PackagedItems)I).numberOfItemsInPackage();
            if(number<=0) number=1;
            Environmental owner=I.owner();
            Vector parts=((PackagedItems)I).unPackage(number);
            if(parts.size()==0) return I;
            for(int p=0;p<parts.size();p++)
            {
                I=(Item)parts.elementAt(p);
                if(owner instanceof Room)
                    ((Room)owner).addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
                else
                if(owner instanceof MOB)
                    ((MOB)owner).addInventory(I);
            }
            return I;
        }
        else
        if((I instanceof RawMaterial)
        &&(I.container()==null)
        &&(!CMLib.flags().isOnFire(I))
        &&(!CMLib.flags().enchanted(I)))
        {
            Ability rott=I.fetchEffect("Poison_Rotten");
            if(I.baseEnvStats().weight()>1)
            {
                Environmental owner=I.owner();
                I.baseEnvStats().setWeight(I.baseEnvStats().weight());
                Environmental E=null;
                for(int x=0;x<I.baseEnvStats().weight();x++)
                {
                    E=makeResource(I.material(),-1,true);
                    if(E instanceof Item)
                    {
                        if((E instanceof Food)&&(I instanceof Food))
                            ((Food)E).setDecayTime(((Food)I).decayTime());
                        if(rott!=null)
                            E.addNonUninvokableEffect((Ability)rott.copyOf());
                        if(owner instanceof Room)
                            ((Room)owner).addItemRefuse((Item)E,Item.REFUSE_PLAYER_DROP);
                        else
                        if(owner instanceof MOB)
                            ((MOB)owner).addInventory((Item)E);
                    }
                    else
                        E=null;
                }
                I.destroy();
                return E;
            }
            return I;
        }
        return null;
    }
    
	public int getMaterialRelativeInt(String s)
	{
		for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
			if(s.equalsIgnoreCase(RawMaterial.MATERIAL_DESCS[i]))
				return i;
		return -1;
	}
    public int getMaterialCode(String s, boolean exact)
    {
        for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
            if(s.equalsIgnoreCase(RawMaterial.MATERIAL_DESCS[i]))
                return i<<8;
    	if(exact) return -1;
		s=s.toUpperCase();
		for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
			if(RawMaterial.MATERIAL_DESCS[i].startsWith(s)||s.startsWith(RawMaterial.MATERIAL_DESCS[i]))
				return i<<8;
        return -1;
    }
    public int getResourceCode(String s, boolean exact)
	{
		for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
		{
			if(s.equalsIgnoreCase(RawMaterial.RESOURCE_DESCS[i]))
				return RawMaterial.RESOURCE_DATA[i][0];
		}
    	if(exact) return -1;
		s=s.toUpperCase();
		for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
			if(RawMaterial.RESOURCE_DESCS[i].startsWith(s)||s.startsWith(RawMaterial.RESOURCE_DESCS[i]))
				return RawMaterial.RESOURCE_DATA[i][0];
		return -1;
	}
	
	public Environmental makeResource(int myResource, int localeCode, boolean noAnimals)
	{
		if(myResource<0)
			return null;
		int material=(myResource&RawMaterial.MATERIAL_MASK);
        
		Item I=null;
		String name=RawMaterial.RESOURCE_DESCS[myResource&RawMaterial.RESOURCE_MASK].toLowerCase();
		if(!noAnimals)
		{
			if((myResource==RawMaterial.RESOURCE_WOOL)
			||(myResource==RawMaterial.RESOURCE_FEATHERS)
			||(myResource==RawMaterial.RESOURCE_SCALES)
			||(myResource==RawMaterial.RESOURCE_HIDE)
			||(myResource==RawMaterial.RESOURCE_FUR))
			   material=RawMaterial.MATERIAL_LEATHER;
			for(int i=0;i<RawMaterial.FISHES.length;i++)
				if(RawMaterial.FISHES[i]==myResource)
				{ material=RawMaterial.MATERIAL_VEGETATION; break;}
			if((material==RawMaterial.MATERIAL_LEATHER)
			||(material==RawMaterial.MATERIAL_FLESH))
			{
				switch(myResource)
				{
				case RawMaterial.RESOURCE_MUTTON:
				case RawMaterial.RESOURCE_WOOL:
					return CMClass.getMOB("Sheep");
				case RawMaterial.RESOURCE_LEATHER:
				case RawMaterial.RESOURCE_HIDE:
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
					case 2:
					case 3: return CMClass.getMOB("Cow");
					case 4: return CMClass.getMOB("Bull");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("Doe");
					case 8:
					case 9:
					case 10: return CMClass.getMOB("Buck");
					}
					break;
				case RawMaterial.RESOURCE_PORK:
					return CMClass.getMOB("Pig");
				case RawMaterial.RESOURCE_FUR:
				case RawMaterial.RESOURCE_MEAT:
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Wolf");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("Buffalo");
					case 8:
					case 9: return CMClass.getMOB("BrownBear");
					case 10: return CMClass.getMOB("BlackBear");
					}
					break;
				case RawMaterial.RESOURCE_SCALES:
					switch(CMLib.dice().roll(1,10,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Lizard");
					case 5:
					case 6:
					case 7: return CMClass.getMOB("GardenSnake");
					case 8:
					case 9: return CMClass.getMOB("Cobra");
					case 10: return CMClass.getMOB("Python");
					}
					break;
				case RawMaterial.RESOURCE_POULTRY:
				case RawMaterial.RESOURCE_EGGS:
					return CMClass.getMOB("Chicken");
				case RawMaterial.RESOURCE_BEEF:
					switch(CMLib.dice().roll(1,5,0))
					{
					case 1:
					case 2:
					case 3:
					case 4: return CMClass.getMOB("Cow");
					case 5: return CMClass.getMOB("Bull");
					}
					break;
				case RawMaterial.RESOURCE_FEATHERS:
					switch(CMLib.dice().roll(1,4,0))
					{
					case 1: return CMClass.getMOB("WildEagle");
					case 2: return CMClass.getMOB("Falcon");
					case 3: return CMClass.getMOB("Chicken");
					case 4: return CMClass.getMOB("Parakeet");
					}
					break;
				}
			}
        }
		switch(material)
		{
		case RawMaterial.MATERIAL_FLESH:
			I=CMClass.getItem("GenFoodResource");
			break;
		case RawMaterial.MATERIAL_VEGETATION:
		{
			if(myResource==RawMaterial.RESOURCE_VINE)
				I=CMClass.getItem("GenResource");
			else
			{
				I=CMClass.getItem("GenFoodResource");
				if(myResource==RawMaterial.RESOURCE_HERBS)
					((Food)I).setNourishment(1);
			}
			break;
		}
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_ENERGY:
		{
			I=CMClass.getItem("GenLiquidResource");
			break;
		}
		case RawMaterial.MATERIAL_LEATHER:
		case RawMaterial.MATERIAL_CLOTH:
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_WOODEN:
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_PLASTIC:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_PRECIOUS:
		{
			I=CMClass.getItem("GenResource");
			break;
		}
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		{
			I=CMClass.getItem("GenResource");
			if((myResource!=RawMaterial.RESOURCE_ADAMANTITE)
			&&(myResource!=RawMaterial.RESOURCE_BRASS)
			&&(myResource!=RawMaterial.RESOURCE_BRONZE)
			&&(myResource!=RawMaterial.RESOURCE_STEEL))
				name=name+" ore";
			break;
		}
		}
		if(I!=null)
		{
			I.setMaterial(myResource);
			if(I instanceof Drink)
				((Drink)I).setLiquidType(myResource);
			I.setBaseValue(RawMaterial.RESOURCE_DATA[myResource&RawMaterial.RESOURCE_MASK][1]);
			I.baseEnvStats().setWeight(1);
			if(I instanceof RawMaterial)
				((RawMaterial)I).setDomainSource(localeCode);
			if(I instanceof Drink)
				I.setName("some "+name);
			else
				I.setName("a pound of "+name);
			I.setDisplayText("some "+name+" sits here.");
			I.setDescription("");
			I.recoverEnvStats();
			return I;
		}
		return null;
	}
	
	public Item makeItemResource(int type)
	{
		Item I=null;
		String name=RawMaterial.RESOURCE_DESCS[type&RawMaterial.RESOURCE_MASK].toLowerCase();
		if(((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		||((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		if(I instanceof Drink)
			I.setName("some "+name);
		else
			I.setName("a pound of "+name);
		I.setDisplayText("some "+name+" sits here.");
		I.setDescription("");
		I.setMaterial(type);
		I.setBaseValue(RawMaterial.RESOURCE_DATA[type&RawMaterial.RESOURCE_MASK][1]);
		I.baseEnvStats().setWeight(1);
		I.recoverEnvStats();
		return I;
	}

}
