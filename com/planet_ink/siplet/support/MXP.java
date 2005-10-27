package com.planet_ink.siplet.support;
import java.applet.*;
import java.net.*;
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
public class MXP
{

    private int defaultMode=0;
    public static final int MODE_LINE_OPEN=0;
    public static final int MODE_LINE_SECURE=1;
    public static final int MODE_LINE_LOCKED=2;
    public static final int MODE_RESET=3;
    public static final int MODE_TEMP_SECURE=4;
    public static final int MODE_LOCK_OPEN=5;
    public static final int MODE_LOCK_SECURE=6;
    public static final int MODE_LOCK_LOCKED=7;
    public static final int MODE_LINE_ROOMNAME=10;
    public static final int MODE_LINE_ROOMDESC=11;
    public static final int MODE_LINE_ROOMEXITS=12;
    public static final int MODE_LINE_WELCOME=19;
    
    private Hashtable elements=new Hashtable();
    private Hashtable entities=new Hashtable();

    public MXP()
    {
        super();
        elements.put("B",MXPElement.MXPFinalElement("<B>",""));
        elements.put("BOLD",MXPElement.MXPFinalElement("<B>",""));
        elements.put("STRONG",MXPElement.MXPFinalElement("<B>",""));
        elements.put("U",MXPElement.MXPFinalElement("<U>",""));
        elements.put("UNDERLINE",MXPElement.MXPFinalElement("<U>",""));
        elements.put("I",MXPElement.MXPFinalElement("<I>",""));
        elements.put("ITALIC",MXPElement.MXPFinalElement("<I>",""));
        elements.put("S",MXPElement.MXPFinalElement("<S>",""));
        elements.put("STRIKEOUT",MXPElement.MXPFinalElement("<S>",""));
        elements.put("EM",MXPElement.MXPFinalElement("<I>",""));
        elements.put("H1",MXPElement.MXPFinalElement("<H1>",""));
        elements.put("H2",MXPElement.MXPFinalElement("<H2>",""));
        elements.put("H3",MXPElement.MXPFinalElement("<H3>",""));
        elements.put("H4",MXPElement.MXPFinalElement("<H4>",""));
        elements.put("H5",MXPElement.MXPFinalElement("<H5>",""));
        elements.put("H6",MXPElement.MXPFinalElement("<H6>",""));
        elements.put("HR",MXPElement.MXPFinalCommand("<HR>"));
        elements.put("SMALL",MXPElement.MXPFinalElement("<SMALL>",""));
        elements.put("TT",MXPElement.MXPFinalElement("<PRE>",""));
        elements.put("BR",MXPElement.MXPFinalCommand("<BR>"));
        elements.put("SBR",MXPElement.MXPFinalCommand("&nbsp;")); // not fully supported
        elements.put("P",new MXPElement("","","",-1,false,false,true,true));
        elements.put("C",MXPElement.MXPFinalElement("<FONT COLOR=&fore; STYLE=\"background-color: &back;\">","FORE BACK"));
        elements.put("COLOR",MXPElement.MXPFinalElement("<FONT COLOR=&fore; STYLE=\"background-color: &back;\">","FORE BACK"));
        elements.put("HIGH",MXPElement.MXPFinalElement("","")); // not supported
        elements.put("H",MXPElement.MXPFinalElement("","")); // not supported
        elements.put("FONT",MXPElement.MXPFinalElement("<FONT FACE=&face; SIZE=&size; COLOR=&color; STYLE=\"background-color: &back;\">","FACE SIZE COLOR BACK"));
        elements.put("NOBR",new MXPElement("","","",-1,false,false,true,true));
        elements.put("A",MXPElement.MXPFinalElement("<A HREF=&href; TITLE=&hint;>","HREF HINT EXPIRE"));
        elements.put("SEND",MXPElement.MXPFinalElement("<A TITLE=&hint; HREF=\"javascript: Send('&href');\">","HREF HINT PROMPT EXPIRE"));
        elements.put("EXPIRE",MXPElement.MXPFinalCommand("","NAME")); // not supported
        elements.put("VERSION",new MXPElement("","","",-1,false,true,true,true));
        elements.put("GAUGE",new MXPElement("","ENTITY MAX CAPTION COLOR","",-1,false,true,true,true));
        elements.put("STAT",new MXPElement("","ENTITY MAX CAPTION","",-1,false,true,true,true));
        elements.put("FRAME",new MXPElement("","NAME ACTION TITLE INTERNAL ALIGN LEFT TOP WIDTH HEIGHT SCROLLING FLOATING","",-1,false,true,true,true));
        elements.put("DEST",new MXPElement("","NAME","",-1,false,true,true,true));
        elements.put("RELOCATE",new MXPElement("","URL PORT","",-1,false,true,true,true));
        elements.put("USER",new MXPElement("","","",-1,false,true,true,true));
        elements.put("PASSWORD",new MXPElement("","","",-1,false,true,true,true));
        elements.put("IMAGE",new MXPElement("<IMG SRC=&src;&fname; HEIGHT=&h; WIDTH=&w; ALIGN=&align;>","FNAME URL T H W HSPACE VSPACE ALIGN ISMAP","",-1,false,true,true,true));
        elements.put("FILTER",MXPElement.MXPFinalCommand("","SRC DEST NAME")); // not supported
        elements.put("SCRIPT",MXPElement.MXPFinalCommand("")); // not supported
        elements.put("!ENTITY",new MXPElement("","NAME VALUE DESC PRIVATE PUBLISH DELETE ADD","",-1,false,false,true,true));
        elements.put("!EN",new MXPElement("","NAME VALUE DESC PRIVATE PUBLISH DELETE ADD","",-1,false,false,true,true));
        elements.put("!TAG",new MXPElement("","INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE","",-1,false,false,true,true));
        elements.put("VAR",new MXPElement("","NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE","",-1,false,false,true,true));
        elements.put("V",new MXPElement("","NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE","",-1,false,false,true,true));
        elements.put("!ELEMENT",new MXPElement("","NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY","",-1,false,false,true,true));
        elements.put("!EL",new MXPElement("","NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY","",-1,false,false,true,true));
        elements.put("!ATTLIST",new MXPElement("","NAME ATT","",-1,false,false,true,true));
        elements.put("!AT",new MXPElement("","NAME ATT","",-1,false,false,true,true));
        elements.put("SOUND",new MXPElement("!!SOUND(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)","FNAME V=100 L=1 P=50 T U","",-1,false,true,false,false));
        elements.put("MUSIC",new MXPElement("!!MUSIC(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)","FNAME V=100 L=1 P=50 T U","",-1,false,true,false,false));
        //-------------------------------------------------------------------------
        entities.put("NBSP",new MXPEntity("&nbsp;",true));
        entities.put("LT",new MXPEntity("&lt;",true));
        entities.put("GT",new MXPEntity("&gt;",true));
        entities.put("QUOT",new MXPEntity("&quot;",true));
        entities.put("AMP",new MXPEntity("&amp;",true));
    }
    
