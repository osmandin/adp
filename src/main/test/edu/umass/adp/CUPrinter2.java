/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2016 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package edu.umass.adp;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple test example
 *
 * See: https://stackoverflow.com/questions/32178349/parse-attributes-from-java-files-using-java-parser
 */
public class CUPrinter2 {

    private static Map<Class, Integer> typeMap = new HashMap<Class, Integer>();

    private static List<Integer> features = new ArrayList<Integer>();

    public static void main(String[] args) throws Exception {

        //prepare the vector

        typeMap.put(TypeDeclaration.class, 1);
        typeMap.put(FieldDeclaration.class, 2);
        typeMap.put(MethodDeclaration.class, 3);
        typeMap.put(IfStmt.class, 4);
        typeMap.put(ForStmt.class, 5);
        typeMap.put(ImportDeclaration.class, 6);
        typeMap.put(VariableDeclarator.class, 7);

        // read the file
        FileInputStream in = new FileInputStream("/tmp/test.java");

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // process each node recursively
        processNode(cu);

        //System.out.println(cu.toString());

        System.out.println("Features:" + features);
    }

    static void processNode(Node node) {

        System.out.println("Class:" + node.getClass());

        if (node instanceof TypeDeclaration) {
            features.add(typeMap.get(TypeDeclaration.class));
        } else if (node instanceof MethodDeclaration) {
            features.add(typeMap.get(MethodDeclaration.class));
        } else if (node instanceof FieldDeclaration) {
            features.add(typeMap.get(FieldDeclaration.class));
        } else if (node instanceof IfStmt) {
            features.add(typeMap.get(IfStmt.class));
        } else if (node instanceof ForStmt) {
            features.add(typeMap.get(ForStmt.class));
        } else if (node instanceof ImportDeclaration) {
            features.add(typeMap.get(ImportDeclaration.class));
        } else if (node instanceof VariableDeclarator) {
            features.add(typeMap.get(VariableDeclarator.class));
        } else {
            System.out.println("UNK: " + node.getClass() + " Value: " + node.toString());
        }

        for (Node child : node.getChildNodes()){
            processNode(child);
        }
    }

}
