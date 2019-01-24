var container = 1;
var containerAmount = 2;
var connection;

var sizeX = 1000;
var sizeY = 700;

const LOADING_PANEL = 1;
const MAIN_PANEL = 2;
const SERVER_PANEL = 3;
const USER_PANEL = 4;

var drawtimer;

var sleeptime = 0;

var pointsX;
var pointsY;

var pointsXStore;
var pointsYStore;

var selectedColor;
var colorStore;

var lobbyStore;

var strokeSize = 4;
var strokeStore;

var stateMode = false;

var offScreenCanvas;
var offScreenContext;

var canvas;
var context;

var status;
var statusstr = "";

var stroke;

var isDragging = false;
var useLineToMode = true;
var lineToModeIsFixed = false;

var pointFromX = 0;
var pointFromY = 0;

var mouseX = 0;
var mouseY = 0;

var acceptingNewPoints = true;

var menuopen = false;

var selectedRow = -1;

var countdowntimer;
var countdowntime;

var currentLobbyId = -1;

var capabilities = ["DrawIt", "FreeDraw"];

window.onload = function WindowLoad(event) {

    status = document.getElementById("status");

    offScreenCanvas = document.createElement('canvas');
    offScreenContext = offScreenCanvas.getContext("2d");

    canvas = document.getElementById("canvas");
    canvasPanel = document.getElementById("canvaspanel");
    chatPanel = document.getElementById("chatpanel");
    canvasPanel.style.width = sizeX;
    canvasPanel.style.height = sizeY;
    chatPanel.style.height = sizeY;
    context = canvas.getContext("2d");

    // var servertablewrapper = document.getElementById("servertable-wrapper");
    // servertablewrapper.style.height = sizeY;
    // var creategamewrapper = document.getElementById("creategame-wrapper");
    // creategamewrapper.style.height = sizeY;
    // var previewgamewrapper = document.getElementById("previewgame-wrapper");
    // previewgamewrapper.style.height = sizeY;
    stroke = document.getElementById("stroke");
    stroke.innerHTML = "" + strokeSize;

    lobbyStore = [];

    hideall();
    show(container);

    setUpButtonInputs();
    setUpInputs();
    setUpColor();

    connect();

}

