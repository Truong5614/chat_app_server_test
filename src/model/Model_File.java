/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
/**
 *
 * @author mrtru
 */
public class Model_File {
    private int fileID;
    private String fileName;
    private String fileExtension;
    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Model_File(int fileID, String fileName, String fileExtension) {
        this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
    }

    public Model_File() {
    }


}
