module api.content {

    import AccessControlList = api.security.acl.AccessControlList;
    import PropertyTree = api.data2.PropertyTree;
    import PropertyPath = api.data2.PropertyPath;
    import PropertyIdProvider = api.data2.PropertyIdProvider;

    export class Content extends ContentSummary implements api.Equitable, api.Cloneable {

        private data: PropertyTree;

        private metadata: Metadata[] = [];

        private form: api.form.Form;

        private pageObj: api.content.page.Page;

        private permissions: AccessControlList;

        private inheritedPermissions: AccessControlList;

        private inheritPermissions: boolean;

        constructor(builder: ContentBuilder) {
            super(builder);

            api.util.assertNotNull(builder.data, "data is required for Content");
            this.data = builder.data;
            this.form = builder.form;

            this.metadata = builder.metadata;
            this.pageObj = builder.pageObj;
            this.permissions = builder.permissions || new AccessControlList();
            this.inheritedPermissions = builder.inheritedPermissions || new AccessControlList();
            this.inheritPermissions = builder.inheritPermissions;
        }

        getContentData(): PropertyTree {
            return this.data;
        }

        getMetadata(name: api.schema.metadata.MetadataSchemaName): Metadata {
            return this.metadata.filter((item: Metadata) => item.getName().equals(name))[0];
        }

        getAllMetadata(): Metadata[] {
            return this.metadata;
        }

        getForm(): api.form.Form {
            return this.form;
        }

        getPage(): api.content.page.Page {
            return this.pageObj;
        }

        getPermissions(): AccessControlList {
            return this.permissions;
        }

        getInheritedPermissions(): AccessControlList {
            return this.inheritedPermissions;
        }

        isInheritPermissionsEnabled(): boolean {
            return this.inheritPermissions;
        }

        equals(o: api.Equitable): boolean {

            if (!api.ObjectHelper.iFrameSafeInstanceOf(o, Content)) {
                return false;
            }

            if (!super.equals(o)) {
                return false;
            }

            var other = <Content>o;

            if (!api.ObjectHelper.equals(this.data, other.data)) {
                return false;
            }

            if (!api.ObjectHelper.equals(this.form, other.form)) {
                return false;
            }

            if (!api.ObjectHelper.equals(this.pageObj, other.pageObj)) {
                return false;
            }

            if (!api.ObjectHelper.equals(this.permissions, other.permissions)) {
                return false;
            }

            if (!api.ObjectHelper.equals(this.inheritedPermissions, other.inheritedPermissions)) {
                return false;
            }

            if (this.inheritPermissions !== other.inheritPermissions) {
                return false;
            }

            return true;
        }

        clone(): Content {
            return this.newBuilder().build();
        }

        newBuilder(): ContentBuilder {
            return new ContentBuilder(this);
        }

        static fromJson(json: api.content.json.ContentJson, propertyIdProvider: PropertyIdProvider): Content {

            var type = new api.schema.content.ContentTypeName(json.type);

            if (type.isSite()) {
                return new site.SiteBuilder().fromContentJson(json, propertyIdProvider).build();
            }
            else if (type.isPageTemplate()) {
                return new page.PageTemplateBuilder().fromContentJson(json, propertyIdProvider).build();
            }
            return new ContentBuilder().fromContentJson(json, propertyIdProvider).build();
        }
    }

    export class ContentBuilder extends ContentSummaryBuilder {

        data: PropertyTree;

        form: api.form.Form;

        metadata: Metadata[];

        pageObj: api.content.page.Page;

        permissions: AccessControlList;

        inheritedPermissions: AccessControlList;

        inheritPermissions: boolean = true;

        constructor(source?: Content) {
            super(source);
            if (source) {

                this.data = source.getContentData() ? source.getContentData().copy() : null;
                this.form = source.getForm();

                this.metadata = source.getAllMetadata().map((metadata: Metadata) => metadata.clone());

                this.pageObj = source.getPage() ? source.getPage().clone() : null;
                this.permissions = source.getPermissions(); // TODO clone?
                this.inheritedPermissions = source.getInheritedPermissions(); // TODO clone?
                this.inheritPermissions = source.isInheritPermissionsEnabled();
            }
        }

        fromContentJson(json: api.content.json.ContentJson, propertyIdProvider: PropertyIdProvider): ContentBuilder {

            super.fromContentSummaryJson(json);

            this.data = PropertyTree.fromJson(json.data, propertyIdProvider);
            this.metadata = [];
            json.metadata.forEach((metadataJson: api.content.json.MetadataJson) => {
                this.metadata.push(Metadata.fromJson(metadataJson, propertyIdProvider));
            });
            this.form = json.form != null ? api.form.Form.fromJson(json.form) : null;

            if (this.page) {
                this.pageObj = new api.content.page.PageBuilder().fromJson(json.page, propertyIdProvider).build();
                this.page = true;
            }
            if (json.permissions) {
                this.permissions = AccessControlList.fromJson(json.permissions);
            }
            if (json.inheritedPermissions) {
                this.inheritedPermissions = AccessControlList.fromJson(json.inheritedPermissions);
            }
            if (typeof json.inheritPermissions !== "undefined") {
                this.inheritPermissions = json.inheritPermissions;
            }

            return this;
        }

        setData(value: PropertyTree): ContentBuilder {
            this.data = value;
            return this;
        }

        setForm(value: api.form.Form): ContentBuilder {
            this.form = value;
            return this;
        }

        setPage(value: api.content.page.Page): ContentBuilder {
            this.pageObj = value;
            this.page = value ? true : false;
            return this;
        }

        setMetadata(metadata: Metadata[]): ContentBuilder {
            this.metadata = metadata;
            return this;
        }

        setPermissions(value: AccessControlList): ContentBuilder {
            this.permissions = value;
            return this;
        }

        setInheritedPermissions(value: AccessControlList): ContentBuilder {
            this.inheritedPermissions = value;
            return this;
        }

        setInheritPermissionsEnabled(value: boolean): ContentBuilder {
            this.inheritPermissions = value;
            return this;
        }

        build(): Content {
            return new Content(this);
        }
    }
}