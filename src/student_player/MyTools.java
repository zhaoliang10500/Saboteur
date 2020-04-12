package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import static Saboteur.SaboteurBoardState.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class MyTools {
  
  static SaboteurTile[][] boardAfterWePutMove;
  static double[][] objectives = {{hiddenPos[0][0],hiddenPos[0][1],0.33},
      {hiddenPos[1][0],hiddenPos[1][1],0.33},
      {hiddenPos[2][0],hiddenPos[2][1],0.33}};
  static SaboteurMove opponentMove;
  static boolean needToSteal = false;
  
 
  
  static void storeBoard (SaboteurBoardState boardState, SaboteurMove myMove) {
    boardAfterWePutMove = boardState.getHiddenBoard();
    if (myMove.getCardPlayed() instanceof SaboteurTile) {
      boardAfterWePutMove[myMove.getPosPlayed()[0]][myMove.getPosPlayed()[1]] = (SaboteurTile) myMove.getCardPlayed();
    }
  }
  
  
  static void setup (SaboteurBoardState boardState) {
    boardAfterWePutMove = boardState.getHiddenBoard();
  }
  
  
  static SaboteurMove identifyOpponentMove(SaboteurBoardState boardState) {
    
    SaboteurTile[][] After_Opponent_Move = boardState.getHiddenBoard();
    
    for (int y = 0; y < After_Opponent_Move.length; y++) {
      for (int x = 0; x < After_Opponent_Move[0].length; x++) {
        if (After_Opponent_Move[y][x] != boardAfterWePutMove[y][x]) {
          if (After_Opponent_Move[y][x]!= null && boardAfterWePutMove[y][x]== null) {
            //opponent placed a tile
            opponentMove = new SaboteurMove(After_Opponent_Move[y][x], y, x, 0);
            return opponentMove;
          }
          else if (After_Opponent_Move[y][x] == null && boardAfterWePutMove[y][x] != null) {
            //opponent placed a destroy
            opponentMove = new SaboteurMove(new SaboteurDestroy(), y, x, 0);
            return opponentMove;
          }
          else if (After_Opponent_Move[y][x].getIdx().equals("nugget")) {
            //found nugget
            for (int i = 0; i < objectives.length; i++) {
              if (objectives[i][0] == y && objectives[i][1] == x) {
                updateObjectivesState(i, true);
                break;
              }
            }
          }
          else if (After_Opponent_Move[y][x].getIdx().contains("hidden")) {
            //discover a hidden objective as an tunnel
            for (int i = 0; i < objectives.length; i++) {
              if (objectives[i][0] == y && objectives[i][1] == x && objectives[i][2] != 0) {
                updateObjectivesState(i, false);
                System.out.println("No nugget");
                break;
              }
            }
          }
          else {
            //opponent put a tile
            opponentMove = new SaboteurMove(new SaboteurMap(), y, x, 0);
            System.out.println("Opponent move:" + opponentMove.getCardPlayed() + " Mymovesdsda " + opponentMove.getPosPlayed()[0] + opponentMove.getPosPlayed()[1]);
            return opponentMove;
          }
        }
      }
    }
    return null;
   } 
  
  
  public static void updateObjectivesState(int index, boolean foundNugget) {
    if (foundNugget) {
      objectives[index][2] = 1.0;
    }
    else {
      objectives[index][2] = 0;
    }
  }
  
  
  public static boolean nuggetKnown() {
    boolean findNugget = false;
    for (int i = 0; i < objectives.length; i++) {
      if (objectives[i][2] == 1.0) {
        findNugget = true;
      }
    }
    return findNugget;
  }
  
  
  static double assignMoveValue(int[][] theBoardMap) {
    //deduce the most likely location of the gold
    int[] objective = currentObjective();
    Point goal = new Point(objective[1]*3+1,objective[0]*3+1);
    
    ArrayList<Point> goals = new ArrayList<Point>();
    for (int i = 0; i < objectives.length; i++) {
      Point aGoal = new Point((int)objectives[i][1]*3+1,(int)objectives[i][0]*3+1);
      goals.add(aGoal);
    }
    
    ArrayList<Point> tunnels = new ArrayList<Point>();
    int[] entrance = {originPos*3+1,originPos*3+1};
    
    int[][] searched = new int[theBoardMap.length][theBoardMap[0].length];
    Stack<int[]> stack = new Stack<int[]>();
    stack.push(entrance);
    while (!stack.empty()) {
      int[] current = stack.pop();
      searched[current[0]][current[1]] = 1;
      //point = {y,x}. y is inverted(positive y is south), x direction is not inverted
      int[] south = {current[0]+1,current[1]};
      int[] north = {current[0]-1,current[1]};
      int[] east = {current[0],current[1]+1};
      int[] west = {current[0],current[1]-1};
      int[][] neighbors = {north,south,east,west};
      
      //check to see if neighbors have a -1 (empty)
      for (int i = 0; i < neighbors.length; i++) {
        
        if (theBoardMap[current[0]][current[1]] == 1 && theBoardMap[neighbors[i][0]][neighbors[i][1]] == -1) {
          Point tunnel = new Point(current[1], current[0]);
          tunnels.add(tunnel);
        }
        
        if (theBoardMap[neighbors[i][0]][neighbors[i][1]] == 1 && searched[neighbors[i][0]][neighbors[i][1]] != 1) {
          stack.push(neighbors[i]);
        }
        
      }
    }
    //find tunnel point with smallest distance to our current goal
    double smallestDistance = 999;
    double[] distances= new double[objectives.length];
    for (int i = 0; i < distances.length; i++) {
      distances[i] = 999;
    }
    for (int i = 0; i < tunnels.size(); i++) {
      for (int j = 0; j < distances.length; j++) {
        double tmp = goals.get(j).distance(tunnels.get(i));
        if (tmp < distances[j]) {
          distances[j] = tmp;
        }
      }
      //get shortest distance to current goal
      double dist = goal.distance(tunnels.get(i));
      if (dist < smallestDistance) {
        smallestDistance = dist;
      }
    }
    return smallestDistance;
  }
  
  
  
  
  static int[] currentObjective() {
    double tmp = -1;
    int index = 0;
    for (int i = 0; i < objectives.length; i++) {
      if (objectives[i][2] > tmp) {
        tmp = objectives[i][2];
        index = i;
      }
    }
    return new int[] {(int) objectives[index][0], (int) objectives[index][1]};
  }
  
  
  static SaboteurMove stealing(SaboteurBoardState boardState, ArrayList<SaboteurMove> tileMoves, SaboteurMove opponentMove) {
    int[] pos = opponentMove.getPosPlayed();
   
    for (int i = 0; i < hiddenPos.length; i++) {
      if (opponentMove.getCardPlayed() instanceof SaboteurTile) {
        
        if (pos[0] == objectives[i][0] && pos[1] == objectives[i][1] - 2) {
          return putStealingMove (pos, i, tileMoves, 0, -1, "10", "8", "9");
        }
        else if (pos[0] == objectives[i][0] && pos[1] == objectives[i][1] + 2) {
          return putStealingMove (pos, i, tileMoves, 0, 1, "10", "8", "9");
        }
        else if (pos[0] == objectives[i][0] - 2 && pos[1] == objectives[i][1]) {
          return putStealingMove (pos, i, tileMoves, -1, 0, "0", "8", "6");
        }
        else if (pos[0] == objectives[i][0] + 2 && pos[1] == objectives[i][1]) {
          return putStealingMove (pos, i, tileMoves, 1, 0, "0", "8", "6");
        }
        else if (pos[0] == objectives[i][0] + 1 && pos[1] == objectives[i][1] + 1) {
          SaboteurMove move = null;
          move = putStealingMove (pos, i, tileMoves, 0, 1, "9", "8", "6");
          if (move != null) {
            return move;
          } 
          move = putStealingMove (pos, i, tileMoves, 0, 1, "7", "ooo", "ooo");
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "6", "7", "8");
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "9", "ooo", "ooo");
          return move;
        }
        else if (pos[0] == objectives[i][0] + 1 && pos[1] == objectives[i][1] - 1) {
          SaboteurMove move = null;
          move = putStealingMove (pos, i, tileMoves, 0, -1, "9", "8", "6");
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 0, -1, "5", "ooo", "ooo");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "5", "6", "8");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "9", "ooo", "ooo");
          
          return move;
        }
        else if (pos[0] == objectives[i][0] - 1 && pos[1] == objectives[i][1] + 1) {
          SaboteurMove move = null;
          move = putStealingMove (pos, i, tileMoves, -1, 0, "9", "8", "6");
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, -1, 0, "5", "ooo", "ooo");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 0, 1, "6", "5", "8");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 0, 1, "9", "ooo", "ooo");
          
          return move;
        }
        else if (pos[0] == objectives[i][0] - 1 && pos[1] == objectives[i][1] - 1) {
          SaboteurMove move = null;
          move = putStealingMove (pos, i, tileMoves, 0, -1, "9", "8", "6");
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 0, -1, "7", "ooo", "ooo");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "6", "7", "8");
          
          if (move != null) {
            return move;
          }
          move = putStealingMove (pos, i, tileMoves, 1, 0, "9", "ooo", "ooo");
          
          return move;
        }
      }
      
    }
    return null;
  }
  
  
  static SaboteurMove putStealingMove (int[] pos, int i, ArrayList<SaboteurMove> tileMoves, int y, int x, String tile1, String tile2, String tile3) {
      for (int j = 0; j < tileMoves.size(); j++) {
        if ((tileMoves.get(j).toPrettyString().contains(tile1) &&
                      tileMoves.get(j).getPosPlayed()[0] == (objectives[i][0] + y) &&
                      tileMoves.get(j).getPosPlayed()[1] == (objectives[i][1] + x)) || 
            (tileMoves.get(j).toPrettyString().contains(tile2) &&
                      tileMoves.get(j).getPosPlayed()[0] == (objectives[i][0] + y) &&
                      tileMoves.get(j).getPosPlayed()[1] == (objectives[i][1] + x)) ||
            (tileMoves.get(j).toPrettyString().contains(tile3) &&
                      tileMoves.get(j).getPosPlayed()[0] == (objectives[i][0] + y) &&
                      tileMoves.get(j).getPosPlayed()[1] == (objectives[i][1] + x))) {
          return tileMoves.get(j);
        }
      }
    return null;
  }
  
  

  
  static double AnalyseMoveValue(int[][] theBoardMap, SaboteurMove move) {
    int[] movePosition = move.getPosPlayed(); 
    
    SaboteurCard card = move.getCardPlayed();
    
    String[] tmp = card.getName().split(":");
    int[][] path = formatCard(SaboteurTile.initializePath(tmp[1]));
    int[] point = new int[2];
    
    for (int y = movePosition[0]*3; y < movePosition[0]*3+3; y++) {
      for (int x = movePosition[1]*3; x < movePosition[1]*3+3; x++) {
        theBoardMap[y][x] = path[y%3][x%3];
        if (theBoardMap[y][x] == 1 ) {
          point[0] = y;
          point[1] = x;
        }
      }
    }
    //check if a path to the entrance exists after the current move.
    int[] entrance = {originPos*3+1, originPos*3+1};
    ArrayList<int[]> entranceArrayList = new ArrayList<int[]>();
    entranceArrayList.add(entrance);
    double assignMoveValue;
    //if a path exists from the move to the entrance
   
    if (checkPath(theBoardMap,point,entranceArrayList)) {
      assignMoveValue = assignMoveValue(theBoardMap);//check assignMoveValue normally
    }
    else { //if the path is broken
      assignMoveValue = 999; // don't consider the move
    }
    return assignMoveValue;
  }
  
 
  
   static int[][] formatCard(int[][] card) {
    // before: card = {{0,1,0},{0,1,1},{0,0,0}}
    // after:  card = {{0,1,0},{1,1,0},{0,0,0}}
    int[][] formatted = new int[3][3];
    //////////////////////////////
    formatted[0][0] = card[0][2];
    formatted[0][1] = card[1][2];
    formatted[0][2] = card[2][2];
    //////////////////////////////
    formatted[1][0] = card[0][1];
    formatted[1][1] = card[1][1];
    formatted[1][2] = card[2][1];
    ///////////////////////////////
    formatted[2][0] = card[0][0];
    formatted[2][1] = card[1][0];
    formatted[2][2] = card[2][0];
    return formatted;
   }
  
  private static Boolean checkPath(int[][] theBoardMap, int[] point, ArrayList<int[]> entrance){ //theBoardMap,point,entrance
    // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
    ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
    ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
    visited.add(point);
    addUnvisitedNeighborToQueue(theBoardMap, point,queue,visited,BOARD_SIZE*3);
    while(queue.size()>0){
        int[] visitingPos = queue.remove(0);
        if(containsIntArray(entrance,visitingPos)){
            return true;
        }
        visited.add(visitingPos);
        addUnvisitedNeighborToQueue(theBoardMap, visitingPos,queue,visited,BOARD_SIZE*3);
        System.out.println(queue.size());
    }
    return false;
}
  
  
  private static void addUnvisitedNeighborToQueue(int[][] theBoardMap, int[] pos,ArrayList<int[]> queue, ArrayList<int[]> visited,int maxSize){
    int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
    int i = pos[0];
    int j = pos[1];
    for (int m = 0; m < 4; m++) {
        if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
            int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
            if(!containsIntArray(visited,neighborPos)){
                if(theBoardMap[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
            }
        }
    }
}
  private static boolean containsIntArray(ArrayList<int[]> a,int[] o){ //the .equals used in Arraylist.contains is not working between arrays..
    if (o == null) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) == null)
                return true;
        }
    } else {
        for (int i = 0; i < a.size(); i++) {
            if (Arrays.equals(o, a.get(i)))
                return true;
        }
    }
    return false;
}
  
  

  
  
  
  
  
  
}