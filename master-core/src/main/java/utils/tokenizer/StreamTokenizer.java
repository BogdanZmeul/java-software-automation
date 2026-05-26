package utils.tokenizer;

public interface StreamTokenizer {
    public void tokenize(String text, TokenConsumer consumer) throws Exception;
}