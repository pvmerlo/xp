module api.content.site.inputtype.moduleconfigurator {

    import AEl = api.dom.AEl;
    import PropertyTree = api.data2.PropertyTree;
    import PropertySet = api.data2.PropertySet;
    import Option = api.ui.selector.Option;
    import FormView = api.form.FormView;
    import FormContextBuilder = api.form.FormContextBuilder;
    import Module = api.module.Module;
    import OptionSelectedEvent = api.ui.selector.OptionSelectedEvent;
    import LoadedDataEvent = api.util.loader.event.LoadedDataEvent;

    export class ModuleView extends api.dom.DivEl {

        private module: Module;

        private formView: FormView;

        private config: PropertySet;

        private removeClickedListeners: {(event: MouseEvent): void;}[];

        private collapseClickedListeners: {(event: MouseEvent): void;}[];

        constructor(mod: Module, config: PropertySet) {
            super("module-view");

            this.removeClickedListeners = [];
            this.collapseClickedListeners = [];

            this.module = mod;
            this.config = config;

            var header = new api.dom.DivEl('header');

            var namesAndIconView = new api.app.NamesAndIconView(new api.app.NamesAndIconViewBuilder().
                setSize(api.app.NamesAndIconViewSize.large)).
                setMainName(this.module.getDisplayName()).
                setSubName(this.module.getName() + "-" + this.module.getVersion()).
                setIconClass("icon-xlarge icon-puzzle");

            header.appendChild(namesAndIconView);

            var removeButton = new api.dom.AEl("remove-button icon-close");
            removeButton.onClicked((event: MouseEvent) => {
                this.notifyRemoveClicked(event);
            });
            header.appendChild(removeButton);

            var collapseButton = new api.dom.AEl('collapse-button');
            collapseButton.setHtml('Collapse');
            collapseButton.onClicked((event: MouseEvent) => {
                if (this.formView.isVisible()) {
                    this.formView.hide();
                    collapseButton.setHtml('Expand');
                    this.addClass('collapsed');
                } else {
                    this.formView.show();
                    collapseButton.setHtml('Collapse');
                    this.removeClass('collapsed');
                }
                this.notifyCollapseClicked(event);
            });
            header.appendChild(collapseButton);

            this.appendChild(header);

            var formContext = new FormContextBuilder().build();

            this.formView = new FormView(formContext, this.module.getForm(), this.config);
            this.formView.addClass("module-form");
            this.appendChild(this.formView);
        }


        getModule(): Module {
            return this.module;
        }

        getFormView(): FormView {
            return this.formView;
        }

        onRemoveClicked(listener: (event: MouseEvent) => void) {
            this.removeClickedListeners.push(listener);
        }

        unRemoveClicked(listener: (event: MouseEvent) => void) {
            this.removeClickedListeners = this.removeClickedListeners.filter((curr) => {
                return listener != curr;
            })
        }

        private notifyRemoveClicked(event: MouseEvent) {
            this.removeClickedListeners.forEach((listener) => {
                listener(event);
            })
        }

        onCollapseClicked(listener: (event: MouseEvent) => void) {
            this.collapseClickedListeners.push(listener);
        }

        unCollapseClicked(listener: (event: MouseEvent) => void) {
            this.collapseClickedListeners = this.collapseClickedListeners.filter((curr) => {
                return listener != curr;
            })
        }

        private notifyCollapseClicked(event: MouseEvent) {
            this.collapseClickedListeners.forEach((listener) => {
                listener(event);
            })
        }
    }
}