package edu.umass.adp;

import au.com.bytecode.opencsv.CSVWriter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    final static String rootPath = "/Users/osmandin/Downloads/adp-source/source/"; //TODO externalize

    final static String projectFile = //TODO externalize
            "/Users/osmandin/IdeaProjects/adp/src/main/resources/links-shorter.csv"; // path to csv file


    public static void main(String args[]) throws Exception {

        // assign numbers to tokens
        final Map<Class, Double> tokenMapping = new TokenMapper().getNormalizedTypes();

        // read the source directory with project files

        final Map <PROMISE, Map> data = new ProjectReader().processProjects(projectFile);
        final Set<PROMISE> projects = data.keySet();

        int numRecords = 0; // total number of records in the dataset

        int unk = 0; //files whose status is not mentioned in the PROMISE dataset

        // Phase 1: Extract AST features from disk
        for (final PROMISE p: projects) {

            final List<double[]> astFeaturesForProject = new ArrayList<>();

            logger.info("Reading project bugs:{}", p.getProjectName());

            Map<String, String> fileBugStatus = data.get(p); // extracted from PROMISE repo. This should be added to the tokens as the last field

            logger.info("Reading project for token extraction:{}", p.getProjectName());

            // construct path to project TODO replace with path fully specified inside CSV
            final String project = rootPath + p.getSourcePath(); // looks for /tmp/java/camel-1.4 for example

            final File dir = new File(project);

            if (!dir.exists()) {
                logger.error("Project not found:{}. Specified path:{}", dir, p.getSourcePath());
                return;
            }

            logger.info("Reading directory:{}", dir.getCanonicalPath());

            final List<File> files = (List<File>) FileUtils.listFiles(dir, getJavaFiles(), TrueFileFilter.INSTANCE);

            final ASTFeatures ast = new ASTFeatures(tokenMapping);


            for (final File file : files) {
                // logger.info("Processing file:{}", file.getCanonicalPath());

                final FileInputStream in = new FileInputStream(file);

                final CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                } catch (Exception e) {
                    logger.error("Parsing exception", e); //TODO log these to file
                    continue;
                }

                final double[] featuresForFile = ast.walk(cu);
                logger.info("Extracted raw features from:{}", file.getCanonicalPath());


                final List<String> className = ast.className(cu);

                int label = 0;

                for (final String s : className) {
                    //logger.info("Extracted class name:{} from file:{}", s, file.getName());

                    // look up label

                    //logger.debug("Match:{}", fileBugStatus.get(s));

                    String status = fileBugStatus.get(s);

                    if (status == null) {
                        label = -1; // exclude for now
                        unk = unk + 1;
                    } else if (Integer.parseInt(status) == 0) {
                        label = 0;
                    } else {
                       // label = 1; // binary classification
                        label = Integer.parseInt(status);
                    } // TODO does this overwrite?
                }

                double[] featureswithlabel = new double[featuresForFile.length + 1];
                System.arraycopy( featuresForFile, 0, featureswithlabel, 0, featuresForFile.length);
                featureswithlabel[featureswithlabel.length - 1 ] = label; // labels are appended to the end

                if (label != -1) {
                    astFeaturesForProject.add(featureswithlabel);
                }
            }

            p.setRawFeatures(astFeaturesForProject); // assigns features to each project
        }

        // Phase 2: Normalize projects

        int max = 0;

        for (final PROMISE p : projects) {
            List<double[]> features = p.getRawFeatures();

            numRecords += features.size();

            for (double[] d : features) {
                if (d.length > max) {
                    max = d.length;
                }
            }
        }

        logger.info("Maximum length:{}", max);

        // pad with 0s // TODO replace with streaming (inefficent)

        int notBuggy = 0, buggy = 0;

        for (final PROMISE p : projects) {
            List<double[]> features = p.getRawFeatures();
            List<double[]> paddedFetures = new ArrayList<>();

            for (double[] f : features) {
                if (f.length <= max) {
                    double[] padded = new double[max];
                    System.arraycopy( f, 0, padded, 0, f.length-1); // don't copy the label
                    padded[max - 1] = f[f.length-1]; // copy the label to the end of the array

                    if (f[f.length - 1] != padded[max - 1]) {
                        logger.error("Failed confirmation check");
                        return;
                    }

                    // test skip if label is 0

                    if (padded[max - 1] == 0) {
                        notBuggy = notBuggy + 1;
                        paddedFetures.add(padded);
                    } else {
                        buggy = buggy + 1;
                        paddedFetures.add(padded);
                    }
                }
            }
            p.setPaddedFeatures(paddedFetures);
        }

        logger.debug("Not buggy:{}", notBuggy);
        logger.debug("buggy:{}", buggy);
        logger.debug("Num records:{}", numRecords);
        logger.debug("File not mentioned in PROMISE:{}", unk);

        // confirm that padding worked

        for (final PROMISE p : projects) {
            List<double[]> features = p.getPaddedFeatures();

            for (double[] d : features) {
                if (d.length < max) {
                    logger.error("Error processing. Found unpadded arrays:{}", d.length);
                    return;
                }
            }
        }

        // Phase 3: write AST features to file for later processing (i.e. semantic generation)

        for (final PROMISE p: projects) {

            final String astFile = "/tmp/" + p.getProjectName() + File.separator +  p.getVersion(); //"; //TODO

            File folder = createTempDirectory(astFile);

            CSVWriter csvWriter = new CSVWriter(new FileWriter(folder.getAbsolutePath() + "/astfile.csv"), ',','\0'); //TODO check

            for (final double[] feature : p.getPaddedFeatures()) {
                // transform features to strings so that they can be written by OpenCSV
                // DoubleStream doubleStream = Arrays.stream(feature);

                String[] featuresStr = new String[feature.length];

                for (int i = 0; i < feature.length; i++) {

                    if (i == feature.length - 1) { // for the label, we want to write 1.0 as 1
                        featuresStr[i] = String.valueOf(feature[i]).replace(".0", "");
                    }
                    else {
                        featuresStr[i] = String.valueOf(feature[i]);
                    }
                }

                //String[] featureStr = doubleStream.sorted().mapToObj(String::valueOf).toArray(String[]::new);
                csvWriter.writeNext(featuresStr);
            }

            logger.debug("Writing features to folder:{} for:{} for verison:{}", folder.getAbsolutePath(), p.getProjectName(), p.getVersion());
            csvWriter.close(); //TODO check


            // generate semantic features:

            SemanticFeatures semanticFeatures = new SemanticFeatures();
            String generated = semanticFeatures.generate(folder.getAbsolutePath() + "/astfile.csv", max - 1, numRecords); //currently not on the path


            final StringWriter s = new StringWriter();
            CSVWriter writer = new CSVWriter(s, '\t');
            // feed in your array (or convert your data to an array)
            writer.writeNext(generated.split(","));
            writer.close();
            String finalString = s.toString();
            final File file = new File(p.getProjectName() + "-" + p.getVersion() + "-" + "semantic_features.txt");
            FileUtils.writeStringToFile(file, finalString);
        }


    }

    /**
     * Ignore non Java files
     * @return whether it's a Java file
     */
    private static IOFileFilter getJavaFiles() {
        return new IOFileFilter() {
            public boolean accept(File file) {
                return file.getAbsolutePath().contains(".java");
            }

            public boolean accept(File file, String s) {
                return false;
            }
        };
    }

    public static File createTempDirectory(String name)
            throws IOException
    {
        final File temp;

        temp = File.createTempFile(name, Long.toString(System.nanoTime()));

        if(!(temp.delete()))
        {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if(!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }


}