    private int mode=0;
    public int mode(){return mode;}
    public void setMode(int newMode){mode=newMode;}
    public void setModeAndExecute(int newMode){setMode(newMode); executeMode();}
    
    public void executeMode()
    {
        switch(mode)
        {
        case MODE_RESET:
            defaultMode=MODE_LINE_OPEN;
            mode=defaultMode;
            closeAllTags();
            break;
        case MODE_LOCK_OPEN:
        case MODE_LOCK_SECURE:
        case MODE_LOCK_LOCKED:
            defaultMode=mode;
            break;
        }
    }
    
    public void newlineDetected()
    {
        switch(mode)
        {
        case MODE_LINE_OPEN:
        case MODE_LINE_SECURE:
        case MODE_LINE_LOCKED:
        case MODE_TEMP_SECURE:
            closeAllOpenTags();
            setModeAndExecute(defaultMode);
            break;
        }
    }

    // does not close Secure tags -- they are never ever closed
    public void closeAllOpenTags()
    {
        
    }
    
    public void closeAllTags()
    {
        
    }
    
    public String escapeTranslate(String escapeString)
    {
        if(escapeString.endsWith("z"))
        {
            int code=Util.s0_int(escapeString.substring(0,escapeString.length()-1));
            if(code<20)
                setModeAndExecute(code);
            else
            if(code<100)
            {
                
            }
            return "";
        }
        return escapeString;
    }

