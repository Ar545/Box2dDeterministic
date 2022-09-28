/*
 * Entity.cs
 *
 * Base model class to support collisions.
 *
 * This model is A LOT simpler than lab 4.  That is because we do not have
 * to worry about joints and the separation into simple and complex
 * physics objects.
 *
 * There were also some complicated features in Lab 4 with the properties.
 * That is because we wanted to allow resizing, which destroys the fixture.
 * This time we can do everything without destroying the fixture, or we 
 * we create new objects if we need new fixtures.  Hence we were able
 * to remove a lot of that overhead and make the game easier to understand.
 *
 * Author: Walker M. White
 * LibGDX version, 3/12/2015
 */
package edu.cornell.gdiac.box2d;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Instances represents a single physics object with no Joints.
 */
public abstract class Entity {
	// The Box2D information
	/** The physics body (MOVEMENT) */
	protected Body body;
	/** The physics body for Box2D drawing. */
	protected Body draw_body;
	public void syncBodies() {
		draw_body.setType(body.getType());
		draw_body.setTransform(body.getPosition(), body.getAngle());
//		draw_body.setEnabled(body.isEnabled());
		draw_body.setAwake(body.isAwake());
		draw_body.setBullet(body.isBullet());
		draw_body.setLinearVelocity(body.getLinearVelocity());
		draw_body.setSleepingAllowed(body.isSleepingAllowed());
		draw_body.setFixedRotation(body.isFixedRotation());
		draw_body.setGravityScale(body.getGravityScale());
		draw_body.setAngularDamping(body.getAngularDamping());
		draw_body.setLinearDamping(body.getLinearDamping());
	}
	/** The physics geometry (collisions) */
	protected Fixture fixture;
	/** The size (width and height) of the bounding box */
	protected Vector2 size;
	/** Whether this is a static object */
	protected boolean isStatic;

	// The drawing information
	/** The polygon region for drawing this shape */
	protected PolygonRegion poly;
	/** The drawing color */
	protected Color color;

	// Buffers for physical values
	/** The initial position of the physics object */
	protected Vector2 position;
	/** The initial density of the physics object */
	protected float density;
	/** The initial friction of the physics object */
	protected float friction;
	/** The initial restitution of the physics object */
	protected float restitution;

	/**
	 * Returns the position of this physics object
	 * 
	 * If the physics object has been initialized (e.g. it has an allocated body), 
	 * then it will return the current position.  It is unsafe to change the returned
	 * object.  Modifications to this object result in undefined behavior.
	 *
	 * @return the position of this physics object
	 */
	public Vector2 getPosition() {
		if (body == null) {
			return position;
		}
		return body.getPosition();
	}

	/**
	 * Sets the positition of the physics object
	 *
	 * If the object is already initialized (e.g. it has an allocated body), this will
	 * move the object to the given position.  Otherwise, this just sets the initial
	 * object position.
	 *
	 * This is necessary because Box2D will not allow us to move static objects after
	 * creation.
	 *
	 * @param value  the positition of the physics object
	 */
	public void setPosition(Vector2 value) {
		position.set(value);
		if (body != null) {
			body.setTransform(value,body.getAngle());
		}
	}
	
	/** 
	 * Returns the Box2D body for this object (MOVEMENT).
	 *
	 * This method returns a reference to the body, allowing you
	 * to apply forces directly to it.
	 *
	 * @return the Box2D body for this object (MOVEMENT).
	 */
	public Body getBody() {
		return body;
	}

	/**
	 * Returns the Box2D fixture for this object (COLLISIONS).
	 *
	 * This method returns a reference to the fixture, allowing
	 * you to change density, friction, or restitution.  However,
	 * changes to the density require that you reset the mass data.
	 *
	 * @return the Box2D fixture for this object (COLLISIONS).
	 */
	public Fixture getFixture() {
		return fixture;
	}
	
	/**
	 * Returns the color of this shape
	 *
	 * This method returns a reference to the color object, allowing it 
	 * to be changed.
	 *
	 * @return the color of this shape
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the bounding box size of this object.
	 *
	 * This is fixed on creation.  Changing the size means making
	 * a new fixture, and you saw what that did to the code in Lab 4.
	 * We do not allow the fixture to be changed anymore, so changing
	 * this value has no effect.
	 *
	 * @return the bounding box size of this object.
	 */
	public Vector2 getSize() {
		return size;
	}

