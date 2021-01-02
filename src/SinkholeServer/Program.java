package SinkholeServer;

public class Program {
    public static void main(String[] args) {
        SinkholeServer server = new SinkholeServer(5300, 20);
        server.Start();
    }
}
