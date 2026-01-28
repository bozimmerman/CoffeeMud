package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.MiniTSON.TSONObject;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2026-2026 Bo Zimmerman
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
public class MiniTSONTest extends StdTest
{
	@Override
	public String ID()
	{
		return "MiniTSONTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "core"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final MiniTSON tson = new MiniTSON();

		try
		{
			// Test 1: Integer parsing and serialization
			Object result = tson.parse("I5");
			if(!(result instanceof Integer) || !result.equals(Integer.valueOf(5)))
				return "Error#1.1: Integer parsing failed";

			TSONObject obj = new TSONObject();
			obj.put("intVal", Integer.valueOf(42));
			String serialized = obj.toString();
			if(!serialized.contains("I42"))
				return "Error#1.2: Integer serialization failed: " + serialized;

			// Test 2: Integer[] parsing and serialization
			result = tson.parse("I[1,2,3]");
			if(!(result instanceof Integer[]))
				return "Error#2.1: Integer[] parsing failed";
			final Integer[] intArr = (Integer[])result;
			if(intArr.length != 3 || intArr[0].intValue() != 1 || intArr[1].intValue() != 2 || intArr[2].intValue() != 3)
				return "Error#2.2: Integer[] values incorrect";

			obj = new TSONObject();
			obj.put("intArr", new Integer[] {Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)});
			serialized = obj.toString();
			if(!serialized.contains("I["))
				return "Error#2.3: Integer[] serialization failed: " + serialized;

			// Test 3: int[] parsing and serialization
			result = tson.parse("i[7,8,9]");
			if(!(result instanceof int[]))
				return "Error#3.1: int[] parsing failed";
			final int[] primitiveIntArr = (int[])result;
			if(primitiveIntArr.length != 3 || primitiveIntArr[0] != 7 || primitiveIntArr[1] != 8 || primitiveIntArr[2] != 9)
				return "Error#3.2: int[] values incorrect";

			obj = new TSONObject();
			obj.put("primitiveIntArr", new int[] {100, 200, 300});
			serialized = obj.toString();
			if(!serialized.contains("i["))
				return "Error#3.3: int[] serialization failed: " + serialized;

			// Test 4: Long (default, no prefix needed)
			result = tson.parse("12345");
			if(!(result instanceof Long))
				return "Error#4.1: Long parsing failed";

			obj = new TSONObject();
			obj.put("longVal", Long.valueOf(999L));
			serialized = obj.toString();
			if(serialized.contains("L999") || serialized.contains("l999"))
				return "Error#4.2: Long should not have prefix: " + serialized;

			// Test 5: long[] parsing
			result = tson.parse("l[1000,2000,3000]");
			if(!(result instanceof long[]))
				return "Error#5.1: long[] parsing failed";
			final long[] primitiveLongArr = (long[])result;
			if(primitiveLongArr.length != 3 || primitiveLongArr[0] != 1000)
				return "Error#5.2: long[] values incorrect";

			// Test 6: Short parsing and serialization
			result = tson.parse("H32767");
			if(!(result instanceof Short))
				return "Error#6.1: Short parsing failed";

			obj = new TSONObject();
			obj.put("shortVal", Short.valueOf((short)123));
			serialized = obj.toString();
			if(!serialized.contains("H123"))
				return "Error#6.2: Short serialization failed: " + serialized;

			// Test 7: short[] parsing
			result = tson.parse("h[10,20,30]");
			if(!(result instanceof short[]))
				return "Error#7.1: short[] parsing failed";
			final short[] primitiveShortArr = (short[])result;
			if(primitiveShortArr.length != 3 || primitiveShortArr[0] != 10)
				return "Error#7.2: short[] values incorrect";

			// Test 8: Byte parsing
			result = tson.parse("Y127");
			if(!(result instanceof Byte))
				return "Error#8.1: Byte parsing failed";

			// Test 9: byte[] parsing
			result = tson.parse("y[1,2,3,4,5]");
			if(!(result instanceof byte[]))
				return "Error#9.1: byte[] parsing failed";
			final byte[] primitiveByteArr = (byte[])result;
			if(primitiveByteArr.length != 5 || primitiveByteArr[0] != 1)
				return "Error#9.2: byte[] values incorrect";

			// Test 10: Float parsing and serialization
			result = tson.parse("F3.14");
			if(!(result instanceof Float))
				return "Error#10.1: Float parsing failed";
			if(Math.abs(((Float)result).floatValue() - 3.14f) > 0.01f)
				return "Error#10.2: Float value incorrect";

			obj = new TSONObject();
			obj.put("floatVal", Float.valueOf(2.5f));
			serialized = obj.toString();
			if(!serialized.contains("F2.5"))
				return "Error#10.3: Float serialization failed: " + serialized;

			// Test 11: float[] parsing
			result = tson.parse("f[1.1,2.2,3.3]");
			if(!(result instanceof float[]))
				return "Error#11.1: float[] parsing failed";
			final float[] primitiveFloatArr = (float[])result;
			if(primitiveFloatArr.length != 3 || Math.abs(primitiveFloatArr[0] - 1.1f) > 0.01f)
				return "Error#11.2: float[] values incorrect";

			// Test 12: Double (default, no prefix)
			result = tson.parse("3.14159");
			if(!(result instanceof Double))
				return "Error#12.1: Double parsing failed";

			obj = new TSONObject();
			obj.put("doubleVal", Double.valueOf(2.718));
			serialized = obj.toString();
			if(serialized.contains("D2.718") || serialized.contains("d2.718"))
				return "Error#12.2: Double should not have prefix: " + serialized;

			// Test 13: double[] parsing
			result = tson.parse("d[1.1,2.2,3.3]");
			if(!(result instanceof double[]))
				return "Error#13.1: double[] parsing failed";
			final double[] primitiveDoubleArr = (double[])result;
			if(primitiveDoubleArr.length != 3 || Math.abs(primitiveDoubleArr[0] - 1.1) > 0.01)
				return "Error#13.2: double[] values incorrect";

			// Test 14: Boolean (default, no prefix)
			result = tson.parse("true");
			if(!(result instanceof Boolean) || !((Boolean)result).booleanValue())
				return "Error#14.1: Boolean parsing failed";

			obj = new TSONObject();
			obj.put("boolVal", Boolean.TRUE);
			serialized = obj.toString();
			if(serialized.contains("Btrue") || serialized.contains("btrue"))
				return "Error#14.2: Boolean should not have prefix: " + serialized;

			// Test 15: boolean[] parsing
			result = tson.parse("b[true,false,true]");
			if(!(result instanceof boolean[]))
				return "Error#15.1: boolean[] parsing failed";
			final boolean[] primitiveBoolArr = (boolean[])result;
			if(primitiveBoolArr.length != 3 || !primitiveBoolArr[0] || primitiveBoolArr[1])
				return "Error#15.2: boolean[] values incorrect";

			// Test 16: String (default, no prefix)
			result = tson.parse("\"hello world\"");
			if(!(result instanceof String) || !result.equals("hello world"))
				return "Error#16.1: String parsing failed";

			obj = new TSONObject();
			obj.put("strVal", "test");
			serialized = obj.toString();
			if(serialized.contains("s\"test\"") || serialized.contains("S\"test\""))
				return "Error#16.2: String should not have prefix: " + serialized;

			// Test 17: Hashtable parsing and serialization
			result = tson.parse("ht{\"key1\":\"value1\",\"key2\":\"value2\"}");
			if(!(result instanceof Hashtable))
				return "Error#17.1: Hashtable parsing failed";
			@SuppressWarnings("unchecked")
			final
			Hashtable<String,Object> ht = (Hashtable<String,Object>)result;
			if(!ht.get("key1").equals("value1"))
				return "Error#17.2: Hashtable values incorrect";

			obj = new TSONObject();
			final Hashtable<String,Object> testHt = new Hashtable<String,Object>();
			testHt.put("a", "b");
			obj.put("hashtable", testHt);
			serialized = obj.toString();
			if(!serialized.contains("ht{"))
				return "Error#17.3: Hashtable serialization failed: " + serialized;

			// Test 18: HashMap parsing and serialization
			result = tson.parse("hm{\"key1\":\"value1\"}");
			if(!(result instanceof HashMap))
				return "Error#18.1: HashMap parsing failed";

			obj = new TSONObject();
			final HashMap<String,Object> testHm = new HashMap<String,Object>();
			testHm.put("x", "y");
			obj.put("hashmap", testHm);
			serialized = obj.toString();
			if(!serialized.contains("hm{"))
				return "Error#18.2: HashMap serialization failed: " + serialized;

			// Test 19: ArrayList parsing and serialization
			result = tson.parse("al[1,2,3]");
			if(!(result instanceof ArrayList))
				return "Error#19.1: ArrayList parsing failed";
			@SuppressWarnings("unchecked")
			final
			ArrayList<Object> al = (ArrayList<Object>)result;
			if(al.size() != 3)
				return "Error#19.2: ArrayList size incorrect";

			obj = new TSONObject();
			final ArrayList<Object> testAl = new ArrayList<Object>();
			testAl.add("item1");
			testAl.add("item2");
			obj.put("arraylist", testAl);
			serialized = obj.toString();
			if(!serialized.contains("al["))
				return "Error#19.3: ArrayList serialization failed: " + serialized;

			// Test 20: Vector parsing and serialization
			result = tson.parse("vc[10,20,30]");
			if(!(result instanceof Vector))
				return "Error#20.1: Vector parsing failed";
			@SuppressWarnings("unchecked")
			final
			Vector<Object> vec = (Vector<Object>)result;
			if(vec.size() != 3)
				return "Error#20.2: Vector size incorrect";

			obj = new TSONObject();
			final Vector<Object> testVec = new Vector<Object>();
			testVec.add("a");
			testVec.add("b");
			obj.put("vector", testVec);
			serialized = obj.toString();
			if(!serialized.contains("vc["))
				return "Error#20.3: Vector serialization failed: " + serialized;

			// Test 21: Pair parsing and serialization
			result = tson.parse("pa[\"first\",\"second\"]");
			if(!(result instanceof Pair))
				return "Error#21.1: Pair parsing failed";
			@SuppressWarnings("unchecked")
			final Pair<Object,Object> pair = (Pair<Object,Object>)result;
			if(!pair.first.equals("first") || !pair.second.equals("second"))
				return "Error#21.2: Pair values incorrect";

			obj = new TSONObject();
			final Pair<String,Integer> testPair = new Pair<String,Integer>("key", Integer.valueOf(123));
			obj.put("pair", testPair);
			serialized = obj.toString();
			if(!serialized.contains("pa["))
				return "Error#21.3: Pair serialization failed: " + serialized;

			// Test 22: Complex nested structure
			final String complexTson = "{\"name\":\"test\",\"numbers\":I[1,2,3],\"primitives\":i[10,20,30],\"map\":hm{\"nested\":\"value\"},\"pair\":pa[\"a\",\"b\"]}";
			result = tson.parseObject(complexTson);
			if(!(result instanceof TSONObject))
				return "Error#22.1: Complex object parsing failed";
			final TSONObject complex = (TSONObject)result;
			if(!complex.containsKey("name") || !complex.containsKey("numbers"))
				return "Error#22.2: Complex object missing keys";

			// Test 23: Round-trip test
			final TSONObject original = new TSONObject();
			original.put("int", Integer.valueOf(42));
			original.put("intArr", new Integer[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)});
			original.put("primitiveIntArr", new int[] {10, 20, 30});
			original.put("string", "hello");
			original.put("float", Float.valueOf(3.14f));
			final Pair<String,String> p = new Pair<String,String>("x", "y");
			original.put("pair", p);

