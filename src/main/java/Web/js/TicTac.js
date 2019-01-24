var container = 1;
var containerAmount = 3;
var connection;

const LOADING_PANEL = 1;
const MAIN_PANEL = 2;
const SERVER_PANEL = 3;
const USER_PANEL = 4;

var lobbyStore;

var status;
var statusstr = "";

var menuopen = false;

var currentLobbyId = -1;

var selectedRow = -1;

var selecting;
var displayChar;

var capabilities = ["TicTacToe"];

var path = "TicTac.html";

var sizeX = 600;

window.onload = function WindowLoad(event) {
    status = document.getElementById("status");

    hideall();
    show(LOADING_PANEL);

    var chatpanel = document.getElementById("chatpanel");
    var tictac = document.getElementById("tictac");

    chatpanel.style.height = sizeX;
    tictac.style.height = sizeX;

    lobbyStore = [];

    setUpButtonInputs();
    setUpInputs();
    connect();
}

function setUpButtonInputs() {
    $("#menu").click(function() {
        //hide("chatpanel");
        showMenu();
    });
    $("#back").click(function() {
        hideMenu();
    });
    $("#invite").click(function() {
        copyTextToClipboard(path);
        addChatMessage("Copied invite url to clipboard.");
    });
    $("#join").click(function() {
        //alert(selectedRow);
        if (selectedRow == -1) return;
        var lobbyIndex = getLobbyByRealId(selectedRow);
        //alert(lobbyIndex);

        if (lobbyIndex == -1) return;
        var lobby = lobbyStore[lobbyIndex];
        if (lobby.private) {
            //todo
            hide("servertable-wrapper");
            hide("buttondisplay");

            //Todo: populate preview game w/ gameinfo

            showname("previewgame-wrapper");
        } else {
            //Todo: nick
            var toSend = {
                "argument": "join",
                "nick": "todo",
                "id": lobby.id,
                "capabilities": capabilities
            }
            sendmessage(JSON.stringify(toSend));
        }
    });
    $("#leave").click(function() {
        sendmessage(JSON.stringify({
            "argument": "leave"
        }));
    });
    $("#create").click(function() {
        hide("servertable-wrapper");
        hide("buttondisplay");
        showname("creategame-wrapper");
    });
    $("#preview").click(function() {
        if (selectedRow == -1) return;
        var lobbyIndex = getLobbyByRealId(selectedRow);
        if (lobbyIndex == -1) return;

        var lobby = lobbyStore[lobbyIndex];
        //todo: from here make a seperate fct
        hide("servertable-wrapper");
        hide("buttondisplay");

        //Todo: populate preview game w/ gameinfo

        showname("previewgame-wrapper");
    })

    $("#create-cancel").click(function() {

        //Todo: clear data

        hide("creategame-wrapper");
        showname("servertable-wrapper");
        showname("buttondisplay");

    });
    $("#create-ok").click(function() {
        var toSend = {
                "argument": "create",
                "gamemode": "TicTacToe",
                "nick": document.getElementById("create-nick").value,
                "name": document.getElementById("create-name").value
            }
            //alert(document.getElementById("create-check").value);
        if (document.getElementById("create-check").checked) {
            toSend.pass = document.getElementById("create-pass").value;
        }
        sendmessage(JSON.stringify(toSend));

        hide("creategame-wrapper");
        showname("servertable-wrapper");
        showname("buttondisplay");
    });

    $("#preview-cancel").click(function() {
        //todo: clear data
        hide("previewgame-wrapper");
        showname("servertable-wrapper");
        showname("buttondisplay");
    });
    $("#preview-ok").click(function() {
        var lobbyIndex = getLobbyByRealId(selectedRow);
        if (lobbyIndex == -1) {
            //todo: clear data
            hide("previewgame-wrapper");
            showname("servertable-wrapper");
            showname("buttondisplay");
            return;
        }
        var lobby = lobbyStore[lobbyIndex];
        var toSend = {
            "argument": "join",
            "nick": document.getElementById("preview-nick").value,
            "id": lobby.id,
            "capabilities": capabilities
        }
        if (lobby.private) {
            toSend.pass = document.getElementById("preview-pass").value;
        }
        sendmessage(JSON.stringify(toSend));
        //todo: clear data
        hide("previewgame-wrapper");
        showname("servertable-wrapper");
        showname("buttondisplay");

    });
}

function setUpInputs() {
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
            //draw();
            return false;
        }
    }
    $("#grid").click(function(event) {
        var target = $(event.target);
        $td = target.closest('td');

        //$td.html(parseInt($td.html()) + 1);
        var col = $td.index();
        var row = $td.closest('tr').index();
        console.log("click: " + col + "," + row);
        if (selecting) {
            var toSend = {
                "argument": "lobby",
                "type": "selection",
                "x": row,
                "y": col
            }
            connection.send(JSON.stringify(toSend));
        }
    });
}

function connect() {
    try {
        var host = 'ws://138.197.170.7:8080';
        connection = new WebSocket(host);
        //console.log("?????????");
        connection.onopen = function(e) {
            console.log("server open");

            var id = getParam("id");
            if (id != null) {
                var token = getParam("token");
                var toSend = {
                    "argument": "jointoken",
                    "id": id,
                    "capabilities": capabilities,
                    "token": token
                }
                connection.send(JSON.stringify(toSend));
            } else {
                var jump = readCookie("jump");
                if (jump != null) {
                    var toSend = {
                        "argument": "jump",
                        "token": jump
                    }
                    eraseCookie("jump");
                    connection.send(JSON.stringify(toSend));
                } else {
                    var toSend = {
                        "argument": "capabilities",
                        "capabilities": capabilities
                    }
                    connection.send(JSON.stringify(toSend));
                }
            }


            reset();
            show(MAIN_PANEL);
        };
        connection.onerror = function(e) {
            connection.close();
            createCookie("errorPage", "TicTac.html", 0);
            window.location.replace("error.html");
        };
        connection.onmessage = function(e) {
            console.log("From server: " + e.data);

            var input = JSON.parse(e.data);
            onMessage(input);

        }
    } catch (e) {
        console.log("error");
        console.log(e);
    }
}

