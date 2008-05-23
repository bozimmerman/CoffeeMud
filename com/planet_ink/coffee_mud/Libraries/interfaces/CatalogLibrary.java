package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.*;
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
public interface CatalogLibrary 
{
    public DVector getCatalogItems();
    public DVector getCatalogMobs();
    public boolean isCatalogObj(Environmental E);
    public boolean isCatalogObj(String name);
    public int getCatalogItemIndex(String called);
    public int getCatalogMobIndex(String called);
    public Item getCatalogItem(int index);
    public MOB getCatalogMob(int index);
    public int[] getCatalogItemUsage(int index);
    public int[] getCatalogMobUsage(int index);
    public CataData getCatalogItemData(int index);
    public CataData getCatalogMobData(int index);
    public void delCatalog(Environmental E);
    public void addCatalogReplace(Environmental E);
    public void addCatalog(Environmental E);
    public void propogateCatalogChange(Environmental E);
    public void changeCatalogUsage(Environmental E, boolean add);
    public Item getDropItem(MOB M, boolean live);
    public void unLoad();
    
    public static class CataData 
    {
        public Vector lmaskV=null;
        public String lmaskStr=null;
        public boolean live=false;
        public double rate=0.0;
        
        public CataData(String catadata)
        {
            build(catadata);
        }
        
        public CataData(String _lmask, String _rate, boolean _live)
        {
            this(_lmask,CMath.s_pct(_rate),_live);
        }
        
        public CataData(String _lmask, double _rate, boolean _live)
        {
            live=_live;
            lmaskStr=_lmask;
            lmaskV=null;
            if(lmaskStr.length()>0)
                lmaskV=CMLib.masking().maskCompile(lmaskStr);
            rate=_rate;
        }
        
        public String data() 
        {
            StringBuffer buf=new StringBuffer("");
            buf.append("<CATALOGDATA>");
            buf.append("<RATE>"+CMath.toPct(rate)+"</RATE>");
            buf.append("<LMASK>"+CMLib.xml().parseOutAngleBrackets(lmaskStr)+"</LMASK>");
            buf.append("<LIVE>"+live+"</LIVE>");
            buf.append("</CATALOGDATA>");
            return buf.toString();
        }
        
        public void build(String catadata)
        {
            Vector V=null;
            if((catadata!=null)&&(catadata.length()>0))
            {
                V=CMLib.xml().parseAllXML(catadata);
                XMLLibrary.XMLpiece piece=CMLib.xml().getPieceFromPieces(V,"CATALOGDATA");
                if((piece!=null)&&(piece.contents!=null)&&(piece.contents.size()>0))
                {
                    lmaskStr=CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(piece.contents,"LMASK"));
                    String ratestr=CMLib.xml().getValFromPieces(piece.contents,"RATE");
                    rate=CMath.s_pct(ratestr);
                    lmaskV=null;
                    if(lmaskStr.length()>0)
                        lmaskV=CMLib.masking().maskCompile(lmaskStr);
                    live=CMath.s_bool(CMLib.xml().getValFromPieces(piece.contents,"LIVE"));
                }
            }
            else
            {
                lmaskV=null;
                lmaskStr="";
                live=false;
                rate=0.0;
            }
        }
    }
}
