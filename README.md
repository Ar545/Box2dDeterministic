# Box2d Deterministic Experiments
 
This experiment aims to find the source of indeterministic of Box2D physics in Java

**Exp I: Under the box2D directory**
There are three objects in the world: 
- Avatar:

An free-moving object with full restitution (elasticity = 1) experience a gravity force to the barrier. (Mag of force is prop to 1/radius^2)
- Barrier:

An static object.
- Car:

An kinetic object that experience constant linear velocity on +x direction. 
Used to check if the time frame goes by 0.003s (check position increase for isolated object with only linear impluse).

Plan to introduce additional objects:
- Alternative avatar:

An free-moving object with full restitution (elasticity = 1) experience a linear impulse toward the barrier. 

**Indeterministic behavior observed on avatar even though physics are updated on 0.003f(s) steps.**

Two experiment is undergoing:
- On main branch:
World 1 update physics according to frame rate.
World 2 update physics by 0.015f(s) for the first time, then according to the previous frame rate.
- On Inverted_sequence branch:
World 1 update physics according to frame rate.
World 2 do not update physics for the first time period, then update physics accroding to the current frame rate, and before it updates the physics by the first frame rate in a duplicate world to conpare the results.

**Concern on potential source of indeterministic:**
- Floating point numbers
- Player force input are calculated over period of frame rate



