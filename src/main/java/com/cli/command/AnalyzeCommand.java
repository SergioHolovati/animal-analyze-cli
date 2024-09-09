package com.cli.command;

import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.cli.util.JsonUtils.loadWordTree;
import static org.apache.commons.lang3.StringUtils.join;

@Component
public class AnalyzeCommand {

    private static final String USAGE_MESSAGE = "Usage: java -jar cli.jar analyze --depth <n> \"{phrase}\"";
    private static final String INVALID_ARGS_MESSAGE = "Invalid arguments. " + USAGE_MESSAGE;

    public void analyze(String[] args) throws IOException {

        if (args.length < 4) {
            System.out.println(USAGE_MESSAGE);
            return;
        }

        boolean verbose = Arrays.asList(args).contains("--verbose");

        Optional<Integer> depthOpt = parseDepthArgument(args);
        Optional<String> phraseOpt = parsePhraseArgument(args);

        if (!depthOpt.isPresent() || !phraseOpt.isPresent()) {
            System.out.println(INVALID_ARGS_MESSAGE);
            return;
        }

        int depth = depthOpt.get();
        String phrase = phraseOpt.get();

        Instant startLoad = Instant.now();
        Map<String, Object> rootNode = loadWordTree("dicts/tree.json");
        Instant endLoad = Instant.now();

        Instant startAnalyze = Instant.now();
        Map<String, Long> result = analyzePhrase(rootNode, phrase, depth);
        Instant endAnalyze = Instant.now();

        displayResults(result, depth, verbose, startLoad, endLoad, startAnalyze, endAnalyze);
    }

    public Optional<Integer> parseDepthArgument(String[] args) {
        try {
            return Optional.of(Integer.parseInt(args[2]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Optional<String> parsePhraseArgument(String[] args) {
        try {
            return Optional.of(args[3].trim());
        } catch (ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Map<String, Long> analyzePhrase(Map<String, Object> rootNode, String phrase, int targetDepth) {
        String[] words = phrase.split("\\s+");
        Map<String, Long> result = new HashMap<>();
        traverseTree(rootNode, words, targetDepth, 0, result);
        return result;
    }

    public void traverseTree(Map<String, Object> node, String[] words, int targetDepth, int currentDepth,
                              Map<String, Long> result) {
        if (currentDepth == targetDepth - 1) {
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String parentKey = entry.getKey();
                Object subtree = entry.getValue();
                long count = countMatchesInSubtree(words, subtree);
                if (count > 0) {
                    result.merge(parentKey, count, Long::sum);
                }
            }
        } else if (currentDepth < targetDepth - 1) {
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    traverseTree((Map<String, Object>) value, words, targetDepth, currentDepth + 1, result);
                }
            }
        }
    }

    public long countMatchesInSubtree(String[] words, Object subtree) {
        if (subtree instanceof List) {
            List<?> list = (List<?>) subtree;
            return Arrays.stream(words)
                    .filter(word -> list.contains(WordUtils.capitalize(word)))
                    .count();
        } else if (subtree instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) subtree;
            return map.values().stream()
                    .mapToLong(child -> countMatchesInSubtree(words, child))
                    .sum();
        }
        return 0;
    }

    public void displayResults(Map<String, Long> result, int depth, boolean verbose,
                                Instant startLoad, Instant endLoad, Instant startAnalyze, Instant endAnalyze) {
        if (result.isEmpty()) {
            System.out.println(join("Na frase não existe nenhum filho do nível ", depth,
                    " e nem o nível ", depth, " possui os termos especificados."));
        } else {
            result.forEach((category, count) -> {
                String message = count > 1 ? " foram mencionados(as)" : " foi mencionado(o)";
                System.out.println(join(category, " = ", count, " ( ", count, " ", category, message, " )"));
            });
        }

        if (verbose) {
            System.out.println(join("Tempo de carregamento dos parâmetros: ",
                    Duration.between(startLoad, endLoad).toMillis(), "ms"));
            System.out.println(join("Tempo de verificação da frase: ",
                    Duration.between(startAnalyze, endAnalyze).toMillis(), "ms"));
        }
    }
}
