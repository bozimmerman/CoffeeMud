package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2023 Bo Zimmerman

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
 * Text/String encoders and decoders.  Some encrypters, hashers, that sort of
 * thing.
 *
 * @author Bo Zimmerman
 *
 */
public interface TextEncoders extends CMLibrary
{
	/**
	 * Decompress a string from a binary data buffer.
	 *
	 * @see TextEncoders#compressString(String)
	 *
	 * @param b the compressed data buffer
	 * @return the uncompressed string
	 */
	public String decompressString(byte[] b);

	/**
	 * Compress a string into a binary data buffer.
	 *
	 * @see TextEncoders#decompressString(byte[])
	 *
	 * @param s the uncompressed string
	 * @return the compressed data buffer
	 */
	public byte[] compressString(String s);

	/**
	 * Checks whether hash passwords are used, and if so, hashes
	 * the password and returns the encoded string, and otherwise,
	 * just returns the raw password back.
	 *
	 * @param rawPassword the raw password straight from the user
	 * @return the final password to store
	 */
	public String makeFinalPasswordString(final String rawPassword);

	/**
	 * Compares two passwords to see if they are the same.  It does not
	 * matter which is which, or which is encoded and which is not, or
	 * anything else.  This Just Works.
	 *
	 * @param pass1 one of the passwords
	 * @param pass2 the other password
	 * @return true if they match, false otherwise
	 */
	public boolean passwordCheck(final String pass1, final String pass2);

	/**
	 * Generates a random 10 char password string.
	 * Not encoded or anything.
	 *
	 * @return a random password string
	 */
	public String generateRandomPassword();

	/**
	 * Hashes the given string and returns an encoding of it.
	 *
	 * @param str the string to encode
	 * @return the encoded string
	 */
	public String makeRepeatableHashString(final String str);

	/**
	 * Decrypts a weakly encrypted string.
	 *
	 * @see TextEncoders#filterEncrypt(String)
	 * @see TextEncoders#filterDecrypt(String)
	 *
	 * @param str the string to decrypt
	 * @return the original string
	 */
	public String filterDecrypt(String str);

	/**
	 * Weakly encrypts a string.
	 *
	 * @see TextEncoders#filterDecrypt(String)
	 *
	 * @param str the string to encrypt
	 * @return the encrypted string
	 */
	public String filterEncrypt(String str);
}
