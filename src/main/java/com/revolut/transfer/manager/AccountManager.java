package com.revolut.transfer.manager;

import com.revolut.transfer.exceptions.AccountNotFoundException;
import com.revolut.transfer.exceptions.BadParameterException;
import com.revolut.transfer.exceptions.InsufficientFundsException;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.AccountLockingSession;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.persistence.AccountRepository;
import com.revolut.transfer.persistence.TransferTransactionRepository;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.revolut.transfer.exceptions.AccountNotFoundException.ACCOUNT_ID;

/**
 * Manage money transfer between accounts.
 *
 * It must be used as a singleton.
 */
public class AccountManager {

    private final static Logger logger = Logger.getLogger(AccountManager.class);
    private static AccountManager instance = new AccountManager();
    private AccountLockManager accountLockManager = new AccountLockManager();
    private AccountRepository accountRepository = new AccountRepository();
    private TransferTransactionRepository transferTransactionRepository = new TransferTransactionRepository();

    public AccountManager() {
        initEntityManager();
    }


    public AccountManager(AccountLockManager accountLockManager, AccountRepository accountRepository, TransferTransactionRepository transferTransactionRepository) {
        this.accountLockManager = accountLockManager;
        this.accountRepository = accountRepository;
        this.transferTransactionRepository = transferTransactionRepository;
        initEntityManager();
    }

    public static AccountManager getInstance() {
        return instance;
    }

    /**
     * <p></p>Transfer the input amount from the account represented by originAccountId to the account represented by
     * destinationAccountId</p>
     *
     * <p>It's guaranteed that only one thread can work with an specific account. So code is race condition free.
     * To achieve that, it's mandatory that only one instance of AccountLockManager is running in all the system.</p>
     *
     * @param originAccountId id of the account to take the money from
     * @param destinationAccountId id of the account to send the money to
     * @param amount quantity of money to be transferred between the accounts
     *
     * @throws BadParameterException if the patameters are incorrect (i.e. null values,
     * same origin account and destination account or amount is less or equal to 0
     * @throws AccountNotFoundException if originAccountId or destinationAccountId are not ids of existing accounts
     * @throws InsufficientFundsException if origin account does not have at least the given amount to be transferred
     *
     */
    public void transfer(Long originAccountId, Long destinationAccountId, BigDecimal amount) {
        logger.info(String.format("Transfer called: originAccountId: %d, destinationAccountId: %d, amount: %s", originAccountId, destinationAccountId, amount));
        if (originAccountId == null) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "originAccountId")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "NOT_NULL");
        }
        if (destinationAccountId == null) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "destinationAccountId")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "NOT_NULL");
        }
        if (originAccountId.equals(destinationAccountId)) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "originAccountId, destinationAccountId")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "DIFFERENT");
        }
        if (amount == null) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "amount")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "NOT_NULL");
        }
        if (amount.compareTo(new BigDecimal(0)) < 0) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "amount")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "POSITIVE");
        }
        if (amount.compareTo(new BigDecimal(0)) == 0) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "amount")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "NOT_EQUAL_TO_ZERO");
        }
        AccountLockingSession accountLockingSession = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);

        try {
            accountLockingSession.lock();
            accountRepository.startTransaction();
            Account originAccount = accountRepository.findById(originAccountId);
            if (originAccount == null) {
                throw new AccountNotFoundException().withProperty(ACCOUNT_ID, originAccountId);
            }
            if (originAccount.getAmount().compareTo(amount) < 0) {
                throw new InsufficientFundsException()
                        .withProperties(
                                InsufficientFundsException.ACCOUNT_ID, originAccountId,
                                InsufficientFundsException.CURRENT_FUNDS, originAccount.getAmount());
            }
            Account destinationAccount = accountRepository.findById(destinationAccountId);
            if (destinationAccount == null) {
                throw new AccountNotFoundException().withProperty(ACCOUNT_ID, destinationAccountId);
            }
            logger.info("Transfer: All data is validated, performing transfer");
            originAccount.setAmount(originAccount.getAmount().subtract(amount));
            destinationAccount.setAmount(destinationAccount.getAmount().add(amount));
            TransferTransaction transferTransaction = new TransferTransaction();
            transferTransaction.setOriginAccount(originAccount);
            transferTransaction.setDestinationAccount(destinationAccount);
            transferTransaction.setAmount(amount);
            transferTransaction.setTime(LocalDateTime.now());
            transferTransactionRepository.save(transferTransaction);
            accountRepository.flush();
            accountRepository.commitTransaction();
            logger.info("Transfer finished successfully");
        } catch (Throwable e) {
            logger.error("Transfer rolled back", e);
            accountRepository.rollbackTransaction();
            throw e;
        } finally {
            accountLockManager.releaseLocks(originAccountId, destinationAccountId);
        }

    }

    /**
     * Returns the account identified by the id
     * @param id of the account to be retrieved.
     * @return Account identified by id
     */
    public Account getAccountById(Long id) {
        if (id == null) {
            throw new BadParameterException()
                    .withProperty(BadParameterException.PARAMETER_NAME, "accountId")
                    .withProperty(BadParameterException.EXPECTED_CONDITION, "NOT_NULL");
        }
        Account account = accountRepository.findById(id);
        if (account == null) {
            throw new AccountNotFoundException().withProperty(ACCOUNT_ID, id);
        }
        return account;
    }

    /**
     * Creates an account, this method has been created for testing purposes as the management of accounts is out
     * of scope of this manager
     *
     * @param account to be created
     * @return the created account with the db id
     */
    public Account createAccount(Account account) {
        accountRepository.startTransaction();
        try {
            account = accountRepository.save(account);
            accountRepository.commitTransaction();
            return account;
        } catch (Exception e) {
            accountRepository.rollbackTransaction();
            throw e;
        }

    }

    /**
     *
     * @return all the accounts known by the system
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Cleans the database. To be used on testing
     */
    public void cleanAll() {
        accountRepository.startTransaction();
        try {
            transferTransactionRepository.deleteAll();
            accountRepository.deleteAll();
            accountRepository.commitTransaction();
        } catch (Exception e) {
            accountRepository.rollbackTransaction();
            throw e;
        }
    }

    /**
     * To share the same entity manager (and same transactions), we create and set to all repositories here.
     * With dependency injection it should be use something like @ResourceContext at the level of the repositories
     */
    private void initEntityManager() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("revolut-ds");
        EntityManager entityManager = factory.createEntityManager();
        this.accountRepository.setEntityManager(entityManager);
        this.transferTransactionRepository.setEntityManager(entityManager);
    }
}
