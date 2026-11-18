package et.cbe.banking;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TransferService {

    public boolean processTransfer(String fromAccount, String toAccount, double amount) {
        return amount > 0 && fromAccount != null && toAccount != null;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"cbe-retail-transfer-service\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("cbe-retail-transfer-service started on port 8080...");
    }
}
