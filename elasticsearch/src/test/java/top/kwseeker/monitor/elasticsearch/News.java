package top.kwseeker.monitor.elasticsearch;

import lombok.Data;

@Data
public class News {
   private String id;
   private String newsId;
   private String newsTitle;
   private String newsContent;
   private int readCount;
}
