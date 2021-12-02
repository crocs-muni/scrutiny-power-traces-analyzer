package muni.scrutiny.cmdapp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import muni.scrutiny.cmdapp.actions.base.Action;
import muni.scrutiny.cmdapp.di.CMDModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Process " + args[0] + " started at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            Injector injector = Guice.createInjector(new CMDModule());
            if (args.length > 0) {
                Action action = injector.getInstance(Key.get(Action.class, Names.named(args[0])));
                action.executeAction(args);
            }

            System.out.println("Process finished at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception ex) {
            System.out.println("Process failed at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            ex.printStackTrace(System.out);
        }
    }
}
