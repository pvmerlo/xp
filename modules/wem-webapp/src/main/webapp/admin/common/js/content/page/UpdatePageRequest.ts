module api.content.page {

    export class UpdatePageRequest extends PageResourceRequest<api.content.json.ContentJson> {

        private contentId: api.content.ContentId;

        private template: api.content.page.TemplateKey;

        private config: api.data.RootDataSet;

        private regions: PageRegions;

        constructor(contentId: api.content.ContentId) {
            super();
            super.setMethod("POST");
            this.contentId = contentId;
        }

        setPageTemplateKey(pageTemplateKey: api.content.page.TemplateKey): UpdatePageRequest {
            this.template = pageTemplateKey;
            return this;
        }

        setConfig(config: api.data.RootDataSet): UpdatePageRequest {
            this.config = config;
            return this;
        }

        setRegions(value: PageRegions): UpdatePageRequest {
            this.regions = value;
            return this;
        }

        getParams(): Object {
            return {
                contentId: this.contentId.toString(),
                template: this.template.toString(),
                config: this.config != null ? this.config.toJson() : null,
                regions: this.regions != null ? this.regions.toJson() : null
            };
        }

        getRequestPath(): api.rest.Path {
            return api.rest.Path.fromParent(super.getResourcePath(), "update");
        }

        sendAndParse(): JQueryPromise<api.content.Content> {

            var deferred = jQuery.Deferred<api.content.Content>();

            this.send().
                done((response: api.rest.JsonResponse<api.content.json.ContentJson>) => {
                var content = null;
                if( !response.isBlank() ) {
                    content = this.fromJsonToContent(response.getResult());
                }
                deferred.resolve(content);
            }).fail((response: api.rest.RequestError) => {
                    deferred.reject(null);
                });

            return deferred;
        }
    }
}