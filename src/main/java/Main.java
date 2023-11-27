
import java.util.EnumMap;

import examples.StarterGhostComm.Blinky;
import examples.StarterGhostComm.Inky;
import examples.StarterGhostComm.Pinky;
import examples.StarterGhostComm.Sue;
import examples.StarterPacMan.DijkstraPacMan;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.GHOST;
import pacman.game.internal.POType;

public class Main {

    public static void main(String[] args) {

        int sightRadius = 10; // 5000 is maximum

        Executor executor = new Executor.Builder()
                .setVisual(true)
                .setPacmanPO(false)
                .setTickLimit(20000)
                .setScaleFactor(2) // Increase game visual size
                .setPOType(POType.RADIUS) // pacman sense objects around it in a radius wide fashion instead of straight
                                          // line sights
                .setSightLimit(sightRadius) // The sight radius limit, set to maximum
                .build();

        EnumMap<GHOST, IndividualGhostController> controllers = new EnumMap<>(GHOST.class);

        controllers.put(GHOST.INKY, new Inky());
        controllers.put(GHOST.BLINKY, new Blinky());
        controllers.put(GHOST.PINKY, new Pinky());
        controllers.put(GHOST.SUE, new Sue());

        int speed = 1; // smaller number will run faster

        MASController ghosts = new POCommGhosts(50);

        // executor.runGame(new AStarPacMan(), ghosts, speed);
        executor.runGame(new DijkstraPacMan(), ghosts, speed);
        // executor.runGame(new TreeSearchPacMan(), ghosts, speed);

    }
}
