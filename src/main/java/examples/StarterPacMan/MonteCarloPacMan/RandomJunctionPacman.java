package examples.StarterPacMan.MonteCarloPacMan;

import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class RandomJunctionPacman extends Controller<MOVE>
{
	
	public MOVE getMove(Game game, long timeDue) {
		
		if (MCTS.junctions == null)
			MCTS.junctions = MCTS.getJunctions(game);
			
		
		MOVE lastMove = game.getPacmanLastMoveMade();
		
		if (inJunction(game))
			return randomAction(lastMove);
		else
			return lastMove;
		
	}
	
	private boolean inJunction(Game game) {
		return MCTS.junctions.contains(game.getPacmanCurrentNodeIndex());
	}

	private MOVE randomAction(MOVE except) {
		MOVE move = null;
		
		while(move == null){
			int random = (int) (Math.random() * 4);
			
			switch(random){
			case 0: move = MOVE.UP; break;
			case 1: move = MOVE.RIGHT; break;
			case 2: move = MOVE.DOWN; break;
			case 3: move = MOVE.LEFT; break;
			}
			
			if (move == except)
				move = null;
			
		}
		
		return move;
	}

	
	
	

}