package Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet {

    private String walletId;
    private String accountId;
    private BigDecimal balance;
    private String walletStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Wallet() {
    }

    public Wallet(String walletId, String accountId, BigDecimal balance,
            String walletStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.walletId = walletId;
        this.accountId = accountId;
        this.balance = balance;
        this.walletStatus = walletStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getWalletStatus() {
        return walletStatus;
    }

    public void setWalletStatus(String walletStatus) {
        this.walletStatus = walletStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
