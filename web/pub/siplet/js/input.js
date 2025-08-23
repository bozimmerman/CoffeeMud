var inputAreaHeight = 70;
var inputSpanArea = null;
var inputTextArea = null;
var inputbacklog = [];
var inputbacklogindex = -1;
var inputcursor = null;

var AsciiKeyMap = 
{
	Tab: 9,
	Enter: 13,
	Escape: 27,
	ArrowUp: "\x1B[A",
	ArrowDown: "\x1B[B",
	ArrowLeft: "\x1B[D",
	ArrowRight: "\x1B[C",
	Home: "\x1B[H",
	End: "\x1B[F",
	Insert: "\x1B[2~"
};

function ConfigureInput(obj, ta)
{
	obj.style.position='fixed';
	obj.style.bottom=0;
	obj.style.width='100%';
	obj.style.height=inputAreaHeight+'px';
	obj.style.background='$f0f0f0';
	inputSpanArea = obj;
	inputTextArea = ta;
	inputTextArea.style.width='calc(100vw - 10px)';
	inputTextArea.style.margin='5px';
	inputTextArea.style.padding='5px';
	inputTextArea.onkeypress=inputKeyPress;
	inputTextArea.onkeydown=inputKeyDown;
	inputTextArea.style.resize='none';
}

function setInputVisibility(yn)
{
	if(yn)
	{
		inputSpanArea.style.visibility = 'visible';
		window.windowArea.style.height=window.winAreaHeight;
		TurnOffBoxlessInputCursor();
	}
	else
	{
		inputSpanArea.style.visibility = 'hidden';
		var winAreaHeight = 'calc(100% - '+(window.winAreaTop)+'px)';
		window.windowArea.style.height=winAreaHeight;
		TurnOnBoxlessInputCursor();
	}
}

function isInputVisible()
{
	return inputSpanArea.style.visibility == 'visible';
}

function submitBacklog(x)
{
	var max = getConfig('window/input_buffer',500);
	while(inputbacklog.length>max)
		inputbacklog.splice(0,1);
	if(x && x !== 'undefined')
	{
		if(inputbacklog[inputbacklog.length-1] !== x)
			inputbacklog.push(x);
		inputbacklogindex=inputbacklog.length;
	}
}

function inputSubmit(x)
{
	submitBacklog(x);
	if(x && window.currWin != null)
	{
		x = window.currWin.aliasProcess(x);
		if(!x)
			return;
	}
	if(window.currWin)
	{
		window.currWin.scrollToBottom(window.currWin.window,0);
		window.currWin.submitInput(x);
	}
	else
		window.alert("You need to connect first!");
}

function inputKeyPress(e)
{
	if(!isInputVisible())
		return;
	if(e.keyCode == 13)
	{
		e.preventDefault();
		if(window.currWin.wsopened)
		{
			inputSubmit(inputTextArea.value);
			inputTextArea.value='';
		}
		else
		SiConfirm('Reconnect?',function(tf) {
			if(tf)
			{
				window.currWin.closeSocket();
				window.currWin.reset();
				window.currWin.connect(window.currWin.url);
				setInputBoxFocus();
			}
		});
	}
}

function sendOneLine(txt)
{
	if(txt != null)
	{
		txt = txt.replaceAll('\n','%0D').replaceAll('\r','');
		window.currWin.submitInput(txt);
	}
}

