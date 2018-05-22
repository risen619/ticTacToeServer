package application;

import application.GameManager;
import application.SocketInteractionInformation;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Server
{
    private GameManager gm = GameManager.getInstance();
    private ServerSocket sSocket;
    private ArrayList<SocketInteractionInformation> sockets = new ArrayList<>();
    private FileOutputStream logFile;
    private Thread acceptThread;

    public Server(int port)
    {
        this.acceptThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while (!sSocket.isClosed())
                    {
                        if (sockets.size() >= 2) continue;
                        
                        SocketInteractionInformation sii = new SocketInteractionInformation();
                        sii.setSocket(sSocket.accept());
                        String message = "New connection: " + sii.getSocket().getInetAddress() + "\r\n";
                        log(message);
                        sii.setOnSocketClosed(s -> onSocketClosed(s));
                        sii.startReading(b -> onSocketRead(b));
                        sockets.add(sii);
                        
                        message = "Number of connections: " + sockets.size() + "\r\n";
                        log(message);
                        log(gm.getStarted() + " " + sockets.size() + "\r\n");
                        
                        if (!gm.getStarted() && sockets.size() == 2)
                        {
	                        log("Enough players\r\n");
	                        gm.prepare();
	                        int i = 0;
	                        for (SocketInteractionInformation s2 : sockets)
	                            s2.write("poll:" + gm.getPlayer((int)i++).sign);
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            this.sSocket = new ServerSocket(port);
            this.acceptThread.start();
            this.logFile = new FileOutputStream("./server.log");
            this.log("Server started\r\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start()
    {
        gm.start();
        for (SocketInteractionInformation s : sockets) {
            s.write("start:" + gm.getTurn());
        }
        log("Game started\r\n");
    }

    private void log(String message)
    {
        System.out.print(message);
        try {
            logFile.write(message.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Void onSocketClosed(Socket s)
    {
        this.sockets = (ArrayList<SocketInteractionInformation>)sockets.stream()
        		.filter(v -> !v.getSocket().isClosed()).collect(Collectors.toList());
        String message = "Number of connections: " + this.sockets.size() + "\r\n";
        log(message);
        
        gm.reset();
        for (SocketInteractionInformation sock : this.sockets)
            sock.write("restart");
        return null;
    }

    private Void onSocketRead(byte[] bytes)
    {
        String s = new String(bytes).trim();
        String message = "Socket read: " + s + " -> ";
        if (s.equals("ready")) {
            log("Adds one ready player\r\n");
            gm.addReady();
            if (gm.playersReady())
                start();
        }
        else if(s.matches("^\\w\\d:(O|X)$") && gm.getStarted()) {
            message = String.valueOf(message) + "needs to update game";
            String[] parts = s.split(":");
            String sign = parts[1];
            int id = Integer.parseInt(String.valueOf(parts[0].charAt(parts[0].length() - 1)));
            if (gm.getTurn().equals(sign)) {
                gm.setCell(id, sign);
                String win = gm.anybodyWin();
                for (SocketInteractionInformation socket : sockets) {
                    if (win.isEmpty()) {
                        socket.write(s);
                        continue;
                    }
                    socket.write("restart:" + win);
                    gm.reset();
                }
                gm.toggleTurn();
            }
        }
        message = String.valueOf(message) + "\r\n";
        log(message);
        return null;
    }

}