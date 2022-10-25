/*
 * Ellipse.java
 *
 * Elliptical-shaped model to support collisions.
 *
 * Sometimes you want ellipses instead of boxes. There is no ellipse shape; you
 * have to build one with polygons.  Unfortunately, there is a limit on the number
 * of vertices in a Box2D polygon.  That limit is 8.  So if you want something
 * smoother, you either need to use a circle (same width and height), or you need
 * to build the ellipse from multiply polygons like we did with PolygonObject
 * in Lab 4.
 *
 * Author: Walker M. White
 * LibGDX version, 3/12/2015
 */
package edu.cornell.gdiac.box2d.shape;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
//import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.box2d.*;  // For GameCanvas and Entity
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.common.Vec2;

/**
 * An elliptical physics object.
 */
public class Ellipse extends Entity {
	/** Number of polygon edges to use */
	private static final int NUM_EDGES = 8; // APPEARS TO BE THE BOX2D MAXIMUM

	/** Shape information for this ellipse */
	protected PolygonShape shape;

	/**
	 * Create a new ellipse, but do not initialize it yet.
	 */
	public Ellipse() {} 

	/**
	 * Returns an array of vertices representing an ellipse
	 *
	 * This array is used for both physics AND drawing.
	 *
	 * @return an array of vertices representing an ellipse
	 */
	private float[] makeEllipse(Vec2 size) {
		// Make the ellipse centered at origin
		float stepSize = 2*(float)Math.PI / NUM_EDGES;
		
		float[] vertices = new float[2*NUM_EDGES];
		float xRadius = size.x / 2.0f;
		float yRadius = size.y / 2.0f;
		for (int ii = 0; ii < NUM_EDGES; ii++) {
			double angle =  stepSize * ii;
			vertices[2*ii  ] = (float)Math.cos(angle) * xRadius;
			vertices[2*ii+1] = (float)Math.sin(angle) * yRadius;
		}
		
		return vertices;
	}
	
	/**
     * Create the collision shape information
     *
     * @param size The object bounding box
     */
	protected void makeFixture(Vec2 size) {
		shape = new PolygonShape();
		shape.set(makeEllipse(size));

		// Create the fixture
		FixtureDef def = new FixtureDef();
		def.density = density;
		def.friction = friction;
		def.restitution = restitution;
		def.shape = shape;

		fixture = body.createFixture(def);
//		shape.dispose(); // Do not need it anymore
	}

	/**
     * Create the drawing shape information
     *
     * @param size The object bounding box
     */
	protected void makeGraphics(Vec2 size) {
		float[] vertices = makeEllipse(size);

		// Triangle fans were removed in XNA 4.0.  Indices are a way around this.
		short[] indices = new short[3 * (NUM_EDGES-2)];
		for (int ii = 0; ii < NUM_EDGES - 2; ii++) {
			indices[3 * ii] = 0;
			indices[3 * ii + 1] = (short)(ii + 1);
			indices[3 * ii + 2] = (short)(ii + 2);
		}

		poly = new PolygonRegion(new TextureRegion(), vertices, indices);
    }
}