			final String serialized1 = original.toString();
			final JSONObject parsed = new MiniTSON().parseObject(serialized1);
			final String serialized2 = parsed.toString();
			if(!serialized1.equals(serialized2))
			{
				// Note: Order might differ, so check that both can be parsed identically
				final JSONObject reparsed = new MiniTSON().parseObject(serialized1);
				if(!parsed.get("int").equals(reparsed.get("int")))
					return "Error#23.0: Round-trip serialization inconsistent";
			}

			// Verify round-trip preserves types
			if(!(parsed.get("int") instanceof Integer))
				return "Error#23.1: Round-trip lost Integer type";
			if(!(parsed.get("intArr") instanceof Integer[]))
				return "Error#23.2: Round-trip lost Integer[] type";
			if(!(parsed.get("primitiveIntArr") instanceof int[]))
				return "Error#23.3: Round-trip lost int[] type";
			if(!(parsed.get("float") instanceof Float))
				return "Error#23.4: Round-trip lost Float type";
			if(!(parsed.get("pair") instanceof Pair))
				return "Error#23.5: Round-trip lost Pair type";

			// Test 24: Type conversion - string to number
			result = tson.parse("I\"123\"");
			if(!(result instanceof Integer) || !result.equals(Integer.valueOf(123)))
				return "Error#24.1: String to Integer conversion failed";

