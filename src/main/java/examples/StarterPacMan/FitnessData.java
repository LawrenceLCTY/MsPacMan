package examples.StarterPacMan;

import java.util.ArrayList;
import java.util.List;

public class FitnessData {
	private List<LevelFitness> levelFitnessList;

	public FitnessData() {
		this.levelFitnessList = new ArrayList<>();
	}

	public void recordFitness(int level, double fitnessType1, int fitnessType2) {
		LevelFitness levelFitness = new LevelFitness(level, fitnessType1, fitnessType2);
		levelFitnessList.add(levelFitness);
	}

	public void printData() {
		System.out.println("Level\tFitnessType1\tFitnessType2");
		for (LevelFitness levelFitness : levelFitnessList) {
			System.out.printf("%d\t%.2f\t\t\t%d\n",
					levelFitness.getLevel(),
					levelFitness.getFitnessType1(),
					levelFitness.getFitnessType2());
		}
	}
}

class LevelFitness {
	private int level;
	private double fitnessType1;
	private int fitnessType2;

	public LevelFitness(int level, double fitnessType1, int fitnessType2) {
		this.level = level;
		this.fitnessType1 = fitnessType1;
		this.fitnessType2 = fitnessType2;
	}

	public int getLevel() {
		return this.level;
	}

	public double getFitnessType1() {
		return this.fitnessType1;
	}

	public int getFitnessType2() {
		return this.fitnessType2;
	}
}