function setUpButtonInputs() {
    $('#line').click(function(event) {
        document.getElementById("freedraw").classList.remove('buttonselected');
        document.getElementById("line").classList.add('buttonselected');
        //event.target.classList.add('buttonselected');
        useLineToMode = true;
        lineToModeIsFixed = false;
        return false;
    });
    $('#freedraw').click(function(event) {
        document.getElementById("line").classList.remove('buttonselected');
        document.getElementById("freedraw").classList.add('buttonselected');
        useLineToMode = false;
        lineToModeIsFixed = false;
        return false;
    });
    $("#undo").click(function() {
        if (currentLobbyId == -1) return false;
        if (!stateMode) return false;
        var lobbyIndex = getLobbyByRealId(currentLobbyId);
        if (lobbyIndex == -1) return false;
        var lobby = lobbyStore[lobbyIndex];
        if (lobby.gamemode != "FreeDraw") {
            undo();
            clearBoth();
        }
        var toSend = {
            "argument": "lobby",
            "type": "deleteline"
        }
        sendmessage(JSON.stringify(toSend));
        return false;
    });
    $("#reset").click(function() {
        if (currentLobbyId == -1) return false;
        if (!stateMode) return false;
        var lobbyIndex = getLobbyByRealId(currentLobbyId);
        if (lobbyIndex == -1) return false;
        var lobby = lobbyStore[lobbyIndex];
        if (lobby.gamemode != "FreeDraw") {
            reset();
            clearBoth();
        }
        var toSend = {
                "argument": "lobby",
                "type": "deleteall"
            }
            //redraw();
        sendmessage(JSON.stringify(toSend));
        return false;
    });
    $("#plus").click(function() {
        if (strokeSize < 10) {
            strokeSize += 1;
        }
        stroke.innerHTML = "" + strokeSize;
        redrawLater();
    });
    $("#minus").click(function() {
        if (strokeSize > 1) {
            strokeSize -= 1;
        }
        stroke.innerHTML = "" + strokeSize;
        clearBoth();
    });
    $("#menu").click(function() {
        //hide("chatpanel");
        showMenu();
    });
    // $("#back").click(function() {
    //     hideMenu();
    // });
    // $("#join").click(function() {
    //     //alert(selectedRow);
    //     if (selectedRow == -1) return;
    //     var lobbyIndex = getLobbyByRealId(selectedRow);
    //     //alert(lobbyIndex);

    //     if (lobbyIndex == -1) return;
    //     var lobby = lobbyStore[lobbyIndex];
    //     if (lobby.private) {
    //         //todo
    //         hide("servertable-wrapper");
    //         hide("buttondisplay");

    //         //Todo: populate preview game w/ gameinfo

    //         showname("previewgame-wrapper");
    //     }
    //     else {
    //         //Todo: nick
    //         var toSend = {
    //             "argument": "join",
    //             "nick": "todo",
    //             "id": lobby.id,
    //             "capabilities": capabilities
    //         }
    //         sendmessage(JSON.stringify(toSend));
    //     }
    // });
    // $("#leave").click(function() {
    //     sendmessage(JSON.stringify({
    //         "argument": "leave"
    //     }));
    // });
    // $("#create").click(function() {
    //     hide("servertable-wrapper");
    //     hide("buttondisplay");
    //     showname("creategame-wrapper");
    // });
    // $("#preview").click(function() {
    //     if (selectedRow == -1) return;
    //     var lobbyIndex = getLobbyByRealId(selectedRow);
    //     if (lobbyIndex == -1) return;

    //     var lobby = lobbyStore[lobbyIndex];
    //     //todo: from here make a seperate fct
    //     hide("servertable-wrapper");
    //     hide("buttondisplay");

    //     //Todo: populate preview game w/ gameinfo

    //     showname("previewgame-wrapper");
    // })

    // $("#create-cancel").click(function() {

    //     //Todo: clear data

    //     hide("creategame-wrapper");
    //     showname("servertable-wrapper");
    //     showname("buttondisplay");

    // });
    // $("#create-ok").click(function() {
    //     var toSend = {
    //             "argument": "create",
    //             "gamemode": "DrawIt",
    //             "nick": document.getElementById("create-nick").value,
    //             "name": document.getElementById("create-name").value
    //         }
    //         //alert(document.getElementById("create-check").value);
    //     if (document.getElementById("create-check").checked) {
    //         toSend.pass = document.getElementById("create-pass").value;
    //     }
    //     sendmessage(JSON.stringify(toSend));

    //     hide("creategame-wrapper");
    //     showname("servertable-wrapper");
    //     showname("buttondisplay");
    // });

    // $("#preview-cancel").click(function() {
    //     //todo: clear data
    //     hide("previewgame-wrapper");
    //     showname("servertable-wrapper");
    //     showname("buttondisplay");
    // });
    // $("#preview-ok").click(function() {
    //     var lobbyIndex = getLobbyByRealId(selectedRow);
    //     if (lobbyIndex == -1) {
    //         //todo: clear data
    //         hide("previewgame-wrapper");
    //         showname("servertable-wrapper");
    //         showname("buttondisplay");
    //         return;
    //     }
    //     var lobby = lobbyStore[lobbyIndex];
    //     var toSend = {
    //         "argument": "join",
    //         "nick": document.getElementById("preview-nick").value,
    //         "id": lobby.id,
    //         "capabilities": capabilities
    //     }
    //     if (lobby.private) {
    //         toSend.pass = document.getElementById("preview-pass").value;
    //     }
    //     sendmessage(JSON.stringify(toSend));
    //     //todo: clear data
    //     hide("previewgame-wrapper");
    //     showname("servertable-wrapper");
    //     showname("buttondisplay");

    // });
}

