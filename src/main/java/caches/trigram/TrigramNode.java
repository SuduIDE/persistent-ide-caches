package caches.trigram;

import caches.GlobalVariables;
import caches.records.Revision;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public record TrigramNode(Revision revision, int parent, List<FileAction> fileActions) {

    public static int HEADER_BYTE_SIZE = 9;
    public static int TAIL_BYTE_SIZE = 4;

    public static TrigramNode readTrigramNode(RandomAccessFile raf) {
        try {
            raf.seek(raf.getFilePointer() - TAIL_BYTE_SIZE);
            var size = raf.readInt();
            raf.seek(raf.getFilePointer() - (long) FileAction.BYTE_SIZE * size - HEADER_BYTE_SIZE);
            var revision = new Revision(raf.readInt());
            var parent = raf.readInt();
            List<FileAction> fileActions = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                fileActions.add(FileAction.readFileAction(raf));
            }
            return new TrigramNode(revision, parent, fileActions);
        } catch (IOException e) {
            throw new RuntimeException("Error on reading node", e);
        }
    }

    byte[] toBytes() {
        var bytes = ByteBuffer.allocate(HEADER_BYTE_SIZE +
                        fileActions().size() * FileAction.BYTE_SIZE + TAIL_BYTE_SIZE)
                .putInt(revision.revision())
                .putInt(parent);
        fileActions.forEach(it -> it.putInBuffer(bytes));
        return bytes.putInt(fileActions.size()).array();
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

    public record FileAction(File file, Action action) {
        public static int BYTE_SIZE = 5;

        private void putInBuffer(ByteBuffer byteBuffer) {
            byteBuffer.putInt(GlobalVariables.reverseFilesInProject.get(file))
                    .put((byte) (action.state ? 1 : 0));
        }

        private static FileAction readFileAction(RandomAccessFile raf) {
            try {
                return new FileAction(GlobalVariables.filesInProject.get(raf.readInt()),
                        raf.readBoolean() ? Action.ADD : Action.DELETE);
            } catch (IOException e) {
                throw new RuntimeException("Error on reading FileAction", e);
            }
        }
    }
}
