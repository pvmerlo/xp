module app_wizard {

    export class RelationshipTypeWizardPanel extends api_app_wizard.WizardPanel {

        private static DEFAULT_CHEMA_ICON_URL:string = '/admin/rest/schema/image/RelationshipType:_:_';

        private saveAction:api_ui.Action;

        private closeAction:api_ui.Action;

        private formIcon:api_app_wizard.FormIcon;

        private toolbar:api_ui_toolbar.Toolbar;

        private persistedRelationshipType:api_remote_relationshiptype.RelationshipType;

        private relationshipTypeForm:RelationshipTypeForm;

        constructor(id:string) {
            this.formIcon =
            new api_app_wizard.FormIcon(RelationshipTypeWizardPanel.DEFAULT_CHEMA_ICON_URL, "Click to upload icon", "rest/upload");

            this.closeAction = new api_app_wizard.CloseAction(this);
            this.saveAction = new api_app_wizard.SaveAction(this);

            this.toolbar = new RelationshipTypeWizardToolbar({
                saveAction: this.saveAction,
                closeAction: this.closeAction
            });

            super({
                formIcon: this.formIcon,
                toolbar: this.toolbar
            });

            this.setDisplayName("New Relationship Type");
            this.setName(this.generateName(this.getDisplayName()));
            this.setAutogenerateDisplayName(true);
            this.setAutogenerateName(true);
            this.relationshipTypeForm = new RelationshipTypeForm();
            this.addStep(new api_app_wizard.WizardStep("Relationship Type", this.relationshipTypeForm));
        }

        setPersistedItem(relationshipType:api_remote_relationshiptype.RelationshipType) {
            super.setPersistedItem(relationshipType);

            this.setDisplayName(relationshipType.displayName);
            this.setName(relationshipType.name);
            this.formIcon.setSrc(relationshipType.iconUrl);

            this.setAutogenerateDisplayName(!relationshipType);
            this.setAutogenerateName(!relationshipType.name || relationshipType.name == this.generateName(relationshipType.displayName));

            this.persistedRelationshipType = relationshipType;

            var relationshipTypeGetParams:api_remote_relationshiptype.GetParams = {
                qualifiedName: relationshipType.module + ":" + relationshipType.name,
                format: 'XML'
            };

            api_remote_relationshiptype.RemoteRelationshipTypeService.relationshipType_get(relationshipTypeGetParams, (result:api_remote_relationshiptype.GetResult) => {
                this.relationshipTypeForm.setFormData({"xml": result.relationshipTypeXml})
            });
        }

        persistNewItem(successCallback?:() => void) {
            var formData = this.relationshipTypeForm.getFormData();
            var createParams:api_remote_relationshiptype.CreateOrUpdateParams = {
                relationshipType: formData.xml,
                iconReference: this.getIconUrl()
            };

            api_remote_relationshiptype.RemoteRelationshipTypeService.relationshipType_createOrUpdate(createParams, () => {
                new app_wizard.RelationshipTypeCreatedEvent().fire();
                api_notify.showFeedback('Relationship type was created!');
            });
        }

        updatePersistedItem(successCallback?:() => void) {
            var formData = this.relationshipTypeForm.getFormData();
            var updateParams:api_remote_relationshiptype.CreateOrUpdateParams = {
                relationshipType: formData.xml,
                iconReference: this.getIconUrl()
            };

            api_remote_relationshiptype.RemoteRelationshipTypeService.relationshipType_createOrUpdate(updateParams, () => {
                new app_wizard.RelationshipTypeUpdatedEvent().fire();
                api_notify.showFeedback('Relationship type was saved!');
            });
        }

    }
}