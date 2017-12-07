import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class TeamIsGenome implements Genome {
    private List<Boolean> strategy;
    private int maxGenomeSize;
    private Random rng;

    private final double MUTATION_CHANCE = Dilemma.MUTATE_PROBABILITY;
    private final double SPLIT_PROB = 0.1;
    private final double DUPLICATE_PROB = 0.1;

    public TeamIsGenome (List<Boolean> s, int t, int maxGenomeSize) {
        byte b;
        int val;
        strategy = new ArrayList<Boolean>(s);
        this.maxGenomeSize = maxGenomeSize;
        rng = new Random();
    }

    //"Random" constructor
    public TeamIsGenome(int maxGenomeSize, int noOfTeams){
        strategy = new ArrayList<Boolean>();
        this.maxGenomeSize = maxGenomeSize;
        rng = new Random();
        strategy.add(rng.nextBoolean());
        strategy.add(rng.nextBoolean());
    }

    public List<Boolean> getStrategy(){
        return strategy;
    }

    public int getTeam() {return 0;}

    public byte getMove(List<Byte> history){
        if(history.size() == 0){
            return firstMove();
        }
        int val = 0;
        int b;

        for (int i = 0; i < history.size() && i < maxGenomeSize; i++) {
            b = history.get(history.size()-1-i);
            val |= (b << i);
        }
        boolean willCooperate = strategy.get(val%strategy.size());
        return (willCooperate) ? (byte)1 : (byte)0;
    }

    private byte firstMove(){
        return (strategy.get(1)) ? (byte)1 : (byte)0; //Start off assuming the other guy cooperated.
                                              //Works well for a lot of basic strategies like
                                              //T4T, AD and AC.
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
        else if(geneRoll<DUPLICATE_PROB+SPLIT_PROB && strategy.size() < maxGenomeSize){
            int length = strategy.size();
            for(int i = 0; i < length; i++){
                strategy.add(strategy.get(i));
            }
        }
        else {
            int muta = rng.nextInt(strategy.size());
            strategy.set(muta, !strategy.get(muta));
        }
    }

    public Color color() {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < strategy.size(); i++) {
            if (strategy.get(i)) {
                r += 45*(i%3);
                g += 45*((i+1)%3);
                b += 45*((i+2)%3);
            }
        }
        r = r%255;
        g = g%255;
        b = b%255;
        return new Color(r,g,b);
    }

    public TeamIsGenome clone() {
        return new TeamIsGenome(strategy, 0, maxGenomeSize);
    }
}
