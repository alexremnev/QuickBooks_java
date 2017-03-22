package com.controller;

import com.intuit.ipp.util.StringUtils;
import com.service.QueueService;
import com.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebhooksController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebhooksController.class);
    private static final String SIGNATURE = "intuit-signature";
    private SecurityService securityService;
    private QueueService queueService;

    @Autowired
    public WebhooksController(SecurityService securityService, QueueService queueService) {
        this.queueService = queueService;
        this.securityService = securityService;
    }

    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> webhooks(@RequestHeader(SIGNATURE) String signature, @RequestBody String payload) {
        try {
            if (!StringUtils.hasText(signature)) {
                return new ResponseEntity<>("ERROR", HttpStatus.FORBIDDEN);
            }
            if (!StringUtils.hasText(payload)) {
                new ResponseEntity<>("SUCCESS", HttpStatus.OK);
            }
            LOGGER.info("request recieved ");
            if (securityService.isRequestValid(signature, payload)) {
                queueService.add(payload);
            } else {
                return new ResponseEntity<>("ERROR", HttpStatus.FORBIDDEN);
            }
            LOGGER.info("response sent ");
            return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Exception occured when application tried to handle incoming notifications", e.getCause());
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
