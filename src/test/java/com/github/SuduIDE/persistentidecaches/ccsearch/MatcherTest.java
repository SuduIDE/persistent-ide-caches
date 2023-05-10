package com.github.SuduIDE.persistentidecaches.ccsearch;

import static com.github.SuduIDE.persistentidecaches.ccsearch.Matcher.NEG_INF;
import static com.github.SuduIDE.persistentidecaches.ccsearch.Matcher.match;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MatcherTest {

    public void assertMatches(final String pattern, final String symbol) {
        final int res = match(pattern, symbol);
        System.out.println(pattern + " " + symbol + " " + res);
        Assertions.assertTrue(res > NEG_INF);
    }

    public void assertDoesntMatch(final String pattern, final String symbol) {
        final int res = match(pattern, symbol);
        System.out.println(pattern + " " + symbol + " " + res);
        Assertions.assertTrue(res <= NEG_INF);
    }

    public void assertPreference(final String pattern1, final String pattern2, final String symbol) {
        final int res1 = match(pattern1, symbol);
        final int res2 = match(pattern2, symbol);
        System.out.println(pattern1 + " " + pattern2 + " " + symbol + " " + res1 + " " + res2);
        assertTrue(res1 >= res2);
    }

    public void assertNoPreference(final String pattern1, final String pattern2, final String symbol) {
        final int res1 = match(pattern1, symbol);
        final int res2 = match(pattern2, symbol);
        System.out.println(pattern1 + " " + pattern2 + " " + symbol + " " + res1 + " " + res2);
        assertEquals(res1, res2);
    }

    @Test
    public void testSimpleCases() {
        assertMatches("N", "NameUtilTest");
        assertMatches("NU", "NameUtilTest");
        assertMatches("NUT", "NameUtilTest");
        assertMatches("NaUT", "NameUtilTest");
        assertDoesntMatch("NeUT", "NameUtilTest");
        assertDoesntMatch("NaUTa", "NameUtilTest");
        assertMatches("NaUtT", "NameUtilTest");
        assertMatches("NaUtT", "NameUtilTest");
        assertMatches("NaUtTe", "NameUtilTest");
        assertMatches("AACl", "AAClass");
        assertMatches("ZZZ", "ZZZZZZZZZZ");
    }

    @Test
    public void testEmptyPrefix() {
        assertMatches("", "");
        assertMatches("", "asdfs");
    }

    @Test
    public void testSkipWords() {
        assertMatches("nt", "NameUtilTest");
//		assertMatches("repl map", "ReplacePathToMacroMap");
        assertMatches("replmap", "ReplacePathToMacroMap");
        assertMatches("CertificateEx", "CertificateEncodingException");
//		assertDoesntMatch("ABCD", "AbstractButton.DISABLED_ICON_CHANGED_PROPERTY");

        assertMatches("templipa", "template_impl_template_list_panel");
        assertMatches("templistpa", "template_impl_template_list_panel");
    }

    @Test
    public void testSimpleCasesWithFirstLowercased() {
        assertMatches("N", "nameUtilTest");
        assertDoesntMatch("N", "anameUtilTest");
        assertMatches("NU", "nameUtilTest");
        assertDoesntMatch("NU", "anameUtilTest");
        assertMatches("NUT", "nameUtilTest");
        assertMatches("NaUT", "nameUtilTest");
        assertDoesntMatch("NeUT", "nameUtilTest");
        assertDoesntMatch("NaUTa", "nameUtilTest");
        assertMatches("NaUtT", "nameUtilTest");
        assertMatches("NaUtT", "nameUtilTest");
        assertMatches("NaUtTe", "nameUtilTest");
    }

    @Test
    public void testUnderscoreStyle() {
//		assertMatches("N_U_T", "NAME_UTIL_TEST");
        assertMatches("NUT", "NAME_UTIL_TEST");
        assertDoesntMatch("NUT", "NameutilTest");
    }

    @Test
    public void testAllUppercase() {
        assertMatches("NOS", "NetOutputStream");
    }

    @Test
    public void testLowerCaseWords() {
        assertMatches("uct", "unit_controller_test");
        assertMatches("unictest", "unit_controller_test");
        assertMatches("uc", "unit_controller_test");
        assertDoesntMatch("nc", "unit_controller_test");
        assertDoesntMatch("utc", "unit_controller_test");
    }

    @Test
    public void testPreferCamelHumpsToAllUppers() {
        assertPreference("ProVi", "PROVIDER", "ProjectView");
    }

    @Test
    public void testLong() {
        assertMatches("Product.findByDateAndNameGreaterThanEqualsAndQualityGreaterThanEqual",
                "Product.findByDateAndNameGreaterThanEqualsAndQualityGreaterThanEqualsIntellijIdeaRulezzz");
    }

    @Test
    public void testUpperCaseMatchesLowerCase() {
        assertMatches("ABC_B.C", "abc_b.c");
    }

    @Test
    public void testLowerCaseHumps() {
        assertMatches("foo", "foo");
        assertDoesntMatch("foo", "fxoo");
        assertMatches("foo", "fOo");
        assertMatches("foo", "fxOo");
        assertMatches("foo", "fXOo");
        assertMatches("fOo", "foo");
//	    assertDoesntMatch("fOo", "FaOaOaXXXX");
        assertMatches("ncdfoe", "NoClassDefFoundException");
        assertMatches("fob", "FOO_BAR");
        assertMatches("fo_b", "FOO_BAR");
        assertMatches("fob", "FOO BAR");
        assertMatches("fo b", "FOO BAR");
        assertMatches("AACl", "AAClass");
        assertMatches("ZZZ", "ZZZZZZZZZZ");
        assertMatches("em", "emptyList");
        assertMatches("bui", "BuildConfig.groovy");
        assertMatches("buico", "BuildConfig.groovy");
//	    assertMatches("buico.gr", "BuildConfig.groovy");
//	    assertMatches("bui.gr", "BuildConfig.groovy");
//	    assertMatches("*fz", "azzzfzzz");

        assertMatches("WebLogic", "Weblogic");
        assertMatches("WebLOgic", "WebLogic");
        assertMatches("WEbLogic", "WebLogic");
        assertDoesntMatch("WebLogic", "Webologic");

        assertMatches("Wlo", "WebLogic");
    }

    @Test
    public void testDigits() {
        assertMatches("foba4", "FooBar4");
        assertMatches("foba", "Foo4Bar");
//		assertMatches("*TEST-* ", "TEST-001");
//		assertMatches("*TEST-0* ", "TEST-001");
//		assertMatches("*v2 ", "VARCHAR2");
        assertMatches("smart8co", "SmartType18CompletionTest");
        assertMatches("smart8co", "smart18completion");
    }

    @Test
    public void testMatchingDegree() {
        assertPreference("jscote", "JsfCompletionTest", "JSCompletionTest");
        assertPreference("OCO", "OneCoolObject", "OCObject");
        assertPreference("MUp", "MavenUmlProvider", "MarkUp");
        assertPreference("MUP", "MarkUp", "MavenUmlProvider");
        assertPreference("CertificateExce", "CertificateEncodingException", "CertificateException");
        assertPreference("boo", "Boolean", "boolean");
        assertPreference("Boo", "boolean", "Boolean");
        assertPreference("getCU", "getCurrentSomething", "getCurrentUser");
        assertPreference("cL", "class", "coreLoader");
        assertPreference("cL", "class", "classLoader");
        assertPreference("inse", "InstrumentationError", "intSet");
        assertPreference("String", "STRING", "String");
    }

    @Test
    public void testPreferNoWordSkipping() {
        assertPreference("CBP", "CustomProcessBP", "ComputationBatchProcess");
    }

    @Test
    public void testMatchStartDoesntMatterForDegree() {
        assertNoPreference(" path", "getAbsolutePath", "findPath");
    }

    @Test
    public void testCapsMayMatchNonCaps() {
        assertMatches("PDFRe", "PdfRenderer");
    }

    @Test
    public void testACapitalAfterAnotherCapitalMayMatchALowercaseLetterBecauseShiftWasAccidentallyHeldTooLong() {
        assertMatches("USerDefa", "UserDefaults");
        assertMatches("NSUSerDefa", "NSUserDefaults");
        assertMatches("NSUSER", "NSUserDefaults");
        assertMatches("NSUSD", "NSUserDefaults");
        assertMatches("NSUserDEF", "NSUserDefaults");
    }

    @Test
    public void testCamelHumpWinsOverConsecutiveCaseMismatch() {
        assertPreference("GEN", "GetName", "GetExtendedName");
    }

    @Test
    public void testLowerCaseAfterCamels() {
        assertMatches("LSTMa", "LineStatusTrackerManager");
    }
}