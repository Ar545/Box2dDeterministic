/*
 * Triangle.java
 *
 * Triangle-shaped model to support collisions.
 *
 * This shows how to work with a single, simple triangle.
 *
 * Author: Walker M. White
 * LibGDX Value, 3/12/2015
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
 * A triangular physics object.
 */
public class Triangle extends Entity {
	
	/** Shape information for this box */
	protected PolygonShape shape;
	
	/**
	 * Create a new box, but do not initialize it yet.
	 */
	public Triangle() { } 

	/**
	 * Returns an array of vertices representing an equilateral triangle
	 *
	 * This array is used for both physics AND drawing.
	 *
	 * @return an array of vertices representing an equilateral triangle
	 */
	private float[] makeTriangle(Vec2 size) {
		float[] vertices = new float[6];
		float altitude  = (float)Math.sqrt(3) * size.y / 2.0f;
		float halfWidth = size.x / 2.0f;

		vertices[0] = -halfWidth;
		vertices[1] = altitude / 3.0f;
		vertices[2] = 0.0f;
		vertices[3] = -2.0f * altitude / 3.0f;
		vertices[4] = halfWidth;
		vertices[5] = altitude / 3.0f;
		
		return vertices;
	}

	/**
	 * Returns an array of vertices representing an equilateral triangle
	 *
	 * This array is used for both physics AND drawing.
	 *
	 * @return an array of vertices representing an equilateral triangle
	 */
	private Vec2[] makeTriangleVec2(Vec2 size) {
		Vec2[] vertices = new Vec2[3];
		float altitude  = (float)Math.sqrt(3) * size.y / 2.0f;
		float halfWidth = size.x / 2.0f;

//		vertices[0] = -halfWidth;
//		vertices[1] = altitude / 3.0f;
		vertices[0] = new Vec2(-halfWidth, altitude / 3.0f);
//		vertices[2] = 0.0f;
//		vertices[3] = -2.0f * altitude / 3.0f;
		vertices[1] = new Vec2(0.0f, -2.0f * altitude / 3.0f);
//		vertices[4] = halfWidth;
//		vertices[5] = altitude / 3.0f;
		vertices[2] = new Vec2(halfWidth, altitude / 3.0f);

		return vertices;
	}
	
	/**
     * Create the collision shape information
     *
     * @param size The object bounding box
     */
	protected void makeFixture(Vec2 size) {
		shape = new PolygonShape();
		shape.set(makeTriangleVec2(size), 3);

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
		float[] vertices = makeTriangle(size);

		// Indices are trivial
		short[] indices = new short[3];
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;

		poly = new PolygonRegion(new TextureRegion(), vertices, indices);
	}
}