function setUpInputs() {
    var isDragging = false;
    $("#canvas")
        .mousedown(function(e) {
            isDragging = true;
            if (!stateMode || !useLineToMode) return;
            if (e.button == 2 && lineToModeIsFixed) {
                //Cancel
                lineToModeIsFixed = false;
                clear();
            }
            if (e.button == 0) {

                var parentOffset = $(this).parent().offset();

                var relX = (e.pageX - parentOffset.left) | 0;
                var relY = (e.pageY - parentOffset.top) | 0;

                if (lineToModeIsFixed) {
                    var X = [pointFromX, relX];
                    var Y = [pointFromY, relY];

                    pointsXStore.push(X);
                    pointsYStore.push(Y);
                    colorStore.push(selectedColor);
                    strokeStore.push(strokeSize);

                    sendLine(X, Y);

                    redrawLater();
                }
                else {
                    lineToModeIsFixed = true;
                }

                pointFromX = relX;
                pointFromY = relY;
                return false;
            }
        })
        .mouseup(function() {
            isDragging = false;
            console.log("MouseUp(outer)");
            if (stateMode && !useLineToMode) {
                console.log("MouseUp(inner)");
                pointsXStore.push(pointsX);
                pointsYStore.push(pointsY);
                colorStore.push(selectedColor);
                strokeStore.push(strokeSize);

                sendLine(pointsX, pointsY);

                pointsX = [];
                pointsY = [];

                redrawLater();
            }
        })
        .mousemove(function(e) {
            var parentOffset = $(this).parent().offset();

            var relX = (e.pageX - parentOffset.left) | 0;
            var relY = (e.pageY - parentOffset.top) | 0;
            if (isDragging && stateMode && !useLineToMode && acceptingNewPoints) {


                pointsX.push(relX);
                pointsY.push(relY);

                redrawLater();
                acceptingNewPoints = false;
                setTimeout(function() {
                    acceptingNewPoints = true;
                }, 10);

                sendPoint(relX | 0, relY | 0);
            }

            mouseX = relX;
            mouseY = relY;
            if (lineToModeIsFixed) {
                clear();
                if (lineToModeIsFixed) {
                    setTimeout(function() {
                        context.strokeStyle = "gray";
                        context.lineWidth = strokeSize;
                        context.beginPath();
                        context.moveTo(pointFromX, pointFromY);
                        context.lineTo(mouseX, mouseY);
                        context.stroke();
                    }, 1);
                }

                //redrawLater();
            }
        })
        .contextmenu(function() {
            return false;
        });
    document.getElementById('input').onkeypress = function(e) {
        if (!e) e = window.event;
        var keyCode = e.keyCode || e.which;
        if (keyCode == '13') {
            // Enter pressed

            var toSend = {
                "argument": "lobby",
                "type": "chat",
                "message": document.getElementById('input').value
            }

            connection.send(JSON.stringify(toSend));

            document.getElementById('input').value = "";
            redrawLater();
            //draw();
            return false;
        }
    }
    $(".buttons").mousedown(function(event) {
        event.target.classList.add('buttonpressed');
    });
    $(".buttons").mouseup(function(event) {
        event.target.classList.remove('buttonpressed');
    });
}

function setUpColor() {
    // var colorinput_wrapper = document.getElementById("colorinput-wrapper");
    // colorinput_wrapper.style.backgroundColor = "#000000";

    // var colorinput = document.getElementById('colorinput');

    // colorinput.value = "#000000";

    // colorinput.addEventListener("input", function() {
    //     //console.log("Color: " + colorinput.value);
    //     colorinput_wrapper.style.backgroundColor = colorinput.value;
    //     var color = hexToRgb(colorinput.value);

    //     selectedColor = [color.r, color.g, color.b, 255];
    // }, false);
}

