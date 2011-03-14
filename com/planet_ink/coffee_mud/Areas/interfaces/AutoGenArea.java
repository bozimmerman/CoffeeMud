package com.planet_ink.coffee_mud.Areas.interfaces;

import java.util.List;
import java.util.Map;

import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.interfaces.*;
/* 
Copyright 2000-2011 Bo Zimmerman

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
 * AutoGenAreas are areas that utilize the random area generation system (percolator)
 * to generate their rooms.  As such, the common parameters for the auto gen system
 * are herein exposed
 * @author Bo Zimmerman
 */
public interface AutoGenArea extends Area
{
	/**
	 * Get the path to the xml file to use to generate this areas rooms 
	 * @return the path
	 */
	public String getGeneratorXmlPath();
	
	/**
	 * Set the path to the xml file to use to generate this areas rooms
	 * @param path the resource path
	 */
	public void setGeneratorXmlPath(String path);
	
	/**
	 * Get a list of area type ids, for when the xml file contains options 
	 * @return the list of ids
	 */
	public List<String> getXmlAreaTypeIds();
	
	/**
	 * Set a list of area type ids, for when the xml file contains options
	 * @param list the ids
	 */
	public void setXmlAreaTypeIds(List<String> list);
	
	/**
	 * Set a list of area type ids, for when the xml file contains options
	 * Formula is comma delimited
	 * @param list the ids
	 */
	public void setXmlAreaTypeIds(String commaList);
	
	/**
	 * Get a list of area theme ids, for when the xml file contains options 
	 * @return the list of ids
	 */
	public List<String> getXmlThemeIds();
	
	/**
	 * Set a list of area theme ids, for when the xml file contains options
	 * @param list the ids
	 */
	public void setXmlThemeIds(List<String> list);
	
	/**
	 * Set a list of area theme ids, for when the xml file contains options
	 * Formula is comma delimited
	 * @param list the ids
	 */
	public void setXmlThemeIds(String commaList);
	
	/**
	 * Get a math formula describing how to set the size of the area
	 * x1 = level of the first user to enter
	 * @return the formula 
	 */
	public String getAreaSizeFormula();
	
	/**
	 * Set a math formula describing how to set the size of the area
	 * x1 = level of the first user to enter
	 * @param formula the size formula
	 */
	public void setAreaSizeFormula(String formula);
	
	/**
	 * Get a math formula describing the pct chance that mobs
	 * are aggressive
	 * x1 = level of the first user to enter
	 * @return the formula 
	 */
	public String getAggroFormula();
	
	/**
	 * Set a math formula describing the pct chance that mobs
	 * are aggressive
	 * x1 = level of the first user to enter
	 * @param formula the size formula
	 */
	public void setAggroFormula(String formula);
	
	/**
	 * Get a math formula describing how to set the mob level for the area
	 * x1 = level of the first user to enter
	 * @return the formula
	 */
	public String getAreaLevelFormula();
	
	/**
	 * Set a math formula describing how to set the mob level for the area
	 * x1 = level of the first user to enter
	 * @param formula the level formula
	 */
	public void setAreaLevelFormula(String formula);
	
	/**
	 * Get a miscellaneous, xml-specific set of other vars to set
	 * when generating a new area
	 * @return the variable mappings
	 */
	public Map<String,String> getOtherAutoGenVars();
	
	/**
	 * Set a miscellaneous, xml-specific set of other vars to set
	 * when generating a new area
	 * @param vars the variable mappings
	 */
	public void setOtherAutoGenVars(Map<String,String> vars);
	
	/**
	 * Set a miscellaneous, xml-specific set of other vars to set
	 * when generating a new area. Format is VAR=VALUE VAR2="VALUE"
	 * @param vars the variable mappings
	 */
	public void setOtherAutoGenVars(String vars);
}
