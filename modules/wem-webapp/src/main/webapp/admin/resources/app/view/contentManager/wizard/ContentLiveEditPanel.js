Ext.define('Admin.view.contentManager.wizard.ContentLiveEditPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.contentLiveEditPanel',

    requires: [
        'Admin.view.contentManager.wizard.ContentWizardToolbar',
        'Admin.view.contentManager.wizard.ContentWizardPanel'
    ],

    layout: {
        type: 'card'
    },

    header: false,

    border: 0,
    autoScroll: false,

    isLiveMode: false,

    defaults: {
        border: false
    },


    listeners: {
        afterrender: function () {
            this.setLiveMode(this.isLiveMode);
        }
    },

    initComponent: function () {

        this.tbar = Ext.createByAlias('widget.contentWizardToolbar', {
            isLiveMode: this.isLiveMode
        });
        var wizardPanel = {
            xtype: 'contentWizardPanel',
            data: this.data
        };

        var liveEdit = {
            flex: 1,
            itemId: 'livePreview',
            xtype: 'contentLive',
            border: false,
            hidden: true
        };

        this.items = [wizardPanel, liveEdit];

        this.callParent(arguments);
    },

    setLiveMode: function (mode) {
        this.getLayout().setActiveItem(mode ? 1 : 0);

        if (mode) {
            var livePreviewPanel = this.down('#livePreview');
            if (!livePreviewPanel.iFrameLoaded) {
                livePreviewPanel.load('/dev/live-edit/page/bootstrap.jsp', true);
            }
        }
    },

    toggleLive: function () {
        this.isLiveMode = !this.isLiveMode;

        this.setLiveMode(this.isLiveMode);
    }
});