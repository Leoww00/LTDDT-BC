package main;

import java.awt.Desktop;
import java.net.URI;
import java.net.BindException;

/** Chay trang web bang Java co ban. */
public class Main {
    public static void main(String[] args) {
        int startPort = 9090; // Cong mac dinh (khong dung 8080 de tranh bi trung)
        boolean openBrowser = true;
        for (String arg : args) {
            if (arg.equals("--no-browser")) openBrowser = false;
            else {
                try { startPort = Integer.parseInt(arg); }
                catch (NumberFormatException ignored) { }
            }
        }
        try {
            AppContext app = new AppContext();
            for (int port = startPort; port < startPort + 10; port++) {
                try {
                    WebServer server = new WebServer(app, port);
                    server.start();
                    String url = "http://localhost:" + port;
                    System.out.println("----------------------------------------------");
                    System.out.println("WEBSITE DA CHAY THANH CONG!");
                    System.out.println("Mo Chrome va truy cap: " + url);
                    System.out.println("Giu cua so nay mo de website hoat dong.");
                    System.out.println("----------------------------------------------");
                    if (openBrowser) {
                        try {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                                Desktop.getDesktop().browse(URI.create(url));
                        } catch (Exception ignored) {
                            // Van co the mo duong dan thu cong trong Chrome.
                        }
                    }
                    return;
                } catch (BindException e) {
                    System.out.println("Cong " + port + " dang duoc dung, thu cong khac...");
                }
            }
            System.out.println("Khong tim thay cong trong. Hay dong cac website Java cu va thu lai.");
        } catch (Exception e) {
            System.out.println("Loi khoi dong: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
