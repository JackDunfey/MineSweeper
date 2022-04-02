package jib.minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * JavaFX App
 */

public class App extends Application {

  public static final double WIDTH = 600;
  public static final double HEIGHT = 600;
  public static final int ROWS = 10;
  public static final int COLS = 10;
  public static final boolean WRAP = true;

  @Override
  public void start(Stage stage) {
    stage.setTitle("Minesweeper!");
    var root = new BorderPane();
    var grid = new Grid(ROWS, COLS, WIDTH, HEIGHT);
    grid.draw();
    root.setCenter(grid);
//    var controls = new VBox(10);
//    var resetBtn = new Button("Reset");
//    controls.getChildren().add(resetBtn);
//    root.setRight(controls);
//    stage.setScene(new Scene(root, App.WIDTH + 300, App.HEIGHT));
    stage.setScene(new Scene(root, App.WIDTH, App.HEIGHT));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

class Grid extends Pane {
  private final double BOMB_RATE = 0.05;
  private final double WIDTH;
  private final double HEIGHT;
  private final int BOMBS;
  private int rows;
  private int cols;
  private double w;
  private double h;
  private int[][] board;
  private boolean[][] visible;
  private ArrayList<Integer[]> flags;
  private int game = 2;
  
  public static List<Integer> makeList(int first, int second){
    return Arrays.asList(first, second);
  }
  
  public Grid(int rows, int cols, double w, double h) {
    this.rows = rows;
    this.cols = cols;
    this.WIDTH = w;
    this.HEIGHT = h;
    this.w = this.WIDTH/this.cols;
    this.h = this.HEIGHT/this.rows;
    this.BOMBS = (int)(BOMB_RATE * this.rows*this.cols);
//    this.BOMBS = 5;
    this.board = new int[this.rows][this.cols];
    this.visible = new boolean[this.rows][this.cols];
    this.flags = new ArrayList<Integer[]>(0);
    
    this.setOnMouseClicked(event -> {
      int clickX = (int)(event.getX()/this.w);
      int clickY = (int)(event.getY()/this.h);
      if((clickX < 0 || clickX >= this.cols) || (clickY < 0 || clickY >= this.rows))
        return;
      if(event.getButton() == MouseButton.PRIMARY){
        if(this.game != 0)
          this.initialize(clickX, clickY);
        else if(!this.visible[clickY][clickX]){
          this.visit(clickY,clickX);
          if (hasWon()){
            this.game = 1;
          }
        }
      } else if (event.getButton() == MouseButton.SECONDARY){
        this.flag(clickY, clickX);
      }
      this.draw();
    });
  }
  public Grid(int rows, int cols) {
    this(rows, cols, rows * 20, cols * 20);
  }
  
  private void gameOver(){
    for(int i = 0; i < this.board.length; i++)
      for(int j = 0; j < this.board[i].length; j++)
        this.visible[i][j] = true;
    this.game = -1;
  }
  private boolean hasWon(){
    if(this.game != 0) return false;
    for(int i = 0; i < this.visible.length; i++)
      for(int j = 0; j < this.visible[i].length; j++)
        if(!this.visible[i][j] && this.board[i][j] != 9)
          return false;
    return true;
  }
  public int countNeighbors(int y, int x){
    int count = 0;
    //Top L->R
    if(y-1 >= 0){
      if(x-1 >= 0 && this.board[y-1][x-1] == 9)
        count++;
      if(this.board[y-1][x] == 9)
        count++;
      if(x+1 < this.cols && this.board[y-1][x+1] == 9)
        count++;
    }
    //Left->Right
    if(x-1 >= 0 && this.board[y][x-1] == 9)
      count++;
    if(x+1 < this.cols && this.board[y][x+1] == 9)
      count++;
    //Down L->R
    if(y+1 < this.rows){
      if(x-1 >= 0 && this.board[y+1][x-1] == 9)
        count++;
      if(this.board[y+1][x] == 9)
        count++;
      if(x+1 < this.cols && this.board[y+1][x+1] == 9)
        count++;
    }
    return count;
  }
  private void visit(int y, int x){
    if(this.board[y][x] == 9){
      gameOver();
      return;
    }
    this.visible[y][x] = true;
    if(this.board[y][x] != 0)
      return;
    var toVisit = new ArrayList<List<Integer>>(0);
    if(y-1 >= 0){
      if(x-1 >= 0)
        toVisit.add(makeList(y-1,x-1));
      toVisit.add(makeList(y-1,x));
      if(x+1 < this.cols)
        toVisit.add(makeList(y-1,x+1));
    }
    if(x-1 >= 0)
      toVisit.add(makeList(y,x-1));
    if(x+1 < this.cols)
      toVisit.add(makeList(y,x+1));
    if(y+1 < this.rows){
      if(x-1 >= 0)
        toVisit.add(makeList(y+1,x-1));
      toVisit.add(makeList(y+1,x));
      if(x+1 < this.cols)
        toVisit.add(makeList(y+1,x+1));
    }
    /*
    IDE converted the below automatically!!
    for(List<Integer> coords : toVisit)
      if(!this.visible[coords.get(0)][coords.get(1)])
        visit(coords.get(0),coords.get(1));
    */
    toVisit.stream().filter(coords -> (!this.visible[coords.get(0)][coords.get(1)])).forEachOrdered(coords -> {
      visit(coords.get(0),coords.get(1));
    });
  }
  public void initialize(int clickX, int clickY){
    this.game = 0;
    if(clickX > this.rows-1 || clickY > this.rows-1) return;
    //Setup Board
    do {
      for(int i = 0; i < this.rows; i++)
        for(int j = 0; j < this.cols; this.visible[i][j] = false, this.board[i][j] = 0, j++)
          this.flags.clear();
      //Make bombs
      for(int i = 0; i < this.BOMBS;){
        int newY = (int)(this.rows*Math.random());
        int newX = (int)(this.cols*Math.random());
        if(newY == clickY && newX == clickX) continue;
        this.board[newY][newX] = 9;
        i++;
      }
    } while(this.countNeighbors(clickY, clickX) != 0);
    //Set values
    for(int y = 0; y < this.rows; y++){
      for(int x = 0; x < this.cols; x++){
        if(this.board[y][x] == 9)
          continue;
        this.board[y][x] = this.countNeighbors(y,x);
      }
    }
    //Set visibility
    visit(clickY,clickX);
  }
  public void flag(int y, int x){
    if(this.visible[y][x]) return;
    boolean addFlag = true;
    for(int i = 0; i < this.flags.size(); i++){
      if(this.flags.get(i)[0] == y && this.flags.get(i)[1] == x){
        this.flags.remove(i);
        addFlag = false;
      }
    }
    if(addFlag)
      this.flags.add(new Integer[]{y,x});
  }
  
  public int getRows(){
    return this.rows;
  }
  public int getCols(){
    return this.cols;
  }
  public int[][] getBoard(){
    return this.board;
  }
  
  private void paint() {
    this.getChildren().clear();
    for(int r = 0; r < this.rows; r++) {
      for(int c = 0; c < this.cols; c++) {
        if(this.visible[r][c] == false)
          continue;
        var rect = new Rectangle(c*this.w,r*this.h, this.w, this.h);
        rect.setFill(Color.GRAY);
        this.getChildren().add(rect);
        if(this.board[r][c] >= 1 && this.board[r][c] <= 8){
          var text = new Text(String.valueOf(this.board[r][c]));
          text.setX((c+0.5)*this.w);
          text.setY((r+0.5)*this.h);
          this.getChildren().add(text);
        } else if(this.board[r][c] == 9){
          var circle = new Circle((c+0.5)*this.w,(r+0.5)*this.h, this.w/2); //Change to ellipse
          this.getChildren().add(circle);
        }
      }
    }
    for(int i = 0; i < this.flags.size(); i++){
      if(this.visible[this.flags.get(i)[0]][this.flags.get(i)[1]]){
        this.flags.remove(i);
        continue;
      }
      var flag = new Rectangle((this.flags.get(i)[1]+0.25)*this.w,(this.flags.get(i)[0]+0.25)*this.h, this.w/2, this.h/2);
      flag.setFill(Color.RED);
      this.getChildren().add(flag);
    }
    for(int r = 0; r < this.rows+1; r++)
      this.getChildren().add(new Line(0,this.h*r,this.WIDTH,this.h*r));
    for(int c = 0; c < this.cols+1; c++)
      this.getChildren().add(new Line(this.w*c,0,this.w*c,this.HEIGHT));
    if(this.game % 2 == 0)
      return;
    this.getChildren().clear();
    var text = new Text(this.game == 1 ? "You Won! :)" : "You Lost :(");
    text.setFont(new Font(60));
    var pane = new StackPane(text);
    pane.setPrefSize(this.WIDTH, this.HEIGHT);
    StackPane.setAlignment(text, Pos.CENTER);
    pane.setStyle("-fx-background-color: coral;");
    this.getChildren().add(pane);
  }
  public void draw(){
    this.paint();
  }
  
  @Override
  public String toString(){
    String[][] b = new String[this.rows][this.cols];
    for(int i = 0; i < this.rows; i++)
      for(int j = 0; j < this.cols; j++)
        b[i][j] = String.valueOf(this.board[i][j]);
    String out = "";
    for (String[] row : b)
      out += String.join(" ", row) + "\n";
    return out;
  }
}