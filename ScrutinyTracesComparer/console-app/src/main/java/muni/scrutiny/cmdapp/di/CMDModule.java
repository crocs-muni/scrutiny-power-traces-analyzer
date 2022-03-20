package muni.scrutiny.cmdapp.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import muni.scrutiny.cmdapp.actions.*;
import muni.scrutiny.cmdapp.actions.base.Action;

public class CMDModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Action.class)
                .annotatedWith(Names.named(CreateReferenceProfileAction.name))
                .toInstance(new CreateReferenceProfileAction());
        bind(Action.class)
                .annotatedWith(Names.named(CompareProfilesAction.name))
                .toInstance(new CompareProfilesAction());
        bind(Action.class)
                .annotatedWith(Names.named(COTemplateFinderAction.name))
                .toInstance(new COTemplateFinderAction());
        bind(Action.class)
                .annotatedWith(Names.named(ConcatTracesAction.name))
                .toInstance(new ConcatTracesAction());
        bind(Action.class)
                .annotatedWith(Names.named(PeaksFinderAction.name))
                .toInstance(new PeaksFinderAction());
        bind(Action.class)
                .annotatedWith(Names.named(TraceClassifierAction.name))
                .toInstance(new TraceClassifierAction());
    }
}
