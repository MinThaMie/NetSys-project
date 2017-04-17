package com.nedap.university;

import com.nedap.university.utils.TerminalOutput;
import com.nedap.university.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by anne-greeth.vanherwijnen on 16/04/2017.
 */
public class fileListTest {
    private String filePath = "files";
    private File file;
    private String[] files;
    @Before
    public void SetUp(){
        file = new File(filePath);
        files = file.list();
    }

    @Test
    public void fileListTest(){
        assertThat(files.length, is(3));
    }

    @Test
    public void arrayToString(){
        String fileString = Utils.stringArrayToString(files);
        TerminalOutput.showFiles(fileString);
    }
}
