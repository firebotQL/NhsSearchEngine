package filter;

import java.io.File;
import java.io.FileFilter;

public class JsonFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".json");
    }
}
