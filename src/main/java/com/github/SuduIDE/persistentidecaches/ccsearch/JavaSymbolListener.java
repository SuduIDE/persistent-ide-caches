package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.grammars.java.JavaLexer;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.ClassDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.ConstDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.ConstantDeclaratorContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.EnumDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.FieldDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.InterfaceDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.InterfaceMethodDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.LocalTypeDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.LocalVariableDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.MethodDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.RecordDeclarationContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.VariableDeclaratorContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParser.VariableDeclaratorIdContext;
import com.github.SuduIDE.persistentidecaches.grammars.java.JavaParserBaseListener;
import com.github.SuduIDE.persistentidecaches.symbols.Symbols;
import java.util.ArrayList;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class JavaSymbolListener extends JavaParserBaseListener {

    Symbols symbols = new Symbols(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    public static Symbols getSymbolsFromString(final String javaFile) {
        final JavaLexer lexer = new JavaLexer(CharStreams.fromString(javaFile));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final JavaParser parser = new JavaParser(tokens);
        final ParseTree tree = parser.compilationUnit();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final JavaSymbolListener listener = new JavaSymbolListener();
        walker.walk(listener, tree);
        return listener.symbols;
    }

    @Override
    public void enterClassDeclaration(final ClassDeclarationContext ctx) {
        symbols.classOrInterfaceSymbols().add(ctx.identifier().getText());
    }

    @Override
    public void enterEnumDeclaration(final EnumDeclarationContext ctx) {
        symbols.classOrInterfaceSymbols().add(ctx.identifier().getText());
    }

    @Override
    public void enterInterfaceDeclaration(final InterfaceDeclarationContext ctx) {
        symbols.classOrInterfaceSymbols().add(ctx.identifier().getText());
    }

    @Override
    public void enterFieldDeclaration(final FieldDeclarationContext ctx) {
        ctx.variableDeclarators().variableDeclarator().stream()
                .map(VariableDeclaratorContext::variableDeclaratorId)
                .map(VariableDeclaratorIdContext::identifier)
                .map(RuleContext::getText)
                .forEach(symbols.fieldSymbols()::add);
    }

    @Override
    public void enterConstDeclaration(final ConstDeclarationContext ctx) {
        symbols.fieldSymbols().addAll(
                ctx.constantDeclarator().stream()
                        .map(ConstantDeclaratorContext::identifier)
                        .map(RuleContext::getText)
                        .toList()
        );
    }

    @Override
    public void enterInterfaceMethodDeclaration(final InterfaceMethodDeclarationContext ctx) {
        symbols.methodSymbols().add(ctx.interfaceCommonBodyDeclaration().identifier().getText());
    }

    @Override
    public void enterRecordDeclaration(final RecordDeclarationContext ctx) {
        symbols.classOrInterfaceSymbols().add(ctx.identifier().getText());
    }

    @Override
    public void enterMethodDeclaration(final MethodDeclarationContext ctx) {
        symbols.methodSymbols().add(ctx.identifier().getText());
    }

    // we need it?
    @Override
    public void enterLocalVariableDeclaration(final LocalVariableDeclarationContext ctx) {
        super.enterLocalVariableDeclaration(ctx);
    }

    @Override
    public void enterLocalTypeDeclaration(final LocalTypeDeclarationContext ctx) {
        super.enterLocalTypeDeclaration(ctx);
    }
}
