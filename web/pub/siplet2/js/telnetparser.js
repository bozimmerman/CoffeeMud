var TELOPT =
{
	ESC: 27,
	IAC: 0xFF,
	BINARY: 0,
	ECHO: 1,
	LOGOUT: 18,
	SUPRESS_GO_AHEAD: 3,
	TERMTYPE: 24,
	NAWS: 31,
	TOGGLE_FLOW_CONTROL: 33,
	LINEMODE: 34,
	MSDP: 69,
	MSSP: 70,
	COMPRESS: 85,
	COMPRESS2: 86,
	MSP: 90,
	MXP: 91,
	AARD: 102,
	SE: 0xF0,
	AYT: 0xF6,
	EC: 0xF7,
	ATCP: 0xC8,
	GMCP: 0xC9,
	SB: 0xFA,
	WILL: 0xFB,
	WONT: 0xFC,
	ANSI16: 0xFC,
	DO: 0xFD,
	ANSI: 0xFD,
	DONT: 0xFE,
	GA: 0xF9,
	NOP: 0xF1,
	BINARY: 0,
	EOR: 25,
	ECHO: 1,
	LOGOUT: 18,
	TTYPE: 24
};

var MSDPOP =
{
	MSDP_VAR: 1,
	MSDP_VAL: 2,
	MSDP_TABLE_OPEN: 3,
	MSDP_TABLE_CLOSE: 4,
	MSDP_ARRAY_OPEN: 5,
	MSDP_ARRAY_CLOSE: 6
}

var StringToAsciiArray = function(str) {
	var arr = [];
	for(var i=0;i<str.length;i++)
		arr.push(str.charCodeAt(i));
	return arr;
};

