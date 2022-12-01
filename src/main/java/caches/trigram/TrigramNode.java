package caches.trigram;

import caches.GlobalVariables;
import caches.records.Revision;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public record TrigramNode(Revision revision, int parent, Action action) {

    public static int BYTE_SIZE = 9;

    public static TrigramNode readTrigramNode(RandomAccessFile raf) {
        try {
            return new TrigramNode(new Revision(raf.readInt()),
                    raf.readInt(),
//                    GlobalVariables.filesInProject.get(raf.readInt()),
                    raf.readBoolean() ? Action.ADD : Action.DELETE
            );
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        return ByteBuffer.allocate(BYTE_SIZE)
                .putInt(revision.revision())
                .putInt(parent)
//                .putInt(GlobalVariables.reverseFilesInProject.get(file))
                .put((byte) (action.state ? 1 : 0))
                .array();

    }

    enum Action {
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
}
