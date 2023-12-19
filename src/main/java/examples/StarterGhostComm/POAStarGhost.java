package examples.StarterGhostComm;

import pacman.controllers.IndividualGhostController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;


/**
 * Created by pwillic on 25/02/2016.
 */

public class POAStarGhost extends IndividualGhostController {
    private final static float CONSISTENCY = 0.9f;    //attack Ms Pac-Man with this probability
    private final static int PILL_PROXIMITY = 15;        //if Ms Pac-Man is this close to a power pill, back away
    Random rnd = new Random();
    private int TICK_THRESHOLD;
    private int lastPacmanIndex = -1;
    private int tickSeen = -1;

    public POAStarGhost(Constants.GHOST ghost) {
        this(ghost, 5);
    }

    public POAStarGhost(Constants.GHOST ghost, int TICK_THRESHOLD) {
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
                    if (rnd.nextFloat() < CONSISTENCY) {            //attack Ms Pac-Man otherwise (with certain probability)
                        try {
                            // Constants.MOVE move = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                            //         pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH);
                            Constants.MOVE move = aStarSearch(game.getGhostCurrentNodeIndex(ghost),
                                                            pacmanIndex, game, ghost);
                    
                            return move;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println(e);
                            System.out.println(pacmanIndex + " : " + currentIndex);
                        }
                    }
                }
            } else {
                Constants.MOVE[] possibleMoves = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
                return possibleMoves[rnd.nextInt(possibleMoves.length)];
            }
        }
        return null;
    }

    // A* Search Implementation with Heuristic Function
    public Constants.MOVE aStarSearch(int ghostCurrentNodeIndex, int pacmanIndex, Game game, Constants.GHOST ghost) {
        PriorityQueue<int[]> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(arr -> calculateAStarPriority(arr, pacmanIndex, game, ghost)));
        Set<Integer> explored = new HashSet<>();
        int[] neighbouringNodes = game.getNeighbouringNodes(ghostCurrentNodeIndex);
    
        for (int node : neighbouringNodes) {
            int priority = calculateAStarPriority(new int[]{node, node}, pacmanIndex, game, ghost);
            priorityQueue.add(new int[]{node, node, priority});
        }
    
        while (!priorityQueue.isEmpty()) {
            int[] current = priorityQueue.remove();
            int parentNode = current[0];
            int currentNode = current[1];
    
            if (!explored.contains(currentNode)) {
                explored.add(currentNode);
            }
    
            if (currentNode == pacmanIndex) {
                return game.getMoveToMakeToReachDirectNeighbour(ghostCurrentNodeIndex, parentNode);
            }
    
            for (int nextNode : game.getNeighbouringNodes(currentNode)) {
                if (nextNode != -1 && !explored.contains(nextNode)) {
                    int priority = calculateAStarPriority(new int[]{currentNode, nextNode}, pacmanIndex, game, ghost);
                    priorityQueue.add(new int[]{currentNode, nextNode, priority});
                }
            }
        }
    
        return null;
    }
    
    private int calculateAStarPriority(int[] nodes, int pacmanIndex, Game game, Constants.GHOST ghost) {
        int currentNode = nodes[1];
        int pacmanXCood = game.getNodeXCood(pacmanIndex);
        int pacmanYCood = game.getNodeYCood(pacmanIndex);
        int currentNodeXCood = game.getNodeXCood(currentNode);
        int currentNodeYCood = game.getNodeYCood(currentNode);
    
        // Calculate the Manhattan distance as the heuristic
        int deltaX = Math.abs(currentNodeXCood - pacmanXCood);
        int deltaY = Math.abs(currentNodeYCood - pacmanYCood);

        // The priority is the sum of the cost so far (g) and the heuristic (h)
        int gValue = nodes[0] == nodes[1] ? 0 : 1;  // g(n) - cost from start to current node
        return gValue + deltaX + deltaY;
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