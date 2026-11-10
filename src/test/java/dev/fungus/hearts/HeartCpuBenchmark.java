package dev.fungus.hearts;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
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
        StringBuilder report = new StringBuilder("CPU-only: 100 visible players, real Minecraft BufferBuilder; no GPU/game launch.\n");
        var allocations = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().threadId();
        allocations.setThreadAllocatedMemoryEnabled(true);
        boolean worst = args.length > 0 && args[0].equals("extreme");
        try (ByteBufferBuilder storage = new ByteBufferBuilder(1_048_576)) {
            var pose = new PoseStack();
            pose.translate(0.1, 2.4, -6);
            pose.scale(0.025f, -0.025f, 0.025f);
            long coldStart = System.nanoTime();
            batch(storage, pose, worst);
            double coldMs = (System.nanoTime() - coldStart) / 1e6;
            for (int i = 0; i < 100; i++) batch(storage, pose, worst);
            long bytesBefore = allocations.getThreadAllocatedBytes(thread);
            double[] times = new double[500];
            for (int i = 0; i < times.length; i++) {
                long start = System.nanoTime();
                batch(storage, pose, worst);
                times[i] = (System.nanoTime() - start) / 1e6;
            }
            long bytesAfter = allocations.getThreadAllocatedBytes(thread);
            Arrays.sort(times);
            report.append(String.format(Locale.ROOT,
                    "%s: first batch %.3f ms; median %.3f ms; p95 %.3f ms; max %.3f ms; heap %.0f bytes/batch; %d vertices/batch%n",
                    worst ? "Extreme health + absorption + blinking" : "Standard ten hearts",
                    coldMs, times[250], times[475], times[499],
                    (bytesAfter - bytesBefore) / 500.0, consumed));
        }
        Path output = Path.of(System.getProperty("hearts.reportDirectory"), "heart-cpu-benchmark-" + (worst ? "extreme" : "standard") + ".txt");
        Files.createDirectories(output.getParent());
        Files.writeString(output, report);
        System.out.print(report);
    }

    private static void batch(ByteBufferBuilder storage, PoseStack pose, boolean worst) {
        BufferBuilder builder = TestBuffers.create(storage);
        for (int player = 0; player < PLAYERS; player++) {
            float maximum = worst ? 1_000_000 : 20;
            HeartFrame frame = new HeartFrame(maximum, maximum, worst ? 1_000_000 : 0, maximum,
                    3, worst, worst ? HeartStyle.FROZEN : HeartStyle.NORMAL, worst, worst,
                    true, 1, HeartFrameTest.PALETTE);
            frame.render(pose.last(), builder);
        }
        try (var mesh = builder.buildOrThrow()) { consumed = mesh.drawState().vertexCount(); }
        storage.clear();
    }
}
