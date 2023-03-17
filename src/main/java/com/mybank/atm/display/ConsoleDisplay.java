package com.mybank.atm.display;

import java.util.Formatter;

public class ConsoleDisplay implements IDisplay{
    @Override
    public void show(String message) {
        System.out.print(message);
    }

    @Override
    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
