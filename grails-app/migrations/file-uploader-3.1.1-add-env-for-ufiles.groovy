databaseChangeLog = {

    changeSet(author: 'Ankit Agrawal', id: '20180309-1') {
        addColumn(tableName: 'ufile') {
            column(name: 'env_name', type: 'VARCHAR')
        }
    }

    changeSet(author: 'Ankit Agrawal', id: '20180309-2') {
        grailsChange {
            change {
                sql.execute('update ufile set env_name = \'production\'')
                sql.execute('ALTER TABLE ufile ALTER COLUMN env_name SET NOT NULL')
            }
        }
    }
}