package utils.tokenizer;

@FunctionalInterface
public interface TokenConsumer {
   public void accept(String token) throws Exception;
}