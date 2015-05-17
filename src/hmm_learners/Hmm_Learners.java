/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hmm_learners;

/**
 *
 * @author lorenzo
 */
import java.util.*;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesReader;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class demonstrates how to build a HMM with known parameters, how to
 * generate a sequence of observations given a HMM, how to learn the parameters
 * of a HMM given observation sequences, how to compute the probability of an
 * observation sequence given a HMM and how to convert a HMM to a Postscript
 * drawing.
 * <p>
 * The example used is that of a wireless computer network that can experience
 * jamming. When the wireless medium is (resp. is not) jammed, a lot (resp. few)
 * packets are lost. Thus, the HMMs built here have two states (jammed/not
 * jammed).
 */
public class Hmm_Learners {

    private static final int MAX_ITER = 100;

    public enum AOI {

        AB, BA;

        public ObservationDiscrete<AOI> observation() {
            return new ObservationDiscrete<AOI>(this);
        }

    }

    static public void main(String[] argv)
            throws java.io.IOException {

        /*Init hmm*/
        Hmm<ObservationDiscrete<AOI>> originalHmm = new Hmm<ObservationDiscrete<AOI>>(8, new OpdfDiscreteFactory<AOI>(AOI.class));

        double[] pi = {1};

        for (int i = 0; i < originalHmm.nbStates(); i++) {
            originalHmm.setPi(i, pi[i]);
        }

        double[][] t = {{1}};
        for (int i = 0; i < originalHmm.nbStates(); i++) {
            for (int j = 0; j < originalHmm.nbStates(); j++) {
                originalHmm.setAij(i, j, t[i][j]);
            }
        }

        double[][] o = {{1}};
        for (int i = 0; i < originalHmm.nbStates(); i++) {
            originalHmm.setOpdf(i, new OpdfDiscrete<AOI>(AOI.class, o[i]));
        }

        /* Baum-Welch learning */
        BaumWelchLearner bwl = new BaumWelchLearner();
        Hmm<ObservationDiscrete<AOI>> trainedHmm;
        trainedHmm = bwl.iterate(originalHmm, null);
        for (int i = 0; i < MAX_ITER; i++) {
            trainedHmm = bwl.iterate(trainedHmm, null);
        }

        /*LO*/
        System.out.println("Sequence probability: "
                + trainedHmm.probability(null));

    }

    List< List< ObservationDiscrete<AOI>>> readSequence(String filename) {
        List< List< ObservationDiscrete<AOI>>> o = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<ObservationDiscrete<AOI>> tmp = new ArrayList<>();
                for (String singleObs : line.split(":")) {
                    tmp.add(AOI.valueOf(singleObs).observation());
                }
                o.add(tmp);
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Hmm_Learners.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Hmm_Learners.class.getName()).log(Level.SEVERE, null, ex);
        }
        return o;
    }

}
