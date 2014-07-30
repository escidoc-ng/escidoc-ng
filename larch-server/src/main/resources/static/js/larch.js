var ctx = "";
$(document).ready(function() {
  var contextPath = $('#contextPath');
  if (contextPath) {
    if (contextPath.attr('src')) {
      ctx = contextPath.attr('src');
    }
  }
});

function createEntity(id, workspaceId, type, label, tags, parentId) {
    var tagList = tags.split(',');
    for (var i = 0; i< tagList.length;i++) {
        tagList[i] = $.trim(tagList[i]);
    }
    var entity = {
        'id' : id,
        'workspaceId' : workspaceId,
        'type' : type,
        'label' : label,
        'parentId' : parentId,
        'tags' : tagList
    };
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : csrf_token
        },
        url: ctx + "/workspace/" + workspaceId + "/entity",
        type: "POST",
        data: JSON.stringify(entity),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(createdId){
            document.location.href = ctx + 'workspace' + workspaceId + '/entity/' + createdId;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function createWorkspace(id, name) {
    var workspace = {
        'id' : id,
        'name' : name
    };
    var csrf_token = $("meta[name='_csrf']").attr("content");
    $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : csrf_token
        },
        url: ctx + "/workspace",
        type: "POST",
        data: JSON.stringify(workspace),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(createdId){
            document.location.href = ctx + '/workspace/' + createdId;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function deleteUser(name) {
   $.ajax ({
        xhrFields: {
           withCredentials: true
        },
        headers: {
            "X-CSRF-TOKEN" : $("meta[name='_csrf']").attr("content")
        },
        url: ctx + "/user/" + name,
        type: "DELETE",
        success: function(createdId){
           location.reload(false);
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}

function openUser(name) {
    document.location.href = ctx + '/user/' + name;
}

function edit(td, name) {
    var currentValue = $(td).html();
    $(td).unbind('click');
    $(td).removeAttr('onclick');
    $(td).html('<input id="__edit" type="text" name="' + name + '" value="' + currentValue + '"/>');
    $(td).append('<input type="hidden" id="' + name + 'OriginalValue" value="' + currentValue + '"/>');
    $('#__edit').focus();
    $('#__edit').blur(function() {
        stopEdit(td, name);
    });
}

function stopEdit(td, name) {
    var original = $('#' + name + 'OriginalValue').val();
    var newValue = $('#__edit').val();
    $('#__edit').unbind('blur');
    if (original != newValue) {
        $('#updateButton').unbind('click');
        $('#updateButton').removeClass('hidden');
        $('#updateButton').click(function () {
            patchEntity();
        });
        $('#cancelUpdateButton').unbind('click');
        $('#cancelUpdateButton').removeClass('hidden');
        $('#cancelUpdateButton').click(function() {
            document.location.href = document.location.href;
        });
        $(td).addClass('changed');
    }
    $(td).html(newValue);
    $(td).click(function() {
        edit(td, name);
    });
    patch[name] = newValue;
}

function patchEntity() {
    $.ajax({
        url : ctx + "/entity/" + $('#entityId').html(),
        type : "PATCH",
        data : JSON.stringify(patch),
        contentType : "application/json",
        success : function() {
            document.location.href = document.location.href;
        },
        error : function(request, msg, error) {
            throwError(request);
        }
    });
}
    
function throwError(request) {
    var responseText = null;
    if (request != null && request.responseText != null && request.responseText.length > 0) {
        try {
            responseText = JSON.parse(request.responseText);
        } catch (e) {}
    }
    var location = ctx + '/error-page';
    if (responseText != null) {
        if (responseText.status != null) {
            location += '?status=' + responseText.status;
        }
        if (responseText.message != null && responseText.message.length > 0) {
            if (location.length == 11) {
                location += '?';
            } else {
                location += '&';
            }
            location += 'message=' + responseText.message;
        }
        if (responseText.path != null && responseText.path.length > 0) {
            if (location.length == 11) {
                location += '?';
            } else {
                location += '&';
            }
            location += 'path=' + responseText.path;
        }
    }
    document.location.href = location;
}