$(document).ready(function() {
    $(".def").hover( function() { defHoverOn(this); }, function() { defHoverOff(this); } );
});

function defHoverOn(node) {
    var term = node.innerHTML.toLowerCase();
    createDefHover();
    $("#defHoverTitle").html(term);
    
    $.get("/content/def/"+ term +".php", null, function(data, textStatus) {
        $("#defHoverCopy").html(data);
        
        var win = $(window);
        var divjq = $("#defHoverDiv");
        var nodejq = $(node);
        divjq.show();
        
        setTimeout(function() {
            var nodeoff = nodejq.offset();
            var h = divjq.height();
            
            var top = nodeoff.top + nodejq.height() + 5;
            if (top + divjq.height() > win.scrollTop() + win.height())
                // Off the bottom. Show above the node instead.
                top = nodeoff.top - divjq.height() - 17;
            
            var left = nodeoff.left - 10;
            if (left + divjq.width() > win.scrollLeft() + win.width())
                // Off the right side. Show to the left instead.
                left = nodeoff.left + nodejq.width() - divjq.width();
            
            // If content is centered, so we need to subtract off its offset too.
            //left -= $("#hoverHook").offset().left;
            
            divjq.css("top", top +"px");
            divjq.css("left", left +"px");
        }, 1);
    });
}

function defHoverOff(node) {
    $("#defHoverDiv").hide();
}

function createDefHover() {
    if ($("#defHoverDiv").length > 0)
        return;
    
    var html = '';
    html += '<div id="defHoverDiv" style="display:none;top:0;left:0;max-width:450px;">';
    html += '<span id="defHoverTitle"></span>: ';
    html += '<span id="defHoverCopy"></span>';
    html += '</div>';
    $("#hoverHook").append(html);
}

function dbclickCheck(btn) {
    if (btn.wasClicked)
        return false;
    btn.wasClicked = true;
    return true;
}