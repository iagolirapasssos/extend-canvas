package com.bosonshiggs.extendedcanvas.helpers;

import com.google.appinventor.components.runtime.Canvas;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.LinkedList;
import java.util.Queue;

import java.util.concurrent.ArrayBlockingQueue; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.Stack;

//import java.awt.Point;

import android.util.Log;

public class FloodFillHandler {
    private android.view.View view;
    private int width, height;
    Bitmap bitmap;
    Canvas canvas;
    
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private static final int MAX_STATES = 10;
    
    private String LOG_NAME = "ExtendedCanvas";
    private boolean flagLog = false;
    
    public void setCanvas(Canvas canvas) {
    	if (flagLog) Log.d(LOG_NAME, "Setting canvas");
        this.view = canvas.getView();
        this.width = canvas.Width();
        this.height = canvas.Height();
        this.canvas = canvas;

        //recycleBitmap(this.bitmap);
        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (flagLog) Log.d(LOG_NAME, "Canvas configured!");
    }
    
    private void recycleBitmap(Bitmap bitmap) {
        if (this.bitmap != null && !this.bitmap.isRecycled()) {
            this.bitmap.recycle();
        }
    }
	
    public void clearMemory() {
        if (flagLog) Log.d(LOG_NAME, "Clearing memory");
        clearStack(undoStack);
        clearStack(redoStack);
        recycleBitmap(this.bitmap); // Só recicle this.bitmap se você for recriá-lo depois
        this.bitmap = null; // Defina como null após a reciclagem
        this.canvas.Clear();
    }

    private void clearStack(Stack<Bitmap> stack) {
    	if (flagLog) Log.d(LOG_NAME, "Clearing stack");
        while (!stack.isEmpty()) {
            Bitmap bitmap = stack.pop();
            if (bitmap != null) {
                bitmap.recycle(); // Libera os recursos associados ao bitmap
            }
        }
    }

	
    public void copiesCurrentState() {
    	if (this.bitmap == null) {
            // Opção: Recriar o bitmap aqui se for necessário
            this.bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        }
    	
    	if (flagLog) Log.d(LOG_NAME, "Copying current state");
        
    	// Aqui, você está copiando o estado do canvas para this.bitmap
    	try {
	        for (int i = 0; i < this.width; i++) {
	            for (int j = 0; j < this.height; j++) {
	                int color = this.canvas.GetBackgroundPixelColor(i, j); //To copy sprites GetPixelColor(i, j)
	                this.bitmap.setPixel(i, j, color);
	            }
	        }
	        if (flagLog) Log.d(LOG_NAME, "Copy completed successfully!");
    	} catch (Exception e) {
    		if (flagLog) Log.e(LOG_NAME, "Copying current state", e);
    	}
    }
	
    public void saveCurrentState() {
        if (flagLog) Log.d(LOG_NAME, "Saving current state");
        copiesCurrentState();
        
        // Cria uma cópia do estado atual do bitmap
        Bitmap currentState = Bitmap.createBitmap(this.bitmap.getWidth(), this.bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new android.graphics.Canvas(currentState).drawBitmap(this.bitmap, 0, 0, null);
        
        // Empurra o estado atual para a stack de undo
        undoStack.push(currentState);

        // Limpa a stack de redo
        clearStack(redoStack);

        if (flagLog) Log.d(LOG_NAME, "Canvas state saved");
    }
	
    public void copyBitmapToCanvas(Bitmap bitmap) {
    	if (flagLog) Log.d(LOG_NAME, "Copying bitmap to canvas");
        if (bitmap == null || bitmap.isRecycled()) {
        	if (flagLog) Log.d(LOG_NAME, "Bitmap is null or recycled");
            return;
        }
        
        int oldColor = this.canvas.PaintColor();
        try {
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	                int color = bitmap.getPixel(i, j);
	                this.canvas.PaintColor(color);
	                this.canvas.DrawPoint(i, j);
	            }
	        }
	        if (flagLog) Log.d(LOG_NAME, "Bitmap copy successful!");
        } catch (Exception e) {
    		if (flagLog) Log.e(LOG_NAME, "Error: ", e);
    	}
        
