package utils.io;

import java.io.IOException;

public interface Storage {
    public void save(Object obj, String fileName) throws IOException;
    public <T> T load(String fileName, Class<T> clazz) throws IOException;
}
