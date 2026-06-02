package booleanDictionary;

import java.util.List;

public interface BooleanDictionary<T> {
    public T getDocVector(String word);

    public T and(T a, T b);
    public T or(T a, T b);
    public T not(T a);

    public List<String> getDocuments(T bs);
}