var TELNET = function(sipwin)
{
	this.reset = function()
	{
		this.neverSupportMSP = !(getConfig("window/term/msp",'true') === 'true');
		this.neverSupportMXP = !(getConfig("window/term/mxp",'true') === 'true');
		this.neverSupportMSDP= !(getConfig("window/term/msdp",'true') === 'true');
		this.neverSupportGMCP = !(getConfig("window/term/gmcp",'true') === 'true');
		this.neverSupportMCCP = true; //TODO: maybe add pako later?
		this.msdpInforms = "";
		this.gmcpInforms = "";
	};
	this.reset();
	
	this.process = function(dat)
	{
		var response = [];
		//TODO:BZ:DELME
		//var s='>' + new Date().getTime()+'>';for(var i=0;i<dat.length;i++)s+=dat[i]+',';console.log(s);
		switch (dat[0])
		{
		case TELOPT.SB:
		{
			var subOptionData = [];
			var subOptionCode = dat[1];
			var last = 0;
			var i=1;
			while ((i < (dat.length - 1)) && ((last = dat[++i]) != -1))
			{
				if ((last == TELOPT.IAC) && (i < (dat.length - 1)))
				{
					last = dat[++i];
					if (last == TELOPT.IAC)
						subOptionData.push(TELOPT.IAC);
					else
					if (last == TELOPT.SE)
						break;
				}
				else
					subOptionData.push(last);
			}
			if (subOptionCode == TELOPT.TTYPE)
			{
				response = response.concat([TELOPT.IAC, TELOPT.SB, TELOPT.TTYPE, 0]);
				response = response.concat(StringToAsciiArray("siplet"));
				response = response.concat([TELOPT.IAC, TELOPT.SE]);
			}
			else
			if (subOptionCode == TELOPT.NAWS)
			{
				response = response.concat([
					TELOPT.IAC, TELOPT.SB, TELOPT.NAWS,
						0,sipwin.width,0,sipwin.height,
					TELOPT.IAC, TELOPT.SE]);
			}
			else
			if (subOptionCode == TELOPT.COMPRESS2)
			{
				// probably need to handle this earlier
			}
			else
			if (subOptionCode == TELOPT.MSDP)
			{
				if(subOptionData.length > 1)
				{
					if(subOptionData[subOptionData.length-1] == 255)
						subOptionData.splice(subOptionData.length-1);
					var received = this.msdpReceive(subOptionData);
					msdpInforms += received;
					//TODO: really, we just accumulate it?!
				}
			}
			else
			if (subOptionCode == TELOPT.GMCP)
			{
				if(subOptionData.length > 1)
				{
					if(subOptionData[subOptionData.length-1] == 255)
						subOptionData.splice(subOptionData.length-1);
					var received = this.gmcpReceive(subOptionData);
					this.gmcpInforms += received + "\n";
					//TODO: really, we just accumulate it?!
				}
			}
			break;
		}
		case TELOPT.WILL:
			switch(dat[1])
			{
			case TELOPT.NAWS:
				response = response.concat([
					TELOPT.IAC, TELOPT.SB, TELOPT.NAWS,
						0,sipwin.width,0,sipwin.height,
					TELOPT.IAC, TELOPT.SE]);
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (sipwin.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
						sipwin.MSPsupport = false;
					}
				}
				else
				if (!sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSP]);
					sipwin.MSPsupport = true;
				}
				break;
			case TELOPT.MSDP:
				if (this.neverSupportMSDP)
				{
					if (sipwin.MSDPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
						sipwin.MSDPsupport = false;
					}
				}
				else
				if (!sipwin.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MSDP]);
					sipwin.MSDPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (sipwin.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
						sipwin.GMCPsupport = false;
					}
				}
				else
				if (!sipwin.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.GMCP]);
					sipwin.GMCPsupport =true;
					response = response.concat([TELOPT.IAC, TELOPT.SB,TELOPT.GMCP]);
					var gmcpMsg = "core.hello {\"client\":\"siplet\",\"version\":" 
								+ sipwin.siplet.VERSION_MAJOR + "}";
					response = response.concat(StringToAsciiArray(gmcpMsg));
					response = response.concat([TELOPT.IAC, TELOPT.SE]);
				}
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (sipwin.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
						sipwin.MXPsupport = false;
					}
				}
				else
				if (!sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MXP]);
					sipwin.MXPsupport = true;
				}
				break;
			case TELOPT.COMPRESS2:
				if (this.neverSupportMCCP)
				{
					if (sipwin.MCCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MCCP]);
						sipwin.MCCPsupport = false;
					}
				}
				else
				if (!sipwin.MCCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DO, TELOPT.MCCP]);
					sipwin.MCCPsupport = false;
					//TODO: it cant get here, but when it does, fix it.
				}
				break;
			case TELOPT.LOGOUT:
				// goot for you serverdude
				break;
			case TELOPT.BINARY:
				response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.BINARY]);
				break;
			};
			break;
		case TELOPT.WONT:
			switch(dat[1])
			{
			case TELOPT.MSP:
				if (sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSP]);
					sipwin.MSPsupport = false;
				}
				break;
			case TELOPT.MSDP:
				if (sipwin.MSDPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MSDP]);
					sipwin.MSDPsupport = false;
				}
				break;
			case TELOPT.GMCP:
				if (sipwin.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.GMCP]);
					sipwin.GMCPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.DONT, TELOPT.MXP]);
					sipwin.MXPsupport = false;
					//if (mxpModule != null) mxpModule.shutdownMXP();
				}
				break;
			};
			break;
		case TELOPT.DO:
			switch(dat[1])
			{
			case TELOPT.TERMTYPE:
				break;
			case TELOPT.MSP:
				if (this.neverSupportMSP)
				{
					if (sipwin.MSPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
						sipwin.MSPsupport = false;
					}
				}
				else
				if (!sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MSP]);
					sipwin.MSPsupport = true;
				}
				break;
			case TELOPT.GMCP:
				if (this.neverSupportGMCP)
				{
					if (sipwin.GMCPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.GMCP]);
						sipwin.GMCPsupport = false;
					}
				}
				else
				if (!sipwin.GMCPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.GMCP]);
					sipwin.GMCPsupport = true;
				}
				break;
			case TELOPT.LOGOUT:
				// good for you serverdude
				break;
			case TELOPT.MXP:
				if (this.neverSupportMXP)
				{
					if (sipwin.MXPsupport)
					{
						response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
						sipwin.MXPsupport = false;
					}
				}
				else
				if (!sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WILL, TELOPT.MXP]);
					sipwin.MXPsupport = true;
				}
				break;
			default:
				if (dat[1] != TELOPT.BINARY)
					response = response.concat([TELOPT.IAC, TELOPT.WONT, dat[1]]);
				break;
			}
			break;
		case TELOPT.DONT:
			switch(dat[1])
			{
			case TELOPT.MSP:
				if (sipwin.MSPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MSP]);
					sipwin.MSPsupport = false;
				}
				break;
			case TELOPT.MXP:
				if (sipwin.MXPsupport)
				{
					response = response.concat([TELOPT.IAC, TELOPT.WONT, TELOPT.MXP]);
					sipwin.MXPsupport = false;
					//if (mxpModule != null) mxpModule.shutdownMXP();
				}
				break;
			case TELOPT.GA:
			case TELOPT.NOP:
				break;
			}
			break;
		default:
			break;
		}
		//TODO:BZ:DELME
		//var s='<' + new Date().getTime()+'<';for(var i=0;i<response.length;i++)s+=response[i]+',';console.log(s);
		return new Uint8Array(response).buffer;
	};
	
	this.gmcpReceive = function(buffer)
	{
		var s = '';
		for (var i=0; i <buffer.length; i++)
			s += String.fromCharCode( buffer[i]);
		var x=s.indexOf(' ');
		if(x<0)
			return s;
		var cmd = s.substring(0,x);
		var jsonStr = s.substring(x+1).trim();
		if(cmd.toLowerCase() == "siplet.input")
		{
			var obj=JSON.parse(jsonStr);
			window.sipletInputTitle = obj["title"];
			window.sipletInputText = obj["text"];
			var content = getOptionWindow("Siplet.Input",60,40);
			populateDivFromUrl(content, 'dialogs/editor.htm');
			window.SubmitSipletInputEntry = function()
			{
			    var textarea = content.getElementsByTagName('textarea')[0];
			    sendOneLine(textarea.value);
			    hideOptionWindow();
			    setTimeout(boxFocus,500);
			};
			var SipletInputEntryFocus = function()
			{
			    var textarea = content.getElementsByTagName('textarea')[0];
			    textarea.focus();
			};
			setTimeout(SipletInputEntryFocus,1000);
			return "";
		}
		return s;
	};
	
	this.msdpReceive = function(buffer)
	{
		var map = {};
		var stack = [];
		stack.push({});
		var str = null;
		var v = null;
		var valVar = null;
		var x = -1;
		var M = null;
		while (++x < dataSize)
		{
			switch (buffer[x])
			{
			case MSDPOP.MSDP_VAR: // start a string
				str = '';
				v = str;
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(str);
				else
					stack[stack.length-1][str] = '';
				break;
			case MSDPOP.MSDP_VAL:
				valVar = v;
				v = null;
				str = '';
				break;
			case MSDPOP.MSDP_TABLE_OPEN: // open a table
				M = {};
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(M);
				else
				if(varVal != null)
					stack[stack.length-1][valVar] = M;
				valVar = null;
				stack.push(M);
				break;
			case MSDPOP.MSDP_TABLE_CLOSE: // done with table
				if((stack.length > 1)&&(!Array.isArray(stack[stack.length-1])))
					stack.pop();
				break;
			case MSDPOP.MSDP_ARRAY_OPEN: // open an array
				M = [];
				if (Array.isArray(stack[stack.length-1]))
					stack[stack.length-1].push(M);
				else
				if (valVar != null)
					stack[stack.length-1][valVar]= M;
				valVar = null;
				stack.push(M);
				break;
			case MSDPOP.MSDP_ARRAY_CLOSE: // close an array
				if((stack.length > 1)&&(Array.isArray(stack[stack.length-1])))
					stack.pop();
				break;
			default:
				if (Array.isArray(stack[stack.length-1]))
				{
					if(!In(str, stack[stack.length-1]))
						stack[stack.length-1].push(str);
				}
				else
					stack[stack.length-1][valVar]= str;
				valVar = null;
				if (str != null)
					str += String.fromCharCode(buffer[x]);
				break;
			}
		}
		return JSON.stringify(stack[0]);
	}
};

var updateTelnetOptions = function() {
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		siplet.telnet.neverSupportMSP = !(getConfig("window/term/msp",'true') === 'true');
		siplet.telnet.neverSupportMXP = !(getConfig("window/term/mxp",'true') === 'true');
		siplet.telnet.neverSupportMSDP= !(getConfig("window/term/msdp",'true') === 'true');
		siplet.telnet.neverSupportGMCP = !(getConfig("window/term/gmcp",'true') === 'true');
    }
}
addConfigListener('window/term/msp', updateTelnetOptions);
addConfigListener('window/term/mxp', updateTelnetOptions);
addConfigListener('window/term/msdp', updateTelnetOptions);
addConfigListener('window/term/gmcp', updateTelnetOptions);
