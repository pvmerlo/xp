interface ToolTipPosition {
    x: number;
    y: number;
}

module LiveEdit.ui {

    // Uses
    var $ = $liveEdit;
    var domHelper = LiveEdit.DomHelper;

    export class ToolTip extends LiveEdit.ui.Base {
        private OFFSET_X = 10;
        private OFFSET_Y = 10;

        constructor() {
            super();
            this.addView();
            this.attachEventListeners();
            this.registerGlobalListeners();
        }

        private registerGlobalListeners():void {
            $(window).on('selectComponent.liveEdit', () => this.hide());
        }

        private addView():void {
            var html:string = '<div class="live-edit-tool-tip" style="top:-5000px; left:-5000px;">' +
                              '    <span class="live-edit-tool-tip-name-text"></span>' +
                              '    <span class="live-edit-tool-tip-type-text"></span> ' +
                              '</div>';

            this.createHtmlFromString(html);
            this.appendTo($('body'));
        }

        private  setText(componentType:string, componentName:string):void {
            var $tooltip = this.getEl();
            $tooltip.children('.live-edit-tool-tip-type-text').text(componentType);
            $tooltip.children('.live-edit-tool-tip-name-text').text(componentName);
        }

        private attachEventListeners():void {
            $(document).on('mousemove', '[data-live-edit-type]', (event) => {

                // fixme: Use PubSub instead of calling DragDrop object.
                if (LiveEdit.Selection.pageHasSelectedElements() || LiveEdit.DragDropSort.isDragging()) {
                    this.hide();
                    return;
                }

                var pos = this.getPositionFromEvent(event);

                this.getEl().css({
                    top: pos.y,
                    left: pos.x
                });

            });

            $(document).on('hover', '[data-live-edit-type]', (event) => {
                if (event.type === 'mouseenter') {
                    var component:LiveEdit.component.Component = new LiveEdit.component.Component($(event.target).closest('[data-live-edit-type]'));
                    this.setText(component.getComponentType().getName(), component.getName());

                    this.getEl().hide(null).fadeIn(300);
                }
            });

            $(document).on('mouseout', () => this.hide());
        }

        private getPositionFromEvent(event:JQueryEventObject) {
            var pageX = event.pageX,
                pageY = event.pageY,
                x = pageX + this.OFFSET_X,
                y = pageY + this.OFFSET_Y,
                viewPortSize = domHelper.getViewPortSize(),
                scrollTop = domHelper.getDocumentScrollTop(),
                toolTipWidth = this.getEl().width(),
                toolTipHeight = this.getEl().height();

            if (x + toolTipWidth > (viewPortSize.width - this.OFFSET_X * 2) - 50) {
                x = pageX - toolTipWidth - (this.OFFSET_X * 2);
            }
            if (y + toolTipHeight > (viewPortSize.height + scrollTop - this.OFFSET_Y * 2)) {
                y = pageY - toolTipHeight - (this.OFFSET_Y * 2);
            }
            return {
                x: x,
                y: y
            };
        }

        private hide():void {
            this.getEl().css({
                top: '-5000px',
                left: '-5000px'
            });
        }

    }
}