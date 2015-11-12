package com.github.b0ch3nski.rtla.common.model;

import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.*;

import java.util.concurrent.TimeUnit;

/**
 * @author bochen
 */
@SuppressWarnings("all")
@State(Scope.Benchmark)
public class SerializationBenchmarkTest {

//    @Test
    public void launchBenchmark() throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(getClass().getName() + ".*")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.seconds(5))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(5))
                .measurementIterations(50)
                .shouldFailOnError(false)
                .forks(1)
                .threads(1)
                .build();
        new Runner(opts).run();
    }

    @Param({"100", "250", "500", "750", "1000", "2500", "5000", "7500", "10000"})
    public int kryoBufferSize;

    @Param({"-1", "100", "250", "500", "750", "1000", "2500", "5000", "7500", "10000"})
    public int maxKryoBufferSize;

    @State(Scope.Thread)
    public static class BenchmarkState {
        public SimplifiedLogSerializer serializer;
        public SimplifiedLog toSerialize;

        @Setup(Level.Trial)
        public void initialize() {
            serializer = new SimplifiedLogSerializer();
            toSerialize = RandomLogFactory.create();
        }
    }

    @Benchmark
    public byte[] serialize(BenchmarkState state) {
        return state.serializer.toBytes(state.toSerialize, kryoBufferSize, maxKryoBufferSize);
    }
}
