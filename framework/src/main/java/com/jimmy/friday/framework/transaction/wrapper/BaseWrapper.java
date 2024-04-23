package com.jimmy.friday.framework.transaction.wrapper;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.framework.core.GlobalCache;
import com.jimmy.friday.framework.transaction.def.AffectedData;
import com.jimmy.friday.framework.transaction.def.StatementInfo;
import com.jimmy.friday.framework.transaction.def.TableStruct;
import com.jimmy.friday.framework.utils.CacheConstants;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseWrapper {

    protected GlobalCache globalCache;

    public BaseWrapper(GlobalCache globalCache) {
        this.globalCache = globalCache;
    }

    protected String onBeforeExecute(StatementInfo statementInfo) throws SQLException {
        String statementQuery = statementInfo.getStatementQuery();

        try {
            Statement statement = CCJSqlParserUtil.parse(statementQuery);

            if (statement instanceof Update) {
                //sqlExecuteInterceptor.preUpdate((Update) statement);
            } else if (statement instanceof Delete) {
                //sqlExecuteInterceptor.preDelete((Delete) statement);
            } else if (statement instanceof Insert) {
                //sqlExecuteInterceptor.preInsert((Insert) statement);
            } else if (statement instanceof Select) {
                //sqlExecuteInterceptor.preSelect(new LockableSelect((Select) statement));
            }
        } catch (JSQLParserException e) {
            throw new SQLException(e);
        }
        return statementQuery;

    }

    /**
     * @param statementInfo
     * @param update
     */
    private void prepareUpdate(StatementInfo statementInfo, Update update) {
        Connection connection = statementInfo.getConnection();

        Expression where = update.getWhere();
        ArrayList<UpdateSet> updateSets = update.getUpdateSets();

        UpdateSet updateSet = updateSets.get(0);
        ArrayList<Column> columns = updateSet.getColumns();
        ArrayList<Expression> expressions = updateSet.getExpressions();

        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            Expression expression = expressions.get(i);


        }
    }

    /**
     * 获取被影响的数据
     *
     * @param connection
     * @param sql
     * @return
     */
    private List<AffectedData> queryAffectedData(Connection connection, String sql) {
        return null;
    }

    /**
     * 获取表相关信息
     *
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     */
    private TableStruct getTableInfo(Connection connection, String table) throws SQLException {
        String catalog = connection.getCatalog();
        String tableInfoCacheKey = CacheConstants.TRANSACTION_TABLE_INFO + connection.getCatalog() + StrUtil.DOT + table;

        TableStruct tableStructInfo = globalCache.get(tableInfoCacheKey, TableStruct.class);
        if (tableStructInfo != null) {
            return tableStructInfo;
        }

        tableStructInfo = new TableStruct();
        tableStructInfo.setTableName(table);
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, null, table)) {
            while (resultSet.next()) {
                tableStructInfo.getPrimaryKeys().add(resultSet.getString("COLUMN_NAME"));
            }
        }

        globalCache.put(tableInfoCacheKey, tableInfoCacheKey);
        return tableStructInfo;
    }
}
