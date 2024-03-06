package bgu.spl.net.impl.tftp;

import bgu.spl.net.impl.tftp.packets.AbstractPacket;
import bgu.spl.net.impl.tftp.packets.PacketFactory;

import java.util.Scanner;

import static bgu.spl.net.impl.tftp.DisplayMessage.print;

public class CliInterface implements Runnable {

    private final ClientCoordinator coordinator;
    private final Scanner scan = new Scanner(System.in);

    public CliInterface(ClientCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String input = scan.nextLine();
                AbstractPacket packet = PacketFactory.createPacket(input);
                if(packet != null) {
                    if(packet.addSelf(coordinator));
                } else {
                    print("Operation is not supported.");
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
