package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbols;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CamelCaseIndexTest {

    @Test
    void getSymbolsFromStringTest() {
        final var javaAClass = """
                public class MyBestClass {
                    public final int myCuteInt;
                    public void myBarMethod() {
                    }
                }
                """;
        Assertions.assertEquals(CamelCaseIndex.getSymbolsFromString(javaAClass), new Symbols(
                List.of("MyBestClass"),
                List.of("myCuteInt"),
                List.of("myBarMethod")
        ));
    }

    @Test
    void getSymbolsFromStringTestSomeRealFile() {
        final var javaAClass = """
                package info.kgeorgiy.ja.chulkov.walk;
                                
                                
                import java.io.Closeable;
                import java.io.IOException;
                import java.io.Writer;
                import java.nio.file.Path;
                import java.util.HexFormat;
                                
                public class HashResultsHandler implements Closeable {
                                
                    private static final String ERROR_HASH_HEX = "0".repeat(64);
                    private final Writer writer;
                                
                    public HashResultsHandler(final Writer writer) {
                        this.writer = writer;
                    }
                                
                    public void processSuccess(final Path file, final byte[] hash) throws IOException {
                        processResult(HexFormat.of().formatHex(hash), file.toString());
                    }
                                
                    public void processError(final String path) throws IOException {
                        processResult(ERROR_HASH_HEX, path);
                    }
                                
                    private void processResult(final String hexHash, final String path) throws IOException {
                        writer.write(hexHash + " " + path + System.lineSeparator());
                    }
                                
                    @Override
                    public void close() throws IOException {
                        writer.close();
                    }
                }
                                
                """;
        Assertions.assertEquals(CamelCaseIndex.getSymbolsFromString(javaAClass), new Symbols(
                List.of("HashResultsHandler"),
                List.of(/*"ERROR_HASH_HEX", */"writer"),
                List.of("processSuccess", "processError", "processResult", "close")
        ));
    }


    @Test
    void isCamelCaseTest() {
        Assertions.assertTrue(CamelCaseIndex.isCamelCase("Test"));
        Assertions.assertTrue(CamelCaseIndex.isCamelCase("True"));
        Assertions.assertTrue(CamelCaseIndex.isCamelCase("TestThisCamel"));
        Assertions.assertTrue(CamelCaseIndex.isCamelCase("CamelCaseSearch"));
        Assertions.assertTrue(CamelCaseIndex.isCamelCase("CamelCaseField"));
        Assertions.assertFalse(CamelCaseIndex.isCamelCase("NOT_CAMEL_CASE"));
        Assertions.assertFalse(CamelCaseIndex.isCamelCase("Bad$Symbol"));
    }

    @Test
    void getInterestTrigramsTest() {
        Assertions.assertEquals(CamelCaseIndex.getInterestTrigrams("ItCamelCase").stream().sorted().toList(),
                Stream.of(
                        "$It",
                        "$IC",
                        "ItC",
                        "ICa",
                        "ICC",
                        "tCa",
                        "tCC",
                        "Cam",
                        "CaC",
                        "CCa",
                        "ame",
                        "amC",
                        "aCa",
                        "mel",
                        "meC",
                        "mCa",
                        "elC",
                        "eCa",
                        "lCa",
                        "Cas",
                        "ase"
                ).map(String::getBytes).map(Trigram::new).sorted().toList());
    }

    @Test
    void getInterestTrigramsTest2() {
        Assertions.assertEquals(CamelCaseIndex.getInterestTrigrams("writer").stream().sorted().toList(),
                Stream.of(
                        "$wr",
                        "wri",
                        "rit",
                        "ite",
                        "ter"
                ).map(String::getBytes).map(Trigram::new).sorted().toList());

    }
}