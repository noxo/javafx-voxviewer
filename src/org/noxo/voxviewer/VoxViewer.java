package org.noxo.voxviewer;

// Erkki Nokso-Koivisto 20/Sept/2014

import org.noxo.voxviewer.VoxImporter.VoxImporterListener;

import java.io.DataInputStream;
import java.io.InputStream;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;


public class VoxViewer extends Application implements VoxImporterListener {
	
	final Group voxels = new Group();
	
	final int WINDOW_WIDTH = 1280;
	final int WINDOW_HEIGHT = 720;
	
	private void buildModel(String modelName)
	{
		
		VoxImporter modelReader = new VoxImporter(this, true);
		
		try {
			InputStream is = VoxViewer.class.getResourceAsStream(modelName);
			DataInputStream stream = new DataInputStream(is);
			modelReader.readMagica(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {

		PerspectiveCamera camera = new PerspectiveCamera();

	    String backgroundImageURL = getClass().getResource("smooth.png").toExternalForm();
	    String styleCSS = "-fx-background-image: url(\"" + backgroundImageURL + "\");  -fx-background-position: center center;  -fx-background-size: stretch; ";
	    
		HBox backgroundImage = new HBox();
		backgroundImage.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		backgroundImage.setBackground(null);
        backgroundImage.setStyle(styleCSS);
        
		Group models3d = new Group();
        models3d.getChildren().add(voxels);
        models3d.setTranslateX(WINDOW_WIDTH/2);
        models3d.setTranslateY(WINDOW_HEIGHT/2);
        models3d.setTranslateZ(-100); // away from background image
        
	    Group root =  new Group();

        root.getChildren().add(backgroundImage);
		root.getChildren().add(models3d);

        voxels.setRotationAxis(Rotate.X_AXIS);
        voxels.setRotate(90);
        
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setDelay(Duration.millis(4));
        rotateTransition.setDuration(Duration.millis(4000));
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(false);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setByAngle(360);
        rotateTransition.setNode(models3d);
        rotateTransition.play();
        
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, true);
       
        scene.setCamera(camera);
        stage.setScene(scene);
        
        stage.show();
        
        models3d.setScaleX(13);
        models3d.setScaleY(13);
        models3d.setScaleZ(13);
        
        Thread builderThread = new Thread()
        {
        	public void run()
        	{
        		try {
        			// download models from http://voxel.codeplex.com/
        			buildModel("pinksomething.vox");
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        };
        
        builderThread.start();
	}

	public void cleanStage()
	{
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				VoxViewer.this.voxels.getChildren().clear();
			}
		};
		
		Platform.runLater(r);
	}
	
	@Override
	public void blockConstructed(final int sizex, 
			final int sizey, 
			final int sizez, 
			final int x, 
			final int y,
			final int z, 
			final int color) {
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				
				Box box = new Box(1,1,1);

				int a = (color >> 24) & 0xff;
				int r = (color >> 16) & 0xff;
				int g = (color >> 8) & 0xff;
				int b = color & 0xff;
				
				PhongMaterial mat = new PhongMaterial(Color.rgb(r, g, b));
				
				box.setMaterial(mat);
				box.setTranslateX(x);
				box.setTranslateY(y);
				box.setTranslateZ(z);
				
				voxels.getChildren().add(box);
			}
		};
		
		Platform.runLater(r);
		
	}
	
	public static void main(String args[])
	{
		launch(args);	
	}
}
