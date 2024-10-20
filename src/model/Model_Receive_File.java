/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author mrtru
 */
public class Model_Receive_File {
    private int fileID;
    private long fileSize; // in bytes


    public Model_Receive_File() {
    }

    public Model_Receive_File(int fileID, long fileSize) {
        this.fileID = fileID;
        this.fileSize = fileSize;

    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }



    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    
}
