package com.example.mazegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {
    private enum direction{
        UP,DOWN,LEFT,RIGHT
    }
    private Cell[][] cells;
    private Cell player,exit;
    private static final int COLS=7,ROWS=10;
    private static final float wall_thickness=4;
    private float cellSize, hMargin, vMargin;
    private Random random;
    private Paint wallpaint,playerpaint,exitpaint;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        wallpaint = new Paint();
        wallpaint.setColor(Color.BLACK);
        random = new Random();
        playerpaint = new Paint();
        playerpaint.setColor(Color.RED);
        exitpaint = new Paint();
        exitpaint.setColor(Color.BLUE);
        wallpaint.setStrokeWidth(wall_thickness);
        createMaze();
    }

    private Cell getNeighbour(Cell cell)
    {
        ArrayList<Cell> neighbours = new ArrayList<>();
        //left neighbour
        if (cell.col>0)
        if (!cells[cell.col-1][cell.row].visited)
            neighbours.add(cells[cell.col-1][cell.row]);
        //right neighbour
        if (cell.col<COLS-1)
            if (!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col+1][cell.row]);
        //top neighbour
        if (cell.row>0)
            if (!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);
        //bottom neighbour
        if (cell.row<ROWS-1)
            if (!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);
           if(neighbours.size()>0) {
               int index = random.nextInt(neighbours.size());
               return neighbours.get(index);
           }
           return null;
    }

    private void removewall(Cell current, Cell next)
    {
        if (current.col == next.col && current.row== next.row+1)
        {
            current.topwall=false;
            next.bottomwall=false;
        }
        if (current.col == next.col && current.row== next.row-1)
        {
            current.bottomwall=false;
            next.topwall=false;
        }
        if (current.col == next.col+1 && current.row== next.row)
        {
            current.leftwall=false;
            next.rightwall=false;
        }
        if (current.col == next.col-1&& current.row== next.row)
        {
            current.rightwall=false;
            next.leftwall=false;
        }

    }

    private void createMaze()
    {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                cells[x][y] = new Cell(x,y);
            }
        }
        player = cells[0][0];
        exit =  cells[COLS-1][ROWS-1];

        //Recursive stack tracker algorithim
        current = cells[0][0];
        next = getNeighbour(current);
        current.visited=true;

        do {
            if (next != null) {
                removewall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop();
            }
        }while (!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);
        int width = getWidth();
        int height= getHeight();
        if(width/height<COLS/ROWS)
            cellSize = width/(COLS+1);
        else
            cellSize = height/(ROWS+1);
        hMargin = (width-COLS*cellSize)/2;
        vMargin = (height-ROWS*cellSize)/2;
        canvas.translate(hMargin,vMargin);
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                if(cells[x][y].topwall)
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallpaint
                    );

            if(cells[x][y].leftwall)
                canvas.drawLine(
                        x*cellSize,
                        y*cellSize,
                        x*cellSize,
                        (y+1)*cellSize,
                        wallpaint
                );

        if(cells[x][y].bottomwall)
            canvas.drawLine(
                    x*cellSize,
                    (y+1)*cellSize,
                    (x+1)*cellSize,
                    (y+1)*cellSize,
                    wallpaint
            );

    if(cells[x][y].rightwall)
            canvas.drawLine(
                    (x+1)*cellSize,
                    y*cellSize,
                    (x+1)*cellSize,
                    (y+1)*cellSize,
                    wallpaint
                    );

            }
        }
        float margin = cellSize/10;
        canvas.drawRect(
                player.col*cellSize+margin,
                player.row*cellSize+margin,
                (player.col+1)*cellSize-margin,
                (player.row+1)*cellSize-margin,
                playerpaint
        );
        canvas.drawRect(
                exit.col*cellSize+margin,
                exit.row*cellSize+margin,
                (exit.col+1)*cellSize-margin,
                (exit.row+1)*cellSize-margin,
                exitpaint
        );
    }

    private void moveplayer(direction direction){
        switch (direction){
            case UP:
                if(!player.topwall)
                player = cells[player.col][player.row-1];
                break;
            case DOWN:
                if(!player.bottomwall)
                    player = cells[player.col][player.row+1];
                break;
            case LEFT:
                if(!player.leftwall)
                    player = cells[player.col-1][player.row];
                break;
            case RIGHT:
                if(!player.rightwall)
                    player = cells[player.col+1][player.row];
                break;
        }
        invalidate();
        checkexit();
    }
    private void checkexit(){
        if(player==exit)
            createMaze();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN)
                return true;
        if (event.getAction()==MotionEvent.ACTION_MOVE)
        {
            float x = event.getX();
            float y = event.getY();
            float playercentrex = hMargin + (player.col+0.5f)*cellSize;
            float playercentrey = vMargin + (player.row+0.5f)*cellSize;
            float dx = x-playercentrex;
            float dy = y-playercentrey;
            float absdx= Math.abs(dx);
            float absdy= Math.abs(dy);
            if (absdx>cellSize||absdy>cellSize)
            {
                if (absdx>absdy){
                    //move in x direction
                    if (dx>0)
                        moveplayer(direction.RIGHT);
                    else
                        moveplayer(direction.LEFT);
                }
                else
                {
                    //mov in y direction
                    if (dy>0)
                        moveplayer(direction.DOWN);
                        else
                        moveplayer(direction.UP);

                }
            }
        }
        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean
        topwall = true,
        leftwall = true,
        bottomwall = true,
        visited = false,
        rightwall = true;
        int col,row;
        public Cell(int col, int row)
        {
            this.col=col;
            this.row= row;
        }
    }
}