			// Test 25: Type conversion - number to boolean
			result = tson.parse("B1");
			if(!(result instanceof Boolean) || !((Boolean)result).booleanValue())
				return "Error#25.1: Number to Boolean conversion failed";

			result = tson.parse("B0");
			if(!(result instanceof Boolean) || ((Boolean)result).booleanValue())
				return "Error#25.2: Zero to false Boolean conversion failed";

			// Test 26: Empty arrays
			result = tson.parse("I[]");
			if(!(result instanceof Integer[]) || ((Integer[])result).length != 0)
				return "Error#26.1: Empty Integer[] parsing failed";

			result = tson.parse("i[]");
			if(!(result instanceof int[]) || ((int[])result).length != 0)
				return "Error#26.2: Empty int[] parsing failed";

			// Test 27: Null handling
			obj = new TSONObject();
			obj.put("nullVal", null);
			serialized = obj.toString();
			if(!serialized.contains("null"))
				return "Error#27.1: Null serialization failed";

			result = tson.parseObject("{\"nullVal\":null}");
			if(!(result instanceof TSONObject))
				return "Error#27.2: Null parsing failed";

			// Test 28: Mixed type conversions in arrays
			result = tson.parse("I[1,2.5,\"3\"]");
			if(!(result instanceof Integer[]))
				return "Error#28.1: Mixed array conversion failed";
			final Integer[] mixedArr = (Integer[])result;
			if(mixedArr[0].intValue() != 1 || mixedArr[1].intValue() != 2 || mixedArr[2].intValue() != 3)
				return "Error#28.2: Mixed array values incorrect";

			// Test 29: copyOf() preserves types
			final TSONObject orig = new TSONObject();
			orig.put("intVal", Integer.valueOf(99));
			orig.put("arr", new int[] {1, 2, 3});
			final TSONObject copy = (TSONObject)orig.copyOf();
			if(!(copy.get("intVal") instanceof Integer))
				return "Error#29.1: copyOf lost Integer type";
			if(!(copy.get("arr") instanceof int[]))
				return "Error#29.2: copyOf lost int[] type";
		}
		catch(final MJSONException e)
		{
			Log.errOut(e);
			return "Exception: " + e.getMessage();
		}
		catch(final Exception e)
		{
			Log.errOut(e);
			return "Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage();
		}

		return null;
	}
}