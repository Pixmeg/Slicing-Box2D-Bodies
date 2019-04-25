package com.pixmeg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainScreen extends ScreenAdapter implements InputProcessor {
    private GameClass gameClass;
    private ShapeRenderer renderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private World world;
    private Box2DDebugRenderer b2dr;
    private RayCastCallback laserFired;
    private boolean rayEnters, rayLeaves;

    private boolean drawing;
    private Vector2 startPoint, endPoint, enterPoint, exitPoint, centerPoint;
    private Array<Vector2> firstSlice, secondSlice;

    private Array<Body> affectedByLaser;
    private Array<Vector2> entryPoints;

    private Array<Fixture> fixtures;

    private EarClippingTriangulator triangulator;


    public MainScreen(GameClass gameClass) {
        this.gameClass = gameClass;
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Constants.V_WIDTH,Constants.V_HEIGHT,camera);

        world = new World(new Vector2(0,-10f),true);
        b2dr = new Box2DDebugRenderer();

        startPoint = new Vector2();
        endPoint = new Vector2();
        enterPoint = new Vector2();
        exitPoint = new Vector2();
        centerPoint = new Vector2();

        affectedByLaser = new Array<Body>();
        entryPoints = new Array<Vector2>();

        firstSlice = new Array<Vector2>();
        secondSlice = new Array<Vector2>();

        fixtures = new Array<Fixture>();

        triangulator = new EarClippingTriangulator();

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

         createBox(Constants.V_WIDTH/2,10,Constants.V_WIDTH,20);
         createBox(500,300,160,160);
         createBox(300,300,160,160);


        laserFired = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

                Body affectedBody = fixture.getBody();

                PolygonShape affectedPolygon = (PolygonShape) fixture.getShape();
                int index = affectedByLaser.indexOf(affectedBody, true);

                if (index == -1) {
                    affectedByLaser.add(affectedBody);
                    entryPoints.add(new Vector2(point));
                    rayEnters = true;
                } else {
                    enterPoint.set(entryPoints.get(index));
                    exitPoint.set(point);

                    Vector2 rayCenter = new Vector2((point.x + entryPoints.get(index).x) / 2, (point.y + entryPoints.get(index).y) / 2);
                    centerPoint.set(rayCenter);

                    float rayAngle = (float) Math.atan2(entryPoints.get(index).y - point.y, entryPoints.get(index).x - point.x);


                    Array<Vector2> polyVertices = new Array<Vector2>();
                    for (int i = 0; i < affectedPolygon.getVertexCount(); i++) {
                        Vector2 vrt = new Vector2();
                        affectedPolygon.getVertex(i, vrt);
                        polyVertices.add(vrt);
                    }

                    Array<Vector2> newPolyVertices1 = new Array<Vector2>();
                    Array<Vector2> newPolyVertices2 = new Array<Vector2>();


                    newPolyVertices1.add(entryPoints.get(index));
                    newPolyVertices2.add(entryPoints.get(index));

                    for (int i = 0; i < polyVertices.size; i++) {
                        Vector2 worldpoint = new Vector2(affectedBody.getWorldPoint(polyVertices.get(i)));
                        float cutAngle = (float) Math.atan2(worldpoint.y - rayCenter.y, worldpoint.x - rayCenter.x) - rayAngle;

                        if (cutAngle < Math.PI * -1) {
                            cutAngle += Math.PI * 2;
                        }


                        if (cutAngle > 0 && cutAngle <= Math.PI) {
                            newPolyVertices1.add(worldpoint);
                           // firstSlice.add(worldpoint);
                        } else {
                            newPolyVertices2.add(worldpoint);
                           // secondSlice.add(worldpoint);
                        }

                    }

                    newPolyVertices1.add(exitPoint);
                    newPolyVertices2.add(exitPoint);

                    createSlice(newPolyVertices1);
                    createSlice(newPolyVertices2);
                    world.destroyBody(affectedBody);

                    rayLeaves = true;
                }

                    return 1;

            }
        };
    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Constants.BG_COLOR.r,Constants.BG_COLOR.g,Constants.BG_COLOR.b,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.getFixtures(fixtures);


        for(Fixture fixture:fixtures){
            PolygonShape shape = (PolygonShape) fixture.getShape();
            Transform transform = fixture.getBody().getTransform();
            float[] vrts = new float[2 * shape.getVertexCount()];
            Array<Vector2> polyVrts = new Array<Vector2>();
            for (int i = 0; i < shape.getVertexCount(); i++) {
                Vector2 vertex = new Vector2();
                shape.getVertex(i, vertex);
                fixture.getBody().getWorldPoint(vertex);
                transform.mul(vertex);
                vrts[2 * i] = vertex.x * Constants.PPM;
                vrts[2 * i + 1] = vertex.y * Constants.PPM;
                polyVrts.add(new Vector2(vertex.x * Constants.PPM, vertex.y * Constants.PPM));
            }

            ShortArray triangleIndices = new ShortArray();
            triangleIndices.addAll(triangulator.computeTriangles(vrts));

            Array<Vector2> triangles = new Array<Vector2>();

            for (int i = 0; i < triangleIndices.size; i += 1) {
                triangles.add(polyVrts.get(triangleIndices.get(i)));
            }

            fixture.setUserData(triangles);
        }

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Constants.SHAPE_COLOR);
        for(Fixture fixture:fixtures){
                Array<Vector2> vertices = (Array<Vector2>) fixture.getUserData();

                for (int i = 0; i < vertices.size; i += 3) {
                    renderer.triangle(vertices.get(i).x, vertices.get(i).y, vertices.get(i + 1).x, vertices.get(i + 1).y, vertices.get(i + 2).x, vertices.get(i + 2).y);
                }

        }

        if(drawing){
            renderer.rectLine(startPoint.x,startPoint.y, endPoint.x, endPoint.y, 1, Color.WHITE,Color.WHITE);
        }

      /*  if(rayLeaves){
            renderer.setColor(Color.YELLOW);
            renderer.circle(enterPoint.x * Constants.PPM, enterPoint.y * Constants.PPM, 6);
            renderer.setColor(Color.CYAN);
            renderer.circle(exitPoint.x * Constants.PPM, exitPoint.y * Constants.PPM, 6);
            renderer.setColor(Color.GREEN);
            renderer.circle(centerPoint.x*Constants.PPM,centerPoint.y*Constants.PPM,6);

            renderer.setColor(Color.WHITE);
            for (int i = 0;i<firstSlice.size;i++){
                Vector2 vrt = firstSlice.get(i);
                renderer.circle(vrt.x*Constants.PPM,vrt.y*Constants.PPM,7);
            }

            renderer.setColor(Color.RED);
            for (int i = 0;i<secondSlice.size;i++){
                Vector2 vrt = secondSlice.get(i);
                renderer.circle(vrt.x*Constants.PPM,vrt.y*Constants.PPM,7);
            }

            renderer.setColor(Color.WHITE);

        }*/
        renderer.end();


        b2dr.render(world,camera.combined.scl(Constants.PPM));
        camera.update();
        world.step(1/60f,6,2);

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            System.out.println("BC          "+world.getBodyCount());
        }

    }


    public Body createBox(float x, float y, float w, float h){
        Body body;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(x/Constants.PPM, y/Constants.PPM);

        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w/(2*Constants.PPM),h/(2*Constants.PPM));

        body.createFixture(shape,1);
        return body;
    }

    public Body createSlice(Array<Vector2> vertices){
        float[] vrts = new float[2*vertices.size];
        for(int i = 0;i<vertices.size;i++){
            vrts[2*i] = vertices.get(i).x;
            vrts[2*i +1] = vertices.get(i).y;
        }
        Body body;
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.set(vrts);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;

        body.createFixture(fdef);

        shape.dispose();

        return body;
    }



    @Override
    public void resize(int width, int height) {
        viewport.update(width,height,true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        rayEnters = false;
        rayLeaves = false;

         drawing = true;
        firstSlice.clear();
        secondSlice.clear();

        Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX,screenY));
        startPoint.set(worldCoordinates);
        endPoint.set(worldCoordinates);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 worldCoordinates = viewport.unproject(new Vector2(screenX,screenY));
        endPoint.set(worldCoordinates);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        drawing = false;

        Vector2 sp = new Vector2(startPoint.x/Constants.PPM,startPoint.y/Constants.PPM);
        Vector2 ep = new Vector2(endPoint.x/Constants.PPM,endPoint.y/Constants.PPM);

        if(sp.x != ep.x || sp.y != ep.y) {
            world.rayCast(laserFired, sp, ep);
            world.rayCast(laserFired, ep, sp);
        }

        affectedByLaser.clear();
        entryPoints.clear();

        return true;
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }


    public World getWorld() {
        return world;
    }


}
