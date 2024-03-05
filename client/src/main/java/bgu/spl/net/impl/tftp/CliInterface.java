package bgu.spl.net.impl.tftp;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CliInterface implements Runnable {

    private final SynchronousQueue<String> inputProcess;
    private final Scanner scan = new Scanner(System.in);

    public CliInterface(SynchronousQueue<String> inputProcess) {
        this.inputProcess = inputProcess;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String input = scan.nextLine();
                if (inputProcess.offer(input)) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            scan.close();
        }
    }
}
