package com.planet_ink.siplet.support;
import java.util.*;

public class MXPElement implements Cloneable
{
    private String definition="";
    private String attributes="";
    private String flag="";
    private int tag=-1;
    private boolean open=false;
    private boolean command=false;
    private boolean isFinalElement=false;
    private boolean specialProcessing=false;
    private Vector parsedAttributes=null;
    private Hashtable defaultAttributeValues=null;
    
    public MXPElement(String theDefinition,
                      String theAttributes,
                      String theFlag,
                      int theTag,
                      boolean isOpen,
                      boolean isCommand,
                      boolean isFinal,
                      boolean isSpecialProcessor)
    {
        super();
        definition=theDefinition;
        attributes=theAttributes;
        flag=theFlag;
        tag=theTag;
        open=isOpen;
        command=isCommand;
        isFinalElement=isFinal;
        specialProcessing=isSpecialProcessor;
    }
    
    public static MXPElement MXPFinalCommand(String definition)
    {
        return new MXPElement(definition,"","",-1,false,true,true,false);
    }
    public static MXPElement MXPFinalCommand(String definition, String attributes)
    {
        return new MXPElement(definition,attributes,"",-1,false,true,true,false);
    }
    public static MXPElement MXPFinalElement(String definition, String attributes)
    {
        return new MXPElement(definition,attributes,"",-1,false,false,true,false);
    }
    
    public boolean isCommand(){return command;}
    public boolean isFinal(){return isFinalElement;}
    public boolean isOpen(){return open;}
    public boolean isSpecialProcessor(){return specialProcessing;}
    public String getDefinition(){return definition;}
    public String getAttributes(){return attributes;}
    public void setAttributes(String newAttributes)
    { 
        attributes=newAttributes;
        parsedAttributes=null;
        defaultAttributeValues=null;
    }
    public String getDefaultValue(String tag)
    {
        getParsedAttributes();
        String s=(String)defaultAttributeValues.get(tag.toUpperCase().trim());
        if(s==null) return "";
        return s;
    }
    public synchronized Vector getParsedAttributes()
    {
        if(parsedAttributes!=null) return parsedAttributes;
        parsedAttributes=new Vector();
        defaultAttributeValues=new Hashtable();
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
                    defaultAttributeValues.put(tag,bit);
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
    public int getTag(){return tag;}
}
