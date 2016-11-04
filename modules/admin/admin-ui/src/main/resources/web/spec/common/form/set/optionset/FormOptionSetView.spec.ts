module FormOptionSetViewSpec {

    import FormOptionSet = api.form.FormOptionSet;
    import FormOptionSetJson = api.form.json.FormOptionSetJson;
    import FormOptionSetOptionJson = api.form.json.FormOptionSetOptionJson;
    import FormOptionSetView = api.form.FormOptionSetView;
    import FormOptionSetViewConfig = api.form.FormOptionSetViewConfig;
    import PropertySet = api.data.PropertySet;
    import FormContext = api.form.FormContext;
    import ValidationRecording = api.form.ValidationRecording;
    import FormItem = api.form.FormItem;
    import FormOptionSetOccurrenceView = api.form.FormOptionSetOccurrenceView;
    import FormItemPath = api.form.FormItemPath;
    import RecordingValidityChangedEvent = api.form.RecordingValidityChangedEvent;
    import FormOptionSetOccurrences = api.form.FormOptionSetOccurrences;
    import Button = api.ui.button.Button;
    import AEl = api.dom.AEl;

    describe("api.form.FormOptionSetView", function () {

        let optionSet: FormOptionSet;
        let optionSetViewConfig: FormOptionSetViewConfig;
        let optionSetView: FormOptionSetView;

        beforeEach(function () {
            optionSet = FormOptionSetSpec.createOptionSet(FormOptionSetSpec.getOptionSetJsonWithOptions());
            optionSetViewConfig = getFormOptionSetViewConfig(optionSet, getPropertySet());
            optionSetView = new FormOptionSetView(optionSetViewConfig);
        });

        describe("constructor", function () {

            it('should be defined', function () {
                expect(optionSetView).toBeDefined();
            });

            it('should have help text', function () {
                expect(optionSetView.hasHelpText()).toBeTruthy();
            });

            it('should have form option set', function () {
                expect(optionSetView['formOptionSet']).toEqual(optionSetViewConfig.formOptionSet);
            });

            it('should have parent data set', function () {
                expect(optionSetView['parentDataSet']).toEqual(optionSetViewConfig.parentDataSet);
            });

            it('should have even class when first level', function () {
                expect(optionSetView.hasClass('even')).toBeTruthy();
            });

            it('should have odd class when fourth level', function () {
                spyOn(optionSet, 'getPath').and.returnValue(FormItemPath.fromString('path.to.some.view'));
                let optionSetView2 = new FormOptionSetView(optionSetViewConfig);

                expect(optionSetView2.hasClass('odd')).toBeTruthy();
            });

        });

        describe('layout()', function () {
            var formOptionSetOccurrencesConstructor,
                formOptionSetOccurrences: FormOptionSetOccurrences;

            beforeEach(function () {
                formOptionSetOccurrencesConstructor = api.form.FormOptionSetOccurrences;

                spyOn(api.form, 'FormOptionSetOccurrences').and.callFake(function (config) {
                    formOptionSetOccurrences = new formOptionSetOccurrencesConstructor(config);
                    return formOptionSetOccurrences;
                });

                spyOn(optionSetView, "validate").and.stub();
            });

            describe('default behaviour', function () {
                var addButtonSpy, collapseButtonSpy;

                beforeEach(function (done) {
                    addButtonSpy = spyOn(optionSetView, "makeAddButton").and.callThrough();
                    collapseButtonSpy = spyOn(optionSetView, "makeCollapseButton").and.callThrough();
                    spyOn(api.form.FormSetOccurrences.prototype, "layout").and.returnValue(wemQ<void>(null));

                    optionSetView.layout().then(function () {
                        done();
                    });
                });

                it('should create a container for occurrence views and append it to DOM', function () {
                    expect(optionSetView.getEl().getElementsByClassName("occurrence-views-container").length).toEqual(1);
                });

                it('should create form option set occurrences', function () {
                    expect(api.form.FormOptionSetOccurrences).toHaveBeenCalled();
                });

                it('should perform layout of the option set occurrences with validation', function () {
                    expect(formOptionSetOccurrences.layout).toHaveBeenCalledWith(true);
                });

                it('should create add button for occurrences', function () {
                    expect(addButtonSpy).toHaveBeenCalled();
                });

                it('should create collapse button for occurrences', function () {
                    expect(collapseButtonSpy).toHaveBeenCalled();
                });

                it('should run validation', function () {
                    expect(optionSetView.validate).toHaveBeenCalled();
                });

            });

            describe('when layout is called without validation', function () {

                beforeEach(function (done) {
                    spyOn(api.form.FormSetOccurrences.prototype, "layout").and.returnValue(wemQ<void>(null));

                    optionSetView.layout(false).then(function () {
                        done();
                    });
                });

                it('should perform layout of the option set occurrences without validation', function () {
                    expect(formOptionSetOccurrences.layout).toHaveBeenCalledWith(false);
                });

                it('should NOT run validation', function () {
                    expect(optionSetView.validate).not.toHaveBeenCalled();
                });
            });

            describe('occurrences events', function () {
                let handleValiditySpy;

                beforeEach(function (done) {
                    spyOn(optionSetView, 'refresh').and.stub();
                    handleValiditySpy = spyOn(optionSetView, 'handleFormSetOccurrenceViewValidityChanged').and.stub();
                    spyOn(optionSetView, 'notifyEditContentRequested').and.callThrough();
                    // need actual layout to pass edit content request event
                    spyOn(api.form.FormSetOccurrences.prototype, "layout").and.callThrough();

                    optionSetView.layout(false).then(function () {
                        done();
                    });
                });

                it('should listen to occurrence added', function () {
                    formOptionSetOccurrences['notifyOccurrenceAdded'](null, null);
                    expect(optionSetView.refresh).toHaveBeenCalled();
                });

                it('should listen to occurrence rendered', function () {
                    formOptionSetOccurrences['notifyOccurrenceRendered'](null, null, false);
                    expect(optionSetView.validate).toHaveBeenCalled();
                });

                it('should listen to occurrence removed', function () {
                    formOptionSetOccurrences['notifyOccurrenceRemoved'](null, null);
                    expect(optionSetView.refresh).toHaveBeenCalled();
                });

                it('should listen to validity changed event', function () {
                    let views = formOptionSetOccurrences.getOccurrenceViews();
                    views[0]['notifyValidityChanged'](null);
                    expect(handleValiditySpy).toHaveBeenCalled();
                });

                it('should listen to edit content request', function () {
                    let views = formOptionSetOccurrences.getOccurrenceViews();
                    views[0].getFormItemViews()[0]['notifyEditContentRequested'](null);
                    expect(optionSetView.notifyEditContentRequested).toHaveBeenCalled();
                });

            });

            describe('buttons interaction', function () {
                let addSpy, collapseSpy, showSpy;

                beforeEach(function (done) {
                    addSpy = spyOn(optionSetView, 'makeAddButton').and.callThrough();
                    collapseSpy = spyOn(optionSetView, 'makeCollapseButton').and.callThrough();
                    showSpy = spyOn(api.form.FormSetOccurrences.prototype, "showOccurrences").and.callThrough();

                    optionSetView.layout(false).then(function () {
                        done();
                    });
                });

                it('should add occurrence on add button click', function () {
                    let createSpy = spyOn(api.form.FormSetOccurrences.prototype, 'createAndAddOccurrence');

                    let button: Button = addSpy.calls.mostRecent().returnValue;
                    button.getHTMLElement().click();

                    expect(createSpy).toHaveBeenCalled();
                });

                it('should collapse occurrence on collapse link click', function () {
                    spyOn(api.form.FormSetOccurrences.prototype, "isCollapsed").and.returnValues(false);

                    let link: AEl = collapseSpy.calls.mostRecent().returnValue;
                    link.getHTMLElement().click();

                    expect(link.getHtml()).toEqual('Expand');
                    expect(showSpy).toHaveBeenCalledWith(false);
                });

                it('should expand occurrence on expand button click', function () {
                    spyOn(api.form.FormSetOccurrences.prototype, "isCollapsed").and.returnValues(true);

                    let link: AEl = collapseSpy.calls.mostRecent().returnValue;
                    link.getHTMLElement().click();

                    expect(link.getHtml()).toEqual('Collapse');
                    expect(showSpy).toHaveBeenCalledWith(true);
                });
            })
        });

        describe("validate()", function () {

            it('should throw an exception if not laid out yet', function () {
                expect(optionSetView.validate).toThrowError("Can't validate before layout is done");
            });

            describe('after layout was done', function () {
                let renderValidationErrorsSpy, notifyValidityChangedSpy;

                beforeEach(function (done) {
                    optionSetView.layout(false).then(function () {
                        done();
                    });

                    renderValidationErrorsSpy = spyOn(optionSetView, 'renderValidationErrors').and.callThrough();
                    notifyValidityChangedSpy = spyOn(optionSetView, 'notifyValidityChanged').and.callThrough();
                });

                describe('default behavior', function () {
                    let recording: ValidationRecording;

                    beforeEach(function () {
                        recording = optionSetView.validate();
                    });

                    it('should return ValidationRecording', function () {
                        expect(recording).toBeDefined();
                    });

                    it('should have called renderValidationErrors', function () {
                        expect(renderValidationErrorsSpy).toHaveBeenCalled();
                    });

                    it('should NOT have called notifyValidityChanged', function () {
                        expect(notifyValidityChangedSpy).not.toHaveBeenCalled();
                    });
                });

                describe('not silent validate', function () {
                    let recording;

                    beforeEach(function () {
                        recording = optionSetView.validate(false);
                    });

                    it('should have called notifyValidityChanged', function () {
                        expect(notifyValidityChangedSpy).toHaveBeenCalled();
                    });
                });

                describe('validate with exclusions', function () {
                    let recording;

                    beforeEach(function () {
                        recording = optionSetView.validate(false);
                    });
                });
            });
        });

        describe('update()', function () {

            it('should become invalid after setting invalid data', function (done) {

                optionSetView.layout(false).then(function () {

                    let recording = optionSetView.validate();
                    expect(recording.isValid()).toBeTruthy('ValidationRecording should be valid');

                    optionSetView.update(getPropertySet(false)).then(function () {

                        recording = optionSetView.validate();
                        expect(recording.isValid()).toBeFalsy('ValidationRecording should\'ve become invalid');

                        done();
                    });
                });
            });

            it('should become valid after setting valid data', function (done) {

                optionSetView = new FormOptionSetView(getFormOptionSetViewConfig(optionSet, getPropertySet(false)));
                optionSetView.layout(false).then(function () {

                    let recording = optionSetView.validate();
                    expect(recording.isValid()).toBeFalsy('ValidationRecording should be invalid');

                    optionSetView.update(getPropertySet()).then(function () {

                        recording = optionSetView.validate();
                        expect(recording.isValid()).toBeTruthy('ValidationRecording should\'ve become valid');

                        done();
                    });
                });
            })
        })
    });

    export function getFormOptionSetViewConfig(optionSet: FormOptionSet, dataSet: PropertySet): FormOptionSetViewConfig {
        return {
            context: getFormContext(),
            formOptionSet: optionSet,
            parent: undefined,
            parentDataSet: dataSet
        }
    }

    export function getFormContext(): FormContext {
        return FormContext.create().setShowEmptyFormItemSetOccurrences(true).build();
    }

    export function getPropertySet(valid: boolean = true): PropertySet {
        var tree = new api.data.PropertyTree();
        var set = tree.addPropertySet('optionSet');

        var optionSet1 = set.addPropertySet("option1");
        optionSet1.addString("input1", "Option 1 value from data");

        var optionSet2 = set.addPropertySet("option2");
        var itemSet1 = optionSet2.addPropertySet('itemSet1');
        if (valid) {
            itemSet1.addString("input2-1", "Option 2 value from data");
            itemSet1.addBoolean("input2-2", true);
        }

        return tree.getRoot();
    }
}