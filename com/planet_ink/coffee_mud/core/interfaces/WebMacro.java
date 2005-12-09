package com.planet_ink.coffee_mud.interfaces;
import java.util.*;
import com.planet_ink.coffee_mud.system.http.*;
import com.planet_ink.coffee_mud.exceptions.*;

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
public interface WebMacro extends CMObject
{
	public String ID();
	public String name();

    public boolean preferBinary();
	public boolean isAdminMacro();
    public boolean isAWebPath();
    public String getFilename(ExternalHTTPRequests httpReq, String filename);
    public byte[] runBinaryMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException;
	public String runMacro(ExternalHTTPRequests httpReq, String parm) throws HTTPServerException;
}