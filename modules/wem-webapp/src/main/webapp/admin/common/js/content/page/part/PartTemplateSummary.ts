module api.content.page.part {

    export class PartTemplateSummary extends api.content.page.TemplateSummary {

        constructor(builder: PartTemplateSummaryBuilder) {
            super(builder);
        }
    }

    export class PartTemplateSummaryBuilder extends api.content.page.TemplateSummaryBuilder {

        public build(): PartTemplateSummary {
            return new PartTemplateSummary(this);
        }

        static fromJson(json: api.content.page.part.json.PartTemplateSummaryJson): PartTemplateSummaryBuilder {
            var builder = new PartTemplateSummaryBuilder();
            builder.setKey(api.content.page.TemplateKey.fromString(json.key));
            builder.setDisplayName(json.displayName);
            builder.setDescriptorKey(api.content.page.DescriptorKey.fromString(json.descriptorKey));
            return builder;
        }
    }
}