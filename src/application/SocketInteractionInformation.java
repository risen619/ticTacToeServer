package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.Function;

public class SocketInteractionInformation
{
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Function<Socket, Void> onSocketClosed = null;

    public Socket getSocket() { return socket; }

    public void setSocket(Socket s)
    {
        socket = s;
        try
        {
            input = s.getInputStream();
            output = s.getOutputStream();
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void setOnSocketClosed(Function<Socket, Void> f) { this.onSocketClosed = f; }

    public void close()
    {
        try
        {
            socket.close();
            if (onSocketClosed != null)
                onSocketClosed.apply(this.socket);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void write(String data)
    {
        try
        {
            output.write(data.getBytes(), 0, data.length());
            output.flush();
            System.out.println("Socket writes: " + data);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void startReading(final Function<byte[], Void> callback)
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!SocketInteractionInformation.this.socket.isClosed())
                {
                    try
                    {
                        byte[] buffer = new byte[128];
                        int result = SocketInteractionInformation.this.input.read(buffer, 0, 128);
                        if (result == -1) SocketInteractionInformation.this.close();
                        callback.apply(buffer);
                    }
                    catch (IOException e)
                    {
                    	if (!e.getMessage().equals("Connection reset")) break;
                        close();
                        break;
                    }
                }
            }
        });
        t.start();
    }
}