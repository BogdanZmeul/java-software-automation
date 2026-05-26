package dictionary.wildcardDictionary;

import dictionary.Dictionary;

import java.util.List;

public interface WildcardDictionary extends Dictionary {
    public List<String> findMatchingTerms(String wildcardQuery);
    public List<Integer> getPostings(String term);
    public String getDocumentName(int docId);
}
