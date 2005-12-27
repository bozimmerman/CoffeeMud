package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

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
/**
 * The base of those objects whose displayable english strings can or are contained
 * in script text files in the resources/scripts/[language]/*.properties files
 * @author Bo Zimmerman
 *
 */
public abstract class ForeignScriptable
{
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @return the value of the string referenced.
     */
	public static String getScr(String filename, String strname)
	{
		ResourceBundle scripts=Scripts.load(filename);
		if(scripts==null) return "";
		if(scripts.getString(strname)!=null)
			return scripts.getString(strname);
		return "";
	}
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @return the value of the string referenced.
     */
	public static String getScr(String filename, String strname, String replaceX)
	{
		String msg=getScr(filename,strname);
		if(msg.length()>0)
			msg=CMStrings.replaceAll(msg,"@x1",replaceX);
		return msg;
	}
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @param replaceX2 what to replace @x2 in the resulting string with
     * @return the value of the string referenced.
     */
	public static String getScr(String filename, String strname, String replaceX, String replaceX2)
	{
		String msg=getScr(filename,strname);
		if(msg.length()>0)
		{
			msg=CMStrings.replaceAll(msg,"@x1",replaceX);
			msg=CMStrings.replaceAll(msg,"@x2",replaceX2);
		}
		return msg;
	}
    
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @param replaceX2 what to replace @x2 in the resulting string with
     * @param replaceX3 what to replace @x3 in the resulting string with
     * @return the value of the string referenced.
     */
	public static String getScr(String filename, 
                                String strname, 
								String replaceX, 
								String replaceX2, 
								String replaceX3)
	{
		String msg=getScr(filename,strname);
		if(msg.length()>0)
		{
			msg=CMStrings.replaceAll(msg,"@x1",replaceX);
			msg=CMStrings.replaceAll(msg,"@x2",replaceX2);
			msg=CMStrings.replaceAll(msg,"@x3",replaceX3);
		}
		return msg;
	}
    
    
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @param replaceX2 what to replace @x2 in the resulting string with
     * @param replaceX3 what to replace @x3 in the resulting string with
     * @param replaceX4 what to replace @x4 in the resulting string with
     * @return the value of the string referenced.
     */
    public static String getScr(String filename, 
                                String strname, 
                                String replaceX, 
                                String replaceX2, 
                                String replaceX3, 
                                String replaceX4)
    {
        String msg=getScr(filename,strname);
        if(msg.length()>0)
        {
            msg=CMStrings.replaceAll(msg,"@x1",replaceX);
            msg=CMStrings.replaceAll(msg,"@x2",replaceX2);
            msg=CMStrings.replaceAll(msg,"@x3",replaceX3);
            msg=CMStrings.replaceAll(msg,"@x4",replaceX4);
        }
        return msg;
    }
    
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @param replaceX2 what to replace @x2 in the resulting string with
     * @param replaceX3 what to replace @x3 in the resulting string with
     * @param replaceX4 what to replace @x4 in the resulting string with
     * @param replaceX5 what to replace @x5 in the resulting string with
     * @return the value of the string referenced.
     */
    public static String getScr(String filename, 
                                String strname, 
                                String replaceX, 
                                String replaceX2, 
                                String replaceX3, 
                                String replaceX4, 
                                String replaceX5)
    {
        String msg=getScr(filename,strname);
        if(msg.length()>0)
        {
            msg=CMStrings.replaceAll(msg,"@x1",replaceX);
            msg=CMStrings.replaceAll(msg,"@x2",replaceX2);
            msg=CMStrings.replaceAll(msg,"@x3",replaceX3);
            msg=CMStrings.replaceAll(msg,"@x4",replaceX4);
            msg=CMStrings.replaceAll(msg,"@x5",replaceX5);
        }
        return msg;
    }
    /**
     * Return the displayable english string contained in resources/scripts directory for
     * the language from the coffeemud.ini file, and the properties file defined by
     * the filename passed herein.  The particular string in that file is returned
     * based on the strname field passed herein.
     * @see ForeignScriptable
     * @param filename The scripts file to look in
     * @param strname the string in the scripts file to return
     * @param replaceX what to replace @x1 in the resulting string with
     * @param replaceX2 what to replace @x2 in the resulting string with
     * @param replaceX3 what to replace @x3 in the resulting string with
     * @param replaceX4 what to replace @x4 in the resulting string with
     * @param replaceX5 what to replace @x5 in the resulting string with
     * @param replaceX6 what to replace @x6 in the resulting string with
     * @return the value of the string referenced.
     */
    public static String getScr(String filename, 
                                String strname, 
                                String replaceX, 
                                String replaceX2, 
                                String replaceX3, 
                                String replaceX4, 
                                String replaceX5,
                                String replaceX6)
    {
        String msg=getScr(filename,strname);
        if(msg.length()>0)
        {
            msg=CMStrings.replaceAll(msg,"@x1",replaceX);
            msg=CMStrings.replaceAll(msg,"@x2",replaceX2);
            msg=CMStrings.replaceAll(msg,"@x3",replaceX3);
            msg=CMStrings.replaceAll(msg,"@x4",replaceX4);
            msg=CMStrings.replaceAll(msg,"@x5",replaceX5);
            msg=CMStrings.replaceAll(msg,"@x6",replaceX6);
        }
        return msg;
    }
}