function onMessage(input) {
    var argument = input['argument'];
    //var canvas = document.getElementById("canvas");


    switch (argument) {
        case "chat":
            {
                addChatMessage(input['message']);
                break;
            }
        case "countdown":
            {
                addChatMessage("Time until next round: " + input["time"] + " seconds");
                //setCountdown(input["time"]);
                break;
            }
        case "lobbyopen":
            {
                //hideMenu();
                addChatMessage(input["chatmessage"]);
                setStatus("Lobby id: " + input["id"] + " | " + input["gamemode"] + " | ");
                currentLobbyId = input["id"];
                break;
            }

        case "addlobby":
            {
                addLobby(getLobbyObject(input));
                break;
            }
        case "removelobby":
            {
                var lobby = getLobbyObject(input);
                var lobbyIndex = getLobbyById(lobby);
                if (lobbyIndex != -1) {
                    removeLobby(lobby, lobbyIndex);
                }
                break;
            }
        case "addlobbys":
            {
                for (var i = 0; i < input['lobbys'].length; i++) {
                    addLobby(getLobbyObject(input['lobbys'][i]));
                }
                break;
            }
        case "leave":
            {
                setStatus("Not in a lobby");
                addChatMessage(input['chatmessage']);
                currentLobbyId = -1;
                break;
            }

        case "start":
            {
                reset();
                displayChar = input['display'];
                if (input['selecting']) {
                    addChatMessage("Your turn to select");
                } else {
                    addChatMessage("Opponents turn to select");
                }
                selecting = input['selecting'];
                tempStatus(" | " + String.fromCharCode(displayChar) + " | " + selecting ? "Your turn" : "Opponents turn");
                break;
            }

        case "turn":
            {
                document.getElementById(input['x'] + "," + input['y']).innerHTML = String.fromCharCode(input['display']);
                if (input['selecting']) {
                    addChatMessage("Your turn to select");

                } else {
                    addChatMessage("Opponents turn to select");
                }
                selecting = input['selecting'];
                tempStatus(" | " + String.fromCharCode(displayChar) + " | " + selecting ? "Your turn" : "Opponents turn");
                //setStatus(input['selecting'] ? "test":)
                break;
            }
        case "end":
            {
                //Todo: support for highlite...
                document.getElementById(input['finalx'] + "," + input['finaly']).innerHTML = String.fromCharCode(input['display']);
                if (input["type"] == 0) {
                    //Tie
                    addChatMessage("Game ended in a tie.");
                } else if (input["type"] == 1) {
                    //We won
                    addChatMessage("You won!");
                } else {
                    //They won
                    addChatMessage("Your opponent won.");
                }
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
}

function getLobbyObject(a) {
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
    //Todo: reset
    displayChar = ' ';
    selecting = false;
    genTables();
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

    var table = document.getElementById("servertable");

    var tableRow = document.createElement("tr");
    tableRow.setAttribute("id", "row" + lobby.id);
    tableRow.addEventListener('click', function() {

        if (selectedRow != -1) {
            try {
                document.getElementById("row" + selectedRow).style = "background-color:white";
            } catch (e) {

            }
        }
        tableRow.style = "background-color:lightgray";
        selectedRow = tableRow.id.substring(3);
    });
    tableRow.innerHTML = "<td class = 'tdid'>" + lobby.id + "</td><td class = 'tdname'>" + lobby.name + "</td><td class = 'tdplayers'>" + lobby.players + "/" + lobby.maxplayers +
        "</td><td class = 'tdprivate'>" + lobby.private + "</td><td class = 'tdgamemode'>" + lobby.gamemode + "</td>";
    table.appendChild(tableRow);
}

function genTables() {
    var table = document.getElementById("grid");
    table.innerHTML = "";
    for (var i = 0; i < 3; i++) {
        var tableRow = document.createElement("tr");
        tableRow.setAttribute("id", "gridrow" + i);

        for (var x = 0; x < 3; x++) {
            var tableData = document.createElement("td");
            //tableData.innerHTML = i + "," + x;
            tableData.innerHTML = " ";
            tableData.setAttribute("id", i + "," + x);
            tableRow.appendChild(tableData);
        }
        table.appendChild(tableRow);
    }
}

function removeLobby(lobby, lobbyIndex) {
    lobbyStore.splice(lobbyIndex, 1);
    var element = document.getElementById("row" + lobby.id);
    element.parentNode.removeChild(element);
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

function addChatMessage(message) {
    $("#chatdisplay").animate({
        scrollTop: $("#chatdisplay")[0].scrollHeight
    }, 100);
    document.getElementById("chatdisplay").innerHTML += "<br>" + message.replace(/\n/g, "<br>");
}

function setStatus(a) {
    statusstr = a;
    //status.innerHTML = a;
    document.getElementById("status").innerText = a;
}

function tempStatus(a) {
    document.getElementById("status").innerText = statusstr + a;
}

function showMenu() {
    hide("tictac");
    hide("buttonpanel");
    showname("serverbuttonpanel");
    showname("container3");
    menuopen = true;
}

function hideMenu() {
    showname("chatpanel");
    showname("tictac");
    showname("buttonpanel");
    hide("container3");
    hide("serverbuttonpanel");
    menuopen = false;
}