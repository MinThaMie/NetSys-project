package com.nedap.university;

import com.nedap.university.communication.Client;
import com.nedap.university.communication.Pi;

public class Main {

    private Main() {}

    public static void main(String[] args) {
        String whatAmI = args[0];
        boolean isPi = whatAmI.equals("raspberry");

        if(isPi){
            Pi.init();
        } else {
            Client.init();
        }

        System.out.println("Stopped");
    }

}
