package edu.umass.adp;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ClassPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.springframework.util.ClassUtils;

public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    final static String rootPath = "/Users/osmandin/Downloads/adp-source/source/"; //TODO

    public static void main(String args[]) throws Exception {

        // assign numbers to tokens
        final Map<Class, Double> tokenMapping = new TokenMapper().getNormalizedTypes();

        // read the source directory with project files

        final Map <PROMISE, Map> data = new ProjectReader().processProjects("/Users/osmandin/IdeaProjects/adp/src/main/resources/links.csv"); //TODO

        final Set<PROMISE> projects = data.keySet();

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
                logger.info("Processing file:{}", file.getCanonicalPath());

                //final String filePath = extractPath(file.getCanonicalPath(), p.getSourcePath());

                //String extractedName = fileBugStatus.get(ClassUtils.convertResourcePathToClassName(filePath));

                //logger.info("Extracted class name:{}", extractedName);

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

                // extract package name to construct the full class name
                final CompilationUnit cu2;
                try {
                    cu2 = JavaParser.parse(in);
                } catch (Exception e) {
                    logger.error("Parsing exception", e); //TODO log these to file
                    continue;
                }

                final List<String> className = ast.className(cu);

                for (String s : className) {
                    logger.info("Extracted class name:{} from file:{}", className, file.getName());
                }

                astFeaturesForProject.add(featuresForFile);
            }

            p.setRawFeatures(astFeaturesForProject); // assigns features to each project
        }

        // Phase 2: Normalize projects

        int max = 0;

        for (final PROMISE p : projects) {
            List<double[]> features = p.getRawFeatures();

            for (double[] d : features) {
                if (d.length > max) {
                    max = d.length;
                }
            }
        }

        logger.info("Maximum length:{}", max);

        // pad with 0s // TODO replace with streaming (inefficent)

        for (final PROMISE p : projects) {
            List<double[]> features = p.getRawFeatures();
            List<double[]> paddedFetures = new ArrayList<>();

            for (double[] d : features) {
                if (d.length <= max) {
                    double[] newone = new double[max];
                    System.arraycopy( d, 0, newone, 0, d.length);
                    paddedFetures.add(newone);
                }
            }
            p.setPaddedFeatures(paddedFetures);
        }

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
                DoubleStream intStream = Arrays.stream(feature);
                String[] featureStr = intStream.sorted().mapToObj(String::valueOf).toArray(String[]::new);
                csvWriter.writeNext(featureStr);
            }

            logger.debug("Writing features to folder:{} for:{} for verison:{}", folder.getAbsolutePath(), p.getProjectName(), p.getVersion());
            csvWriter.close(); //TODO check


            // generate semantic features:

            SemanticFeatures semanticFeatures = new SemanticFeatures();
            semanticFeatures.generate(folder.getAbsolutePath() + "/astfile.csv"); //currently not on the path
        }


    }

    private static String extractPath (String rootPath, String projectName) {

        String[] output = rootPath.split("/");

        for (String o : output) {
            logger.info("Path:", o);

        }
        return null;

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
