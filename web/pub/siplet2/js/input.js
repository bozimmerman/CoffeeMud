var inputAreaHeight = 70;
var inputTextArea = null;
var inputbacklog = [];
var inputbacklogindex = -1;

function configureInput(obj, ta)
{
    obj.style.position='fixed';
    obj.style.bottom=0;
    obj.style.width='100%';
    obj.style.height=inputAreaHeight+'px';
    obj.style.background='$f0f0f0';
    inputTextArea = ta;
    inputTextArea.style.width='calc(100vw - 10px)';
    inputTextArea.style.margin='5px';
    inputTextArea.style.padding='5px';
    inputTextArea.onkeypress=inputKeyPress;
    inputTextArea.onkeydown=inputKeyDown;
}

function inputSubmit(x)
{
    var max = getConfig('window/input_buffer',500);
    while(inputbacklog.length>max)
        inputbacklog.splice(0,1);
    if(x.length>0)
    {
        inputbacklog.push(x);
        inputbacklogindex=inputbacklog.length;
    }
    window.currentSiplet.submitInput(x);
}

function inputKeyPress(e)
{
    if(e.keyCode == 13)
    {
        e.preventDefault();
        inputSubmit(inputTextArea.value);
        inputTextArea.value='';
    }
}

function inputKeyDown(e)
{
    var x = e.keyCode;
    if(x == 38) // up
    {
        if(inputbacklogindex>0)
        {
            if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
                inputTextArea.value=inputbacklog[inputbacklogindex];
            inputbacklogindex--;
            if((inputTextArea.value=='')
            ||((inputbacklogindex<inputbacklog.length-1)
                &&inputTextArea.value==inputbacklog[inputbacklogindex+1]))
                inputTextArea.value=inputbacklog[inputbacklogindex];
        }
    }
    else
    if(x == 40) // down
    {
        if(inputbacklogindex<inputbacklog.length-1)
        {
            if((e.shiftKey)&&(inputbacklogindex>=0)&&(inputbacklogindex<inputbacklog.length))
                inputTextArea.value=inputbacklog[inputbacklogindex];
            inputbacklogindex++;
            if((inputTextArea.value=='')
            ||((inputbacklogindex>0)
                &&inputTextArea.value==inputbacklog[inputbacklogindex-1]))
                inputTextArea.value=inputbacklog[inputbacklogindex];
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
        inputTextArea.value=x+" ";
}

function boxFocus() 
{
    setTimeout(function() {
        inputTextArea.focus();
    }, 100);
}
