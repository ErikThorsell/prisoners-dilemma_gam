import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class Dilemma {
    // CONSTANTS
    public static final int N = 64; //Width of lattice
    public static final int NO_OF_TEAMS = 6;
    public static final int NO_OF_ROUNDS = 100; //Number of rounds in a game
    public static final boolean MUTATIONS_ON = true; //Whether genomes might mutate
    public static final boolean MUTATE_ON_SURVIVAL = true; //whether genomes might mutate at any time or only after being captured
    public static final boolean ever = true;

    // FIXED VARIABLES (Does not change during running of one round)
    private static int memMaxLength = 8;
    private static Random rng = new Random();
    private static boolean reset = true;
    private static boolean resetWithOriginal = true;

    public static double  MUTATE_PROBABILITY        = 0.001; //Probability that a genome migh mutate (0-1)
    public static double  MISTAKE_RATE              = 0.02;
    public static boolean SCORE_IS_AVERAGE          = false; //Whether capture is based on team averages
    public static boolean SCORE_ADD_AVERAGE         = false; //whether capture is based on your score + team average
    public static boolean FRIENDLY_CONQUEST_ENABLED = true;
    public static boolean PRINT_MODE = false;

    // VARIABLES
    private static double[][] scores = new double[N][N];
    private static double[] teamSize = new double[NO_OF_TEAMS];
    private static double[] teamScores = new double[NO_OF_TEAMS];
    private static double[] actualTeamScores = new double[NO_OF_TEAMS];
    private static Genome[][] lattice = new Genome[N][N];
    private static Genome[][] nextLattice = lattice.clone();
    private static int roundTime; // The number of turns the lattice have played
    public  static int delayTime = 50; // The delay between rounds in milliseconds
    private static boolean pause = false;
    private static boolean step = false;
    private static Semaphore pauseBlock = new Semaphore(1, true);

    public static double getMutate() {return MUTATE_PROBABILITY;}
    public static double getMistake() {return MISTAKE_RATE;}
    public static void setMutate(double d) {MUTATE_PROBABILITY = d; reset=true;}
    public static void setMistake(double d) {MISTAKE_RATE = d; reset=true;}
    public static void setDelay(int d) {delayTime = d;}
    public static void fastest() {delayTime = 1;}
    public static void fast() {delayTime = 50;}
    public static void slow() {delayTime = 600;}
    public static double[] getTeamScores() {return actualTeamScores;}
    public static void reset() {reset = true;}
    public static void resetWithOriginal() {resetWithOriginal = true;}
    public static void pause(){
        if(pause){
            pause = false;
            pauseBlock.release();
            pauseBlock.release();
        }
        else{
            pause = true;
            try{
                pauseBlock.acquire();
            }catch(InterruptedException e){
                System.out.println("Exception in pause()");
            }
        }
    }
    public static void step() {
        if(pause){
            step = true;
            pauseBlock.release();
        }
    };
    public static void exit() {System.exit(0);}
    public static void togglePrint() {
        if(PRINT_MODE) {
            PRINT_MODE = false;
        } else {
            PRINT_MODE = true;
            reset = true;
        }
    }

    public static void main (String[] args) {
        System.out.println("Borders mean:\nYellow = All Coop\nRed = All Defect\nBlue =TitForTat\nGreen = Anti-TitForTat");
        PaintLattice p = new PaintLattice();
        for(;ever;){
            if(pause){
                try{
                    pauseBlock.acquire();
                }catch(InterruptedException e){
                    System.out.println("Exception with semaphore in main");
                }
            }
            if (reset) {
                if (resetWithOriginal) {
                    fillWithGeneTeam(NO_OF_TEAMS);
                } else {
                    fillWithRandom(NO_OF_TEAMS);
                }
                reset = false;
                p.roundTime = 0;
                roundTime = 0;
            }
            printLattice(p);
            delay(delayTime); //time between steps in milliseconds
            p.roundTime++; //increase the roundcounter on the display
            roundTime++;
            updateScores();
            if (SCORE_IS_AVERAGE) {averageScores();}
            else if (SCORE_ADD_AVERAGE) {addAverageScores();}
            actualTeamScores = teamScores.clone();
            updateLattice();
            if(!pause){
                delay(delayTime); //time between steps in milliseconds
            }
            step = false;
        }
    }

    private static void updateScores() {
        Genome p1;
        Genome p2;
        int x2;
        int y2;
        Touble tempScore;
        for(int i = 0; i < NO_OF_TEAMS; i++){
            teamScores[i] = 0;
        }
        for(int z = 0; z < NO_OF_TEAMS; z++) {teamSize[z] = 0;}
        for(int x = 0; x < N; x++){
            for(int y = 0; y < N; y++){
                x2 = (x+1)%N;
                y2 = (y+1)%N;
                p1 = lattice[x][y];
                teamSize[p1.getTeam()]++;
                p2 = lattice[x][y2];
                tempScore = Game.playWithMistakes(p1, p2, NO_OF_ROUNDS, MISTAKE_RATE);
                scores[x][y] += tempScore.x;
                scores[x][y2] += tempScore.y;
                teamScores[p1.getTeam()] += tempScore.x;
                teamScores[p2.getTeam()] += tempScore.y;

                p2 = lattice[x2][y];
                tempScore = Game.playWithMistakes(p1, p2, NO_OF_ROUNDS, MISTAKE_RATE);
                scores[x][y] += tempScore.x;
                scores[x2][y] += tempScore.y;
                teamScores[p1.getTeam()] += tempScore.x;
                teamScores[p2.getTeam()] += tempScore.y;
            }
        }
        for(int i = 0; i < NO_OF_TEAMS; i++){
            teamScores[i] /= (teamSize[i]);
            if (teamScores[i] < 0) {System.out.println("!");}
        }
    }

    private static void updateLattice(){
        for(int x = 0; x < N; x++){
            for(int y = 0; y < N; y++){
                double highScore = scores[x][y];
                int x2 = (x+1)%N;
                int y2 = (y+1)%N;
                nextLattice[x][y] = lattice[x][y];
                double geneRoll = rng.nextDouble();
                if(MUTATIONS_ON && MUTATE_ON_SURVIVAL) {
                    nextLattice[x][y].mutate(geneRoll);
                }
                if(highScore < scores[x][y2])
                    highScore = conquer(x, y, x, y2);
                if(highScore < scores[x2][y])
                    highScore = conquer(x, y, x2, y);

                x2 = (x-1+N)%N;
                y2 = (y-1+N)%N;
                if(highScore < scores[x][y2])
                    highScore = conquer(x, y, x, y2);
                if(highScore < scores[x2][y])
                    highScore = conquer(x, y, x2, y);
            }
        }
        lattice = nextLattice;
        nextLattice = new Genome[N][N];
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                scores[i][j] = 0;
            }
        }
    }
    private static double conquer(int x1, int y1, int x2, int y2){
        if(FRIENDLY_CONQUEST_ENABLED ||
           lattice[x1][y1].getTeam() != lattice[x2][y2].getTeam())
        {
            nextLattice[x1][y1] = lattice[x2][y2].clone();
            if (MUTATIONS_ON) {nextLattice[x1][y1].mutate(rng.nextDouble());}
            return scores[x2][y2];
        }
        else
            return scores[x1][y1];
    }

    public static void averageScores() {
        int team;
        for(int x = 0; x < N; x++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                scores[x][y] = teamScores[team];
            }
        }
    }

    public static void addAverageScores() {
        int team;
        for(int x = 0; x < N; x ++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                scores[x][y] += teamScores[team];
            }
        }
    }

    public static void lowestScores() {
        int team;
        Arrays.fill(teamScores, Double.MAX_VALUE);
        for(int x = 0; x < N; x ++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                if (teamScores[team] > scores[x][y]) {teamScores[team] = scores[x][y];}

            }
        }
        for(int x = 0; x < N; x ++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                scores[x][y] = teamScores[team];
            }
        }
    }

    public static void highestScores() {
        int team;
        Arrays.fill(teamScores, 0);
        for(int x = 0; x < N; x ++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                if (teamScores[team] < scores[x][y]) {teamScores[team] = scores[x][y];}

            }
        }
        for(int x = 0; x < N; x ++){
            for(int y = 0; y < N; y++){
                team = lattice[x][y].getTeam();
                scores[x][y] = teamScores[team];
            }
        }
    }

    public static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void fillWithGeneTeam(int numTeams) {
        int t;
        List<Boolean> s;
        List<Boolean>[] defStrats = new List[4];
        defStrats[0] = new ArrayList<Boolean>();
        defStrats[1] = new ArrayList<Boolean>();
        defStrats[2] = new ArrayList<Boolean>();
        defStrats[3] = new ArrayList<Boolean>();

        // defStrats[0] == 00 by default
        defStrats[0].add(false);
        defStrats[0].add(false);
        // defStrats[1] == 01
        defStrats[1].add(false);
        defStrats[1].add(true);
        // defStrats[2] == 10
        defStrats[2].add(true);
        defStrats[2].add(false);
        // defStrats[3] == 11
        defStrats[3].add(true);
        defStrats[3].add(true);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                // some randomness stuff for genomes & teams
                int test = ThreadLocalRandom.current().nextInt(0, 3 + 1);
                t = test;
                s = defStrats[test];
                //t = ThreadLocalRandom.current().nextInt(0, numTeams + 1);
                //s = defStrats[ThreadLocalRandom.current().nextInt(0, 3 + 1)];
                lattice[i][j] = new TeamIsGenome(s, t, memMaxLength);
            }
        }
    }

    private static void fillWithRandom(int numTeams) {
        int t;
        List<Boolean> s;
        List<Boolean>[] defStrats = new List[4];
        defStrats[0] = new ArrayList<Boolean>();
        defStrats[1] = new ArrayList<Boolean>();
        defStrats[2] = new ArrayList<Boolean>();
        defStrats[3] = new ArrayList<Boolean>();

        // defStrats[0] == 00 by default
        defStrats[0].add(false);
        defStrats[0].add(false);
        // defStrats[1] == 01
        defStrats[1].add(false);
        defStrats[1].add(true);
        // defStrats[2] == 10
        defStrats[2].add(true);
        defStrats[2].add(false);
        // defStrats[3] == 11
        defStrats[3].add(true);
        defStrats[3].add(true);

        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                // some randomness stuff for genomes & teams
                t = ThreadLocalRandom.current().nextInt(0, numTeams);
                teamSize[t]++;
                s = defStrats[ThreadLocalRandom.current().nextInt(0, 3 + 1)];
                lattice[i][j] = new FixedTeamGenome(s, t, memMaxLength);
            }
        }
    }

    public static void playWithAverage() {
        SCORE_IS_AVERAGE = true;
        SCORE_ADD_AVERAGE = false;
        FRIENDLY_CONQUEST_ENABLED = false;
        resetWithOriginal = false;
        reset = true;
    }

    public static void playWithOriginal() {
        SCORE_IS_AVERAGE = false;
        SCORE_ADD_AVERAGE = false;
        FRIENDLY_CONQUEST_ENABLED = true;
        resetWithOriginal = true;
        reset = true;
    }

    public static void playWithAddedAverage() {
        SCORE_IS_AVERAGE = false;
        SCORE_ADD_AVERAGE = true;
        FRIENDLY_CONQUEST_ENABLED = true;
        resetWithOriginal = false;
        reset = true;
    }

    private static void printLattice(PaintLattice p) {
        p.paint(lattice);

        if (PRINT_MODE) {
            try{
                for (int i = 0; i < teamSize.length; i++) { //print size of each team
                    PrintWriter writer = new PrintWriter(new FileWriter(new File("data/team_" + i + ".csv"), true));
                    writer.println(roundTime + "," + teamSize[i]);
                    writer.close();
                }


                //print size of each strategy
                final Map<String, Integer> counter = new HashMap<String, Integer>();
                for(int i = 0; i < lattice.length; i++) {
                    for(int j = 0; j < lattice[i].length; j++) {
                        Boolean[] bs = lattice[i][j].getStrategy().toArray(new Boolean[lattice[i][j].getStrategy().size()]);
                        String strategyString = "";
                        for (int q = 0; q < bs.length; q++) {
                            String eachBit = bs[q] ? "1" : "0";
                            strategyString += eachBit;
                        }
                        counter.put(strategyString, 1 + (counter.containsKey(strategyString) ? counter.get(strategyString) : 0));
                    }
                }
                List<String> list = new ArrayList<String>(counter.keySet());
                /*Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String x, String y) {
                        return counter.get(y) - counter.get(x);
                    }
                });
                */
                for (String oneStrategy : list) {
                    PrintWriter writer = new PrintWriter(new FileWriter(new File("data/strategy_" + oneStrategy + ".csv"), true));
                    writer.println(roundTime + "," + counter.get(oneStrategy));
                    writer.close();
                    
                }

            } catch (IOException e) {
               // do something
            }
        }
        
    }

    private static void printScores() {
        System.out.println();
        for (int i = 0; i < 64; i++) {
            System.out.println();
            for (int j = 0; j < 64; j++) {
                System.out.print(scores[i][j]);
                System.out.print(" ");
            }
        }
    }
}

