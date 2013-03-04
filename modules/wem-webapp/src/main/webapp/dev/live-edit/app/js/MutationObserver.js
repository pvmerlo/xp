(function ($) {
    'use strict';

    // Class definition (constructor function)
    var mutationObserver = AdminLiveEdit.MutationObserver = function () {
        this.mutationObserver = null;
        this.$observedComponent = null;
        this.registerGlobalListeners();
    };

    // Fix constructor
    mutationObserver.constructor = mutationObserver;

    // Shorthand ref to the prototype
    var proto = mutationObserver.prototype;


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    proto.registerGlobalListeners = function () {
        var me = this;
        /*$(window).on('component:mouseover', $.proxy(me.observe, me));
        $(window).on('component:mouseout', $.proxy(me.disconnect, me));
        $(window).on('component:click:select', $.proxy(me.observe, me));
        $(window).on('component:click:deselect', $.proxy(me.disconnect, me));*/

        $(window).on('component:paragraph:edit', $.proxy(me.observe, me));
        $(window).on('shader:click', $.proxy(me.disconnect, me));

    };


    proto.observe = function (event, $component) {
        var me = this;

        var isAlreadyObserved = me.$observedComponent && me.$observedComponent[0] === $component[0];
        if (isAlreadyObserved) {
            return;
        }
        me.disconnect(event);

        me.$observedComponent = $component;

        me.mutationObserver = new AdminLiveEdit.MutationSummary({
            callback: function (summaries) {
                me.onMutate(summaries, event);
            },
            rootNode: $component[0],
            queries: [{ all: true}]
        });
    };


    // Called when the html in the observed component mutates
    proto.onMutate = function (summaries, event) {
        if (summaries && summaries[0]) {
            var $targetComponent = $(summaries[0].target);
            var targetComponentIsSelected = $targetComponent.hasClass('live-edit-selected-component');
            var componentIsNotSelectedAndMouseIsOver = event.type === 'component:mouseover' && !targetComponentIsSelected;
            var componentIsParagraphAndBeingEdited = $targetComponent.data('live-edit-paragraph-mode');


            if (componentIsParagraphAndBeingEdited) {
                $(window).trigger('component:paragraph:edit', [$targetComponent]);
            } else if (componentIsNotSelectedAndMouseIsOver) {
                $(window).trigger('component:mouseover', [$targetComponent]);
            } else {
                $(window).trigger('component:click:select', [$targetComponent]);
            }
        }
    };


    proto.disconnect = function (event) {
        var targetComponentIsSelected = (this.$observedComponent && this.$observedComponent.hasClass('live-edit-selected-component'));
        var componentIsSelectedAndUserMouseOut = event.type === 'component:mouseout' && targetComponentIsSelected;
        if (componentIsSelectedAndUserMouseOut) {
            return;
        }

        this.$observedComponent = null;
        if (this.mutationObserver) {
            this.mutationObserver.disconnect();
            this.mutationObserver = null;
        }
    };

}($liveedit));