	/**
	 * Returns the width of the bounding box.
	 *
	 * This is fixed on creation.  Changing the size means making
	 * a new fixture, and you saw what that did to the code in Lab 4.
	 * We do not allow the fixture to be changed anymore.
	 *
	 * @return the width of the bounding box.
	 */
	public float getWidth() {
		return size.x;
	}

	/**
	 * Returns the height of the bounding box.
	 *
	 * This is fixed on creation.  Changing the size means making
	 * a new fixture, and you saw what that did to the code in Lab 4.
	 * We do not allow the fixture to be changed anymore.
	 *
	 * @return the height of the bounding box.
	 */
	public float getHeight() {
		return size.y;
	}

	/**
	 * Returns the density for this object
	 *
	 * Together with the shape, the density determines the mass.
	 *
	 * @return the density for this object	
	 */
	public float getDensity() {
		return density;
	}
	
	/**
	 * Sets the density for this object
	 *
	 * If the fixture already exists, this requires that we reset the
	 * mass data on the body.
	 *
	 * @param value the density for this object	
	 */
	public void setDensity(float value) {
		density = value;
		if (fixture != null) {
			fixture.setDensity(density);
			body.resetMassData ();
		}
	}
	
	/**
	 * Returns the shape friction
	 *
	 * @return the shape friction
	 */
	public float getFriction() {
		return friction;
	}
		 
	/**
	 * Sets the shape friction
	 *
	 * Information is passed through to the fixture if it exists.
	 *
	 * @param value the shape friction
	 */
	public void setFriction(float value) {
		friction = value;
		if (fixture != null) {
			fixture.setFriction(friction);
		}
	}

	/**
	 * Returns the shape restitution
	 *
	 * @return the shape restitution
	 */
	public float getRestitution() {
		return restitution;
	}
		 
	/**
	 * Sets the shape restitution
	 *
	 * Information is passed through to the fixture if it exists.
	 *
	 * @param value the shape restitution
	 */
	public void setRestitution(float value) {
		restitution = value;
		if (fixture != null) {
			fixture.setRestitution(restitution);
		}
	}

	/**
	 * Returns true if the body type is static (not dynamic)
	 *
	 * There are actually three body types: Static, Kinematic, 
	 * and Dynamic.  But only Static and Dynamic are useful, so
	 * this is an easy way to go back and forth.
	 *
	 * @return true if the body type is static
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/**
	 * Sets whether the body type is static (if true) or dynamic
	 *
	 * There are actually three body types: Static, Kinematic, 
	 * and Dynamic.  But only Static and Dynamic are useful, so
	 * this is an easy way to go back and forth.
	 *
	 * @param value true if the body type is static
	 */
	public void setStatic(boolean value) {
		isStatic = value;
		if (body != null) {
			body.setType(isStatic ? BodyType.StaticBody : BodyType.DynamicBody);
		}
	}
	

	/**
	 * Creates a new physics object for the given world
	 */
	protected Entity() { 
		// No physics until we initialize
		body = null;
		draw_body = null;
		fixture = null;

		// Set the default values
		isStatic = false;
		density  = 1.0f;
		friction = 0.1f;
		restitution = 0.0f;
		color = new Color(Color.WHITE);
		position = new Vector2();
	}
			
	/**
	 * Initialize the object and create the physics bodies
	 *
 	 * Delaying (from the constructor) this allows us to take advantage of reflection.
 	 *
 	 * @param world The physics world
 	 * @param size  The size of the bounding box
 	 */
	public void initialize(World world, World drawWorld, Vector2 size) {
		// Make a body, if possible
		BodyDef def = new BodyDef();
		def.type = (isStatic ? BodyType.StaticBody : BodyType.DynamicBody);
		def.position.set(position);

		body = world.createBody(def);
		body.setUserData(this);

		draw_body = drawWorld.createBody(def);
		draw_body.setUserData(this); // TODO:
		
		makeFixture(size);
		makeGraphics(size);
	}
			
	/**
     * Draws the physics object.
     *
     * @param canvas The drawing context
     */
	public void draw(GameCanvas canvas) {
		// TO DO: Check units
		canvas.draw(poly, color, draw_body.getPosition().x, draw_body.getPosition().y, draw_body.getAngle());
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas, float offset) {
		// TO DO: Check units
		canvas.draw(poly, color, draw_body.getPosition().x + offset, draw_body.getPosition().y, draw_body.getAngle());
	}

	/**
     * Create the collision shape information
     *
     * @param size The object bounding box
     */
	protected abstract void makeFixture(Vector2 size);

	/**
     * Create the drawing shape information
     *
     * @param size The object bounding box
     */
	protected abstract void makeGraphics(Vector2 size);

}