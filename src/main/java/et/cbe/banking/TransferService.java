package et.cbe.banking;

public class TransferService {
    public boolean processTransfer(String fromAccount, String toAccount, double amount) {
        if (amount <= 0 || fromAccount == null || toAccount == null) {
            return false;
        }
        return true;
    }
}
