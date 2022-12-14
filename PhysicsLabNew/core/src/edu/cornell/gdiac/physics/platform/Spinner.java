/*
 * Spinner.java
 *
 * This class provides a spinning rectangle on a fixed pin.  We did not really need
 * a separate class for this, as it has no update.  However, ComplexObstacles always
 * make joint management easier.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.obstacle.*;

public class Spinner extends ComplexObstacle {
	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The primary spinner obstacle */
	private BoxObstacle barrier;

	private WheelObstacle pin;

	/**
	 * Creates a new spinner with the given physics data.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 * @param data  	The physics constants for this rope bridge
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public Spinner(JsonValue data, float width, float height) {
		super(data.get("pos").getFloat(0),data.get("pos").getFloat(1));
		setName("spinner");
		this.data = data;

		// Create the barrier
		float x = data.get("pos").getFloat(0);
		float y = data.get("pos").getFloat(1);
		barrier = new BoxObstacle(x,y,width,height);
		barrier.setName("barrier");
		barrier.setDensity(data.getFloat("high_density", 0));
		bodies.add(barrier);

		//#region INSERT CODE HERE
		// Create a pin to anchor the barrier
		// Radius:  data.getFloat("radius")
		// Density: data.getFloat("low_density")
		// Name: "pin"

		pin = new WheelObstacle(data.getFloat("radius"));
		pin.setDensity(data.getFloat("low_density"));
		pin.setName("pin");

//		bodies.insert(0, pin);
		//#endregion
	}

	/** THIS IS AN INCORRECT IMPLEMENTATION OF DOUBLE WORLD
	 * TODO :: implement true double-world on platform world
	 * @param world Box2D world to store joints
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world, World drawWorld) {
		boolean real = createJoints(world);
		boolean draw = createJoints(drawWorld);
		return real && draw;
	}

	/**
	 * Creates the joints for this object.
	 *
	 * We implement our custom logic here.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;

		//#region INSERT CODE HERE
		// Attach the barrier to the pin here
		Vector2 anchor1 = new Vector2();
		Vector2 anchor2 = new Vector2(data.get("pos").getFloat(0), data.get("pos").getFloat(1));

//		Obstacle pin = bodies.get(0);
		pin.setBodyType(BodyDef.BodyType.StaticBody);
		pin.activatePhysics(world);

		// Definition for a revolute joint
		RevoluteJointDef jointDef = new RevoluteJointDef();

		// Initial joint
		jointDef.bodyA = barrier.getBody();
		jointDef.bodyB = pin.getBody();
		jointDef.localAnchorA.set(anchor1);
		jointDef.localAnchorB.set(anchor2);
		jointDef.collideConnected = false;
		Joint joint = world.createJoint(jointDef);
		joints.add(joint);

		//#endregion

		return true;
	}

	public void setTexture(TextureRegion texture) {
		barrier.setTexture(texture);
	}

	public TextureRegion getTexture() {
		return barrier.getTexture();
	}
}
