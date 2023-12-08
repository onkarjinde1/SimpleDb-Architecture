package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    private File file;
    private TupleDesc tupleDesc;

	public HeapFile(File f, TupleDesc td) {
        this.file = f;
        this.tupleDesc = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return tupleDesc; 
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        try {
            byte[] rawPgData = HeapPage.createEmptyPageData();
            int skipOffset = pid.getPageNumber() * BufferPool.getPageSize();
            // Read data from file
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(skipOffset);
                raf.read(rawPgData);
            }
            return new HeapPage(new HeapPageId(pid.getTableId(), pid.getPageNumber()), rawPgData);
        } catch (IOException e) {
            throw new IllegalArgumentException(" I/O error");
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // get total size of file in bytes
        long size = file.length();
        
        // calculate # pages that fit
        return (int) Math.ceil(size * 1.0 / BufferPool.getPageSize()); 
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    /**
     * Helper class that implements the DbFileIterator for HeapFiles.
     */
    public class HeapFileIterator implements DbFileIterator {

        private TransactionId tid;
        private HeapFile hf;
        private Iterator<Tuple> it;
        private int curpgno;
        private boolean open;

        public HeapFileIterator(HeapFile hf, TransactionId tid) {
            this.hf = hf;
            this.tid = tid;
            this.open = false;
        }

        public void open() throws DbException, TransactionAbortedException {
            curpgno = -1;
            open = true;
        }

        public boolean hasNext() throws DbException, TransactionAbortedException {

            if (it != null && !it.hasNext())
                it = null;

            if (it == null && curpgno < hf.numPages() - 1) {
                curpgno++;
                HeapPageId pid = new HeapPageId(hf.getId(), curpgno);
                HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid,
                        pid, Permissions.READ_ONLY);
                it = page.iterator();
            }

            return it != null && it.hasNext();
        }

        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (it == null || !it.hasNext())
                throw new NoSuchElementException();
            return it.next();
        }

        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        public void close() {
            it = null;
            curpgno = -1;
            open = false;
        }
    }  

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return new HeapFileIterator(this, tid);
    }

}

