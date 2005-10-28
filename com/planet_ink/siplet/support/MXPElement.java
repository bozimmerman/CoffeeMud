package com.planet_ink.siplet.support;
import java.util.*;

public class MXPElement implements Cloneable
{
    public static final int BIT_OPEN=1;
    public static final int BIT_COMMAND=2;
    public static final int BIT_NEEDTEXT=4;
    public static final int BIT_SPECIAL=8;
    public static final int BIT_HTML=16;
    
    private String name="";
    private String definition="";
    private String attributes="";
    private String flag="";
    private int bitmap=0;
    private Vector parsedAttributes=null;
    private Hashtable attributeValues=null;
    
    private int bufInsert=-1;
    private Vector userParms=new Vector();
    
    public MXPElement(String newName,
                      String theDefinition,
                      String theAttributes,
                      String theFlag,
                      int theBitmap)
    {
        super();
        name=newName;
        definition=theDefinition;
        attributes=theAttributes;
        flag=theFlag;
        bitmap=theBitmap;
        if((!isCommand())
        &&(theDefinition.toUpperCase().indexOf("&TEXT;")>=0))
              bitmap=bitmap|BIT_NEEDTEXT;
    }
    
    public MXPElement copyOf()
    {
        try
        {
            return (MXPElement)this.clone();
        }
        catch(Exception e){};
        return null;
    }

    public static MXPElement createMXPCommand(String name, String definition)
    {
        return new MXPElement(name,definition,"","",BIT_COMMAND);
    }
    public static MXPElement createHTMLCommand(String name,String definition)
    {
        return new MXPElement(name,definition,"","",BIT_COMMAND|BIT_HTML);
    }
    public static MXPElement createMXPCommand(String name, String definition, String attributes)
    {
        return new MXPElement(name,definition,attributes,"",BIT_COMMAND);
    }
    public static MXPElement createMXPElement(String name, String definition, String attributes)
    {
        return new MXPElement(name,definition,attributes,"",0);
    }
    public static MXPElement createHTMLElement(String name, String definition, String attributes)
    {
        return new MXPElement(name,definition,attributes,"",BIT_HTML);
    }

    public String name(){return name;}
    public void setName(String newName){name=newName;}
    public boolean isCommand(){return Util.bset(bitmap,BIT_COMMAND);}
    public boolean isOpen(){return Util.bset(bitmap,BIT_OPEN);}
    public boolean isHTML(){return Util.bset(bitmap,BIT_HTML);}
    public boolean isSpecialProcessor(){return Util.bset(bitmap,BIT_SPECIAL);}
    public String getDefinition(){return definition;}
    public String getAttributes(){return attributes;}
    public boolean needsText(){return Util.bset(bitmap,BIT_NEEDTEXT);}
    public void setAttributes(String newAttributes)
    { 
        attributes=newAttributes;
        parsedAttributes=null;
        attributeValues=null;
    }
    public String getAttributeValue(String tag)
    {
        getParsedAttributes();
        return (String)attributeValues.get(tag.toUpperCase().trim());
    }
    
