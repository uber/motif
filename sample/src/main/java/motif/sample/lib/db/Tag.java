package motif.sample.lib.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "tag")
public class Tag {

    @PrimaryKey
    @NonNull
    public final String name;

    public Tag(@NonNull String name) {
        this.name = name;
    }
}
