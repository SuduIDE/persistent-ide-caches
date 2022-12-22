package caches.trigram;

import caches.GlobalVariables;
import caches.records.Revision;
import caches.records.Trigram;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record TrigramDataFileCluster(Revision revision, Revision parent, List<TrigramFileDelta> deltas) {

    private static final int HEADER_BYTE_SIZE = Integer.BYTES + Integer.BYTES + Integer.BYTES;
    private static final int RECORD_BYTE_SIZE = Character.BYTES * 3 + Integer.BYTES + Integer.BYTES;

    public static TrigramDataFileCluster readTrigramNode(RandomAccessFile raf) {
        try {
            var revision = new Revision(raf.readInt());
            var parent = new Revision(raf.readInt());
            var size = raf.readInt();
            List<TrigramFileDelta> deltas = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                deltas.add(TrigramFileDelta.read(raf));
            }
            return new TrigramDataFileCluster(revision, parent, deltas);
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        var bytes = ByteBuffer.allocate(HEADER_BYTE_SIZE + deltas().size() * RECORD_BYTE_SIZE)
                .putInt(revision.revision())
                .putInt(parent.revision())
                .putInt(deltas.size());
        deltas.forEach(it -> it.putInBuffer(bytes));
        return bytes.array();
    }

    public enum Action {
        ADD(true),
        DELETE(false);

        private final boolean state;

        Action(boolean state) {
            this.state = state;
        }

        public boolean getState() {
            return state;
        }
    }

    public record TrigramFileAction(Trigram trigram, File file, Action action) {
        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram().getBytes());
            byteBuffer.putInt(GlobalVariables.reverseFilesInProject.get(file));
            byteBuffer.put((byte) (action.state ? 1 : 0));
        }

        private static TrigramFileAction read(RandomAccessFile raf) throws IOException {
            var trigram = new Trigram(new String(new char[]{raf.readChar(), raf.readChar(), raf.readChar()}));
            var file = GlobalVariables.filesInProject.get(raf.readInt());
            var action = raf.readBoolean() ? Action.ADD : Action.DELETE;
            return new TrigramFileAction(trigram, file, action);
        }
    }

    public record TrigramFileDelta(Trigram trigram, File file, int delta) {
        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.put(trigram.trigram().getBytes());
            byteBuffer.putInt(GlobalVariables.reverseFilesInProject.get(file));
            byteBuffer.putInt(delta);
        }

        private static TrigramFileDelta read(RandomAccessFile raf) throws IOException {
            var trigram = new Trigram(new String(new char[]{raf.readChar(), raf.readChar(), raf.readChar()}));
            var file = GlobalVariables.filesInProject.get(raf.readInt());
            var delta = raf.readInt();
            return new TrigramFileDelta(trigram, file, delta);
        }
    }
}
