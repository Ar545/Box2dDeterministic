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
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.box2d.shape.*;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all 
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen, ContactListener {
	
	/** The font for giving messages to the player */
	private static BitmapFont theFont;

	// The various ways to move a physics object
	/** No active controller */
	public static final int CONTROL_NONE = -1;
	/** Move objects by force */
	public static final int CONTROL_FORCE = 0;
	/** Move objects by impulse */
	public static final int CONTROL_IMPULSE = 1;
	/** Move objects by kinetically */
	public static final int CONTROL_VELOCITY = 2;
	/** Move objects by manual translation */
	public static final int CONTROL_TRANSLATE = 3;
	public static final int NUM_CONTROLS = 4;
	
	// We cannot measure physics objects in pixels.  The scale is too great.
	// So we make objects small, and scale them when we draw.
	public static final float DEFAULT_SCALE = 80.0f;

	// Default sizes for the model objects
	public static final int AVATAR_WIDTH   = 1;
	public static final int AVATAR_HEIGHT  = 1;
	public static final int BARRIER_WIDTH  = 3;
	public static final int BARRIER_HEIGHT = 1;

	// For translating the controls
	public static final float MAX_DENSITY = 1000.0f; 
	public static final float MIN_DENSITY = 0.1f; 
	public static final float MAX_STICKY  = 100.0f; 
	public static final float MIN_STICKY  = 0.001f; 
	public static final float MAX_BOUNCY  = 10.0f; 
	public static final float MIN_BOUNCY  = 0.001f;
	
	// Text offsets
	public static final float LEFT_OFFSET  = 0.25f;
	public static final float RIGHT_OFFSET = 2.6f;
	public static final float MID_OFFSET   = 1.5f;
	public static final float TOP_OFFSET   = 0.25f;
	public static final float BOT_OFFSET   = 0.2f;
	
	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;

	/** Canvas used to draw the game onto the screen (VIEW CLASS) */
	private GameCanvas canvas;

	/** Controller to read input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;

	/** Physics world (CONTROLLER CLASS) */
	private World world;

	// Physics bodies (MODEL CLASSES) 
	/** Player controlled avatar */
	private Entity avatar;
	/** Static (unmovable) barrier */
	private Entity barrier;
	/** Cache object for moving the player */
	private Vector2 translate = new Vector2();

	// Reference states for game HUD
	/** The current active control screen */
	private int controls = CONTROL_FORCE;
	
	// The following are all Java hacks to allow us to pass the variables by reference
	// See changeValue() for an example of how we use them.
	/** Object density (array so we can use as a reference variable) */
	private float[] density =  { 1.0f };
	/** Object friction (array so we can use as a reference variable) */
	private float[] friction = { 0.1f };
	/** Object restitution (array so we can use as a reference variable) */
	private float[] restitution = { 0.0f };

	// To try out new shape classes, add them here.
	/** The list of shape classes */
	private Class[] shapeTypes = { Box.class, Triangle.class, Circle.class, Ellipse.class };
	/** The current active shape */
	private int shape = 0;

	/** Whether or not this player mode is still active */
	private boolean active;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas) {
		this.canvas = canvas;
		canvas.getScale().set(DEFAULT_SCALE, DEFAULT_SCALE);
		
		// Create the controllers.
		inputController = new InputController();
		
		// Create the entities
		reset();
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		canvas = null;
		world.dispose();
		world  = null;
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		theFont = directory.getEntry("display",BitmapFont.class);
	}

	/**
	 * Creates the game models and puts them into place
	 */
	protected void reset() {
		if (world != null) {
			world.dispose();
		}
		
		// Create the world
		Vector2 size = new Vector2 (canvas.getWidth()/canvas.getSX(), canvas.getHeight()/canvas.getSY());
		world = new World(Vector2.Zero, false);

		// Create the player
		Vector2 position = new Vector2(size.x / 2,  size.y / 3.0f);
		avatar = makeEntity(shape);
		avatar.getColor().set(Color.RED);
		avatar.setDensity(density[0]);
		avatar.setFriction(friction[0]);
		avatar.setRestitution(restitution[0]);
		avatar.setPosition(position);
		avatar.initialize(world, new Vector2(AVATAR_WIDTH, AVATAR_HEIGHT));

		// Create the barrier
		position = new Vector2 (size.x / 2, 2 * size.y/3.0f);
		barrier = makeEntity(shape);
		barrier.setStatic(true);
		barrier.getColor().set(Color.YELLOW);
		barrier.setDensity(density[0]);
		barrier.setFriction(friction[0]);
		barrier.setRestitution(restitution[0]);
		barrier.setPosition(position);
		barrier.initialize(world, new Vector2(BARRIER_WIDTH, BARRIER_HEIGHT));

		world.setContactListener(this);
	}
		
	/**
	 * Creates an entity of the given shape from shapeTypes.
	 *
	 * This method acts as a polymorphic constructor.
	 *
	 * @return an entity of the given shape
	 */
	private Entity makeEntity(int shape) {
		try {
			return (Entity)shapeTypes[shape].getDeclaredConstructor().newInstance();			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * Update the game state.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		inputController.readInput();

		// Process all the settings (density, friction, etc.) changes
		changeSettings();

		// Process avatar movement.
		switch (controls) {
		case CONTROL_FORCE:
			avatar.getBody().applyForce (inputController.getLinearForce(), avatar.getBody().getPosition(), true);
			avatar.getBody().applyTorque(inputController.getAngularForce(), true);
			break;
		case CONTROL_IMPULSE:
			avatar.getBody().applyLinearImpulse(inputController.getLinearForce(), avatar.getBody().getPosition(), true);
			avatar.getBody().applyAngularImpulse(inputController.getAngularForce(), true);
			break;
		case CONTROL_VELOCITY:
			avatar.getBody().setLinearVelocity(inputController.getLinearForce());
			avatar.getBody().setAngularVelocity(inputController.getAngularForce());
			break;
		case CONTROL_TRANSLATE:
			// We have to adjust this a lot to keep it from zipping about
			translate.set(inputController.getLinearForce()).scl(1/50.0f);
			translate.add(avatar.getBody().getPosition());

			float ang = avatar.getBody().getAngle() + inputController.getAngularForce();
			avatar.getBody().setTransform (translate, ang);
			break;
		default:
			// Do nothing
			break;
		}
		
		// Process physicsSize
		world.step(delta, WORLD_VELOC, WORLD_POSIT);
	}
	
	/**
	 * Change the physics settings
	 *
	 * This class is like a primitive technical prototype.  It allows us to 
	 * change various physics settings on the fly.
	 */
	private void changeSettings() {
		boolean reset = false;

		// Change shape.  The best thing to do is to reset
		if (inputController.getShape() > 0) {
			shape = ((shape + inputController.getShape()) % shapeTypes.length);
			reset = true;
		} else if (inputController.getShape() < 0) {
			shape = ((shape + (shapeTypes.length + inputController.getShape())) % shapeTypes.length);
			reset = true;
		}

		//  Change physics constants if appropriate
		if (changeValue(density, inputController.getDensity(), MIN_DENSITY, MAX_DENSITY)) {
			avatar.setDensity(density[0]);
			barrier.setDensity(density[0]);
		}
		if (changeValue(friction, inputController.getFriction(), MIN_STICKY, MAX_STICKY)) {
			avatar.setFriction(friction[0]);
			barrier.setFriction(friction[0]);
		}
		if (changeValue (restitution, inputController.getRestitution(), MIN_BOUNCY, MAX_BOUNCY)) {
			avatar.setRestitution(restitution[0]);
			barrier.setRestitution(restitution[0]);
		}

		// Change the input controls
		if (inputController.getControls() > 0) {
			controls = (controls + inputController.getControls()) % NUM_CONTROLS;
			// Stop the body.
			avatar.getBody().setLinearVelocity(Vector2.Zero);
			avatar.getBody().setAngularVelocity(0f);
		} else if (inputController.getControls() < 0) {
			controls = (controls + (NUM_CONTROLS+inputController.getControls())) % NUM_CONTROLS;
			// Stop the body.
			avatar.getBody().setLinearVelocity(Vector2.Zero);
			avatar.getBody().setAngularVelocity(0f);
		}

		if (inputController.didReset() || reset) {
			reset();
		}
	}
	
	/**
	 * Change a physical value by a factor of 10.
	 *
	 * This function allows us to change behavior on a log scale, which is what we
	 * have to do to see major differences in behavior.
	 *
	 * Note that the first value is an array.  This is a Java hack for passing a
	 * primitive variable (in this case a float) by reference.
	 *
	 * @param value  The current value to change
	 * @param factor The amount to change by (multiply or divide by 10)
	 * @param min	 The minimum possible value
	 * @param max	 The maximum possible value
	 *
	 * @return True if the value was changed (by reference).  False otherwise.
	 */
	private boolean changeValue(float[] value, float factor, float min, float max) {
		if (factor == 1.0f) {
			return false;
		}

		// If was at zero, and increasing, jump to minimum.
		if (value[0] == 0.0f && factor > 1.0f) {
			value[0] = min;
			return true;
		}

		// Changing nonzero value
		value[0] *= factor;
		if (value[0] > max) {
			value[0] = max;
		}
		if (value[0] < min) {
			value[0] = 0.0f;
		}
		return true;
	}
		
	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw(float delta) {
		canvas.begin();

		// Draw the shapes.
		avatar.draw(canvas);
		barrier.draw(canvas);

		// Draw the HUD.
		String shape = "Shape: " + avatar.getClass().getSimpleName();
		String version = "Controls: " + getControlName(controls);

		String dense = "Density: " + String.format("%4.3f",density[0]);
		//String frict = "Friction: " + String.format("%4.3f",friction[0]);
		String frict = "Friction: " + String.format("%4.3f",avatar.getFriction());
		String resti = "Restitution: " + String.format("%4.3f",restitution[0]);

		
		// Lots of magic numbers here.  Bad programming style. Don't do this.
		Vector2 size = new Vector2 (canvas.getWidth()/canvas.getSX(), canvas.getHeight()/canvas.getSY());
		canvas.drawText(shape, theFont, LEFT_OFFSET, size.y-TOP_OFFSET);
		canvas.drawText(version, theFont, size.x-RIGHT_OFFSET, size.y-TOP_OFFSET);
		canvas.drawText(dense, theFont, LEFT_OFFSET, BOT_OFFSET);
		canvas.drawText(frict, theFont, size.x/2.0f-MID_OFFSET, BOT_OFFSET);
		canvas.drawText(resti, theFont, size.x-RIGHT_OFFSET, BOT_OFFSET);
		
		canvas.end();
	}
	
	/**
	 * Returns the string representation of the control code
	 *
	 * This is used to display the current controls in the HUD.
	 *
	 * @return the string representation of the control code
	 */
	public String getControlName(int control) {
		switch (controls) {
		case CONTROL_FORCE:
			return "Force";
		case CONTROL_IMPULSE:
			return "Impulse";
		case CONTROL_VELOCITY:
			return "Velocity";
		case CONTROL_TRANSLATE:
			return "Translate";
		default:
			return "None";
		}	
	}

	/// SCREEN
	
	/**
	 * Called when the Screen is resized. 
	 *
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
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw(delta);
			if (inputController.didExit() && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
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
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}
	
	/// CONTACT LISTENER
	
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		Entity obj1 = (Entity)body1.getUserData();
		Entity obj2 = (Entity)body2.getUserData();

		// If either object is the avatar, change color
		if (obj1 == avatar || obj2 == avatar) {
			avatar.getColor().set(Color.PURPLE);
		}
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */ 
	public void endContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		Entity obj1 = (Entity)body1.getUserData();
		Entity obj2 = (Entity)body2.getUserData();

		// If either object is the avatar, restore color
		if (obj1 == avatar || obj2 == avatar) {
			avatar.getColor().set(Color.RED);
		}
	}
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}