function connect() {
    try {
        connection = new WebSocket('ws://localhost:8080');
        connection.onopen = function() {
            console.log("server open");

            var jump = readCookie("jump");
            if (jump != null) {
                var toSend = {
                    "argument": "jump",
                    "token": jump
                }
                eraseCookie("jump");
                connection.send(JSON.stringify(toSend));
            }
            else {
                var toSend = {
                    "argument": "capabilities",
                    "capabilities": capabilities
                }
                connection.send(JSON.stringify(toSend));
            }

            reset();
            //drawtimer = setInterval(draw, sleeptime);
            draw();
            show(MAIN_PANEL);
        };
        connection.onerror = function(e) {
            connection.close();
            createCookie("errorPage", "index.html", 0);
            window.location.replace("error.html");
        };
        connection.onmessage = function(e) {
            console.log("From server: " + e.data);

            canvas.width = sizeX;
            canvas.height = sizeY;

            offScreenCanvas.width = sizeX;
            offScreenCanvas.height = sizeY;

            var input = JSON.parse(e.data);

            onMessage(input);



        }
    }
    catch (e) {
        console.log(e);
    }
}

function onMessage(input) {
    var argument = input['argument'];
    //var canvas = document.getElementById("canvas");


    switch (argument) {
        case "chat":
            {
                if (input['result']) {
                    addChatMessage("Guess for: " + input['message'] + " was correct!");
                }
                else {
                    addChatMessage(input['message']);
                }
                redrawLater();
                break;
            }
        case "drawpoint":
            {
                addPoint(input['x'], input['y'], input['cR'], input['cG'], input['cB'], input['cA'], input['stroke']);
                break;
            }
        case "drawline":
            {
                addLine(input['lineX'], input['lineY'], input['cR'], input['cG'], input['cB'], input['cA'], input['stroke'], input['delete']);
                break;
            }
        case "drawlines":
            {
                for (var i = 0; i < input['lines'].length; i++) {
                    addLine(input['lines'][i]['lineX'], input['lines'][i]['lineY'], input['lines'][i]['cR'], input['lines'][i]['cG'], input['lines'][i]['cB'], input['lines'][i]['cA'], input['lines'][i]['stroke']);
                }
                break;
            }
        case "startguessing":
            {
                addChatMessage("You are GUESSING this round!<br> You have " + input["time"] + " seconds to guess." +
                    "<br>The word is " + input["wordlength"] + " chars long.<br>Good Luck!");
                setCountdown(input["time"]);
                stateMode = false;
                reset();
                break;
            }
        case "startdrawing":
            {
                addChatMessage("You are DRAWING this round!<br>You have " + input["time"] + " seconds to draw." +
                    "<br>Your word to draw is: '" + input["word"] + "'.<br>Good Luck!");
                setCountdown(input["time"]);
                stateMode = true;
                reset();
                break;
            }
        case "endguessing":
            {
                addChatMessage("ROUND OVER!<br>The word was: '" + input["word"] + "'.<br> There were " +
                    input["correctguesses"] + " correct guesses.<br>" + input["scoreboard"] + "Time until next round: " + input["time"] + " seconds");
                setCountdown(input["time"]);
                break;
            }
        case "countdown":
            {
                addChatMessage("Time until next round: " + input["time"] + " seconds");
                setCountdown(input["time"]);
                break;
            }
        case "deleteline":
            {
                undo();
                redrawLater();
                break;
            }
        case "deleteall":
            {
                reset();
                redrawLater();
                break;
            }
        case "lobbyopen":
            {
                hideMenu();
                addChatMessage(input["chatmessage"]);
                stateMode = input["gamemode"] == "FreeDraw";
                setStatus("Lobby id: " + input["id"] + " | " + input["gamemode"] + " | ");
                redrawLater();
                currentLobbyId = input["id"];
                break;
            }

        case "addlobby":
            {
                addLobby(getLobbyObject(input));
                redrawLater();
                break;
            }
        case "removelobby":
            {
                var lobby = getLobbyObject(input);
                var lobbyIndex = getLobbyById(lobby);
                if (lobbyIndex != -1) {
                    removeLobby(lobby, lobbyIndex);
                    redrawLater();
                }
                break;
            }
        case "addlobbys":
            {
                for (var i = 0; i < input['lobbys'].length; i++) {
                    addLobby(getLobbyObject(input['lobbys'][i]));
                }
                redrawLater();
                break;
            }
        case "leave":
            {
                reset();
                stateMode = false;
                clearInterval(countdowntimer);
                setStatus("Not in a lobby");
                addChatMessage(input['chatmessage']);
                redrawLater();
                currentLobbyId = -1;
                break;
            }
        case "jump":
            {
                createCookie("jump", input["token"], 0);
                window.location.replace(input["page"]);
                connection.close();
                break;
            }
    }

    // case "lobbyjoin":
    //     {
    //         // addChatMessage("Connected to lobby " + input["name"] + "<br>Lobby id: " + input["id"]);
    //         // setStatus("Lobby id: " + input["id"] + " | " + input["gamemode"] + " | ");
    //         // currentLobbyId = input["id"];
    //         break;
    //     }
}

