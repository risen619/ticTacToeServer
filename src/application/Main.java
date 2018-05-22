package application;

import application.Server;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        new Server(55555);
        Thread thread = Thread.currentThread();
        synchronized (thread) {
            Thread.currentThread().wait();
        }
    }
}

