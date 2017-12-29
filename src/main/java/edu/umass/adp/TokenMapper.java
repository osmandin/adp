package edu.umass.adp;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps type of token to an (arbitrary) integer
 *
 */
public class TokenMapper {

    public  Map<Class, Double> getNormalizedTypes() {
        final Map<Class, Double> m = new HashMap<Class, Double>();

        List<Class> list = new ArrayList<>();

        list.add((AnnotationDeclaration.class));
        list.add((AnnotationMemberDeclaration.class));
        list.add((BodyDeclaration.class));
        list.add((CallableDeclaration.class));
        list.add((ClassOrInterfaceDeclaration.class));
        list.add((ConstructorDeclaration.class));
        list.add((EnumConstantDeclaration.class));
        list.add((FieldDeclaration.class));
        list.add((InitializerDeclaration.class));
        list.add((MethodDeclaration.class));
        list.add((Parameter.class));
        list.add((ReceiverParameter.class));
        list.add((TypeDeclaration.class));
        list.add((VariableDeclarator.class));

        // com.github.javaparser.ast.stmt

        list.add((AssertStmt.class));
        list.add((BlockStmt.class));
        list.add((BreakStmt.class));
        list.add((CatchClause.class));
        list.add((ContinueStmt.class));
        list.add((DoStmt.class));
        list.add((EmptyStmt.class));
        list.add((ExplicitConstructorInvocationStmt.class));
        list.add((ExpressionStmt.class));
        list.add((ForeachStmt.class));
        list.add((ForStmt.class));
        list.add((IfStmt.class));
        list.add((LabeledStmt.class));
        list.add((LocalClassDeclarationStmt.class));
        list.add((ReturnStmt.class));
        list.add((Statement.class));
        list.add((SwitchEntryStmt.class));
        // list.add((SwitchStatement.class, 32));
        // list.add((SynchronizedStatement.class, 33));
        list.add((ThrowStmt.class));
        // list.add((TryStatement.class, 35));
        list.add((UnparsableStmt.class));
        // list.add((WhileStatement.class, 37));


        // others

        list.add((ImportDeclaration.class));
        list.add((PackageDeclaration.class));
  
        double j = 0.2;

        for (Class c: list) {
            m.put(c, j);
            j = j + 0.02;
        }

        return m;
    }


    private static Map<Class, Integer> getTypes() {
        final Map<Class, Integer> m = new HashMap<Class, Integer>();

        // com.github.javaparser.ast.body
        m.put(AnnotationDeclaration.class, 1);
        m.put(AnnotationMemberDeclaration.class, 2);
        m.put(BodyDeclaration.class, 3);
        m.put(CallableDeclaration.class, 4);
        m.put(ClassOrInterfaceDeclaration.class, 5);
        m.put(ConstructorDeclaration.class, 6);
        m.put(EnumConstantDeclaration.class, 7);
        m.put(FieldDeclaration.class, 8);
        m.put(InitializerDeclaration.class, 9);
        m.put(MethodDeclaration.class, 10);
        m.put(Parameter.class, 11);
        m.put(ReceiverParameter.class, 12);
        m.put(TypeDeclaration.class, 13);
        m.put(VariableDeclarator.class, 14);

        // com.github.javaparser.ast.stmt

        m.put(AssertStmt.class, 15);
        m.put(BlockStmt.class, 16);
        m.put(BreakStmt.class, 17);
        m.put(CatchClause.class, 18);
        m.put(ContinueStmt.class, 19);
        m.put(DoStmt.class, 20);
        m.put(EmptyStmt.class, 21);
        m.put(ExplicitConstructorInvocationStmt.class, 22);
        m.put(ExpressionStmt.class, 23);
        m.put(ForeachStmt.class, 24);
        m.put(ForStmt.class, 25);
        m.put(IfStmt.class, 26);
        m.put(LabeledStmt.class, 27);
        m.put(LocalClassDeclarationStmt.class, 28);
        m.put(ReturnStmt.class, 29);
        m.put(Statement.class, 30);
        m.put(SwitchEntryStmt.class, 31);
       // m.put(SwitchStatement.class, 32);
       // m.put(SynchronizedStatement.class, 33);
        m.put(ThrowStmt.class, 34);
       // m.put(TryStatement.class, 35);
        m.put(UnparsableStmt.class, 36);
       // m.put(WhileStatement.class, 37);


        // others

        m.put(ImportDeclaration.class, 38);
        m.put(PackageDeclaration.class, 39);

        return m;
    }
}