function inputKeyDown(e)
{
	if(!isInputVisible())
		return;
	var x = e.keyCode;
	if(x == 38) // up
	{
		if(inputbacklogindex>0)
		{
			if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
			{
				if (inputbacklog[inputbacklogindex] === undefined)
					inputTextArea.value='';
				else
					inputTextArea.value=inputbacklog[inputbacklogindex];
			}
			inputbacklogindex--;
			if((inputTextArea.value=='')
			||((inputbacklogindex<inputbacklog.length-1)
				&&inputTextArea.value==inputbacklog[inputbacklogindex+1]))
			{
				if (inputbacklog[inputbacklogindex] === undefined)
					inputTextArea.value='';
				else
					inputTextArea.value=inputbacklog[inputbacklogindex];
			}
		}
	}
	else
	if(x == 40) // down
	{
		if(inputbacklogindex<inputbacklog.length-1)
		{
			if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
			{
				if (inputbacklog[inputbacklogindex] === undefined)
					inputTextArea.value='';
				else
					inputTextArea.value=inputbacklog[inputbacklogindex];
			}
			inputbacklogindex++;
			if((inputTextArea.value=='')
			||((inputbacklogindex>0)
				&&inputTextArea.value==inputbacklog[inputbacklogindex-1]))
			{
				if (inputbacklog[inputbacklogindex] === undefined)
					inputTextArea.value='';
				else
					inputTextArea.value=inputbacklog[inputbacklogindex];
			}
		}
	}
	else
	if((x == 13) && (e.shiftKey))
	{
		inputTextArea.value += '\n';
		e.preventDefault();
	}
	else
	if(x == 10)
		e.preventDefault();
}

function addToPrompt(x,att)
{
	inputTextArea.value='';
	if(att)
		inputSubmit(x);
	else
	if(!x)
		return;
	else
	if(!isInputVisible())
	{
		if(x) x += ' ';
		for(var i=0;i<x.length;i++)
		{
			var fakeE = {
				ctrlKey: false,
				altKey: false,
				key: x[i]
			};
			document.onkeydown(fakeE);
		}
	}
	else
		inputTextArea.value=x+" ";
}

function setInputBoxFocus() 
{
	setTimeout(function() {
		if(!isInputVisible())
		{
			DisplayFakeInput(null);
			return;
		}
		inputTextArea.focus();
	}, 100);
}

function DisplayFakeInput(key)
{
	if((!window.currWin) || (!window.currWin.window))
		return;
	TurnOffBoxlessInputCursor();
	if(isInputVisible())
		return;
	var echo = window.currWin.telnet.localEcho;
	if((key != null) && echo)
	{
		if(key == 'Backspace')
		{
			var span = document.getElementById("CURRENTINPUT"+window.currWin.windowName);
			if((span !== null)&&(span.innerHTML.length>0))
				span.innerHTML = span.innerHTML.substr(0, span.innerHTML.length-1);
		}
		else
		{
			var span = document.getElementById("CURRENTINPUT"+window.currWin.windowName);
			var span2 = window.currWin.displayBit(key);
			if(span == null)
			{
				span = span2;
				span.id = 'CURRENTINPUT'+window.currWin.windowName;
				span.style.color = "white";
				span.style.backgroundColor = 'black';
			}
			else
			{
				if(window.currWin.lastChild !== span)
				{
					window.currWin.window.removeChild(span);
					window.currWin.window.appendChild(span);
				}
				window.currWin.window.removeChild(span2);
				span.innerHTML += key;
			}
		}
	}
	TurnOnBoxlessInputCursor();
}

function TurnOffBoxlessInputCursor()
{
	if(inputcursor != null)
	{
		if(inputcursor.parentNode != null)
			inputcursor.parentNode.removeChild(inputcursor);
	}
}

function TurnOnBoxlessInputCursor()
{
	if(inputcursor == null)
	{
		const cursor = document.createElement("span");
		cursor.id = "INPUTCURSOR";
		cursor.textContent = "|";
		cursor.style.display = "inline";
		cursor.style.fontFamily = "monospace";
		cursor.style.fontSize = "16px";
		cursor.style.color = "white";
		cursor.style.visibility = 'visible';
		inputcursor = cursor;
		setInterval(() => {
			var isCursorVisible = cursor.style.visibility == 'visible';
			cursor.style.visibility = isCursorVisible ? "hidden" : "visible";
		}, 500);
	} 
	if(window.currWin && window.currWin.window && (inputcursor.parentNode === null))
		window.currWin.window.appendChild(inputcursor);
}

