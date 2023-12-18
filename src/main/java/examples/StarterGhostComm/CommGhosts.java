package examples.StarterGhostComm;

import com.fossgalaxy.object.annotations.ObjectDef;
import java.util.EnumMap;
import pacman.controllers.MASController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;

public class CommGhosts extends MASController {
   public CommGhosts() {
      this(50, "default");
   }

   @ObjectDef("CommGhosts")
   public CommGhosts(int TICK_THRESHOLD, String ghostAI) {
      super(true, new EnumMap<>(Constants.GHOST.class));
      switch (ghostAI.toLowerCase()) {
         case "astar":
               this.controllers.put(GHOST.BLINKY, new POAStarGhost(GHOST.BLINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.INKY, new POAStarGhost(GHOST.INKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.PINKY, new POAStarGhost(GHOST.PINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.SUE, new POAStarGhost(GHOST.SUE, TICK_THRESHOLD));
            break;
         
         case "bfs":
               this.controllers.put(GHOST.BLINKY, new POBFSGhost(GHOST.BLINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.INKY, new POBFSGhost(GHOST.INKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.PINKY, new POBFSGhost(GHOST.PINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.SUE, new POBFSGhost(GHOST.SUE, TICK_THRESHOLD));
            break;
         
         default:
               this.controllers.put(GHOST.BLINKY, new POCommGhost(GHOST.BLINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.INKY, new POCommGhost(GHOST.INKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.PINKY, new POCommGhost(GHOST.PINKY, TICK_THRESHOLD));
               this.controllers.put(GHOST.SUE, new POCommGhost(GHOST.SUE, TICK_THRESHOLD));
            break;
      }
   }

   public String getName() {
      return "CommGhosts";
   }
}
