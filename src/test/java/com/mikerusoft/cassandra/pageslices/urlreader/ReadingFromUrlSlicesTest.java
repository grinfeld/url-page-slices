package com.mikerusoft.cassandra.pageslices.urlreader;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import com.mikerusoft.cassandra.pageslices.utils.Utils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReadingFromUrlSlicesTest {

    @Test
    void whenReadingBytesLessThenBufferSize_expectedSliceWithOnly1Slice() throws Exception {
        byte[] bytes = "Hello".getBytes();

        ReadingFromUrlSlices.ChannelCreatorFactory factoryMock = createFactoryMock(
            Arrays.asList(
                byteBuffer -> {
                    byteBuffer.put(bytes,0, bytes.length);
                    return bytes.length;
                },
                byteBuffer -> -1
            )
        );

        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.of(factoryMock), 6);

        Flux<Slice> sliceFlux = readingFromUrlSlices.readFrom("stam");

        StepVerifier.create(sliceFlux)
                .expectNext(Slice.builder().content("Hello").slice(0).url("stam").build())
                .verifyComplete();
    }

    @Test
    void whenReadingTwiceMinus1CharacterThenBufferSize_expected2Slices() throws Exception {
        ReadingFromUrlSlices.ChannelCreatorFactory factoryMock = createFactoryMock(
            Arrays.asList(
                byteBuffer -> {
                    byteBuffer.put("Hello ".getBytes(),0, 6);
                    return 6;
                },
                byteBuffer -> {
                    byteBuffer.put("World".getBytes(),0, 5);
                    return 5;
                },
                byteBuffer -> -1
            )
        );

        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.of(factoryMock), 6);

        Flux<Slice> sliceFlux = readingFromUrlSlices.readFrom("stam");

        StepVerifier.create(sliceFlux)
                .expectNext(Slice.builder().content("Hello ").slice(0).url("stam").build())
                .expectNext(Slice.builder().content("World").slice(1).url("stam").build())
                .verifyComplete();
    }

    @Test
    void whenReadingExactlyTwiceAsBufferSize_expected2Slices() throws Exception {
        ReadingFromUrlSlices.ChannelCreatorFactory factoryMock = createFactoryMock(
            Arrays.asList(
                byteBuffer -> {
                    byteBuffer.put("Hello ".getBytes(),0, 6);
                    return 6;
                },
                byteBuffer -> {
                    byteBuffer.put("World!".getBytes(),0, 6);
                    return 6;
                },
                byteBuffer -> -1
            )
        );

        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.of(factoryMock), 6);

        Flux<Slice> sliceFlux = readingFromUrlSlices.readFrom("stam");

        StepVerifier.create(sliceFlux)
                .expectNext(Slice.builder().content("Hello ").slice(0).url("stam").build())
                .expectNext(Slice.builder().content("World!").slice(1).url("stam").build())
                .verifyComplete();
    }

    static ReadingFromUrlSlices.ChannelCreatorFactory createFactoryMock(List<Function<ByteBuffer, Integer>> results) throws Exception {
        ReadableByteChannel channel = mock(ReadableByteChannel.class);
        doAnswer(new Answer<Integer>() {
            private AtomicInteger i = new AtomicInteger(0);
            @Override
            public Integer answer(InvocationOnMock mock) throws Throwable {
                return results.get(i.getAndIncrement()).apply(mock.getArgument(0));
            }
        }).when(channel).read(any(ByteBuffer.class));

        return source -> channel;
    }

    @Test
    void whenReadThrowsIOException_expectedReturnedErrorSlice() throws Exception {
        ReadingFromUrlSlices.ChannelCreatorFactory factoryMock = createFactoryMockWithException(IOException.class);
        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.of(factoryMock), 10000);

        StepVerifier.create(readingFromUrlSlices.readFrom("stam"))
                .expectNext(Utils.errorSlice())
                .verifyComplete();
    }

    @Test
    void whenReadThrowsRuntime_IllegalArgumentException_expectedReturnedErrorSlice() throws Exception {
        ReadingFromUrlSlices.ChannelCreatorFactory factoryMock = createFactoryMockWithException(IllegalArgumentException.class);
        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.of(factoryMock), 10000);

        StepVerifier.create(readingFromUrlSlices.readFrom("stam"))
                .expectNext(Utils.errorSlice())
                .verifyComplete();
    }

    static ReadingFromUrlSlices.ChannelCreatorFactory createFactoryMockWithException(Class<? extends Throwable> t) throws Exception {
        ReadableByteChannel channel = mock(ReadableByteChannel.class);
        doThrow(t).when(channel).read(any(ByteBuffer.class));
        return source -> channel;
    }

    @Test
    @Disabled // set disabled since test will work only if computer where test runs has connectivity to internet
    void withInternetConnection_whenRealWorldUseCase_expectedMoreThanZeroSlicesWithValidParameters() {
        ReadingFromUrlSlices readingFromUrlSlices = new ReadingFromUrlSlices(Optional.empty(), 10000);
        List<Slice> slices = readingFromUrlSlices.readFrom("https://www.ynet.co.il/").collectList().block();

        assertThat(slices).isNotNull().isNotEmpty();
        assertThat(slices.get(0))
            .hasFieldOrPropertyWithValue("url", "https://www.ynet.co.il/")
            .hasFieldOrPropertyWithValue("slice", 0);

        assertThat(slices.get(slices.size() - 1))
            .hasFieldOrPropertyWithValue("url", "https://www.ynet.co.il/")
            .hasFieldOrPropertyWithValue("slice", slices.size() - 1);
    }
}