
function animateloop () {

    var visible = $('.progress:visible');

    if (visible) {

        $("#progressinner").css({marginLeft: "-45%"});
        $("#progressinner").animate(
            {
                marginLeft: "145%"
            },
            2000,
            function() {
                animateloop()
            }
        );

    }

}

$(document).ready(function(){

    $('.modal-footer button').click(function(){

        var button = $(this);

        if ( button.attr("data-dismiss") != "modal" ){

            var inputs = $('form input');
            var title = $('.modal-title');
            var form = $('form');
            var progressBar = $('.progress');

            button.hide();
            progressBar.show();
            animateloop();

            form.submit();
            inputs.attr("disabled", "disabled");

        }
    });

    $('#loginModal').on('hidden.bs.modal', function (e) {
        ga('send', 'event', 'modal', 'hide', 'login');
    });

    $('#loginModal').on('shown.bs.modal', function (e) {

        var progressBar = $('.progress');
        var button = $('.modal-footer button');
        var inputs = $('form input');

        progressBar.hide();
        button.show();
        inputs.removeAttr("disabled");
        inputs.val("");

        ga('send', 'event', 'modal', 'show', 'login');

    });



});
