module app.remove {

    export class ModuleDeleteDialog extends api.app.remove.DeleteDialog {

        private moduleToDelete:api.module.ModuleSummary;

        constructor() {
            super("Module");

            this.setDeleteAction(new ModuleDeleteDialogAction());

            this.getDeleteAction().addExecutionListener(() => {
                new api.module.DeleteModuleRequest(this.moduleToDelete.getModuleKey().toString()).sendAndParse()
                    .done((module:api.module.Module) => {
                        api.notify.showFeedback('Module \'' + module.getDisplayName() + '\' was deleted');
                        new api.module.ModuleDeletedEvent(module.getModuleKey()).fire();
                    }).fail(() => {
                        api.notify.showError('Error while deleting module.');
                    }).always(() => {
                        this.close();
                    });
            });
        }

        setModuleToDelete(moduleModel:api.module.ModuleSummary) {
            this.moduleToDelete = moduleModel;
            var deleteItem = new api.app.remove.DeleteItem(api.util.getAdminUri('common/images/icons/icoMoon/32x32/puzzle.png'), moduleModel.getDisplayName());
            var deleteItems:api.app.remove.DeleteItem[] = [deleteItem];
            this.setDeleteItems(deleteItems);
        }
    }

    export class ModuleDeleteDialogAction extends api.ui.Action {

        constructor() {
            super("Delete", "enter");
        }
    }
}
