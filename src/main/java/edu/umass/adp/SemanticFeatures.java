package edu.umass.adp;


import org.deeplearning4j.nn.api.Layer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class SemanticFeatures {

    final static Logger logger = LoggerFactory.getLogger(SemanticFeatures.class);

    /**
     * Util
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception{
        new SemanticFeatures().generate("example.csv", 6, 6);
    }

    /**
     * Generates semantic features
     * @throws Exception
     */
    public String generate(final String file,  int labelIndex, int numRecords) throws Exception {
        int seed = 1234;
        int numClasses = 100; // Number of labels
        int batchSize = numRecords;
        int iterations = 1;
        int listenerFreq = iterations/ 5;

        logger.info("Loading data....");

        final DataSetIterator dataSetIterator = readCSVDataset(file, batchSize, labelIndex, numClasses);

        // a stack of RBMs

        final int FEATURE_OUTPUT = 100;

        // track time

        long now = System.currentTimeMillis();

        final MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .activation(Activation.SIGMOID)
                .weightInit(WeightInit.XAVIER)
                .learningRate(0.01)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new RBM.Builder().nIn(labelIndex).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(1, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(2, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(3, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(4, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(5, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(6, new RBM.Builder().nIn(100).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(7, new RBM.Builder().nIn(100).nOut(labelIndex).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(8, new OutputLayer.Builder(LossFunctions.LossFunction.MSE) // DON'T SET TO ELSE
                        .activation(Activation.SIGMOID)
                        .nIn(labelIndex).nOut(FEATURE_OUTPUT)
                        .build())
                .pretrain(true).backprop(true)
                .build();


        final MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(listenerFreq));

        logger.info("Training model....");

        while (dataSetIterator.hasNext()) {
            final DataSet next = dataSetIterator.next();
            model.fit(next);
        }

        long time = System.currentTimeMillis() - now;

        logger.info("Trained in {} msec", time);

        List<INDArray> feed = model.feedForwardToLayer(8, true);

        logger.info("Layer names:{}", model.getLayerNames());

        return feed.get(8).toString();
    }

    /**
     * used for testing and training
     *
     * @param csvFileClasspath
     * @param batchSize
     * @param labelIndex
     * @param numClasses
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static DataSetIterator readCSVDataset(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

        logger.info("reading:{} with label index:{}", csvFileClasspath, labelIndex);

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(csvFileClasspath)));
        // logger.info("First record:{}", rr.nextRecord().getRecord().toString());
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);
        return iterator;

    }


    /**
     * Can be used to get a record from a test dataset
     * @param csvFileClasspath
     * @param batchSize
     * @param labelIndex
     * @param numClasses
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static Double[] getRecordFromCSV(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(csvFileClasspath)));
        System.out.println(rr.nextRecord().getRecord().toString());

        String[] d = rr.nextRecord().getRecord().toString().replace("[","").replace("]","").split(",");

        logger.info("Record:{}", d);

        Double[] dd = new Double[labelIndex];

        for (int i = 0; i < d.length - 1; i++) {
            dd[i] = Double.parseDouble(d[i]);
        }

        return dd;
    }

    /**
     * Used for reading file from classpath
     * @param csvFileClasspath
     * @param batchSize
     * @param labelIndex
     * @param numClasses
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static org.nd4j.linalg.dataset.DataSet getRecordsfromClasspath(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new ClassPathResource(csvFileClasspath).getFile()));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);
        return iterator.next();
    }

}
