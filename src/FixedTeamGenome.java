import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.Color;

public class FixedTeamGenome implements Genome {
        private static final int NO_OF_TEAMS = Dilemma.NO_OF_TEAMS;
        private final double MUTATION_CHANCE = Dilemma.MUTATE_PROBABILITY;
        private final double SPLIT_PROB = 0.1;
        private final double DUPLICATE_PROB = 0.1;
        private final double ROGUE_PROB = 0.01; //Chance that the genome changes team
        private List<Boolean> strategy;
        private int team;
        private int maxGenomeSize;
        private int maxMemorySize;
        private Random rng;

        public FixedTeamGenome (List<Boolean> s, int t, int maxMemorySize) {
            strategy = new ArrayList<Boolean>(s);
            team = t;
            this.maxGenomeSize = 1 << maxGenomeSize;
            this.maxMemorySize = maxMemorySize;
            rng = new Random();
        }

        //"Random" constructor
        public FixedTeamGenome(int maxMemorySize, int noOfTeams){
            this.maxGenomeSize = 1 << maxMemorySize;
            this.maxMemorySize = maxMemorySize;
            rng = new Random();
            team = rng.nextInt();
            strategy.add(rng.nextBoolean());
            strategy.add(rng.nextBoolean());
        }

        public List<Boolean> getStrategy(){
            return strategy;
        }

        public int getTeam() {
            return team;
        }

        public byte getMove(List<Byte> history){
            if(history.size() == 0){
                return firstMove();
            }
            int val = 0;
            int b;

            for (int i = 0; i < history.size() && i < maxMemorySize; i++) {
                b = history.get(history.size()-1-i);
                val |= (b << i);
            }
            boolean willCooperate = strategy.get(val%strategy.size());
            return (willCooperate) ? (byte)1 : (byte)0;
        }

        private byte firstMove(){
            if (strategy.size() > 1) {
                return (strategy.get(1)) ? (byte)1 : (byte)0; //Start off assuming the other guy cooperated.
                                                              //Works well for a lot of basic strategies like
                                                              //T4T, AD and AC.
            } else {
                return strategy.get(0) ? (byte)1 : (byte)0; // if stratsize == 1 then play that strat.
            }
        }

        public void mutate(double mutaRoll) {
            if(mutaRoll > MUTATION_CHANCE)
                return;
            double geneRoll = rng.nextDouble();
            if(geneRoll<SPLIT_PROB && strategy.size() > 2){
                int length = strategy.size();
                for(int i = 0; i < length/2; i++){
                    strategy.remove(strategy.size()-1);
                }
            }
            else if(geneRoll<DUPLICATE_PROB+SPLIT_PROB && strategy.size() < maxMemorySize){
                int length = strategy.size();
                for(int i = 0; i < length; i++){
                    strategy.add(strategy.get(i));
                }
            }
            else if(geneRoll<DUPLICATE_PROB+SPLIT_PROB+ROGUE_PROB){
                team = rng.nextInt(NO_OF_TEAMS);
            }
            else{
                int muta = rng.nextInt(strategy.size());
                strategy.set(muta, !strategy.get(muta));
            }
        }

        public Color color() {
            return PaintLattice.dc[team];
        }

        public FixedTeamGenome clone() {
            return new FixedTeamGenome(strategy, team, maxMemorySize);
        }
}
