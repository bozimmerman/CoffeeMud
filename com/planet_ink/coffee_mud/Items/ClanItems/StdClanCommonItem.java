package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdClanCommonItem extends StdClanItem
{
	public String ID(){	return "StdClanCommonItem";}
	private int workDown=0;
    private static final Hashtable needChart=new Hashtable();
    
	public StdClanCommonItem()
	{
		super();

		setName("a clan workers item");
		baseEnvStats.setWeight(1);
		setDisplayText("an workers item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_GATHERITEM);
		material=EnvResource.RESOURCE_OAK;
		recoverEnvStats();
	}

    public boolean fireHere(Room R)
    {
        for(int i=0;i<R.numItems();i++)
        {
            Item I2=R.fetchItem(i);
            if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
                return true;
        }
        return false;
    }
    
    public Vector resourceHere(Room R, int material)
    {
        Vector here=new Vector();
        for(int i=0;i<R.numItems();i++)
        {
            Item I2=R.fetchItem(i);
            if((I2!=null)
            &&(I2.container()==null)
            &&(I2 instanceof EnvResource)
            &&(((I2.material()&EnvResource.RESOURCE_MASK)==material)
                ||(((I2.material())&EnvResource.MATERIAL_MASK)==material))
            &&(!Sense.enchanted(I2)))
                here.addElement(I2);
        }
        return here;
    }
    
    public Vector resourceHere(MOB M, int material)
    {
        Vector here=new Vector();
        for(int i=0;i<M.inventorySize();i++)
        {
            Item I2=M.fetchInventory(i);
            if((I2!=null)
            &&(I2.container()==null)
            &&(I2 instanceof EnvResource)
            &&(((I2.material()&EnvResource.RESOURCE_MASK)==material)
                ||(((I2.material())&EnvResource.MATERIAL_MASK)==material))
            &&(!Sense.enchanted(I2)))
                here.addElement(I2);
        }
        return here;
    }
    
    public Vector resourceHere(Room R, Vector materials)
    {
        Vector allMat=new Vector();
        Vector V=null;
        for(int m=0;m<materials.size();m++)
        {
            V=resourceHere(R,((Integer)materials.elementAt(m)).intValue());
            for(int v=0;v<V.size();v++)
                allMat.addElement(V.elementAt(v));
            V.clear();
        }
        return allMat;
    }
    public Vector resourceHere(MOB M, Vector materials)
    {
        Vector allMat=new Vector();
        Vector V=null;
        for(int m=0;m<materials.size();m++)
        {
            V=resourceHere(M,((Integer)materials.elementAt(m)).intValue());
            for(int v=0;v<V.size();v++)
                allMat.addElement(V.elementAt(v));
            V.clear();
        }
        return allMat;
    }
    
    public Vector enCode(MOB M, String req)
    {
        req=req.toUpperCase();
        Vector V=new Vector();
        for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
        {
            int x=req.indexOf(EnvResource.MATERIAL_DESCS[i]);
            if(x<0) continue;
            if((x>0)&&Character.isLetter(req.charAt(x-1)))
                continue;
            if(((x+EnvResource.MATERIAL_DESCS[i].length())<req.length())
            &&Character.isLetter(req.charAt((x+EnvResource.MATERIAL_DESCS[i].length()))))
                continue;
            V.addElement(new Integer(i<<8));
        }
        for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
        {
            int x=req.indexOf(EnvResource.RESOURCE_DESCS[i]);
            if(x<0) continue;
            if((x>0)&&Character.isLetter(req.charAt(x-1)))
                continue;
            if(((x+EnvResource.RESOURCE_DESCS[i].length())<req.length())
            &&Character.isLetter(req.charAt((x+EnvResource.RESOURCE_DESCS[i].length()))))
                continue;
            V.addElement(new Integer(i));
        }
        if((M.location()!=null)
        &&(V.contains(new Integer(EnvResource.MATERIAL_METAL)))
        &&(resourceHere(M.location(),EnvResource.MATERIAL_WOODEN).size()==0))
            V.addElement(new Integer(EnvResource.MATERIAL_WOODEN));
        return V;
    }
    
    public boolean trackTo(MOB M, MOB M2)
    {
        Ability A=CMClass.getAbility("Skill_Track");
        if(A!=null)
        {
            Room R=M2.location();
            if((R!=null)&&(Sense.isInTheGame(M2,true)))
            {
                A.invoke(M,Util.parse("\""+CMMap.getExtendedRoomID(R)+"\""),R,true,0);
                return true;
            }
        }
        return false;
    }
    public boolean trackTo(MOB M, Room R)
    {
        Ability A=CMClass.getAbility("Skill_Track");
        if((A!=null)&&(R!=null))
        {
            A.invoke(M,Util.parse("\""+CMMap.getExtendedRoomID(R)+"\""),R,true,0);
            return true;
        }
        return false;
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_CLANITEM)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(readableText().length()>0)
		&&(((MOB)owner()).getClanID().equals(clanID()))
		&&((--workDown)<=0)
        &&(!Sense.isATrackingMonster(owner))
        &&(Sense.isInTheGame(owner,true))
		&&(!Sense.isAnimalIntelligence((MOB)owner())))
		{
			workDown=Dice.roll(1,7,0);
			MOB M=(MOB)owner();
			if(M.fetchEffect(readableText())==null)
			{
				Ability A=CMClass.getAbility(readableText());
				if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
				{
					A.setProfficiency(100);
                    boolean success=false;
                    if((!Util.bset(A.flags(),Ability.FLAG_CRAFTING))&&(Sense.isMobile(M)))
                    {
                        DVector DV=(DVector)needChart.get(M.location().getArea());
                        if(DV!=null)
                        {
                            Vector needs=null;
                            MOB M2=null;
                            boolean getToWork=false;
                            if(A.ID().equalsIgnoreCase("FireBuilding"))
                            {
                                MOB possibleMOBToGoTo=null;
                                for(int i=0;i<DV.size();i++)
                                {
                                    try{
                                        int rand=i;
                                        needs=(Vector)DV.elementAt(rand,2);
                                        M2=(MOB)DV.elementAt(rand,1);
                                    }catch(Exception e){continue;}
                                    if((needs!=null)&&(M2!=null)
                                    &&(needs.contains(new Integer(EnvResource.MATERIAL_METAL)))
                                    &&(!fireHere(M2.location()))
                                    &&(resourceHere(M2.location(),EnvResource.MATERIAL_WOODEN).size()>0))
                                    {
                                        if(M.location()==M2.location())
                                        {
                                            getToWork=true;
                                            break;
                                        }
                                        else
                                        if((possibleMOBToGoTo==null)||(Dice.roll(1,2,0)==1))
                                            possibleMOBToGoTo=M2;
                                    }
                                }
                                if((!getToWork)
                                &&(possibleMOBToGoTo!=null)
                                &&(trackTo(M,possibleMOBToGoTo)))
                                {
                                    return true;
                                }
                            }
                            Vector rsc=null;
                            // if I have the stuff on hand.
                            if(!getToWork)
                            for(int i=DV.size()-1;i>=0;i--)
                            {
                                try{
                                    int rand=i;
                                    needs=(Vector)DV.elementAt(rand,2);
                                    M2=(MOB)DV.elementAt(rand,1);
                                    if(!Sense.isInTheGame(M2,true))
                                    {
                                        DV.removeElementAt(i);
                                        continue;
                                    }
                                }catch(Exception e){continue;}
                                if((needs!=null)&&(M2!=null)
                                &&(M.location()==M2.location()))
                                {
                                    rsc=resourceHere(M,needs);
                                    if(rsc.size()>0)
                                    {
                                        for(int r=0;r<rsc.size();r++)
                                            CommonMsgs.drop(M,(Environmental)rsc.elementAt(r),false,true);
                                        return true;
                                    }
                                }
                            }
                            if(!getToWork)
                            for(int i=0;i<DV.size();i++)
                            {
                                try{
                                    int rand=Dice.roll(1,DV.size(),-1);
                                    needs=(Vector)DV.elementAt(rand,2);
                                    M2=(MOB)DV.elementAt(rand,1);
                                }catch(Exception e){continue;}
                                if((needs!=null)&&(M2!=null)
                                &&(M.location()!=M2.location()))
                                {
                                    rsc=resourceHere(M,needs);
                                    if((rsc.size()>0)
                                    &&(trackTo(M,M2)))
                                        return true;
                                }
                            }
                            if(!getToWork)
                            for(int i=0;i<DV.size();i++)
                            {
                                try{
                                    int rand=Dice.roll(1,DV.size(),-1);
                                    needs=(Vector)DV.elementAt(rand,2);
                                    M2=(MOB)DV.elementAt(rand,1);
                                }catch(Exception e){continue;}
                                if((needs!=null)&&(M2!=null)
                                &&(M.location()!=M2.location()))
                                {
                                    rsc=resourceHere(M.location(),needs);
                                    if(rsc.size()>0)
                                    {
                                        for(int r=0;r<rsc.size();r++)
                                            CommonMsgs.get(M,null,(Item)rsc.elementAt(r),false);
                                        if(trackTo(M,M2))
                                            return true;
                                    }
                                }
                            }
                        }
                    }
                    
					if((M.inventorySize()>1)&&(Util.bset(A.flags(),Ability.FLAG_CRAFTING)))
					{
						Item I=null;
						int tries=0;
						while((I==null)&&((++tries)<20))
						{
							I=M.fetchInventory(Dice.roll(1,M.inventorySize(),-1));
							if((I==null)
                            ||(I==this)
                            ||(I instanceof EnvResource)
                            ||(!I.amWearingAt(Item.INVENTORY)))
								I=null;
						}
						Vector V=new Vector();
						if(I!=null)	V.addElement(I.name());
						success=A.invoke(M,V,null,false,envStats().level());
					}
					else
						success=A.invoke(M,new Vector(),null,false,envStats().level());
                    if(Util.bset(A.flags(),Ability.FLAG_CRAFTING))
                    {
                        DVector DV=(DVector)needChart.get(M.location().getArea());
                        if(!success)
                        {
                            if(DV==null)
                            {
                                DV=new DVector(2);
                                needChart.put(M.location().getArea(),DV);
                            }
                            DV.removeElement(M);
                            String req=A.accountForYourself();
                            int reqIndex=req.indexOf(":");
                            if(reqIndex>0)
                                DV.addElement(M,enCode(M,req.substring(reqIndex+1)));
                            else
                                DV.addElement(M,enCode(M,req));
                        }
                        else
                        if(DV!=null)
                            DV.removeElement(M);
                    }
				}

			}
		}
		return true;
	}
}
