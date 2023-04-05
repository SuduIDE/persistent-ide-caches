package com.github.SuduIDE.persistentidecaches.ccsearch;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CamelCaseIndexTest {

    @Test
    void getSymbolsFromString() {
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
}