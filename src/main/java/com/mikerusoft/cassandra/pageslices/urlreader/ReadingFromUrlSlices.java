package com.mikerusoft.cassandra.pageslices.urlreader;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;
import com.mikerusoft.cassandra.pageslices.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mikerusoft.cassandra.pageslices.utils.Utils.rethrow;

@Slf4j
@Service
public class ReadingFromUrlSlices implements ReadingSlicesService<String> {

    private int bufferSizeBytes;
    private ChannelCreatorFactory factory;

    public ReadingFromUrlSlices(Optional<ChannelCreatorFactory> channelCreatorFactory,
                                @Value("${buffer.size.bytes:10000}") int bufferSizeBytes) {
        this.bufferSizeBytes = bufferSizeBytes;
        this.factory = channelCreatorFactory.orElse(this::defaultReadableByteChannel);
    }

    private ReadableByteChannel defaultReadableByteChannel(String source) {
        try {
            return Channels.newChannel(new URL(source).openStream());
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    @Override
    public Flux<Slice> readFrom(String urlSource) {
        // "customers" of this Flux, better not to use parallelism, since reading from one input stream in parallel requires much more synchronization
        // and have too much places for the race conditions. ReadableByteChannel, uses synchronized during filling bytes into ByteBuffer, so all threads
        // will be stuck on "synchronized" block (reading from input stream). One of possible problems for parallel solution: when to close stream
        return Flux.generate(
            () -> new SliceChannelNioReader(factory.create(urlSource), bufferSizeBytes),
            this::readFromSliceChannelBuffer,
            SliceChannelReader::clear
        ).map(slice -> slice.toBuilder().url(urlSource).build())
                // we want on error return some ERROR object to filter it after that
                .onErrorReturn(Utils.errorSlice());
    }

    private SliceChannelReader readFromSliceChannelBuffer(SliceChannelReader slice, SynchronousSink<Slice> sink) {
        Slice read = slice.read();
        if (read == null) {
            sink.complete();
            slice.close();
        } else {
            sink.next(read);
            slice.clear();
        }

        return slice;
    }

    public interface ChannelCreatorFactory {
        ReadableByteChannel create(String source);
    }

    public interface SliceChannelReader {
        Slice read();
        void clear();
        void close();
    }

    // leaving package-level for test purpose
    static class SliceChannelNioReader implements SliceChannelReader {

        private ByteBuffer buffer;
        private ReadableByteChannel readableByteChannel;
        private AtomicInteger counter;

        SliceChannelNioReader(ReadableByteChannel readableByteChannel, int sizeBytes) {
            this.readableByteChannel = readableByteChannel;
            this.buffer = ByteBuffer.allocate(sizeBytes);
            this.counter = new AtomicInteger(0);
        }

        @Override
        public Slice read() {
            byte[] bytes = null;
            int read = 0;
            try {
                read = readableByteChannel.read(buffer);
            } catch (Exception e) {
                throw rethrow(e);
            }
            if (read != -1) {
                bytes = new byte[read];
                System.arraycopy(buffer.array(), 0, bytes, 0, read);
            }
            buffer.clear();

            int slice = this.counter.getAndIncrement();

            return bytes != null && bytes.length > 0 ?
                    Slice.builder().content(new String(bytes)).slice(slice).build() : null;
        }

        @Override
        public void clear() {
            try {
                buffer.clear();
            } catch (Exception ignore) {}
        }

        @Override
        public void close() {
            clear();
            try {
                readableByteChannel.close();
            } catch (Exception ignore) {}
        }
    }
}
