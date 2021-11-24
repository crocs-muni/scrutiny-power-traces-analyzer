package muni.scrutiny.cmdapp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import muni.scrutiny.cmdapp.actions.base.Action;
import muni.scrutiny.cmdapp.di.CMDModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new CMDModule());
            if (args.length >= 2) {
                Action action = injector.getInstance(Key.get(Action.class, Names.named(args[1])));
                action.executeAction(args);
            }

            System.out.println("OK");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
