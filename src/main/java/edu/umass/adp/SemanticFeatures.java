package edu.umass.adp;


import org.nd4j.linalg.dataset.api.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;

public class SemanticFeatures {

    final static Logger logger = LoggerFactory.getLogger(SemanticFeatures.class);

    /**
     * Test
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception{
        new SemanticFeatures().generate("example.csv");
    }

    /**
     * Generates semantic features
     * @throws Exception
     */
    public int[][] generate(final String file) throws Exception {

        int seed = 123; //TODO
        //int numSamples = MnistDataFetcher.NUM_EXAMPLES;

        int labelIndex = 6; // TODO (would depend on the length)
        int numClasses = 6; // Number of classes
        int batchSize = 15;
        int iterations = 100;
        int listenerFreq = iterations/2;

        logger.info("Loading data....");

        final DataSet dataset = readCSVDataset(file, batchSize, labelIndex, numClasses);

        dataset.getFeatures();

        logger.info("Building model....");

        // a stack of RBMs

        final MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .learningRate(0.1)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new RBM.Builder().nIn(6).nOut(3).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(1, new RBM.Builder().nIn(3).nOut(3).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.KL_DIVERGENCE) // DON'T SET TO ELSE
                        .activation(Activation.SOFTMAX)
                        .nIn(3).nOut(6).build())
                //.backprop(true).pretrain(false)
                .pretrain(true).backprop(true)
                .build();

        final MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.setListeners(new ScoreIterationListener(listenerFreq));

        logger.info("Training model....");

        // logger.info("Num of examples:{}", dataset.numExamples());

        model.fit(dataset);

        double[][] data = { //TODO
                {0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1},
                {0, 0 ,0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1},

        };

        List<INDArray> feed = model.feedForward(); // maybe this it?

        NDArray testDataset = new NDArray(data);
        int[] prediction = model.predict(testDataset);

        logger.info("Prediction:");

        for (int i = 0; i < data.length; i++) {
            System.out.println(prediction[i]);
        }

        return null; //FIXME

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
    private static org.nd4j.linalg.dataset.DataSet readCSVDataset(
            String csvFileClasspath, int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{

        logger.info("reading:{}", csvFileClasspath);

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(csvFileClasspath)));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr,batchSize,labelIndex,numClasses);
        return iterator.next();
    }
}
