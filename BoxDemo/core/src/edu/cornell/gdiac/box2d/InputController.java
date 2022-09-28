/*
 * InputController.cs
 *
 * Device-independent input manager.
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * LibGDX version: 3/8/2015
 */
package edu.cornell.gdiac.box2d;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.*;

/**
 * Class for reading player input. 
 *
 * This class has an alterate way of handling button presses: a cooldown.  If 
 * a user holds down a button, we only register it a few times a second.
 */
public class InputController {

	/** Amount to adjust the inputs to speed movement */
	private static final float MOVE_SCALE = 5.0f;
	private static final float ROT_SCALE = 5.0f;

	/** How long to wait until we recognize a button again. */
	private static final int COOLDOWN = 20;

	/** Whether the player chose to exit */
	protected boolean exit;
	/** Whether the player chose to reset */
	protected boolean reset;

	/** The amount to change the density by */
	protected float density;
	/** The amount to change the friction by */
	protected float friction;
	/** The amount to change the restitution by */
	protected float restitution;
	/** The direction to cycle the shapes */
	protected int   shape;
	/** The direction to cycle the controls */
	protected int   controls;

	/** A value to implement a cooldown on all buttons */
	protected int cooldown = 0;

	// Fields to manage shape movement
	/** The linear force (or velocity) from the input */
	protected Vector2 linearForce;
	/** A cache variable to allow safe access to linearForce */
	protected Vector2 linearCache;
	/** The angular force (or velocity) from the input */
	protected float angularForce;
	
	/** An X-Box controller (if it is connected) */
	private XBoxController xbox;

	
	/**
	 * Return the amount of translational movement
	 *
	 * This does not return a reference to the input vector.  Subsequent calls
	 * to this method will reset the vector returned.
	 *
	 * @return the amount of translational movement
	 */
	public Vector2 getLinearForce() {
		return linearCache.set(linearForce);
	}

	/**
	 * Returns the amount of rotational movement
	 *
	 * @return the amount of rotational movement
	 */
	public float getAngularForce() {
		return angularForce;
	}

	/**
	 * Returns the shape change offset (0 if no change)
	 *
	 * The shapes are arranged in a list. If positive, we are to cycle right in 
	 * the list.  If negative, we are to cycle left.
	 *
	 * @return the shape change offset (0 if no change)
	 */
	public int getShape() {
		return shape;
	}

	/**
	 * Returns the controls change offset (0 if no change)
	 *
	 * The controls are arranged in a list. If positive, we are to cycle right in 
	 * the list.  If negative, we are to cycle left.
	 *
	 * @return the controls change offset (0 if no change)
	 */
	public int getControls() {
		return controls;
	}

	/**
	 * Returns the amount to change the test shape density
	 *
	 * The value is a factor that we will be converted to a log scale.  So +1 
	 * means multiply by 10, while -1 is divide by 10.
	 *
	 * @return the amount to change the test shape density
	 */
	public float getDensity() {
		return density;
	}

	/**
	 * Returns the amount to change the test shape friction
	 *
	 * The value is a factor that we will be converted to a log scale.  So +1 
	 * means multiply by 10, while -1 is divide by 10.
	 *
	 * @return the amount to change the test shape friction
	 */
	public float getFriction() {
		return friction; 
	}

	/**
	 * Returns the amount to change the test shape restitution
	 *
	 * The value is a factor that we will be converted to a log scale.  So +1 
	 * means multiply by 10, while -1 is divide by 10.
	 *
	 * @return the amount to change the test shape restitution
	 */
	public float getRestitution() {
		return restitution; 
	}

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return reset;
	}

	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exit;
  	}

	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get( 0 );
		} else {
			xbox = null;
		}
		linearForce = new Vector2();
		linearCache = new Vector2();
	}


	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method will first try a game controller, and then roll over to 
	 * a keyboard.
	 */
	public void readInput() {
		// Reset old values
		clear();

		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad();
			readKeyboard(true); // Read as a back-up
		} else {
			readKeyboard(false);
		}

		// Adjust to scale
		linearForce.x *= MOVE_SCALE;
		linearForce.y *= MOVE_SCALE;
		angularForce *= ROT_SCALE;
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 */
	private void readGamepad() {
	    // Reset options
		reset = xbox.getA();
		exit  = xbox.getBack();

		// Change physics values
		if (cooldown == 0) {
			if (xbox.getDPadUp()) {
				density *= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getDPadDown()) {
				density /= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getDPadRight()) {
				friction *= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getDPadLeft()) {
				friction /= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getRBumper()) {
				restitution *= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getLBumper()) {
				restitution /= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getX()) {
				restitution *= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getB()) {
				restitution /= 10;
				cooldown = COOLDOWN;
			}
			if (xbox.getRightTrigger() > 0.6) {
				controls += 1;
				cooldown = COOLDOWN;
			}
			if (xbox.getLeftTrigger() > 0.6) {
				controls -= 1;
				cooldown = COOLDOWN;
			}
		}
				
		// Move the shape
		linearForce.x = xbox.getRightX();
		linearForce.y = xbox.getRightY();
		linearForce.nor();
		
		angularForce = (float)Math.acos(linearForce.x);
		if (linearForce.y < 0) {
			angularForce += (float)Math.PI;
		}
	
		linearForce.x = xbox.getLeftX();
		linearForce.y = xbox.getLeftY();
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(boolean secondary) {
		// Reset options
		// Give priority to gamepad results
		reset = (secondary && reset) || (Gdx.input.isKeyPressed(Input.Keys.R));
		exit  = (secondary && exit) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		
		// Change physics values
		if (cooldown == 0) {
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
				density /= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
				density *= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
				friction /= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
   				friction *= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_5)) {
				restitution /= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_6)) {
				restitution *= 10;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_7)) {
				shape -= 1;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_8)) {
				shape += 1;
				cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_9)) {
					controls -= 1;
					cooldown = COOLDOWN;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_0)) {
				controls += 1;
				cooldown = COOLDOWN;
			}
		}

		// Move the shape
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			linearForce.x -= 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			linearForce.x += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			linearForce.y += 1.0f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			linearForce.y -= 1.0f;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			angularForce -= 1f;
		}  
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			angularForce += 1f;
		}	
    }
	
	/**
	 * Reset the input state.
	 */
	private void clear() {
		linearForce.set(0,0);
		angularForce = 0.0f;
		exit  = false;
		reset = false;
		
		density = 1.0f;
		friction = 1.0f;
		restitution = 1.0f;
		shape = 0;
		controls = 0;

		// Adjust button cooldown
		if (cooldown > 0) {
			cooldown--;
		}
	}
}