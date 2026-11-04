package et.cbe.banking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransferServiceTest {
    @Test
    void testValidTransfer() {
        TransferService service = new TransferService();
        assertTrue(service.processTransfer("1000123456", "1000654321", 250.00));
    }
}
