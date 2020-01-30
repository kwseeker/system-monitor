package top.kwseeker.monitor.webapplogger.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/logback")
@Slf4j
public class LogGenController {

    private AtomicInteger logId = new AtomicInteger(1);

    //TODO: 后续把日志格式规范化，做成一个日志模板
    @GetMapping("/all")
    public String generateLog() {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            int logType = random.nextInt(16);
            if(logType >> 3 == 1) {         //权重8
                log.debug("Debug日志：logId={}", logId.getAndAdd(1));
            } else if (logType >> 2 == 1) { //权重4
                log.info("Info日志：logId={}", logId.getAndAdd(1));
            } else if (logType >> 1 == 1) { //权重2
                log.warn("Warn日志：logId={}", logId.getAndAdd(1));
            } else {                        //权重2
                log.error("Error日志：logId={}", logId.getAndAdd(1));
            }
        }
        return "done";
    }

}
