import java.util.List;
import java.awt.Color;

public interface Genome {
    public List<Boolean> getStrategy();

    public byte getMove(List<Byte> History);
    public int getTeam();
    public void mutate(double mutaRoll);
    public Genome clone();
    public Color color();
}