        this.canvas.PaintColor(oldColor);
        this.view.invalidate();
        if (flagLog) Log.d(LOG_NAME, "The copy loop has ended!");
    }
	
    public void undo() {
    	if (flagLog) Log.d(LOG_NAME, "Performing undo");
        if (!undoStack.isEmpty()) {
            this.canvas.Clear();
            Bitmap previousState = undoStack.pop();
            if (previousState == null || previousState.isRecycled()) {
                //Log.d(LOG_NAME, "Previous state is null or recycled");
                return;
            }
            // Recicle o bitmap atual após empurrá-lo para redoStack
            redoStack.push(Bitmap.createBitmap(this.bitmap));
            recycleBitmap(this.bitmap); 
            this.bitmap = previousState;
            copyBitmapToCanvas(previousState);
        }
        this.view.invalidate();
    }

    public void redo() {
    	if (flagLog) Log.d(LOG_NAME, "Performing redo");
        if (!redoStack.isEmpty()) {
            this.canvas.Clear();
            
            Bitmap nextState = redoStack.pop();
            if (nextState == null || nextState.isRecycled()) {
            	if (flagLog) Log.d(LOG_NAME, "Next state is null or recycled");
                return;
            }
            
            // Recicle o bitmap atual após empurrá-lo para undoStack
            undoStack.push(Bitmap.createBitmap(this.bitmap));
            recycleBitmap(this.bitmap);
            this.bitmap = nextState;
            copyBitmapToCanvas(nextState);
        }
        this.view.invalidate();
    }

    public void fastFloodFill(int x, int y, int newColor) {
    	saveCurrentState();
    	
    	if (flagLog) Log.d(LOG_NAME, "Starting floodFill"); // Log to start the flood fill
        if (this.canvas == null || this.bitmap == null) {
        	if (flagLog) Log.d(LOG_NAME, "Canvas is null");
            return;
        }
        
        if (flagLog) Log.d(LOG_NAME, "Canvas Dimensions: Width = " + this.width + ", Height = " + this.height); // Log for canvas dimensions
   
        int targetColor = this.bitmap.getPixel(x, y);
        int oldColor = this.canvas.PaintColor();
        
        // Cria uma matriz para controlar os pixels visitados
        boolean[][] visited = new boolean[width][height];
        
        
        // Aplica o algoritmo de preenchimento
        if (flagLog) Log.d(LOG_NAME, "Target color: " + targetColor); // Log for target color
        if (targetColor != newColor) {
            Queue<Point> queue = new LinkedList<>();
            queue.add(new Point(x, y));

            if (flagLog) Log.d(LOG_NAME, "Starting flood fill loop");
            while (!queue.isEmpty()) {
                Point p = queue.remove();
                if (p.x >= 0 && p.x < this.width && p.y >= 0 && p.y < this.height && !visited[p.x][p.y] && this.bitmap.getPixel(p.x, p.y) == targetColor) {
                	this.bitmap.setPixel(p.x, p.y, newColor);
                	this.canvas.PaintColor(newColor);
                	this.canvas.DrawCircle(p.x, p.y, 1, true);;
                    visited[p.x][p.y] = true; // Marca o pixel como visitado

                    // Adiciona pontos adjacentes à fila
                    queue.add(new Point(p.x - 1, p.y));
                    queue.add(new Point(p.x + 1, p.y));
                    queue.add(new Point(p.x, p.y - 1));
                    queue.add(new Point(p.x, p.y + 1));
                }
            }
            this.canvas.PaintColor(oldColor);

            if (flagLog) Log.d(LOG_NAME, "Flood fill completed");
        } else {
        	if (flagLog) Log.d(LOG_NAME, "Target color is the same as the new color"); // Log if the target color is the same as the new color
        	return;
        }

        // Desenha o bitmap modificado de volta no Canvas
        this.view.invalidate();
        if (flagLog) Log.d(LOG_NAME, "Canvas invalidated after flood fill");
    }
    
    public void floodFillCircle(int x, int y, int newColor, float radius) {
    	saveCurrentState();
    	
    	if (flagLog) Log.d(LOG_NAME, "Starting floodFill"); // Log to start the flood fill
        if (this.canvas == null) {
        	if (flagLog) Log.d(LOG_NAME, "Canvas is null");
            return;
        }
        
        if (flagLog) Log.d(LOG_NAME, "Canvas Dimensions: Width = " + this.width + ", Height = " + this.height); // Log for canvas dimensions


        // Applies flood fill to the bitmap
        int targetColor = this.bitmap.getPixel(x, y);
        int oldColor = this.canvas.PaintColor();
        if (flagLog) Log.d(LOG_NAME, "Target color: " + targetColor); // Log for target color
        if (targetColor != newColor) {
            Queue<Point> queue = new LinkedList<>();
            queue.add(new Point(x, y));
            
            if (flagLog) Log.d(LOG_NAME, "Starting flood fill loop");
            while (!queue.isEmpty()) {
                Point p = queue.remove();
                if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height && this.bitmap.getPixel(p.x, p.y) == targetColor) {
                    this.bitmap.setPixel(p.x, p.y, newColor);
                    this.canvas.PaintColor(newColor);
                    this.canvas.DrawCircle(p.x, p.y, radius, true);

                    // Add points to the queue with the specified density
                    queue.add(new Point(p.x - 1, p.y));
                    queue.add(new Point(p.x + 1, p.y));
                    queue.add(new Point(p.x, p.y - 1));
                    queue.add(new Point(p.x, p.y + 1));
                }
            }
            this.canvas.PaintColor(oldColor);
            if (flagLog) Log.d(LOG_NAME, "Flood fill completed");
        } else {
            Log.d(LOG_NAME, "Target color is the same as the new color"); // Log if the target color is the same as the new color
            return;
        }

        // Draws the modified bitmap back onto the Canvas
        this.view.invalidate();
        if (flagLog) Log.d(LOG_NAME, "Canvas invalidated after flood fill");
    }
    
    public void floodFillDensity(int x, int y, int newColor, int density) {
    	saveCurrentState();
    	
    	if (flagLog) Log.d(LOG_NAME, "Starting floodFill"); // Log to start the flood fill
        if (this.canvas == null) {
        	if (flagLog) Log.d(LOG_NAME, "Canvas is null");
            return;
        }
        
        if (flagLog) Log.d(LOG_NAME, "Canvas Dimensions: Width = " + this.width + ", Height = " + this.height); // Log for canvas dimensions

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.view.invalidate();

        // Applies flood fill to the bitmap
        int targetColor = this.bitmap.getPixel(x, y);
        if (flagLog) Log.d(LOG_NAME, "Target color: " + targetColor); // Log for target color
        
        if (targetColor != newColor) {
            Queue<Point> queue = new LinkedList<>();
            queue.add(new Point(x, y));
            
            if (flagLog) Log.d(LOG_NAME, "Starting flood fill loop");
            while (!queue.isEmpty()) {
                Point p = queue.remove();
                if (p.x >= 0 && p.x < this.width && p.y >= 0 && p.y < this.height && this.bitmap.getPixel(p.x, p.y) == targetColor) {
                	this.bitmap.setPixel(p.x, p.y, newColor);
                	this.canvas.SetBackgroundPixelColor(p.x, p.y, newColor);

                    // Add points to the queue with the specified density
                    if (p.x - density >= 0) queue.add(new Point(p.x - density, p.y));
                    if (p.x + density < width) queue.add(new Point(p.x + density, p.y));
                    if (p.y - density >= 0) queue.add(new Point(p.x, p.y - density));
                    if (p.y + density < height) queue.add(new Point(p.x, p.y + density));
                }
            }
            if (flagLog) Log.d(LOG_NAME, "Flood fill completed");
        } else {
        	if (flagLog) Log.d(LOG_NAME, "Target color is the same as the new color"); // Log if the target color is the same as the new color
        	return;
        }

        // Draws the modified bitmap back onto the Canvas
        this.view.invalidate();
        if (flagLog) Log.d(LOG_NAME, "Canvas invalidated after flood fill");
    }
    
    public void floodFillPontSizeDensity(int x, int y, int newColor, int pointSize, int density) {
    	saveCurrentState();
    	
    	if (flagLog) Log.d(LOG_NAME, "Starting floodFill"); // Log to start the flood fill
        if (this.canvas == null) {
        	if (flagLog) Log.d(LOG_NAME, "Canvas is null");
            return;
        }
        
        if (flagLog) Log.d(LOG_NAME, "Canvas Dimensions: Width = " + this.width + ", Height = " + this.height); // Log for canvas dimensions

        // Applies flood fill to the bitmap
        int targetColor = this.bitmap.getPixel(x, y);
        if (flagLog) Log.d(LOG_NAME, "Target color: " + targetColor); // Log for target color
        if (targetColor != newColor) {
        	Queue<Point> queue = new LinkedList<>();
            queue.add(new Point(x, y));

            while (!queue.isEmpty()) {
                Point p = queue.remove();
                if (isValidPoint(p.x, p.y, this.width, this.height, targetColor, this.bitmap)) {
                    paintSquare(p.x, p.y, pointSize, newColor, this.width, this.height, this.bitmap, this.canvas);

                    // Adicionar pontos com a densidade especificada, sem verificar se já foram visitados
                    addPointWithDensity(queue, p.x - density, p.y, this.width, this.height);
                    addPointWithDensity(queue, p.x + density, p.y, this.width, this.height);
                    addPointWithDensity(queue, p.x, p.y - density, this.width, this.height);
                    addPointWithDensity(queue, p.x, p.y + density, this.width, this.height);
                }
            }
            if (flagLog) Log.d(LOG_NAME, "Flood fill completed");
        } else {
        	if (flagLog) Log.d(LOG_NAME, "Target color is the same as the new color"); // Log if the target color is the same as the new color
        	return;
        }

        // Draws the modified bitmap back onto the Canvas
        this.view.invalidate();
        if (flagLog) Log.d(LOG_NAME, "Canvas invalidated after flood fill");
    }
    
    private void paintSquare(int x, int y, int pointSize, int color, int width, int height, Bitmap bitmap, Canvas canvas) {
    	if (flagLog) Log.d(LOG_NAME, "Painting square");
        for (int dx = -pointSize; dx <= pointSize; dx++) {
            for (int dy = -pointSize; dy <= pointSize; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    bitmap.setPixel(nx, ny, color);
                    canvas.SetBackgroundPixelColor(nx, ny, color);
                }
            }
        }
        bitmap.setPixel(x, y, color);
        canvas.SetBackgroundPixelColor(x, y, color);
    }
    
    private void addPointWithDensity(Queue<Point> queue, int x, int y, int width, int height) {
    	if (flagLog) Log.d(LOG_NAME, "Adding point with density");
        if (x >= 0 && x < width && y >= 0 && y < height) {
            queue.add(new Point(x, y));
        }
    }

    private boolean isValidPoint(int x, int y, int width, int height, int targetColor, Bitmap bitmap) {
    	if (flagLog) Log.d(LOG_NAME, "Checking if point is valid");
        return x >= 0 && x < width && y >= 0 && y < height && bitmap.getPixel(x, y) == targetColor;
    }
}    