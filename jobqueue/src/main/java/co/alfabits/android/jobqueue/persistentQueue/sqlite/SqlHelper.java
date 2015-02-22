package co.alfabits.android.jobqueue.persistentQueue.sqlite;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import co.alfabits.android.jobqueue.log.JqLog;

/**
 * Helper class for {@link SqliteJobQueue} to generate sql queries and statements.
 */
public class SqlHelper {

    //region Fields

    /**package**/ String FIND_BY_ID_QUERY;

    private SQLiteStatement insertStatement;
    private SQLiteStatement insertOrReplaceStatement;
    private SQLiteStatement deleteStatement;
    private SQLiteStatement onJobFetchedForRunningStatement;
    private SQLiteStatement countStatement;
    private SQLiteStatement nextJobDelayedUntilWithNetworkStatement;
    private SQLiteStatement nextJobDelayedUntilWithoutNetworkStatement;

    private final DbOpenHelper dbOpenHelper;
    private final String tableName;
    private final String primaryKeyColumnName;
    private final int columnCount;
    private final long sessionId;

    //endregion

    //region Constructors

    public SqlHelper(DbOpenHelper dbOpenHelper, String tableName, String primaryKeyColumnName, int columnCount, long sessionId) {
        this.dbOpenHelper = dbOpenHelper;
        this.tableName = tableName;
        this.columnCount = columnCount;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.sessionId = sessionId;

        FIND_BY_ID_QUERY = "SELECT * FROM " + tableName + " WHERE " + DbOpenHelper.ID_COLUMN.columnName + " = ?";
    }

    //endregion

    //region Methods

    public static String create(String tableName, Property primaryKey, Property... properties) {
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append(tableName).append(" (");
        builder.append(primaryKey.columnName).append(" ");
        builder.append(primaryKey.type);
        builder.append("  primary key autoincrement ");
        for (Property property : properties) {
            builder.append(", `").append(property.columnName).append("` ").append(property.type);
        }
        builder.append(" );");
        JqLog.d(builder.toString());
        return builder.toString();
    }

    public static String drop(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public SQLiteStatement getInsertStatement(SQLiteDatabase db) {
        SQLiteStatement result;

        StringBuilder builder = new StringBuilder("INSERT INTO ").append(tableName);
        builder.append(" VALUES (");

        for (int i = 0; i < columnCount; i++) {
            if (i != 0) {
                builder.append(",");
            }

            builder.append("?");
        }

        builder.append(")");
        result = db.compileStatement(builder.toString());

        return result;
    }

    public SQLiteStatement getCountStatement(SQLiteDatabase db) {
        SQLiteStatement result = db.compileStatement("SELECT COUNT(*) FROM " + tableName + " WHERE " +
                    DbOpenHelper.RUNNING_SESSION_ID_COLUMN.columnName + " != ?");

        return result;
    }

    public SQLiteStatement getInsertOrReplaceStatement(SQLiteDatabase db) {
        SQLiteStatement result;

        StringBuilder builder = new StringBuilder("INSERT OR REPLACE INTO ").append(tableName);
        builder.append(" VALUES (");

        for (int i = 0; i < columnCount; i++) {
            if (i != 0) {
                builder.append(",");
            }

            builder.append("?");
        }

        builder.append(")");
        result = db.compileStatement(builder.toString());

        return result;
    }

    public SQLiteStatement getDeleteStatement(SQLiteDatabase db) {
        SQLiteStatement result = db.compileStatement("DELETE FROM " + tableName + " WHERE " + primaryKeyColumnName + " = ?");

        return result;
    }

    public SQLiteStatement getOnJobFetchedForRunningStatement(SQLiteDatabase db) {
        SQLiteStatement result;

        String sql = "UPDATE " + tableName + " SET "
                + DbOpenHelper.RUN_COUNT_COLUMN.columnName + " = ? , "
                + DbOpenHelper.RUNNING_SESSION_ID_COLUMN.columnName + " = ? "
                + " WHERE " + primaryKeyColumnName + " = ? ";
        result = db.compileStatement(sql);

        return result;
    }

    public SQLiteStatement getNextJobDelayedUntilWithNetworkStatement(SQLiteDatabase db) {
        SQLiteStatement result;

        String sql = "SELECT " + DbOpenHelper.DELAY_UNTIL_NS_COLUMN.columnName
                + " FROM " + tableName + " WHERE "
                + DbOpenHelper.RUNNING_SESSION_ID_COLUMN.columnName + " != " + sessionId
                + " ORDER BY " + DbOpenHelper.DELAY_UNTIL_NS_COLUMN.columnName + " ASC"
                + " LIMIT 1";
        result = db.compileStatement(sql);

        return result;
    }

    public SQLiteStatement getNextJobDelayedUntilWithoutNetworkStatement(SQLiteDatabase db) {
        SQLiteStatement result;

        String sql = "SELECT " + DbOpenHelper.DELAY_UNTIL_NS_COLUMN.columnName
                + " FROM " + tableName + " WHERE "
                + DbOpenHelper.RUNNING_SESSION_ID_COLUMN.columnName + " != " + sessionId
                + " AND " + DbOpenHelper.REQUIRES_NETWORK_COLUMN.columnName + " != 1"
                + " ORDER BY " + DbOpenHelper.DELAY_UNTIL_NS_COLUMN.columnName + " ASC"
                + " LIMIT 1";
        result = db.compileStatement(sql);

        return result;
    }

    public String createSelect(String where, Integer limit, Order... orders) {
        StringBuilder builder = new StringBuilder("SELECT * FROM ");
        builder.append(tableName);

        if (where != null) {
            builder.append(" WHERE ").append(where);
        }

        boolean first = true;

        for (Order order : orders) {
            if (first) {
                builder.append(" ORDER BY ");
            }
            else {
                builder.append(",");
            }

            first = false;
            builder.append(order.property.columnName).append(" ").append(order.type);
        }

        if (limit != null) {
            builder.append(" LIMIT ").append(limit);
        }

        return builder.toString();
    }

    public void truncate(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + DbOpenHelper.JOB_HOLDER_TABLE_NAME);
        vacuum(db);
    }

    public void vacuum(SQLiteDatabase db) {
        db.execSQL("VACUUM");
    }

    public void resetDelayTimesTo(SQLiteDatabase db, long newDelayTime) {
        String query = String.format("UPDATE %s SET %s =?", DbOpenHelper.JOB_HOLDER_TABLE_NAME, DbOpenHelper.DELAY_UNTIL_NS_COLUMN.columnName);
        Object[] params = new Object[] { newDelayTime };

        db.execSQL(query, params);
    }

    //endregion

    //region Inner classes

    public static class Property {
        /*package*/ final String columnName;
        /*package*/ final String type;
        public final int columnIndex;

        public Property(String columnName, String type, int columnIndex) {
            this.columnName = columnName;
            this.type = type;
            this.columnIndex = columnIndex;
        }
    }

    public static class Order {
        final Property property;
        final Type type;

        public Order(Property property, Type type) {
            this.property = property;
            this.type = type;
        }

        public static enum Type {
            ASC,
            DESC
        }
    }

    //endregion
}
