package dictionary;

import java.io.Serializable;

public interface Dictionary extends Serializable {
    public void addWord(String word, int docId);
    public int addDocument (String word);
}
