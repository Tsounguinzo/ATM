package com.mybank.atm.keypad;

import java.util.Scanner;

public class ConsoleKeypad implements IKeypad{
    private Scanner scanner;

    @Override
    public String getInput() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        return scanner.nextLine();
    }
}
