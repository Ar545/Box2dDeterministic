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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;


/**
 * Instances represents a single physics object with no Joints.
 */
public abstract class ComplexEntity {
	// The Box2D information
	/** The physics body (MOVEMENT) */
	protected Body body;
	protected Body leftBody;
	protected Body rightBody;

	/** The physics body for Box2D drawing. */
	protected Body draw_body;
	protected Body draw_leftBody;
	protected Body draw_rightBody;

	protected Joint joint;

	public void syncBodiesEach(Body corr_draw_body, Body real_body) {
		corr_draw_body.setType(real_body.getType());
		corr_draw_body.setTransform(real_body.getPosition(), real_body.getAngle());
		corr_draw_body.setAwake(real_body.isAwake());
		corr_draw_body.setBullet(real_body.isBullet());
		corr_draw_body.setLinearVelocity(real_body.getLinearVelocity());
		corr_draw_body.setSleepingAllowed(real_body.isSleepingAllowed());
		corr_draw_body.setFixedRotation(real_body.isFixedRotation());
		corr_draw_body.setGravityScale(real_body.getGravityScale());
		corr_draw_body.setAngularDamping(real_body.getAngularDamping());
		corr_draw_body.setLinearDamping(real_body.getLinearDamping());
	}

	public void syncBodies(){
		syncBodiesEach(draw_body, body);
		syncBodiesEach(draw_leftBody, leftBody);
		syncBodiesEach(draw_rightBody, rightBody);
	}

	/** The physics geometry (collisions) */
	protected Fixture fixture;
	/** The size (width and height) of the bounding box */
	protected Vec2 size;
	/** Whether this is a static object */
	protected boolean isStatic;

	// The drawing information
	/** The polygon region for drawing this shape */
	protected PolygonRegion poly;
	/** The drawing color */
	protected Color color;

	// Buffers for physical values
	/** The initial position of the physics object */
	protected Vec2 position;
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
	public Vec2 getPosition() {
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
	public void setPosition(Vec2 value) {
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
	public Vec2 getSize() {
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
	 * There are actually three body types: Static, Kinematic,
	 * and Dynamic.  But only Static and Dynamic are useful, so
	 * this is an easy way to go back and forth.
	 *
	 * @param value true if the body type is static
	 */
	public void setStatic(boolean value) {
		isStatic = value;
		if (body != null) {
			body.setType(isStatic ? BodyType.STATIC : BodyType.DYNAMIC);
		}
	}

	/** Sets the body type to kinetic. */
	public void setKinetic() {
		if (body != null) {
			body.setType(BodyType.KINEMATIC);
		}
	}


	/**
	 * Creates a new physics object for the given world
	 */
	protected ComplexEntity() {
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
		position = new Vec2();
	}
			
	/**
	 * Initialize the object and create the physics bodies
	 *
 	 * Delaying (from the constructor) this allows us to take advantage of reflection.
 	 *
 	 * @param world The physics world
 	 * @param size  The size of the bounding box
 	 */
	public void initialize(World world, World drawWorld, Vec2 size) {
		// Make a body, if possible
		BodyDef def = new BodyDef();
		def.type = (isStatic ? BodyType.STATIC : BodyType.DYNAMIC);
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
	protected abstract void makeFixture(Vec2 size);

	/**
     * Create the drawing shape information
     *
     * @param size The object bounding box
     */
	protected abstract void makeGraphics(Vec2 size);

	/** Gravity is proportional to one over radius squared */
	public void updateAttractionForce(ComplexEntity barrier) {
		Vec2 directedForce = barrier.getPosition().clone().sub(this.getPosition());
		float radius = directedForce.length();
		directedForce.normalize();
		directedForce.mul(1/(radius * radius));
		this.body.applyForceToCenter(directedForce);
//		restitution
	}

	/** Gravity is proportional to one over radius squared */
	public void updateDrawBodyAttractionForce(ComplexEntity barrier) {
		Vec2 directedForce = barrier.getPosition().clone().sub(this.getPosition());
		float radius = directedForce.length();
		directedForce.normalize();
		directedForce.mul(1/(radius * radius));
		this.draw_body.applyForceToCenter(directedForce);
//		restitution
	}

//	protected abstract void updatePhysics();

}