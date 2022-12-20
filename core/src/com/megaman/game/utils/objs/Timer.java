package com.megaman.game.utils.objs;

import com.badlogic.gdx.utils.Array;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;

@Getter
public class Timer {

    public Array<TimeMarkedRunnable> tmRunnables = new Array<>();

    private final Queue<TimeMarkedRunnable> tmrQ = new PriorityQueue<>();

    private float time;
    private float duration;
    private boolean justFinished;

    public Timer() {
        this(1f);
    }

    public Timer(Timer timer) {
        tmRunnables.addAll(timer.tmRunnables);
        tmrQ.addAll(timer.tmrQ);
        time = timer.time;
        duration = timer.duration;
        justFinished = timer.justFinished;
    }

    public Timer(float duration, TimeMarkedRunnable... tmRunnables) {
        this(duration, false, tmRunnables);
    }

    public Timer(float duration, boolean setToEnd, TimeMarkedRunnable... tmRunnables) {
        this(duration, setToEnd, Arrays.asList(tmRunnables));
    }

    public Timer(float duration, Collection<TimeMarkedRunnable> tmRunnables) {
        this(duration, false, tmRunnables);
    }

    public Timer(float duration, boolean setToEnd, Collection<TimeMarkedRunnable> tmRunnables) {
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

    public void setDuration(float duration) {
        if (duration < 0f) {
            throw new IllegalStateException();
        }
        this.duration = duration;
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

    public void setToEnd() {
        time = duration;
    }

    public boolean update(float delta) {
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
        return isFinished();
    }

    public boolean reset() {
        boolean isFinished = isFinished();
        time = 0f;
        tmrQ.clear();
        for (TimeMarkedRunnable tmr : tmRunnables) {
            tmrQ.add(tmr);
        }
        return isFinished;
    }

}