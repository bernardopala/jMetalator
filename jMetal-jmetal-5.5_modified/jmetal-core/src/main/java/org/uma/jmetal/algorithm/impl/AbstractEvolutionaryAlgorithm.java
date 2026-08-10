package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.util.CurrentSolutionSetReceiver;
import org.uma.jmetal.problem.Problem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class representing an evolutionary algorithm
 * @param <S> Solution
 * @param <R> Result
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractEvolutionaryAlgorithm<S, R>  implements Algorithm<R>{
  protected List<S> population;
  protected Problem<S> problem ;

  private final Set<CurrentSolutionSetReceiver<S>> currentSolutionSetReceivers = Collections.newSetFromMap(
          new ConcurrentHashMap<CurrentSolutionSetReceiver<S>, Boolean>(0));

  public List<S> getPopulation() {
    return population;
  }
  public void setPopulation(List<S> population) {
    this.population = population;
  }

  public void setProblem(Problem<S> problem) {
    this.problem = problem ;
  }
  public Problem<S> getProblem() {
    return problem ;
  }

  protected abstract void initProgress();

  protected abstract void updateProgress();

  protected abstract boolean isStoppingConditionReached();

  protected abstract  List<S> createInitialPopulation() ;

  protected abstract List<S> evaluatePopulation(List<S> population);

  protected abstract List<S> selection(List<S> population);

  protected abstract List<S> reproduction(List<S> population);

  protected abstract List<S> replacement(List<S> population, List<S> offspringPopulation);

  @Override public abstract R getResult();

  @Override public void run() {
    List<S> offspringPopulation;
    List<S> matingPopulation;

    population = createInitialPopulation();
    population = evaluatePopulation(population);
    initProgress();
    while (!isStoppingConditionReached()) {
      this.sendCurrentSolutionSetToReceivers(population);
      matingPopulation = selection(population);
      offspringPopulation = reproduction(matingPopulation);
      offspringPopulation = evaluatePopulation(offspringPopulation);
      population = replacement(population, offspringPopulation);
      updateProgress();
    }

    this.sendCurrentSolutionSetToReceivers(population);
  }

  private void sendCurrentSolutionSetToReceivers(List<S> solutionSet) {
    for (CurrentSolutionSetReceiver<S>  observer : this.currentSolutionSetReceivers) { // this is safe due to thread-safe Set
      observer.ReceiveCurrentSolutionSet(solutionSet);
    }
  }

  public void subscribeCurrentSolutionSetReceiver(CurrentSolutionSetReceiver<S> receiver) {
    if (currentSolutionSetReceivers == null) return;
    this.currentSolutionSetReceivers.add(receiver);
  }

  public void unsubscribeCurrentSolutionSetReceiver(CurrentSolutionSetReceiver<S> receiver) {
    if (currentSolutionSetReceivers == null) return;
    this.currentSolutionSetReceivers.remove(receiver);
  }
}
