var startWatch = function() {
    clearLog();
    LogManager.send(DWRUtil.getValue("log_file"));
}

function stopWatch() {
    LogManager.stop();
}

function startPoll() {
    DWREngine.setActiveReverseAjax(true);

    LogManager.getLogFileNames(function (data) {
        DWRUtil.removeAllOptions("log_file");
        DWRUtil.addOptions("log_file", data);
    });

    LogManager.getFilters(function(data) {
        for (var i = 0; i < data.length; i++) {
            addFilterDiv(data[i].pattern, data[i].id);
        }
    });
}

function stopPoll() {
    DWREngine.setActiveReverseAjax(false);
}

function clearLog() {
    var mainPanel = $("main_panel");
    while (mainPanel.hasChildNodes()) {
        mainPanel.removeChild(mainPanel.firstChild);
    }
}

var addNewLine = function(line) {
    var mainPanel = $("main_panel");
    var lineLimitNum = DWRUtil.getValue("num_input");
    var lineNum = mainPanel.childNodes.length;
    if (lineLimitNum != null && lineNum >= lineLimitNum) {
        mainPanel.removeChild(mainPanel.firstChild);
    }
    var li = document.createElement("li");
    var txt = document.createTextNode(line);
    li.appendChild(txt);
    mainPanel.appendChild(li);
}

function changeType(obj) {
    if (obj.checked == true) {
        LogManager.getLogFileNamesFromDir(function (data) {
            DWRUtil.removeAllOptions("log_file");
            DWRUtil.addOptions("log_file", data);
        });
    } else {
        LogManager.getLogFileNames(function (data) {
            DWRUtil.removeAllOptions("log_file");
            DWRUtil.addOptions("log_file", data);
        });
    }
}

function addFilter() {
    var regex = prompt("输入正则表达式", "");
    if (regex != null && regex != "") {
        LogManager.addFilter(regex, function (filterId) {
            addFilterDiv(regex, filterId);
        });
    }
}

function addFilterDiv(pattern, filterId) {
    var filterPanel = $("filter_panel");
    var filterItem = document.createElement("div");
    filterItem.id = "filter_" + filterId;
    filterPanel.appendChild(filterItem);

    var filterText = document.createElement("span");
    filterItem.appendChild(filterText);
    filterText.appendChild(document.createTextNode(pattern));
    filterText.className = "filterText";

    var filterDelLink = document.createElement("a");
    filterDelLink.href = "javascript:deleteFilter(" + filterId + ");";
    filterDelLink.appendChild(document.createTextNode("删除"));
    filterDelLink.className = "delLink";
    filterItem.appendChild(filterDelLink);
}

function deleteFilter(filterId) {
    LogManager.removeFilter(filterId, function(data) {
        var filterPanel = $("filter_panel");
        var target = document.getElementById("filter_" + filterId);
        filterPanel.removeChild(target);
    });
}