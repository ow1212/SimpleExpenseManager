package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.database.Cursor;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "200730P.db";
    private static final String TABLE_NAME_ACC = "account";
    private static final String TABLE_NAME_TRANSACTION = "transactions";
    private static final int DEF_LIMIT = 0;

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+ TABLE_NAME_ACC +
                " (accountno TEXT PRIMARY KEY ,"+
                "bankname TEXT  ,"+
                "accountHolderName TEXT, "+
                "balance REAL"
                +")");
        sqLiteDatabase.execSQL("CREATE TABLE "+ TABLE_NAME_TRANSACTION +
                " (transaction_no INTEGER  PRIMARY KEY AUTOINCREMENT,"+
                "accountno TEXT  ,"+
                "date TEXT, "+
                "expenseType TEXT ,"+
                "amount REAL"
                +")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_ACC);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME_TRANSACTION);
        onCreate(sqLiteDatabase);
    }


    public void insertData(String table_name, ContentValues content){
        SQLiteDatabase db = this.getWritableDatabase();
        long result;
        try{
            result = db.insertOrThrow(table_name, null,content);
        }catch(Exception e){
            result = -1;
            System.out.println("Data Insert Error....");
        }

    }



    public Cursor getData(String table_name, String [] columns, String [][] conditions){
        return getDataWithLimit(table_name, columns, conditions, DEF_LIMIT);
    }

    public Cursor getDataWithLimit(String table_name, String [] columns, String [][] constraints,int limit){
        SQLiteDatabase db = this.getWritableDatabase();

        StringBuilder cols = new StringBuilder();
        if (columns.length != 0){
            for (String column : columns) {
                cols.append(column).append(" , ");
            }
            cols = new StringBuilder(cols.substring(0, cols.length() - 2));
        }
        StringBuilder constraint = new StringBuilder();
        String[] args = null;
        if(constraints.length != 0){
            args = new String[constraints.length];
            constraint.append(" WHERE ");
            for (int i = 0;i < constraints.length ;i++){
                if(constraints[i].length == 3) {
                    String[] temp = constraints[i];
                    constraint.append(temp[0]).append(" ").append(temp[1]).append(" ? AND ");
                    args[i] = temp[2];
                }
            }
            constraint = new StringBuilder(constraint.substring(0, constraint.length() - 4));
        }else{
            constraint = new StringBuilder();
        }
        String lim = "";
        if(limit != 0){
            lim = " LIMIT " + (limit);
        }

        String sql = "select "+cols+" from "+table_name+constraint+lim;
        return db.rawQuery(sql,args);
    }

    public boolean updateData(String table_name,ContentValues content, String[ ] condition){
        SQLiteDatabase db = this.getWritableDatabase();
        String cond = condition[0]+" "+condition[1]+" ? ";
        String[] args = {condition[2]};

        long output;
        try{
            output = db.update(table_name, content,cond,args);
        }catch (Exception e){
            output = -1;
        }

        return output != -1;
    }

    public Integer deleteData(String table_name, String column, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(table_name, column+" = ?", new String[] {id});
    }

    public void deleteTableContent(String table_name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+table_name);
    }
}
