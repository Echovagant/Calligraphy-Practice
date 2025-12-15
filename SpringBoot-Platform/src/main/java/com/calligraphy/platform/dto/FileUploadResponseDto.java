package com.calligraphy.platform.dto;

public class FileUploadResponseDto {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private String message;
    
    // 鏃犲弬鏋勯€犲櫒
    public FileUploadResponseDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
    public FileUploadResponseDto(String fileName, String fileUrl, String fileType, long fileSize, String message) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.message = message;
    }
    
    // Getter 鍜?Setter 鏂规硶
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}


