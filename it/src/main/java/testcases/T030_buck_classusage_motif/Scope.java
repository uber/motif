package testcases.T030_buck_classusage_motif;

import external.T030_buck_classusage_motif.Child;

@motif.Scope
public interface Scope {

    Child child();

    @motif.Dependencies
    interface Dependencies {}
}
