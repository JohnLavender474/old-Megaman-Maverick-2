package com.megaman.game.screens.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldVals;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public class TextHandle implements Drawable {

    public static final String FONT_SRC = "Megaman10Font.ttf";
    public static final int FONT_SIZE = Math.round(WorldVals.PPM / 2f);

    private final BitmapFont font;
    private final GlyphLayout layout;

    public Vector2 pos;
    public boolean centerX;
    public boolean centerY;
    public Supplier<String> textSupplier;

    public TextHandle(Vector2 pos) {
        this(pos, "");
    }

    public TextHandle(Vector2 pos, String text) {
        this(pos, text, false, false);
    }

    public TextHandle(Vector2 pos, Supplier<String> textSupplier) {
        this(pos, textSupplier, false, false);
    }

    public TextHandle(Vector2 pos, String text, boolean centerX, boolean centerY) {
        this(pos, () -> text, centerX, centerY);
    }

    public TextHandle(Vector2 pos, Supplier<String> textSupplier, boolean centerX, boolean centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.pos = pos;
        layout = new GlyphLayout();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_SRC));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = FONT_SIZE;
        font = generator.generateFont(parameter);
        generator.dispose();
        this.textSupplier = textSupplier;
    }

    public void setText(String text) {
        setTextSupplier(() -> text);
    }

    public void clear() {
        setText("");
    }

    @Override
    public void draw(SpriteBatch batch) {
        layout.setText(font, textSupplier.get().toUpperCase());
        float x = centerX ? pos.x - layout.width / 2f : pos.x;
        float y = centerY ? pos.y - layout.height / 2f : pos.y;
        font.draw(batch, layout, x, y);
    }

}
