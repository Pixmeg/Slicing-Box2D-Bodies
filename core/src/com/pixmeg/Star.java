package com.pixmeg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Star extends Sprite {

    private DemoScreen demoScreen;
    private World world;
    private Body body;
    private float x,y;

    public boolean taken;

    public Star(DemoScreen demoScreen, float x, float y){
        this.demoScreen = demoScreen;
        world = demoScreen.getWorld();
        this.x = x;
        this.y = y;

        createBody();

        Texture texture = new Texture(Gdx.files.internal("images/star.png"));

        setRegion(texture);

    }

    public void update(SpriteBatch batch, float delta){
            setBounds(body.getPosition().x*Constants.PPM-getWidth()/2,body.getPosition().y*Constants.PPM-getHeight()/2,getRegionWidth()/5,getRegionHeight()/5);
            draw(batch);

    }

    private void createBody(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(x/Constants.PPM,y/Constants.PPM);
        bdef.type = BodyDef.BodyType.StaticBody;

        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(13/Constants.PPM,13/Constants.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;
        fdef.isSensor = true;

        fdef.filter.categoryBits = Constants.STAR_BIT;
        fdef.filter.maskBits = Constants.OBJECT_BIT;

        body.createFixture(fdef).setUserData(this);
        shape.dispose();
    }

    public void destroyBody(){
        world.destroyBody(body);
    }

    public Vector2 getPosition(){
        return new Vector2(x,y);
    }
}