function getLobbyObject(a) {
    //return [a['id'], a['name'], a['private'], a['players'], a['maxplayers'], a['gamemode'], a['status']]
    return {
        'id': a['id'],
        'name': a['name'],
        'private': a['private'],
        'players': a['players'],
        'maxplayers': a['maxplayers'],
        'gamemode': a['gamemode'],
        'status': a['status']
    };
}

function hide(a) {
    document.getElementById(a).style.display = 'none';
}

function clear() {
    context.clearRect(0, 0, sizeX, sizeY);
    context.drawImage(offScreenCanvas, 0, 0);
}

function clearBoth() {
    context.clearRect(0, 0, sizeX, sizeY);
    offScreenContext.clearRect(0, 0, sizeX, sizeY);
    redraw();
    context.drawImage(offScreenCanvas, 0, 0);
}

function hideall() {
    for (var i = 0; i < containerAmount; i++) {
        document.getElementById("container" + (i + 1)).style.display = 'none';
    }
}

function show(a) {
    document.getElementById("container" + container).style.display = 'none';
    document.getElementById("container" + a).style.display = 'block';
    container = a;
}

function showname(a) {
    document.getElementById(a).style.display = 'block';
}

function reset() {

    pointsX = [];
    pointsY = [];
    pointsXStore = [];
    pointsYStore = [];
    colorStore = [];
    strokeStore = [];
    selectedColor = [0, 0, 0, 255];
    canvas.width = canvas.width;
    redraw();
}

function undo() {
    pointsXStore.pop();
    pointsYStore.pop();
    colorStore.pop();
    strokeStore.pop();
    canvas.width = canvas.width;
    redraw();
}

function addChatMessage(message) {
    $("#chatdisplay").animate({
        scrollTop: $("#chatdisplay")[0].scrollHeight
    }, 100);
    document.getElementById("chatdisplay").innerHTML += "<br>" + message.replace(/\n/g, "<br>");
}

function addPoint(x, y, red, green, blue, alpha, stroke) {
    pointsX.push(x);
    pointsY.push(y);
    selectedColor = [red, green, blue, alpha];
    strokeSize = stroke;
    redraw();
}

function addLine(x, y, red, green, blue, alpha, stroke, del) {

    if (del) {
        pointsX = [];
        pointsY = [];
    }

    pointsXStore.push(x);
    pointsYStore.push(y);
    colorStore.push([red, green, blue, alpha]);
    strokeStore.push(stroke);
    redraw();
}

function redraw() {
    //var c = document.getElementById("canvas");
    //var ctx = c.getContext("2d");
    //ctx.fillStyle = "white";
    //ctx.fillRect(0, 0, sizeX, sizeY);

    offScreenContext.lineWidth = strokeSize;
    for (var i = 0; i < pointsXStore.length; i++) {
        offScreenContext.strokeStyle = "rgb(" + colorStore[i][0] + ", " + colorStore[i][1] + ", " + colorStore[i][2] + ")";
        offScreenContext.lineWidth = strokeStore[i];
        offScreenContext.beginPath();
        offScreenContext.moveTo(pointsXStore[i][0], pointsYStore[i][0]);
        for (var x = 1; x < pointsXStore[i].length; x++) {
            offScreenContext.lineTo(pointsXStore[i][x], pointsYStore[i][x]);
        }
        offScreenContext.stroke();
    }
    if (pointsX.length > 0) {
        offScreenContext.lineWidth = strokeSize;
        offScreenContext.strokeStyle = "rgb(" + selectedColor[0] + ", " + selectedColor[1] + ", " + selectedColor[2] + ")";
        offScreenContext.moveTo(pointsX[0], pointsY[0]);
        for (var i = 1; i < pointsX.length; i++) {
            offScreenContext.lineTo(pointsX[i], pointsY[i]);
        }
        offScreenContext.stroke();
    }



}

