package com.pixmeg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;


public class WorldContactListener implements ContactListener {

    private Array<ParticleEffect> effects;

    public WorldContactListener(){
        effects = new Array<ParticleEffect>();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int collide = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (collide){
            case Constants.OBJECT_BIT | Constants.STAR_BIT:
                if(fixA.getFilterData().categoryBits == Constants.STAR_BIT){
                    ((Star)fixA.getUserData()).taken = true;

                    ParticleEffect effect = new ParticleEffect();
                    effect.load(Gdx.files.internal("particles/particle.p"),Gdx.files.internal("images"));
                    effect.setPosition(((Star)fixA.getUserData()).getPosition().x,((Star)fixA.getUserData()).getPosition().y);
                    effect.start();

                    effects.add(effect);

                }
                else
                {
                    ((Star)fixB.getUserData()).taken = true;

                    ParticleEffect effect = new ParticleEffect();
                    effect.load(Gdx.files.internal("particles/particle.p"),Gdx.files.internal("images"));
                    effect.setPosition(((Star)fixB.getUserData()).getPosition().x,((Star)fixB.getUserData()).getPosition().y);
                    effect.start();

                    effects.add(effect);
                }

                break;
        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public void update(SpriteBatch batch,float delta){
        for(ParticleEffect effect:effects){
            if(effect.isComplete()){
                effects.removeValue(effect,true);
                Constants.NO_OF_STARS -=1;
            }
            else {
                effect.draw(batch,delta);
            }
        }

    }
}
