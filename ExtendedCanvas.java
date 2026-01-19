package com.bosonshiggs.extendedcanvas;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.common.OptionList;

import com.bosonshiggs.extendedcanvas.helpers.FloodFillHandler;
import com.bosonshiggs.extendedcanvas.helpers.ImageType;

import java.lang.Thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection; //Update pictures

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

@DesignerComponent(
    version = 3,
    description = "Extended Canvas with flood fill functionality",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class ExtendedCanvas extends AndroidNonvisibleComponent {
    private FloodFillHandler floodFillHandler;
    private Canvas canvasComponent; // Reference to the Kodular Canvas component
    private String LOG_NAME = "ExtendedCanvas";
    private boolean flagLog = false;
    private Bitmap gridBitmap;
    
    public ExtendedCanvas(ComponentContainer container) {
        super(container.$form());
        floodFillHandler = new FloodFillHandler();
    }

    @SimpleFunction(description = "Perform flood fill from a point with a specified color. "
    		+ "A spacing value of 1 will fill every adjacent point (as it was originally doing), "
    		+ " while a higher value will leave more space between the filled points, creating a sparser fill pattern.")
    public void FloodFillDensity(
    		final int x, 
    		final int y, 
    		final int newColor, 
    		final int density) 
    {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		//Run floodFillHandler
            		floodFillHandler.floodFillDensity(x, y, newColor, density); // Now the canvas reference is passed correctly
            	} catch (final Exception e) {
                	if (flagLog) Log.e(LOG_NAME, "Error in FastFloodFill: " + e.getMessage(), e);
                    ReportError("FastFloodFill - Error: " + e.getMessage());
                }
            }
        }).start();
    }
    
    @SimpleFunction(description = "Perform flood fill from a point with a specified color. "
    		+ "A spacing value of 1 will fill every adjacent point (as it was originally doing), "
    		+ " while a higher value will leave more space between the filled points, creating a sparser fill pattern.\n"
    		+ "For example, a pointSize of 1 will paint a 3x3 pixel square (the center point plus one pixel in each direction). "
    		+ "Adjust this value as needed to get the desired dot size")
    public void FloodFillPontSizeDensity(
    		final int x, 
    		final int y, 
    		final int newColor, 
    		final int pointSize, 
    		final int density) 
    {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		floodFillHandler.floodFillPontSizeDensity(x, y, newColor, pointSize, density); // Now the canvas reference is passed correctly
            	} catch (final Exception e) {
                	if (flagLog) Log.e(LOG_NAME, "Error in FastFloodFill: " + e.getMessage(), e);
                    ReportError("FastFloodFill - Error: " + e.getMessage());
                }
		    }
		}).start();
    }
    
    @SimpleFunction(description = "Perform flood fill from a point with a specified color.")
    public void FloodFillCircle(
    		final int x, 
    		final int y, 
    		final int newColor, 
    		final float radius) 
    {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		floodFillHandler.floodFillCircle(x, y, newColor, radius); // Now the canvas reference is passed correctly
            	} catch (final Exception e) {
                	if (flagLog) Log.e(LOG_NAME, "Error in FastFloodFill: " + e.getMessage(), e);
                    ReportError("FastFloodFill - Error: " + e.getMessage());
                }
		    }
		}).start();
    }
    
    @SimpleFunction(description = "Perform flood fill from a point with a specified color.")
    public void FastFloodFill(
    		final int x, 
    		final int y, 
    		final int newColor) 
    {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
	        		// Any UI update or event call must be done on the UI thread
	            	floodFillHandler.fastFloodFill(x, y, newColor);
            	} catch (final Exception e) {
            		if (flagLog) Log.e(LOG_NAME, "Error in FastFloodFill: " + e.getMessage(), e);
                    ReportError("FastFloodFill - Error: " + e.getMessage());
                }
		    }
		}).start();
    }
    
    @SimpleFunction(description = "Undoes the last change in Canvas.")
    public void UndoCanvas() {
    	new Thread (new Runnable() {
		@Override
		public void run() {
			try 
			{
				floodFillHandler.undo();
			} catch (Exception e) {
				if (flagLog) Log.e(LOG_NAME, "Error in UndoCanvas: " + e.getMessage(), e);
                ReportError("UndoCanvas - Error: " + e.getMessage());			
			}
				
    	}
		}).start();
    }
    
    @SimpleFunction(description = "Redo the last change in Canvas.")
    public void RedoCanvas() {
    	new Thread(new Runnable() {
    		@Override
    		public void run() {
    			try {
					floodFillHandler.redo();  				
    			} catch (final Exception e) {
    				if (flagLog) Log.e(LOG_NAME, "Error in RedoCanvas: " + e.getMessage(), e);
                    ReportError("RedoCanvas - Error: " + e.getMessage());				
    			}
    		}
    	}).start();
    	
    }
    
    @SimpleFunction(description = "Method for saving the current state of the Canvas to be used with the Undo and Redo methods.\n"
    		+ "Careful! Saving too many states can lead to excessive memory consumption.\n"
    		+ "By default, the method saves the initial state of the application.")
    public void saveCurrentState() {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		floodFillHandler.saveCurrentState();
            		SaveCurrentStateReady();
            	} catch(Exception e) {
            		if (flagLog) Log.e(LOG_NAME, "Error: " + e.getMessage(), e);
            		ReportError("saveCurrentState - Error: " + e.getMessage());
            	}
            }
		}).start();
    }
    
    @SimpleFunction(description = "In this clearMemory method, the Undo and Redo stacks are emptied, and the bitmaps in each stack are recycled.\n"
    		+ "The recycle method is called on each Bitmap to free up the memory resources it is using.\n"
    		+ "Note that after a bitmap is recycled, it can no longer be used. "
    		+ "Therefore, it is important to ensure that clearMemory is only called when you are sure that these bitmaps will no longer be needed.\n"
    		+ "Additionally, the call to recycle and emptying the stacks help signal to the Java garbage collector that these resources can be freed, which can help reduce the application's memory usage.")
    public void clearMemory() {
    	new Thread(new Runnable() {
            @Override
            public void run() {
            	floodFillHandler.clearMemory();
            }
		}).start();
    }
    	
    @SimpleFunction(description = "Set the Canvas component used for painting.")
    public void SetCanvas(Canvas canvas) {
        this.canvasComponent = canvas; // Updates the canvasComponent reference
        floodFillHandler.setCanvas(canvas);
    }
    
    @SimpleFunction(description = "Download the current state of the Canvas as an image.")
    public void DownloadCanvasImage(String imageName, @Options(ImageType.class) String imageType, String myDirName) {
        Bitmap.CompressFormat format;

        switch (imageType) {
            case "jpeg":
                format = Bitmap.CompressFormat.JPEG;
                break;
            case "png":
                format = Bitmap.CompressFormat.PNG;
                break;
            case "webp":
                format = Bitmap.CompressFormat.WEBP;
                break;
            default:
                format = Bitmap.CompressFormat.PNG;
                break;
        }

        // Verifica o nível da API e solicita permissão de escrita se necessário
        if (Build.VERSION.SDK_INT < 30) {
            if (form.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                form.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }

        // Obter a View do Canvas
        android.view.View canvasView = canvasComponent.getView();

        // Criar um Bitmap para armazenar a imagem do Canvas
        Bitmap canvasBitmap = Bitmap.createBitmap(canvasView.getWidth(), canvasView.getHeight(), Bitmap.Config.ARGB_8888);
        android.graphics.Canvas bitmapCanvas = new android.graphics.Canvas(canvasBitmap);
        canvasView.draw(bitmapCanvas);

        // Caminho da pasta Pictures no armazenamento externo
        File picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        // Criar um diretório exclusivo para as imagens do Canvas
        File appImagesPath = new File(picturesPath, myDirName);
        if (!appImagesPath.exists()) {
            appImagesPath.mkdirs();
        }

        // Caminho do arquivo de imagem dentro do diretório exclusivo
        File imageFile = new File(appImagesPath, imageName + "." + imageType);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            canvasBitmap.compress(format, 100, fos);
            fos.close();

            // Atualizar a galeria
            MediaScannerConnection.scanFile(form, new String[] { imageFile.getAbsolutePath() }, null, null);

            // Disparar um evento para notificar que a imagem foi salva
            ImageDownloaded(imageFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(LOG_NAME, "Error in DownloadCanvasImage: " + e.getMessage(), e);
            ReportError("DownloadCanvasImage - Error: " + e.getMessage());
        }
    }

    @SimpleEvent(description = "Triggered when an image is successfully downloaded.")
    public void ImageDownloaded(String filePath) {
        EventDispatcher.dispatchEvent(this, "ImageDownloaded", filePath);
    }

    
    @SimpleEvent(description = "Received a generated image")
    public void SaveCurrentStateReady() {
    	EventDispatcher.dispatchEvent(this, "SaveCurrentStateReady");
    }
    
    @SimpleEvent(description = "Report an error with a custom message")
    public void ReportError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "ReportError", errorMessage);
    }
    
}