    public synchronized Vector getParsedAttributes()
    {
        if(parsedAttributes!=null) return parsedAttributes;
        parsedAttributes=new Vector();
        attributeValues=new Hashtable();
        StringBuffer buf=new StringBuffer(attributes.trim());
        StringBuffer bit=new StringBuffer("");
        Vector quotes=new Vector();
        int i=-1;
        char lastC=' ';
        boolean firstEqual=false;
        while((bit!=null)&&((++i)<buf.length()))
        {
            switch(buf.charAt(i))
            {
            case '=':
                if((!firstEqual)&&(bit.length()>0))
                {
                    String tag=bit.toString().toUpperCase().trim();
                    bit=new StringBuffer("");
                    parsedAttributes.addElement(tag);
                    attributeValues.put(tag,bit);
                }
                else
                    bit.append(buf.charAt(i));
                firstEqual=true;
                break;
            case '\n':
            case '\r':
            case ' ':
            case '\t':
                if(quotes.size()==0)
                {
                    if((!firstEqual)&&(bit.length()>0))
                        parsedAttributes.addElement(bit.toString().toUpperCase().trim());
                    bit=new StringBuffer("");
                    firstEqual=false;
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
                            if((!firstEqual)&&(bit.length()>0))
                                parsedAttributes.addElement(bit.toString().toUpperCase().trim());
                            bit=new StringBuffer("");
                            firstEqual=false;
                        }
                    }
                    else
                        quotes.addElement(new Character(buf.charAt(i)));
                }
                break;
            default:
                bit.append(buf.charAt(i));
                break;
            }
            lastC=buf.charAt(i);
        }
        if((!firstEqual)&&(bit.length()>0))
            parsedAttributes.addElement(bit.toString().toUpperCase().trim());
        return parsedAttributes;
    }
    public String getFlag(){return flag;}
    public void saveSettings(int insertPoint, Vector theUserParms)
    {
        bufInsert=insertPoint;
        userParms=theUserParms;
    }
    public int getBufInsert(){return bufInsert;}
    public void deleteAttribute(String name)
    {
        Vector aV=getParsedAttributes();
        attributeValues.remove(name.toUpperCase().trim());
    }
    
    public Vector getEndableTags(String desc)
    {
        StringBuffer buf=new StringBuffer(desc);
        Vector tags=new Vector();
        StringBuffer bit=null;
        Vector quotes=new Vector();
        int i=-1;
        while((++i)<buf.length())
        {
            switch(buf.charAt(i))
            {
            case '<':
                if(quotes.size()>0)
                {
                    bit=null;
                    break;
                }
                else
                if(bit!=null)
                    return tags;
                else
                    bit=new StringBuffer("");
                break;
            case '>':
                if((quotes.size()==0)&&(bit!=null))
                    tags.add(bit.toString().toUpperCase().trim());
                bit=null;
                break;
            case ' ':
            case '\t':
                if((quotes.size()==0)&&(bit!=null))
                    tags.add(bit.toString().toUpperCase().trim());
                bit=null;
                break;
            case '"':
            case '\'':
                if((quotes.size()>0)&&(((Character)quotes.lastElement()).charValue()==buf.charAt(i)))
                    quotes.removeElementAt(quotes.size()-1);
                else
                    quotes.addElement(new Character(buf.charAt(i)));
                bit=null;
                break;
            default:
                if((bit!=null)&&(Character.isLetterOrDigit(buf.charAt(i))))
                    bit.append(buf.charAt(i));
                else
                    bit=null;
                break;
            }
            break;
        }
        return tags;
    }
    
    public String getFoldedDefinition(String text)
    {
        Vector aV=getParsedAttributes();
        attributeValues.remove("TEXT");
        attributeValues.put("TEXT",text);
        if((userParms!=null)&&(userParms.size()>0))
        {
            int position=-1;
            String avParm=null;
            String userParm=null;
            for(int u=0;u<userParms.size();u++)
            {
                userParm=((String)userParms.elementAt(u)).toUpperCase().trim();
                boolean found=false;
                for(int a=0;a<aV.size();a++)
                {
                    avParm=(String)aV.elementAt(a);
                    if((userParm.startsWith(avParm+"="))||(avParm.equals(userParm)))
                    {
                        found=true;
                        if(a>position) position=a;
                        attributeValues.remove(avParm);
                        String val=(avParm.equals(userParm))?"":((String)userParms.elementAt(u)).trim().substring(avParm.length()+1);
                        attributeValues.put(avParm,val);
                        break;
                    }
                }
                if((!found)&&(position<(aV.size()-1)))
                {
                    position++;
                    avParm=(String)aV.elementAt(position);
                    attributeValues.remove(avParm);
                    attributeValues.put(avParm,((String)userParms.elementAt(u)).trim());
                }
            }
        }
        return definition;
    }
}
