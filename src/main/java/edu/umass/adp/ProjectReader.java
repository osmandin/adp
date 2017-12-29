package edu.umass.adp;

import jdk.nashorn.api.scripting.URLReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Functionality for reading projects from PROMISE
 */
public class ProjectReader {

    private final static Logger logger = LoggerFactory.getLogger(ProjectReader.class);

    /**
     * Process projects
     * @return Returns a Map; each entry consisting of PROMISE projects and associated files with their statuses
     * @throws Exception
     */
    public Map<PROMISE, Map> processProjects(final String projectList) throws Exception {
        logger.info("Processing projects");

        final Reader in = new FileReader(projectList); //FIXME

        final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(new String[] {"name", "version", "url", "folder"}).parse(in);

        final Map<PROMISE, Map> projectMap = new HashMap<PROMISE, Map>();

        for (final CSVRecord record : records) { // for each project, e.g., camel 1.2, 1.4, 1.6, jedit xx // TODO ideally iterate over PROMISE objet
            final String name = record.get("name");
            final String version = record.get("version");
            final String url = record.get("url");
            final String folder = record.get("folder"); // path on disk

            if (name.equals("name")) {
                continue;
            }

            final PROMISE promise = new PROMISE(name, version, url, folder);

            logger.debug("Downloading project:{}", url);

            final Map<String, String> files = process(url);

            projectMap.put(promise, files);
        }

        return projectMap;
    }

    /**
     * Returns a list of filename and buggy status in a particular project, e.g., camel 1.4.csv
     * @param URL
     * @return
     * @throws Exception
     */
    private Map<String, String> process(final String URL) throws Exception {
        final Reader in = readContents(URL);

        // headers: "name,version,name,wmc,dit,noc,cbo,rfc,lcom,ca,ce,npm,lcom3,loc,dam,moa,mfa,cam,ic,cbm,amc,max_cc,avg_cc,bug";
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(new String[] {"name", "version", "filename",
                "wmc", "dit", "noc", "cbo", "rfc", "lcom", "ca", "ce", "npm", "lcom3", "loc", "dam", "moa", "mfa", "cam",
                "ic", "cbm", "amc", "max_cc", "avg_cc", "bug"}).parse(in);

        final Map<String, String> fileStatus = new HashMap<String, String>();

        for (final CSVRecord record : records) {

            final String project = record.get("name");

            if (project.equals("name")) {  // skip first row
                continue;
            }

            final String version = record.get("version");
            final String fileName = record.get("filename");
            final String buggy = record.get("bug");

            //System.out.println("Populating filename:" + fileName);
            //System.out.println("buggy:" + buggy);

            fileStatus.put(fileName, buggy);
        }

        in.close();

        return fileStatus;
    }

    // TODO replace
    private BufferedReader readContents(final String url) throws  Exception {
        URL oracle = new URL(url);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        return in;
    }


    private static void test() throws Exception {
        final Reader in = new FileReader("/Users/osmandin/IdeaProjects/adp/src/main/test/camel/camel-1.4.csv");
        // "name,version,name,wmc,dit,noc,cbo,rfc,lcom,ca,ce,npm,lcom3,loc,dam,moa,mfa,cam,ic,cbm,amc,max_cc,avg_cc,bug";
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(new String[] {"name", "version", "filename", "wmc",
                "dit", "noc", "cbo", "rfc", "lcom", "ca", "ce", "npm", "lcom3", "loc", "dam", "moa", "mfa", "cam", "ic",
                "cbm", "amc", "max_cc", "avg_cc", "bug"}).parse(in);
        for (final CSVRecord record : records) {
            String project = record.get("name");
            String version = record.get("version");
            final String fileName = record.get("filename");
            final String buggy = record.get("bug");
            System.out.println("filename:" + fileName);
            System.out.println("buggy:" + buggy);
        }
    }

    public static void main (String args[]) throws  Exception{
        // test();
        new ProjectReader().processProjects("/Users/osmandin/IdeaProjects/adp/src/main/resources/links.csv");
    }

}


