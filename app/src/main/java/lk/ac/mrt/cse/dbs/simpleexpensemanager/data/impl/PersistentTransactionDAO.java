package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private final DBHelper myDB;
    private static final String TABLE_NAME_TRANSACTION = "transactions";
    private static final String DATE_OF_TRANSACTION = "date";
    private static final String ACC_OF_TRANSACTION = "accountno";
    private static final String TRANSACTION_TYPE = "expenseType";
    private static final String TRANSACTION_AMOUNT = "amount";
    public PersistentTransactionDAO(DBHelper db){
        this.myDB = db;
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        if(expenseType == ExpenseType.EXPENSE){
            PersistentAccountDAO persistent_acc = new PersistentAccountDAO(this.myDB);
            try {
                Account user = persistent_acc.getAccount(accountNo);
                if(user.getBalance() < amount){
                    return;
                }
            }catch (Exception e){
                System.out.println("Invalid Account");
            }
        }
        String sDate = date.toString();
        ContentValues transaction_content = new ContentValues();
        transaction_content.put(ACC_OF_TRANSACTION, accountNo);
        transaction_content.put(DATE_OF_TRANSACTION, sDate);
        transaction_content.put(TRANSACTION_TYPE, getStringExpense(expenseType));
        transaction_content.put(TRANSACTION_AMOUNT, amount);
        this.myDB.insertData(TABLE_NAME_TRANSACTION, transaction_content);
    }

    public void deleteTransaction(String transactionNo) throws InvalidAccountException {
        int output = this.myDB.deleteData("transaction","transaction_no",transactionNo);
        if(output == 0){
            throw new InvalidAccountException("Invalid Transaction");
        }
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        Cursor output = this.myDB.getData(TABLE_NAME_TRANSACTION,new String[] {"*"},new String[][] {});
        List<Transaction> transactions = new ArrayList<>();
        if(output.getCount() != 0) {

            while (output.moveToNext()) {
                String dateStr = output.getString(output.getColumnIndex(DATE_OF_TRANSACTION));
                String accountNo = output.getString(output.getColumnIndex(ACC_OF_TRANSACTION));
                String expenseType = output.getString(output.getColumnIndex(TRANSACTION_TYPE));
                double amount = output.getDouble(output.getColumnIndex(TRANSACTION_AMOUNT));
                Date date = stringToDate(dateStr);

                Transaction transaction = new Transaction(date,accountNo,getExpense(expenseType),amount );
                transactions.add(transaction);
            }
        }
        output.close();
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        Cursor result = this.myDB.getDataWithLimit(TABLE_NAME_TRANSACTION,new String[] {"*"},new String[][] {},limit);
        List<Transaction> transactions = new ArrayList<>();
        if(result.getCount() != 0) {

            while (result.moveToNext()) {
                String dateS = result.getString(result.getColumnIndex(DATE_OF_TRANSACTION));
                String accountNo = result.getString(result.getColumnIndex(ACC_OF_TRANSACTION));
                String expenseType = result.getString(result.getColumnIndex(TRANSACTION_TYPE));
                double amount = result.getDouble(result.getColumnIndex(TRANSACTION_AMOUNT));
                Date date = stringToDate(dateS);
                Transaction transaction = new Transaction(date,accountNo,getExpense(expenseType),amount );
                transactions.add(transaction);
            }
        }
        result.close();
        return transactions;
    }

    public ExpenseType getExpense(String expense){
        if(expense.equals("Expense")){
            return ExpenseType.EXPENSE;
        }else{
            return ExpenseType.INCOME;
        }
    }

    public String getStringExpense(ExpenseType expense){
        if(expense == ExpenseType.EXPENSE){
            return "Expense";
        }else{
            return "Income";
        }
    }

    public Date stringToDate(String strDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        Date date = new Date();
        try{
            date = dateFormat.parse(strDate);
        }catch(Exception e){
            System.out.println(e);
        }
        return date;
    }

}
