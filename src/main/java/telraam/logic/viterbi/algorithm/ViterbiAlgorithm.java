package telraam.logic.viterbi.algorithm;

import java.util.*;

/**
 * The class performing the Viterbi algorithm.
 * @param <O> The type of the observations.
 */
public class ViterbiAlgorithm<O> {
    private final ViterbiModel<Integer, O> model;

    private ViterbiState lastState;

    public ViterbiAlgorithm(ViterbiModel<Integer, O> viterbiModel) {
        this.model = viterbiModel;

        System.out.println("Starting Viterbi algorithm with observations " + viterbiModel.getObservations() + " and hidden states " + viterbiModel.getHiddenStates());

        this.verifyProbabilities();

        // Set up the initial probabilities
        int numSegments = this.model.getHiddenStates().size();
        double[] probabilities = new double[numSegments];
        int[] previousSegments = new int[numSegments];

        for (Map.Entry<Integer, Double> entry : viterbiModel.getStartProbabilities().entrySet()) {
            probabilities[entry.getKey()] = entry.getValue();
            previousSegments[entry.getKey()] = -1;
        }

        this.lastState = new ViterbiState(null, probabilities, previousSegments);
    }

    /**
     * Verify that the given probabilities are valid.
     * @throws InvalidParameterException If the probabilities were not valid.
     */
    private void verifyProbabilities() {
        if (!this.model.getTransitionProbabilities().keySet().equals(this.model.getHiddenStates())) {
            throw new InvalidParameterException("Invalid key set for transition probabilities");
        }

        for (Integer state : this.model.getHiddenStates()) {
            if (!this.model.getTransitionProbabilities().get(state).keySet().equals(this.model.getHiddenStates())) {
                throw new InvalidParameterException("Invalid key set for transition probabilities for state " + state);
            }
        }

        if (!this.model.getEmitProbabilities().keySet().equals(this.model.getHiddenStates())) {
            throw new InvalidParameterException("Invalid key set for emission probabilities: " + this.model.getEmitProbabilities().keySet() + " != " + this.model.getHiddenStates());
        }

        for (Integer state : this.model.getHiddenStates()) {
            if (!this.model.getTransitionProbabilities().get(state).keySet().equals(this.model.getHiddenStates())) {
                throw new InvalidParameterException(
                        "Invalid key set for emission probabilities for state " +
                        state +
                        ": " +
                        this.model.getTransitionProbabilities().get(state).keySet() +
                        " != " +
                        this.model.getObservations()
                );
            }
        }
    }

    /**
     * Handle an observation.
     * @param observation The observation to process.
     */
    public void observe(O observation) {
        int numSegments = this.model.getHiddenStates().size();
        double[] probabilities = new double[numSegments];
        int[] previousSegments = new int[numSegments];

        for (int nextSegment = 0; nextSegment < numSegments; nextSegment++) {
            probabilities[nextSegment] = 0;
            for (int previousSegment = 0; previousSegment < numSegments; previousSegment++) {
                double probability = this.lastState.getProbability(previousSegment) *
                        this.model.getTransitionProbabilities().get(previousSegment).get(nextSegment) *
                        this.model.getEmitProbabilities().get(nextSegment).get(observation);
                if (probabilities[nextSegment] < probability) {
                    probabilities[nextSegment] = probability;
                    previousSegments[nextSegment] = previousSegment;
                }
            }
        }

        // normalize probabilities
        double sum = Arrays.stream(probabilities).sum();
        for (int i = 0; i < numSegments; i++) {
            probabilities[i] /= sum;
        }

        this.lastState = new ViterbiState(this.lastState, probabilities, previousSegments);

        //TODO: only keep the last X Results, as there will be a LOT of observations.
    }

    /**
     * Get the current state of the Viterbi algorithm.
     * @return The last Result.
     */
    public ViterbiState getState() {
        return this.lastState;
    }
}
