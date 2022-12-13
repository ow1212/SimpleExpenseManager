package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.*;
import android.database.Cursor;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

public class PersistentAccountDAO implements AccountDAO {
    private final DBHelper myDB;
    private static final String TABLE_NAME_ACC = "account";
    private static final String ACC_NUMBER = "accountno";
    private static final String ACC_BANK = "bankname";
    private static final String ACC_HOLDER = "accountHolderName";
    private static final String ACC_BAL = "balance";
    public PersistentAccountDAO(DBHelper datab){
        this.myDB = datab;
    }


    @Override
    public List<Account> getAccountsList() {
        Cursor output = this.myDB.getData(TABLE_NAME_ACC,new String[] {"*"}, new String[][] {});
        List<Account> accounts = new ArrayList<>();
        if(output.getCount() != 0) {
            while (output.moveToNext()) {
                String accNo = output.getString(0);
                String bank = output.getString(1);
                String accHolder = output.getString(2);
                double balance = output.getDouble(3);
                Account acc = new Account(accNo, bank, accHolder, balance);
                accounts.add(acc);
            }
        }
        output.close();
        return accounts;
    }

    @Override
    public List<String> getAccountNumbersList() {
        Cursor output = this.myDB.getData(TABLE_NAME_ACC,new String[] {"accountno"}, new String[][] {});
        List<String> accountNumbers = new ArrayList<>();
        if(output.getCount() != 0) {
            while (output.moveToNext()) {
                accountNumbers.add(output.getString(0));
            }
        }
        output.close();
        return accountNumbers;
    }

    @Override
    public void addAccount(Account acc) {
        ContentValues accContent = new ContentValues();
        accContent.put(ACC_NUMBER, acc.getAccountNo());
        accContent.put(ACC_BANK, acc.getBankName());
        accContent.put(ACC_HOLDER, acc.getAccountHolderName());
        accContent.put(ACC_BAL, acc.getBalance());
        this.myDB.insertData(TABLE_NAME_ACC, accContent);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        int output = this.myDB.deleteData("account","accountno",accountNo);
        if(output == 0){
            throw new InvalidAccountException("");
        }
    }

    @Override
    public Account getAccount(String accNo) throws InvalidAccountException {
        String[] constraint = {"accountno", "=",accNo};
        Cursor outputStr = this.myDB.getData(TABLE_NAME_ACC,new String[] {"*"}, new String[][] {constraint});
        if(outputStr.getCount() == 0){
            throw new InvalidAccountException("Invalid Account No.");
        }
        String acc_no = "";
        String bank = "";
        String acc_holder = "";
        double balance = 0;
        while(outputStr.moveToNext()){
            acc_no = outputStr.getString(outputStr.getColumnIndex(ACC_NUMBER));
            bank = outputStr.getString(outputStr.getColumnIndex(ACC_BANK));
            acc_holder = outputStr.getString(outputStr.getColumnIndex(ACC_HOLDER));
            balance = outputStr.getDouble(outputStr.getColumnIndex(ACC_BAL));
        }
        outputStr.close();
        return new Account(acc_no,bank,acc_holder,balance);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        double balance;
        double tot;
        try{
            Account acc = getAccount(accountNo);
            balance = acc.getBalance();
        }catch(Exception e){
            throw new InvalidAccountException("Invalid Account No.");
        }

        if (expenseType == ExpenseType.EXPENSE){
            if(balance < amount){
                throw new InvalidAccountException("Balance Insufficient");
            }
            tot = balance-amount;
        }else{
            tot = amount +balance;
        }
        String[] constraints = {"accountno","=",accountNo};
        ContentValues accContent = new ContentValues();
        accContent.put(ACC_BAL, tot);
        boolean output = this.myDB.updateData("account",accContent,constraints);
        if(!output){
            throw new InvalidAccountException("Invalid Account No.");
        }
    }
}