package com.megaman.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.megaman.game.utils.interfaces.Drawable;
import com.megaman.game.world.WorldConstVals;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public class TextHandle implements Drawable {

    public static final String FONT_SRC = "Megaman10Font.ttf";
    public static final int FONT_SIZE = Math.round(WorldConstVals.PPM / 2f);

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public final Vector2 center = new Vector2();
    public Supplier<String> textSupplier;

    public TextHandle(Vector2 center) {
        this(center, () -> "");
    }

    public TextHandle(Vector2 center, String text) {
        this(center, () -> text);
    }

    public TextHandle(Vector2 center, Supplier<String> textSupplier) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_SRC));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = FONT_SIZE;
        font = generator.generateFont(parameter);
        generator.dispose();
        this.center.set(center);
        this.textSupplier = textSupplier;
    }

    public void setText(String text) {
        textSupplier = () -> text;
    }

    @Override
    public void draw(SpriteBatch batch) {
        layout.setText(font, textSupplier.get().toUpperCase());
        font.draw(batch, layout, center.x - (layout.width / 2f), center.y);
    }

}
