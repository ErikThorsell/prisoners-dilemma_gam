import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game {
    private static Random rng = new Random();

    // The payoff matrix for p1 given the move of p2 is payoffMatrix[p1][p2]
    public static double P = 1;
    public static double T = 5;
    public static double S = 0;
    public static double R = 3;
    
    public final static double[][] payoffMatrix = {{P,T},{S,R}};

    // Takes 2 Genomes and a number of turns and plays the finitely iterated game for that many turns and returns the average score per turn for the two players as a Tuple
    public static Touble play (Genome p1, Genome p2, int nbrOfTurns) {
        ArrayList<Byte> p1History = new ArrayList<>();
        ArrayList<Byte> p2History = new ArrayList<>();
        double p1Score = 0;
        double p2Score = 0;
        byte p1Move;
        byte p2Move;
        Touble payoff;
        for (int i = 0; i < nbrOfTurns; i++) {
            p1Move = p1.getMove(p1History);
            p2Move = p2.getMove(p2History);
            payoff = getPayoff(p1Move, p2Move);
            p1Score += payoff.x;
            p2Score += payoff.y;
            p1History.add(p1Move); p1History.add(p2Move);
            p2History.add(p2Move); p2History.add(p1Move);
        }
        return new Touble(p1Score/(double)nbrOfTurns,p2Score/(double)nbrOfTurns);
    }

    // Takes 2 Genomes, a number of turns, and a mistake rate [0,1] and plays the finitely iterated game for that many turns and returns the average score per turn for the two players as a Tuple
    public static Touble playWithMistakes (Genome p1, Genome p2, int nbrOfTurns, double mistakeRate) {
        ArrayList<Byte> p1History = new ArrayList<>();
        ArrayList<Byte> p2History = new ArrayList<>();
        double p1Score = 0;
        double p2Score = 0;
        byte p1Move;
        byte p2Move;
        boolean p1Mistake;
        boolean p2Mistake;
        Touble payoff;
        for (int i = 0; i < nbrOfTurns; i++) {
            p1Mistake = rng.nextDouble() < mistakeRate;
            p2Mistake = rng.nextDouble() < mistakeRate;
            p1Move = p1.getMove(p1History);
            p2Move = p2.getMove(p2History);
            if (p1Mistake) {p1Move = (byte)(((int)p1Move+1)%2);}            
            if (p2Mistake) {p2Move = (byte)(((int)p2Move+1)%2);}     
            payoff = getPayoff(p1Move, p2Move);
            p1Score += payoff.x;
            p2Score += payoff.y;
            p1History.add(p1Move); p1History.add(p2Move);
            p2History.add(p2Move); p2History.add(p1Move);
        }
        return new Touble(p1Score/(double)nbrOfTurns,p2Score/(double)nbrOfTurns);
    }

    // Takes two players moves and returns a tuple containing their respective score from the single round game as given in the payoff matrix (byte[][] payoffMatrix). x in Tuple is p1 score. y in Tuple is p2 score.
    public static Touble getPayoff(byte p1Move, byte p2Move) {
        return new Touble(payoffMatrix[p1Move][p2Move],payoffMatrix[p2Move][p1Move]);
    }

    public static void main(String[] args) {
        List<Boolean>[] defStrats = new List[4];
        defStrats[0] = new ArrayList<Boolean>();
        defStrats[1] = new ArrayList<Boolean>();
        defStrats[2] = new ArrayList<Boolean>();
        defStrats[3] = new ArrayList<Boolean>();

        // defStrats[0] == 00 by default
        defStrats[0].add(true);
        defStrats[0].add(true);
        defStrats[0].add(false);
        defStrats[0].add(true);
        defStrats[0].add(true);
        defStrats[0].add(false);
        defStrats[0].add(false);
        defStrats[0].add(true);
        // defStrats[1] == 01
        defStrats[1].add(true);
        defStrats[1].add(false);
        defStrats[1].add(false);
        defStrats[1].add(true);
        defStrats[1].add(true);
        defStrats[1].add(false);
        defStrats[1].add(false);
        defStrats[1].add(true);
        // defStrats[2] == 10
        defStrats[2].add(true);
        defStrats[2].add(false);
        // defStrats[3] == 11
        defStrats[3].add(true);
        defStrats[3].add(true);
        System.out.print(playWithMistakes(new TeamIsGenome(defStrats[0], 0, 8), new FixedTeamGenome(defStrats[1], 0, 8),100,0.02).toString());
    }

}