document.onkeydown = function(e) {
	var win = window.currWin;
	if(win == null)
		return;
	if(e.altKey)
	{
		if(e.key == 'o')
		{
			if(window.isElectron && win)
			{
				if(win.logStream != null)
					win.closeLog();
				else
				{
					var captureFilename;
					if(win.pb && win.pb.capture)
						captureFilename = win.pb.capture;
					else
					{
						var os = require('os');
						var osPath = require('path');
						captureFilename = osPath.join(os.tmpdir(),Siplet.NAME.toLowerCase()+'.log');
					}
					win.openLog(captureFilename);
				}
			}
		}
		/* wont work because !inputvisible means immediate char sends.
		else
		if(!isInputVisible() && !isDialogOpen())
		{
			var x = e.keyCode;
			var span = document.getElementById("CURRENTINPUT"+win.windowName);
			if(span === null)
			{
				span = win.displayBit(' ');
				span.innerHTML = '';
				span.id = 'CURRENTINPUT'+win.windowName;
				span.style.color = "white";
				span.style.backgroundColor = 'black';
				span.style.visibility = 'visible';
			}
			if(x == 38) // up
			{
				if(inputbacklogindex>0)
				{
					if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
						span.innerHTML=inputbacklog[inputbacklogindex];
					inputbacklogindex--;
					if((inputTextArea.value=='')
					||((inputbacklogindex<inputbacklog.length-1)
						&&span.innerHTML==inputbacklog[inputbacklogindex+1]))
						span.innerHTML=inputbacklog[inputbacklogindex];
				}
			}
			else
			if(x == 40) // down
			{
				if(inputbacklogindex<inputbacklog.length-1)
				{
					if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
						span.innerHTML=inputbacklog[inputbacklogindex];
					inputbacklogindex++;
					if((inputTextArea.value=='')
					||((inputbacklogindex>0)
						&&span.innerHTML==inputbacklog[inputbacklogindex-1]))
						span.innerHTML=inputbacklog[inputbacklogindex];
				}
			}
		}
		*/
	}
	else
	if(!isInputVisible() && !isDialogOpen())
	{
		if(e.key === 'Backspace')
		{
			var bs = 8;
			if((win.pb)&&(win.pb.bsCode))
				bs = win.pb.bsCode;
			DisplayFakeInput(e.key);
			win.sendRaw([bs]);
		}
		else
		if(e.key.length == 1)
		{
			if(e.ctrlKey)
			{
				var ctrlCode = e.key.toLowerCase().charCodeAt(0)-96;
				if((ctrlCode>0)&&(ctrlCode<27))
				{
					if(ctrlCode == 7)
					{
						var audio = new Audio('images/ding.wav');
						audio.play();
					}
					win.sendRaw([ctrlCode]);
				}
			}
			else
			{
				DisplayFakeInput(e.key);
				win.sendRaw([e.key.charCodeAt(0)]);
			}
		}
		else
		if(AsciiKeyMap[e.key])
		{
			var keyCode = AsciiKeyMap[e.key];
			if(typeof keyCode === 'string')
			{
				win.sendStr(keyCode);
				e.preventDefault();
			}
			else
			if(keyCode == 13)
			{
				win.sendRaw([keyCode]);
				DisplayFakeInput('\n');
				win.scrollToBottom(win.window,0);
				var span = document.getElementById("CURRENTINPUT"+win.windowName);
				if(span != null)
				{
					span.id = "";
					delete span.id; 
					if(span.innerHTML)
					{
						submitBacklog(span.innerHTML);
						win.aliasProcess(span.innerHTML);
					}
				}
			}
			else
			if(keyCode == 9)
			{
				win.sendRaw([keyCode]);
				DisplayFakeInput('\t');
			}
			//TODO bell? 7?
		}
	}
};
