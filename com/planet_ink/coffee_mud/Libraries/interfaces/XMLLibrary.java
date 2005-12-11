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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
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
public interface XMLLibrary extends CMObject
{
    /**
     * Returns the double value of a string without crashing
     * 
     * <br><br><b>Usage:</b> dSize = WebIQBase.s_double(WebIQBase.getRes(AttStatsRes,"BlobSize"));
     * @param double String to convert
     * @return double Double value of the string
     */
    public double s_double(String DOUBLE);
    /**
     * Returns the integer value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
     * @param INT Integer value of string
     * @return int Integer value of the string
     */
    public int s_int(String INT);
    /**
     * Returns the long value of a string without crashing
     * 
     * <br><br><b>Usage:</b> int num=s_long(CMD.substring(14));
     * @param LONG Long value of string
     * @return long Long value of the string
     */
    public  long s_long(String LONG);
    /**
     * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
     * 
     * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
     * @param TName Tag name to search for
     * @param Data String to searh
     * @return String Information corresponding to the tname
     */
    public String convertXMLtoTag(String TName, String Data);
    /**
     * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
     * 
     * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
     * @param TName Tag name to search for
     * @param Data String to searh
     * @return String Information corresponding to the tname
     */
    public String convertXMLtoTag(String TName, int Data);
    /**
     * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
     * 
     * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
     * @param TName Tag name to search for
     * @param Data String to searh
     * @return String Information corresponding to the tname
     */
    public String convertXMLtoTag(String TName, boolean Data);
    /**
     * Return the outer wrapper and contents of an XML tag <TNAME>Data</TNAME>
     * 
     * <br><br><b>Usage:</b> Data+=XMLoTag("MODELOBJECTONE",VA.ModelObjectOne);
     * @param TName Tag name to search for
     * @param Data String to searh
     * @return String Information corresponding to the tname
     */
    public String convertXMLtoTag(String TName, long Data);
    /**
     * Return the contents of an XML tag, given the tag to search for
     * 
     * <br><br><b>Usage:</b> String XML=returnXMLBlock(Response,"PDIModelErrors");
     * @param Blob String to searh
     * @param Tag Tag name to search for
     * @return String Information corresponding to the tname
     */
    public String returnXMLBlock(String Blob, String Tag);
    
    public static class XMLpiece
    {
        public String tag="";
        public String value="";
        public Vector contents=new Vector();
        public Vector parms=contents;
        public XMLpiece parent=null;
        public int outerStart=-1;
        public int innerStart=-1;
        public int innerEnd=-1;
        public int outerEnd=-1;
        public void addContent(XMLpiece x)
        {
            if(x==null) return;
            if(contents==null) contents=new Vector();
            contents.addElement(x);
        }
    }
    
    public String parseOutParms(String blk, Vector parmList);
    public String getValFromPieces(Vector V, String tag);
    public Vector getContentsFromPieces(Vector V, String tag);
    public Vector getRealContentsFromPieces(Vector V, String tag);
    public XMLpiece getPieceFromPieces(Vector V, String tag);
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=getBoolFromPieces(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return boolean Information from XML block
     */
    public boolean getBoolFromPieces(Vector V, String tag);
    
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=getIntFromPieces(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return int Information from XML block
     */
    public int getIntFromPieces(Vector V, String tag);
    
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=getLongFromPieces(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return long Information from XML block
     */
    public long getLongFromPieces(Vector V, String tag);
    
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=getDoubleFromPieces(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return double Information from XML block
     */
    public double getDoubleFromPieces(Vector V, String tag);
    
    public boolean acceptableTag(StringBuffer str, int start, int end);
    
    public XMLpiece nextXML(StringBuffer buf, XMLpiece parent, int start);
    
    public Vector parseAllXML(String buf);
        
    public Vector parseAllXML(StringBuffer buf);
    
    /**
     * Return the data value within the first XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow);
     * @param Blob String to searh
     * @return String Information from first XML block
     */
    public String returnXMLValue(String Blob);
    
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return String Information from XML block
     */
    public String returnXMLValue(String Blob, String Tag);
    
    /**
     * Return the data value within a given XML block
     * <TAG>Data</TAG>
     * 
     * <br><br><b>Usage:</b> String ThisColHead=returnXMLValue(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return String Information from XML block
     */
    public boolean returnXMLBoolean(String Blob, String Tag);

    /**
     * Return a parameter value within an XML tag
     * <TAG Parameter="VALUE">
     * 
     * <br><br><b>Usage:</b> String ThisColHead=ReturnXMLParm(ThisRow,"TD");
     * @param Blob String to search
     * @param Tag Tag to search for
     * @return String Parameter value
     */
    public String returnXMLParm(String Blob, String Tag);
}
