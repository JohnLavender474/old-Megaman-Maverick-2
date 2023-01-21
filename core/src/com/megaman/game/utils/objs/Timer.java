package com.megaman.game.utils.objs;

import com.badlogic.gdx.utils.Array;
import com.megaman.game.utils.interfaces.Resettable;
import com.megaman.game.utils.interfaces.Updatable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

@Getter
public class Timer implements Updatable, Resettable {

    private final Array<TimeMarkedRunnable> tmRunnables;
    private final Queue<TimeMarkedRunnable> tmrQ;

    private float time;
    private float duration;
    private boolean justFinished;

    @Setter
    public Runnable runOnEnd;

    public Timer() {
        this(0f);
    }

    public Timer(Timer timer) {
        tmRunnables = new Array<>();
        tmrQ = new PriorityQueue<>();
        tmRunnables.addAll(timer.tmRunnables);
        tmrQ.addAll(timer.tmrQ);
        time = timer.time;
        duration = timer.duration;
        justFinished = timer.justFinished;
        runOnEnd = timer.runOnEnd;
    }

    public Timer(float duration, Runnable runOnEnd) {
        this(duration);
        this.runOnEnd = runOnEnd;
    }

    public Timer(float duration, TimeMarkedRunnable... tmRunnables) {
        this(duration, false, tmRunnables);
    }

    public Timer(float duration, boolean setToEnd, TimeMarkedRunnable... tmRunnables) {
        this(duration, setToEnd, Arrays.asList(tmRunnables));
    }

    public Timer(float duration, Iterable<TimeMarkedRunnable> tmRunnables) {
        this(duration, false, tmRunnables);
    }

    public Timer(float duration, boolean setToEnd, Iterable<TimeMarkedRunnable> tmRunnables) {
        this.tmRunnables = new Array<>();
        this.tmrQ = new PriorityQueue<>();
        setDuration(duration);
        for (TimeMarkedRunnable tmr : tmRunnables) {
            if (tmr.time() < 0f || tmr.time() > duration) {
                throw new IllegalArgumentException();
            }
            this.tmRunnables.add(tmr);
        }
        if (setToEnd) {
            setToEnd();
        } else {
            reset();
        }
    }

    public Timer setDuration(float duration) {
        if (duration < 0f) {
            throw new IllegalStateException("Timer duration cannot be less than zero");
        }
        this.duration = duration;
        return this;
    }

    public float getRatio() {
        return duration > 0f ? Math.min(time / duration, 1f) : 0f;
    }

    public boolean isAtBeginning() {
        return time == 0f;
    }

    public boolean isFinished() {
        return time >= duration;
    }

    public boolean isJustFinished() {
        return justFinished;
    }

    public Timer setToEnd() {
        time = duration;
        return this;
    }

    @Override
    public void update(float delta) {
        boolean finishedBefore = isFinished();
        time = Math.min(duration, time + delta);
        while (!tmrQ.isEmpty() && tmrQ.peek().time() <= time) {
            TimeMarkedRunnable tmr = tmrQ.poll();
            if (tmr == null || tmr.runnable() == null) {
                continue;
            }
            tmr.runnable().run();
        }
        justFinished = !finishedBefore && isFinished();
        if (runOnEnd != null && justFinished) {
            runOnEnd.run();
        }
    }

    @Override
    public void reset() {
        time = 0f;
        tmrQ.clear();
        for (TimeMarkedRunnable tmr : tmRunnables) {
            tmrQ.add(tmr);
        }
    }

}