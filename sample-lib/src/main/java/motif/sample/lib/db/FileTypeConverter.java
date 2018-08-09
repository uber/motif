package motif.sample.lib.db;

import android.arch.persistence.room.TypeConverter;

import java.io.File;

public class FileTypeConverter {

    @TypeConverter
    public static File toFile(String value) {
        return value == null ? null : new File(value);
    }

    @TypeConverter
    public static String toString(File value) {
        return value == null ? null : value.getAbsolutePath();
    }
}