package student_player;

import boardgame.Move;
import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import static Saboteur.SaboteurBoardState.*;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260781081");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    boolean isFirstMove = true;
    public Move chooseMove(SaboteurBoardState boardState) {
    
      //check if it is the first move
      if (boardState.firstPlayer() == player_id && isFirstMove) {
        MyTools.setup(boardState);
        isFirstMove = false;
      }
      //deduce the opponent's move
      MyTools.identifyOpponentMove(boardState);
      
      //steal the object.
      MyTools.stealing(boardState);
      
      //get all legal moves here.
      ArrayList<SaboteurMove> allLegalMoves = boardState.getAllLegalMoves();
      SaboteurMove myMove = null;
      
      //check what kind of moves we can process.
      ArrayList<SaboteurMove> tileMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> mapMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> destroyMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> bonusMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> malusMoves = new ArrayList<SaboteurMove>();
      ArrayList<SaboteurMove> dropMoves = new ArrayList<SaboteurMove>();
      
      //check the end points of existing connected path.
      MyTools.checkExistingConnectedPath(boardState);
      
      //specify the current goal object.
      int[] goal = MyTools.currentObjective();
      System.out.println("current goal: " + goal[0] + ", " + goal[1]);
      
      System.out.println("AI cards: ");
      ArrayList<SaboteurCard> cards = boardState.getCurrentPlayerCards();
      
      for (int i = 0; i < cards.size(); i++) {
        System.out.println(cards.get(i).getName());
      }
      
      for (int i = 0; i < allLegalMoves.size(); i++) {
        if (allLegalMoves.get(i).toPrettyString().contains("Tile")) {
          tileMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Map")) {
          mapMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Destroy")) {
          destroyMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Bonus")) {
          bonusMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Malus")) {
          malusMoves.add(allLegalMoves.get(i));
        }
        else if (allLegalMoves.get(i).toPrettyString().contains("Drop")) {
          dropMoves.add(allLegalMoves.get(i));
        }
      }
      
      //prioritize the use of map cards.
      if (mapMoves.size() > 1 && !MyTools.nuggetKnown()) {
        for (int i = 0; i < mapMoves.size(); i++) {
          int[] mapPosition = mapMoves.get(i).getPosPlayed(); // Y X
          int[] objectPosition = MyTools.currentObjective();  // Y X
          if (mapPosition[0] == objectPosition[0] && mapPosition[1] == objectPosition[1]) {
            myMove = mapMoves.get(i);
            System.out.println("playing map");
            return myMove;
          }
        }
      }
      //we don't use destroy for this game.
      else if (!destroyMoves.isEmpty()) {
        
      }
      else if (!bonusMoves.isEmpty() && boardState.getNbMalus(player_id) > 0) {
        myMove = bonusMoves.get(0);
        return myMove;
      }
      else if (malusMoves.size() > 0) {
        myMove = malusMoves.get(0);
        return myMove;
      }
      else {
        if (currentHeuristic <= 4) {
          System.out.println("relax the heuristic");
          currentHeuristic++;
        }
        //play the tile card with the smallest heuristic.
        for (int i = 0; i < tileMoves.size(); i++) {
          double tmp = myMove.simulateMove(bitMap, tileMoves.get(i));
          if (tmp < currentHeuristic) {
            currentHeuristic = tmp;
            myMove = tileMoves.get(i);
            return myMove;
          }
        }
      }
      //if AI does not choose any move, drop the move.
      if (myMove == null) {
        myMove = dropMoves.get(0);
      }
      
      MyTools.storeBoard(boardState, myMove);
      
      // Return your move to be processed by the server.
      return myMove;
    }
}