package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class GenPostman extends StdPostman
{
    public String ID(){return "GenPostman";}
    private String PrejudiceFactors="";
    private String postalChain="main";
    private String IgnoreMask="";

    public GenPostman()
    {
        super();
        Username="a generic postman";
        setDescription("He looks bored and slow.");
        setDisplayText("A generic postman stands here.");
    }

    public boolean isGeneric(){return true;}

    public String text()
    {
        if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
            miscText=Util.compressString(CoffeeMaker.getPropertiesStr(this,false));
        else
            miscText=CoffeeMaker.getPropertiesStr(this,false).getBytes();
        return super.text();
    }

    public String prejudiceFactors(){return PrejudiceFactors;}
    public void setPrejudiceFactors(String factors){PrejudiceFactors=factors;}
    public String ignoreMask(){return IgnoreMask;}
    public void setIgnoreMask(String factors){IgnoreMask=factors;}
    public String postalChain(){return postalChain;}
    public void setPostalChain(String name){postalChain=name;}

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        CoffeeMaker.resetGenMOB(this,newText);
    }
    private static String[] MYCODES={"WHATISELL",
                                     "PREJUDICE",
                                     "POSTCHAIN","POSTMIN","POSTLBS",
                                     "POSTHOLD","POSTNEW","POSTHELD",
                                     "IGNOREMASK"};
    public String getStat(String code)
    {
        if(CoffeeMaker.getGenMobCodeNum(code)>=0)
            return CoffeeMaker.getGenMobStat(this,code);
        switch(getCodeNum(code))
        {
        case 0: return ""+whatIsSold();
        case 1: return prejudiceFactors();
        case 2: return postalChain();
        case 3: return ""+minimumPostage();
        case 4: return ""+postagePerPound();
        case 5: return ""+holdFeePerPound();
        case 6: return ""+feeForNewBox();
        case 7: return ""+maxMudMonthsHeld();
        case 8: return ignoreMask();
        }
        return "";
    }
    public void setStat(String code, String val)
    {
        if(CoffeeMaker.getGenMobCodeNum(code)>=0)
            CoffeeMaker.setGenMobStat(this,code,val);
        else
        switch(getCodeNum(code))
        {
        case 0: setWhatIsSold(Util.s_int(val)); break;
        case 1: setPrejudiceFactors(val); break;
        case 2: setPostalChain(val); break;
        case 3: setMinimumPostage(Util.s_double(val)); break;
        case 4: setPostagePerPound(Util.s_double(val)); break;
        case 5: setHoldFeePerPound(Util.s_double(val)); break;
        case 6: setFeeForNewBox(Util.s_double(val)); break;
        case 7: setMaxMudMonthsHeld(Util.s_int(val)); break;
        case 8: setIgnoreMask(val); break;
        }
    }
    protected int getCodeNum(String code){
        for(int i=0;i<MYCODES.length;i++)
            if(code.equalsIgnoreCase(MYCODES[i])) return i;
        return -1;
    }
    private static String[] codes=null;
    public String[] getStatCodes()
    {
        if(codes!=null) return codes;
        String[] superCodes=CoffeeMaker.GENMOBCODES;
        codes=new String[superCodes.length+MYCODES.length];
        int i=0;
        for(;i<superCodes.length;i++)
            codes[i]=superCodes[i];
        for(int x=0;x<MYCODES.length;i++,x++)
            codes[i]=MYCODES[x];
        return codes;
    }
    public boolean sameAs(Environmental E)
    {
        if(!(E instanceof GenPostman)) return false;
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
