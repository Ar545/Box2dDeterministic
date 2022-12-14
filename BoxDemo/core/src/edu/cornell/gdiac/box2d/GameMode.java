/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a 
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * LibGDX version, 3/12/2015
 */
package edu.cornell.gdiac.box2d;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.*;
//import com.badlogic.gdx.physics.box2d.*;
import org.jbox2d.callbacks.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;
import org.jbox2d.collision.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;

/**
 * The primary controller class for the game.
 * While GDXRoot is the root class, it delegates all the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all 
 * the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen, ContactListener {

	public static int[][] debugArray = new int[3][40];



	/** The font for giving messages to the player */
	private static BitmapFont theFont;

	// We cannot measure physics objects in pixels.  The scale is too great.
	// So we make objects small, and scale them when we draw.
	public static final float DEFAULT_SCALE = 80.0f;

	/** Whether this player mode is still active */
	private boolean active;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** Canvas used to draw the game onto the screen (VIEW CLASS) */
	private GameCanvas canvas;

	/** Controller to read input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;


	Vector2 size = null;

	public GameplayController leftController;
	public GameplayController rightController;

	/**
	 * Creates a new game with the given drawing context.
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas) {
		this.canvas = canvas;
		canvas.getScale().set(DEFAULT_SCALE, DEFAULT_SCALE);

		// Create the controllers.
		inputController = new InputController();
		leftController = new GameplayController(true);
		rightController = new GameplayController(false);

		// Create the entities
		reset();

	}

	/**
	 * Gather the assets for this controller.
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		theFont = directory.getEntry("display",BitmapFont.class);
		leftController.gatherAssets(theFont);
		rightController.gatherAssets(theFont);
	}

	float offset = 0;

	protected void reset(){
		// Create the world
		size = new Vector2 (canvas.getWidth()/(canvas.getSX() * 2), canvas.getHeight()/canvas.getSY());
		offset = canvas.getWidth()/(canvas.getSX() * 2);
		leftController.reset(size);
		rightController.reset(size);
		leftController.setContactListener(this);
		rightController.setContactListener(this);
		print = false;
	}


	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		canvas = null;
		leftController.dispose();
		rightController.dispose();
	}

	float delta_cache = 0.018f;

	public void update(float delta){
		inputController.readInput();


		// Process all the settings (density, friction, etc.) changes
		changeSettings();
		leftController.update(delta, inputController, debugArray);
		rightController.update(delta_cache, inputController, debugArray);
		delta_cache = delta;
	}

	/**
	 * Change the physics settings
	 * This class is like a primitive technical prototype.  It allows us to
	 * change various physics settings on the fly.
	 */
	private void changeSettings() {
		if (inputController.didReset()) {
			reset();
		}else {
			leftController.changeSettings(inputController);
			rightController.changeSettings(inputController);
		}
	}

	/**
	 * Draw the status of this player mode.
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw(float delta) {
		canvas.begin();

		leftController.draw(canvas);
		rightController.draw(canvas, offset);

		String y_pos_diff = "Y position difference of avatar is " + (leftController.avatar.getPosition().y - rightController.avatar.getPosition().y);
		canvas.drawText(y_pos_diff, theFont, 1, 2);
		String y_pos_diff_scl = "Log2 y_diff = " + Math.log(Math.abs((leftController.avatar.getPosition().y - rightController.avatar.getPosition().y)));
		canvas.drawText(y_pos_diff_scl, theFont, 1, 1.5f);
		String x_pos_diff = "X position difference of avatar is  " + (leftController.avatar.getPosition().x - rightController.avatar.getPosition().x);
		canvas.drawText(x_pos_diff, theFont, 1, 2.5f);
		String car_pos_diff = "Position difference of car is " + (leftController.car.getPosition().x - rightController.car.getPosition().x);
		canvas.drawText(car_pos_diff, theFont, 1, 3);

		canvas.end();
	}

	/// SCREEN

	/**
	 * Called when the Screen is resized.
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			print(delta);
			draw(delta);
			if (inputController.didExit() && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	boolean print = false;
	public void print(float delta){
//		if()
//		System.out.print("delta:" + delta + ",left-time:");
//		System.out.print(leftController.car.getPosition().x + ",right-time:");
//		System.out.print(rightController.car.getPosition().x + ",left-pos:");
//		System.out.print(Float.floatToRawIntBits(leftController.avatar.getPosition().y) + ",right-pos:");
//		System.out.print(Float.floatToRawIntBits(rightController.avatar.getPosition().y) + ",diff-pos:");
//		System.out.print(Float.floatToRawIntBits(leftController.avatar.getPosition().y) - Float.floatToRawIntBits(rightController.avatar.getPosition().y));
//		System.out.println(".");

		int leftTime = Math.round(leftController.car.getPosition().x * 1000 / 3) - 1;
		int rightTime = Math.round(rightController.car.getPosition().x * 1000 / 3) - 1;
		if(leftTime > debugArray[0].length && rightTime > debugArray[0] .length && !print){
			for(int i = 0; i < debugArray[0].length; i ++){
				System.out.print("time:" + i);
				System.out.print(",left-pos:" + debugArray[0][i] + ",right-pos:");
				System.out.println(debugArray[1][i] + ",diff-pos:" + (debugArray[0][i] - debugArray[1][i]));
			}
			print = true;
		}
	}

	/**
	 * Called when the Screen is paused.
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/// CONTACT LISTENER

	/**
	 * Callback method for the start of a collision
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		leftController.beginContact(contact);
		rightController.beginContact(contact);
	}

	/**
	 * Callback method for the start of a collision
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the character is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		leftController.endContact(contact);
		rightController.endContact(contact);
	}

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

}