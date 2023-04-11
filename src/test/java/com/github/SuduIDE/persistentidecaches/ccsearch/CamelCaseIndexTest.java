package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import java.util.Set;
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
                Set.of("MyBestClass"),
                Set.of("myCuteInt"),
                Set.of("myBarMethod")
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
}