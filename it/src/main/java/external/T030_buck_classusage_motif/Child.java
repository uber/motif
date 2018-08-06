package external.T030_buck_classusage_motif;

import motif.Scope;

@Scope
public interface Child {

    @motif.Objects
    abstract class Objects {

        abstract A a();
    }
}
