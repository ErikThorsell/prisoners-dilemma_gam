import java.awt.*;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;

public class PaintLattice {
    private Grid grid;
    private InfoPanel info;
    public static final Color[] dc = {
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.PINK,
        new Color(255,102,0),
        new Color(255,0,255),
        Color.WHITE,
        Color.GRAY,
        Color.CYAN
    };

    private static Color[][] teams = new Color[64][64];
    private static Color[][] genomes = new Color[64][64];
    public static int roundTime;
    private static String avgName = "";


    public static class InfoPanel extends JPanel {
        private HashMap<Integer,ArrayList<String>> genomes;

        private void setGenomes (Genome[][] lattice) {
            genomes = new HashMap<Integer,ArrayList<String>>();
            for(int i = 0; i < lattice.length; i++) {
                for(int j = 0; j < lattice[i].length; j++) {
                    Boolean[] bs = lattice[i][j].getStrategy().toArray(new Boolean[lattice[i][j].getStrategy().size()]);
                    boolean[] b = new boolean[bs.length];
                    for (int q = 0; q < bs.length; q++) {
                            b[q] = bs[q].booleanValue();
                    }

                    int t = lattice[i][j].getTeam();

                    ArrayList<String> ts;

                    if (genomes.containsKey(t)) {
                        ts = genomes.get(t);
                    } else {
                        ts = new ArrayList<String>();
                        //genomes.add(t, ts) //probably unnecesary
                    }

                    StringBuilder sb = new StringBuilder();

                    for (int p = 0; p < b.length; p++) {
                        if (b[p] == true) {
                            sb.append("1");
                        } else {
                            sb.append("0");
                        }
                    }

                    String ss = sb.toString();

                    //Adds this Strategy to it's teams ArrayList of strategies in the hashmap
                    boolean exists = false;
                    //for (strategy : ts) {

                    for (int p = 0; p < ts.size(); p++) {
                        if (ts.get(p) == ss) exists = true;
                    }

                    if (!exists) {
                        ts.add(ss);
                        genomes.put(t,ts);
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < Dilemma.NO_OF_TEAMS; i++) {
                g.drawString("Team: " + Integer.toString(i),1+110*i,20);
                String s = Double.toString(Dilemma.getTeamScores()[i]/4);
                if (avgName.equals("Avg: ")) {
                    g.drawString(avgName + s.substring(0,Math.min(3,s.length()-1)),1+110*i,20+11);
                }
            }

            for (Map.Entry<Integer, ArrayList<String>> genomeEntry : genomes.entrySet()) {
                int team = genomeEntry.getKey();
                ArrayList<String> strategies = genomeEntry.getValue();


                final Map<String, Integer> counter = new HashMap<String, Integer>();
                for (String str : strategies)
                    counter.put(str, 1 + (counter.containsKey(str) ? counter.get(str) : 0));

                List<String> list = new ArrayList<String>(counter.keySet());
                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String x, String y) {
                        return counter.get(y) - counter.get(x);
                    }
                });

                int ti = 1;
                for (String oneStrategy : list) {
                    ti++;
                    g.drawString(oneStrategy + ": " + counter.get(oneStrategy),1+110*team,20+ti*11);
                }
            }
        }


    }

    public static class Grid extends JPanel {

        public Grid() {}

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < 64; i++) {
                for (int j = 0; j < 64; j++) {
                    if(avgName.equals("Avg: ")) {
                        g.setColor(genomes[i][j]);
                        g.fillRect(10+i*10,10+j*10,10,10);
                    
                        g.setColor(teams[i][j]);
                        g.fillRect(12+i*10,12+j*10,7,7);
                    } else {
                        g.setColor(teams[i][j]);
                        g.fillRect(10+i*10,10+j*10,10,10);
                    }
                }
            }

            g.setColor(Color.BLACK);
            g.drawString("Round: " + Integer.toString(roundTime),10,10);
            g.drawRect(10, 10, 640, 640);

            for (int i = 10; i <= 640; i += 10) {
                g.drawLine(i, 10, i, 650);
            }

            for (int i = 10; i <= 640; i += 10) {
                g.drawLine(10, i, 650, i);
            }
        }

        public void fillCell(int x, int y, Genome g) {

            teams[x][y] = g.color();
            List<Boolean> strategy = g.getStrategy();
            byte b;
            int val = 0;
            for (int i = 0; i < strategy.size(); i++) {
                b = strategy.get(strategy.size()-1-i) ? (byte)1 : (byte)0;
                val |= (b << i);
            }
            genomes[x][y] = dc[val%dc.length];

            repaint();
        }

    }

    public void paint(Genome[][] lattice ) {
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                grid.fillCell(i, j, lattice[i][j]);
            }
         }

        info.setGenomes(lattice);
        info.repaint();
        grid.repaint();
    }

    public PaintLattice () {
        grid = new Grid();
        info = new InfoPanel();

        info.setPreferredSize(new Dimension((1400-665), 680));

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JPanel botPanel = new JPanel(new GridLayout(1,1));
                JButton fastest = new JButton("Fastest");
                JButton fast = new JButton("Fast");
                JButton slow  = new JButton("Slow");
                JButton reset = new JButton("Reset");
                JButton close = new JButton("Close");
                JButton pause = new JButton("Pause");
                JButton step = new JButton("Step");
                JButton print = new JButton("Print");

                reset.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.reset();
                        Dilemma.step();//Makes the reset instant if game is paused
                    }
                });
                fast.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.fast();
                    }
                });
                slow.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.slow();
                    }
                });
                fastest.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.fastest();
                    }
                });
                close.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.exit();
                    }
                });
                pause.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.pause();
                    }
                });
                step.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.step();
                    }
                });
                print.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.togglePrint();
                    }
                });

                botPanel.add(slow);
                botPanel.add(fast);
                botPanel.add(fastest);
                botPanel.add(reset);
                botPanel.add(pause);
                botPanel.add(step);
                botPanel.add(print);
                botPanel.add(close);

                JPanel sim = new JPanel(new BorderLayout(1,1));
                sim.add(grid, BorderLayout.CENTER);
                sim.add(botPanel, BorderLayout.SOUTH);

                JPanel genomePanel = new JPanel(new GridLayout(1,1));
                genomePanel.setBackground(Color.BLACK);

                JPanel infoAndGenomes = new JPanel(new BorderLayout(1,1));
                infoAndGenomes.add(info, BorderLayout.CENTER);
                infoAndGenomes.add(genomePanel, BorderLayout.SOUTH);

                JButton original   = new JButton("Original");
                JButton average    = new JButton("Average");
                JButton addaverage = new JButton("Added Average");
                JButton mistake    = new JButton("Mistake Rate");
                JButton mutation   = new JButton("Mutation Rate");

                original.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.playWithOriginal();
                        avgName = "";
                    }
                });
                average.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.playWithAverage();
                        avgName = "Avg: ";
                    }
                });
                addaverage.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Dilemma.playWithAddedAverage();
                        avgName = "Avg: ";
                    }
                });

                mistake.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        String r, curr;
                        curr = String.valueOf(Dilemma.getMistake());
                        r = JOptionPane.showInputDialog("Set mistake rate, currently @: " + curr);
                        if (r != null && !r.isEmpty()) {
                            Dilemma.setMistake(Double.parseDouble(r));
                        } else {
                            Dilemma.setMistake(Double.parseDouble(curr));
                        }
                    }
                });
                mutation.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        String r, curr;
                        curr = String.valueOf(Dilemma.getMutate());
                        r = JOptionPane.showInputDialog("Set mutate probability, currently @: " + curr);
                        if (r != null && !r.isEmpty()) {
                            Dilemma.setMutate(Double.parseDouble(r));
                        } else {
                            Dilemma.setMutate(Double.parseDouble(curr));
                        }
                    }
                });

                genomePanel.add(original);
                genomePanel.add(average);
                genomePanel.add(addaverage);
                genomePanel.add(mistake);
                genomePanel.add(mutation);

                JPanel content = new JPanel(new BorderLayout(1,1));
                content.add(sim, BorderLayout.CENTER);
                content.add(infoAndGenomes, BorderLayout.EAST);

                JFrame window = new JFrame();
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                window.getContentPane().add(content);
                window.pack();
                window.setSize(1400, 710);
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            }
        });

    }



}
