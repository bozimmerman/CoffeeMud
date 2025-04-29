
var MSP = function(flags, playerName)
{
	this.sounders=[];
	this.soundDates=[];
	this.soundPriorities=[];
	
	this.PlaySound = function(key,url,repeats,volume,priority)
	{
		var theSoundPlayer = this.sounders[playerName];  
		if(theSoundPlayer)
		{
			var now=new Date();
			var ellapsed=Math.abs(now.getTime() - this.soundDates[playerName].getTime());
			if((ellapsed < 1500) && (priority <= this.soundPriorities[playerName]))
				return;
			if(theSoundPlayer.stop)
				theSoundPlayer.stop();
			document.childNodes[0].removeChild(theSoundPlayer);
		}
		theSoundPlayer=document.createElement('audio');
		if(!theSoundPlayer || !theSoundPlayer.play) 
			theSoundPlayer=document.createElement('embed');
		if(!theSoundPlayer) 
			return;
		this.sounders[playerName]=theSoundPlayer;
		this.soundDates[playerName]=new Date();
		this.soundPriorities[playerName]=priority;
		document.childNodes[0].appendChild(theSoundPlayer);
		theSoundPlayer.setAttribute('src', url+key);
		//TODO: use the actual extension in the mime type
		theSoundPlayer.setAttribute('type','audio/wav');
		theSoundPlayer.setAttribute('hidden','true');
		theSoundPlayer.setAttribute('volume', volume);
		if(theSoundPlayer.play)
			theSoundPlayer.play();
	}
	
	this.StopSound - function(key)
	{
		var theSoundPlayer=document.getElementById(playerName);
		theSoundPlayer.src='';
		theSoundPlayer.Play();
		theSoundPlayer.innerHTML='';
	};
	
	this.process = function(str)
	{
		if ((flags == null) || (!flags.MSPsupport))
			return str;
		var x = str.indexOf('!!SOUND(');
		if(x >= 0)
			return this.processSoundEmbed(str, x);
		x = str.indexOf('!!MUSIC(');
		if(x >= 0)
			return this.processMusicEmbed(str, x);
		return str;
	};

	this.processParms = function(tag, parms)
	{
		var parmLetters = "V#L#P#C#T$U$";
		var last = -1;
		var state=2;
		var tagLetter = 'N';
		var value='';
		var num = false;
		var z;
		for(var i=0;i<tag.length;i++)
		{
			var c = tag[i];
			switch(state)
			{
			case 0:
				z=parmLetters.indexOf(c.toUpperCase()); 
				if((z>=0)&&(c!='#')&&(c!='$'))
				{
					tagLetter = c.toUpperCase();
					num = parmLetters[z+1] == '#';
					state=1;
				}
				break;
			case 1:
				if(c=='=')
				{
					state=2;
					value='';
				}
				else
				if(c!=' ')
				{
					state=0;
					i=i-1;
				}
				break;
			case 2:
				if(c!=' ')
					value += c;
				else
				{
					// done
					state=0;
					parms[tagLetter] = num ? Number(value) : value;
					value='';
					tagLetter = ' ';
				}
				break;
			}
		}
		if(state == 2)
			parms[tagLetter] = num ? Number(value) : value;
	};
	
	this.processSoundEmbed = function(str, x)
	{
		var y = str.indexOf(')', x+8);
		if(y<0)
			return str;
		//!!SOUND(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)
		var parms = {
			N: "",
			V: 50,
			L: 0,
			P: 50,
			C: 0,
			T: "",
			U: ""
		};
		var tag = str.substr(0,y).substr(x+8);
		str = str.substr(0,x) + str.substr(y+1);
		this.processParms(tag, parms);
		if((parms.U.length == 0)||(parms.N.length==0))
			return str; // no internal sound files, so....
		if(parms.N.toLowerCase() == 'off')
			this.StopSound('');
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P);
		return str;
	};
	
	this.processMusicEmbed = function(str, x)
	{
		var y = str.indexOf(')', x+8);
		if(y<0)
			return str;
		//!!MUSIC(&fname; V=&v; L=&l; P=&p; T=&t; U=&u;)
		var parms = {
			N: "",
			V: 50,
			L: 0,
			P: 50,
			C: 1,
			T: "",
			U: ""
		};
		var tag = str.substr(0,y).substr(x+8);
		str = str.substr(0,x) + str.substr(y+1);
		this.processParms(tag, parms);
		if((parms.U.length == 0)||(parms.N.length==0))
			return str; // no internal sound files, so....
		if(parms.N.toLowerCase() == 'off')
			this.StopSound('');
		else
			this.PlaySound(parms.N,parms.U,parms.L,parms.V,parms.P)
		return str;
	};
}

