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
public class RawCMaterial extends StdLibrary implements MaterialLibrary
{
    public String ID(){return "RawCMaterial";}
 
	public int getRandomResourceOfMaterial(int material)
	{
		material=material&RawMaterial.MATERIAL_MASK;
		if((material<0)||(material>=RawMaterial.MATERIAL_DESCS.length))
			return -1;
		RawMaterial.CODES codes = RawMaterial.CODES.instance();
		int countDown=codes.total();
		int rscIndex=CMLib.dice().roll(1,countDown,-1);
		int rsc=0;
		while(--countDown>=0)
		{
			rsc=codes.get(rscIndex);
			if((rsc&RawMaterial.MATERIAL_MASK)==material)
				return rsc;
			if((--rscIndex)<0)
				rscIndex=codes.total()-1;
		}
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
		if((item==null)||(item.amDestroyed())) 
            return false;
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
				&&(I.container()==item.container())
				&&(I.rawSecretIdentity().equals(item.rawSecretIdentity())))
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
                &&(I.container()==item.container())
                &&(I.rawSecretIdentity().equals(item.rawSecretIdentity())))
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
			int weight=I.baseEnvStats().weight();
			totalWeight+=weight;
			totalValue+=I.baseGoldValue();
			if(weight>maxFound)
			{
				maxFound=weight;
				bundle=(Item)found.elementAt(i);
			}
		    if((I instanceof Decayable)
		    &&(((Decayable)I).decayTime()>0)
		    &&(((Decayable)I).decayTime()<lowestNonZeroFoodNumber))
		        lowestNonZeroFoodNumber=((Decayable)I).decayTime();
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
		bundle.baseEnvStats().setWeight(totalWeight);
		bundle.setBaseValue(totalValue);
        adjustResourceName(bundle);
		if(bundle instanceof Food)
		    ((Food)bundle).setNourishment(totalNourishment);
		if(bundle instanceof Drink)
		{
		    ((Drink)bundle).setLiquidHeld(totalThirstHeld);
		    ((Drink)bundle).setLiquidRemaining(totalThirstRemain);
		}
		if(bundle instanceof Decayable)
		    ((Decayable)bundle).setDecayTime(lowestNonZeroFoodNumber);
		for(Enumeration e=foundAblesH.keys();e.hasMoreElements();)
		{
			A=(Ability)foundAblesH.get(e.nextElement());
			if(bundle.fetchEffect(A.ID())==null)
				bundle.addNonUninvokableEffect((Ability)A.copyOf());
		}
		for(int i=0;i<found.size();i++)
			((RawMaterial)found.elementAt(i)).quickDestroy();
        if((owner instanceof Room)&&(((Room)owner).numItems()>0)&&(((Room)owner).fetchItem(((Room)owner).numItems()-1)!=bundle))
        {
            ((Room)owner).delItem(bundle);
            ((Room)owner).bringItemHere(bundle,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),false);
        }
        if((owner instanceof MOB)&&(((MOB)owner).inventorySize()>0)&&(((MOB)owner).fetchInventory(((MOB)owner).inventorySize()-1)!=bundle))
        {
            ((MOB)owner).delInventory(bundle);
            ((MOB)owner).giveItem(bundle);
        }
		Room R=CMLib.map().roomLocation(bundle);
		if(R!=null) R.recoverRoomStats();
		return true;
	}
	
    public Environmental unbundle(Item I, int number)
    {
        if((I==null)||(I.amDestroyed())) 
            return null;
        if((I instanceof PackagedItems)
        &&(I.container()==null)
        &&(!CMLib.flags().isOnFire(I)))
        {
            PackagedItems pkg=(PackagedItems)I;
            if(number<=0) number=pkg.numberOfItemsInPackage();
            if(number<=0) number=1;
            if(number>pkg.numberOfItemsInPackage())
                number=pkg.numberOfItemsInPackage();
            Environmental owner=I.owner();
            Vector parts=((PackagedItems)I).unPackage(number);
            if(parts.size()==0) return I;
            for(int p=0;p<parts.size();p++)
            {
                I=(Item)parts.elementAt(p);
                if(owner instanceof Room)
                    ((Room)owner).addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                else
                if(owner instanceof MOB)
                    ((MOB)owner).addInventory(I);
            }
            if(!pkg.amDestroyed())
            {
            	if(owner instanceof Room)
            	{
            		((Room)owner).delItem(pkg);
            		((Room)owner).addItemRefuse(pkg,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
            	}
            	else
	            if(owner instanceof MOB)
	            {
	            	((MOB)owner).delInventory(pkg);
	                ((MOB)owner).addInventory(pkg);
	            }
	            else
	            	pkg.destroy();
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
                if(number<=0) number=I.baseEnvStats().weight();
                if(number<=0) number=1;
                if(number>=(I.baseEnvStats().weight()-1))
                    number=I.baseEnvStats().weight();
                I.baseEnvStats().setWeight(I.baseEnvStats().weight());
                int loseValue=0;
                int loseNourishment=0;
                int loseThirstHeld=0;
                int loseThirstRemain=0;
                Environmental E=null;
                for(int x=0;x<number;x++)
                {
                    E=makeResource(I.material(),null,true,I.rawSecretIdentity());
                    if(E instanceof Item)
                    {
                        loseValue+=I.baseGoldValue();
                        if((E instanceof Decayable)&&(I instanceof Decayable))
                            ((Decayable)E).setDecayTime(((Decayable)I).decayTime());
                        if((E instanceof Food)&&(I instanceof Food))
                            loseNourishment+=((Food)E).nourishment();
                        if((E instanceof Drink)&&(I instanceof Drink))
                        {
                            loseThirstHeld+=((Drink)E).liquidHeld();
                            loseThirstRemain+=((Drink)E).liquidRemaining();
                        }
                        if(rott!=null)
                            E.addNonUninvokableEffect((Ability)rott.copyOf());
                        if(owner instanceof Room)
                            ((Room)owner).addItemRefuse((Item)E,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                        else
                        if(owner instanceof MOB)
                            ((MOB)owner).addInventory((Item)E);
                    }
                    else
                        E=null;
                }
                if((I.baseEnvStats().weight()-number)>1)
                {
                    I.baseEnvStats().setWeight(I.baseEnvStats().weight()-number);
                    I.setBaseValue(I.baseGoldValue()-loseValue);
                    this.adjustResourceName(I);
                    if(I instanceof Food)
                    {
                        ((Food)I).setNourishment(((Food)I).nourishment()-loseNourishment);
                        if(((Food)I).nourishment()<=0)
                            ((Food)I).setNourishment(0);
                    }
                    if(I instanceof Drink)
                    {
                        ((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()-loseThirstHeld);
                        if(((Drink)I).liquidHeld()<=0)
                            ((Drink)I).setLiquidHeld(0);
                        ((Drink)I).setLiquidRemaining(((Drink)I).liquidRemaining()-loseThirstRemain);
                        if(((Drink)I).liquidRemaining()<=0)
                            ((Drink)I).setLiquidRemaining(0);
                    }
                    I.recoverEnvStats();
                    // now move it to the end!
                    if(owner instanceof Room)
                    {
                        ((Room)owner).delItem(I);
                        ((Room)owner).addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
                    }
                    else
                    if(owner instanceof MOB)
                    {
                        ((MOB)owner).delInventory(I);
                        ((MOB)owner).addInventory(I);
                    }
                    else
                        I.destroy();
                }
                else
                    I.destroy();
                return E;
            }
            return I;
        }
        return null;
    }
    
    public String getMaterialDesc(int MASK)
    {
        MASK=(MASK&RawMaterial.MATERIAL_MASK)>>8;
        if((MASK>=0)&&(MASK<RawMaterial.MATERIAL_DESCS.length))
            return RawMaterial.MATERIAL_DESCS[MASK];
        return "";
    }
    
    public String getResourceDesc(int MASK)
    {
        if(RawMaterial.CODES.IS_VALID(MASK))
            return RawMaterial.CODES.NAME(MASK);
        return "";
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
    	int code = RawMaterial.CODES.FIND_IgnoreCase(s);
    	if(code>=0) return code;
    	if(exact) return -1;
		s=s.toUpperCase();
    	code = RawMaterial.CODES.FIND_StartsWith(s);
		return code;
	}
	
    public void addEffectsToResource(Item I)
    {
    	if(I==null) return;
    	Ability[] As=RawMaterial.CODES.EFFECTA(I.material());
    	if((As==null)||(As.length==0)) 
    		return;
    	for(Ability A : As)
    		if(I.fetchEffect(A.ID())==null)
    			I.addNonUninvokableEffect((Ability)A.copyOf());
    }
    
	public Environmental makeResource(int myResource, String localeCode, boolean noAnimals, String fullName)
	{
		if(myResource<0)
			return null;
		int material=(myResource&RawMaterial.MATERIAL_MASK);
        
		Item I=null;
		if(!noAnimals)
		{
			if((myResource==RawMaterial.RESOURCE_WOOL)
			||(myResource==RawMaterial.RESOURCE_FEATHERS)
			||(myResource==RawMaterial.RESOURCE_SCALES)
			||(myResource==RawMaterial.RESOURCE_HIDE)
			||(myResource==RawMaterial.RESOURCE_FUR))
			   material=RawMaterial.MATERIAL_LEATHER;
			if(CMParms.contains(RawMaterial.CODES.FISHES(), myResource))
				material=RawMaterial.MATERIAL_VEGETATION;
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
			break;
		}
		}
		if(I!=null)
		{
		    if((fullName!=null)&&(fullName.length()>0))
		        I.setSecretIdentity(fullName);
			I.setMaterial(myResource);
			if(I instanceof Drink)
				((Drink)I).setLiquidType(myResource);
			I.setBaseValue(RawMaterial.CODES.VALUE(myResource));
			I.baseEnvStats().setWeight(1);
			if(I instanceof RawMaterial)
				((RawMaterial)I).setDomainSource(localeCode);
            adjustResourceName(I);
			I.setDescription("");
			addEffectsToResource(I);
			I.recoverEnvStats();
			return I;
		}
		return null;
	}
	
    public String genericType(Item I) {
        if(I instanceof RawMaterial)
            return CMStrings.capitalizeAndLower(getMaterialDesc(I.material()));
        if(I instanceof Weapon)
            return "weapons";
        if(I instanceof Armor)
            return "armor";
        if(I instanceof Coins)
            return "currency";
        if(I instanceof Drink)
            return "liquid";
        if(I instanceof Food)
            return "food";
        if(I instanceof Pill)
            return "pills";
        if(I instanceof Light)
            return "light sources";
        if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
            return "papers";
        if(I instanceof Scroll)
            return "papers";
        if(I instanceof Electronics)
            return "technology";
        if(I instanceof Key)
            return "keys";
        if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER)
            return "papers";
        if(I instanceof DeadBody)
            return "corpses";
        if((I instanceof Container)&&(((Container)I).capacity()>0))
            return "containers";
        return "items";
    }
    
    
    public void adjustResourceName(Item I)
    {
        String name=RawMaterial.CODES.NAME(I.material()).toLowerCase();
        if((I.material()==RawMaterial.MATERIAL_MITHRIL)
        ||(I.material()==RawMaterial.MATERIAL_METAL))
        {
            if((I.material()!=RawMaterial.RESOURCE_ADAMANTITE)
            &&(I.material()!=RawMaterial.RESOURCE_BRASS)
            &&(I.material()!=RawMaterial.RESOURCE_BRONZE)
            &&(I.material()!=RawMaterial.RESOURCE_STEEL))
                name=name+" ore";
        }
        
        if(I.baseEnvStats().weight()==1)
        {
            if((I.rawSecretIdentity()!=null)
            &&(I.rawSecretIdentity().length()>0))
            {
                I.setName(I.rawSecretIdentity());
                I.setDisplayText(I.rawSecretIdentity()+" has been left here.");
            }
            else
            {
                if(I instanceof Drink)
                    I.setName("some "+name);
                else
                    I.setName("a pound of "+name);
                I.setDisplayText("some "+name+" sits here.");
            }
        }
        else
        {
            if(I instanceof Drink)
                I.setName("a "+I.baseEnvStats().weight()+"# pool of "+name);
            else
                I.setName("a "+I.baseEnvStats().weight()+"# "+name+" bundle");
            I.setDisplayText(I.name()+" is here.");
        }
    }
    
	public Item makeItemResource(int type)
	{
		Item I=null;
		if(((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		||((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
        I.baseEnvStats().setWeight(1);
        I.setMaterial(type);
        adjustResourceName(I);
		I.setDescription("");
		I.setBaseValue(RawMaterial.CODES.VALUE(type));
		addEffectsToResource(I);
		I.recoverEnvStats();
		return I;
	}
    
    
    public int destroyResources(Room E, int howMuch, int finalMaterial, int otherMaterial, Item never)
    { return destroyResources(getAllItems(E),E,howMuch,finalMaterial,otherMaterial,never);}
    public int destroyResources(MOB E, int howMuch, int finalMaterial, int otherMaterial, Item never)
    { return destroyResources(getAllItems(E),E,howMuch,finalMaterial,otherMaterial,never);}
    public int destroyResources(Vector V,
    						    Environmental addTo,
                                int howMuch,
                                int finalMaterial,
                                int otherMaterial,
                                Item never)
    {
        int lostValue=0;
        if((V==null)||(V.size()==0)) return 0;
        
        if((howMuch>0)||(otherMaterial>0))
        for(int i=V.size()-1;i>=0;i--)
        {
            Item I=(Item)V.elementAt(i);
            if(I==null) break;
            if(I==never) continue;

            if((otherMaterial>0)
            &&(I instanceof RawMaterial)
            &&(I.material()==otherMaterial)
            &&(I.container()==null)
            &&(!CMLib.flags().isOnFire(I))
            &&(!CMLib.flags().enchanted(I)))
            {
                if(I.baseEnvStats().weight()>1)
                {
                    I.baseEnvStats().setWeight(I.baseEnvStats().weight()-1);
                    Environmental E=makeResource(otherMaterial,null,true,I.rawSecretIdentity());
                    if(E instanceof Item)
                        lostValue+=((Item)E).value();
                    adjustResourceName(I);
                }
                else
                {
                    lostValue+=I.value();
                    ((RawMaterial)I).quickDestroy();
                }
                otherMaterial=-1;
                if((finalMaterial<0)||(howMuch<=0)) break;
            }
            else
            if((I instanceof RawMaterial)
            &&(I.material()==finalMaterial)
            &&(I.container()==null)
            &&(!CMLib.flags().isOnFire(I))
            &&(!CMLib.flags().enchanted(I))
            &&(howMuch>0))
            {
                if(I.baseEnvStats().weight()>howMuch)
                {
                    I.baseEnvStats().setWeight(I.baseEnvStats().weight()-howMuch);
                    Environmental E=makeResource(finalMaterial,null,true,I.rawSecretIdentity());
                    if(E instanceof Item)
                        lostValue+=(((Item)E).value()*howMuch);
                    adjustResourceName(I);
                    howMuch=0;
                }
                else
                {
                    howMuch-=I.baseEnvStats().weight();
                    lostValue+=I.value();
                    ((RawMaterial)I).quickDestroy();
                }
                if(howMuch<=0)
                {
                    finalMaterial=-1;
                    if(otherMaterial<0) break;
                }
            }
        }
        return lostValue;
    }
    
	public Item findFirstResource(Room E, String other){return findFirstResource(getAllItems(E),other);}
	public Item findFirstResource(MOB E, String other){return findFirstResource(getAllItems(E),other);}
    public Item findFirstResource(Vector V, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		int code = RawMaterial.CODES.FIND_IgnoreCase(other);
		if(code >=0 )
			return findFirstResource(V,code);
		return null;
	}
	public Item findFirstResource(Room E, int resource){return findFirstResource(getAllItems(E),resource);}
	public Item findFirstResource(MOB E, int resource){return findFirstResource(getAllItems(E),resource);}
	protected Item findFirstResource(Vector V, int resource)
	{
		for(int i=0;i<V.size();i++)
		{
			Item I=(Item)V.elementAt(i);
			if((I instanceof RawMaterial)
			&&(I.material()==resource)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().enchanted(I))
			&&(I.container()==null))
				return I;
		}
		return null;
	}
	
	public Item findMostOfMaterial(Room E, String other){return findMostOfMaterial(getAllItems(E),other);}
	public Item findMostOfMaterial(MOB E, String other){return findMostOfMaterial(getAllItems(E),other);}
	protected Item findMostOfMaterial(Vector V, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
			if(RawMaterial.MATERIAL_DESCS[i].equalsIgnoreCase(other))
				return findMostOfMaterial(V,(i<<8));
		return null;
	}

	public int findNumberOfResource(Room E, int resource){return findNumberOfResource(getAllItems(E),resource);}
	public int findNumberOfResource(MOB E, int resource){return findNumberOfResource(getAllItems(E),resource);}
	protected int findNumberOfResource(Vector V, int resource)
	{
		int foundWood=0;
		for(int i=0;i<V.size();i++)
		{
			Item I=(Item)V.elementAt(i);
			if((I instanceof RawMaterial)
			&&(I.material()==resource)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().enchanted(I))
			&&(I.container()==null))
				foundWood+=I.envStats().weight();
		}
		return foundWood;
	}

	public Item findMostOfMaterial(Room E, int material){return findMostOfMaterial(getAllItems(E),material);}
	public Item findMostOfMaterial(MOB E, int material){return findMostOfMaterial(getAllItems(E),material);}
	protected Item findMostOfMaterial(Vector V, int material)
	{
		int most=0;
		int mostMaterial=-1;
		Item mostItem=null;
		for(int i=0;i<V.size();i++)
		{
			Item I=(Item)V.elementAt(i);
			if((I instanceof RawMaterial)
			&&((I.material()&RawMaterial.MATERIAL_MASK)==material)
			&&(I.material()!=mostMaterial)
			&&(!CMLib.flags().isOnFire(I))
			&&(!CMLib.flags().enchanted(I))
			&&(I.container()==null))
			{
				int num=findNumberOfResource(V,I.material());
				if(num>most)
				{
					mostItem=I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}

	protected Vector getAllItems(Room R)
	{
		Vector V=new Vector();
		Item I=null;
		if(R!=null)
		for(int r=0;r<R.numItems();r++)
		{
			I=R.fetchItem(r);
			if(I!=null)V.addElement(I);
		}
		return V;
	}
	protected Vector getAllItems(MOB M)
	{
		Vector V=new Vector();
		Item I=null;
		if(M!=null)
		for(int i=0;i<M.inventorySize();i++)
		{
			I=M.fetchInventory(i);
			if(I!=null)V.addElement(I);
		}
		return V;
	}
	
	public Item fetchFoundOtherEncoded(Room E, String otherRequired){return fetchFoundOtherEncoded(getAllItems(E),otherRequired);}
	public Item fetchFoundOtherEncoded(MOB E, String otherRequired){return fetchFoundOtherEncoded(getAllItems(E),otherRequired);}
	protected Item fetchFoundOtherEncoded(Vector V, String otherRequired)
	{
		if((otherRequired==null)||(otherRequired.trim().length()==0))
			return null;
		Item firstOther=null;
		boolean resourceOther=otherRequired.startsWith("!");
		if(resourceOther) otherRequired=otherRequired.substring(1);
		if((otherRequired.length()>0)&&(!resourceOther))
			firstOther=findMostOfMaterial(V,otherRequired);
		if((firstOther==null)&&(otherRequired.length()>0))
			firstOther=findFirstResource(V,otherRequired);
		return firstOther;
	}
}
