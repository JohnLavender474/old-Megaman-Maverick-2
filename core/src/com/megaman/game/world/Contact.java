package com.megaman.game.world;

import com.megaman.game.entities.Entity;
import com.megaman.game.utils.objs.Pair;
import com.megaman.game.utils.objs.Wrapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Contact {

    public final Fixture f1;
    public final Fixture f2;
    public Pair<Fixture> mask;

    public boolean acceptMask(FixtureType t1, FixtureType t2) {
        if (f1.fixtureType == t1 && f2.fixtureType == t2) {
            mask = new Pair<>(f1, f2);
            return true;
        } else if (f2.fixtureType == t1 && f1.fixtureType == t2) {
            mask = new Pair<>(f2, f1);
            return true;
        }
        return false;
    }

    public boolean acceptMask(FixtureType t, Wrapper<FixtureType> w, FixtureType... s) {
        for (FixtureType f : s) {
            if (acceptMask(t, f)) {
                w.data = f;
                return true;
            }
        }
        return false;
    }

    public Entity mask1stEntity() {
        return mask.getFirst().entity;
    }

    public Entity mask2ndEntity() {
        return mask.getSecond().entity;
    }

    public Body mask1stBody() {
        return mask1stEntity().getComponent(BodyComponent.class).body;
    }

    public Body mask2ndBody() {
        return mask2ndEntity().getComponent(BodyComponent.class).body;
    }

    public <T> T mask1stData(String key, Class<T> tClass) {
        return tClass.cast(mask.getFirst().userData.get(key));
    }

    public <T> T mask2ndData(String key, Class<T> tClass) {
        return tClass.cast(mask.getSecond().userData.get(key));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contact c)) {
            return false;
        }
        if (f1.equals(c.f1)) {
            return f2.equals(c.f2);
        }
        if (f1.equals(c.f2)) {
            return f2.equals(c.f1);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 49;
        hash += 7 * f1.hashCode();
        hash += 7 * f2.hashCode();
        return hash;
    }

}
