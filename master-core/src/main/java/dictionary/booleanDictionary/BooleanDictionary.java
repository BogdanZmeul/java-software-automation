package dictionary.booleanDictionary;

import dictionary.Dictionary;

import java.io.Serializable;
import java.util.List;

public interface BooleanDictionary<T> extends Dictionary {
    public T getDocVector(String word);

    public T and(T a, T b);
    public T or(T a, T b);
    public T not(T a);

    public List<String> getDocuments(T bs);
}