    public int processTag(StringBuffer buf, int i)
    {
        // first step is to parse the motherfather
        // if we can't parse it, we convert the < char at i into &lt;
        // remember, incomplete tags should nodify the main filterdude
        Vector parts=new Vector();
        int oldI=i;
        char lastC=' ';
        Vector quotes=new Vector();
        StringBuffer bit=new StringBuffer("");
        
        //allowing the ! and / as a second char in a tag is an EXCEPTION! 
        if(((i+1)<buf.length())&&((buf.charAt(i+1)=='!')||(buf.charAt(i+1)=='/')))
        {
            i++;
            bit.append(buf.charAt(i));
        }
        while((bit!=null)&&((++i)<buf.length()))
        {
            switch(buf.charAt(i))
            {
            case '\n':
            case '\r':
                buf.setCharAt(oldI,'&');
                buf.insert(oldI+1,"lt;");
                return 3;
            case ' ':
            case '\t':
                if(quotes.size()==0)
                {
                    if(bit.length()>0) parts.addElement(bit.toString());
                    bit.setLength(0);
                }
                else
                    bit.append(buf.charAt(i));
                break;
            case '"':
            case '\'':
                bit.append(buf.charAt(i));
                if((lastC=='=')
                ||(quotes.size()>0)
                ||((quotes.size()==0)&&((lastC==' ')||(lastC=='\t'))))
                {
                    if((quotes.size()>0)&&(((Character)quotes.lastElement()).charValue()==buf.charAt(i)))
                    {
                        quotes.removeElementAt(quotes.size()-1);
                        if(quotes.size()==0)
                        {
                            parts.addElement(bit.toString());
                            bit.setLength(0);
                        }
                    }
                    else
                        quotes.addElement(new Character(buf.charAt(i)));
                }
                break;
            case '<':
                if(quotes.size()>0)
                    bit.append(buf.charAt(i));
                else
                {
                    // argh! abort! abort!
                    buf.setCharAt(oldI,'&');
                    buf.insert(oldI+1,"lt;");
                    return 3;
                }
                break;
            case '>':
                if(quotes.size()>0)
                    bit.append(buf.charAt(i));
                else
                {
                    if(bit.length()>0) parts.add(bit.toString());
                    bit=null;
                }
                break;
            default:
                if((quotes.size()>0)
                ||(Character.isLetter(buf.charAt(i)))
                ||(bit.length()>0))
                    bit.append(buf.charAt(i));
                else
                {
                    // DANGER WILL ROBINSON! DANGER!
                    buf.setCharAt(oldI,'&');
                    buf.insert(oldI+1,"lt;");
                    return 3;
                }
                break;
            }
            lastC=buf.charAt(i);
        }
        // never hit the end, so let papa know
        if((i>=buf.length())&&(buf.charAt(buf.length()-1)!='>'))
            return Integer.MAX_VALUE;
        
        //nothing doin
        if((parts.size()==0)
        ||(!elements.containsKey(((String)parts.firstElement()).toUpperCase().trim())))
        {
            buf.setCharAt(oldI,'&');
            buf.insert(oldI+1,"lt;");
            return 3;
        }
        String tag=(String)parts.firstElement();
        MXPElement E=(MXPElement)elements.get(tag.toUpperCase().trim());
        if(E.getDefinition().length()>0)
        {
            
        }
        return 0;
    }
    public int processEntity(StringBuffer buf, int i)
    {
        boolean convertIt=false;
        int x=i;
        if((buf.charAt(i+1)=='#')&&(Character.isDigit(buf.charAt(i+2))))
        {
            x++; // skip to the hash, the next line will skip to the digit
            while((++x)<buf.length())
            {
                if(buf.charAt(x)==';')
                {
                    convertIt=false;
                    break;
                }
                else
                if(!Character.isDigit(buf.charAt(x)))
                {
                    convertIt=true;
                    break;
                }
            }
        }
        else
        while((++x)<buf.length())
        {
            if(buf.charAt(x)==';')
            {
                convertIt=false;
                break;
            }
            else
            if(!Character.isLetter(buf.charAt(x)))
            {
                convertIt=true;
                break;
            }
        }
        if(!convertIt) return 0;
        buf.insert(i+1,"amp;");
        return 4;
    }
    
}
