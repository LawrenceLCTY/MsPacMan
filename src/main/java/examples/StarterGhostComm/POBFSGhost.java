package examples.StarterGhostComm;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Queue;
import java.util.Set;


/**
 * Created by pwillic on 25/02/2016.
 */

public class POBFSGhost extends IndividualGhostController {
    private final static float CONSISTENCY = 0.9f;    //attack Ms Pac-Man with this probability
    private final static int PILL_PROXIMITY = 15;        //if Ms Pac-Man is this close to a power pill, back away
    Random rnd = new Random();
    private int TICK_THRESHOLD;
    private int lastPacmanIndex = -1;
    private int tickSeen = -1;

    public POBFSGhost(Constants.GHOST ghost) {
        this(ghost, 5);
    }

    public POBFSGhost(Constants.GHOST ghost, int TICK_THRESHOLD) {
        super(ghost);
        this.TICK_THRESHOLD = TICK_THRESHOLD;
    }

    @Override
    public Constants.MOVE getMove(Game game, long timeDue) {
        // Housekeeping - throw out old info
        int currentTick = game.getCurrentLevelTime();
        if (currentTick <= 2 || currentTick - tickSeen >= TICK_THRESHOLD) {
            lastPacmanIndex = -1;
            tickSeen = -1;
        }

        // Can we see PacMan? If so tell people and update our info
        int pacmanIndex = game.getPacmanCurrentNodeIndex();
        int currentIndex = game.getGhostCurrentNodeIndex(ghost);
        Messenger messenger = game.getMessenger();
        if (pacmanIndex != -1) {
            lastPacmanIndex = pacmanIndex;
            tickSeen = game.getCurrentLevelTime();
            if (messenger != null) {
                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex, game.getCurrentLevelTime()));
            }
        }

        // Has anybody else seen PacMan if we haven't?
        if (pacmanIndex == -1 && game.getMessenger() != null) {
            for (Message message : messenger.getMessages(ghost)) {
                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer information
                        lastPacmanIndex = message.getData();
                        tickSeen = message.getTick();
                    }
                }
            }
        }
        if (pacmanIndex == -1) {
            pacmanIndex = lastPacmanIndex;
        }

        Boolean requiresAction = game.doesGhostRequireAction(ghost);
        if (requiresAction != null && requiresAction)        //if ghost requires an action
        {
            if (pacmanIndex != -1) {
                if (game.getGhostEdibleTime(ghost) > 0 || closeToPower(game))    //retreat from Ms Pac-Man if edible or if Ms Pac-Man is close to power pill
                {
                    try {
                        return game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
                                game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e);
                        System.out.println(pacmanIndex + " : " + currentIndex);
                    }
                } else {
                    try {
                        // Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                        //         pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                        Constants.MOVE move = breadthFirstSearch(game.getGhostCurrentNodeIndex(ghost),
                                                                pacmanIndex, 
                                                                game);
                        return move;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e);
                        System.out.println(pacmanIndex + " : " + currentIndex);
                    }
                }
            } else {
                Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
                return possibleMoves[rnd.nextInt(possibleMoves.length)];
            }
        }
        return null;
    }

    public Constants.MOVE breadthFirstSearch(int ghostCurrentNodeIndex, int pacmanIndex, Game game){
        Queue<int[]> queue = new LinkedList<>();
        Set<Integer> explored = new HashSet<>();
        int[] neighbouringNodes = game.getNeighbouringNodes(ghostCurrentNodeIndex);
        for (int node : neighbouringNodes) {
            queue.add(new int[] {node, node});
        }
        int pacmanXCood = game.getNodeXCood(pacmanIndex);
        int pacmanYCood = game.getNodeYCood(pacmanIndex);

        while (!queue.isEmpty()) {
            int[] current = queue.remove();
            int parentNode = current[0];
            int currentNode = current[1];
            if (!explored.contains(currentNode)) {
                explored.add(currentNode);
            }
            if (pacmanXCood == game.getNodeXCood(currentNode) && pacmanYCood == game.getNodeYCood(currentNode)) {
                return game.getMoveToMakeToReachDirectNeighbour(ghostCurrentNodeIndex, parentNode);
            }
            for (int nextNode: game.getNeighbouringNodes(currentNode)) {
                if (nextNode != -1 && !explored.contains(nextNode)){
                    queue.add(new int[] {parentNode, nextNode});
                }   
            }
            
        }

        return null;

    }

    //This helper function checks if Ms Pac-Man is close to an available power pill
    private boolean closeToPower(Game game) {
        int[] powerPills = game.getPowerPillIndices();

        for (int i = 0; i < powerPills.length; i++) {
            Boolean powerPillStillAvailable = game.isPowerPillStillAvailable(i);
            int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
            if (pacmanNodeIndex == -1) {
                pacmanNodeIndex = lastPacmanIndex;
            }
            if (powerPillStillAvailable == null || pacmanNodeIndex == -1) {
                return false;
            }
            if (powerPillStillAvailable && game.getShortestPathDistance(powerPills[i], pacmanNodeIndex) < PILL_PROXIMITY) {
                return true;
            }
        }

        return false;
    }
}