function draw() {
    context.drawImage(offScreenCanvas, 0, 0);
    requestAnimationFrame(draw);
}

function setStatus(a) {
    statusstr = a;
    //status.innerHTML = a;
    document.getElementById("status").innerText = a;
}

function setCountdown(time) {
    clearInterval(countdowntimer);
    countdowntime = time;

    countdowntimer = setInterval(function() {
        countdowntime -= 1;
        document.getElementById("status").innerText = statusstr + " Time remaining: " + countdowntime;
        if (countdowntime <= 0) {
            clearInterval(countdowntimer);
        }
    }, 1000)
}

function addLobby(lobby) {
    var targetIndex = getLobbyById(lobby);

    if (targetIndex != -1) {
        //Update the target lobby by removing it
        var target = lobbyStore[targetIndex];
        removeLobby(target, targetIndex);
    }
    lobbyStore.push(lobby);

    // var table = document.getElementById("servertable");

    // var tableRow = document.createElement("tr");
    // tableRow.setAttribute("id", "row" + lobby.id);
    // tableRow.addEventListener('click', function() {

    //     if (selectedRow != -1) {
    //         try {
    //             document.getElementById("row" + selectedRow).style = "background-color:white";
    //         }
    //         catch (e) {

    //         }
    //     }
    //     tableRow.style = "background-color:lightgray";
    //     selectedRow = tableRow.id.substring(3);
    // });
    // tableRow.innerHTML = "<td class = 'tdid'>" + lobby.id + "</td><td class = 'tdname'>" + lobby.name + "</td><td class = 'tdplayers'>" + lobby.players + "/" + lobby.maxplayers +
    //     "</td><td class = 'tdprivate'>" + lobby.private + "</td><td class = 'tdgamemode'>" + lobby.gamemode + "</td>";
    // table.appendChild(tableRow);
}

function removeLobby(lobby, lobbyIndex) {
    lobbyStore.splice(lobbyIndex, 1);
    // var element = document.getElementById("row" + lobby.id);
    // element.parentNode.removeChild(element);
}

function getLobbyById(lobby) {
    for (var i = 0; i < lobbyStore.length; i++) {
        if (lobbyStore[i].id == lobby.id) {
            return i;
        }
    }
    return -1;
}

function getLobbyByRealId(id) {
    for (var i = 0; i < lobbyStore.length; i++) {
        if (lobbyStore[i].id == id) {
            return i;
        }
    }
    return -1;
}

function sendmessage(message) {
    connection.send(message);
}

function sendPoint(x, y) {
    var toSend = {
        "argument": "lobby",
        "type": "drawpoint",
        "x": x,
        "y": y
    }
    sendmessage(JSON.stringify(toSend));
}

function sendLine(x, y) {
    var toSend = {
        "argument": "lobby",
        "type": "drawline",
        "lineX": x,
        "lineY": y,
        "stroke": strokeSize,
        "cR": selectedColor[0],
        "cG": selectedColor[1],
        "cB": selectedColor[2],
        "cA": selectedColor[3],
        "apoints": x.length
    }
    sendmessage(JSON.stringify(toSend));
}

function redrawLater() {
    setTimeout(function() {
        redraw();
    }, 1);
}

function showMenu() {
    hide("canvaspanel");
    hide("buttonpanel");
    showname("serverbuttonpanel");
    showname("container3");
    menuopen = true;
}

function hideMenu() {
    showname("chatpanel");
    showname("canvaspanel");
    showname("buttonpanel");
    hide("container3");
    hide("serverbuttonpanel");
    menuopen = false;
}

function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
}