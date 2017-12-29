package edu.umass.adp;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class ASTFeatures {

    private Map<Class, Double> typeMap;


    ASTFeatures(Map<Class, Double> typeMap) {
        this.typeMap = typeMap;
    }

    /**
     * Walks a node
     * @param node the node to process, a compilation unit
     * @return Array of features
     */
    public double[] walk(final Node node) {
        final List<Double> features = new ArrayList<>();
        walkTree(node, features);
        double[] featuresInt = features.stream().mapToDouble(i->i).toArray();
        return featuresInt;
    }

    private void walkTree(final Node node, final List<Double> features) {

        for (final Class c: typeMap.keySet()) {

            if (node.getClass().equals(c)) {
                synchronized (this) {
                    features.add(typeMap.get(node.getClass()));
                }
            }
        }

        for (final Node child : node.getChildNodes()){
            walkTree(child, features);
        }
    }


    /**
     * Walks a node
     * @param node the node to process, a compilation unit
     * @return Array of features
     */
    public List<String> className(final Node node) {

        final PackageDetails packageDetails = new PackageDetails();

        for (final Node child : node.getChildNodes()){
            walk2(child, packageDetails);
        }

        final List<String> found = new ArrayList<>();

        for (String s: packageDetails.classes ) {
            found.add(packageDetails.packageName + "." + s);
        }

        return found;

    }

    private void walk2(final Node node, final PackageDetails packageDetails) {

        if (node.getClass().equals(PackageDeclaration.class)) {
            PackageDeclaration p = (PackageDeclaration) node;
            packageDetails.packageName = p.getNameAsString();
        }

        if (node.getClass().equals(ClassOrInterfaceDeclaration.class)) {

            ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) node;
            packageDetails.classes.add(c.getNameAsString());
        }

        for (final Node child : node.getChildNodes()){
            walk2(child, packageDetails);
        }
    }

    private class PackageDetails {
        String packageName = "";

        List<String> classes = new ArrayList<>();

        public PackageDetails() {
        }

        public PackageDetails(String packageName, List<String> classes) {
            this.packageName = packageName;
            this.classes = classes;
        }
    }



}
