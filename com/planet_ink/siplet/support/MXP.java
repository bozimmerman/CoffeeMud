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
    private Hashtable tags=new Hashtable();
    private Hashtable entities=new Hashtable();
    private Vector openElements=new Vector();

    public MXP()
    {
        super();
        addElement(MXPElement.createHTMLElement("B","<B>",""));
        addElement(MXPElement.createHTMLElement("BOLD","<B>",""));
        addElement(MXPElement.createHTMLElement("STRONG","<B>",""));
        addElement(MXPElement.createHTMLElement("U","<U>",""));
        addElement(MXPElement.createHTMLElement("UNDERLINE","<U>",""));
        addElement(MXPElement.createHTMLElement("I","<I>",""));
        addElement(MXPElement.createHTMLElement("ITALIC","<I>",""));
        addElement(MXPElement.createHTMLElement("S","<S>",""));
        addElement(MXPElement.createHTMLElement("STRIKEOUT","<S>",""));
        addElement(MXPElement.createHTMLElement("EM","<I>",""));
        addElement(MXPElement.createHTMLElement("H1","<H1>",""));
        addElement(MXPElement.createHTMLElement("H2","<H2>",""));
        addElement(MXPElement.createHTMLElement("H3","<H3>",""));
        addElement(MXPElement.createHTMLElement("H4","<H4>",""));
        addElement(MXPElement.createHTMLElement("H5","<H5>",""));
        addElement(MXPElement.createHTMLElement("H6","<H6>",""));
        addElement(MXPElement.createHTMLCommand("HR","<HR>"));
        addElement(MXPElement.createHTMLElement("SMALL","<SMALL>",""));
        addElement(MXPElement.createHTMLElement("TT","<PRE>",""));
        addElement(MXPElement.createHTMLCommand("BR","<BR>"));
        addElement(MXPElement.createHTMLCommand("SBR","&nbsp;")); // not fully supported
        addElement(new MXPElement("P","","","",MXPElement.BIT_HTML|MXPElement.BIT_NEEDTEXT|MXPElement.BIT_SPECIAL));
        addElement(MXPElement.createMXPElement("C","<FONT COLOR=&fore; STYLE=\"background-color: &back;\">","FORE BACK"));
        addElement(MXPElement.createMXPElement("COLOR","<FONT COLOR=&fore; STYLE=\"background-color: &back;\">","FORE BACK"));
        addElement(MXPElement.createMXPElement("HIGH","","")); // not supported
        addElement(MXPElement.createMXPElement("H","","")); // not supported
        addElement(MXPElement.createMXPElement("FONT","<FONT FACE=&face; SIZE=&size; COLOR=&color; STYLE=\"background-color: &back;\">","FACE SIZE COLOR BACK"));
        addElement(new MXPElement("NOBR","","","",MXPElement.BIT_NEEDTEXT|MXPElement.BIT_SPECIAL));
        addElement(MXPElement.createMXPElement("A","<A HREF=&href; TITLE=&hint;>","HREF HINT EXPIRE"));
        addElement(MXPElement.createMXPElement("SEND","<A TITLE=&hint; HREF=\"javascript: Send('&href');\">","HREF HINT PROMPT EXPIRE"));
        addElement(MXPElement.createMXPCommand("EXPIRE","","NAME")); // not supported
        addElement(new MXPElement("VERSION","","","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("GAUGE","","ENTITY MAX CAPTION COLOR","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("STAT","","ENTITY MAX CAPTION","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("FRAME","","NAME ACTION TITLE INTERNAL ALIGN LEFT TOP WIDTH HEIGHT SCROLLING FLOATING","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("DEST","","NAME","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("RELOCATE","","URL PORT","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("USER","","","",0)); // not supported, yet
        addElement(new MXPElement("PASSWORD","","","",0)); // not supported, yet
        addElement(MXPElement.createMXPCommand("IMAGE","<IMG SRC=&src;&fname; HEIGHT=&h; WIDTH=&w; ALIGN=&align;>","FNAME URL T H W HSPACE VSPACE ALIGN ISMAP"));
        addElement(MXPElement.createMXPCommand("FILTER","","SRC DEST NAME")); // not supported
        addElement(MXPElement.createMXPCommand("SCRIPT","")); // not supported
        addElement(new MXPElement("ENTITY","","NAME VALUE DESC PRIVATE PUBLISH DELETE ADD","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("EN","","NAME VALUE DESC PRIVATE PUBLISH DELETE ADD","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("TAG","","INDEX WINDOWNAME FORE BACK GAG ENABLE DISABLE","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("VAR","","NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("V","","NAME DESC PRIVATE PUBLISH DELETE ADD REMOVE","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("ELEMENT","","NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("EL","","NAME DEFINITION ATT TAG FLAG OPEN DELETE EMPTY","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("ATTLIST","","NAME ATT","",MXPElement.BIT_SPECIAL));
        addElement(new MXPElement("AT","","NAME ATT","",MXPElement.BIT_SPECIAL));
        addElement(MXPElement.createMXPCommand("SOUND","!!SOUND(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)","FNAME V=100 L=1 P=50 T U"));
        addElement(MXPElement.createMXPCommand("MUSIC","!!MUSIC(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)","FNAME V=100 L=1 P=50 T U"));
        //-------------------------------------------------------------------------
        entities.put("nbsp",new MXPEntity("nbsp","&nbsp;"));
        entities.put("lt",new MXPEntity("lt","&lt;"));
        entities.put("gt",new MXPEntity("gt","&gt;"));
        entities.put("quot",new MXPEntity("quot","&quot;"));
        entities.put("amp",new MXPEntity("amp","&amp;"));
    }
    
    public void addElement(MXPElement E)
    {
        elements.put(E.name(),E);
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
            closeAllTags();
            setModeAndExecute(defaultMode);
            break;
        }
    }

    // does not close Secure tags -- they are never ever closed
    public void closeAllTags()
    {
        //TODO: check to see if we're waiting for text
        // no idea how that will be handled -- probably NOT!!!
    }
    
    public boolean isUIonHold()
    {
        MXPElement E=null;
        for(int i=0;i<openElements.size();i++)
        {
            E=(MXPElement)openElements.elementAt(i);
            if(E.needsText())
                return true;
        }
        return false;
    }
    
    public String closeTag(MXPElement E)
    {
        Vector endTags=E.getEndableTags(E.getDefinition());
        StringBuffer newEnd=new StringBuffer("");
        for(int e=endTags.size()-1;e>=0;e--)
            if(elements.containsKey((((String)endTags.elementAt(e))).toUpperCase().trim()))
                newEnd.append("</"+((String)endTags.elementAt(e)).toUpperCase().trim());
        return newEnd.toString();
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
                //TODO: process an MXP tag
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
        if((i>=buf.length())||(buf.charAt(i)!='>'))
            return Integer.MAX_VALUE;
        int endI=i+1;
        
        //nothing doin
        String tag=(parts.size()>0)?((String)parts.firstElement()).toUpperCase().trim():"";
        if(tag.startsWith("!")) tag=tag.substring(1);
        boolean endTag=tag.startsWith("/");
        if(endTag)tag=tag.substring(1);
        if((tag.length()==0)||(!elements.containsKey(tag)))
        {
            buf.setCharAt(oldI,'&');
            buf.insert(oldI+1,"lt;");
            return 3;
        }
        MXPElement E=(MXPElement)elements.get(tag.toUpperCase().trim());
        String text="";
        if(endTag)
        {
            int foundAt=-1;
            MXPElement troubleE=null;
            for(int x=openElements.size()-1;x>=0;x--)
            {
                E=(MXPElement)openElements.elementAt(x);
                if(E.name().equals(tag))
                {
                    foundAt=x;
                    openElements.removeElementAt(x);
                    break;
                }
                if(E.needsText())
                    troubleE=E;
            }
            buf.delete(oldI,endI);
            if(foundAt<0) return -1;
            
            // a close tag of an mxp element always erases an **INTERIOR** needstext element
            if(troubleE!=null)
                openElements.removeElement(troubleE);
            String close=closeTag(E);
            if(close.length()>0)
                buf.insert(oldI,close.toString());
            if(E.needsText())
            {
                text=buf.substring(E.getBufInsert(),oldI);
                oldI=E.getBufInsert();
            }
        }
        else
        {
            E=E.copyOf();
            E.saveSettings(oldI,parts);
            if(!E.isCommand())
                openElements.addElement(E);
            buf.delete(oldI,endI);
            if(E.needsText())
                return -1; // we want it to continue to look for closing tag 
        }
        if((endTag)&&(!E.isCommand())&&(E.getFlag().length()>0))
        {
            //TODO: process any flags that matter
        }
        if(E.isSpecialProcessor())
            specialElements(E);
        String totalDefinition=E.getFoldedDefinition(text);
        buf.insert(oldI,totalDefinition);
        if(E.isHTML())
            return totalDefinition.length()-1;
        return -1;
    }
    
    public void specialElements(MXPElement E)
    {
        addElement(new MXPElement("ENTITY","","NAME VALUE DESC PRIVATE PUBLISH DELETE ADD","",MXPElement.BIT_SPECIAL));
        if(E.name().equals("ELEMENT")||E.name().equals("EL"))
        {
            String name=E.getAttributeValue("NAME");
            String definition=E.getAttributeValue("DEFINITION");
            String attributes=E.getAttributeValue("ATT");
            String tag=E.getAttributeValue("TAG");
            String flags=E.getAttributeValue("FLAG");
            String OPEN=E.getAttributeValue("OPEN");
            String DELETE=E.getAttributeValue("DELETE");
            String EMPTY=E.getAttributeValue("EMPTY");
            if(name==null) return;
            if((DELETE!=null)&&(elements.containsKey(name)))
            {
                E=(MXPElement)elements.get(name);
                if(E.isOpen())
                    elements.remove(name);
                return;
            }
            if(definition==null) definition="";
            if(attributes==null) attributes="";
            int bitmap=0;
            if(OPEN!=null) bitmap|=MXPElement.BIT_OPEN;
            if(EMPTY!=null) bitmap|=MXPElement.BIT_COMMAND;
            MXPElement L=new MXPElement(name.toUpperCase().trim(),definition,attributes,flags,bitmap);
            elements.remove(L.name());
            elements.put(L.name(),L);
            if((tag!=null)&&(Util.isInteger(tag))&&(Util.s_int(tag)>19)&&(Util.s_int(tag)<100))
            {
                int tagNum=Util.s_int(tag);
                if(tags.containsKey(new Integer(tagNum))) tags.remove(new Integer(tagNum));
                tags.put(new Integer(tagNum),L);
            }
            return;
        }
        
        if(E.name().equals("ENTITY")||E.name().equals("EN"))
        {
            String name=E.getAttributeValue("NAME");
            String value=E.getAttributeValue("VALUE");
            //String desc=E.getAttributeValue("DESC");
            //String PRIVATE=E.getAttributeValue("PRIVATE");
            //String PUBLISH=E.getAttributeValue("PUBLISH");
            String DELETE=E.getAttributeValue("DELETE");
            String REMOVE=E.getAttributeValue("REMOVE");
            String ADD=E.getAttributeValue("ADD");
            if((name==null)||(name.length()==0)) return;
            if(DELETE!=null)
            {
                entities.remove(name);
                return;
            }
            if(REMOVE!=null)
            {
                // whatever a string list is (| separated things) this removes it
            }
            else
            if(ADD!=null)
            {
                // whatever a string list is (| separated things) this removes it
            }
            else
                entities.put(name,new MXPEntity(name,value));
            return;
        }
    }
    
    public int processEntity(StringBuffer buf, int i)
    {
        boolean convertIt=false;
        int oldI=i;
        StringBuffer content=new StringBuffer("");
        if((buf.charAt(i+1)=='#')&&(Character.isDigit(buf.charAt(i+2))))
        {
            i++; // skip to the hash, the next line will skip to the digit
            while((++i)<buf.length())
            {
                if(buf.charAt(i)==';')
                {
                    convertIt=false;
                    break;
                }
                else
                if(!Character.isDigit(buf.charAt(i)))
                {
                    convertIt=true;
                    break;
                }
            }
        }
        else
        while((++i)<buf.length())
        {
            if(buf.charAt(i)==';')
            {
                convertIt=false;
                break;
            }
            else
            if(!Character.isLetterOrDigit(buf.charAt(i)))
            {
                convertIt=true;
                break;
            }
            else
            if((!Character.isLetter(buf.charAt(i)))&&(content.length()==0))
            {
                convertIt=true;
                break;
            }
            content.append(buf.charAt(i));
        }
        if((i>=buf.length())&&(content.length()>0))
            return Integer.MAX_VALUE;
        if((convertIt)||(content.length()==0)||(buf.charAt(i)!=';'))
        {
            buf.insert(oldI+1,"amp;");
            return 4;
        }
        String tag=content.toString().trim();
        String val=null;
        for(int x=openElements.size()-1;x>=0;x--)
        {
            MXPElement E=(MXPElement)openElements.elementAt(x);
            val=E.getAttributeValue(tag);
            if(val!=null) break;
        }
        String oldValue=buf.substring(oldI,i);
        buf.delete(oldI,i);
        if(val!=null)
        {
            buf.insert(oldI,val);
            return -1;
        }
        MXPEntity N=(MXPEntity)entities.get(tag);
        if(N==null)
        {
            buf.insert(oldI+1,"amp;");
            return 4;
        }
        buf.insert(oldI+1,N.getDefinition());
        if(N.getDefinition().equals(oldValue))
            return oldValue.length()-1;
        return -1;
    }
    
}
