package com.pixmeg;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD {

    DemoScreen demoScreen;
    public Stage stage;
    Viewport viewport;
    Skin skin;

    private Label Slices;

    public HUD(DemoScreen demoScreen){
        this.demoScreen = demoScreen;
        viewport = new ExtendViewport(Constants.V_WIDTH,Constants.V_HEIGHT);
        stage = new Stage(viewport,demoScreen.getBatch());
        skin = demoScreen.getSkin();

        Table table = new Table();
        table.setFillParent(true);
        //  table.debug();
        table.top();


        Label label = new Label("Level  ",skin);
        Label level = new Label("2",skin);

        Slices = new Label("",skin);

        table.add(label).left().padLeft(30);
        table.add(level).left();
        table.add(Slices).expandX().padRight(100);

        stage.addActor(table);
    }


    public void createDialog(final GameClass gameClass){
        Constants.NO_OF_STARS = 1;

        Dialog dialog = new Dialog("",skin,"gameOver"){

            @Override
            public float getPrefWidth() {
                return 400*0.8f;
            }

            @Override
            public float getPrefHeight() {
                return 240*0.8f;
            }

            @Override
            protected void result(Object object) {
                boolean value = (Boolean)object;
                if(value) {
                    gameClass.setScreen(new DemoScreen(gameClass));
                }
            }

        };

        TextButton home = new TextButton("",skin,"home");
        TextButton replay = new TextButton("",skin,"replay");
        TextButton next = new TextButton("",skin,"next");

        dialog.getButtonTable().defaults().width(52).height(52).padBottom(20).spaceRight(40);
        dialog.button(home,false);
        dialog.button(replay,true);
        dialog.button(next,false);

        dialog.show(stage);

    }

}
