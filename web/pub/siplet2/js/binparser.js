var BPTYPE =
{
	TEXT: 0,
	ANSI: 1,
	TELNET: 2
};

var BPSTATE = 
{
	OUTER: 0,
	ANSI: 1,
	ANSI2: 2,
	ANSI3: 3,
	TELNET: 4,
	TELNET2: 5,
	TELNETSB: 6,
};

var BPCODE =
{
	ESC: 27,
	IAC: 0xFF,
	TELNET_SE: 0xF0,
	TELNET_SB: 0xFA,
	TELNET_WILL: 0xFB,
	TELNET_WONT: 0xFC,
	TELNET_DO: 0xFD,
	TELNET_DONT: 0xFE,
	TELNET_GA: 0xF9,
	TELNET_NOP: 0xF1
};

var BPENTRY = function()
{
	this.type = BPTYPE.TEXT;
	this.data = [];
	this.done = false;
};

var BPPARSE = function(lfok)
{
	this.entries = [];
	this.data = [];
	this.prev = 0;
	this.nolf = !lfok;
	this.state = BPSTATE.OUTER;
	this.parse = function()
	{
		var entries = this.entries;
		if (entries.length == 0)
			entries.push(new BPENTRY());
		var curr = entries[entries.length-1];
		var i=-1;
		var c=this.prev;
		// should this be Uint16Array, since I'm encoding unicode?
		var data = new Uint8Array(this.data);
		while(++i < data.length)
		{
			var prev = c;
			c = data[i];
			switch(this.state)
			{
			case BPSTATE.OUTER:
				switch(c)
				{
				case BPCODE.ESC:
					this.state = BPSTATE.ANSI;
					break;
				case BPCODE.IAC:
					this.state = BPSTATE.TELNET;
					break;
				case 13:
					curr.data.push(c);
					curr.done = true;
					curr = new BPENTRY();
					this.entries.push(curr);
					break;
				case 10:
					if (!this.nolf)
						curr.data.push(c);
					break;
				default:
					if((c & 0xFF00) > 0)
					{
						curr.data.push((c & 0xFF00)>>8);
						curr.data.push(c & 0xFF);
					}
					else
						curr.data.push(c);
					break;
				}
				break;
			case BPSTATE.ANSI:
				if(c==91) // [
				{
					this.state = BPSTATE.ANSI2;
					curr.done = true;
					curr = new BPENTRY();
					this.entries.push(curr);
					curr.type = BPTYPE.ANSI;
				}
				else
				{
					this.state = BPSTATE.OUTER;
					curr.data.push(27);
					curr.data.push(c);
				}
				break;
			case BPSTATE.ANSI2:
				curr.data.push(c);
				if(c==34) // "
					this.state = BPSTATE.ANSI3;
				else
				if((c==109) || (c==122)) //m, z
				{
					this.state = BPSTATE.OUTER;
					curr.done=true;
					curr = new BPENTRY();
					this.entries.push(curr);
				}
				break;
			case BPSTATE.ANSI3:
				curr.data.push(c);
				if(c==34) // "
					this.state = BPSTATE.ANSI2;
				else
				if((curr.data.length>128) || (!isLetter(c)))
				{
					//something went very very wrong.  kill with fire.
					this.state = BPSTATE.OUTER;
					curr.type=BPTYPE.TEXT;
					curr.data=[];
					i--;
				}
				break;
			case BPSTATE.TELNET:
				switch(c)
				{
				case BPCODE.IAC: // double escape?  Just stay here..
					break;
				case BPCODE.TELNET_SB:
					curr.done = true;
					curr = new BPENTRY();
					this.entries.push(curr);
					curr.type = BPTYPE.TELNET;
					curr.data.push(c);
					this.state = BPSTATE.TELNETSB;
					break;
				case BPCODE.TELNET_WILL:
				case BPCODE.TELNET_WONT:
				case BPCODE.TELNET_DO:
				case BPCODE.TELNET_DONT:
					curr.done = true;
					curr = new BPENTRY();
					this.entries.push(curr);
					curr.type = BPTYPE.TELNET;
					curr.data.push(c);
					this.state = BPSTATE.TELNET2;
					break;
				case BPCODE.TELNET_GA:
				case BPCODE.TELNET_NOP:
					// just eat it
					this.state = BPSTATE.OUTER;
					break;
				default:
					curr.data.push(BPCODE.IAC);
					curr.data.push(c);
					this.state = BPSTATE.OUTER;
					break;
				}
				break;
			case BPSTATE.TELNET2:
				curr.data.push(c);
				curr.done = true;
				curr = new BPENTRY();
				this.entries.push(curr);
				this.state = BPSTATE.OUTER;
				break;
			case BPSTATE.TELNETSB:
				if(prev == BPCODE.IAC)
				{
					if(c == BPCODE.IAC)
						curr.data.push(c);
					else
					if(c == BPCODE.TELNET_SE)
					{
						curr.done = true;
						curr = new BPENTRY();
						this.entries.push(curr);
						this.state = BPSTATE.OUTER;
					}
				}
				else
					curr.data.push(c);
				break;
			}
		}
		this.prev = c;
		if((curr.type == BPTYPE.TEXT) && (curr.data.length > 0))
			curr.done = true;
		return '';
	};
};
