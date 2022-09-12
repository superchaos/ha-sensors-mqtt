Ext.require(['Ext.data.*', 'Ext.grid.*']);

Ext.define('Dict', {
    extend: 'Ext.data.Model',
    fields: [{
        name: 'id',
        type: 'int',
        useNull: true
    }, 'dictType', 'dictKey', 'dictValue'],
    validations: [{
        type: 'length',
        field: 'dictType',
        min: 1
    }, {
        type: 'length',
        field: 'dictKey',
        min: 1
    }, {
        type: 'length',
        field: 'dictValue',
        min: 1
    }]
});

Ext.onReady(function(){
    // 初始化数据集
    let store = Ext.create('Ext.data.Store', {
        autoLoad: true,
        autoSync: true,
        model: 'Dict',
        proxy: {
            type: 'rest',
            api: {
                read : '/dict/view',
                create : '/dict/create',
                update: '/dict/update',
                destroy: '/dict/delete'
            },
            actionMethods : {
                create: "POST",
                read: "POST",
                update: "POST",
                destroy: "POST"
            },
            reader: {
                type: 'json',
                totalProperty: 'total',
                successProperty: 'success',
                idProperty: 'id',
                rootProperty: 'data',
                messageProperty: 'msg'
            },
            writer: {
                type: 'json',
                writeAllFields: true
            }
        },
        listeners: {
            write: function(store, operation) {
                let record = operation.getRecords()[0], name = Ext.String.capitalize(operation.action);
                console.log(Ext.String.format("{0} user: {1}", name, record.getId()));
                // 重载数据
                store.load();
            }
        }
    });

    let rowEditing = Ext.create('Ext.grid.plugin.RowEditing', {
        listeners: {
            cancelEdit: function(rowEditing, context) {
                if (context.record.phantom) {
                    store.remove(context.record);
                }
            }
        }
    });

    let grid = Ext.create('Ext.grid.Panel', {
        renderTo: document.body,
        plugins: [rowEditing],
        width: window.innerWidth - 10,
        height: window.innerHeight - 10,
        frame: true,
        store: store,
        columns: [{
            text: 'ID',
            width: 50,
            sortable: true,
            dataIndex: 'id',
            // 行编辑时还没有id
            renderer: function(v, meta, rec) {
                return rec.phantom ? '' : v;
            }
        }, {
            text: '字典类型',
            width: 150,
            flex: 1,
            sortable: true,
            dataIndex: 'dictType',
            field: {
                xtype: 'textfield'
            }
        }, {
            header: '字典文本',
            width: 150,
            sortable: true,
            dataIndex: 'dictKey',
            field: {
                xtype: 'textfield'
            }
        }, {
            text: '字典值',
            width: 150,
            sortable: true,
            dataIndex: 'dictValue',
            field: {
                xtype: 'textfield'
            }
        }],
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: '新增',
                iconCls: 'icon-add',
                handler: function() {
                    // empty record
                    let rec = new Dict();
                    rec.data.id = null;
                    store.insert(0, rec);
                    rowEditing.startEdit(rec, 0);
                }
            }, '-', {
                itemId: 'delete',
                text: '删除',
                iconCls: 'icon-delete',
                disabled: true,
                handler: function(){
                    let selection = grid.getView().getSelectionModel().getSelection()[0];
                    if (selection) {
                        store.remove(selection);
                    }
                }
            }]
        }]
    });
    // 选中后删除有效
    grid.getSelectionModel().on('selectionchange', function(selModel, selections){
        grid.down('#delete').setDisabled(selections.length === 0);
    });
});