package com.atguigu.gmall0417.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {

    //@value注解可以直接从配置文件中取值，使用@value注解比如在spring容器中才能使用（因为这个类有controller，能被spring容器搜索
    @Value("${fileServer.url}")
    private String fileUrl;

    //springmvc:百度空间能够支持多数据上传MultipartFile
    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file)throws IOException, MyException {
        String imgUrl=fileUrl;
        if(file!=null){
            System.out.println("multipartFile = " + file.getName()+"|"+file.getSize());
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getConnection();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            //获取文件名称
            String filename=  file.getOriginalFilename();
            //获取后缀名
            //StringUtils：commons lang3的
            String extName = StringUtils.substringAfterLast(filename, ".");

            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl=fileUrl ;
            //s = group1
            //s = M00/00/00/wKjeglywP_2AV-7DAAAl_GXv6Z4127.jpg
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl+="/"+path;
            }

        }
        //回显图片的路径
        //要实现软编码而不是硬编码，便于改动，应该放在配置文件
        return imgUrl;
    }
}
