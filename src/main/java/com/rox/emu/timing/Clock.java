package com.rox.emu.timing;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Clock {
    /**
     * 1 MHz: 1 million cycles per second.
     *
     * NES is 1.79MHz or 1.66MHz in PAL
     */
//    private final double clockSpeed;
//    private final int groupTimingInMillis;
//    //We can't emulate millions of individual ticks per second so we group them into an emulated group cycle
//    private final long emulatedTicksInGroupTicks;
//    private final Set<ClockWatcher> watchers = new HashSet<>();
//
//    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Some class which wants to be informed when the clock progresses */
    public interface ClockWatcher {
        void start();
        void tick();
        void stop();
    }
//
//    Clock(final double clockSpeed){
//        this.clockSpeed = clockSpeed;
//
//        //TODO this should be programatically determined as the case for 1, for example cannot be divided
//        double roundTicks = clockSpeed / 1000;  //maps directly to group count
//        double additionalTicks = clockSpeed % 1000; //how do we assign these additional cycles
//
//        /**
//         * TODO (The intent of this)
//         *
//         * To take a second (1000ms) and as evenly as possible distributed execution of clock ticks across it.
//         *
//         * If we have under 1000MHz (ticks per second) then it's that number of ticks in the second
//         *   If we have a multiple of 1000MHz then we need to group because Java isn't that time-granular.  So 1 tick for every multiple of 1000MHz seperated by 1ms
//         *     The remainder needs distributed into these time windows somehow
//         * If we have 1000MHz then it's 1000 ticks executed with a 1ms gap
//         */
//
//        if (clockSpeed < 1000){
//            //One group per cycle
//            this.groupTimingInMillis = (int)(1000 / clockSpeed);
//            this.emulatedTicksInGroupTicks = 1;
//            //TODO how do we get timings between groups sub 1000
//        }else{
//            this.groupTimingInMillis = 1;
//            emulatedTicksInGroupTicks = Math.round(clockSpeed / getTickGroupCount());
//        }
//        System.out.println("TODO");
//    }
//
//    public int getTickGroupCount(){
//        return 1000 / groupTimingInMillis;
//    }
//
//    public long getTickGroupSize(){
//        return emulatedTicksInGroupTicks;
//    }
//
//    public void start(){
//        executor.submit(() -> {
//            final String threadName = Thread.currentThread().getName();
//            System.out.println("'" + threadName + "' running...");
//            watchers.forEach(ClockWatcher::start);
//
//            while (true){
//                System.out.println("\n<Click cycle starting>");
//                //execute n ticks
//                for (int tick=0; tick<emulatedTicksInGroupTicks; tick++){
//                    watchers.forEach(ClockWatcher::tick);
//                }
//                Thread.sleep(groupTimingInMillis);
//            }
//        });
//        System.out.println("Thread submitted to executor");
//    }
//
//    public void stop(){
//        executor.shutdown();
//        executor.shutdownNow();
//        watchers.forEach(ClockWatcher::stop);
//    }
//
//    public boolean watch(final ClockWatcher watcher){
//        return watchers.add(watcher);
//    }
//
//    public boolean unwatch(final ClockWatcher watcher){
//        return watchers.add(watcher);
//    }
//
//    public static void main(String[] args){
//        final Clock myClock = new Clock(1.66f);
//        myClock.watch(new ClockWatcher() {
//            @Override
//            public void start() {
//                System.out.println("START");
//            }
//
//            @Override
//            public void tick() {
//                System.out.print("TICK...");
//            }
//
//            @Override
//            public void stop() {
//                System.out.println("\n...STOP");
//            }
//        });
//
//        myClock.start();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        myClock.stop();
//
//    }
}
