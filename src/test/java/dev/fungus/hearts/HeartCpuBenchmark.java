package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

public final class HeartCpuBenchmark {
    private static volatile int consumed;
    private static final int PLAYERS = 100;

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "standard" : args[0];
        boolean stress = mode.equals("unlimitedStress");
        boolean finite = mode.equals("extreme");
        boolean standard = mode.equals("standard");
        int players = stress ? 1 : PLAYERS;
        float maximum = stress || finite ? 1_000_000 : standard ? 20 : 1024;
        float absorption = stress || finite ? 1_000_000 : standard ? 0 : 64;
        HeartConfig config = new HeartConfig();
        if (finite) {
            config.normalHeartLimit = HeartConfig.MAX_HEART_LIMIT;
            config.absorptionHeartLimit = HeartConfig.MAX_HEART_LIMIT;
        }
        int samples = stress ? 1 : 500;
        StringBuilder report = new StringBuilder(String.format(Locale.ROOT,
                "CPU-only: %d visible entities, real Minecraft BufferBuilder; no GPU/game launch.%n" +
                "Health %.0f; absorption %.0f; normal limit %s; absorption limit %s.%n",
                players, maximum, absorption, finite ? "100" : "Unlimited", finite ? "100" : "Unlimited"));
        var allocations = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().threadId();
        allocations.setThreadAllocatedMemoryEnabled(true);
        try (ByteBufferBuilder storage = new ByteBufferBuilder(1_048_576)) {
            var pose = new PoseStack();
            pose.translate(0.1, 2.4, -6);
            pose.scale(0.025f, -0.025f, 0.025f);
            long coldStart = System.nanoTime();
            batch(storage, pose, players, maximum, absorption, config, !standard);
            double coldMs = (System.nanoTime() - coldStart) / 1e6;
            for (int i = 0; i < (stress ? 0 : 100); i++) {
                batch(storage, pose, players, maximum, absorption, config, !standard);
            }
            long bytesBefore = allocations.getThreadAllocatedBytes(thread);
            double[] times = new double[samples];
            for (int i = 0; i < times.length; i++) {
                long start = System.nanoTime();
                batch(storage, pose, players, maximum, absorption, config, !standard);
                times[i] = (System.nanoTime() - start) / 1e6;
            }
            long bytesAfter = allocations.getThreadAllocatedBytes(thread);
            Arrays.sort(times);
            report.append(String.format(Locale.ROOT,
                    "%s: first batch %.3f ms; median %.3f ms; p95 %.3f ms; max %.3f ms; heap %.0f bytes/batch; %d vertices/batch%n",
                    mode, coldMs, times[samples / 2], times[Math.min(samples - 1, samples * 95 / 100)],
                    times[samples - 1], (bytesAfter - bytesBefore) / (double) samples, consumed));
        }
        Path output = Path.of(System.getProperty("hearts.reportDirectory"), "heart-cpu-benchmark-" + mode + ".txt");
        Files.createDirectories(output.getParent());
        Files.writeString(output, report);
        System.out.print(report);
    }

    private static void batch(ByteBufferBuilder storage, PoseStack pose, int players, float maximum,
                              float absorption, HeartConfig config, boolean effects) {
        BufferBuilder builder = TestBuffers.create(storage);
        for (int player = 0; player < players; player++) {
            HeartFrame frame = new HeartFrame(maximum, maximum, absorption, maximum,
                    3, effects, effects ? HeartStyle.FROZEN : HeartStyle.NORMAL, effects, effects,
                    true, 1, HeartFrameTest.PALETTE, config.normalHeartLimit, config.absorptionHeartLimit, -1);
            frame.render(pose.last(), builder);
        }
        try (var mesh = builder.buildOrThrow()) { consumed = mesh.drawState().vertexCount(); }
        storage.clear();
    }
}
